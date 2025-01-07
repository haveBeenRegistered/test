-- filepath: /c:/Users/81804/Desktop/hibiki/109.pkb

-- パッケージ内広域変数宣言
g_err_pos NUMBER;
z_err_code VARCHAR2(10);
z_errmsg VARCHAR2(1000);

-- 定数宣言
C_PROGRAM_ID CONSTANT VARCHAR2(10) := 'PROGRAM_ID';
C_PROGRAM_ID_FILE CONSTANT VARCHAR2(10) := 'PROGRAM_ID_FILE';
C_SYSTEM_MIN_DATE CONSTANT DATE := TO_DATE('1900/01/01 00:00:00', 'YYYY/MM/DD HH24:MI:SS');
C_SYSTEM_MAX_DATE CONSTANT DATE := TO_DATE('4712/12/31 23:59:59', 'YYYY/MM/DD HH24:MI:SS');
C_KEKKIN_JIYU_BYOKETU CONSTANT VARCHAR2(1) := '1';
C_KEKKIN_JIYU_TEISI CONSTANT VARCHAR2(1) := '2';
C_KEKKIN_JIYU_SANKYU_U_SA CONSTANT VARCHAR2(1) := '3';
C_KEKKIN_JIYU_SIJI CONSTANT VARCHAR2(1) := '4';
C_KEKKIN_JIYU_KAIGO CONSTANT VARCHAR2(1) := '5';
C_KEKKIN_JIYU_YUKYU_IKUKYU CONSTANT VARCHAR2(1) := '9';
C_FLG_ON CONSTANT VARCHAR2(1) := 'Y';
C_FLG_OFF CONSTANT VARCHAR2(1) := 'N';
C_TORIKESI_FLG_ON CONSTANT VARCHAR2(1) := '1';
C_TORIKESI_FLG_OFF CONSTANT VARCHAR2(1) := '0';
C_STATUS_NEW CONSTANT VARCHAR2(1) := '1';
C_STATUS_SYONIN_ZUMI CONSTANT VARCHAR2(1) := '2';
C_STATUS_SYURYOBI_TYU CONSTANT VARCHAR2(1) := '3';
C_STATUS_SYURYOBI_ZUMI CONSTANT VARCHAR2(1) := '4';
C_STATUS_KIKAN_TYU CONSTANT VARCHAR2(1) := '5';
C_STATUS_TORIKESI_TYU CONSTANT VARCHAR2(1) := '6';
C_STATUS_SYUSSANBI_TYU CONSTANT VARCHAR2(1) := '7';
C_KANRI_STATUS_KBN_END CONSTANT VARCHAR2(1) := 'E';
C_KANRI_STATUS_KBN_TYU CONSTANT VARCHAR2(1) := 'T';
C_CTRL_INFO_NEW CONSTANT VARCHAR2(1) := 'N';
C_CTRL_INFO_UPD CONSTANT VARCHAR2(1) := 'U';
C_CTRL_INFO_DEL CONSTANT VARCHAR2(1) := 'D';
C_BYOMEI_SANKYU CONSTANT VARCHAR2(20) := '産休';
C_BYOMEI_YUKYU_IKUKYU CONSTANT VARCHAR2(20) := '有給育休';
C_SYUTOKU_STATUS_MI CONSTANT VARCHAR2(1) := '0';
C_SYUTOKU_STATUS_SYUTOKU CONSTANT VARCHAR2(1) := '1';
C_SYUTOKU_STATUS_TORIKESI CONSTANT VARCHAR2(1) := '2';
C_HANKYU_KBN_ALL CONSTANT VARCHAR2(1) := 'A';
C_HANKYU_KBN_GOZEN CONSTANT VARCHAR2(1) := 'G';
C_HANKYU_KBN_GOGO CONSTANT VARCHAR2(1) := 'P';
C_CREATED_BY CONSTANT VARCHAR2(20) := 'SYSTEM';
C_CREATION_DATE CONSTANT DATE := SYSDATE;
C_LAST_UPDATE_DATE CONSTANT DATE := SYSDATE;
C_LAST_UPDATED_BY CONSTANT VARCHAR2(20) := 'SYSTEM';
C_LAST_UPDATE_LOGIN CONSTANT NUMBER := TO_NUMBER(FND_PROFILE.VALUE('USER_ID'));
C_TYOKETU_HANDAN_NISSU CONSTANT NUMBER(3) := 60;
C_SAKUJO_ALL CONSTANT VARCHAR2(1) := 'A';
C_SAKUJO_NOT_ALL CONSTANT VARCHAR2(1) := 'N';
C_DUMMY_BYOMET CONSTANT VARCHAR2(20) := 'ダミーレコード';
C_PARAMETER_5_SKIP CONSTANT VARCHAR2(5) := 'SKIP';
C_YUKYUIKUKYU_NISSU_MAX CONSTANT NUMBER(3) := 10;

-- PROCEDURE MAIN
PROCEDURE MAIN(
    errbuf OUT VARCHAR2,
    retcode OUT NUMBER,
    p_syori_ymd VARCHAR2,
    p_koin_no VARCHAR2,
    p_mode VARCHAR2
) IS
    prev_syori_ymd DATE;
    cnt_data_cnt NUMBER;
    warning_cnt NUMBER;
    sg FND_NEW_MESSAGES.MESSAGE_TEXT%TYPE;
    syori_case VARCHAR2(2);
    relational_sequence_no IHR_SYUTU_KEKKIN_DB.SEQUENCE_NO%TYPE;
    prev_sequence_id IHR_KEKKIN_DETAILS.SEQUENCE_ID%TYPE;
    prev_kekkin_kaisibi IHR_KEKKIN_DETAILS.KEKKIN_KAISIBI%TYPE;
    prev_kekkin_syuryobi IHR_KEKKIN_DETAILS.KEKKIN_SYURYOBI%TYPE;
    prev_status IHR_KEKKIN_DETAILS.STATUS%TYPE;
    EXCEPTION
        WHEN OTHERS THEN
            errbuf := SQLERRM;
            retcode := 2;

