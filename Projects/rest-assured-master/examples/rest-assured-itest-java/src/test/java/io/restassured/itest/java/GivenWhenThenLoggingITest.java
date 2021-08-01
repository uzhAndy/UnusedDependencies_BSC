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

import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.itest.java.support.WithJetty;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import java.io.PrintStream;
import java.io.StringWriter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

public class GivenWhenThenLoggingITest extends WithJetty {

    @Test
    public void logsEverythingResponseUsingGivenWhenThenSyntax() throws Exception {
        final StringWriter writer = new StringWriter();
        final PrintStream captor = new PrintStream(new WriterOutputStream(writer), true);

        given().
                config(RestAssuredConfig.config().logConfig(LogConfig.logConfig().defaultStream(captor).and().enablePrettyPrinting(false))).
                pathParam("firstName", "John").
                pathParam("lastName", "Doe").
        when().
                get("/{firstName}/{lastName}").
        then().
                log().all().
                body("fullName", equalTo("John Doe"));

        assertThat(writer.toString(), equalTo(String.format("HTTP/1.1 200 OK%n" +
                "Content-Type: application/json;charset=utf-8%n" +
                "Content-Length: 59%n" +
                "Server: Jetty(9.4.34.v20201102)%n" +
                "%n" +
                "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"fullName\":\"John Doe\"}%n")));
    }

    @Test
    public void logResponseThatHasCookiesWithLogDetailCookiesUsingGivenWhenThenSyntax() throws Exception {
        final StringWriter writer = new StringWriter();
        final PrintStream captor = new PrintStream(new WriterOutputStream(writer), true);
        given().
                config(RestAssuredConfig.config().logConfig(LogConfig.logConfig().defaultStream(captor).and().enablePrettyPrinting(false))).
        when().
                get("/multiCookie").
        then().
                log().cookies().
                body(equalTo("OK"));
        assertThat(writer.toString(), startsWith("cookie1=cookieValue1;Domain=localhost\ncookie1=cookieValue2;Path=/;Domain=localhost;Max-Age=1234567;Secure;Expires="));
    }

    @Test
    public void logOnlyHeadersUsingResponseUsingLogSpecWithGivenWhenThenSyntax() throws Exception {
        final StringWriter writer = new StringWriter();
        final PrintStream captor = new PrintStream(new WriterOutputStream(writer), true);

        given().
                config(RestAssuredConfig.config().logConfig(new LogConfig(captor, true))).
                pathParam("firstName", "John").
                pathParam("lastName", "Doe").
        when().
                get("/{firstName}/{lastName}").
        then().
                log().headers().
                body("fullName", equalTo("John Doe"));


        assertThat(writer.toString(), equalTo(String.format("Content-Type: application/json;charset=utf-8%n" +
                "Content-Length: 59%n" +
                "Server: Jetty(9.4.34.v20201102)%n")));
    }
}
