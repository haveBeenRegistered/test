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


-- filepath: /c:/Users/81804/Desktop/hibiki/21

20:31:23
YUKYU IKUKYU

PROCPROCEDURE

育児休業の一部期間を有給化するために、育児休業の終了を前倒し、 その分を別の有給育休欠勤レコードとして作成するための処理。
当処理は出欠勤DBレコードへのデータ作成後起動され、育休を 含むレコードについて部分有給化の処理を行う。

2022年10月以降、有給期間は出産日ごとに最大10営業日とする。

PROCEDURE YUKYU_IKUKYU_PROC(
    p_syori_ym VARCHAR2,
    p_prev_syori_ymd DATE,
    p_syori_ymd DATE
) IS
    l_yukyu_ikukyu_new_seq VARCHAR2(50);
    l_yukyu_ikukyu_start_date DATE;
    l_yukyuikukyu_syutoku_ka_nissu NUMBER;
    l_yukyu_kikan_from DATE;
    EXCEPTION EXC_MESSAGE_ERROR;

    CURSOR cur_yukyu_ikukyu(
        p_yukyu_ikukyu_start_date DATE,
        p_syori_ym VARCHAR2,
        p_prev_syori_ymd DATE,
        p_syori_ymd DATE
    ) IS
        SELECT
            db.KOIN_NO,
            db.PERSON_ID,
            db.SEQUENCE_NO,
            db.SANGO_KYUMU_SYURYOBI,
            db.IKUKYU_KAISIBI,
            db.IKUKYU_SYURYOBI,
            db.KANRI_STATUS_KBN,
            db.YUKYU_IKUKYU_SEQUENCE_NO,
            isid.IKUKYU_KAISIBI,
            isid.SANGO_KYUMU_STATUS,
            isid.IKUKYU_STATUS,
            isid.YUKYU_IKUKYU_SEQUENCE_NO,
            db_yukyu.SEQUENCE_NO AS db_yukyu_sequence_no,
            isid.SYUKKEKKIN_SANKYU_SOSIN_FLG,
            db.KEKKIN_KAISIBI
        FROM
            IHR_SYUTU_KEKKIN_DB db
            JOIN IHR_SANKYU_IKUKYU_DETAILS isid ON db.PERSON_ID = isid.PERSON_ID
            LEFT JOIN IHR_SYUTU_KEKKIN_DB db_yukyu ON isid.YUKYU_IKUKYU_SEQUENCE_NO = db_yukyu.SEQUENCE_NO
        WHERE
            db.SOSIN_TAISYO_YM = p_syori_ym
            AND db.KEKKIN_JIYU = 'SANKYU'
            AND isid.SYUKKEKKIN_SEQUENCE_NO = db.SEQUENCE_NO
            AND isid.PERSON_ID = db.PERSON_ID
            AND isid.YUKYU_IKUKYU_SEQUENCE_NO = db_yukyu.SEQUENCE_NO(+)
            AND db.PERSON_ID = isid.PERSON_ID
            AND db.SEQUENCE_NO = isid.SYUKKEKKIN_SEQUENCE_NO
            AND paaf.PERSON_ID = db.PERSON_ID
            AND p_syori_ymd BETWEEN paaf.EFFECTIVE_START_DATE AND paaf.EFFECTIVE_END_DATE
            AND paaf.ASSIGNMENT_TYPE = 'E'
            AND paaf.PRIMARY_FLAG = 'Y'
            AND paaf.GRADE_ID <> IHR_KEIYAKU_COMMON.GET_GRADE_ID_KEIYAKU
            AND NOT (
                isid.SANKYU_SAISYU_SYURYOBI < TO_DATE(p_syori_ym || '01', 'YYYYMMDD')
                AND isid.SYONINBI NOT BETWEEN p_prev_syori_ymd AND p_syori_ymd
            )
        ORDER BY
            db.KOIN_NO, isid.IKUKYU_KAISIBI;

    CURSOR cur_end_yukyu_kanri_status(
        p_prev_syori_ymd DATE,
        p_syori_ymd DATE,
        p_syori_ym VARCHAR2
    ) IS
        SELECT
            db1.PERSON_ID,
            db1.SEQUENCE_NO
        FROM
            IHR_SYUTU_KEKKIN_DB db1
            JOIN IHR_SANKYU_IKUKYU_DETAILS san ON db1.PERSON_ID = san.PERSON_ID
        WHERE
            san.PERSON_ID = db1.PERSON_ID
            AND db1.SEQUENCE_NO = san.YUKYU_IKUKYU_SEQUENCE_NO
            AND san.STATUS = 'SYURYOBI_ZUMI'
            AND san.SANKYU_SAISYU_SYURYOBI < TO_DATE(p_syori_ym || '01', 'YYYYMMDD')
            AND san.SYONINBI NOT BETWEEN p_prev_syori_ymd AND p_syori_ymd
            AND db1.KANRI_STATUS_KBN = 'TYU'
            AND db1.KEKKIN_JIYU = 'YUKYU_IKUKYU'
            AND san.TORIKESI_FLG = 0;

