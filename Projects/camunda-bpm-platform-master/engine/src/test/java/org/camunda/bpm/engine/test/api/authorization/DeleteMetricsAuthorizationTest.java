/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.authorization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeleteMetricsAuthorizationTest {

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected ManagementService managementService;
  protected IdentityService identityService;

  @Before
  public void init() {
    processEngine = engineRule.getProcessEngine();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    managementService = processEngine.getManagementService();
    identityService = processEngine.getIdentityService();

    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    identityService.clearAuthentication();
    removeAdminUser();
  }

  @Test
  public void shouldDeleteTaskMetricsWithAuthorizedAdmin() {
    // given
    authenticateAdminUser();

    // when
    managementService.deleteMetrics(null);

    // then
    // expect no exception
  }

  @Test
  public void shouldThrowExceptionWhenDeleteTaskMetricsWithAuthorizedNonAdmin() {
    // given
    authenticateUser();

    // when/then
    assertThatThrownBy(() -> managementService.deleteMetrics(null))
      .isInstanceOf(AuthorizationException.class)
      .hasMessageContaining("Required admin authenticated group or user.");
  }

  protected void authenticateAdminUser() {
    processEngineConfiguration.getAdminUsers().add("user");
    authenticateUser();
  }

  protected void authenticateUser() {
    identityService.setAuthentication("user", null, null);
  }

  protected void removeAdminUser() {
    processEngineConfiguration.getAdminUsers().remove("user");
  }
}
