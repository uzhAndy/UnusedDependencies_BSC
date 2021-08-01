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

import io.restassured.module.webtestclient.setup.PostController;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class WebTestClientResponseTimeTest {

	@Test
	public void
	can_use_response_time_validation_in_web_test_client_extension() {
		RestAssuredWebTestClient.given()
				.standaloneSetup(new PostController())
				.param("name", "Johan")
				.when()
				.post("/greetingPost")
				.then()
				.time(lessThan(3L), SECONDS)
				.statusCode(is(200));
	}

	@Test
	public void
	can_extract_response_time_in_web_test_client_extension() {
		long time =
				RestAssuredWebTestClient.given()
						.standaloneSetup(new PostController())
						.param("name", "Johan")
						.when()
						.post("/greetingPost")
						.then()
						.extract().time();

		assertThat(time).isGreaterThan(-1).isLessThan(3000L);
	}
}