-- CURSOR cur_kekkin
CURSOR cur_kekkin (
    p_prev_syori_ymd DATE,
    p_syori_ymd DATE,
    p_koin_no VARCHAR2
) IS
    SELECT
        kek.KOIN_NO,
        kek.PERSON_ID,
        kek.SEQUENCE_ID,
        kek.KEKKIN_KAISIBI,
        NVL(kek.KEKKIN_SYURYOBI, kek.KEKKIN_SYURYO_YOTEIBI),
        kek.TOSYO_KEKKIN_KAISIBI,
        kek.KEKKIN_JIYU,
        kek.DESCRIPTION,
        kek.STATUS,
        kek.TORIKESI_FLG,
        kek.SYONINBI,
        db.SYUKKEKKIN_DB_SEQUENCE_NO,
        NULL AS SANZEN_TOKUKYU_KAISIBI,
        NULL AS SANZEN_TOKUKYU_SYURYOBI,
        NULL AS IKUKYU_KAISIBI,
        NULL AS IKUKYU_SYURYOBI,
        NULL AS SYUSSAN_YOTEIBI,
        NULL AS SYUSSANBI,
        NULL AS SANZEN_TOKUKYU_STATUS,
        NULL AS SANZEN_KYUMU_STATUS,
        NULL AS SANGO_KYUMU_STATUS,
        NULL AS IKUKYU_STATUS,
        NULL AS TYOKETU_KAISIBI,
        NULL AS TYOKETU_SYURYOBI,
        NULL AS YUKYU_IKUKYU_STATUS,
        NULL AS SYUKKEKKIN_DB_SEQUENCE_NO
    FROM
        IHR_KEKKIN_DETAILS kek
        LEFT JOIN IHR_SYUTU_KEKKIN_DB db ON kek.PERSON_ID = db.PERSON_ID
        AND kek.SYUKKEKKIN_DB_SEQUENCE_NO = db.SEQUENCE_NO
    WHERE
        kek.SYONINBI BETWEEN p_prev_syori_ymd AND p_syori_ymd
        AND (kek.KOIN_NO = p_koin_no OR p_koin_no IS NULL)
        AND kek.KEKKIN_JIYU IN (C_KEKKIN_JIYU_BYOKETU, C_KEKKIN_JIYU_TEISI, C_KEKKIN_JIYU_SIJI, C_KEKKIN_JIYU_KAIGO)
        AND IHR_KINMU_COMMON.TS_KOINGAT_PARTNER_SS(kek.KOIN_NO, NULL) = 'Y'
    UNION ALL
    SELECT
        KOIN_NO,
        PERSON_ID,
        SEQUENCE_ID,
        NULL AS KEKKIN_KAISIBI,
        NULL AS KEKKIN_SYURYOBI,
        NULL AS TOSYO_KEKKIN_KAISIBI,
        C_KEKKIN_JIYU_SANKYU AS KEKKIN_JIYU,
        DESCRIPTION,
        STATUS,
        TORIKESI_FLG,
        SYONINBI,
        SYUKKEKKIN_DB_SEQUENCE_NO,
        NVL(SANGO_KYUMU_SYURYOBI, SANGO_KYUMU_SYURYO_YOTEIBI) AS SANGO_KYUMU_SYURYOBI,
        NULL AS IKUKYU_KAISIBI,
        NVL(IKUKYU_SYURYOBI, IKUKYU_SYURYO_YOTEIBI) AS IKUKYU_SYURYOBI,
        SYUSSAN_YOTEIBI,
        SYUSSANBI,
        SANZEN_TOKUKYU_STATUS,
        SANZEN_KYUMU_STATUS,
        SANGO_KYUMU_STATUS,
        IKUKYU_STATUS,
        TYOKETU_KAISIBI,
        TYOKETU_SYURYOBI,
        YUKYU_IKUKYU_STATUS,
        SYUKKEKKIN_DB_SEQUENCE_NO
    FROM
        IHR_SANKYU_IKUKYU_DETAILS
    WHERE
        SYONINBI BETWEEN p_prev_syori_ymd AND p_syori_ymd
        AND (KOIN_NO = p_koin_no OR p_koin_no IS NULL)
    ORDER BY
        KOIN_NO, KEKKIN_JIYU, KEKKIN_KAISIBI, SYONINBI;


/* 2005/07/12 START */

/* 管理中データの終了処理用カーソル
   2005-12-1修正 コントロール情報も抽出 */

-- 定义游标 cur_end_kanri_status
CURSOR cur_end_kanri_status(
    p_prev_syori_ymd DATE,  -- 上次处理日期
    p_syori_ymd DATE,       -- 处理日期
    p_syori_ym VARCHAR2     -- 处理年月
) IS
    -- 第一个查询
    SELECT
        db1.PERSON_ID,       -- 员工ID
        db1.SEQUENCE_NO      -- 序列号
    FROM
        IHR_SYUTU_KEKKIN_DB db1,  -- 缺勤数据库表
        IHR_KEKKIN_DETAILS kek    -- 缺勤详细信息表
    WHERE
        kek.PERSON_ID = db1.PERSON_ID  -- 员工ID匹配
        AND db1.SEQUENCE_NO = kek.SYUKKEKKIN_DB_SEQUENCE_NO  -- 序列号匹配
        AND kek.STATUS = C_STATUS_SYURYOBI_ZUMI  -- 状态为结束
        AND kek.KEKKIN_SYURYOBI < TO_DATE(p_syori_ym || '01', 'YYYYMMDD')  -- 缺勤结束日期小于处理年月的第一天
        AND kek.SYONINBI NOT BETWEEN p_prev_syori_ymd AND p_syori_ymd  -- 批准日期不在上次处理日期和处理日期之间
        AND db1.KANRI_STATUS_KBN = C_KANRI_STATUS_KBN_TYU  -- 管理状态为处理中
        -- 2006/02/13追加
        AND kek.torikesi_flg = '0'  -- 取消标志为0
    UNION ALL
    -- 第二个查询
    SELECT
        db1.PERSON_ID,       -- 员工ID
        db1.SEQUENCE_NO      -- 序列号
    FROM
        IHR_SYUTU_KEKKIN_DB db1,  -- 缺勤数据库表
        IHR_SANKYU_IKUKYU_DETAILS san  -- 产休育休详细信息表
    WHERE
        san.PERSON_ID = db1.PERSON_ID  -- 员工ID匹配
        AND san.SANKYU_SAISYU_SYURYOBI < TO_DATE(p_syori_ym || '01', 'YYYYMMDD')  -- 产休结束日期小于处理年月的第一天
        AND db1.SEQUENCE_NO = san.SYUKKEKKIN_SEQUENCE_NO  -- 序列号匹配
        AND san.STATUS = C_STATUS_SYURYOBI_ZUMI  -- 状态为结束
        AND san.SYONINBI NOT BETWEEN p_prev_syori_ymd AND p_syori_ymd  -- 批准日期不在上次处理日期和处理日期之间
        AND db1.KANRI_STATUS_KBN = C_KANRI_STATUS_KBN_TYU  -- 管理状态为处理中
        AND db1.KEKKIN_JIYU = C_KEKKIN_JIYU_SANKYU  -- 缺勤理由为产休
        -- 2006/02/13追加
        AND san.torikesi_flg = '0';  -- 取消标志为0

/* 2005/08/16 END */
/* 2005/07/12 END */

BEGIN
    g_err_pos := '00000'; -- 初始化错误位置
    l_syori_ymd := NULL; -- 初始化处理日期
    l_koin_no := p_koin_no; -- 将输入的员工编号赋值给局部变量
    l_mode := p_mode; -- 将输入的模式赋值给局部变量
    l_data_cnt := 0; -- 初始化数据计数
    l_warning_cnt := 0; -- 初始化警告计数

    -- 处理结束时，通过 GET_PROC_PARAM 将此处理的执行信息
    -- 注册到并发结果表中。
    -- 设置并发结果表的注册参数
    -- 当参数检查正常进行时，将此过程的参数设置为注册参数。
    IHR_IHRCSBT00_PKG.row_concurrent_result := NULL; -- 初始化并发结果记录
    IHR_IHRCSBT00_PKG.row_concurrent_result.PROGRAM_ID := C_PROGRAM_ID; -- 设置程序ID
    IHR_IHRCSBT00_PKG.row_concurrent_result.SYORI_YMD := SYSDATE; -- 设置处理日期为当前系统日期

    g_err_pos := '00100'; -- 更新错误位置

    /* 参数检查。
       检查传递的参数。
       对处理期间From、处理期间To、输出目录、
       输出文件分别进行检查，如果有错误则结束处理 */

    -- 处理日期的检查
    -- 如果参数为NULL，则设置为系统日期，
    -- 如果日期值不正确，则输出日志后
    -- 返回FALSE
    -- 获取从字符串类型转换为日期类型的值
    IF NOT IHR_IHRCSBT00_PKG.CHK_DATE(p_syori_ymd, l_syori_ymd) THEN
        RAISE EXC_PARAM_CHK; -- 抛出参数检查异常
    END IF;

    -- 获取处理日期的年月
    l_syori_ym := TO_CHAR(l_syori_ymd, 'YYYYMM'); -- 将处理日期转换为年月格式

    g_err_pos := '00200'; -- 更新错误位置

