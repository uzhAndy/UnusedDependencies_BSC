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

package io.restassured.module.webtestclient;

import io.restassured.config.LogConfig;
import io.restassured.module.webtestclient.setup.PutController;
import io.restassured.module.webtestclient.setup.support.Greeting;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.webtestclient.config.RestAssuredWebTestClientConfig.newConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PutTest {

  @BeforeClass
	public static void configureWebTestClientInstance() {
		RestAssuredWebTestClient.webTestClient(WebTestClient.bindToController(new PutController()).build());
	}

	@AfterClass
	public static void restRestAssured() {
		RestAssuredWebTestClient.reset();
	}

	@Test
	public void
	doesnt_automatically_add_x_www_form_urlencoded_as_content_type_when_putting_params() {
		StringWriter writer = new StringWriter();
		PrintStream captor = new PrintStream(new WriterOutputStream(writer), true);

		RestAssuredWebTestClient.given()
				.config(newConfig().logConfig(new LogConfig(captor, true)))
				.param("name", "Johan")
				.when()
				.put("/greetingPut")
				.then()
				.log().all()
				.statusCode(415);

		assertThat(writer.toString(), equalTo(String.format("415: Unsupported Media Type%n")));
	}

	@Test
	public void
	automatically_adds_x_www_form_urlencoded_as_content_type_when_putting_form_params() {
		RestAssuredWebTestClient.given()
				.formParam("name", "Johan")
				.when()
				.put("/greetingPut")
				.then()
				.body("id", equalTo(1))
				.body("content", equalTo("Hello, Johan!"));
	}

	@Test
	public void
	can_supply_string_as_body_for_put() {
		RestAssuredWebTestClient.given()
				.body("a string")
				.when()
				.put("/stringBody")
				.then()
				.body(equalTo("a string"));
	}

	@Test
	public void
	can_supply_object_as_body_and_serialize_as_json() {
		Greeting greeting = new Greeting();
		greeting.setFirstName("John");
		greeting.setLastName("Doe");

		RestAssuredWebTestClient.given()
				.contentType(JSON)
				.body(greeting)
				.when()
				.put("/jsonReflect")
				.then()
				.statusCode(200)
				.body("firstName", equalTo("John"))
				.body("lastName", equalTo("Doe"));
	}

	@Test
	public void
	can_supply_multipart_file_as_parameter_for_put() throws IOException {
		File file = new File("rest-assured.txt");
		RestAssuredWebTestClient.given()
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.multiPart(file)
				.when()
				.put("/multipartFileUpload")
				.then()
				.statusCode(200)
				.log().all();
		file.delete();
	}
}
