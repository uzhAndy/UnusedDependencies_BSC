/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.exception;

/**
 * Represents an unexpected error during Java code generation.
 */
public class GenerationException extends RuntimeException {

    private static final long serialVersionUID = -2105441912033842653L;

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(Throwable cause) {
        super(cause);
    }

    public GenerationException(String message, ClassNotFoundException cause) {
        super(message, cause);
    }
}
