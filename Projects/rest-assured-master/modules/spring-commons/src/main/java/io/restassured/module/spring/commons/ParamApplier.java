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
package io.restassured.module.spring.commons;

import java.util.Collection;
import java.util.Map;

public abstract class ParamApplier {

    private Map<String, Object> map;

    protected ParamApplier(Map<String, Object> parameters) {
        this.map = parameters;
    }

    public void applyParams() {
        for (Map.Entry<String, Object> listEntry : map.entrySet()) {
            Object value = listEntry.getValue();
            String[] stringValues;
            if (value instanceof Collection) {
                Collection col = (Collection) value;
                stringValues = new String[col.size()];
                int index = 0;
                for (Object val : col) {
                    stringValues[index] = val == null ? null : val.toString();
                    index++;
                }
            } else {
                stringValues = new String[1];
                stringValues[0] = value == null ? null : value.toString();
            }
            applyParam(listEntry.getKey(), stringValues);
        }
    }

    protected abstract void applyParam(String paramName, String[] paramValues);
}