-- filepath: /c:/Users/81804/Desktop/hibiki/109.pkb

-- 检查处理日期的年月的缺勤信息文件是否已经结束
-- 如果并发结果表中存在处理日期的缺勤信息文件创建的执行日志，则认为该月份已经结束
-- 处理模式指定

g_err_pos := '00300'; -- 更新错误位置

-- 查询并发结果表中是否存在当前处理年月的缺勤信息文件创建的执行日志
SELECT
    COUNT(ROWID) -- 统计行数
INTO
    I_cnt -- 将结果存入 I_cnt 变量
FROM
    IHR_CONCURRENT_RESULT -- 查询并发结果表
WHERE
    PROGRAM_ID = C_PROGRAM_ID_FILE -- 程序ID匹配
    AND PARAMETER_4 = l_syori_ym -- 参数4匹配处理年月
    AND STATUS IN (IHR_IHRCSBT00_PKG.C_RESULT_NORMAL, IHR_IHRCSBT00_PKG.C_RESULT_WARNING); -- 状态为正常或警告

-- 如果查询结果不为0，表示该月份的缺勤信息文件已经创建
IF I_cnt <> 0 THEN
    IF l_mode IS NULL THEN
        -- 手动执行的情况下，如果缺勤信息文件已经创建，则报错
        IF NOT IHR_IHRCSBT00_PKG.PUT_MSG('IHRHR267', TO_CHAR(l_syori_ymd, 'YYYY/MM/DD HH24:MI:SS')) THEN
            NULL; -- 如果消息未能成功放入，则执行空操作
        END IF;
        RAISE EXC_PARAM_CHK; -- 抛出参数检查异常
    ELSE
        -- 夜间批处理模式的情况下，将处理对象更改为下个月，并重新检查是否可以执行
        l_syori_ym := TO_CHAR(ADD_MONTHS(TO_DATE(l_syori_ym, 'YYYYMM'), 1), 'YYYYMM'); -- 将处理年月加一个月

        -- 再次查询并发结果表中是否存在新的处理年月的缺勤信息文件创建的执行日志
        SELECT
            COUNT(ROWID) -- 统计行数
        INTO
            I_cnt -- 将结果存入 I_cnt 变量
        FROM
            IHR_CONCURRENT_RESULT -- 查询并发结果表
        WHERE
            PROGRAM_ID = C_PROGRAM_ID_FILE -- 程序ID匹配
            AND PARAMETER_4 = l_syori_ym -- 参数4匹配新的处理年月
            AND STATUS IN (IHR_IHRCSBT00_PKG.C_RESULT_NORMAL, IHR_IHRCSBT00_PKG.C_RESULT_WARNING); -- 状态为正常或警告

        -- 如果查询结果大于0，表示新的处理年月的缺勤信息文件已经创建
        IF I_cnt > 0 THEN
            IF NOT IHR_IHRCSBT00_PKG.PUT_MSG('IHRHR267', TO_CHAR(l_syori_ymd, 'YYYY/MM/DD HH24:MI:SS')) THEN
                NULL; -- 如果消息未能成功放入，则执行空操作
            END IF;
            RAISE EXC_PARAM_CHK; -- 抛出参数检查异常
        END IF;
    END IF;
ELSE
-- 如果缺勤信息文件还未创建
-- 夜间批处理模式的情况下，检查是否手动创建了缺勤接口信息，
-- 如果已经创建，则不再进行信息创建。

IF l_mode IS NOT NULL THEN
    -- 手动创建缺勤接口信息的并发日志
    SELECT
        COUNT(ROWID) -- 统计行数
    INTO
        I_cnt -- 将结果存入 I_cnt 变量
    FROM
        IHR_CONCURRENT_RESULT -- 查询并发结果表
    WHERE
        PROGRAM_ID = C_PROGRAM_ID -- 程序ID匹配
        AND STATUS IN (IHR_IHRCSBT00_PKG.C_RESULT_NORMAL, IHR_IHRCSBT00_PKG.C_RESULT_WARNING) -- 状态为正常或警告
        AND PARAMETER_4 IS NULL; -- 参数4为空

    IF I_cnt > 0 THEN
        -- 只进行并发结果表的注册准备，跳过结束处理
        IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_1 := TO_CHAR(l_syori_ymd, 'YYYYMMDDHH24MISS'); -- 设置参数1为处理日期时间
        IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_2 := TO_CHAR(l_koin_no); -- 设置参数2为员工编号
        IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_3 := l_syori_ym; -- 设置参数3为处理年月
        IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_4 := NULL; -- 设置参数4为空
        IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_5 := C_PARAMETER_5_SKIP; -- 设置参数5为跳过标志

        GOTO skip_process; -- 跳过处理
    END IF;
END IF;

g_err_pos := '00400'; -- 更新错误位置

/* 出欠勤DB退避テーブルへのデータ的退避处理
   今月处理的初次执行时，将出欠勤DB退避テーブル中的数据全部删除，
   如果处理年月与处理日期的年月一致的数据存在，
   则判断为再处理，不进行数据的退避。
   如果不存在，则判断为今月处理的初次执行，进行数据的退避。 */

-- 欠勤申请退避テーブルへのデータ的退避处理
-- 今月处理的初次执行时，将欠勤申请退避テーブル中的数据全部删除。
-- 从欠勤申请DB表中的所有数据创建欠勤申请退避テーブル的数据。