BEGIN
    g_err_pos := '06000';

    -- 制度開始年月日を固定値からルックアップに変更。延期された為。
    l_yukyu_ikukyu_new_seq := NULL;
    l_yukyu_ikukyu_start_date := NULL;

    g_err_pos := '06050';

    SELECT TO_DATE(MEANING, 'YYYYMMDD')
    INTO l_yukyu_ikukyu_start_date
    FROM FND_LOOKUP_VALUES
    WHERE LOOKUP_TYPE = 'IHR_YUKYU_IKUKYU_START_DATE'
    AND LANGUAGE = 'JA'
    AND LOOKUP_CODE = '1';

    -- 出欠勤DBテーブルより取得した産休育休レコードについて、以下の3つの処理を行う
    FOR row_yukyu_ikukyu IN cur_yukyu_ikukyu(
        p_yukyu_ikukyu_start_date,
        p_syori_ym,
        p_prev_syori_ymd,
        p_syori_ymd
    ) LOOP
        -- データ登録用変数初期化
        g_row_syutu_kekkin_db := NULL;
        l_yukyu_kikan_from := NULL;
        l_yukyuikukyu_syutoku_ka_nissu := 0;

        -- 新規に有給育休レコードを作成する際に使用するシーケンスNoを設定しておく
        l_yukyu_ikukyu_new_seq := GET_NEW_SEQUENCE_NO(p_person_id => row_yukyu_ikukyu.PERSON_ID);

        g_err_pos := '06100';

        -- 産休育休レコードに紐付く有給育休欠勤レコードの作成(既に存在する場合は削除または更新)
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_STATUS = 'SYUTOKU' THEN
            g_err_pos := '06200';

            -- 有給育休が何日取得できるか
            l_yukyuikukyu_syutoku_ka_nissu := YUKYUIKUKYU_SYUTOKUKANOU_NISSU(
                p_person_id => row_yukyu_ikukyu.PERSON_ID,
                p_syussanbi => row_yukyu_ikukyu.DETAIL_SYUSSANBI,
                p_syussan_yoteibi => row_yukyu_ikukyu.DETAIL_SYUSSAN_YOTEIBI,
                p_ikukyu_kaisibi => row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI
            );

            -- 有給育休の開始日
            IF l_yukyuikukyu_syutoku_ka_nissu > 0 THEN
                l_yukyu_kikan_from := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI, l_yukyuikukyu_syutoku_ka_nissu);

                -- 当該産休育休レコードに紐づく有給育休レコードは存在するか? (Case2)
                IF row_yukyu_ikukyu.DETAIL_YUKYU_IKUKYU_SEQ_NO IS NULL THEN
                    g_err_pos := '06300';

                    IF l_yukyuikukyu_syutoku_ka_nissu > 0 THEN
                        -- 有給育休レコードを新規作成
                        g_row_syutu_kekkin_db.CTRL_INFO := 'NEW';
                        g_row_syutu_kekkin_db.KOIN_NO := row_yukyu_ikukyu.KOIN_NO;
                        g_row_syutu_kekkin_db.PERSON_ID := row_yukyu_ikukyu.PERSON_ID;
                        g_row_syutu_kekkin_db.SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
                        g_row_syutu_kekkin_db.KANRI_STATUS_KBN := row_yukyu_ikukyu.DB_KANRI_STATUS_KBN;
                        g_row_syutu_kekkin_db.KEKKIN_JIYU := 'YUKYU_IKUKYU';
                        g_row_syutu_kekkin_db.KEKKIN_KAISIBI := l_yukyu_kikan_from;
                        g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI := row_yukyu_ikukyu.DB_KEKKIN_KAISIBI;

                        g_err_pos := '06400';

                        g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI;
                        g_row_syutu_kekkin_db.BYOMEI := 'YUKYU_IKUKYU';
                        g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.SYUSSAN_YOTEIBI := NULL;
                        g_row_syutu_kekkin_db.SYUSSANBI := NULL;
                        g_row_syutu_kekkin_db.SOSIN_TAISYO_YM := p_syori_ym;
                        g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := 'ON';
                        g_row_syutu_kekkin_db.TYOKETU_KAISI_SEQUENCE_ID := NULL;
                        g_row_syutu_kekkin_db.TYOKIN_KEKKIN_SEQUENCE_ID := NULL;
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := NULL;
                        g_row_syutu_kekkin_db.LAST_UPDATE_DATE := 'LAST_UPDATE_DATE';
                        g_row_syutu_kekkin_db.LAST_UPDATED_BY := 'LAST_UPDATED_BY';
                        g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN := 'LAST_UPDATE_LOGIN';
                        g_row_syutu_kekkin_db.CREATED_BY := 'CREATED_BY';
                        g_row_syutu_kekkin_db.CREATION_DATE := 'CREATION_DATE';

                        -- データを挿入
                        INS_SYUTU_KEKKIN_DB;
                    END IF;
                ELSE
                    g_err_pos := '06500';

                    g_row_syutu_kekkin_db.CTRL_INFO := 'UPD';
                    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := row_yukyu_ikukyu.DB_KANRI_STATUS_KBN;
                    g_row_syutu_kekkin_db.KEKKIN_KAISIBI := l_yukyu_kikan_from;
                    g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI := row_yukyu_ikukyu.DB_KEKKIN_KAISIBI;
                    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI;
                    g_row_syutu_kekkin_db.SOSIN_TAISYO_YM := p_syori_ym;
                    g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := 'ON';
                    g_row_syutu_kekkin_db.LAST_UPDATE_DATE := 'LAST_UPDATE_DATE';
                    g_row_syutu_kekkin_db.LAST_UPDATED_BY := 'LAST_UPDATED_BY';
                    g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN := 'LAST_UPDATE_LOGIN';

                    IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                        UPDATE IHR_SYUTU_KEKKIN_DB
                        SET
                            CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                            KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN,
                            KEKKIN_KAISIBI = g_row_syutu_kekkin_db.KEKKIN_KAISIBI,
                            KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                            TOSYO_KEKKIN_KAISIBI = g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI,
                            SOSIN_TAISYO_YM = g_row_syutu_kekkin_db.SOSIN_TAISYO_YM,
                            SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
                            LAST_UPDATE_DATE = g_row_syutu_kekkin_db.LAST_UPDATE_DATE,
                            LAST_UPDATED_BY = g_row_syutu_kekkin_db.LAST_UPDATED_BY,
                            LAST_UPDATE_LOGIN = g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN
                        WHERE
                            PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                            AND SEQUENCE_NO = row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
                    END IF;
                END IF;
            END IF;
        END IF;

        g_err_pos := '06600';

        -- 産休育休レコードの更新(取得可能営業日数分短くする処理)
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_STATUS = 'SYUTOKU' AND row_yukyu_ikukyu.DETAIL_SANGO_KYUMU_STATUS = 'SYUTOKU' THEN
            g_err_pos := '07000';

            IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
                    g_err_pos := '07100';

                    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
                    l_yukyu_kikan_from := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;

                    IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
                    ELSE
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
                    END IF;

                    UPDATE IHR_SYUTU_KEKKIN_DB
                    SET
                        KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                        IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                        YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                    WHERE
                        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
                ELSE
                    g_err_pos := '07200';

                    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_SANGO_KYUMU_SYURYOBI;
                    g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
                    g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;

                    IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
                    ELSE
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
                    END IF;

                    UPDATE IHR_SYUTU_KEKKIN_DB
                    SET
                        KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                        IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
                        IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                        YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                    WHERE
                        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
                END IF;
            END IF;
        END IF;

        g_err_pos := '07300';

        -- 以前にホスト送信されたレコードか?
        IF row_yukyu_ikukyu.DETAIL_SANKYU_SOSIN_FLG-- filepath: /c:/Users/81804/Desktop/hibiki/21

20:31:23
YUKYU IKUKYU

PROCPROCEDURE

育児休業の一部期間を有給化するために、育児休業の終了を前倒し、 その分を別の有給育休欠勤レコードとして作成するための処理。
当処理は出欠勤DBレコードへのデータ作成後起動され、育休を 含むレコードについて部分有給化の処理を行う。

2022年10月以降、有給期間は出産日ごとに最大10営業日とする。

