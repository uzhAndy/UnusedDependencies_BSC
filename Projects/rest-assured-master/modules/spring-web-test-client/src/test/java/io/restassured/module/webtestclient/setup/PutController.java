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

package io.restassured.module.webtestclient.setup;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
public class PutController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @PutMapping(value = "/greetingPut", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<Greeting> greeting(@RequestParam("name") String name) {
        return Mono.just(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }

    @PutMapping("/stringBody")
    public Mono<String> stringBody(@RequestBody String body) {
        return Mono.just(body);
    }

    @PutMapping(value = "/jsonReflect", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Mono<String> jsonReflect(@RequestBody String body) {
        return Mono.just(body);
    }

    @PutMapping(value = "/multipartFileUpload", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public Mono<String> multipartFileUpload(@RequestPart("file") Mono<FilePart> file) {
        return Mono.just(file.toString());
    }
}