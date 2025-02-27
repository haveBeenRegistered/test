public String 30tkinmuHotegaiHantel(int person_id, String kinmubi) {
    String result = "";
    String sql;

    BEGIN
    1: IHR_WF_KINMU.GET_HOTEGAI_HANTE120 +
    in_person_id => :2, iv_kinmubi => :3
    END;

    OracleCallableStatement stmt = null;

    try {
        stmt = (OracleCallableStatement) getOADBTransaction().createCallableStatement(sql, 1);
        stmt.registerOutParameter(1, Types.VARCHAR, 0,2);
        stmt.setInt(2, person_id);
        stmt.setString(3, kinmubi);
        stmt.execute();
        result = stmt.getString(1);
        stmt.close();
    } catch (SQLException ex) {
        throw OAException.wrapperException(ex);
    }
    return result;
}

/**
 * 勤務締め処理チェック - 考勤结算处理检查
 * 参数说明:
 * @param p_person_id - 员工ID
 * @param p_tst_start_date - 开始日期
 * @param p_tgt_end_date - 结束日期
 * @param p_syugyo_kbn_chk_flg - 就业区分检查标志
 * @return 错误消息(无错误时返回NULL)
 */
FUNCTION CHK_KINMU_SIME(
    p_person_id          IN VARCHAR2,    -- 员工ID
    p_tst_start_date     IN VARCHAR2,    -- 开始日期(YYYYMMDD格式)
    p_tgt_end_date       IN VARCHAR2,    -- 结束日期(YYYYMMDD格式) 
    p_syugyo_kbn_chk_flg IN VARCHAR2     -- 就业区分检查标志
) RETURN VARCHAR2 IS

    -- 定义局部变量
    l_rtn        VARCHAR2(1);    -- 用于存储检查结果的返回值
    l_start_ym   VARCHAR2(6);    -- 开始年月(YYYYMM格式)
    l_end_ym     VARCHAR2(6);    -- 结束年月(YYYYMM格式)
    l_start_date DATE;           -- 开始日期
    l_end_date   DATE;           -- 结束日期

BEGIN
    -- 设置错误位置标识
    g_err_pos := '04000';

    -- 从输入的日期字符串中提取年月部分
    l_start_ym := SUBSTR(p_tst_start_date, 1, 6);  -- 取开始日期的前6位作为年月
    l_end_ym := SUBSTR(p_tgt_end_date, 1, 6);      -- 取结束日期的前6位作为年月

    -- 循环处理每个月份的检查
    LOOP
        g_err_pos := '04100';
        
        -- 检查当前月份的勤务月次信息状态
        l_rtn := CHK_KINMU_MONTH_STATUS(p_person_id, l_start_ym);
        
        -- 如果月次信息状态检查返回'1'
        IF l_rtn = '1' THEN
            g_err_pos := '04200';
            
            -- 如果就业区分检查标志为'2'，直接返回错误
            IF p_syugyo_kbn_chk_flg = '2' THEN
                RETURN 'IHR_SELF_KNMCMN_ERR_004';
            ELSE
                g_err_pos := '04300';
                
                -- 设置当月处理期间
                -- 设置月初日期
                l_start_date := TO_DATE(l_start_ym || '01', 'YYYYMMDD');
                -- 设置月末日期
                l_end_date := LAST_DAY(TO_DATE(l_start_ym || '01', 'YYYYMMDD'));
                
                -- 调整实际处理的开始和结束日期
                IF p_tst_start_date > l_start_date THEN
                    l_start_date := TO_DATE(p_tst_start_date, 'YYYYMMDD');
                END IF;
                
                IF p_tgt_end_date < l_end_date THEN
                    l_end_date := TO_DATE(p_tgt_end_date, 'YYYYMMDD');
                END IF;
                
                -- 根据检查标志进行不同的检查处理
                -- 如果是批量导入检查(标志='0')
                IF p_syugyo_kbn_chk_flg = '0' THEN
                    l_rtn := CHK_IKKATU_TORIKOMI(
                        p_person_id,
                        TO_CHAR(l_start_date, 'YYYYMMDD'),
                        TO_CHAR(l_end_date, 'YYYYMMDD')
                    );
                END IF;
                
                -- 如果是就业区分检查(标志='1')
                IF p_syugyo_kbn_chk_flg = '1' THEN
                    l_rtn := CHK_KINMU_SYUGYO_KBN(
                        p_person_id,
                        TO_CHAR(l_start_date, 'YYYYMMDD'),
                        TO_CHAR(l_end_date, 'YYYYMMDD')
                    );
                    
                    -- 如果就业区分检查返回'1'，表示存在错误
                    IF l_rtn = '1' THEN
                        RETURN 'IHR_SELF_KNMCMN_ERR_004';
                    END IF;
                END IF;
            END IF;
        END IF;

        g_err_pos := '04400';
        
        -- 检查是否处理完所有月份
        IF l_start_ym >= l_end_ym THEN
            EXIT;  -- 如果当前处理月份大于等于结束月份，退出循环
        END IF;

        g_err_pos := '04450';
        
        -- 设置下一个月份
        l_start_ym := TO_CHAR(ADD_MONTHS(TO_DATE(l_start_ym || '01', 'YYYYMMDD'), 1), 'YYYYMM');
    END LOOP;

    -- 所有检查都通过，返回NULL
    g_err_pos := '04500';
    RETURN NULL;

-- 异常处理部分
EXCEPTION
    WHEN OTHERS THEN
        -- 发生未预期的错误时，抛出应用错误
        RAISE_APPLICATION_ERROR(
            -20000,
            'ERR_POS: ' || IHR_KINMU_COMMON.g_err_pos || 
            ' SQLERR: ' || SQLCODE || l_start_ym
        );
END CHK_KINMU_SIME;