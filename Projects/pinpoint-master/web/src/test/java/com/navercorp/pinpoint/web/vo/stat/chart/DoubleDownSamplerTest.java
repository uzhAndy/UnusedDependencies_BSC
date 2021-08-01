/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.stat.chart;

import org.junit.Assert;

/**
 * @author HyunGil Jeong
 */
public class DoubleDownSamplerTest extends DownSamplerTestBase<Double> {

    @Override
    protected DownSampler<Double> getSampler() {
        return DownSamplers.getDoubleDownSampler(DEFAULT_VALUE, DOUBLE_COMPARISON_DELTA);
    }

    @Override
    protected Double createSample() {
        return RANDOM.nextDouble();
    }

    @Override
    protected void assertEquals(Double expected, Double actual) {
        Assert.assertEquals(expected, actual, DOUBLE_COMPARISON_DELTA);
    }
}
