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

package com.github.pagehelper.test.reasonable;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.mapper.UserMapper;
import com.github.pagehelper.model.User;
import com.github.pagehelper.util.MybatisReasonableHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PageTest {
    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testMapperWithStartPage() {
        SqlSession sqlSession = MybatisReasonableHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            //获取第20页，2条内容
            //分页插件会自动改为查询最后一页
            PageHelper.startPage(20, 50);
            List<User> list = userMapper.selectAll();
            PageInfo<User> page = new PageInfo<User>(list);
            assertEquals(33, list.size());
            assertEquals(151, page.getStartRow());
            assertEquals(4, page.getPageNum());
            assertEquals(183, page.getTotal());

            //获取第-3页，2条内容
            //由于只有7天数据，分页插件会自动改为查询最后一页
            PageHelper.startPage(-3, 50);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(50, list.size());
            assertEquals(1, page.getStartRow());
            assertEquals(1, page.getPageNum());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMapperWithStartPageAndReasonableFalse() {
        SqlSession sqlSession = MybatisReasonableHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            //获取第20页，2条内容
            //分页插件会自动改为查询最后一页
            PageHelper.startPage(20, 50, true, false, false);
            List<User> list = userMapper.selectAll();
            PageInfo<User> page = new PageInfo<User>(list);
            assertEquals(0, list.size());
            assertEquals(0, page.getStartRow());
            assertEquals(20, page.getPageNum());
            assertEquals(183, page.getTotal());

            PageHelper.startPage(4, 50, true, false, false);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(33, list.size());
            assertEquals(151, page.getStartRow());
            assertEquals(4, page.getPageNum());
            assertEquals(183, page.getTotal());

            PageHelper.startPage(-1, 50, true, false, false);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(0, list.size());
            assertEquals(0, page.getStartRow());
            assertEquals(-1, page.getPageNum());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMapperWithStartPageAndReasonableTrue() {
        SqlSession sqlSession = MybatisReasonableHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            //获取第20页，2条内容
            //分页插件会自动改为查询最后一页
            PageHelper.startPage(20, 50, true, true, false);
            List<User> list = userMapper.selectAll();
            PageInfo<User> page = new PageInfo<User>(list);
            assertEquals(33, list.size());
            assertEquals(151, page.getStartRow());
            assertEquals(4, page.getPageNum());
            assertEquals(183, page.getTotal());

            PageHelper.startPage(4, 50, true, true, false);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(33, list.size());
            assertEquals(151, page.getStartRow());
            assertEquals(4, page.getPageNum());
            assertEquals(183, page.getTotal());

            PageHelper.startPage(-1, 50, true, true, false);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(50, list.size());
            assertEquals(1, page.getStartRow());
            assertEquals(1, page.getPageNum());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMapperWithStartPageAndPageSizeZeroFalse() {
        SqlSession sqlSession = MybatisReasonableHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            //获取第20页，2条内容
            //分页插件会自动改为查询最后一页
            PageHelper.startPage(1, 0, true, true, false);
            List<User> list = userMapper.selectAll();
            PageInfo<User> page = new PageInfo<User>(list);
            assertEquals(0, list.size());
            assertEquals(0, page.getStartRow());
            assertEquals(1, page.getPageNum());
            assertEquals(183, page.getTotal());

            PageHelper.startPage(1, Integer.MAX_VALUE, true, true, false);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(183, list.size());
            assertEquals(1, page.getStartRow());
            assertEquals(1, page.getPageNum());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMapperWithStartPageAndPageSizeZeroTrue() {
        SqlSession sqlSession = MybatisReasonableHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            //获取第20页，2条内容
            //分页插件会自动改为查询最后一页
            PageHelper.startPage(1, 0, true, true, true);
            List<User> list = userMapper.selectAll();
            PageInfo<User> page = new PageInfo<User>(list);
            assertEquals(183, list.size());
            assertEquals(1, page.getStartRow());
            assertEquals(1, page.getPageNum());
            assertEquals(183, page.getTotal());

            PageHelper.startPage(1, -1, true, true, true);
            list = userMapper.selectAll();
            page = new PageInfo<User>(list);
            assertEquals(0, list.size());
            assertEquals(0, page.getStartRow());
            assertEquals(1, page.getPageNum());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

}
