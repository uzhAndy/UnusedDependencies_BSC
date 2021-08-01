/*
Copyright 2019 The Vitess Authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package mysql

import (
	"bytes"
	"crypto/subtle"
	"crypto/x509"
	"encoding/json"
	"flag"
	"io/ioutil"
	"net"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"vitess.io/vitess/go/vt/log"
	querypb "vitess.io/vitess/go/vt/proto/query"
	"vitess.io/vitess/go/vt/proto/vtrpc"
	"vitess.io/vitess/go/vt/vterrors"
)

var (
	mysqlAuthServerStaticFile           = flag.String("mysql_auth_server_static_file", "", "JSON File to read the users/passwords from.")
	mysqlAuthServerStaticString         = flag.String("mysql_auth_server_static_string", "", "JSON representation of the users/passwords config.")
	mysqlAuthServerStaticReloadInterval = flag.Duration("mysql_auth_static_reload_interval", 0, "Ticker to reload credentials")
)

const (
	localhostName = "localhost"
)

// AuthServerStatic implements AuthServer using a static configuration.
type AuthServerStatic struct {
	methods          []AuthMethod
	file, jsonConfig string
	reloadInterval   time.Duration
	// This mutex helps us prevent data races between the multiple updates of entries.
	mu sync.Mutex
	// entries contains the users, passwords and user data.
	entries map[string][]*AuthServerStaticEntry

	sigChan chan os.Signal
	ticker  *time.Ticker
}

// AuthServerStaticEntry stores the values for a given user.
type AuthServerStaticEntry struct {
	// MysqlNativePassword is generated by password hashing methods in MySQL.
	// These changes are illustrated by changes in the result from the PASSWORD() function
	// that computes password hash values and in the structure of the user table where passwords are stored.
	// mysql> SELECT PASSWORD('mypass');
	// +-------------------------------------------+
	// | PASSWORD('mypass')                        |
	// +-------------------------------------------+
	// | *6C8989366EAF75BB670AD8EA7A7FC1176A95CEF4 |
	// +-------------------------------------------+
	// MysqlNativePassword's format looks like "*6C8989366EAF75BB670AD8EA7A7FC1176A95CEF4", it store a hashing value.
	// Use MysqlNativePassword in auth config, maybe more secure. After all, it is cryptographic storage.
	MysqlNativePassword string
	Password            string
	UserData            string
	SourceHost          string
	Groups              []string
}

// InitAuthServerStatic Handles initializing the AuthServerStatic if necessary.
func InitAuthServerStatic() {
	// Check parameters.
	if *mysqlAuthServerStaticFile == "" && *mysqlAuthServerStaticString == "" {
		// Not configured, nothing to do.
		log.Infof("Not configuring AuthServerStatic, as mysql_auth_server_static_file and mysql_auth_server_static_string are empty")
		return
	}
	if *mysqlAuthServerStaticFile != "" && *mysqlAuthServerStaticString != "" {
		// Both parameters specified, can only use one.
		log.Exitf("Both mysql_auth_server_static_file and mysql_auth_server_static_string specified, can only use one.")
	}

	// Create and register auth server.
	RegisterAuthServerStaticFromParams(*mysqlAuthServerStaticFile, *mysqlAuthServerStaticString, *mysqlAuthServerStaticReloadInterval)
}

// RegisterAuthServerStaticFromParams creates and registers a new
// AuthServerStatic, loaded for a JSON file or string. If file is set,
// it uses file. Otherwise, load the string. It log.Exits out in case
// of error.
func RegisterAuthServerStaticFromParams(file, jsonConfig string, reloadInterval time.Duration) {
	authServerStatic := NewAuthServerStatic(file, jsonConfig, reloadInterval)
	if len(authServerStatic.entries) <= 0 {
		log.Exitf("Failed to populate entries from file: %v", file)
	}
	RegisterAuthServer("static", authServerStatic)
}

// NewAuthServerStatic returns a new empty AuthServerStatic.
func NewAuthServerStatic(file, jsonConfig string, reloadInterval time.Duration) *AuthServerStatic {
	a := &AuthServerStatic{
		file:           file,
		jsonConfig:     jsonConfig,
		reloadInterval: reloadInterval,
		entries:        make(map[string][]*AuthServerStaticEntry),
	}

	a.methods = []AuthMethod{NewMysqlNativeAuthMethod(a, a)}

	a.reload()
	a.installSignalHandlers()
	return a
}

// NewAuthServerStaticWithAuthMethodDescription returns a new empty AuthServerStatic
// but with support for a different auth method. Mostly used for testing purposes.
func NewAuthServerStaticWithAuthMethodDescription(file, jsonConfig string, reloadInterval time.Duration, authMethodDescription AuthMethodDescription) *AuthServerStatic {
	a := &AuthServerStatic{
		file:           file,
		jsonConfig:     jsonConfig,
		reloadInterval: reloadInterval,
		entries:        make(map[string][]*AuthServerStaticEntry),
	}

	var authMethod AuthMethod
	switch authMethodDescription {
	case CachingSha2Password:
		authMethod = NewSha2CachingAuthMethod(a, a, a)
	case MysqlNativePassword:
		authMethod = NewMysqlNativeAuthMethod(a, a)
	case MysqlClearPassword:
		authMethod = NewMysqlClearAuthMethod(a, a)
	case MysqlDialog:
		authMethod = NewMysqlDialogAuthMethod(a, a, "")
	}

	a.methods = []AuthMethod{authMethod}

	a.reload()
	a.installSignalHandlers()
	return a
}

// HandleUser is part of the Validator interface. We
// handle any user here since we don't check up front.
func (a *AuthServerStatic) HandleUser(user string) bool {
	return true
}

// UserEntryWithPassword implements password lookup based on a plain
// text password that is negotiated with the client.
func (a *AuthServerStatic) UserEntryWithPassword(userCerts []*x509.Certificate, user string, password string, remoteAddr net.Addr) (Getter, error) {
	a.mu.Lock()
	entries, ok := a.entries[user]
	a.mu.Unlock()

	if !ok {
		return &StaticUserData{}, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
	}

	for _, entry := range entries {
		// Validate the password.
		if matchSourceHost(remoteAddr, entry.SourceHost) && subtle.ConstantTimeCompare([]byte(password), []byte(entry.Password)) == 1 {
			return &StaticUserData{entry.UserData, entry.Groups}, nil
		}
	}
	return &StaticUserData{}, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
}

// UserEntryWithHash implements password lookup based on a
// mysql_native_password hash that is negotiated with the client.
func (a *AuthServerStatic) UserEntryWithHash(userCerts []*x509.Certificate, salt []byte, user string, authResponse []byte, remoteAddr net.Addr) (Getter, error) {
	a.mu.Lock()
	entries, ok := a.entries[user]
	a.mu.Unlock()

	if !ok {
		return &StaticUserData{}, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
	}

	for _, entry := range entries {
		if entry.MysqlNativePassword != "" {
			hash, err := DecodeMysqlNativePasswordHex(entry.MysqlNativePassword)
			if err != nil {
				return &StaticUserData{entry.UserData, entry.Groups}, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
			}

			isPass := VerifyHashedMysqlNativePassword(authResponse, salt, hash)
			if matchSourceHost(remoteAddr, entry.SourceHost) && isPass {
				return &StaticUserData{entry.UserData, entry.Groups}, nil
			}
		} else {
			computedAuthResponse := ScrambleMysqlNativePassword(salt, []byte(entry.Password))
			// Validate the password.
			if matchSourceHost(remoteAddr, entry.SourceHost) && subtle.ConstantTimeCompare(authResponse, computedAuthResponse) == 1 {
				return &StaticUserData{entry.UserData, entry.Groups}, nil
			}
		}
	}
	return &StaticUserData{}, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
}

// UserEntryWithCacheHash implements password lookup based on a
// caching_sha2_password hash that is negotiated with the client.
func (a *AuthServerStatic) UserEntryWithCacheHash(userCerts []*x509.Certificate, salt []byte, user string, authResponse []byte, remoteAddr net.Addr) (Getter, CacheState, error) {
	a.mu.Lock()
	entries, ok := a.entries[user]
	a.mu.Unlock()

	if !ok {
		return &StaticUserData{}, AuthRejected, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
	}

	for _, entry := range entries {
		computedAuthResponse := ScrambleCachingSha2Password(salt, []byte(entry.Password))

		// Validate the password.
		if matchSourceHost(remoteAddr, entry.SourceHost) && subtle.ConstantTimeCompare(authResponse, computedAuthResponse) == 1 {
			return &StaticUserData{entry.UserData, entry.Groups}, AuthAccepted, nil
		}
	}
	return &StaticUserData{}, AuthRejected, NewSQLError(ERAccessDeniedError, SSAccessDeniedError, "Access denied for user '%v'", user)
}

// AuthMethods returns the AuthMethod instances this auth server can handle.
func (a *AuthServerStatic) AuthMethods() []AuthMethod {
	return a.methods
}

// DefaultAuthMethodDescription returns the default auth method in the handshake which
// is MysqlNativePassword for this auth server.
func (a *AuthServerStatic) DefaultAuthMethodDescription() AuthMethodDescription {
	return MysqlNativePassword
}

func (a *AuthServerStatic) reload() {
	jsonBytes := []byte(a.jsonConfig)
	if a.file != "" {
		data, err := ioutil.ReadFile(a.file)
		if err != nil {
			log.Errorf("Failed to read mysql_auth_server_static_file file: %v", err)
			return
		}
		jsonBytes = data
	}

	entries := make(map[string][]*AuthServerStaticEntry)
	if err := parseConfig(jsonBytes, &entries); err != nil {
		log.Errorf("Error parsing auth server config: %v", err)
		return
	}

	a.mu.Lock()
	a.entries = entries
	a.mu.Unlock()
}

func (a *AuthServerStatic) installSignalHandlers() {
	if a.file == "" {
		return
	}

	a.sigChan = make(chan os.Signal, 1)
	signal.Notify(a.sigChan, syscall.SIGHUP)
	go func() {
		for range a.sigChan {
			a.reload()
		}
	}()

	// If duration is set, it will reload configuration every interval
	if a.reloadInterval > 0 {
		a.ticker = time.NewTicker(a.reloadInterval)
		go func() {
			for range a.ticker.C {
				a.sigChan <- syscall.SIGHUP
			}
		}()
	}
}

func (a *AuthServerStatic) close() {
	if a.ticker != nil {
		a.ticker.Stop()
	}
	if a.sigChan != nil {
		signal.Stop(a.sigChan)
	}
}

func parseConfig(jsonBytes []byte, config *map[string][]*AuthServerStaticEntry) error {
	decoder := json.NewDecoder(bytes.NewReader(jsonBytes))
	decoder.DisallowUnknownFields()
	if err := decoder.Decode(config); err != nil {
		// Couldn't parse, will try to parse with legacy config
		return parseLegacyConfig(jsonBytes, config)
	}
	return validateConfig(*config)
}

func parseLegacyConfig(jsonBytes []byte, config *map[string][]*AuthServerStaticEntry) error {
	// legacy config doesn't have an array
	legacyConfig := make(map[string]*AuthServerStaticEntry)
	decoder := json.NewDecoder(bytes.NewReader(jsonBytes))
	decoder.DisallowUnknownFields()
	if err := decoder.Decode(&legacyConfig); err != nil {
		return err
	}
	log.Warningf("Config parsed using legacy configuration. Please update to the latest format: {\"user\":[{\"Password\": \"xxx\"}, ...]}")
	for key, value := range legacyConfig {
		(*config)[key] = append((*config)[key], value)
	}
	return nil
}

func validateConfig(config map[string][]*AuthServerStaticEntry) error {
	for _, entries := range config {
		for _, entry := range entries {
			if entry.SourceHost != "" && entry.SourceHost != localhostName {
				return vterrors.Errorf(vtrpc.Code_INVALID_ARGUMENT, "invalid SourceHost found (only localhost is supported): %v", entry.SourceHost)
			}
		}
	}
	return nil
}

func matchSourceHost(remoteAddr net.Addr, targetSourceHost string) bool {
	// Legacy support, there was not matcher defined default to true
	if targetSourceHost == "" {
		return true
	}
	switch remoteAddr.(type) {
	case *net.UnixAddr:
		if targetSourceHost == localhostName {
			return true
		}
	}
	return false
}

// StaticUserData holds the username and groups
type StaticUserData struct {
	username string
	groups   []string
}

// Get returns the wrapped username and groups
func (sud *StaticUserData) Get() *querypb.VTGateCallerID {
	return &querypb.VTGateCallerID{Username: sud.username, Groups: sud.groups}
}