IF I_cnt = 0 THEN
    -- 删除出欠勤DB退避テーブル中的所有数据
    DELETE FROM IHR_SYUTU_KEKKIN_DB_BACKUP;
    -- 将出欠勤DB表中的数据插入到退避表中
    INSERT INTO IHR_SYUTU_KEKKIN_DB_BACKUP(
        CTRL_INFO,
        KOIN_NO,
        PERSON_ID,
        SEQUENCE_NO,
        KANRI_STATUS_KBN,
        KEKKIN_JIYU,
        KEKKIN_KAISIBI,
        KEKKIN_SYURYOBI,
        BYOMEI,
        SANZEN_TOKUKYU_KAISIBI,
        SANZEN_TOKUKYU_SYURYOBI,
        SANZEN_KYUMU_KAISIBI,
        SANZEN_KYUMU_SYURYOBI,
        SANGO_KYUMU_KAISIBI,
        SANGO_KYUMU_SYURYOBI,
        IKUKYU_KAISIBI,
        IKUKYU_SYURYOBI,
        SYUSSAN_YOTEIBI,
        SYUSSANBI,
        SOSIN_TAISYO_YM,
        TYOKETU_KAISIBI,
        TYOKETU_SYURYOBI,
        SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE,
        LAST_UPDATE_LOGIN,
        LAST_UPDATED_BY,
        CREATED_BY,
        CREATION_DATE,
        YUKYU_IKUKYU_SEQUENCE_NO,
        TOSYO_KEKKIN_KAISIBI -- 2013/10/02追加
    )
    SELECT
        CTRL_INFO,
        KOIN_NO,
        PERSON_ID,
        SEQUENCE_NO,
        KANRI_STATUS_KBN,
        KEKKIN_JIYU,
        KEKKIN_KAISIBI,
        KEKKIN_SYURYOBI,
        BYOMEI,
        SANZEN_TOKUKYU_KAISIBI,
        SANZEN_TOKUKYU_SYURYOBI,
        SANZEN_KYUMU_KAISIBI,
        SANZEN_KYUMU_SYURYOBI,
        SANGO_KYUMU_KAISIBI,
        SANGO_KYUMU_SYURYOBI,
        IKUKYU_KAISIBI,
        IKUKYU_SYURYOBI,
        SYUSSAN_YOTEIBI,
        SYUSSANBI,
        SOSIN_TAISYO_YM,
        TYOKETU_KAISIBI,
        TYOKETU_SYURYOBI,
        SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE,
        LAST_UPDATE_LOGIN,
        LAST_UPDATED_BY,
        CREATED_BY,
        CREATION_DATE,
        YUKYU_IKUKYU_SEQUENCE_NO,
        TOSYO_KEKKIN_KAISIBI -- 2013/10/02追加
    FROM
        IHR_SYUTU_KEKKIN_DB;

    g_err_pos := '00800'; -- 更新错误位置

    -- 删除欠勤详细信息退避表中的所有数据
    DELETE FROM IHR_KEKKIN_DETAILS_BACKUP;
    -- 将欠勤详细信息表中的数据插入到退避表中
    INSERT INTO IHR_KEKKIN_DETAILS_BACKUP(
        PERSON_ID,
        SEQUENCE_ID,
        SYUKKEKKIN_SEQUENCE_NO,
        SYUKKEKKIN_DB_SEQUENCE_NO,
        YUKYU_IKUKYU_SEQUENCE_NO, -- 2013/10/02追加
        SYUKKEKKIN_SANKYU_SOSIN_FLG -- 2007/09/05追加
    )
    SELECT
        PERSON_ID,
        SEQUENCE_ID,
        SYUKKEKKIN_SEQUENCE_NO,
        SYUKKEKKIN_DB_SEQUENCE_NO,
        YUKYU_IKUKYU_SEQUENCE_NO, -- 2013/10/02追加
        SYUKKEKKIN_SANKYU_SOSIN_FLG -- 2007/09/05追加
    FROM
        IHR_KEKKIN_DETAILS
    UNION ALL
    SELECT
        PERSON_ID,
        SEQUENCE_ID,
        SYUKKEKKIN_SEQUENCE_NO,
        SYUKKEKKIN_DB_SEQUENCE_NO,
        YUKYU_IKUKYU_SEQUENCE_NO, -- 2013/10/02追加
        SYUKKEKKIN_SANKYU_SOSIN_FLG -- 2007/09/05追加
    FROM
        IHR_SANKYU_IKUKYU_DETAILS;
END IF;

g_err_pos := '00900'; -- 更新错误位置


/* 出欠勤DB退避テーブルから出欠勤DBテーブルへの数据的插入
   再处理的情况下，将出欠勤DB表中的数据全部删除，
   并将出欠勤DB退避表中的所有数据插入到出欠勤DB表中。
   (恢复到上个月的状态并重新处理。) */

/* 欠勤申请退避テーブルから欠勤申请テーブル、産休・育休テーブルへの数据的更新
   再处理的情况下，从欠勤申请退避表更新欠勤申请表和产休・育休表的数据。
   (恢复到上个月的状态并重新处理。) */

/* また、今月処理の再実行時には、
   今月処理の初回実行時~今月処理の再実行時までに欠勤申請画面、産休・育休申請画面から
   追加・更新されたデータがある場合、欠勤申請退避テーブルに追加・更新分を反映する。 */

IF I_cnt = 0 THEN
    -- 删除出欠勤DB表中的所有数据
    DELETE FROM IHR_SYUTU_KEKKIN_DB
    WHERE
        KOIN_NO = p_koin_no OR p_koin_no IS NULL;

    -- 将出欠勤DB退避表中的数据插入到出欠勤DB表中
    INSERT INTO IHR_SYUTU_KEKKIN_DB(
        CTRL_INFO,
        KOIN_NO,
        PERSON_ID,
        SEQUENCE_NO,
        KANRI_STATUS_KBN,
        KEKKIN_JIYU,
        KEKKIN_KAISIBI,
        KEKKIN_SYURYOBI,
        BYOMEI,
        SANZEN_TOKUKYU_KAISIBI,
        SANZEN_TOKUKYU_SYURYOBI,
        SANZEN_KYUMU_KAISIBI,
        SANZEN_KYUMU_SYURYOBI,
        SANGO_KYUMU_KAISIBI,
        SANGO_KYUMU_SYURYOBI,
        IKUKYU_KAISIBI,
        IKUKYU_SYURYOBI,
        SYUSSAN_YOTEIBI,
        SYUSSANBI,
        SOSIN_TAISYO_YM,
        TYOKETU_KAISIBI,
        TYOKETU_SYURYOBI,
        SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE,
        LAST_UPDATED_BY,
        LAST_UPDATE_LOGIN,
        CREATED_BY,
        CREATION_DATE,
        YUKYU_IKUKYU_SEQUENCE_NO, -- 2007/06/05追加
        TOSYO_KEKKIN_KAISIBI -- 2013/10/02追加
    )
    SELECT
        CTRL_INFO,
        KOIN_NO,
        PERSON_ID,
        SEQUENCE_NO,
        KANRI_STATUS_KBN,
        KEKKIN_JIYU,
        KEKKIN_KAISIBI,
        KEKKIN_SYURYOBI,
        BYOMEI,
        SANZEN_TOKUKYU_KAISIBI,
        SANZEN_TOKUKYU_SYURYOBI,
        SANZEN_KYUMU_KAISIBI,
        SANZEN_KYUMU_SYURYOBI,
        SANGO_KYUMU_KAISIBI,
        SANGO_KYUMU_SYURYOBI,
        IKUKYU_KAISIBI,
        IKUKYU_SYURYOBI,
        SYUSSAN_YOTEIBI,
        SYUSSANBI,
        SOSIN_TAISYO_YM,
        TYOKETU_KAISIBI,
        TYOKETU_SYURYOBI,
        SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE,
        LAST_UPDATED_BY,
        LAST_UPDATE_LOGIN,
        CREATED_BY,
        CREATION_DATE,
        YUKYU_IKUKYU_SEQUENCE_NO, -- 2007/06/05追加
        TOSYO_KEKKIN_KAISIBI -- 2013/10/02追加
    FROM
        IHR_SYUTU_KEKKIN_DB_BACKUP
    WHERE
        KOIN_NO = p_koin_no OR p_koin_no IS NULL;

    g_err_pos := '01000'; -- 更新错误位置

    -- 更新欠勤详细信息表中的数据
    UPDATE IHR_KEKKIN_DETAILS kek
    SET
        SYUKKEKKIN_DB_SEQUENCE_NO = (
            SELECT
                kek_bak1.SYUKKEKKIN_DB_SEQUENCE_NO
            FROM
                IHR_KEKKIN_DETAILS_BACKUP kek_bak1
            WHERE
                kek_bak1.TABLE_KBN = '1'
                AND kek_bak1.PERSON_ID = kek.PERSON_ID
                AND kek_bak1.SEQUENCE_ID = kek.SEQUENCE_ID
        )
    WHERE
        (KOIN_NO = p_koin_no OR p_koin_no IS NULL)
        AND EXISTS (
            SELECT
                ROWID
            FROM
                IHR_KEKKIN_DETAILS_BACKUP kek_bak2
            WHERE
                kek_bak2.TABLE_KBN = '1'
                AND kek_bak2.PERSON_ID = kek.PERSON_ID
                AND kek_bak2.SEQUENCE_ID = kek.SEQUENCE_ID
        );

    g_err_pos := '01100'; -- 更新错误位置

    -- 更新产休・育休详细信息表中的数据
    UPDATE IHR_SANKYU_IKUKYU_DETAILS san
    SET
        SYUKKEKKIN_SEQUENCE_NO = (
            SELECT
                kek_bak1.SYUKKEKKIN_DB_SEQUENCE_NO
            FROM
                IHR_KEKKIN_DETAILS_BACKUP kek_bak1
            WHERE
                kek_bak1.TABLE_KBN = '2'
                AND kek_bak1.PERSON_ID = san.PERSON_ID
                AND kek_bak1.SEQUENCE_ID = san.SEQUENCE_ID
        ),
        YUKYU_IKUKYU_SEQUENCE_NO = (
            SELECT
                kek_bak1.YUKYU_IKUKYU_SEQUENCE_NO
            FROM
                IHR_KEKKIN_DETAILS_BACKUP kek_bak1
            WHERE
                kek_bak1.TABLE_KBN = '2'
                AND kek_bak1.PERSON_ID = san.PERSON_ID
                AND kek_bak1.SEQUENCE_ID = san.SEQUENCE_ID
        ),
        SYUKKEKKIN_SANKYU_SOSIN_FLG = (
            SELECT
                kek_bak1.SYUKKEKKIN_SANKYU_SOSIN_FLG
            FROM
                IHR_KEKKIN_DETAILS_BACKUP kek_bak1
            WHERE
                kek_bak1.TABLE_KBN = '2'
                AND kek_bak1.PERSON_ID = san.PERSON_ID
                AND kek_bak1.SEQUENCE_ID = san.SEQUENCE_ID
        )
    WHERE
        (KOIN_NO = p_koin_no OR p_koin_no IS NULL)
        AND EXISTS (
            SELECT
                ROWID
            FROM
                IHR_KEKKIN_DETAILS_BACKUP kek_bak2
            WHERE
                kek_bak2.TABLE_KBN = '2'
                AND kek_bak2.PERSON_ID = san.PERSON_ID
                AND kek_bak2.SEQUENCE_ID = san.SEQUENCE_ID
        );

