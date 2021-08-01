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

package com.github.pagehelper.test.basic;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.model.User;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestNamespaceMap {

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testMapperWithStartPage() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", "刘");
            List<User> list = sqlSession.selectList("selectLike", map);
            assertEquals(78, list.get(0).getId());
            assertEquals(10, list.size());
            assertEquals(15, ((Page<?>) list).getTotal());

            PageHelper.startPage(1, 10);
            map.put("name", "刘睿");
            map.put("py", "LR");
            list = sqlSession.selectList("selectByMap", map);
            assertEquals(78, list.get(0).getId());
            assertEquals(1, list.size());
            assertEquals(1, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }
}