PROCEDURE YUKYU_IKUKYU_PROC(
    p_syori_ym VARCHAR2,
    p_prev_syori_ymd DATE,
    p_syori_ymd DATE
) IS
    l_yukyu_ikukyu_new_seq VARCHAR2(50);
    l_yukyu_ikukyu_start_date DATE;
    l_yukyuikukyu_syutoku_ka_nissu NUMBER;
    l_yukyu_kikan_from DATE;
    EXCEPTION EXC_MESSAGE_ERROR;

    CURSOR cur_yukyu_ikukyu(
        p_yukyu_ikukyu_start_date DATE,
        p_syori_ym VARCHAR2,
        p_prev_syori_ymd DATE,
        p_syori_ymd DATE
    ) IS
        SELECT
            db.KOIN_NO,
            db.PERSON_ID,
            db.SEQUENCE_NO,
            db.SANGO_KYUMU_SYURYOBI,
            db.IKUKYU_KAISIBI,
            db.IKUKYU_SYURYOBI,
            db.KANRI_STATUS_KBN,
            db.YUKYU_IKUKYU_SEQUENCE_NO,
            isid.IKUKYU_KAISIBI,
            isid.SANGO_KYUMU_STATUS,
            isid.IKUKYU_STATUS,
            isid.YUKYU_IKUKYU_SEQUENCE_NO,
            db_yukyu.SEQUENCE_NO AS db_yukyu_sequence_no,
            isid.SYUKKEKKIN_SANKYU_SOSIN_FLG,
            db.KEKKIN_KAISIBI
        FROM
            IHR_SYUTU_KEKKIN_DB db
            JOIN IHR_SANKYU_IKUKYU_DETAILS isid ON db.PERSON_ID = isid.PERSON_ID
            LEFT JOIN IHR_SYUTU_KEKKIN_DB db_yukyu ON isid.YUKYU_IKUKYU_SEQUENCE_NO = db_yukyu.SEQUENCE_NO
        WHERE
            db.SOSIN_TAISYO_YM = p_syori_ym
            AND db.KEKKIN_JIYU = 'SANKYU'
            AND isid.SYUKKEKKIN_SEQUENCE_NO = db.SEQUENCE_NO
            AND isid.PERSON_ID = db.PERSON_ID
            AND isid.YUKYU_IKUKYU_SEQUENCE_NO = db_yukyu.SEQUENCE_NO(+)
            AND db.PERSON_ID = isid.PERSON_ID
            AND db.SEQUENCE_NO = isid.SYUKKEKKIN_SEQUENCE_NO
            AND paaf.PERSON_ID = db.PERSON_ID
            AND p_syori_ymd BETWEEN paaf.EFFECTIVE_START_DATE AND paaf.EFFECTIVE_END_DATE
            AND paaf.ASSIGNMENT_TYPE = 'E'
            AND paaf.PRIMARY_FLAG = 'Y'
            AND paaf.GRADE_ID <> IHR_KEIYAKU_COMMON.GET_GRADE_ID_KEIYAKU
            AND NOT (
                isid.SANKYU_SAISYU_SYURYOBI < TO_DATE(p_syori_ym || '01', 'YYYYMMDD')
                AND isid.SYONINBI NOT BETWEEN p_prev_syori_ymd AND p_syori_ymd
            )
        ORDER BY
            db.KOIN_NO, isid.IKUKYU_KAISIBI;

    CURSOR cur_end_yukyu_kanri_status(
        p_prev_syori_ymd DATE,
        p_syori_ymd DATE,
        p_syori_ym VARCHAR2
    ) IS
        SELECT
            db1.PERSON_ID,
            db1.SEQUENCE_NO
        FROM
            IHR_SYUTU_KEKKIN_DB db1
            JOIN IHR_SANKYU_IKUKYU_DETAILS san ON db1.PERSON_ID = san.PERSON_ID
        WHERE
            san.PERSON_ID = db1.PERSON_ID
            AND db1.SEQUENCE_NO = san.YUKYU_IKUKYU_SEQUENCE_NO
            AND san.STATUS = 'SYURYOBI_ZUMI'
            AND san.SANKYU_SAISYU_SYURYOBI < TO_DATE(p_syori_ym || '01', 'YYYYMMDD')
            AND san.SYONINBI NOT BETWEEN p_prev_syori_ymd AND p_syori_ymd
            AND db1.KANRI_STATUS_KBN = 'TYU'
            AND db1.KEKKIN_JIYU = 'YUKYU_IKUKYU'
            AND san.TORIKESI_FLG = 0;

BEGIN
    g_err_pos := '06000';

    -- 制度開始年月日を固定値からルックアップに変更。延期された為。
    l_yukyu_ikukyu_new_seq := NULL;
    l_yukyu_ikukyu_start_date := NULL;

    g_err_pos := '06050';

    SELECT TO_DATE(MEANING, 'YYYYMMDD')
    INTO l_yukyu_ikukyu_start_date
    FROM FND_LOOKUP_VALUES
    WHERE LOOKUP_TYPE = 'IHR_YUKYU_IKUKYU_START_DATE'
    AND LANGUAGE = 'JA'
    AND LOOKUP_CODE = '1';

    -- 出欠勤DBテーブルより取得した産休育休レコードについて、以下の3つの処理を行う
    FOR row_yukyu_ikukyu IN cur_yukyu_ikukyu(
        p_yukyu_ikukyu_start_date,
        p_syori_ym,
        p_prev_syori_ymd,
        p_syori_ymd
    ) LOOP
        -- データ登録用変数初期化
        g_row_syutu_kekkin_db := NULL;
        l_yukyu_kikan_from := NULL;
        l_yukyuikukyu_syutoku_ka_nissu := 0;

        -- 新規に有給育休レコードを作成する際に使用するシーケンスNoを設定しておく
        l_yukyu_ikukyu_new_seq := GET_NEW_SEQUENCE_NO(p_person_id => row_yukyu_ikukyu.PERSON_ID);

        g_err_pos := '06100';

        -- 産休育休レコードに紐付く有給育休欠勤レコードの作成(既に存在する場合は削除または更新)
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_STATUS = 'SYUTOKU' THEN
            g_err_pos := '06200';

            -- 有給育休が何日取得できるか
            l_yukyuikukyu_syutoku_ka_nissu := YUKYUIKUKYU_SYUTOKUKANOU_NISSU(
                p_person_id => row_yukyu_ikukyu.PERSON_ID,
                p_syussanbi => row_yukyu_ikukyu.DETAIL_SYUSSANBI,
                p_syussan_yoteibi => row_yukyu_ikukyu.DETAIL_SYUSSAN_YOTEIBI,
                p_ikukyu_kaisibi => row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI
            );

            -- 有給育休の開始日
            IF l_yukyuikukyu_syutoku_ka_nissu > 0 THEN
                l_yukyu_kikan_from := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI, l_yukyuikukyu_syutoku_ka_nissu);

                -- 当該産休育休レコードに紐づく有給育休レコードは存在するか? (Case2)
                IF row_yukyu_ikukyu.DETAIL_YUKYU_IKUKYU_SEQ_NO IS NULL THEN
                    g_err_pos := '06300';

                    IF l_yukyuikukyu_syutoku_ka_nissu > 0 THEN
                        -- 有給育休レコードを新規作成
                        g_row_syutu_kekkin_db.CTRL_INFO := 'NEW';
                        g_row_syutu_kekkin_db.KOIN_NO := row_yukyu_ikukyu.KOIN_NO;
                        g_row_syutu_kekkin_db.PERSON_ID := row_yukyu_ikukyu.PERSON_ID;
                        g_row_syutu_kekkin_db.SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
                        g_row_syutu_kekkin_db.KANRI_STATUS_KBN := row_yukyu_ikukyu.DB_KANRI_STATUS_KBN;
                        g_row_syutu_kekkin_db.KEKKIN_JIYU := 'YUKYU_IKUKYU';
                        g_row_syutu_kekkin_db.KEKKIN_KAISIBI := l_yukyu_kikan_from;
                        g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI := row_yukyu_ikukyu.DB_KEKKIN_KAISIBI;

                        g_err_pos := '06400';

                        g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI;
                        g_row_syutu_kekkin_db.BYOMEI := 'YUKYU_IKUKYU';
                        g_row_syutu_kekkin_db.SANZEN_TOKUKYU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.SANZEN_TOKUKYU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.SANZEN_KYUMU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.SANZEN_KYUMU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.SANGO_KYUMU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.SANGO_KYUMU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
                        g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;
                        g_row_syutu_kekkin_db.SYUSSAN_YOTEIBI := NULL;
                        g_row_syutu_kekkin_db.SYUSSANBI := NULL;
                        g_row_syutu_kekkin_db.SOSIN_TAISYO_YM := p_syori_ym;
                        g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := 'ON';
                        g_row_syutu_kekkin_db.TYOKETU_KAISI_SEQUENCE_ID := NULL;
                        g_row_syutu_kekkin_db.TYOKIN_KEKKIN_SEQUENCE_ID := NULL;
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := NULL;
                        g_row_syutu_kekkin_db.LAST_UPDATE_DATE := 'LAST_UPDATE_DATE';
                        g_row_syutu_kekkin_db.LAST_UPDATED_BY := 'LAST_UPDATED_BY';
                        g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN := 'LAST_UPDATE_LOGIN';
                        g_row_syutu_kekkin_db.CREATED_BY := 'CREATED_BY';
                        g_row_syutu_kekkin_db.CREATION_DATE := 'CREATION_DATE';

                        -- データを挿入
                        INS_SYUTU_KEKKIN_DB;
                    END IF;
                ELSE
                    g_err_pos := '06500';

                    g_row_syutu_kekkin_db.CTRL_INFO := 'UPD';
                    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := row_yukyu_ikukyu.DB_KANRI_STATUS_KBN;
                    g_row_syutu_kekkin_db.KEKKIN_KAISIBI := l_yukyu_kikan_from;
                    g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI := row_yukyu_ikukyu.DB_KEKKIN_KAISIBI;
                    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI;
                    g_row_syutu_kekkin_db.SOSIN_TAISYO_YM := p_syori_ym;
                    g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := 'ON';
                    g_row_syutu_kekkin_db.LAST_UPDATE_DATE := 'LAST_UPDATE_DATE';
                    g_row_syutu_kekkin_db.LAST_UPDATED_BY := 'LAST_UPDATED_BY';
                    g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN := 'LAST_UPDATE_LOGIN';

                    IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                        UPDATE IHR_SYUTU_KEKKIN_DB
                        SET
                            CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                            KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN,
                            KEKKIN_KAISIBI = g_row_syutu_kekkin_db.KEKKIN_KAISIBI,
                            KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                            TOSYO_KEKKIN_KAISIBI = g_row_syutu_kekkin_db.TOSYO_KEKKIN_KAISIBI,
                            SOSIN_TAISYO_YM = g_row_syutu_kekkin_db.SOSIN_TAISYO_YM,
                            SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
                            LAST_UPDATE_DATE = g_row_syutu_kekkin_db.LAST_UPDATE_DATE,
                            LAST_UPDATED_BY = g_row_syutu_kekkin_db.LAST_UPDATED_BY,
                            LAST_UPDATE_LOGIN = g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN
                        WHERE
                            PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                            AND SEQUENCE_NO = row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
                    END IF;
                END IF;
            END IF;
        END IF;

        g_err_pos := '06600';

        -- 産休育休レコードの更新(取得可能営業日数分短くする処理)
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_STATUS = 'SYUTOKU' AND row_yukyu_ikukyu.DETAIL_SANGO_KYUMU_STATUS = 'SYUTOKU' THEN
            g_err_pos := '07000';

            IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
                    g_err_pos := '07100';

                    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
                    l_yukyu_kikan_from := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;

                    IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
                    ELSE
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
                    END IF;

                    UPDATE IHR_SYUTU_KEKKIN_DB
                    SET
                        KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                        IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                        YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                    WHERE
                        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
                ELSE
                    g_err_pos := '07200';

                    g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_SANGO_KYUMU_SYURYOBI;
                    g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
                    g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;

                    IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
                    ELSE
                        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
                    END IF;

                    UPDATE IHR_SYUTU_KEKKIN_DB
                    SET
                        KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                        IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
                        IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                        YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                    WHERE
                        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
                END IF;
            END IF;
        END IF;

        g_err_pos := '07300';

        -- 以前にホスト送信されたレコードか?
        IF row_yukyu_ikukyu.DETAIL_SANKYU_SOSIN_FLG


        -- filepath: /c:/Users/81804/Desktop/hibiki/21