INSERT INTO IHR_KEKKIN_DETAILS_BACKUP(
    TABLE_KBN,
    PERSON_ID,
    SEQUENCE_ID,
    SYUKKEKKIN_SEQUENCE_NO,
    SYUKKEKKIN_DB_SEQUENCE_NO,
    YUKYU_IKUKYU_SEQUENCE_NO,
    SYUKKEKKIN_SANKYU_SOSIN_FLG -- 2013/10/02 追加
    -- 2007/06/05 追加
    -- 2007/09/05 追加
)
SELECT
    '1',
    kek.PERSON_ID,
    kek.SEQUENCE_ID,
    kek.SYUKKEKKIN_SEQUENCE_NO,
    kek.SYUKKEKKIN_DB_SEQUENCE_NO,
    kek.YUKYU_IKUKYU_SEQUENCE_NO,
    kek.SYUKKEKKIN_SANKYU_SOSIN_FLG -- 2013/10/02 追加
FROM
    IHR_KEKKIN_DETAILS kek
WHERE
    NOT EXISTS (
        SELECT
            ROWID
        FROM
            IHR_KEKKIN_DETAILS_BACKUP bak
        WHERE
            bak.TABLE_KBN = '1'
            AND bak.PERSON_ID = kek.PERSON_ID
            AND bak.SEQUENCE_ID = kek.SEQUENCE_ID
    )
UNION ALL
SELECT
    '2',
    san.PERSON_ID,
    san.SEQUENCE_ID,
    san.SYUKKEKKIN_SEQUENCE_NO,
    san.SYUKKEKKIN_DB_SEQUENCE_NO,
    san.YUKYU_IKUKYU_SEQUENCE_NO,
    san.SYUKKEKKIN_SANKYU_SOSIN_FLG -- 2013/10/02 追加
FROM
    IHR_SANKYU_IKUKYU_DETAILS san
WHERE
    NOT EXISTS (
        SELECT
            ROWID
        FROM
            IHR_KEKKIN_DETAILS_BACKUP bak
        WHERE
            bak.TABLE_KBN = '2'
            AND bak.PERSON_ID = san.PERSON_ID
            AND bak.SEQUENCE_ID = san.SEQUENCE_ID
    );

END IF;

g_err_pos := '01200';

-- コンカレント結果テーブルへの登録用パラメータの設定
-- 処理終了時、GET_PROC_PARAMによってこの処理の実行情報が
-- コンカレント結果テーブルに登録される。

IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_1 := TO_CHAR(l_syori_ymd, 'YYYYMMDDHH24MISS');
IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_2 := TO_CHAR(l_koin_no); 

-- 処理日の年月をパラメータ3に格納する。(再処理判定用)
IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_3 := l_syori_ym;

-- 処理モードをパラメータ4に格納する。
IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_4 := l_mode;

-- 実行状況をパラメータ5に格納する。
IHR_IHRCSBT00_PKG.row_concurrent_result.PARAMETER_5 := NULL;

g_err_pos := '01300';

-- 欠勤申請警告テーブルのデータを全件削除する。
DELETE FROM IHR_KEKKIN_DETAILS_WARNING;

g_err_pos := '01400';

-- 前月実行時の処理日の取得
-- (処理日の年月)が今月以外かつ、処理スキップフラグでなく処理
-- ステータスが正常、または警告に該当するレコードのうち、
-- パラメータ(処理日)が最大のものを抽出する。
SELECT
    I_prev_syori_ymd
INTO
    l_prev_syori_ymd
FROM
    IHR_CONCURRENT_RESULT
WHERE
    PROGRAM_ID = C_PROGRAM_ID
    AND PARAMETER_3 = l_syori_ym
    AND PARAMETER_5 IS NULL
    AND STATUS IN (IHR_IHRCSBT00_PKG.C_RESULT_NORMAL, IHR_IHRCSBT00_PKG.C_RESULT_WARNING);

-- 取得できなかったときは、システム上の最小日をセットする。
l_prev_syori_ymd := NVL(l_prev_syori_ymd, C_SYSTEM_MIN_DATE);

g_err_pos := '01500';

-- 欠勤申請テーブル、産休・育休テーブルからのレコード取得カーソルをオープンし、各レコードに対して処理を行う。
FOR row_kekkin IN cur_kekkin(
    p_prev_syori_ymd => l_prev_syori_ymd,
    p_syori_ymd => l_syori_ymd,
    p_koin_no => l_koin_no
) LOOP
    -- 中間変数を初期化
    l_syori_case := NULL;
    g_row_syutu_kekkin_db := NULL;
    l_msg := NULL;
    l_syori_case := NULL;
    l_relation := NULL;
    l_relational_sequence_no := NULL;
    l_prev_sequence_id := NULL;
    l_prev_kekkin_kaisibi := NULL;
    l_prev_kekkin_syuryobi := NULL;
    l_prev_status := NULL;

    g_err_pos := '01600';

    -- どの分岐の処理になるのかを判断する
    -- 傷病による長期欠勤の場合
    -- 欠勤事由が「病欠」の場合、長期欠勤明細を作成・更新する処理を行う。
    IF row_kekkin.KEKKIN_JIYU = C_KEKKIN_JIYU_BYOKETU THEN
        -- 抽出データが長期欠勤明細を構成するのに既に使われているかを判断
        -- 抽出データの出欠勤DBシーケンスNoがNULLかどうかを調べて判断する。
        -- NULLの場合は使われていない。NULLでない場合は使われている。
        -- 出欠勤DBに当該の明細が存在する場合(欠勤申請,出欠勤DBシーケンスNo = NULL)
        IF row_kekkin.SKDB_SEQ_NO IS NOT NULL THEN
            -- 欠勤申請,取消済みフラグ='1'(取消)の場合
            IF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_ON THEN
                -- CASE1
                l_syori_case := '1';
            -- 取消明細以外の場合
            ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF THEN
                -- 長欠明細でなく、且つ当初開始日=欠勤開始日の場合
                IF NOT IS_TYOKETU(row_kekkin.KEKKIN_KAISIBI, row_kekkin.KEKKIN_SYURYOBI) AND GET_DATE_COUNT(row_kekkin.TOSYO_KEKKIN_KAISIBI, row_kekkin.KEKKIN_KAISIBI) = 0 THEN
                    -- CASE1
                    l_syori_case := '1';
                -- 上記以外の場合
                ELSE
                    -- CASE2
                    l_syori_case := '2';
                END IF;
            END IF;
