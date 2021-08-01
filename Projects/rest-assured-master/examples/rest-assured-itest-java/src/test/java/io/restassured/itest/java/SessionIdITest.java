/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.restassured.itest.java;

import io.restassured.RestAssured;
import io.restassured.config.SessionConfig;
import io.restassured.itest.java.support.WithJetty;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static io.restassured.config.SessionConfig.DEFAULT_SESSION_ID_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class SessionIdITest extends WithJetty {

    @Test
    public void settingSessionIdThroughTheDSLConfig() {
        given().config(newConfig().sessionConfig(new SessionConfig("1234"))).then().expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
    }

    @Test
    public void settingSessionIdThroughTheDSL() {
        given().sessionId("1234").then().expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
    }

    @Test
    public void settingSessionIdThroughTheDSLHasPrecedenceOverTheConfig() {
        given().config(newConfig().sessionConfig(new SessionConfig("1235"))).and().sessionId("1234").then().expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
    }

    @Test
    public void settingSessionIdThroughTheDSLHasPrecedenceOverTheStaticConfig() {
        RestAssured.config = newConfig().sessionConfig(new SessionConfig("1235"));
        try {
            given().sessionId("1234").then().expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
        } finally {
            RestAssured.reset();
        }
    }

    @Test
    public void settingSessionIdThroughStaticConfig() {
        RestAssured.config = newConfig().sessionConfig(new SessionConfig("1234"));

        try {
            expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
        } finally {
            RestAssured.reset();
        }
    }

    @Test
    public void settingSessionIdNameThroughTheDSLOverridesTheSessionIdInTheDefaultSessionConfig() {
        RestAssured.config = newConfig().sessionConfig(new SessionConfig("phpsessionid", "12345"));

        try {
            given().sessionId("jsessionid", "1234").then().expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
        } finally {
            RestAssured.reset();
        }
    }

    @Test
    public void settingSessionIdNameThroughTheDSLWorks() {
        given().sessionId("phpsessionid", "1234").then().expect().cookie(DEFAULT_SESSION_ID_NAME, "1234").when().get("/sessionId");
    }

    @Test
    public void settingSessionIdStaticallyWorks() {
        RestAssured.sessionId = "1234";

        try {
            expect().that().body(is(equalTo("Success"))).when().get("/sessionId");
        } finally {
            RestAssured.reset();
        }
    }

    @Test
    public void settingTheSessionIdTwiceOverwritesTheFirstOne() {
        given().sessionId("1234").sessionId("1235").expect().statusLine("HTTP/1.1 409 Conflict").when().get("/sessionId");
    }

    @Test
    public void restAssuredResponseSupportsGettingTheSessionId() {
        final String sessionId = get("/sessionId").sessionId();

        assertThat(sessionId, equalTo("1234"));
    }

    @Test
    public void sessionIdReturnsNullWhenNoCookiesAreDefined() {
        final String sessionId = get("/shopping").sessionId();

        assertThat(sessionId, nullValue());
    }
}
