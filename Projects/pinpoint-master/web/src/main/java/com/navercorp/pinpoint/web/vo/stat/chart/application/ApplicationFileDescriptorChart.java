/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roy Kim
 */
public class ApplicationFileDescriptorChart implements StatChart {

    private final ApplicationFileDescriptorChartGroup fileDescriptorChartGroup;

    public ApplicationFileDescriptorChart(TimeWindow timeWindow, List<AggreJoinFileDescriptorBo> aggreJoinFileDescriptorBoList) {
        this.fileDescriptorChartGroup = new ApplicationFileDescriptorChartGroup(timeWindow, aggreJoinFileDescriptorBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return fileDescriptorChartGroup;
    }

    public static class ApplicationFileDescriptorChartGroup implements StatChartGroup {

        private static final LongApplicationStatPoint.UncollectedCreator UNCOLLECTED_FILE_DESCRIPTOR_POINT = new LongApplicationStatPoint.UncollectedCreator(JoinFileDescriptorBo.UNCOLLECTED_VALUE);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> fileDescriptorChartMap;

        public enum FileDescriptorChartType implements ApplicationChartType {
            OPEN_FILE_DESCRIPTOR_COUNT
        }

        public ApplicationFileDescriptorChartGroup(TimeWindow timeWindow, List<AggreJoinFileDescriptorBo> aggreFileDescriptorList) {
            this.timeWindow = timeWindow;
            this.fileDescriptorChartMap = newChart(aggreFileDescriptorList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinFileDescriptorBo> aggreFileDescriptorList) {
            Chart<LongApplicationStatPoint> openFileDescriptorCountChart = newChart(aggreFileDescriptorList, this::newOpenFileDescriptorCount);
            return ImmutableMap.of(FileDescriptorChartType.OPEN_FILE_DESCRIPTOR_COUNT, openFileDescriptorCountChart);
        }

        private Chart<LongApplicationStatPoint> newChart(List<AggreJoinFileDescriptorBo> fileDescriptorList, Function<AggreJoinFileDescriptorBo, LongApplicationStatPoint> filter) {
            TimeSeriesChartBuilder<LongApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_FILE_DESCRIPTOR_POINT);
            return builder.build(fileDescriptorList, filter);
        }

        private LongApplicationStatPoint newOpenFileDescriptorCount(AggreJoinFileDescriptorBo fileDescriptor) {
            final JoinLongFieldBo openFdCountJoinValue = fileDescriptor.getOpenFdCountJoinValue();
            return new LongApplicationStatPoint(fileDescriptor.getTimestamp(), openFdCountJoinValue.getMin(), openFdCountJoinValue.getMinAgentId(),
                    openFdCountJoinValue.getMax(), openFdCountJoinValue.getMaxAgentId(), openFdCountJoinValue.getAvg());
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.fileDescriptorChartMap;
        }
    }
}