-- 未送信
g_err_pos := '07400';

IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 2022/04 ADD
    -- 育休あり、産休育休レコード未送信、有給育休有り
    -- 育休日数は取得可能営業日数より多いか
    -- (育休開始日 < 有給育休開始日ならば育休日数が取得可能日数より多いと判断) (Case7)
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
            -- 育休あり、産休育休レコード未送信、有給育休有り、育休期間の産休育休IF有り
            -- 過去処理分の場合、全期間有給育休→さかのぼって申請されて無給期間が発生のパターン
            g_err_pos := '07500';
            g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;
            -- 育休日数が取得可能営業日数より多い場合
            g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := l_yukyu_kikan_from - 1;
            g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
            IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
            ELSE
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
            END IF;

            IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                -- 処理年月分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            ELSE
                -- 過去処理分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO,
                    KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
                        p_status => row_yukyu_ikukyu.DETAIL_STATUS,
                        p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
                        p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
                    )
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            END IF;
        END IF;
    END IF;
END IF;

-- 育休日数が取得可能営業日数以下の場合 (Case6)
g_err_pos := '07550';
g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_SANGO_KYUMU_SYURYOBI;
g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;
g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;

UPDATE IHR_SYUTU_KEKKIN_DB
SET
    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
    IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
WHERE
    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;

-- (Case8)
-- 育休のみ、産休育休レコード未送信、有給育休有り、産休育休IF無し
-- 過去処理分の場合、全期間有給育休→全期間有給育休のままのパターン(更新不要)
g_err_pos := '07800';

-- 育休日数が取得可能営業日数以下の場合
IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := C_FLG_OFF;
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := C_KANRI_STATUS_KBN_END;

    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード未送信、有給育休取得不可
g_err_pos := '07650';
g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;

IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
ELSE
    -- 過去処理分
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL,
        KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
            p_status => row_yukyu_ikukyu.DETAIL_STATUS,
            p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
            p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
        )
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード送信済み
g_err_pos := '07700';

IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 2022/04 ADD
    -- 育休あり、産休育休レコード送信済み、有給育休あり
    -- 育休日数は取得可能営業日数より多いか
    -- (育休開始日 < 有給育休開始日ならば育休日数が取得可能営業日数より多いと判断)
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
            -- 育休あり、産休育休レコード送信済み、有給育休あり、育休期間の産休育休IF有り
            -- 育休日数が取得可能営業日数より多い場合
            g_err_pos := '07800';
            g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_UPD;
            g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := l_yukyu_kikan_from - 1;
            g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
            IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
            ELSE
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
            END IF;

            IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                -- 処理年月分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            ELSE
                -- 過去処理分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO,
                    KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
                        p_status => row_yukyu_ikukyu.DETAIL_STATUS,
                        p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
                        p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
                    )
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            END IF;
        END IF;
    END IF;
END IF;

-- 育休日数が取得可能営業日数以下の場合 (Case6)
g_err_pos := '07550';
g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_SANGO_KYUMU_SYURYOBI;
g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;
g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;

UPDATE IHR_SYUTU_KEKKIN_DB
SET
    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
    IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
WHERE
    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;

-- (Case8)
-- 育休のみ、産休育休レコード未送信、有給育休有り、産休育休IF無し
-- 過去処理分の場合、全期間有給育休→全期間有給育休のままのパターン(更新不要)
g_err_pos := '07800';

-- 育休日数が取得可能営業日数以下の場合
IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := C_FLG_OFF;
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := C_KANRI_STATUS_KBN_END;

    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード未送信、有給育休取得不可
g_err_pos := '07650';
g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;

IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
ELSE
    -- 過去処理分
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL,
        KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
            p_status => row_yukyu_ikukyu.DETAIL_STATUS,
            p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
            p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
        )
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード送信済み
g_err_pos := '07700';

IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 2022/04 ADD
    -- 育休あり、産休育休レコード送信済み、有給育休あり
    -- 育休日数は取得可能営業日数より多いか
    -- (育休開始日 < 有給育休開始日ならば育休日数が取得可能営業日数より多いと判断)
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
            -- 育休あり、産休育休レコード送信済み、有給育休あり、育休期間の産休育休IF有り
            -- 育休日数が取得可能営業日数より多い場合
            g_err_pos := '07800';
            g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_UPD;
            g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := l_yukyu_kikan_from - 1;
            g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
            IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
            ELSE
               -- filepath: /c:/Users/81804/Desktop/hibiki/21

-- 未送信
g_err_pos := '07400';

IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 2022/04 ADD
    -- 育休あり、産休育休レコード未送信、有給育休有り
    -- 育休日数は取得可能営業日数より多いか
    -- (育休開始日 < 有給育休開始日ならば育休日数が取得可能日数より多いと判断) (Case7)
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
            -- 育休あり、産休育休レコード未送信、有給育休有り、育休期間の産休育休IF有り
            -- 過去処理分の場合、全期間有給育休→さかのぼって申請されて無給期間が発生のパターン
            g_err_pos := '07500';
            g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;
            -- 育休日数が取得可能営業日数より多い場合
            g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := l_yukyu_kikan_from - 1;
            g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
            IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
            ELSE
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
            END IF;

            IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                -- 処理年月分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            ELSE
                -- 過去処理分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO,
                    KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
                        p_status => row_yukyu_ikukyu.DETAIL_STATUS,
                        p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
                        p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
                    )
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            END IF;
        END IF;
    END IF;