-- 出欠勤DBに当該の明細が存在しない場合(欠勤申請、出欠勤DBシーケンスNo = NULL)
ELSIF row_kekkin.SKDB_SEQ_NO IS NULL THEN
    -- 欠勤申請,取消済みフラグ='1'(取消)の場合
    IF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_ON THEN
        -- 欠勤申請取消の処理がホスト送信前に行われた。
        -- ホストIF対象外と判断し、何も処理はしない。
        l_syori_case := '99';
    -- 取消明細以外、且つ当初開始日=欠勤開始日の場合
    ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF AND row_kekkin.TOSYO_KEKKIN_KAISIBI = row_kekkin.KEKKIN_KAISIBI THEN
        -- 長欠明細の確認
        -- 長欠明細でない場合
        IF NOT IS_TYOKETU(row_kekkin.KEKKIN_KAISIBI, row_kekkin.KEKKIN_SYURYOBI) THEN
            -- 当該の明細は長欠明細ではなく、且つ過去に長欠明細も申請されていない。
            -- 当該の明細はホストIF対象外と判断し、何も処理はしない。
            l_syori_case := '99';
        -- 長欠明細の場合
        ELSE
            l_syori_case := '9';
        END IF;
    -- 取消明細以外、且つ当初開始日≠欠勤開始日の場合
    ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF AND GET_DATE_COUNT(row_kekkin.TOSYO_KEKKIN_KAISIBI, row_kekkin.KEKKIN_KAISIBI) <> 0 THEN
        l_syori_case := '9';
    END IF;
END IF;

/* 私事による欠勤・出勤停止・介護休業の場合 */
ELSIF row_kekkin.KEKKIN_JIYU IN (C_KEKKIN_JIYU_TEISI, C_KEKKIN_JIYU_SIJI, C_KEKKIN_JIYU_KAIGO) THEN
    -- 抽出データが出欠勤DBテーブルのデータとして使われているかを判断
    -- 抽出データの出欠勤DBシーケンスNoがNULLかどうかを調べて判断する。
    -- NULLの場合は使われていない。NULLでない場合は使われている。

    -- 既に使われている場合
    -- 出欠勤DBテーブルには、コントロール情報「新規」
    IF row_kekkin.SKDB_SEQ_NO IS NULL THEN
        -- 抽出データが取消されている。
        IF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_ON THEN
            -- CASE99
            l_syori_case := '99';
        -- 抽出データが取消されていない。
        ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF THEN
            -- CASE9
            l_syori_case := '9';
        END IF;
    -- 使われていない場合
    -- 抽出データの取消済みフラグがOnの場合、出欠勤DBテーブルには、コントロール情報"D":削除でデータを更新する。
    -- 抽出データの取消済みフラグがOffの場合、出欠勤DBテーブルには、コントロール情報"U":削除でデータを更新する。
    ELSIF row_kekkin.SKDB_SEQ_NO IS NOT NULL THEN
        -- 抽出データが取消されている。
        IF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_ON THEN
            -- CASE10
            l_syori_case := '10';
        -- 抽出データが取消されていない。
        ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF THEN
            -- CASE11
            l_syori_case := '11';
        END IF;
    END IF;
-- 産休・育休の場合
ELSIF row_kekkin.KEKKIN_JIYU = C_KEKKIN_JIYU_SANKYU THEN
    -- 抽出データが出欠勤DBテーブルのデータとして使われているかを判断
    -- 抽出データの出欠勤DBシーケンスNoがNULLかどうかを調べて判断する。
    -- NULLの場合は使われていない。NULLでない場合は使われている。

    -- 既に使われている場合
    -- 出欠勤DBテーブルには、コントロール情報「新規」でデータを挿入する。
    -- ただし、抽出データの取消済みフラグがOnの場合、データを挿入しない。
    IF row_kekkin.SKDB_SEQ_NO IS NULL THEN
        -- 抽出データが取消されていない。
        IF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_ON THEN
            -- CASE99
            l_syori_case := '99';

            -- 2007/08/27 START
            -- 明細の出欠勤シーケンスNOが設定されていなくても、
            -- 有給部分のみ送信された時
            IF row_kekkin.YUKYU_IKUKYU_SEQUENCE_NO IS NOT NULL THEN
                -- 以下では新規申請の処理を設定しているが、ここで新規に作成される産休育休レコードは9営業日以下の為、
                -- 有給部分で、有給育休レコードに置き換えられて、ホスト送信されない。
                -- (有給休暇処理部にて、上記レコードの備考を「ダミーレコード」と設定する。)
                -- ただ、産休育休レコードを作成しておかないと、有給部の処理が行われない。
                l_syori_case := '12';
            END IF;
        ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF THEN
            -- CASE12
            l_syori_case := '12';
        END IF;

        -- 使われていない場合
-- 抽出データの取消済みフラグがOnの場合、
-- 出欠勤DBテーブルには、コントロール情報"D":削除でデータを更新する。
-- 抽出データの取消済みフラグがOffの場合、
-- 出欠勤DBテーブルには、コントロール情報"U":削除でデータを更新する。
ELSIF row_kekkin.SKDB_SEQ_NO IS NOT NULL THEN
    -- 抽出データが取消されている。
    IF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_ON THEN
        -- CASE13
        l_syori_case := '13';
    -- 抽出データが取消されていない。
    ELSIF row_kekkin.TORIKESI_FLG = C_TORIKESI_FLG_OFF THEN
        -- CASE14
        l_syori_case := '14';
    END IF;
END IF;

g_err_pos := '01700';

-- 実処理
-- 上で判断した分岐に対して、実際の処理を行う。

-- 全ての分岐で共通して使用するカラムをあらかじめセットしておく。
-- 3.Person ID
-- 20.送信対象年月
-- 23.送信対象フラグ
-- 24~28.HOカラム
g_row_syutu_kekkin_db.PERSON_ID := row_kekkin.PERSON_ID;
g_row_syutu_kekkin_db.SOSIN_TAISYO_YM := l_syori_ym;
g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := C_FLG_ON;
g_row_syutu_kekkin_db.LAST_UPDATE_DATE := C_LAST_UPDATE_DATE;
g_row_syutu_kekkin_db.LAST_UPDATED_BY := C_LAST_UPDATED_BY;
g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN := C_LAST_UPDATE_LOGIN;
g_row_syutu_kekkin_db.CREATED_BY := C_CREATED_BY;
g_row_syutu_kekkin_db.CREATION_DATE := C_CREATION_DATE;

g_err_pos := '01800';

