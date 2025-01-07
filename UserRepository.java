/**
 * 常量代表 RkUserV 实体中的 shumuYakushokulleisho 属性。
 */
public static final String SHUMU_YAKUSHOKU_METSHO = "shumuYakushokulleisho";

/**
 * 常量代表 RkUserV 实体中的 zaisekikaGroupId 属性。
 */
public static final String ZAISEKI_KA_GROUP_ID = "zaisekikaGroupId";

/**
 * 常量代表 RkUserV 实体中的 courselleisho 属性。
 */
public static final String COURSE_MEISHO = "courselleisho";

/**
 * 常量代表 RkUserV 实体中的 zaisekiButenCd 属性。
 */
public static final String ZAISEKI_BUTEN_CD = "zaisekiButenCd";

/**
 * 常量代表 RkUserV 实体中的 zaisekiShusukenmukbn 属性。
 */
public static final String ZAISEKI_SHUMU_KENMU_KEM = "zaisekiShusukenmukbn";

/**
 * 常量代表 RkUserV 实体中的 rankmeisho 属性。
 */
public static final String RANK_MEISHO = "rankmeisho";

/**
 * 常量代表 RkUserV 实体中的 userNamekoseki 属性。
 */
public static final String USER_NAME_KOSEKI = "userNamekoseki";

/**
 * 常量代表 RkUserV 实体中的 zaisekiYakushokulleisho 属性。
 */
public static final String ZAISEKI_YAKUSHOKU_MEISHO = "zaisekiYakushokulleisho";

/**
 * 常量代表 RkUserV 实体中的 shukoshaFig 属性。
 */
public static final String SHUKOSHA_FLG = "shukoshaFlg";

/**
 * 常量代表 RkUserV 实体中的 koyokeitai 属性。
 */
public static final String KOYO_KEITAI = "koyokeitai";

/**
 * 常量代表 RkUserV 实体中的 userValidPeriodSd 属性。
 */
public static final String USER_VALID_PERIOD_SD = "userValidPeriodSd";

/**
 * 常量代表 RkUserV 实体中的 shokushu1 属性。
 */

/**
 * 在籍情報取得、主務兼務区分を条件とする。
 *
 * @param kaishaCd        会社コード
 * @param kijunbi         基準日
 * @param userId          ユーザID
 * @param shumukenmuKbn   主務兼務区分
 * @param genericCodeSetId 汎用コードセットID
 * @param kaGroupCd       グループID
 * @return 在籍情報
 */
@Query(
    "SELECT new jp.mufg.mjf.bizcommon.auth2.service.dto.SoshikiKengenInfoDto(" +
        "z.user.userId, " +
        "z.validPeriodSd, " +
        "z.validPeriodEd, " +
        "z.buten.butenId, " +
        "z.kaGroup.kaGroupId, " +
        "z.yakushokuCd, " +
        "COALESCE(z.zaisekiFlg, true), " +
        "z.shumukenmuKbn, " +
        "g.attribute1, " +
        "g.attribute2, " +
        "false, " +

        "s.zokusei6) " +
        
    "FROM User u " +
    "INNER JOIN ZaisekiSoshiki z ON z.user.userId = u.userId " +
        "AND :kijunbi BETWEEN z.validPeriodSd AND z.validPeriodEd " +
        "AND z.shumukenmuKbn IN :shumukenmuKbn " +
        "AND z.deleteFlg = false " +
    "INNER JOIN GenericCode g ON g.kaishald = :kaishaCd " +
        "AND g.genericCodeSetId = :genericCodeSetId " +
        "AND g.codeValue = z.yakushokuCd " +
        "AND :kijunbi BETWEEN z.editingValidPeriodStartDate AND g.editingValidPeriodEndDate " +
        "AND (u.userId = CASE WHEN :userId IS NULL THEN u.userId ELSE z.user.userId END) " +
        "AND g.deleteFlg = false " +
    
    "LEFT OUTER JOIN Shogu s ON s.userId = u.userId " +
        "AND :kijunbi BETWEEN s.validPeriodSd AND s.validPeriodEd " +
        "AND s.deleteFlg = false " +

    "WHERE u.kaishaCd = :kaishaCd " +
        "AND z.kaGroup.kaGroupCd = :kaGroupCd " +
        "AND u.deleteFlg = false " +
    "ORDER BY z.user.userId"
)
List<SoshikiKengenInfoDto> getSoshikiKengenInfoByZaisekiSoshikiAndGenericCodeAndShumuKensukbn(
    @Param("kaishaCd") KaishaCd kaishaCd,
    @Param("kijunbi") LocalDate kijunbi,
    @Param("kaGroupCd") String kaGroupCd,
    @Param("genericCodeSetId") String genericCodeSetId,
    @Param("userId") Long userId,
    @Param("shumukenmuKbn") List<ShusukenmuKbn> shumukenmuKbn
);

