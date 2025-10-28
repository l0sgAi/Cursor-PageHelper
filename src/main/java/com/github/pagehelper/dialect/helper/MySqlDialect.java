/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
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

package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.util.MetaObjectUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuzh
 */
public class MySqlDialect extends AbstractHelperDialect {

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        // ========== Cursor分页参数处理 ==========
        if (page.useCursor()) {
            paramMap.put(PAGEPARAMETER_CURSOR_COLUMN, page.getCursorColumn());
            paramMap.put(PAGEPARAMETER_CURSOR_VALUE, page.getCursorValue());
            paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());

            // 更新缓存键
            pageKey.update(page.getCursorColumn());
            pageKey.update(page.getCursorValue());
            pageKey.update(page.getPageSize());
            pageKey.update(page.getCursorGreaterThan());

            // 处理参数映射
            if (boundSql.getParameterMappings() != null) {
                List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());

                // 添加游标值参数
                Class<?> cursorValueClass = page.getCursorValue().getClass();
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_CURSOR_VALUE, cursorValueClass).build()
                );

                // 添加pageSize参数
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build()
                );

                MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
                metaObject.setValue("parameterMappings", newParameterMappings);
            }
            return paramMap;
        }

        // ========== 传统分页参数处理（保持原有逻辑） ==========
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());

        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            if (page.getStartRow() == 0) {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build()
                );
            } else {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, long.class).build()
                );
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build()
                );
            }
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        // ========== Cursor分页SQL生成 ==========
        if (page.useCursor()) {
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 100);
            sqlBuilder.append(sql);

            // 智能添加WHERE或AND
            String upperSql = sql.toUpperCase().trim();
            if (containsWhereClauseInMainQuery(upperSql)) {
                sqlBuilder.append("\n AND ");
            } else {
                sqlBuilder.append("\n WHERE ");
            }

            // 添加游标条件
            sqlBuilder.append(page.getCursorColumn());
            if (page.getCursorGreaterThan() != null && page.getCursorGreaterThan()) {
                sqlBuilder.append(" > ?");
            } else {
                sqlBuilder.append(" < ?");
            }

            sqlBuilder.append("\n LIMIT ?");
            return sqlBuilder.toString();
        }

        // ========== 传统分页SQL生成（保持原有逻辑） ==========
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (page.getStartRow() == 0) {
            sqlBuilder.append("\n LIMIT ? ");
        } else {
            sqlBuilder.append("\n LIMIT ?, ? ");
        }
        return sqlBuilder.toString();
    }

    /**
     * 检查主查询是否包含WHERE子句
     * 需要排除子查询中的WHERE
     */
    private boolean containsWhereClauseInMainQuery(String upperSql) {
        // 简单判断：查找最后一个WHERE的位置
        int lastWhereIndex = upperSql.lastIndexOf("WHERE");
        if (lastWhereIndex == -1) {
            return false;
        }

        // 查找最后一个FROM的位置（主查询的FROM）
        int lastFromIndex = upperSql.lastIndexOf("FROM");

        // 如果WHERE在FROM之后，说明是主查询的WHERE
        return lastWhereIndex > lastFromIndex;
    }

}
