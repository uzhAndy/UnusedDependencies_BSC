## Changelog

### 5.2.1 - 2021-06-20

- Upgrade dependency jsqlparser 4.0, mybatis 3.5.7
- Automatically recognize the following databases：
    - 虚谷数据库 xugu #599
    - 神通数据库 oscar  by **ranqing**
    - 瀚高数据库 highgo by **ashaiqing**
- BoundSqlInterceptorChain interceptor index parameter bug, fixed #587
- fixed #558
- Add PostgreSQL dialect by **liym@home**
- fixed #604, Solve the problem of total loss
- Add code comments, fixed #547

### 5.2.0 - 2020-07-26

- Upgrading jsqlparser to version 3.2 makes sql parsing better and supports sqlserver better.

- Modify the substitution regularity in sqlserver mode, and now allow spaces in `with( nolock)` brackets.

- Solving the bugs in reasonable, pageSizeZero and offset usage, the meaning and result are more consistent now.

- In the process of splicing paging SQL, a new line character is added to avoid invalid paging part caused by comments in the original SQL.

- ROW_ID alias in Oracle and Db2 is changed to PAGEHELPER_ROW_ID to avoid conflict with common names.

- Solve the special problem when using other interceptors with a single parameter ProviderSql (support mybatis 3.4.0+) [by Luo Zhenyu](https://github.com/luozhenyu)

- Automatic identification of clickhouse is supported, and paging is performed by MySQL.

- Change startRow, endRow type from int to long.

- Page adds a `public <T> PageInfo<T> toPageInfo(Function<E, T> function)` method to convert the data in the query results.

- Refer to `Oracle9iDialect` provided by pr#476, which is also a paging method used before. You can test and select the appropriate paging method by yourself.

   At present, there are two kinds of Oracle pagination as follows:
   ```sql
   -- OracleDialect outer control range
   WHERE ROW_ID <= ? AND ROW_ID > ?
   -- Oracle9iDialect's internal and external control scope respectively
   TMP_PAGE WHERE ROWNUM <= ? ) WHERE ROW_ID > ?
  ```
- Adding `BoundSqlInterceptor` of PageHelper plug-in can process or simply read SQL in three stages, adding `boundSqlInterceptors`, and configuring multiple implementation class names that implement `BoundSqlInterceptor` interface, separated by English commas. PageHelper can also be set for this paging through a `PageHelper.startPage(x,x).boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor)`.

The biggest change of this update is the addition of `BoundSqlInterceptor`, which can intercept the SQL(BoundSQL object) of paging processing at runtime:

```java
/**
 * BoundSql 处理器
 */
public interface BoundSqlInterceptor {
    /**
     * boundsql 处理
     *
     * @param type     类型
     * @param boundSql 当前类型的 boundSql
     * @param cacheKey 缓存 key
     * @param chain    处理器链，通过 chain.doBoundSql 方法继续执行后续方法，也可以直接返回 boundSql 终止后续方法的执行
     * @return 允许修改 boundSql 并返回修改后的
     */
    BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain);

    enum Type {
        /**
         * 原始SQL，分页插件执行前，先执行这个类型
         */
        ORIGINAL,
        /**
         * count SQL，第二个执行这里
         */
        COUNT_SQL,
        /**
         * 分页 SQL，最后执行这里
         */
        PAGE_SQL
    }

    /**
     * 处理器链，可以控制是否继续执行
     */
    interface Chain {
        Chain DO_NOTHING = new Chain() {
            @Override
            public BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey) {
                return boundSql;
            }
        };

        BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey);
    }
}
```

The interface includes boundSql interface method, Type enumeration, and the definition of Chain interface, and you don't need to consider Chain when you implement it yourself.

The interceptor is configured by `boundSqlInterceptors` parameter, and there are three situations when executing:

1. Regardless of whether the currently executed SQL will be paged or not, interceptor methods of `Type.ORIGINAL` will be executed.

2. When the paging method is called, the interceptor will continue to execute the interceptor method of `Type.COUNT_SQL`, which will only be executed when paging is executed and count query is specified.

3. When paging method is called, if count > 0, interceptor method of `Type.PAGE_SQL` will be executed, which will only be executed when paging is executed.

>With the specified parameter `PageHelper.startPage(1, Integer.MAX_VALUE, false).boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor)`, it can also play the role of not paging and count query, but can execute interceptor method of `Type.ORIGINAL`.

If you want to get the page before SQL execution, you only need to pay attention to `Type.ORIGINAL`, and the other two are before count execution and before page execution (when count=0, the page method will not be executed and will not be executed here).

Take the test code as an example:

```java
public class TestBoundSqlInterceptor implements BoundSqlInterceptor {
    public static final String COMMENT = "\n /* TestBoundSqlInterceptor */\n";

    @Override
    public BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain) {
        if (type == Type.ORIGINAL) {
            String sql = boundSql.getSql();
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("sql", sql + COMMENT);
        }
        return chain.doBoundSql(type, boundSql, cacheKey);
    }

}
```
The above code modifies the original sql before SQL execution, but only adds a comment at the end, which does not affect SQL execution. It is configured in the following way:

```xml
<plugin interceptor="com.github.pagehelper.PageInterceptor">
    <!-- 支持通过Mapper接口参数来传递分页参数 -->
    <property name="helperDialect" value="mysql"/>
    <property name="boundSqlInterceptors"
              value="com.github.pagehelper.test.basic.provider.TestBoundSqlInterceptor,com.github.pagehelper.test.basic.provider.TestBoundSqlInterceptor"/>
</plugin>
```

Here, in order to explain that the parameter value can be multiple, it is repeatedly configured once, that is, the above interceptor will execute it twice.

With this configuration, the above SQL will modify the SQL when the page is executed.

In addition to this configuration mode, temporary designation when PageHelper.startPage is also supported. This mode will put the interceptor at the chain head and execute it first, so you can control whether to execute it later or not, or you can do the final processing before returning after all subsequent executions.

Example:

```java
PageHelper.startPage(1, 10).boundSqlInterceptor(new BoundSqlInterceptor() {
    @Override
    public BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain) {
        System.out.println("before: " + boundSql.getSql());
        BoundSql doBoundSql = chain.doBoundSql(type, boundSql, cacheKey);
        System.out.println("after: " + doBoundSql.getSql());
        if (type == Type.ORIGINAL) {
            Assert.assertTrue(doBoundSql.getSql().contains(TestBoundSqlInterceptor.COMMENT));
        }
        return doBoundSql;
    }
});
```



### 5.1.11 - 2019-11-26

- Added support for Shentong database **wangss**
- Add support for HerdDB - support HerdDB, mostly like MySQL - auto detect HerdDB **Enrico Olivelli**
- fix some typos and grammar issues **LHearen**

### 5.1.10 - 2019-06-05

In version *5.1.0 - 2017-08-28*. Added `ReplaceSql` interface for handling sqlServer with (nolock) problem,
add the replaceSql parameters, the optional value is `simple` and `regex`, or to achieve the `ReplaceSql` interface
fully qualified class name. The default value is `simple`, still using the original way to deal with,
the new regex will be convert `with (nolock)` to `table_PAGEWITHNOLOCK`.

This update only changes the default value from `simple` to `regex`, which can almost 100% solve the paging problem of sqlServer.

The following are examples from two issue.

#### issue [#76](https://github.com/pagehelper/pagehelper-spring-boot/issues/76)

Original SQL：
```sql
SELECT *
FROM
forum_post_info a with(nolock)
LEFT JOIN forum_carcase_tags as b with(nolock) on a.id = b.carcase_id where b.tag_id = 127
```
Converted Count SQL：
```sql
SELECT COUNT(0)
FROM forum_post_info a WITH (NOLOCK)
	LEFT JOIN forum_carcase_tags b WITH (NOLOCK) ON a.id = b.carcase_id
WHERE b.tag_id = 127
```
Converted paging SQL：
```sql
SELECT TOP 10 *
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY RAND()) AS PAGE_ROW_NUMBER, *
	FROM (
		SELECT *
		FROM forum_post_info a WITH (NOLOCK)
			LEFT JOIN forum_carcase_tags b WITH (NOLOCK) ON a.id = b.carcase_id
		WHERE b.tag_id = 127
	) PAGE_TABLE_ALIAS
) PAGE_TABLE_ALIAS
WHERE PAGE_ROW_NUMBER > 1
ORDER BY PAGE_ROW_NUMBER
```

#### issue [#398](https://github.com/pagehelper/Mybatis-PageHelper/issues/398)

Original SQL：
```sql
Select AUS.ScheduleID, AUS.SystemID, AUS.ClinicID, AUS.DoctorID, AUS.ScheduleDate,
	AUS.StartTime, AUS.EndTime, AUS.Status, AUS.BookBy, AUS.Note, AUS.Remark, AUS.SourceType, CM.CompanyName,
	AU.UserName As DoctorName, AU.UserNumber As DoctorNumber, CC.CodeDesc As ClinicName, CD.Lat, CD.Lng,
	CD.ContactTel, CD.Address, CR.ConsultationStatusID, CR.RegisterStatus,A1.CodeDesc as AreaLevel1, A2.CodeDesc as AreaLevel2
	From ACM_User_Schedule AUS with(nolock)
	Left Join Client_Register CR with(nolock) On AUS.BookBy=CR.ClientID And CR.SourceType='F' And AUS.ClientRegisterNum=CR.ClientRegisterNum
	Inner Join ACM_User AU with(nolock) On AU.UserID = AUS.DoctorID
	Inner Join Code_Clinic CC with(nolock) On AUS.ClinicID=CC.CodeID
	Inner Join Clinic_Detail CD with(nolock) On CC.CodeID = CD.ClinicID
	Inner Join Code_Area A1 with(nolock) On CD.AreaLevel1ID=A1.CodeID
	Inner Join Code_Area A2 with(nolock) On CD.AreaLevel2ID=A2.CodeID
	Inner Join Company_Master CM with(nolock) On CC.SystemID = CM.SystemID
	Where BookBy=1
```
Converted Count SQL：
```sql
SELECT COUNT(0)
FROM ACM_User_Schedule AUS WITH (NOLOCK)
	LEFT JOIN Client_Register CR WITH (NOLOCK)
	ON AUS.BookBy = CR.ClientID
		AND CR.SourceType = 'F'
		AND AUS.ClientRegisterNum = CR.ClientRegisterNum
	INNER JOIN ACM_User AU WITH (NOLOCK) ON AU.UserID = AUS.DoctorID
	INNER JOIN Code_Clinic CC WITH (NOLOCK) ON AUS.ClinicID = CC.CodeID
	INNER JOIN Clinic_Detail CD WITH (NOLOCK) ON CC.CodeID = CD.ClinicID
	INNER JOIN Code_Area A1 WITH (NOLOCK) ON CD.AreaLevel1ID = A1.CodeID
	INNER JOIN Code_Area A2 WITH (NOLOCK) ON CD.AreaLevel2ID = A2.CodeID
	INNER JOIN Company_Master CM WITH (NOLOCK) ON CC.SystemID = CM.SystemID
WHERE BookBy = 1
```
Converted paging SQL：
```sql
SELECT TOP 10 ScheduleID, SystemID, ClinicID, DoctorID, ScheduleDate
	, StartTime, EndTime, Status, BookBy, Note
	, Remark, SourceType, CompanyName, DoctorName, DoctorNumber
	, ClinicName, Lat, Lng, ContactTel, Address
	, ConsultationStatusID, RegisterStatus, AreaLevel1, AreaLevel2
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY RAND()) AS PAGE_ROW_NUMBER, ScheduleID, SystemID, ClinicID, DoctorID
		, ScheduleDate, StartTime, EndTime, Status, BookBy
		, Note, Remark, SourceType, CompanyName, DoctorName
		, DoctorNumber, ClinicName, Lat, Lng, ContactTel
		, Address, ConsultationStatusID, RegisterStatus, AreaLevel1, AreaLevel2
	FROM (
		SELECT AUS.ScheduleID, AUS.SystemID, AUS.ClinicID, AUS.DoctorID, AUS.ScheduleDate
			, AUS.StartTime, AUS.EndTime, AUS.Status, AUS.BookBy, AUS.Note
			, AUS.Remark, AUS.SourceType, CM.CompanyName, AU.UserName AS DoctorName, AU.UserNumber AS DoctorNumber
			, CC.CodeDesc AS ClinicName, CD.Lat, CD.Lng, CD.ContactTel, CD.Address
			, CR.ConsultationStatusID, CR.RegisterStatus, A1.CodeDesc AS AreaLevel1, A2.CodeDesc AS AreaLevel2
		FROM ACM_User_Schedule AUS WITH (NOLOCK)
			LEFT JOIN Client_Register CR WITH (NOLOCK)
			ON AUS.BookBy = CR.ClientID
				AND CR.SourceType = 'F'
				AND AUS.ClientRegisterNum = CR.ClientRegisterNum
			INNER JOIN ACM_User AU WITH (NOLOCK) ON AU.UserID = AUS.DoctorID
			INNER JOIN Code_Clinic CC WITH (NOLOCK) ON AUS.ClinicID = CC.CodeID
			INNER JOIN Clinic_Detail CD WITH (NOLOCK) ON CC.CodeID = CD.ClinicID
			INNER JOIN Code_Area A1 WITH (NOLOCK) ON CD.AreaLevel1ID = A1.CodeID
			INNER JOIN Code_Area A2 WITH (NOLOCK) ON CD.AreaLevel2ID = A2.CodeID
			INNER JOIN Company_Master CM WITH (NOLOCK) ON CC.SystemID = CM.SystemID
		WHERE BookBy = 1
	) PAGE_TABLE_ALIAS
) PAGE_TABLE_ALIAS
WHERE PAGE_ROW_NUMBER > 1
ORDER BY PAGE_ROW_NUMBER
```

SQL is formatted by https://tool.oschina.net/codeformat/sql

### 5.1.9 - 2019-05-29

- Upgrade jsqlparser to 2.0, upgrade mybatis to 3.5.1. resolve compatibility issues.
- Improve paging logic judgment. fixed #389
- Solve MetaObject version compatibility issues. fixed #349
- Processing order by output warning log when parsing fails, not throwing exception
- Solve three problems that may cause countColumn to fail fixed #325
- Add a comma with less BIT_ fixed #341
- Handling invalid links in documents isea533
- Document sample error. fixed #366
- fixed #373 NPE problem

### 5.1.8 - 2018-11-11

- Resolve the problem of `with(nolock)` in SQLServer ([#pr10](https://gitee.com/free/Mybatis_PageHelper/pulls/10)) by [lvshuyan](https://gitee.com/lvshuyan)

### 5.1.7 - 2018-10-11

- Support Aliyun PPAS database. Automatic identification of edb. fixed #281

### 5.1.6 - 2018-09-05

- Add the parameter useSqlserver2012, set to true, and use sqlserver2012(Dialect) as the default paging method for SQL Server databases, which is useful in dynamic data sources.
- Add an IPage interface. Currently, there is only one parameter to support the MyBatis query method, and when the parameter implements the IPage interface, paging query will be automatically performed if paging parameters exist. Thanks to [moonfruit](https://github.com/moonfruit) Issue two years ago.
- fixed # 276 to resolve hashset concurrency issue
- Optimize code structure and streamline interceptor code

### 5.1.5 - 2018-09-02

- Optimize the code and remove unnecessary checks(**by lenosp**)
- Solve the small problem of pageKey multi-processing once #268
- Added javadoc documentation on gitee(https://apidoc.gitee.com/free/Mybatis_PageHelper)
- Solve the problem of default reflection without cache fixed #275
- Optimizing mysql ifnull function causes paging performance problems (**by miaogr**)（This change was eventually changed to the following `aggregateFunctions`）
- Jsqlparser has been upgraded to version 1.2, which is incompatible with 1.0 and has been resolved. fixed 273
- Remove the g(s)etFirstPage and g(s)etLastPage methods that are ambiguous in PageInfo
- Throws an exception that failed to parse when sorting fixed #257
- Resolve the initialization problem when there is no properties property when configuring the spring use `<bean>`. fixed #26
- Fix the problem that Oracle paging will leak data (**by muyun12**)
- `aggregateFunctions`: The default is the aggregate function of all common databases,
  allowing you to manually add aggregate functions ( affecting the number of rows ).
  All functions that start with aggregate functions will be wrap as subquery.
  Other functions and columns will be replaced with count(0).

After adding the `aggregateFunctions` parameter, the biggest difference from the original is that if there is `select ifnull(XXX,YY) from table ...`, the original count query is
 `select count(0) from (select ifnull(xxx,yy) from table ... ) temp_count` now distinguishes aggregate functions, if not aggregate functions, it will become
 `select count(0) from table ...`.

The aggregate function prefixes included by default are as follows:

```java
/**
 * Aggregate functions, beginning with the following functions are considered aggregate functions
 */
private static final Set<String> AGGREGATE_FUNCTIONS = new HashSet<String>(Arrays.asList(
        ("APPROX_COUNT_DISTINCT," +
        "ARRAY_AGG," +
        "AVG," +
        "BIT_" +
        //"BIT_AND," +
        //"BIT_OR," +
        //"BIT_XOR," +
        "BOOL_," +
        //"BOOL_AND," +
        //"BOOL_OR," +
        "CHECKSUM_AGG," +
        "COLLECT," +
        "CORR," +
        //"CORR_," +
        //"CORRELATION," +
        "COUNT," +
        //"COUNT_BIG," +
        "COVAR," +
        //"COVAR_POP," +
        //"COVAR_SAMP," +
        //"COVARIANCE," +
        //"COVARIANCE_SAMP," +
        "CUME_DIST," +
        "DENSE_RANK," +
        "EVERY," +
        "FIRST," +
        "GROUP," +
        //"GROUP_CONCAT," +
        //"GROUP_ID," +
        //"GROUPING," +
        //"GROUPING," +
        //"GROUPING_ID," +
        "JSON_," +
        //"JSON_AGG," +
        //"JSON_ARRAYAGG," +
        //"JSON_OBJECT_AGG," +
        //"JSON_OBJECTAGG," +
        //"JSONB_AGG," +
        //"JSONB_OBJECT_AGG," +
        "LAST," +
        "LISTAGG," +
        "MAX," +
        "MEDIAN," +
        "MIN," +
        "PERCENT_," +
        //"PERCENT_RANK," +
        //"PERCENTILE_CONT," +
        //"PERCENTILE_DISC," +
        "RANK," +
        "REGR_," +
        "SELECTIVITY," +
        "STATS_," +
        //"STATS_BINOMIAL_TEST," +
        //"STATS_CROSSTAB," +
        //"STATS_F_TEST," +
        //"STATS_KS_TEST," +
        //"STATS_MODE," +
        //"STATS_MW_TEST," +
        //"STATS_ONE_WAY_ANOVA," +
        //"STATS_T_TEST_*," +
        //"STATS_WSR_TEST," +
        "STD," +
        //"STDDEV," +
        //"STDDEV_POP," +
        //"STDDEV_SAMP," +
        //"STDDEV_SAMP," +
        //"STDEV," +
        //"STDEVP," +
        "STRING_AGG," +
        "SUM," +
        "SYS_OP_ZONE_ID," +
        "SYS_XMLAGG," +
        "VAR," +
        //"VAR_POP," +
        //"VAR_SAMP," +
        //"VARIANCE," +
        //"VARIANCE_SAMP," +
        //"VARP," +
        "XMLAGG").split(",")));
```

### 5.1.4 - 2018-04-22

- Add the DaMeng Database (dm) to page using Oracle. If you want to change SqlServer, you can refer to the `dialectAlias` parameter in the 5.1.3 update log.

### 5.1.3 - 2018-04-07

- `Page` `toString` method adds `super.toString()`. The final output form is `Page{Attribute}[Collection]`.
- New `defaultCount` parameter is used to control whether to perform count query in the default method without count query. By default, `true` will execute count query. This is a globally valid parameter, and it is a uniform behavior when multiple data sources are used.
- New `dialogAlias` parameter that allows you to configure aliases for custom implementations. it can be used to automatically obtain corresponding implementations based on JDBC URL. it allows you to overwrite existing implementations in this way. configuration examples are ( Separate multiple configurations with semicolons ):
  ```xml
  <property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
  ```
- The new `PageSerializable` class, a simplified version of the `PageInfo` class, is recommended to use or refer to this class when it does not require much information.

### 5.1.2 - 2017-09-18

- Solve the problem  when using the `PageHelper.orderBy` method alone #110;

### 5.1.1 - 2017-08-30

- The update to solve the problem and only SqlServer 2005,2008 related.
- Resolve `RegexWithNolockReplaceSql` in the Wrong regular `w?`, it should be `w+`.
- Resolved `SqlServerDialect` did not initialize the default `SimpleWithNolockReplaceSql` error.
- `SqlServerRowBoundsDialect` support for the replaceSql parameter.


### 5.1.0 - 2017-08-28

- Added the sorting functionality included in the previous version of 4.x, and the usage is consistent (PageHelper adds several sort-related methods).
- Paging SQL is converted to PreparedStatement SQL.
- Added `ReplaceSql` interface for handling sqlServer with (nolock) problem, add the replaceSql parameters, the optional value is `simple` and `regex`, or to achieve the `ReplaceSql` interface fully qualified class name. The default value is `simple`, still using the original way to deal with, the new regex will be convert `with (nolock)` to `table_PAGEWITHNOLOCK`.
- `PageRowBounds` add `count` attribute, you can control whether execute the count query.


### 5.0.4 - 2017-08-01

- Add a simple configuration support for the Phoenix database, You can configure `helperDialect=phoenix`. Can also automatically identify the Phoenix database jdbc url.
- Simplified cache of `msCountMap`
- Add `countSuffix` count query suffix configuration parameters, this parameter is configured for `PageInterceptor`, the default value is `_COUNT`.
- Add custom count query support, see below for details.

#### Add custom count query support

Add `countSuffix` count query suffix configuration parameters, this parameter is configured for `PageInterceptor`, the default value is `_COUNT`.

The paging plugin will preferentially find the handwritten paging query by the current query `msId + countSuffix`.

If there is no custom query, the query is still automatically created using the previous way.

For example, if there are two queries:
```xml
<select id="selectLeftjoin" resultType="com.github.pagehelper.model.User">
    select a.id,b.name,a.py from user a
    left join user b on a.id = b.id
    order by a.id
</select>
<select id="selectLeftjoin_COUNT" resultType="Long">
    select count(distinct a.id) from user a
    left join user b on a.id = b.id
</select>
```
The above `countSuffix` uses the default value of` _COUNT`, and the paging plugin will automatically get the query to `selectLeftjoin_COUNT`. This query needs to ensure that the result is correct.

The value of the return value must be `resultType =" Long "`, and the same parameter used by `selectLeftjoin` 'is used, so it is used in SQL to follow the selection of` selectLeftjoin`'.

Because the `selectLeftjoin_COUNT` method is invoked automatically, there is no need to provide the appropriate method on the interface, or if it is required to be invoked separately.

The above method to perform the portion of the output log is as follows：
```
DEBUG [main] - ==>  Preparing: select count(distinct a.id) from user a left join user b on a.id = b.id
DEBUG [main] - ==> Parameters: 
TRACE [main] - <==    Columns: C1
TRACE [main] - <==        Row: 183
DEBUG [main] - <==      Total: 1
DEBUG [main] - Cache Hit Ratio [com.github.pagehelper.mapper.CountryMapper]: 0.0
DEBUG [main] - ==>  Preparing: select a.id,b.name,a.py from user a left join user b on a.id = b.id order by a.id LIMIT 10
DEBUG [main] - ==> Parameters: 
TRACE [main] - <==    Columns: ID, COUNTRYNAME, COUNTRYCODE
TRACE [main] - <==        Row: 1, Angola, AO
TRACE [main] - <==        Row: 2, Afghanistan, AF
TRACE [main] - <==        Row: 3, Albania, AL
```

### 5.0.3 -2017-06-20

- Solve the `supportMethodsArguments` parameter problem. It is recommended to upgrade to the latest version.

### 5.0.2 - 2017-05-30

- `Page<E>` implements `Closeable` interface, in JDK7+ which can use the in `try ()` call, it will automatically call `PageHelper.clearPage ();`[#58](https://github.com/pagehelper/Mybatis-PageHelper/issues/58)。
- fixed: DB2 paging must be specified sub-query alias, or an exception will occur [#52](https://github.com/pagehelper/Mybatis-PageHelper/issues/52)
- fixed：if `page.size() == 0` then `pageInfo.isIsLastPage()` is `false` [#50](https://github.com/pagehelper/Mybatis-PageHelper/issues/50)


### 5.0.1 - 2017-04-23
- Add the new parameter `countColumn` used to configure the automatic count column, the default value `0`, that is, `count(0).
- The `Page` class is also added with the `countColumn` parameter, which can be configured for a specific query.
- Modify the document display problem, by liumian* [PR #30](https://github.com/pagehelper/Mybatis-PageHelper/pull/30)
- Resolved sqlserver2012 paging error [42](https://github.com/pagehelper/Mybatis-PageHelper/issues/42)

### 5.0.0 - 2017-01-02

- Use Use [QueryInterceptor spec](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) to handle paging logic
- New pagination plugin interceptor `com.github.pagehelper.PageInterceptor`
- New `Dialect` `PageHelper` is a special implementation class, the previous function is implemented in more user-friendly ways
- New pagination plugin only a `dialect` parameter, the default `dialect` is `PageHelper`
- `PageHelper` continue to support previously provided parameters, Among the latest to use the document has been fully updated
- `PageHelper` has a `helperDialect` parameter which is the same functional as the previous `dialect`
- Added paging implementation based on pure `RowBounds` and `PageRowBounds`, 
in `com.github. pagehelper. dialect. rowbounds` package, it is used as `dialect` Parameter sample implementation, more detailed documentation will be added later
- Removed inappropriate orderby functions that appear in pagination plugin. It will provide a separate sort plug-ins in the future
- Remove `PageHelper` are less commonly used methods
- A new document, an important part of the update has been mentioned in the changelog, provides the English version of this document
- fix bug [#149](http://git.oschina.net/free/Mybatis_PageHelper/issues/149)
- renamed Db2RowDialect to Db2RowBoundsDialect
- All thrown exceptions being replaced by PageException

## Older Changelogs are written in Chinese
You can [view here](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Changelog.md)