-- CASE 1
-- 出欠勤DBに明細が存在する場合で、ホストに対しては、Dとする場合
IF l_syori_case = '1' THEN
    -- 出欠勤DBを"D"で更新
    g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_DEL; -- コントロールフラグ: 削除
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := C_KANRI_STATUS_KBN_END; -- 管理区分: 終了
    g_row_syutu_kekkin_db.SEQUENCE_NO := row_kekkin.SYUKKEKKIN_DB_SEQUENCE_NO;

    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN,
        SOSIN_TAISYO_YM = g_row_syutu_kekkin_db.SOSIN_TAISYO_YM,
        TYOKIN_KEKKIN_SEQUENCE_ID = NULL,
        TYOKETU_KAISI_SEQUENCE_ID = NULL,
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE = g_row_syutu_kekkin_db.LAST_UPDATE_DATE,
        LAST_UPDATED_BY = g_row_syutu_kekkin_db.LAST_UPDATED_BY,
        LAST_UPDATE_LOGIN = g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN
    WHERE
        PERSON_ID = g_row_syutu_kekkin_db.PERSON_ID
        AND SEQUENCE_NO = g_row_syutu_kekkin_db.SEQUENCE_NO;

    -- 抽出データの出欠勤DBシーケンスNoをNULLに更新
    -- 抽出データは取消されるため、出欠勤DBテーブルとの関係を消す。
    UPD_KEKKIN_DETAILS(
        p_person_id => row_kekkin.PERSON_ID,
        p_sequence_id => row_kekkin.SEQUENCE_ID,
        p_syukkekkin_sequence_no => NULL
    );
END IF;

g_err_pos := '02000';

-- filepath: /c:/Users/81804/Desktop/hibiki/109.pkb

-- CASE12
-- 産休・育休、新規の場合
ELSIF l_syori_case = '12' THEN
    -- 残りのカラムに値をセット
    -- 1.コントロール情報
    g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;
    -- 2.行員番号
    g_row_syutu_kekkin_db.KOIN_NO := row_kekkin.KOIN_NO;
    -- 4.シーケンスNo
    g_row_syutu_kekkin_db.SEQUENCE_NO := GET_NEW_SEQUENCE_NO(p_person_id => row_kekkin.PERSON_ID);
    -- 6.欠勤事由
    g_row_syutu_kekkin_db.KEKKIN_JIYU := row_kekkin.KEKKIN_JIYU;
    -- 9.病名
    g_row_syutu_kekkin_db.BYOMEI := C_BYOMEI_SANKYU;

    -- 10.産前特別休業開始日
    -- 11.産前特別休業終了日
    -- 産前特別休業取得状況が取得になっている場合のみ産前特別休業開始日セットする。
    -- 産前特別休業取得状況が取得になっている場合のみ産前休務開始日の前日セットする。
    -- 産前休務取得状況が"0":取得しないの場合は、出産日(NULLの場合は出産予定日)をセットする。
    IF row_kekkin.SANZEN_TOKUKYU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI := row_kekkin.SANZEN_TOKUKYU_KAISIBI;
        IF row_kekkin.SANZEN_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
            g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI := row_kekkin.SANZEN_KYUMU_KAISIBI - 1;
        ELSE
            g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI := NVL(row_kekkin.SYUSSANBI, row_kekkin.SYUSSAN_YOTEIBI);
        END IF;
    END IF;

    -- 12.産前休務開始日
    -- 13.産前休務終了日
    -- 産前休務取得状況が取得になっている場合のみ産前休務開始日セットする。
    -- 産前休務取得状況が取得になっている場合のみ出産日(NULLの場合は出産予定日)をセットする。
    IF row_kekkin.SANZEN_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI := row_kekkin.SANZEN_KYUMU_KAISIBI;
        g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI := NVL(row_kekkin.SYUSSANBI, row_kekkin.SYUSSAN_YOTEIBI);
    END IF;

    -- 14.産後休務開始日
    -- 15.産後休務終了日
    -- 産後休務取得状況が取得になっている場合のみ出産日(NULLの場合は出産予定日)の翌日をセットする。
    -- 産後休務取得状況が取得になっている場合のみ産後休終了日(NULLの場合は産後休務終了予定日)をセットする。
    IF row_kekkin.SANGO_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI := NVL(row_kekkin.SYUSSANBI, row_kekkin.SYUSSAN_YOTEIBI) + 1;
        g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI := row_kekkin.SANGO_KYUMU_SYURYOBI;
    END IF;

    -- 16.育児休業開始日
    -- 17.育児休業終了日
    -- 育児休業取得状況が取得になっている場合のみ育児休業開始日(NULLの場合は育児休業開始予定日)をセットする。
    -- 育児休業取得状況が取得になっている場合のみ育児休業終了日(NULLの場合は育児休業終了予定日)をセットする。
    IF row_kekkin.IKUKYU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.IKUKYU_KAISIBI := row_kekkin.IKUKYU_KAISIBI;
        g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := row_kekkin.IKUKYU_SYURYOBI;
    END IF;

    -- 7.欠勤開始日
    -- 産休・育休テーブル、産前休業開始日、
    -- 産休・育休テーブル、産前休務開始日、
    -- 産休・育休テーブル、産後休務開始日、
    -- 産休・育休テーブル、育児休業開始日。
    -- のうち一番最初の日付をセットする。
    g_row_syutu_kekkin_db.KEKKIN_KAISIBI := LEAST(
        NVL(g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI, C_SYSTEM_MAX_DATE),
        NVL(g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI, C_SYSTEM_MAX_DATE),
        NVL(g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI, C_SYSTEM_MAX_DATE),
        NVL(g_row_syutu_kekkin_db.IKUKYU_KAISIBI, C_SYSTEM_MAX_DATE)
    );

    -- 8.欠勤終了日
    -- 産休・育休テーブル、産前休業終了日、
    -- 産休・育休テーブル、産前休務終了日、
    -- 産休・育休テーブル、産後休務終了日、
    -- 産休・育休テーブル、育児休業終了日。
    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := GREATEST(
        NVL(g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI, C_SYSTEM_MIN_DATE),
        NVL(g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI, C_SYSTEM_MIN_DATE),
        NVL(g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI, C_SYSTEM_MIN_DATE),
        NVL(g_row_syutu_kekkin_db.IKUKYU_SYURYOBI, C_SYSTEM_MIN_DATE)
    );

    g_row_syutu_kekkin_db.SYUSSANBI := row_kekkin.SYUSSANBI;
    g_row_syutu_kekkin_db.SYUSSAN_YOTEIBI := row_kekkin.SYUSSAN_YOTEIBI;
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := GET_KANRI_STATUS_KBN(
        p_status => row_kekkin.STATUS,
        p_syori_gessyo => TO_DATE(l_syori_ym || '01', 'YYYYMMDD'),
        p_kekkin_syuryobi => g_row_syutu_kekkin_db.KEKKIN_SYURYOBI
    );

    -- 当初開始日
    -- 欠勤開始日と同じ値をセットする。
    g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI := g_row_syutu_kekkin_db.KEKKIN_KAISIBI;

    -- データを挿入
    INS_SYUTU_KEKKIN_DB;

    g_err_pos := '03700';

    -- 抽出データの出欠勤DBシーケンスNoを上で挿入したシーケンスNoに更新
    -- 上で挿入したデータと抽出データとの関係を設定する。
    UPD_SANKYU_IKUKYU_DETAILS(
        p_person_id => row_kekkin.PERSON_ID,
        p_sequence_id => row_kekkin.SEQUENCE_ID,
        p_syukkekkin_sequence_no => g_row_syutu_kekkin_db.SEQUENCE_NO
    );
    g_err_pos := '03800';

