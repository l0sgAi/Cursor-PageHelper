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
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql,
            CacheKey pageKey) {
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
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_CURSOR_VALUE,
                                cursorValueClass).build());

                // 添加pageSize参数
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());

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
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
            } else {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, long.class).build());
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
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
            String trimmedSql = sql.trim();
            String upperSql = trimmedSql.toUpperCase();

            // 查找主查询中ORDER BY、GROUP BY、HAVING等子句的位置
            int insertPosition = findInsertPositionForCursorCondition(upperSql, trimmedSql);

            // 构建游标条件
            StringBuilder cursorCondition = new StringBuilder();
            if (containsWhereClauseInMainQuery(upperSql)) {
                cursorCondition.append("\n AND ");
            } else {
                cursorCondition.append("\n WHERE ");
            }

            cursorCondition.append(page.getCursorColumn());
            if (page.getCursorGreaterThan() != null && page.getCursorGreaterThan()) {
                cursorCondition.append(" > ?");
            } else {
                cursorCondition.append(" < ?");
            }

            // 在正确的位置插入游标条件
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 100);
            sqlBuilder.append(trimmedSql, 0, insertPosition);
            sqlBuilder.append(cursorCondition);
            sqlBuilder.append(trimmedSql.substring(insertPosition));
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
     * 查找插入游标条件的正确位置
     * 需要在主查询的WHERE之后、ORDER BY/GROUP BY/HAVING之前插入
     */
    private int findInsertPositionForCursorCondition(String upperSql, String originalSql) {
        // 需要查找的关键字（按优先级排序）
        // 包括常见子句和MySQL特有的锁定、导出、联合查询等子句
        String[] keywords = { "UNION", "UNION ALL", "INTERSECT", "EXCEPT",
                "ORDER BY", "GROUP BY", "HAVING", "WINDOW",
                "LIMIT", "PROCEDURE",
                "INTO OUTFILE", "INTO DUMPFILE", "INTO",
                "FOR UPDATE", "FOR SHARE", "LOCK IN SHARE MODE" };

        int minPosition = originalSql.length(); // 默认在末尾

        // 找到所有关键字在主查询中的位置，取最小值
        for (String keyword : keywords) {
            int pos = findKeywordInMainQuery(upperSql, keyword);
            if (pos != -1 && pos < minPosition) {
                minPosition = pos;
            }
        }

        return minPosition;
    }

    /**
     * 查找关键字在主查询中的位置（排除子查询和括号内的内容）
     */
    private int findKeywordInMainQuery(String upperSql, String keyword) {
        int parenthesesDepth = 0;
        int sqlLength = upperSql.length();
        int keywordLength = keyword.length();

        for (int i = 0; i < sqlLength; i++) {
            char c = upperSql.charAt(i);

            if (c == '(') {
                parenthesesDepth++;
            } else if (c == ')') {
                parenthesesDepth--;
            } else if (parenthesesDepth == 0) {
                // 只在主查询层级查找
                if (i + keywordLength <= sqlLength) {
                    String segment = upperSql.substring(i, i + keywordLength);
                    if (segment.equals(keyword)) {
                        // 确保关键字前后是空白字符或开头/结尾
                        boolean validBefore = (i == 0 || Character.isWhitespace(upperSql.charAt(i - 1)));
                        boolean validAfter = (i + keywordLength >= sqlLength ||
                                Character.isWhitespace(upperSql.charAt(i + keywordLength)));
                        if (validBefore && validAfter) {
                            return i;
                        }
                    }
                }
            }
        }

        return -1;
    }

    /**
     * 检查主查询是否包含WHERE子句
     * 需要排除子查询中的WHERE
     */
    private boolean containsWhereClauseInMainQuery(String upperSql) {
        // 使用新的方法来查找WHERE关键字
        int wherePosition = findKeywordInMainQuery(upperSql, "WHERE");
        return wherePosition != -1;
    }

}