END IF;

-- 育休日数が取得可能営業日数以下の場合 (Case6)
g_err_pos := '07550';
g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_SANGO_KYUMU_SYURYOBI;
g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;
g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;

UPDATE IHR_SYUTU_KEKKIN_DB
SET
    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
    IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
WHERE
    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;

-- (Case8)
-- 育休のみ、産休育休レコード未送信、有給育休有り、産休育休IF無し
-- 過去処理分の場合、全期間有給育休→全期間有給育休のままのパターン(更新不要)
g_err_pos := '07800';

-- 育休日数が取得可能営業日数以下の場合
IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := C_FLG_OFF;
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := C_KANRI_STATUS_KBN_END;

    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード未送信、有給育休取得不可
g_err_pos := '07650';
g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;

IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
ELSE
    -- 過去処理分
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL,
        KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
            p_status => row_yukyu_ikukyu.DETAIL_STATUS,
            p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
            p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
        )
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード送信済み
g_err_pos := '07700';

IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 2022/04 ADD
    -- 育休あり、産休育休レコード送信済み、有給育休あり
    -- 育休日数は取得可能営業日数より多いか
    -- (育休開始日 < 有給育休開始日ならば育休日数が取得可能営業日数より多いと判断)
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
            -- 育休あり、産休育休レコード送信済み、有給育休あり、育休期間の産休育休IF有り
            -- 育休日数が取得可能営業日数より多い場合
            g_err_pos := '07800';
            g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_UPD;
            g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := l_yukyu_kikan_from - 1;
            g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
            IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
            ELSE
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := row_yukyu_ikukyu.YUKYU_SEQUENCE_NO;
            END IF;

            IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
                -- 処理年月分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            ELSE
                -- 過去処理分
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO,
                    KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
                        p_status => row_yukyu_ikukyu.DETAIL_STATUS,
                        p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
                        p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
                    )
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            END IF;
        END IF;
    END IF;
END IF;

-- 育休日数が取得可能営業日数以下の場合 (Case6)
g_err_pos := '07550';
g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := row_yukyu_ikukyu.DB_SANGO_KYUMU_SYURYOBI;
g_row_syutu_kekkin_db.IKUKYU_KAISIBI := NULL;
g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := NULL;
g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;

UPDATE IHR_SYUTU_KEKKIN_DB
SET
    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
    IKUKYU_KAISIBI = g_row_syutu_kekkin_db.IKUKYU_KAISIBI,
    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
WHERE
    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;

-- (Case8)
-- 育休のみ、産休育休レコード未送信、有給育休有り、産休育休IF無し
-- 過去処理分の場合、全期間有給育休→全期間有給育休のままのパターン(更新不要)
g_err_pos := '07800';

-- 育休日数が取得可能営業日数以下の場合
IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := C_FLG_OFF;
    g_row_syutu_kekkin_db.KANRI_STATUS_KBN := C_KANRI_STATUS_KBN_END;

    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
        KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード未送信、有給育休取得不可
g_err_pos := '07650';
g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_NEW;

IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
ELSE
    -- 過去処理分
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
        YUKYU_IKUKYU_SEQUENCE_NO = NULL,
        KANRI_STATUS_KBN = GET_KANRI_STATUS_KBN(
            p_status => row_yukyu_ikukyu.DETAIL_STATUS,
            p_syori_gessyo => TO_DATE(p_syori_ym || '01', 'YYYYMMDD'),
            p_kekkin_syuryobi => row_yukyu_ikukyu.DETAIL_IKUKYU_SYURYOBI
        )
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
END IF;

-- 育休あり、産休育休レコード送信済み
g_err_pos := '07700';

IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 2022/04 ADD
    -- 育休あり、産休育休レコード送信済み、有給育休あり
    -- 育休日数は取得可能営業日数より多いか
    -- (育休開始日 < 有給育休開始日ならば育休日数が取得可能営業日数より多いと判断)
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < l_yukyu_kikan_from THEN
            -- 育休あり、産休育休レコード送信済み、有給育休あり、育休期間の産休育休IF有り
            -- 育休日数が取得可能営業日数より多い場合
            g_err_pos := '07800';
            g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_UPD;
            g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := l_yukyu_kikan_from - 1;
            g_row_syutu_kekkin_db.IKUKYU_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
            IF row_yukyu_ikukyu.YUKYU_SEQUENCE_NO IS NULL THEN
                g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := l_yukyu_ikukyu_new_seq;
            ELSE
-- filepath: /c:/Users/81804/Desktop/hibiki/21

-- 育休あり、産休育休レコード送信済み
再送信
g_err_pos := '07700';

-- 更新者情報
g_row_syutu_kekkin_db.LAST_UPDATED_BY := C_LAST_UPDATED_BY;
g_row_syutu_kekkin_db.LAST_UPDATE_LOGIN := C_LAST_UPDATE_LOGIN;

-- 産休育休レコード送信済み、有給育休あり
IF l_yukyuikukyu_syutoku_ka_nissu THEN
    -- 育休日数は取得可能営業日数より多いか
    IF row_yukyu_ikukyu.DETAIL_IKUKYU_KAISIBI < GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) THEN
        g_err_pos := '07800';

        -- 管理中の明細か
        g_row_syutu_kekkin_db.CTRL_INFO := C_CTRL_INFO_UPD;
        g_row_syutu_kekkin_db.KEKKIN_SYURYOBI := GET_X_EIGYOBI_MAE_DATE(row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI, 10) - 1;
        g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO := I_yukyu_ikukyu_new_seq;

        IF row_yukyu_ikukyu.DB_SOSIN_TAISYO_YM = p_syori_ym THEN
            -- 処理年月分
            UPDATE IHR_SYUTU_KEKKIN_DB
            SET
                CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO
            WHERE
                PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
        ELSE
            -- 過去処理分
            IF row_yukyu_ikukyu.DB_IKUKYU_SYURYOBI <> g_row_syutu_kekkin_db.IKUKYU_SYURYOBI
                OR row_yukyu_ikukyu.DB_KEKKIN_SYURYOBI <> g_row_syutu_kekkin_db.KEKKIN_SYURYOBI
                OR row_yukyu_ikukyu.DB_YUKYU_IKUKYU_SEQUENCE_NO IS NULL THEN
                -- 差分があれば更新し、送信対象とする
                UPDATE IHR_SYUTU_KEKKIN_DB
                SET
                    CTRL_INFO = g_row_syutu_kekkin_db.CTRL_INFO,
                    KEKKIN_SYURYOBI = g_row_syutu_kekkin_db.KEKKIN_SYURYOBI,
                    IKUKYU_SYURYOBI = g_row_syutu_kekkin_db.IKUKYU_SYURYOBI,
                    YUKYU_IKUKYU_SEQUENCE_NO = g_row_syutu_kekkin_db.YUKYU_IKUKYU_SEQUENCE_NO,
                    SOSIN_TAISYO_YM = p_syori_ym,
                    SOSIN_TAISYO_FLG = C_FLG_ON,
                    LAST_UPDATE_DATE = C_LAST_UPDATE_DATE,
                    LAST_UPDATED_BY = C_LAST_UPDATED_BY,
                    LAST_UPDATE_LOGIN = C_LAST_UPDATE_LOGIN
                WHERE
                    PERSON_ID = row_yukyu_ikukyu.PERSON_ID
                    AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
            END IF;
        END IF;
    END IF;
