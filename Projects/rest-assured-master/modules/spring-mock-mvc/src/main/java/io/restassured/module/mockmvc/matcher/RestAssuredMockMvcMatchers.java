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

package io.restassured.module.mockmvc.matcher;

import io.restassured.internal.matcher.xml.XmlDtdMatcher;
import io.restassured.internal.matcher.xml.XmlXsdMatcher;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.matcher.RestAssuredMatchers;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import static org.hamcrest.Matchers.*;

/**
 * Providers Hamcrest matchers that may be useful when validating a MockMvc response.
 */
public class RestAssuredMockMvcMatchers {

    /**
     * Evaluates to true if an XML string matches the supplied XSD (Xml Schema).
     *
     * @param xsd The XSD to match
     * @return The XSD matcher
     */
    public static Matcher<String> matchesXsd(String xsd) {
        return RestAssuredMatchers.matchesXsd(xsd);
    }

    /**
     * Evaluates to true if an XML string matches the supplied XSD (Xml Schema).
     *
     * @param xsd The XSD to match
     * @return The XSD matcher
     */
    public static Matcher<String> matchesXsd(InputStream xsd) {
        return RestAssuredMatchers.matchesXsd(xsd);
    }

    /**
     * Evaluates to true if an XML string matches the supplied XSD (Xml Schema).
     *
     * @param xsd The XSD to match
     * @return The XSD matcher
     */
    public static Matcher<String> matchesXsd(Reader xsd) {
        return RestAssuredMatchers.matchesXsd(xsd);
    }

    /**
     * Evaluates to true if an XML string matches the supplied XSD (Xml Schema).
     *
     * @param xsd The XSD to match
     * @return The XSD matcher
     */
    public static Matcher<String> matchesXsd(File xsd) {
        return RestAssuredMatchers.matchesXsd(xsd);
    }

    /**
     * Evaluates to true if an XML file in classpath matches the supplied XSD.
     *
     * @param path The path to the XSD located in classpath
     * @return The DTD matcher
     */
    public static Matcher<String> matchesXsdInClasspath(String path) {
        return XmlXsdMatcher.matchesXsdInClasspath(path);
    }

    /**
     * Evaluates to true if an XML string matches the supplied DTD.
     *
     * @param dtd The DTD to match
     * @return The DTD matcher
     */
    public static Matcher<String> matchesDtd(String dtd) {
        return RestAssuredMatchers.matchesDtd(dtd);
    }

    /**
     * Evaluates to true if an XML string matches the supplied DTD.
     *
     * @param dtd The DTD to match
     * @return The DTD matcher
     */
    public static Matcher<String> matchesDtd(InputStream dtd) {
        return RestAssuredMatchers.matchesDtd(dtd);
    }

    /**
     * Evaluates to true if an XML string matches the supplied DTD.
     *
     * @param dtd The DTD to match
     * @return The DTD matcher
     */
    public static Matcher<String> matchesDtd(File dtd) {
        return RestAssuredMatchers.matchesDtd(dtd);
    }

    /**
     * Evaluates to true if an XML string matches the supplied DTD.
     *
     * @param url The DTD to match
     * @return The DTD matcher
     */
    public static Matcher<String> matchesDtd(URL url) {
        return RestAssuredMatchers.matchesDtd(url);
    }

    /**
     * Evaluates to true if an XML file in classpath matches the supplied DTD.
     *
     * @param path The path to the DTD file in classpath
     * @return The DTD matcher
     */
    public static Matcher<String> matchesDtdInClasspath(String path) {
        return XmlDtdMatcher.matchesDtdInClasspath(path);
    }

    /**
     * Creates a {@link ResponseAwareMatcher} that extracts the given path from the response and
     * wraps it in a {@link org.hamcrest.Matchers#equalTo(Object)} matcher. This is useful if you have a resource that e.g. returns the given JSON:
     * <pre>
     * {
     *      "userId" : "my-id",
     *      "playerId" : "my-id"
     * }
     * </pre>
     * you can then test it like this:
     * <pre>
     * get("/x").then().body("userId", equalToPath("playerId"));
     * </pre>
     *
     * @param path The path to check
     * @return A {@link ResponseAwareMatcher}
     */
    public static ResponseAwareMatcher<MockMvcResponse> equalToPath(final String path) {
        return new ResponseAwareMatcher<MockMvcResponse>() {
            public Matcher<?> matcher(MockMvcResponse response) {
                return equalTo(response.path(path));
            }
        };
    }

    /**
     * Creates a {@link ResponseAwareMatcher} that extracts the given path from the response and
     * wraps it in a {@link org.hamcrest.Matchers#equalTo(Object)} matcher. This is useful if you have a resource that e.g. returns the given JSON:
     * <pre>
     * {
     *      "userId" : "my-id",
     *      "href" : "http://localhost:8080/my-id"
     * }
     * </pre>
     * you can then test it like this:
     * <pre>
     * get("/x").then().body("href", endsWithPath("userId"));
     * </pre>
     *
     * @param path The path to check
     * @return A {@link ResponseAwareMatcher}
     */
    public static ResponseAwareMatcher<MockMvcResponse> endsWithPath(final String path) {
        return new ResponseAwareMatcher<MockMvcResponse>() {
            public Matcher<?> matcher(MockMvcResponse response) {
                return endsWith(response.<String>path(path));
            }
        };
    }

    /**
     * Creates a {@link ResponseAwareMatcher} that extracts the given path from the response and
     * wraps it in a {@link org.hamcrest.Matchers#equalTo(Object)} matcher. This is useful if you have a resource that e.g. returns the given JSON:
     * <pre>
     * {
     *      "userId" : "my-id",
     *      "baseUri" : "http://localhost:8080",
     *      "href" : "http://localhost:8080/my-id"
     * }
     * </pre>
     * you can then test it like this:
     * <pre>
     * get("/x").then().body("href", startsWithPath("baseUri"));
     * </pre>
     *
     * @param path The path to check
     * @return A {@link ResponseAwareMatcher}
     */
    public static ResponseAwareMatcher<MockMvcResponse> startsWithPath(final String path) {
        return new ResponseAwareMatcher<MockMvcResponse>() {
            public Matcher<?> matcher(MockMvcResponse response) {
                return startsWith(response.<String>path(path));
            }
        };
    }

    /**
     * Creates a {@link ResponseAwareMatcher} that extracts the given path from the response and
     * wraps it in a {@link org.hamcrest.Matchers#equalTo(Object)} matcher. This is useful if you have a resource that e.g. returns the given JSON:
     * <pre>
     * {
     *      "userId" : "my-id",
     *      "href" : "http://localhost:8080/my-id"
     * }
     * </pre>
     * you can then test it like this:
     * <pre>
     * get("/x").then().body("href", containsPath("userId"));
     * </pre>
     *
     * @param path The path to check
     * @return A {@link ResponseAwareMatcher}
     */
    public static ResponseAwareMatcher<MockMvcResponse> containsPath(final String path) {
        return new ResponseAwareMatcher<MockMvcResponse>() {
            public Matcher<?> matcher(MockMvcResponse response) {
                return containsString(response.<String>path(path));
            }
        };
    }
}