-- CASE13
-- 産休・育休、削除の場合
ELSIF l_syori_case = '13' THEN
    -- 残りのカラムに値をセット
    -- 1.コントロール情報
    g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_DEL;
    -- 4.シーケンスNo
    g_row_syutu_kekkin_db.SEQUENCE_NO := row_kekkin.SYUKKEKKIN_DB_SEQUENCE_NO;

    -- データを更新する。
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        KANRI_STATUS_KBN = C_KANRI_STATUS_KBN_END,
        SOSIN_TAISYO_YM = g_row_syutu_kekkin_db.SOSIN_TAISYO_YM,
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE = g_row_syutu_kekkin_db.LAST_UPDATE_DATE,
        LAST_UPDATED_BY = g_row_syutu_kekkin_db.LAST_UPDATED_BY,
        LAST_UPDATE_LOGIN = g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN
    WHERE
        PERSON_ID = g_row_syutu_kekkin_db.PERSON_ID
        AND SEQUENCE_NO = g_row_syutu_kekkin_db.SEQUENCE_NO;

    g_err_pos := '03900';

    -- 2007/08/27以下で産休育休明細の出欠勤DBシーケンスNoをNULLに設定すると、
    -- 有給育休反映処理部において、産休育休明細を軸に処理を行うので
    -- 有給レコードに対する処理が行われないため。
    -- 全取消の場合は、有給育休反映処理部においてNULLに設定する。
    -- 抽出データの出欠勤DBシーケンスNoをNULLに更新
    -- 抽出データは取消されるため、出欠勤DBテーブルとの関係を消す。
    UPD_SANKYU_IKUKYU_DETAILS(
        p_person_id => row_kekkin.PERSON_ID,
        p_sequence_id => row_kekkin.SEQUENCE_ID,
        p_syukkekkin_sequence_no => NULL
    );

    g_err_pos := '04000';·

-- CASE14
-- 産休・育休、更新の場合
ELSIF l_syori_case = '14' THEN
    -- 残りのカラムに値をセット
    -- 1.コントロール情報
    g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_UPD;
    -- 4.シーケンスNo
    g_row_syutu_kekkin_db.SEQUENCE_NO := row_kekkin.SYUKKEKKIN_DB_SEQUENCE_NO;

    -- 10.産前特別休業開始日
    -- 産前特別休業取得状況が取得になっている場合のみ産前特別休業開始日セットする。
    IF row_kekkin.SANZEN_TOKUKYU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI := row_kekkin.SANZEN_TOKUKYU_KAISIBI;
        IF row_kekkin.SANZEN_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
            g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI := row_kekkin.SANZEN_KYUMU_KAISIBI - 1;
        ELSE
            g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI := NVL(row_kekkin.SYUSSANBI, row_kekkin.SYUSSAN_YOTEIBI);
        END IF;
    END IF;

    -- 12.産前休務開始日
    -- 13.産前休務終了日
    -- 産前休務取得状況が取得になっている場合のみ産前休務開始日セットする。
    IF row_kekkin.SANZEN_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI := row_kekkin.SANZEN_KYUMU_KAISIBI;
        g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI := NVL(row_kekkin.SYUSSANBI, row_kekkin.SYUSSAN_YOTEIBI);
    END IF;

    -- 14.産後休務開始日
    -- 15.産後休務終了日
    -- 産後休務取得状況が取得になっている場合のみ出産日(NULLの場合は出産予定日)の翌日をセットする。
    IF row_kekkin.SANGO_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI := NVL(row_kekkin.SYUSSANBI, row_kekkin.SYUSSAN_YOTEIBI) + 1;
        g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI := row_kekkin.SANGO_KYUMU_SYURYOBI;
    END IF;

    -- 16.育児休業開始日
    -- 17.育児休業終了日
    -- 育児休業取得状況が取得になっている場合のみ育児休業開始日(NULLの場合は育児休業開始予定日)をセットする。
    IF row_kekkin.IKUKYU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
        g_row_syutu_kekkin_db.IKUKYU_KAISIBI := row_kekkin.IKUKYU_KAISIBI;
        g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := row_kekkin.IKUKYU_SYURYOBI;
    END IF;

    -- 7.欠勤開始日
    -- 産休・育休テーブル、産前休業開始日、
    -- 産休・育休テーブル、産前休務開始日、
    -- 産休・育休テーブル、産後休務開始日、
    -- 産休・育休テーブル、育児休業開始日。
    -- のうち一番最初の日付をセットする。
    g_row_syutu_kekkin_db.KEKKIN_KAISIBI := LEAST(
        NVL(g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI, C_SYSTEM_MAX_DATE),
        NVL(g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI, C_SYSTEM_MAX_DATE),
        NVL(g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI, C_SYSTEM_MAX_DATE),
        NVL(g_row_syutu_kekkin_db.IKUKYU_KAISIBI, C_SYSTEM_MAX_DATE)
    );

    -- 8.欠勤終了日
    -- 産休・育休テーブル、産前休業終了日、
    -- 産休・育休テーブル、産前休務終了日、
    -- 産休・育休テーブル、産後休務終了日、
    -- 産休・育休テーブル、育児休業終了日。
    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := GREATEST(
        NVL(g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI, C_SYSTEM_MIN_DATE),
        NVL(g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI, C_SYSTEM_MIN_DATE),
        NVL(g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI, C_SYSTEM_MIN_DATE),
        NVL(g_row_syutu_kekkin_db.IKUKYU_SYURYOBI, C_SYSTEM_MIN_DATE)
    );

    g_row_syutu_kekkin_db.SYUSSANBI := row_kekkin.SYUSSANBI;
    g_row_syutu_kekkin_db.SYUSSAN_YOTEIBI := row_kekkin.SYUSSAN_YOTEIBI;
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := GET_KANRI_STATUS_KBN(
        p_status => row_kekkin.STATUS,
        p_syori_gessyo => TO_DATE(l_syori_ym || '01', 'YYYYMMDD'),
        p_kekkin_syuryobi => g_row_syutu_kekkin_db.KEKKIN_SYURYOBI
    );

    -- 当初開始日
    -- 欠勤開始日と同じ値をセットする。
    g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI := g_row_syutu_kekkin_db.KEKKIN_KAISIBI;

    -- データを更新する。
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN,
        KEKKIN_KAISIBI = g_row_syutu_kekkin_db.KEKKIN_KAISIBI,
        KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
        SANZEN_TOKUKYU_KAISIBI = g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI,
        SANZEN_TOKUKYU_SYURYOBI = g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI,
        SANZEN_KYUMU_KAISIBI = g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI,
        SANZEN_KYUMU_SYURYOBI = g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI,
        SANGO_KYUMU_KAISIBI = g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI,
        SANGO_KYUMU_SYURYOBI = g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI,
        IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
        IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
        SYUSSANBI = g_row_syutu_kekkin_db.SYUSSANBI,
        SYUSSAN_YOTEIBI = g_row_syutu_kekkin_db.SYUSSAN_YOTEIBI,
        SOSIN_TAISYO_YM = g_row_syutu_kekkin_db.SOSIN_TAISYO_YM,
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        LAST_UPDATE_DATE = g_row_syutu_kekkin_db.LAST_UPDATE_DATE,
        LAST_UPDATED_BY = g_row_syutu_kekkin_db.LAST_UPDATED_BY,
        LAST_UPDATE_LOGIN = g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN,
        TOSYO_KEKKIN_KAISIBI = g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI
    WHERE
        PERSON_ID = g_row_syutu_kekkin_db.PERSON_ID
        AND SEQUENCE_NO = g_row_syutu_kekkin_db.SEQUENCE_NO;

    g_err_pos := '04100';
END IF;