END IF;

-- 当該産休育休レコードは、産休のみ (Case12)
ELSIF row_yukyu_ikukyu.DETAIL_IKUKYU_STATUS <> C_SYUTOKU_STATUS_SYUTOKU
    AND row_yukyu_ikukyu.DETAIL_SANGO_KYUMU_STATUS = C_SYUTOKU_STATUS_SYUTOKU THEN
    g_err_pos := '08100';

    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        YUKYU_IKUKYU_SEQUENCE_NO = NULL
    WHERE
        PERSON_ID = row_yukyu_ikukyu.PERSON_ID
        AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;

-- 全ての取消 (Case17)
ELSIF row_yukyu_ikukyu.DETAIL_IKUKYU_STATUS <> C_SYUTOKU_STATUS_SYUTOKU
    AND row_yukyu_ikukyu.DETAIL_SANGO_KYUMU_STATUS <> C_SYUTOKU_STATUS_SYUTOKU THEN
    IF row_yukyu_ikukyu.DETAIL_SANKYU_SOSIN_FLG = C_FLG_OFF THEN
        g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG := C_FLG_OFF;
        g_row_syutu_kekkin_db.KANRI_STATUS_KBN := C_KANRI_STATUS_KBN_END;
        g_row_syutu_kekkin_db.BYOMEI := C_DUMMY_BYOMEI;

        UPDATE IHR_SYUTU_KEKKIN_DB
        SET
            SOSIN_TAISYO_FLG = g_row_syutu_kekkin_db.SOSIN_TAISYO_FLG,
            KANRI_STATUS_KBN = g_row_syutu_kekkin_db.KANRI_STATUS_KBN,
            BYOMEI = g_row_syutu_kekkin_db.BYOMEI
        WHERE
            PERSON_ID = row_yukyu_ikukyu.PERSON_ID
            AND SEQUENCE_NO = row_yukyu_ikukyu.DB_SEQUENCE_NO;
    END IF;
END IF;

-- 管理中データの終了処理
FOR row_end_yukyu_kanri_status IN cur_end_yukyu_kanri_status(p_prev_syori_ymd => p_prev_syori_ymd) LOOP
    UPDATE IHR_SYUTU_KEKKIN_DB
    SET
        CTRL_INFO = C_CTRL_INFO_UPD,
        SOSIN_TAISYO_YM = p_syori_ym,
        SOSIN_TAISYO_FLG = C_FLG_ON,
        KANRI_STATUS_KBN = C_KANRI_STATUS_KBN_END,
        LAST_UPDATE_DATE = C_LAST_UPDATE_DATE,
        LAST_UPDATED_BY = C_LAST_UPDATED_BY,
        LAST_UPDATE_LOGIN = C_LAST_UPDATE_LOGIN
    WHERE
        PERSON_ID = row_end_yukyu_kanri_status.PERSON_ID
        AND SEQUENCE_NO = row_end_yukyu_kanri_status.SEQUENCE_NO;
END LOOP;

g_err_pos := '08700';

EXCEPTION
    WHEN EXC_MESSAGE_ERROR THEN
        RAISE;
    WHEN OTHERS THEN
        RAISE;
END YUKYU_IKUKYU_PROC;

-- filepath: /c:/Users/81804/Desktop/hibiki/21

-- PROCEDURE: RENZOKU_SINSEI_CHECK
-- 育休終了後、復職せずに次の産休に入る場合は、初回の有給育休は取得できないため、欠勤IF情報作成処理により作成された有給育休レコードを無効化するメンテナンスが事務方にて行う可能性がある。
-- 当チェックにより、その対象となる可能性のある申請をコンカレント結果ログに警告出力する。

PROCEDURE RENZOKU_SINSEI_CHECK (
    p_syori_ym IN VARCHAR2,
    p_warning_cnt OUT NUMBER -- 警告件数を返す。
) IS
    -- プロシージャ内変数の宣言
    l_warning_cnt NUMBER;
    l_syori_ym VARCHAR2(6);
    l_chk_cnt NUMBER;
    l_renzoku_sequence_no NUMBER; -- 2008/09/22
    l_renzoku_kekkin_kaisibi DATE; -- 2008/09/22

    -- 送信対象となる産休育休レコードまたは有給育休欠勤レコードを取得
    CURSOR cur_taisyo_record (
        p_syori_ym VARCHAR2
    ) IS
        SELECT DISTINCT
            db.KOIN_NO,
            db.PERSON_ID
        FROM
            IHR_SYUTU_KEKKIN_DB db
        WHERE
            db.SOSIN_TAISYO_YM = p_syori_ym
            AND db.SOSIN_TAISYO_FLG = C_FLG_ON
            AND db.CTRL_INFO <> C_CTRL_INFO_DEL -- コントロール情報: 削除を除く
            AND db.KEKKIN_JIYU IN (
                C_KEKKIN_JIYU_YUKYU_IKUKYU,
                C_KEKKIN_JIYU_SANKYU
            );

    -- 対象者の有給育休レコードを全て取得
    CURSOR cur_all_yukyu_record (
        p_person_id VARCHAR2
    ) IS
        SELECT
            db.KOIN_NO,
            db.PERSON_ID
        FROM
            IHR_SYUTU_KEKKIN_DB db
        WHERE
            db.PERSON_ID = p_person_id;
BEGIN
    -- 初期化
    l_warning_cnt := 0;

    -- 送信対象レコードのチェック
    OPEN cur_taisyo_record(p_syori_ym);
    LOOP
        FETCH cur_taisyo_record INTO l_syori_ym, l_chk_cnt;
        EXIT WHEN cur_taisyo_record%NOTFOUND;

        -- 対象者の有給育休レコードのチェック
        OPEN cur_all_yukyu_record(l_syori_ym);
        LOOP
            FETCH cur_all_yukyu_record INTO l_renzoku_sequence_no, l_renzoku_kekkin_kaisibi;
            EXIT WHEN cur_all_yukyu_record%NOTFOUND;

            -- 警告件数のカウント
            l_warning_cnt := l_warning_cnt + 1;
        END LOOP;
        CLOSE cur_all_yukyu_record;
    END LOOP;
    CLOSE cur_taisyo_record;

    -- 警告件数を返す
    p_warning_cnt := l_warning_cnt;
END RENZOKU_SINSEI_CHECK;


-- ユーザー定義例外の宣言
EXCEPTION
    EXC_MESSAGE_ERROR;

BEGIN
    g_err_pos := '10000';

    -- 変数初期化
    l_syori_ym := p_syori_ym;
    l_warning_cnt := 0;
    l_renzoku_sequence_no := NULL; -- 2008/09/22
    l_renzoku_kekkin_kaisibi := NULL; -- 2008/09/22
    l_chk_cnt := 0;

    g_err_pos := '10100';

    -- 送信対象レコードのチェック
    FOR row_taisyo_record IN cur_taisyo_record(p_syori_ym => l_syori_ym) LOOP
        g_err_pos := '10200';

        -- 対象者の全ての有給育休レコードを取得
        FOR row_all_yukyu_record IN cur_all_yukyu_record(p_person_id => row_taisyo_record.PERSON_ID) LOOP
            g_err_pos := '10300';

            -- 変数初期化
            l_renzoku_sequence_no := NULL; -- 2008/09/22
            l_renzoku_kekkin_kaisibi := NULL; -- 2008/09/22
            l_chk_cnt := 0;

            g_err_pos := '10400';

            BEGIN
                -- それぞれ有給育休レコードの後ろ一ヶ月以内に開始する、産休育休または有給育休を取得
                SELECT
                    COUNT(db.PERSON_ID)
                INTO
                    l_chk_cnt
                FROM
                    IHR_SYUTU_KEKKIN_DB db
                WHERE
                    db.KEKKIN_JIYU = C_KEKKIN_JIYU_YUKYU_IKUKYU
                    AND db.PERSON_ID = row_all_yukyu_record.PERSON_ID
                    AND db.SOSIN_TAISYO_FLG = C_FLG_ON -- 送信フラグONの申請
                    AND db.CTRL_INFO <> C_CTRL_INFO_DEL -- コントロール情報: 削除を除く
                    AND db.KEKKIN_SYURYOBI BETWEEN row_all_yukyu_record.KEKKIN_SYURYOBI AND ADD_MONTHS(row_all_yukyu_record.KEKKIN_SYURYOBI, 1);

                IF l_chk_cnt > 0 THEN
                    -- 警告件数のカウント
                    l_warning_cnt := l_warning_cnt + 1;
                END IF;
            END;

            -- filepath: /c:/Users/81804/Desktop/hibiki/109.pkb

