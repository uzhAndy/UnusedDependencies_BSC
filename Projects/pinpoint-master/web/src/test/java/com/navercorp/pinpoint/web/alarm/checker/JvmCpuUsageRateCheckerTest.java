/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.alarm.checker;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

@SuppressWarnings("deprecation")
public class JvmCpuUsageRateCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    private static ApplicationIndexDao applicationIndexDao;

    private static AgentStatDao<JvmGcBo> jvmGcDao;

    private static AgentStatDao<CpuLoadBo> cpuLoadDao;
    
    @BeforeClass
    public static void before() {
        jvmGcDao = new AgentStatDao<JvmGcBo>() {

            @Override
            public List<JvmGcBo> getAgentStatList(String agentId, Range range) {
                return Collections.emptyList();
            }

            @Override
            public boolean agentStatExists(String agentId, Range range) {
                return false;
            }
        };

        cpuLoadDao = new AgentStatDao<CpuLoadBo>() {

            public List<CpuLoadBo> getAgentStatList(String agentId, Range range) {
                List<CpuLoadBo> cpuLoads = new LinkedList<>();
                
                for (int i = 0; i < 36; i++) {
                    CpuLoadBo cpuLoad = new CpuLoadBo();
                    cpuLoad.setJvmCpuLoad(0.6);
                    cpuLoads.add(cpuLoad);
                }
                
                return cpuLoads;
            }

            @Override
            public boolean agentStatExists(String agentId, Range range) {
                return true;
            }
        };
        
        applicationIndexDao = new ApplicationIndexDao() {

            @Override
            public List<Application> selectAllApplicationNames() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Application> selectApplicationName(String applicationName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<String> selectAgentIds(String applicationName) {
                if (SERVICE_NAME.equals(applicationName)) {
                    List<String> agentIds = new LinkedList<String>();
                    agentIds.add("local_tomcat");
                    return agentIds;
                }
                
                throw new IllegalArgumentException();
            }

            @Override
            public void deleteApplicationName(String applicationName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void deleteAgentId(String applicationName, String agentId) {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    
    @Test
    public void checkTest1() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.JVM_CPU_USAGE_RATE.getName(), 60, "testGroup", false, false, false, "");
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, jvmGcDao, cpuLoadDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker checker = new JvmCpuUsageRateChecker(collector, rule);
        
        checker.check();
        assertTrue(checker.isDetected());
    }
    
    @Test
    public void checkTest2() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.JVM_CPU_USAGE_RATE.getName(), 61, "testGroup", false, false, false, "");
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, jvmGcDao, cpuLoadDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker checker = new JvmCpuUsageRateChecker(collector, rule);
        
        checker.check();
        assertFalse(checker.isDetected());
    }

}