/*
 * 详细解释：
 * 
 * 该方法用于根据公司代码（kaishaCd）、基准日期（kijunbi）、组别代码（kaGroupCd）、
 * 泛用代码集ID（genericCodeSetId）、用户ID（userId）以及主务兼务区分（shumukenmuKbn）来获取在籍信息。
 * 
 * JPQL 查询部分：
 * - SELECT 子句通过构造函数表达式将查询结果映射为 SoshikiKengenInfoDto 对象，包含用户ID、
 * 在籍期间的开始和结束日期、部店ID、课组ID、役职代码、在籍标志（为空时默认为 true）、主务兼务区分、以及泛用代码的两个属性。
 * - FROM 子句从 User 实体表 u 开始。
 * - INNER JOIN ZaisekiSoshiki 表 z，与 User 表通过用户ID关联，并且在籍期间包含基准日期，主务兼务区分在给定列表中，且删除标志为 false。
 * - INNER JOIN GenericCode 表 g，与公司代码、泛用代码集ID和役职代码关联，基准日期在编辑有效期间内，用户ID匹配（如果提供），且删除标志为 false。
 * - WHERE 子句进一步过滤公司代码、课组代码，以及用户删除标志。
 * - ORDER BY 子句根据用户ID排序结果。
 * 
 * 返回值：
 * - 返回一个 SoshikiKengenInfoDto 类型的列表，包含满足条件的在籍信息。
 * 
 * 注意事项：
 * - 确保相关字段有适当的索引以优化查询性能。
 * - 在调用此方法前，验证传入参数的有效性和格式。
 * - 处理可能的异常以保证系统稳定性。
 */

/**
 * 従業員権限付与用:在籍情報取得。
 *
 * @param kaishaCd  会社コード
 * @param kijunbi   基準日
 * @param userId    ユーザID
 * @param roleList  権限リスト
 * @return 在籍情報
 */
@Query("SELECT new jp.mufg.ajf.bizcommon.auth2.service.dto.SoshikiKengenInfoDto("
     + "z.user.userId, "
     + "z.validPeriodSd, "
     + "z.validPeriodEd, "
     + "z.buten.butenId, "
     + "z.buten.butenCd, "
     + "z.kaGroup.kaGroupId, "
     + "z.kaGroup.kaGroupCd, "
     + "z.yakushokuCd, "
     + "COALESCE(z.zaisekiFlg, true), "
     + "z.shumukenmuKbn, "
     + "false) "
     + "FROM User u "
     + "INNER JOIN Shogu s ON s.userId = u.userId "
     + "AND :kijunbi BETWEEN s.validPeriodSd AND s.validPeriodEd "
     + "AND s.deleteFlg = false "
     + "INNER JOIN ZaisekiSoshiki z ON z.user.userId = u.userId "
     + "AND :kijunbi BETWEEN z.validPeriodSd AND z.validPeriodEd "
     + "AND z.shumukenmuKbn = CASE WHEN :shumukenmuKbn IS NULL THEN z.shumukenmuKbn ELSE :shumukenmuKbn END "
     + "AND z.deleteFlg = false "
     + "WHERE u.kaishaCd = :kaishaCd "
     + "AND u.userId = CASE WHEN :userId IS NULL THEN u.userId ELSE z.user.userId END "
     + "AND :kijunbi BETWEEN u.validPeriodSd AND u.validPeriodEd "
     + "AND u.deleteFlg = false "
     + "AND NOT EXISTS ( "
         + "SELECT ge.userId "
         + "FROM GenericAuthority ge "
         + "WHERE ge.userId = u.userId "
         + "AND :kijunbi BETWEEN ge.validPeriodStartDate AND ge.validPeriodEndDate "
         + "AND ge.role IN :roleList "
         + "AND ge.deleteFlg = false "
     + ") "
     + "ORDER BY z.user.userId")
List<SoshikiKengenInfoDto> getSoshikiKengenInfoByShoguAndZaisekiSoshiki(
    @Param("kaishaCd") KaishaCd kaishaCd,
    @Param("kijunbi") LocalDate kijunbi,
    @Param("userId") Long userId,
    @Param("roleList") List<String> roleList
);



/**
 * 処遇情報取得
 *
 * @param kaishaCd        会社コード
 * @param kijunbi         基準日
 * @param userId          ユーザID
 * @param shumukenmuKbn   主務兼務区分
 * @return 処遇情報
 */
@Query("SELECT new jp.mufg.mjf.bizcommon.auth2.service.dto.SoshikiKengenInfoDto("
     + "s.userId, "
     + "s.validPeriodSd, "
     + "s.validPeriodEd, "
     + "s.zokusei7, "
     + "z.buten.butenId, "
     + "z.buten.butenCd, "
     + "z.kaGroup.kaGroupId, "
     + "z.kaGroup.kaGroupCd, "
     + "z.yakushokuCd, "
     + "COALESCE(z.zaisekiFlg, true), "
     + "s.shumukenmuKbn, "
     + "false) "
     + "FROM User u "
     + "INNER JOIN Shogu s ON s.userId = u.userId "
     + "AND :kijunbi BETWEEN s.validPeriodSd AND s.validPeriodEd "
     + "AND s.deleteFlg = false "
     + "INNER JOIN ZaisekiSoshiki z ON z.user.userId = u.userId "
     + "AND :kijunbi BETWEEN z.validPeriodSd AND z.validPeriodEd "
     + "AND z.shumukenmuKbn = CASE WHEN :shumukenmuKbn IS NULL THEN z.shumukenmuKbn ELSE :shumukenmuKbn END "
     + "AND z.deleteFlg = false "
     + "WHERE u.kaishaCd = :kaishaCd "
     + "AND u.deleteFlg = false "
     + "ORDER BY z.user.userId")
List<SoshikiKengenInfoDto> getSoshikiKengenInfoByShoguAndZaisekiSoshiki(
    @Param("kaishaCd") KaishaCd kaishaCd,
    @Param("kijunbi") LocalDate kijunbi,
    @Param("userId") Long userId,
    @Param("shumukenmuKbn") ShusukenmuKbn shumukenmuKbn
);