g_err_pos := '10100';

-- 送信対象レコードのチェック
FOR row_taisyo_record IN cur_taisyo_record(p_syori_ym => l_syori_ym) LOOP
    g_err_pos := '10200';

    -- 対象者の全ての有給育休レコードを取得
    FOR row_all_yukyu_record IN cur_all_yukyu_record(p_person_id => row_taisyo_record.PERSON_ID) LOOP
        g_err_pos := '10300';

        -- 変数初期化
        l_renzoku_sequence_no := NULL; -- 2008/09/22
        l_renzoku_kekkin_kaisibi := NULL; -- 2008/09/22
        l_chk_cnt := 0;

        g_err_pos := '10400';

        BEGIN
            -- それぞれ有給育休レコードの後ろ一ヶ月以内に開始する、産休育休または有給育休を取得
            SELECT
                COUNT(db.PERSON_ID)
            INTO
                l_chk_cnt
            FROM
                IHR_SYUTU_KEKKIN_DB db
            WHERE
                db.KEKKIN_JIYU = C_KEKKIN_JIYU_YUKYU_IKUKYU
                AND db.PERSON_ID = row_all_yukyu_record.PERSON_ID
                AND db.SEQUENCE_NO <> row_all_yukyu_record.SEQUENCE_NO
                AND db.SOSIN_TAISYO_FLG = C_FLG_ON -- 送信フラグONの申請
                AND db.CTRL_INFO <> C_CTRL_INFO_DEL -- コントロール情報: 削除を除く
                AND db.KEKKIN_SYURYOBI BETWEEN row_all_yukyu_record.KEKKIN_SYURYOBI AND ADD_MONTHS(row_all_yukyu_record.KEKKIN_SYURYOBI, 1);

            IF l_chk_cnt > 0 THEN
                -- 警告件数のカウント
                l_warning_cnt := l_warning_cnt + 1;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                g_err_pos := '10600';
                l_chk_cnt := 0;
        END;

        g_err_pos := '10500';

        -- 後ろ一ヶ月以内に開始するレコードがあれば警告出力
        IF l_chk_cnt <> 0 THEN
            g_err_pos := '10700';
            IHRCSBTOO_PKG.PUT_MSG(
                p_msgid => 'IHRHR719',
                p_token_1 => row_all_yukyu_record.KOIN_NO,
                p_token_2 => row_all_yukyu_record.SEQUENCE_NO,
                p_token_3 => TO_CHAR(row_all_yukyu_record.KEKKIN_SYURYOBI, 'YYYY/MM/DD')
            );
            g_err_pos := '10800';
            RAISE EXC_MESSAGE_ERROR;
        END IF;
    END LOOP;
END LOOP;

g_err_pos := '10900';
p_warning_cnt := l_warning_cnt;

EXCEPTION
    WHEN EXC_MESSAGE_ERROR THEN
        RAISE;
    WHEN OTHERS THEN
        RAISE;
END RENZOKU_SINSEI_CHECK;

/* PROCEDURE: SEIDO_MAE_SINSEI_CHECK
   制度開始日前に開始された有給育休レコードが存在する場合ログ出力する */

PROCEDURE SEIDO_MAE_SINSEI_CHECK (
    p_syori_ym IN VARCHAR2,
    p_warning_cnt OUT NUMBER -- 警告件数を返す。
) IS
    -- プロシージャ内変数の宣言
    l_warning_cnt NUMBER;
    l_yukyu_ikukyu_start_date DATE;
    l_syori_ym VARCHAR2(6);

    -- 制度開始日前に開始された有給育休欠勤レコードを取得
    CURSOR cur_taisyo_record (
        p_yukyu_ikukyu_start_date DATE,
        p_syori_ym VARCHAR2
    ) IS
        SELECT
            db.KOIN_NO,
            db.PERSON_ID,
            db.KEKKIN_KAISIBI,
            db.KEKKIN_SYURYOBI,
            db.SEQUENCE_NO
        FROM
            IHR_SYUTU_KEKKIN_DB db
        WHERE
            db.SOSIN_TAISYO_YM = p_syori_ym
            AND db.KEKKIN_JIYU = C_KEKKIN_JIYU_YUKYU_IKUKYU
            AND db.KEKKIN_KAISIBI < p_yukyu_ikukyu_start_date
            AND db.SOSIN_TAISYO_FLG = C_FLG_ON; -- 送信フラグONの申請

    -- ユーザー定義例外の宣言
    EXCEPTION
        EXC_MESSAGE_ERROR;

BEGIN
    g_err_pos := '12300';

    -- 変数初期化
    l_warning_cnt := 0;
    l_yukyu_ikukyu_start_date := NULL;
    l_syori_ym := p_syori_ym;

    g_err_pos := '12400';

    BEGIN
        SELECT
            TO_DATE(MEANING, 'YYYYMMDD')
        INTO
            l_yukyu_ikukyu_start_date
        FROM
            FND_LOOKUP_VALUES
        WHERE
            LOOKUP_TYPE = 'IHR_YUKYU_IKUKYU_START_DATE'
            AND LANGUAGE = 'JA'
            AND LOOKUP_CODE = '1';

        g_err_pos := '12500';
    EXCEPTION
        WHEN OTHERS THEN
            RAISE;
    END;

    g_err_pos := '12600';

    FOR row_taisyo_record IN cur_taisyo_record(
        p_yukyu_ikukyu_start_date => l_yukyu_ikukyu_start_date,
        p_syori_ym => l_syori_ym
    ) LOOP
        g_err_pos := '12700';

        IF NOT IHRCSBTOO_PKG.PUT_MSG(
            p_msgid => 'IHRHR720',
            p_token_1 => row_taisyo_record.KOIN_NO,
            p_token_2 => row_taisyo_record.SEQUENCE_NO,
            p_token_3 => TO_CHAR(row_taisyo_record.KEKKIN_KAISIBI, 'YYYY/MM/DD'),
            p_token_4 => TO_CHAR(row_taisyo_record.KEKKIN_SYURYOBI, 'YYYY/MM/DD')
        ) THEN
            g_err_pos := '12800';
            RAISE EXC_MESSAGE_ERROR;
        END IF;

        l_warning_cnt := l_warning_cnt + 1;
    END LOOP;

    g_err_pos := '12900';
    p_warning_cnt := l_warning_cnt;
END SEIDO_MAE_SINSEI_CHECK;


-- filepath: /c:/Users/81804/Desktop/hibiki/109.pkb

p_warning_cnt := l_warning_cnt;

EXCEPTION
    WHEN EXC_MESSAGE_ERROR THEN
        RAISE;
    WHEN OTHERS THEN
        RAISE;
END SEIDO_MAE_SINSEI_CHECK;

