/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
public class ApplicationMemoryChart implements StatChart {

    private final ApplicationMemoryChartGroup applicationMemoryChartGroup;

    public ApplicationMemoryChart(TimeWindow timeWindow, List<AggreJoinMemoryBo> aggreJoinMemoryBos) {
        this.applicationMemoryChartGroup = new ApplicationMemoryChartGroup(timeWindow, aggreJoinMemoryBos);
    }

    @Override
    public StatChartGroup getCharts() {
        return applicationMemoryChartGroup;
    }

    public static class ApplicationMemoryChartGroup implements StatChartGroup {

        private static final DoubleApplicationStatPoint.UncollectedCreator UNCOLLECTED_MEMORY_POINT = new DoubleApplicationStatPoint.UncollectedCreator(JoinMemoryBo.UNCOLLECTED_VALUE);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> memoryChartMap;

        public enum MemoryChartType implements ApplicationChartType {
            MEMORY_HEAP,
            MEMORY_NON_HEAP
        }

        public ApplicationMemoryChartGroup(TimeWindow timeWindow, List<AggreJoinMemoryBo> aggreJoinMemoryBoList) {
            this.timeWindow = timeWindow;
            this.memoryChartMap = newChart(aggreJoinMemoryBoList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinMemoryBo> aggreJoinMemoryBoList) {

            Chart<DoubleApplicationStatPoint> heapChart = newChart(aggreJoinMemoryBoList, this::newHeap);
            Chart<DoubleApplicationStatPoint> nonHeapChart = newChart(aggreJoinMemoryBoList, this::newNonHeap);

            return ImmutableMap.of(MemoryChartType.MEMORY_HEAP, heapChart, MemoryChartType.MEMORY_NON_HEAP, nonHeapChart);
        }

        private DoubleApplicationStatPoint newHeap(AggreJoinMemoryBo memory) {
            final JoinLongFieldBo heapUsedJoinValue = memory.getHeapUsedJoinValue();
            return new DoubleApplicationStatPoint(memory.getTimestamp(), (double) heapUsedJoinValue.getMin(), heapUsedJoinValue.getMinAgentId(),
                    (double) heapUsedJoinValue.getMax(), heapUsedJoinValue.getMaxAgentId(), (double) heapUsedJoinValue.getAvg());
        }

        private DoubleApplicationStatPoint newNonHeap(AggreJoinMemoryBo memory) {
            final JoinLongFieldBo nonHeapUsedJoinValue = memory.getNonHeapUsedJoinValue();
            return new DoubleApplicationStatPoint(memory.getTimestamp(), (double) nonHeapUsedJoinValue.getMin(), nonHeapUsedJoinValue.getMinAgentId(),
                    (double) nonHeapUsedJoinValue.getMax(), nonHeapUsedJoinValue.getMaxAgentId(), (double) nonHeapUsedJoinValue.getAvg());
        }

        private Chart<DoubleApplicationStatPoint> newChart(List<AggreJoinMemoryBo> aggreJoinMemoryBoList, Function<AggreJoinMemoryBo, DoubleApplicationStatPoint> filter) {
            TimeSeriesChartBuilder<DoubleApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_MEMORY_POINT);
            return builder.build(aggreJoinMemoryBoList, filter);
        }


        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.memoryChartMap;
        }
    }
}
