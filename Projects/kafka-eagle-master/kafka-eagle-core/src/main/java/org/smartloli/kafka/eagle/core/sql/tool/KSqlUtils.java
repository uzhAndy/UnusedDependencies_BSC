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
package org.smartloli.kafka.eagle.core.sql.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.smartloli.kafka.eagle.common.constant.JConstants;
import org.smartloli.kafka.eagle.common.util.UnicodeUtils;
import org.smartloli.kafka.eagle.core.sql.common.JSqlMapData;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Define the data structure, query by condition.
 *
 * @author smartloli.
 * <p>
 * Created by Mar 29, 2016
 */
public class KSqlUtils {

    /**
     * @param tabSchema : Table column,such as {"id":"integer","name":"varchar"}
     * @param tableName : Defining table names for query datasets, such as "user"
     * @param dataSets  : DataSets ,such as
     *                  [{"id":1,"name":"aaa"},{"id":2,"name":"bbb"},{}...]
     * @param sql       : such as "SELECT * FROM TBL"
     * @return String
     * @throws Exception : Throws an exception
     */
    public static JSONObject query(JSONObject tabSchema, String tableName, List<JSONArray> dataSets, String sql) throws Exception {
        JSONObject queryResults = new JSONObject();
        String model = createTempJson();
        List<List<String>> list = new LinkedList<>();
        for (JSONArray dataSet : dataSets) {
            for (Object obj : dataSet) {
                JSONObject object = (JSONObject) obj;
                List<String> tmp = new LinkedList<>();
                for (String key : object.keySet()) {
                    tmp.add(UnicodeUtils.encodeForUnicode(object.getString(key)));
                }
                list.add(tmp);
            }
        }
        JSqlMapData.loadSchema(tabSchema, tableName, list);

        Class.forName(JConstants.KAFKA_DRIVER);
        Properties info = new Properties();
        info.setProperty("lex", "JAVA");

        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + model, info);
        Statement st = connection.createStatement();
        ResultSet result = st.executeQuery(UnicodeUtils.encodeForUnicode(sql));
        ResultSetMetaData rsmd = result.getMetaData();
        List<Map<String, Object>> ret = new ArrayList<>();
        while (result.next()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                map.put(rsmd.getColumnName(i), UnicodeUtils.decodeForUnicode(result.getString(rsmd.getColumnName(i))));
            }
            ret.add(map);
        }
        result.close();
        st.close();
        connection.close();
        queryResults.put("result", new Gson().toJson(ret));
        queryResults.put("size", ret.size());
        return queryResults;
    }

    /**
     * Parse datasets to datatable.
     */
    public static String toJSONObject(List<JSONArray> dataSets) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (JSONArray dataSet : dataSets) {
            for (Object obj : dataSet) {
                JSONObject object = (JSONObject) obj;
                Map<String, Object> map = new HashMap<String, Object>();
                for (String key : object.keySet()) {
                    map.put(key, object.getString(key));
                }
                results.add(map);
            }
        }
        return new Gson().toJson(results);
    }

    private static String createTempJson() throws IOException {
        JSONObject object = new JSONObject();
        object.put("version", "1.21.0");
        object.put("defaultSchema", "db");
        JSONArray array = new JSONArray();
        JSONObject tmp = new JSONObject();
        tmp.put("name", "db");
        tmp.put("type", "custom");
        tmp.put("factory", "org.smartloli.kafka.eagle.core.sql.schema.JSqlSchemaFactory");
        JSONObject tmp2 = new JSONObject();
        tmp.put("operand", tmp2.put("database", "ke_memory_db"));
        array.add(tmp);
        object.put("schemas", array);
        return object.toJSONString();
    }

}