-- PROCEDURE: ONAJI_SYUSSANBI_SINSEI_CHECK
-- 同じ出産日で複数の育休申請が存在するかチェック
-- 二回目以降の有給育休期間は無しとするため、手動メンテナンスが必要。
-- 当チェックにより、その対象となる可能性ある申請を抽出する。

PROCEDURE ONAJI_SYUSSANBI_SINSEI_CHECK (
    p_syori_ym IN VARCHAR2,
    p_warning_cnt OUT NUMBER -- 警告件数を返す。
) IS
    -- プロシージャ内変数の宣言
    l_warning_cnt NUMBER;
    l_syori_ym VARCHAR2(6);
    l_chk_cnt NUMBER;
    l_onaji_syussanbi_seq_no NUMBER; -- 2008/09/22
    l_onaji_syussanbi_kaisi DATE; -- 2008/09/22

    -- 今回送信対象の有給育休レコードの出産(予定)日を取得
    CURSOR cur_taisyo_record (
        p_syori_ym VARCHAR2
    ) IS
        SELECT
            db.KOIN_NO,
            db.PERSON_ID,
            db.KEKKIN_KAISIBI,
            NVL(isid.SYUSSANBI, isid.SYUSSAN_YOTEIBI) AS SYUSSANBI,
            db.SEQUENCE_NO
        FROM
            IHR_SYUTU_KEKKIN_DB db,
            IHR_SANKYU_IKUKYU_DETAILS isid
        WHERE
            db.SOSIN_TAISYO_YM = p_syori_ym
            AND db.KEKKIN_JIYU = C_KEKKIN_JIYU_YUKYU_IKUKYU
            AND db.PERSON_ID = isid.PERSON_ID
            AND db.SEQUENCE_NO = isid.YUKYU_TKUKYU_SEQUENCE_NO
            AND db.SOSIN_TAISYO_FLG = C_FLG_ON -- 送信フラグONの申請
            AND db.CTRL_INFO <> C_CTRL_INFO_DEL; -- コントロール情報: 削除を除く

    -- ユーザー定義例外の宣言
    EXCEPTION
        EXC_MESSAGE_ERROR;

BEGIN
    g_err_pos := '13000';

    -- 変数初期化
    l_warning_cnt := 0;
    l_syori_ym := p_syori_ym;
    l_chk_cnt := 0;

    g_err_pos := '13100';

    -- 送信対象の有給育休を取得
    FOR row_taisyo_record IN cur_taisyo_record(p_syori_ym => l_syori_ym) LOOP
        g_err_pos := '13200';

        -- 同じ出産日の有給育休レコードを検索
        BEGIN
            SELECT
                COUNT(db.PERSON_ID)
            INTO
                l_chk_cnt
            FROM
                IHR_SYUTU_KEKKIN_DB db,
                IHR_SANKYU_IKUKYU_DETAILS isid
            WHERE
                NVL(isid.SYUSSANBI, isid.SYUSSAN_YOTEIBI) = row_taisyo_record.SYUSSANBI
                AND isid.PERSON_ID = row_taisyo_record.PERSON_ID
                AND db.PERSON_ID = isid.PERSON_ID
                AND isid.YUKYU_IKUKYU_SEQUENCE_NO = db.SEQUENCE_NO
                AND db.KEKKIN_JIYU = C_KEKKIN_JIYU_YUKYU_IKUKYU
                AND db.SEQUENCE_NO <> row_taisyo_record.SEQUENCE_NO
                AND db.CTRL_INFO <> C_CTRL_INFO_DEL; -- コントロール情報: 削除を除く

            g_err_pos := '13300';
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                l_chk_cnt := 0;
        END;

        -- 同じ出産日の有給育休レコードがある場合は警告ログ出力
        IF l_chk_cnt <> 0 THEN
            g_err_pos := '13400';
            IF NOT IHRCSBTOO_PKG.PUT_MSG(
                p_msgid => 'IHRHR721',
                p_token_1 => row_taisyo_record.KOIN_NO,
                p_token_2 => row_taisyo_record.SEQUENCE_NO,
                p_token_3 => TO_CHAR(row_taisyo_record.KEKKIN_KAISIBI, 'YYYY/MM/DD')
            ) THEN
                RAISE EXC_MESSAGE_ERROR;
            END IF;
            l_warning_cnt := l_warning_cnt + 1;
        END IF;
    END LOOP;

    g_err_pos := '13500';
    p_warning_cnt := l_warning_cnt;

EXCEPTION
    WHEN EXC_MESSAGE_ERROR THEN
        RAISE;
    WHEN OTHERS THEN
        RAISE;
END ONAJI_SYUSSANBI_SINSEI_CHECK;

-- PROCEDURE: END_DATE_CHECK
-- 今回送信対象のデータのうち育休明細の終了日と作成された出欠勤DBの有給育休レコードの終了日が一致しているかをチェック

PROCEDURE END_DATE_CHECK (
    p_syori_ym IN VARCHAR2,
    p_warning_cnt OUT NUMBER -- 警告件数を返す。
) IS
    -- プロシージャ内変数の宣言
    l_warning_cnt NUMBER;
    l_syori_ym VARCHAR2(6);

    -- 対象レコードを取得
    CURSOR cur_taisyo_record (
        p_syori_ym VARCHAR2
    ) IS
        SELECT
            isid.KOIN_NO,
            isid.SEQUENCE_NO,
            NVL(isid.IKUKYU_SYURYOBI, isid.IKUKYU_SYURYO_YOTEIBI) AS DETAIL_END_DATE,
            iskd.KEKKIN_SYURYOBI AS DB_END_DATE
        FROM
            IHR_SANKYU_IKUKYU_DETAILS isid,
            IHR_SYUTU_KEKKIN_DB iskd
        WHERE
            isid.KOIN_NO = iskd.KOIN_NO
            AND isid.YUKYU_IKUKYU_SEQUENCE_NO = iskd.SEQUENCE_NO
            AND NVL(isid.IKUKYU_SYURYOBI, isid.IKUKYU_SYURYO_YOTEIBI) <> iskd.KEKKIN_SYURYOBI
            AND iskd.SOSIN_TAISYO_FLG = C_FLG_ON
            AND iskd.SOSIN_TAISYO_YM = p_syori_ym
            AND iskd.KEKKIN_JIYU = C_KEKKIN_JIYU_YUKYU_IKUKYU;

    -- ユーザー定義例外の宣言
    EXCEPTION
        EXC_MESSAGE_ERROR;

BEGIN
    g_err_pos := '13600';

    -- 変数初期化
    l_warning_cnt := 0;
    l_syori_ym := p_syori_ym;

    g_err_pos := '13700';

    FOR row_taisyo_record IN cur_taisyo_record(p_syori_ym => l_syori_ym) LOOP
        g_err_pos := '13800';

        IF NOT IHRCSBTOO_PKG.PUT_MSG(
            p_msgid => 'IHRHR773',
            p_token_1 => TO_CHAR(row_taisyo_record.DETAIL_END_DATE, 'YYYY/MM/DD'),
            p_token_2 => TO_CHAR(row_taisyo_record.DB_END_DATE, 'YYYY/MM/DD'),
            p_token_3 => row_taisyo_record.KOIN_NO,
            p_token_4 => row_taisyo_record.SEQUENCE_NO
        ) THEN
            g_err_pos := '13900';
            RAISE EXC_MESSAGE_ERROR;
        END IF;

        l_warning_cnt := l_warning_cnt + 1;
    END LOOP;

    g_err_pos := '14000';
    p_warning_cnt := l_warning_cnt;

EXCEPTION
    WHEN EXC_MESSAGE_ERROR THEN
        RAISE;
    WHEN OTHERS THEN
        RAISE;
END END_DATE_CHECK;



