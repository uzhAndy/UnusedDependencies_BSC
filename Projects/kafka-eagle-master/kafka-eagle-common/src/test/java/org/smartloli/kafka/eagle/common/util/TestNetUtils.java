/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.util;

/**
 * TODO
 *
 * @author smartloli.
 * <p>
 * Created by Apr 18, 2018
 */
public class TestNetUtils {

    public static void main(String[] args) {
        System.out.println(NetUtils.telnet("dn1", 9093));
        System.out.println(NetUtils.ping("nna"));
//		System.out.println(CalendarUtils.convertUnixTime(1524249300080L));
//		System.out.println(CalendarUtils.convertUnixTime(1524249900097L));
//        String result = HttpClientUtils.doGet("http://127.0.0.1:8083/connectors");
//        System.out.println(StrUtils.stringListConvertListStrings(result).size());
    }

}
