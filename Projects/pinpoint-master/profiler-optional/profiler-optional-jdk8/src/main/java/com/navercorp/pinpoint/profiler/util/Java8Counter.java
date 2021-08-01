/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java8Counter implements Counter {
    private final LongAdder longAdder = new LongAdder();
    @Override
    public void increment() {
        longAdder.increment();
    }

    @Override
    public void add(long x) {
        longAdder.add(x);
    }

    @Override
    public long longValue() {
        return longAdder.longValue();
    }
}
