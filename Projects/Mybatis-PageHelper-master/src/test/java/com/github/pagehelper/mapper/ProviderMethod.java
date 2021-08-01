/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper.mapper;

import com.github.pagehelper.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzh
 */
public class ProviderMethod {

    public String selectSimple(String str) {
        return "select * from user where name like '%" + str + "'";
    }

    @SuppressWarnings("unchecked")
    public String select(Map<String, Object> map) {
        Map<String, Object> param = (HashMap<String, Object>) map.get("param");
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("select * from user where 1=1 ");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            sbSql.append(" and " + entry.getKey() + "= #{param." + entry.getKey() + "} ");
        }
        sbSql.append("order by id");
        return sbSql.toString();
    }

    public String selectUser(User user) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("select * from user where 1=1 ");
        if (user.getId() > 0) {
            sbSql.append(" and id = #{id} ");
        }
        if (user.getPy() != null) {
            sbSql.append(" and py = #{py} ");
        }
        if (user.getName() != null) {
            sbSql.append(" and name = #{name} ");
        }
        sbSql.append("order by id");
        return sbSql.toString();
    }
}
