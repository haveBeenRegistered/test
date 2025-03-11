public class KinmuMeisaiReviewCO extends OASecondController {

    public static final String RCS_ID = "$Header$";
    public static final boolean RCS_ID_RECORDED = VersionInfo.recordClassVersion(RCS_ID, "Xpackagename%");

    // 勤務環境変数
    private static final String KINMU_ENV_C1_TYOKA = "KINMU_TYOKA";
    private static final String KINMU_ENV_C2_TYOKA = "HOTEGAI_GOKEI";
    private static final String KINMU_ENV_C3_TYOKA = "TOGETU";
    private static final String KINMU_ENV_CAT_TYOKA = "TYOKA_WARNING_TIME";
    private static final String KINMU_ENV_C42_TYOKA = "TYOKA_LIMIT_TIME";
    private static final String KINMU_ENV_C43_TYOKA = "TEKIYO_KAIST_DATE";

    // 申請種類
    private static final String Meisai = "1";
    private static final String Entyo = "4";

    // 就業区分
    private static final String Syukin = "2";
    private static final String Ode = "3";

    // ワーニングフラグ
    private static final String Warn = "1"; // ワーニングあり

    /**
     * Layout and page setup logic for a region.
     *
     * @param pageContext the current OAPageContext
     * @param webBean     the web bean corresponding to the region
     */
    public void processRequestX(OAPageContext pageContext, OAWebBean webBean) {
        try {
            // パーソンIDの取得
            String personId = ProxyManager.getPersonId(pageContext);
            int person_id = Integer.parseInt(personId);
            KinmuAMImpl myAm = (KinmuAMImpl) pageContext.getApplicationModule(webBean);

            // 行員番号取得
            String koinNo = myAm.getKoinNo(personId);
            boolean butentyoCheckFlg = myAm.bushitsuTentyoChk(pageContext);

            if (!butentyoCheckFlg) {
                butentyoCheckFlg = myAm.kikakuChk(1);
            }

            ViewObject inputVO = myAm.getKinmuInputVO();
            KinmuInputVORowImpl inputValue = (KinmuInputVORowImpl) inputVO.first();
            int tgtMonth = Integer.parseInt(inputValue.getTgtMonth());

            OAMessageStyledTextBean bean = (OAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrKinmuShihanki");
            bean.setLabel(myAm.getTgtShikanki(tgtMonth));

            OAMessageStyledTextBean bean2 = (OAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrKinmuShihankiKaigai");
            bean2.setLabel(myAm.getTgtShikanki(tgtMonth));

            // 警告時間の取得
            String strWarningHo = myAm.getKinmuEnvValue(KINMU_ENV_C1_TYOKA, KINMU_ENV_C2_TYOKA, KINMU_ENV_C3_TYOKA, KINMU_ENV_C41_TYOKA);
            String strLimitNo = myAm.getKinmuEnvValue(KINMU_ENV_C1_TYOKA, KINMU_ENV_C2_TYOKA, KINMU_ENV_C3_TYOKA, KINMU_ENV_C42_TYOKA);

            // 警告メッセージに可変値(警告時間,上限時間)をセット
            MessageToken[] token = new MessageToken[3];
            token[0] = new MessageToken("JIKAN1", strWarningHo);
            token[1] = new MessageToken("JIKAN2", strLimitNo);
            token[2] = new MessageToken("JIKAN3", strLimitNo);

            String keikokuMsg = pageContext.getMessage("IHR", "IHR_SELF_KINMU_HIDE_041", token);
            OAStaticStyledTextBean msg06 = (OAStaticStyledTextBean) webBean.findChildRecursive("IhrMessage00");
            msg06.setText(keikokuMsg);

            String kinmuBi = CmnFunc2.convertJboDomainToString(inputValue.getKinmuBiInput());
            String tatYM = kinmuBi.substring(0, 6);

            // 適用開始日の取得
            String startDate = myAm.getKinmuEnvValue(KINMU_ENV_C1_TYOKA, KINMU_ENV_C2_TYOKA, KINMU_ENV_C3_TYOKA, KINMU_ENV_C43_TYOKA);

            // 法定外合計が基準の値を超えた時、警告文言を表示(労務管理:80時間遵守対応)
            if (myAm.getKinmuHoteiGaiHantei(person_id, kinmuBi.equals("0"), Integer.parseInt(startDate), Integer.parseInt(kinmuBi))) {
                msg06.setRendered(false);
            }
// 人事企画Gr勤務担当者、勤務承認者・スタッフ人事による代理申請以外
if (ProxyManager.isProxy(pageContext) && myAm.getKikakuStaffChk()) {
    DAFRowLayoutBean bean4 = (DAFRowLayoutBean) webBean.findIndexedChildRecursive("IhrNest25");
    bean4.setRendered(false);

    DAStaticStyledTextBean bean5 = (DAStaticStyledTextBean) webBean.findIndexedChildRecursive("ThrMessage27");
    bean5.setRendered(false);
}

// 替店長・企画Grによるオペレーションの場合、承認者入力項目を非表示とする
if (butentyoCheckFlg) {
    if (CanFunc2.getConst("C_RESP_MUSS").equalsIgnoreCase(myAm.getLoginResp())
            || CanFunc2.getConst("CRESP_STAFF_JINJI").equalsIgnoreCase(myAm.getLoginResp())) {
        CAMessageStyledTextBean bean2 = (CAMessageStyledTextBean) webBean.findIndexedChildRecursive("ThrkinmuRevSyonin");
        bean2.setRendered(false);

        OAMessageLovInputBean bean3 = (OAMessageLovInputBean) webBean.findIndexedChildRecursive("IhrkinmuinputBusitu");
        bean3.setRendered(false);
    }
}

// 行員番号がPから始まる行員の場合は非表示
if (koinNo.substring(0, 1).equals("P")) {
    DAMessageStyledTextBean kanribean = (DAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrkinmuRevkanri");
    kanribean.setRendered(false);

    DAMessageStyledTextBean sairyobean = (DAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrkinmuRevSairyo");
    sairyobean.setRendered(false);
}

// 契約社員のパートタイマーで、申請月内の労働時間(含有休)が80時間を超えた場合にワーニングを返す
KinmuInputRecordVORowImpl recordRow = (KinmuInputRecordVORowImpl) myAm.getKinmuInputRecordVO().first();
if (recordRow != null && recordRow.getShuugyouKbn().equals("2")) { // 就業区分がパートタイマーの場合
    if (equals(CanFunc.nvl(inputValue.getWarn()))) {
        pageContext.putDialogMessage(new OAException("IHR", inputValue.getWarn(), null, OAException.WARNING, null));
    }
}

// 在宅勤務申請日に8時~20時外の勤務をしている場合、ワーニングを返す
if (equals(CanFunc.nvl(inputValue.getWarn11()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn11(), null, OAException.WARNING, null));
}

// ワーニングメッセージの表示を制御する
if (equals(CanFunc.nvl(inputValue.getWarn1()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn1(), null, OAException.WARNING, null));
}

// 2011/09/10 契約社員の場合
if (CanFunc2.getConst("CKEIYAKU_SYAIN_SYOKUSYU3").equalsIgnoreCase(recordRow.getSyokusyu())) {
    pageContext.putDialogMessage(
            new OAException("IHR", "IHR_SELF_KINMU_INPUT_WARN_09", null, OAException.WARNING, null));
} else {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn(), null, OAException.WARNING, null));
}

if (equals(CanFunc.nvl(inputValue.getWarn7()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn7(), null, OAException.WARNING, null));
}

OAMessageChoiceBean poplist = (OAMessageChoiceBean) webBean.findIndexedChildRecursive("ThrKinmuSaburokukbn");
poplist.setPickListCacheEnabled(false);
poplist.setListDisplayAttribute("Meaning");
poplist.setListValueAttribute("Saburokukbn");
poplist.setPickListViewUsageName("KinmuSaburokukbnListV01");

if (CanFunc2.getConst("CKEIYAKU_SYAIN_SYOKUSYUQ").equalsIgnoreCase(recordRow.getSyokusyu())) {
    DAStackLayoutBean entyoBean = (DAStackLayoutBean) webBean.findIndexedChildRecursive("threntyoSingle");
    entyoBean.setRendered(false);

    DAStackLayoutBean hideBean = (DAStackLayoutBean) webBean.findIndexedChildRecursive("IhrEntyoHide01");
    hideBean.setRendered(false);
} else {
    DAStackLayoutBean entyoBean = (DAStackLayoutBean) webBean.findIndexedChildRecursive("IhrEntyoSingle01");
    entyoBean.setRendered(false);

    DAStackLayoutBean hideBean = (DAStackLayoutBean) webBean.findChildRecursive("IhrEntyoHide01");
    hideBean.setRendered(false);
}

if (equals(CanFunc.nvl(inputValue.getWarn2()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn2(), null, OAException.WARNING, null));
}

if (equals(CanFunc.nvl(inputValue.getWarn3()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn3(), null, OAException.WARNING, null));
}

if (equals(CanFunc.nvl(inputValue.getWarn4()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn4(), null, OAException.WARNING, null));
}

if (equals(CanFunc.nvl(inputValue.getWarn5()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn5(), null, OAException.INFORMATION, null));
}

if (equals(CanFunc.nvl(inputValue.getWarn8()))) {
    pageContext.putDialogMessage(
            new OAException("IHR", inputValue.getWarn8(), null, OAException.INFORMATION, null));
}

CanFunc.setUnvalidated(webBean, "ThrReturn", true);

if (CanFunc2.getConst("CKEIYAKU_SYAIN_SYOKUSYU3").equalsIgnoreCase(recordRow.getSyokusyu())) {
    LOAMessageStyledTextBean tukinhiBean = (LOAMessageStyledTextBean) webBean.findChildRecursive("IhrKinmuRevMonTukinhi");
    tukinhiBean.setDataType("NUMBER");

    DAMessageStyledTextBean tyusyokuBean = (DAMessageStyledTextBean) webBean.findChildRecursive("IhrKineuRevMonTyusyoku");
    tyusyokuBean.setDataType("NUMBER");
}

// OATableBeanの設定
OATableBean waritable = (OATableBean) webBean.findIndexedChildRecursive("TheKinmul");
waritable.prepareForRendering(pageContext);

DataObjectList waricolumnFormat = waritable.getColumnFormats();
DictionaryData hiben = (DictionaryData) waricolumnFormat.getItem(pageContext.findChildIndex(waritable, "Thikinmevarimashi"));
hiben.put("CELL_NO_WRAP_FORMAT_KEY", Boolean.FALSE);
hiben.put("COLUMN_DATA_FORMAT_KEY", "ICON_BUTTON_FORMAT");
hiben.put("WIDTH_KEY", "140");

DictionaryData ikho = (DictionaryData) waricolumnFormat.getItem(pageContext.findChildIndex(waritable, "ThrkinesRevkarimasi"));
ikho.put("CELL_NO_WRAP_FORMAT_KEY", Boolean.FALSE);
ikho.put("COLUMN_DATA_FORMAT_KEY", "ICON_BUTTON_FORMAT");
ikho.put("WIDTH_KEY", "140");

if (CanFunc2.getConst("CKEIYAKU_SYAIN_SYOKUSYUQ").equalsIgnoreCase(recordRow.getSyokusyu())) {
    CAMessageStyledTextBean telierbean = (CAMessageStyledTextBean) webBean.findIndexedChildRecursive("ShekimReviellar");
    telierbean.setRendered(false);

    DAMessageStyledTextBean tukinhiBean = (DAMessageStyledTextBean) webBean.findIndexedChildRecursive("TheKinmuRevTukinhi");
    tukinhiBean.setRendered(false);

    OAMessageStyledTextBean rodojikanMonbean = (OAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrKineuReMonRodojikan");
    rodojikanMonbean.setRendered(false);

    DAMessageStyledTextBean teilerMonbean = (DAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrKineuReMonRodojikan");
    teilerMonbean.setRendered(false);

    OAMessageStyledTextBean tukinhiMonbean = (OAMessageStyledTextBean) webBean.findIndexedChildRecursive("IhrKineuRevMonTukinhi");
    tukinhiMonbean.setRendered(false);

    DAMessageStyledTextBean tyusyokuBean = (DAMessageStyledTextBean) webBean.findIndexedChildRecursive("ThuKineRevMonTyusyoku");
    tyusyokuBean.setRendered(false);
}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 其他方法...

}




CREATE OR REPLACE FUNCTION GET_HOTEGAI_HANTE12(
    in_person_id IN NUMBER,
    iv_kinmubi IN VARCHAR2
) RETURN VARCHAR2 IS
    ov_result VARCHAR2(2) := '0';
    in_hotegai_goke NUMBER;
    id_kinmubi DATE;
    iv_kinmubi VARCHAR2(6);
BEGIN
    id_kinmubi := TO_DATE(iv_kinmubi, 'YYYYMMDD');
    iv_kinmubi := TO_CHAR(id_kinmubi, 'YYYYMM');

    SELECT SUM(ikgj.HOTEGAI_GOKE) ― SUM(NVL(ikkj.HOTEGAI_GOKE_KAIGAI, 0)) HOTEGAL_GOKE INTO in_hotegai_goke
    FROM 
(SELECT 
    PERSON_ID,
    TAISYO_YM,
    KANRI_KBN,
    SAIRYO_FLAG,
    SUM(HOTEGAI_GOKE) AS HOTEGAI_GOKE
FROM 
    IHR_KINMU_GETUJI_JOHO
WHERE 
    PERSON_ID IN (person_id)
    AND KINMUBI = TAISYO_YM
    AND (IKKATU_FLG <> 'Y' OR IKKATU_FLG IS NULL)
GROUP BY 
    PERSON_ID, 
    TAISYO_YM, 
    KANRI_KBN, 
    SAIRYO_FLAG) ikgj,
IHR_KINMU_GETUJI_JOHO ikkj
    where
    ikgj.PERSON_ID = ikkj.PERSON_ID(+)
    AND ikgj.TAISYO_YM = ikkj.TAISYO_YM
    AND ikgj.SAIRYO_FLAG = ikkj.SAIRYO_FLAG
    AND ikgj.KANRI_KBN = ikkj.KANRI_KBN
    AND ikkj.SYUKET_KBN = 1
    AND ikkj.ENABLED_FLAG = 'Y'
    GROUP BY ikgj.PERSON_ID, ikgj.TAISYO_YM;

    IF in_hotegai_goke > TO_NUMBER(THR_KINMU_COMMON.GET_KINMU_ENV_VALUE('KINMU_TYOKA', 'HOTEGAI_GOKE', 'TOGETU', 'TYOKA_WARNING_TIME')) * 60 THEN
        ov_result := '1';
    END IF;

    RETURN ov_result;
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END GET_HOTEGAI_HANTE12;





public void processFormRequestX(OAPageContext pageContext, DAlWebBean webBean) {
    try {
        // Get person ID from proxy manager
        String personId = ProxyManager.getPersonId(pageContext);
        PageTransition.setRedirectCurrentPage(pageContext);

        // Get application module
        EntyoListAMImpl myAm = (EntyoListAMImpl) pageContext.getApplicationModule(webBean);

        // Next button handling
        if (pageContext.getParameter("IhrNext") != null) {
            myAm.raiseMeisaiValidation(pageContext.personId);
            
            if (!myAm.syoninChk(pageContext) && !ProxyManager.isProxy(pageContext)) {
                // Create approval LOV (List of Values)
                CommonLOVCO appLov = new CommonLOVCO();
                
                // Set approval values
                appLov.setApprovalValue(pageContext, webBean, "EntyoSupervisor", "IhrEntyoJid");
                appLov.setApprovalValue(pageContext, webBean, "EntyoDivisionChief", "IhrEntyoJid");

                // Set forward URL for review page
                pageContext.setForwardURL(
                    "IHR_ENTYO_MEISAL_REVIEW", 
                    null, 
                    true, 
                    OAException.ERROR
                );

                myAm.inputChkText();
                myAm.setWarning();
                myAm.setPoplist();
            }
        }

        // Return button handling
        if (pageContext.getParameter("IhrReturn") != null) {
            pageContext.setForwardURL(
                "IHR_ENTYO_LIST", 
                null, 
                true, 
                OAException.ERROR
            );
        }

    } catch (Exception e) {
        // Raise runtime exception with original exception
        BundledException.raiseRuntime(this.pageContext, e);
    }
}




/**
 * 勤務実績照会から勤務実績入力への画面遷移処理
 */
public void listToInput() {
    // 入力値保持用VOを取得
    ViewObject kHsKinmuInputVO = this.getKHsKinmuInputVO();
    kHsKinmuInputVO.setWhereClauseParams(null);
    kHsKinmuInputVO.executeQuery();
    KHsKinmuInputVORowImpl inputValue = (KHsKinmuInputVORowImpl)kHsKinmuInputVO.first();

    // 一覧VOを取得
    ViewObject kHsKinmuListVO = this.getKHsKinmuListVO();
    kHsKinmuListVO.reset();

    boolean isPushFlag = false;

    // ラジオボタンが押された行を見つけるまでループ
    while (kHsKinmuListVO.hasNext() && !isPushFlag) {
        KHsKinmuListVORowImpl kHsKinmuListRow = (KHsKinmuListVORowImpl)kHsKinmuListVO.next();

        // ラジオボタンのチェックを見つけた場合
        if ("Y".equals(kHsKinmuListRow.getSentaku())) {
            // 勤務日を設定
            inputValue.setKinmubi(kHsKinmuListRow.getKinmubi());
            inputValue.setKinmubiInput(kHsKinmuListRow.getKinmubi());
            isPushFlag = true;
        }
    }
}




/**
 * Procedure to handle form submissions for form elements in a region.
 *
 * @param pageContext the current OA page context
 * @param webBean     the web bean corresponding to the region
 */
public void processFormRequestX(OAPageContext pageContext, OAWebBean webBean) {
    try {
        KinmuAMImpl myAm = (KinmuAMImpl) pageContext.getApplicationModule(webBean);
        String personId = ProxyManager.getPersonId(pageContext);

        // 人事ポータルへ戻るボタン押下
        if (pageContext.getParameter("IhrReturnPortal") != null) {
            pageContext.setForwardURL(
                "IHR_COMMON_RETURN_PORTAL", 
                RESET_MENU_CONTEXT,
                null, 
                false, 
                ADD_BREAD_CRUMB_NO, 
                OAException.ERROR
            );
        }
        // 照会画面に遷移します
        else if (pageContext.getParameter("IhrkinmuMeisaiInputRtn") != null) {
            pageContext.setForwardURL(
                "OA.jsp?page=/oracle/apps/ihr/kinmu/pages/IHR_KINMU_LIST_MAIN",
                null,
                REMOVE_MENU_CONTEXT,
                null,
                true,
                ADD_BREAD_CRUMB_NO,
                OAException.ERROR
            );
        }
        
        // 处理“下一步”按钮逻辑
        if (pageContext.getParameter("IhrNext") != null) {
            // 将时间单位有给的输入值反映到业务模块中
            this.setJkanTaniKyuka(pageContext, myAm);
            
            // 输入值检查
            myAm.inputCheck(Meisai, pageContext, personId);
            
            // 本人申請且为代理的情况下注册默认显示表中承认者
            if (!myAm.bushitsutentyoChk(pageContext) &&
                ProxyManager.isProxy(pageContext) &&
                CmnFunc2.getConst("C_RESP_MUSS").equalsIgnoreCase(myAm.getLoginResp()) &&
                CmnFunc2.getConst("C_RESP_STAFF_JINJI").equalsIgnoreCase(myAm.getLoginResp())) {
                
                CommonLOVCO appLov = new CommonLOVCO();
                // 部店長・承認者LOV写入
                appLov.setApprovalValue(pageContext, webBean, "TukijimeSupervisor1", "IhrkinmuSyoninld");
            }
            
            // 取得行员编号
            String koinNo = ProxyManager.getEmployeeNumber(pageContext, webBean);
            
            // 事前算出：注册使用的时间单位休假数据（不提交）
            myAm.registJikankyuJoho(personId, koinNo);
            
            // 事前算出（不提交）
            myAm.reviewNitijiJoho(personId, koinNo, pageContext);
            
            // 进行警告检查
            myAm.inputWarnCheck(personId);
            
            // 将输入值转换为确认画面显示用
            myAm.setKinmuJknRev();
            
            // 转换时间单位有给的值以便于显示
            myAm.setJkanTaniKyukaRev();
            
            // 取得事前算出信息
            myAm.initMeisaiReview(personId);
            
            // 进行错误检查
            myAm.raiseInputValidation();
            
            // 转向新规申请确认画面
            pageContext.setForwardURL(
                "OA.jsp?page=/oracle/apps/ihr/kinmu/pages/IHR_KINMU_MEISAI_REVIEW_MAIN",
                REMOVE_MENU_CONTEXT,
                null,
                null,
                true,
                ADD_BREAD_CRUMB_NO,
                OAException.ERROR
            );
        }
        // 处理“表示”按钮提交
        else if ("IhrHyoji".equals(pageContext.getParameter("_FORM_SUBMIT_BUTTON"))) {
            myAm.KinmuInputHojiChk(personId);
            myAm.clearInputRecordVO();
            pageContext.setForwardURL(
                "OA.jsp?page=/oracle/apps/ihr/kinmu/pages/IHR_KINMU_MEISAI_INPUT_MAIN",
                null,
                REMOVE_MENU_CONTEXT,
                null,
                true,
                ADD_BREAD_CRUMB_NO,
                OAException.ERROR
            );
        }
    } catch (Exception e) {
        BundledException.raiseRuntime(this, pageContext, e);
    }
}











/**
 * 页面请求处理方法
 * @param pageContext 页面上下文
 * @param webBean 页面组件
 */
public void processRequestX(OAPageContext pageContext, OAWebBean webBean) {
    try {
        // 获取人员ID并转换为整数
        String personId = ProxyManager.getPersonId(pageContext);
        int person_id = Integer.parseInt(personId);

        // 获取业务模块实例
        KinmuAMImpl myAm = (KinmuAMImpl)pageContext.getApplicationModule(webBean);

        // 获取行员编号
        String koinNo = myAm.getKoinNo(personId);

        // 检查是否为部门负责人
        boolean butentyoCheckFlg = myAm.bushitsutentyoChk(pageContext);

        if (!butentyoCheckFlg) {
            butentyoCheckFlg = myAm.kikakuChk(1);
        }

        // 获取输入VO
        ViewObject inputVO = myAm.getKinmuInputVO();
        KinmuInputVORowImpl inputValue = (KinmuInputVORowImpl)inputVO.first();
        int tgtMonth = Integer.parseInt(inputValue.getTgtMonth());

        // 设置四半期标签
        OAMessageStyledTextBean bean = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive("IhrkinmuListShihanki");
        bean.setLabel(myAm.getTgtShikanki(tgtMonth));

        // 设置海外四半期标签
        OAMessageStyledTextBean bean6 = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive("IhrkinmuShihankikaigai");
        bean6.setLabel(myAm.getTgtShikanki(tgtMonth));

                // 获取警告时间
        String strWarningNo = myAm.getKinmuEnvValue(
            KINMU_ENV_C1_TYOKA,
            KINMU_ENV_C2_TYOKA,
            KINMU_ENV_C3_TYOKA,
            KINMU_ENV_C41_TYOKA
        );

        // 获取上限时间
        String strLimitNo = myAm.getKinmuEnvValue(
            KINMU_ENV_C1_TYOKA,
            KINMU_ENV_C2_TYOKA,
            KINMU_ENV_C3_TYOKA,
            KINMU_ENV_C42_TYOKA
        );

        // 获取上限时间2
        String strLimitNo2 = myAm.getKinmuEnvValue(
            KINMU_ENV_C1_TYOKA,
            KINMU_ENV_C2_TYOKA,
            KINMU_ENV_C3_TYOKA,
            KINMU_ENV_C42_TYOKA
        );

        // 设置警告消息的可变值（警告时间、上限时间）
        MessageToken[] token = new MessageToken[3];
        token[0] = new MessageToken("JIKAN1", strWarningNo);
        token[1] = new MessageToken("JIKAN2", strLimitNo);
        token[2] = new MessageToken("JIKAN3", strLimitNo2);

        // 获取警告消息文本
        String keikokuMsg = pageContext.getMessage("IHR", "IHR_SELF_KINMU_HIDE_041", token);
        OAStaticStyledTextBean msg06 = (OAStaticStyledTextBean)webBean.findChildRecursive("IhrMessage09");
        msg06.setText(keikokuMsg);

                // 转换工作日期
        String kinmuBi = CmnFunc2.convertJboDomainToString(inputValue.getKinmubiInput());
        String tatYM = kinmuBi.substring(0, 6);

        // 获取适用开始日期
        String startDate = myAm.getKinmuEnvValue(
            KINMU_ENV_C1_TYOKA,
            KINMU_ENV_C2_TYOKA,
            KINMU_ENV_C3_TYOKA,
            KINMU_ENV_C43_TYOKA
        );

        // 法定外合计超过基准值时显示警告（劳务管理：80小时遵守对应）
        if (myAm.getKinmuHoteiGaiHantei(person_id, kinmuBi).equals("0") ||
            Integer.parseInt(startDate) > Integer.parseInt(kinmuBi)) {
            msg06.setRendered(false);
        }

                // 人事企划Gr勤务担当者、勤务承认者、人事工作人员的代理申请以外的情况
        if (!(ProxyManager.isProxy(pageContext) && myAm.getKikakuStaffChk())) {
            OAStaticStyledTextBean bean5 = (OAStaticStyledTextBean)webBean.findIndexedChildRecursive("IhrMessage27");
            bean5.setRendered(false);

            OARowLayoutBean bean4 = (OARowLayoutBean)webBean.findIndexedChildRecursive("IhrNest25");
            bean4.setRendered(false);
        }

        // 部门负责人或企划Gr操作时，隐藏承认者输入项目
        if (butentyoCheckFlg || 
            CmnFunc2.getConst("C_RESP_MUSS").equalsIgnoreCase(myAm.getLoginResp()) ||
            CmnFunc2.getConst("C_RESP_STAFF_JINJI").equalsIgnoreCase(myAm.getLoginResp())) {
            
            OAMessageStyledTextBean bean2 = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive("IhrkinmuRevSyonin");
            bean2.setRendered(false);

            OAMessageLovInputBean bean3 = (OAMessageLovInputBean)webBean.findIndexedChildRecursive("IhrkinmuInputBusitu");
            bean3.setRendered(false);
        }

        // 法定外时间和劳务管理相关的显示控制
if (CanFunc2.getConst("C_KEIYAKU_SYAIN_SYOKUSYUB").equalsIgnoreCase(recordRow.getSyokusyu3())) {
    // 契约社员的场合，隐藏限度时间、限度时间剩余、特别延长状况及特别延长次数相关显示
    OAMessageStyledTextBean ihrkinmuListShihankiGendo = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuListShihankiGendo");
    ihrkinmuListShihankiGendo.setRendered(false);

    OASwitcherBean ihrkinmuListShihankiZanSw1 = 
        (OASwitcherBean)webBean.findChildRecursive("IhrkinmuListShihankiZanSw1");
    ihrkinmuListShihankiZanSw1.setRendered(false);

    OAMessageStyledTextBean ihrkinmuListShihankiEntyo = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuListShihankiEntyo");
    ihrkinmuListShihankiEntyo.setRendered(false);

    OAMessageStyledTextBean ihrkinmuListShihankiEntyoCount = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuListShihankiEntyoCount");
    ihrkinmuListShihankiEntyoCount.setRendered(false);

    // 设置四半期表格宽度
    OATableBean ihrkinmuListShihankiTable = 
        (OATableBean)webBean.findChildRecursive("IhrkinmuListShihankiTable");
    ihrkinmuListShihankiTable.setWidth("180");

    // 隐藏年间相关显示项目
    OAMessageStyledTextBean ihrkinmuListNenkanGendo = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuListNenkanGendo");
    ihrkinmuListNenkanGendo.setRendered(false);

    OASwitcherBean ihrkinmuListNenkanZanSw1 = 
        (OASwitcherBean)webBean.findChildRecursive("IhrkinmuListNenkanZanSw1");
    ihrkinmuListNenkanZanSw1.setRendered(false);

    OAMessageStyledTextBean ihrkinmuListNenkanEntyo = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuListNenkanEntyo");
    ihrkinmuListNenkanEntyo.setRendered(false);

    // 设置年间表格宽度
    OATableBean ihrkinmuListNenkanTable = 
        (OATableBean)webBean.findChildRecursive("IhrkinmuListNenkanTable");
    ihrkinmuListNenkanTable.setWidth("150");
}

// 当月可能法定外时间相关的显示控制
OAStackLayoutBean stackLayoutBean1 = 
    (OAStackLayoutBean)webBean.findIndexedChildRecursive("IhrNest26");
stackLayoutBean1.setRendered(false);

// 1个月限度时间相关显示控制
OAMessageStyledTextBean ihrkinmuMonthRuikeiGendo = 
    (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuMonthRuikeiGendo");
ihrkinmuMonthRuikeiGendo.setRendered(false);

OAMessageStyledTextBean ihrkinmuMonthRuikeiZan = 
    (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuMonthRuikeiZan");
ihrkinmuMonthRuikeiZan.setRendered(false);

OAMessageStyledTextBean ihrkinmuMonthRuikeiEntyo = 
    (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuMonthRuikeiEntyo");
ihrkinmuMonthRuikeiEntyo.setRendered(false);

OAMessageStyledTextBean ihrkinmuMonthRuikeiEntyoCount = 
    (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuMonthRuikeiEntyoCount");
ihrkinmuMonthRuikeiEntyoCount.setRendered(false);

// 设置月累计表格的宽度
OATableBean ihrkinmuMonthRuikeiTable01 = 
    (OATableBean)webBean.findChildRecursive("IhrkinmuMonthRuikeiTable01");
ihrkinmuMonthRuikeiTable01.setWidth("150");

// 36協定法改正後の処理
if (myAm.chkSaburokuHoukaisei(tgtYM)) {
    // 法定外平均限度時間をメッセージにセット
    MessageToken[] token1 = new MessageToken[2];
    token1[0] = new MessageToken(
        "JIKAN1", 
        myAm.getKinmuEnvValue("ROMU_KANRI", "SABUROKU", "HOTEIGAI_AVE", "LIMIT_TIME")
    );
    token1[1] = new MessageToken(
        "JIKAN2", 
        myAm.getKinmuEnvValue("ROMU_KANRI", "SABUROKU", "HOTEIGAI_AVE", "LIMIT_TIME")
    );

    // メッセージを取得して設定
    String msg = pageContext.getMessage("IHR", "IHR_SELF_KINMU_LIST_MSG30", token1);
    OAStaticStyledTextBean msg01 = (OAStaticStyledTextBean)webBean.findChildRecursive("IhrkinmuListAveMessage02");
    msg01.setText(msg);

    // 当月可能法定外時間のラベル設定
    OASwitcherBean bean7 = (OASwitcherBean)webBean.findIndexedChildRecursive("IhrkinmukanouHoteigaiJknZanSw1");
    bean7.setLabel(myAm.getTougetuzanLabel(tgtMonth));

    // 勤務実績累計情報(四半期)の非表示
    OARowLayoutBean bottunRowBean3 = (OARowLayoutBean)webBean.findIndexedChildRecursive("IhrNest13");
    bottunRowBean3.setRendered(false);

    // 3ヶ月累計(海外)の非表示
    OAMessageStyledTextBean ihrkinmuShihankikaigai = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuShihankikaigai");
    ihrkinmuShihankikaigai.setRendered(false);
} else {
    // 当月可能法定外時間の非表示
    OAStackLayoutBean stackLayoutBean1 = 
        (OAStackLayoutBean)webBean.findIndexedChildRecursive("IhrNest26");
    stackLayoutBean1.setRendered(false);

    // 法定外時間累計(単月)の非表示
    OARowLayoutBean bottunRowBean3 = 
        (OARowLayoutBean)webBean.findIndexedChildRecursive("IhrNest27");
    bottunRowBean3.setRendered(false);

    // 法定外時間過去平均の非表示
    OAStackLayoutBean stackLayoutBean2 = 
        (OAStackLayoutBean)webBean.findIndexedChildRecursive("IhrNest28");
    stackLayoutBean2.setRendered(false);

    // 1ヶ月累計(海外)の非表示
    OAMessageStyledTextBean ihrkinmuMonthRuikeiKaigai = 
        (OAMessageStyledTextBean)webBean.findChildRecursive("IhrkinmuMonthKaigai");
    ihrkinmuMonthRuikeiKaigai.setRendered(false);
}

// 時間単位有給の申請チェック
KinmuInputJkanTaniKyukaVOImpl jkanTanikyuka = myAm.getKinmuInputJkanTaniKyukaVO();

// 時間単位有給の申請がない場合は欄を非表示
if (jkanTanikyuka.getRowCount() == 0) {
    OARowLayoutBean rowBean01 = 
        (OARowLayoutBean)webBean.findIndexedChildRecursive("IhrkinmuRevSingleNest01");
    rowBean01.setRendered(false);
}

try {
    // ロールバック処理
    myAm.rollback();
} catch (Exception e) {
    e.printStackTrace();
    BundledException.raiseRuntime(this, pageContext, e);
}














/**
 * 表单请求处理方法
 * @param pageContext 页面上下文
 * @param webBean 页面组件
 */
public void processFormRequestX(OAPageContext pageContext, OAWebBean webBean) {
    try {
        // 获取应用模块实例
        KinmuAMImpl myAm = (KinmuAMImpl)pageContext.getApplicationModule(webBean);
        
        // 获取输入VO
        ViewObject inputVO = myAm.getKinmuInputVO();
        KinmuInputVORowImpl inputValue = (KinmuInputVORowImpl)inputVO.first();

        // 处理"下一步"按钮
        if (pageContext.getParameter("IhrCommit") != null) {
            // 获取记录行
            KinmuInputRecordVORowImpl recordRow = 
                (KinmuInputRecordVORowImpl)myAm.getKinmuInputRecordVO().first();

            // 特别延长申请处理
            if (CanFunc.nvl(inputValue.getWarnFlag()) && 
                CanFunc.getConst("C_KEIYAKU_SYAIN_STATUS")
                       .equalsIgnoreCase(recordRow.getSyokusyu())) {
                
                // 获取人员ID
                String personId = ProxyManager.getPersonId(pageContext);

                // 输入检查
                myAm.inputCheck(Entyo, pageContext, personId);

                // 本人申请时，如果不是部门负责人且是代理操作，需要注册承认者
                if (!myAm.bushitsuTentyoChk(pageContext) && 
                    ProxyManager.isProxy(pageContext) &&
                    CmnFunc2.getConst("C_RESP_MUSS").equalsIgnoreCase(myAm.getLoginResp()) &&
                    CmnFunc2.getConst("C_RESP_STAFF_JINJI").equalsIgnoreCase(myAm.getLoginResp())) {

                    // 创建承认者LOV对象
                    CommonLOVCO appLov = new CommonLOVCO();

                    // 设置部门负责人和承认者LOV
                    appLov.setApprovalValue(
                        pageContext, 
                        webBean, 
                        "EntyoDivisionChief1", 
                        "IhrkinmuBusituId"
                    );

                    // 设置键值
                    SetKey(pageContext);

                    // 获取行员编号
                    String koinNo = ProxyManager.getEmployeeNumber(pageContext, webBean);

                    // 设置特别延长申请工作表数据
                    myAm.setEntyoWfrkData(wfKey, personId, false, pageContext);

                    // 如果不是企划组操作，启动特别延长申请工作流
                    if (!myAm.kikakuChk()) {
                        myAm.startEntyoWorkFlowProcess(wfKey, pageContext, personId, false);
                    }
                }

                // 获取人员ID和行员编号
                String personId = ProxyManager.getPersonId(pageContext);
                String koinNo = ProxyManager.getEmployeeNumber(pageContext, webBean);

                // 注册时间单位休假数据
                myAm.registJikankyuJoho(personId, koinNo);
                myAm.registNitijiJoho(personId, koinNo, pageContext, Meisai);

                // 转向完成页面
                pageContext.setForwardURL(
                    "OA.jsp?page=/oracle/apps/ihr/kinmu/pages/IHR_KINMU_MEISAI_COMP_MAIN",
                    null,
                    REMOVE_MENU_CONTEXT,
                    null,
                    true,
                    ADD_BREAD_CRUMB_NO,
                    OAException.ERROR
                );
            }
        }

        // 处理返回按钮
        if (pageContext.getParameter("IhrReturn") != null) {
            myAm.clearWarn();
            pageContext.setForwardURL(
                "OA.jsp?page=/oracle/apps/ihr/kinmu/pages/IHR_KINMU_MEISAI_INPUT_MAIN",
                null,
                REMOVE_MENU_CONTEXT,
                null,
                true,
                ADD_BREAD_CRUMB_NO,
                OAException.ERROR
            );
        }

    } catch (Exception e) {
        BundledException.raiseRuntime(this, pageContext, e);
    }
}











SELECT /* KinmuJosiListVO */
    NULL AS SENTAKU,
    NULL AS SYOKAI,
    flv.ATTRIBUTES AS SINSEI_SYUBETU_NAME,
    iknj.KOIN_NO,
    NVL(papf.ATTRIBUTE15, papf.PER_INFORMATION18) AS ATTRIBUTE15,
    NVL(papf.ATTRIBUTE16, papf.PER_INFORMATION19) AS ATTRIBUTE16,
    TO_CHAR(TO_NUMBER(TO_CHAR(ikni.KINMUBI)), 
            TO_CHAR(TO_NUMBER(TO_CHAR(iknj.KINMUBI, 'DD')))) AS HIDUKE,
    IHR_KINMU_GET_SYUGYO_KBN_NAME(iknj.SYUGYO_KBN) AS SYUGYO_KBN_NAME,
    IHR_KINMU_GET_KINMU_PATTERN_RYAKUSYO(ikni.KINMU_PATTERN_CODE) AS KINMU_PATTERN_RYAKUSHO,
    IHR_KINMU_GET_JIKOKU(
        ikni.KIMMU_BGN_HH,
        iknj.KIMMU_BGN_MI,
        iknj.KINMU_END_HH,
        iknj.KINMU_END_MI
    ) AS KINMU_JKN,
    IHR_KINMU.DISP_ON_VIEW AS KINMU_DISP,
    NULL AS SPACE_1,
    IHR_WF_KINMU.DISP_ON_VIEW(
        ikni.HYOJI_KBN,
        ikni.IKKATU_FLG,
        ikni.KANRI_KBN,
        ikni.SAIRYO_FLAG,
        IHR_KINMU_COMMON_CONST.GET_CONST_CHAR2('C_KOMOKU_KBN_SAI_KK_HIHYOJI'),
        ikni
    ) AS HOTENAI,
    HOTENAI_SYOTEGAI,
    ikni.HYOJI_KBN,
    ikni.IKKATU_FLG,
    ikni.KANRI_KBN,
    iknj.SAIRYO_FLAG

    SELECT /* KinmuJosiListVO */ 
    -- 基本信息栏位
    NULL AS SENTAKU,
    NULL AS SYOKAI,
    flv.ATTRIBUTES AS SINSEI_SYUBETU_NAME,
    iknj.KOIN_NO,
    NVL(papf.ATTRIBUTE15, papf.PER_INFORMATION18) AS ATTRIBUTE15,
    NVL(papf.ATTRIBUTE16, papf.PER_INFORMATION19) AS ATTRIBUTE16,
    
    -- 日付関連
    TO_CHAR(
        TO_NUMBER(TO_CHAR(ikni.KINMUBI)), 
        TO_CHAR(TO_NUMBER(TO_CHAR(iknj.KINMUBI, 'DD')))
    ) AS HIDUKE,
    
    -- 勤務情報
    IHR_KINMU_GET_SYUGYO_KBN_NAME(iknj.SYUGYO_KBN) AS SYUGYO_KBN_NAME,
    IHR_KINMU_GET_KINMU_PATTERN_RYAKUSYO(ikni.KINMU_PATTERN_CODE) AS KINMU_PATTERN_RYAKUSHO,
    IHR_KINMU_GET_JIKOKU(
        ikni.KIMMU_BGN_HH,
        iknj.KIMMU_BGN_MI,
        iknj.KINMU_END_HH,
        iknj.KINMU_END_MI
    ) AS KINMU_JKN,
    
    -- 表示制御
    IHR_KINMU.DISP_ON_VIEW AS KINMU_DISP,
    NULL AS SPACE_1,
    
    -- 法定外時間関連
    IHR_WF_KINMU.DISP_ON_VIEW(
        ikni.HYOJI_KBN,
        ikni.IKKATU_FLG,
        ikni.KANRI_KBN,
        ikni.SAIRYO_FLAG,
        IHR_KINMU_COMMON_CONST.GET_CONST_CHAR2('C_KOMOKU_KBN_SAI_KK_HIHYOJI'),
        ikni
    ) AS HOTENAI,
    HOTENAI_SYOTEGAI,
    
    -- 表示制御フラグ
    ikni.HYOJI_KBN,
    ikni.IKKATU_FLG,
    ikni.KANRI_KBN,
    iknj.SAIRYO_FLAG,
    
    -- 集計関連
    ikes.RUIKEI_3MONTHS AS RUIKEI_3MONTHS,
    ikas.RUIKEI_YEAR AS RUIKEI_YEAR,
    
    -- 四半期・年間関連
    NULL AS SHIHANKI_GENDO_ZAN,
    NULL AS SHIHANKI_GENDO_ZAN_HYOJI_FLG,
    NULL AS SHIHANKI_ENTYO,
    NULL AS SHIHANKI_ENTYO_COUNT,
    NULL AS NENKAN_GENDO_ZAN,
    NULL AS NENKAN_GENDO_ZAN_HYOJI_FLG,
    NULL AS NENKAN_ENTYO

    SELECT 
    -- 就業情報
    iknj.HYOJI_KBN,
    iknj.IKKATU_FLG,
    iknj.KANRI_KBN,
    iknj.SAIRYO_FLAG,

    -- 共通定数取得
    IHR_KINMU_COMMON_CONST.GET_CONST_CHAR2('C_KOMOKU_KBN_SAI_KK_HIHYOJI') AS CONST_VALUE,
    
    -- 法定外・休日・深夜関連
    IHR_WF_KINMU.DISP_ON_VIEW_GOKEI_SINYA(
        iknj.HYOJI_KBN,
        iknj.HOTEGAI_GOKE
    ) AS HOTEGAI_GOKE1,
    
    -- 累計情報
    ikes.RUIKEI_3MONTHS AS RUIKEI_3MONTHS,
    ikas.RUIKEI_YEAR AS RUIKEI_YEAR,
    
    -- 限度・延長情報
    NULL AS SHIHANKI_GENDO_ZAN,
    NULL AS SHIHANKI_GENDO_ZAN_HYOJI_FLG,
    NULL AS SHIHANKI_ENTYO,
    NULL AS SHIHANKI_ENTYO_COUNT,
    NULL AS NENKAN_GENDO_ZAN,
    NULL AS NENKAN_GENDO_ZAN_HYOJI_FLG,
    NULL AS NENKAN_ENTYO,

    -- 超過判定
    CASE 
        WHEN iknj.KINMUBI > TO_DATE(
            IHR_KINMU_COMMON.GET_KINMU_ENV_VALUE('TEKIYO_KAISI_DATE'), 
            'YYYYMMDD'
        ) THEN
            CASE
                WHEN IHR_WF_KINMU.GET_HOTEGAI_HANTE12(
                    iknj.PERSON_ID,
                    iknj.KINMUBI
                ) = '1' THEN '超過'
                ELSE NULL
            END
        ELSE NULL 
    END AS JYOGEN,
    
    -- その他表示項目
    NULL AS SPACE_2,
    IHR_WF_KINMU.DISP_ON_VIEW_GOKEI_SINYA(
        iknj.HYOJI_KBN,
        iknj.SINYA
    ) AS SINYA,
    flv.ATTRIBUTE2 AS STATUS_NAME

    -- 时间相关字段计算
SELECT 
    -- 基本状态与标识
    NULL AS JOSI_COMMENT,
    iknj.PERSON_ID,
    ikni.KINMU_SYONIN_JOTAI AS STATUS,
    1 AS SINSEI_SYUBETU,
    
    -- 日期格式化
    TO_CHAR(iknj.KINMUBI, 'DD') AS HIDUKE_SORT,
    iknj.KINMUBI,
    TO_CHAR(iknj.KINMUBI, 'YYYYMM') AS TAISYO_YM,
    
    -- 代理标志处理
    CASE 
        WHEN iknj.KOIN_NO = iknj.SINSEISYA_KOIN_NO THEN 'N'
        ELSE 'Y'
    END AS DAIRI_FLG,
    
    -- 申请人信息
    iknj.SINSEISYA_KOIN_NO,
    iknj.SINSEISYA_NAME,
    
    -- 明细相关信息
    ikni.MEISAI_KBN,
    ikni.KANRI_KBN,
    NULL AS TOKUTEI_MARK,
    ikni.TOKUBETU_KINMU_CODE,
    iknj.SYUGYO_KBN,
    
    -- 状态序列信息
    NULL AS SEQUENCE,
    NULL AS JOKYO_STATUS,
    NULL AS TESE_JOKYO_SEQ,
    NULL AS bef_aft_seq,
    NULL AS err_msg_code,
    ikni.SAIRYO_FLAG AS SAIRYO_FLAG
    -- 劳务时间计算部分
SELECT
    -- 劳务工时计算
    CASE 
        WHEN IHR_KINMU_COMMON.GET_JYUGYOSYA_KBN_NAME(
            pptuf.PERSON_TYPE_ID, 
            paaf.GRADE_ID
        ) = 'D' THEN
            CASE 
                WHEN NVL(iknj.roudo_jikansu, 0) > 0 THEN 
                    IHR_WF_KINMU.GET_JIKOKU(NVL(iknj.roudo_jikansu, 0))
                ELSE NULL 
            END
    END AS roudo_jikansu,

    -- 特殊工时计算
    CASE 
        WHEN paaf.GRADE_ID = IHR_KEIYAKU_COMMON.GET_GRADE_ID_KEIYAKU() THEN
            CASE 
                WHEN NVL(iknj.teller_jikansu, 0) > 0 THEN
                    IHR_WF_KINMU.GET_JIKOKU(NVL(iknj.teller_jikansu, 0))
                ELSE NULL
            END
    END AS teller_jikansu,

    -- 通勤费计算
    CASE 
        WHEN paaf.GRADE_ID = IHR_KEIYAKU_COMMON.GET_GRADE_ID_KEIYAKU() THEN
            CASE 
                WHEN NVL(iknj.tukin_hi, 0) > 0 THEN
                    TO_CHAR(NVL(iknj.tukin_hi, 0), '999.999')
                ELSE NULL
            END
    END AS tukin_hi,

    -- 备注说明
    iknj.DESCRIPTION,

    -- 登录时间计算
    IHR_NETPC.GET_JIKOKU(
        TO_NUMBER(TO_CHAR(ill.LOGON_DATE_TIME, 'HH24')),
        TO_NUMBER(TO_CHAR(ill.LOGON_DATE_TIME, 'MI'))
    ) AS LOGON_JKN,

    -- 登出时间计算
    CASE
        WHEN TO_CHAR(ill.LOGOFF_DATE_TIME, 'YYYYMMDD') > ill.KIJUN_BI THEN NULL
        ELSE IHR_NETPC.GET_JIKOKU(
            TO_NUMBER(TO_CHAR(ill.LOGOFF_DATE_TIME, 'HH24')),
            TO_NUMBER(TO_CHAR(ill.LOGOFF_DATE_TIME, 'MI'))
        )
    END AS LOGOFF_JKN,

    -- 各类工时调整计算
    IHR_WF_KINMU.GET_JIKOKU(ikni.TYUSYOKU_KOJO_JIKANSU) AS TYUSYOKU_KOJO_JIKANSU,
    IHR_WF_KINMU.GET_JIKOKU(ikni.TUJO_KOJO_JIKANSU) AS TUJO_KOJO_JIKANSU,
    IHR_WF_KINMU.GET_JIKOKU(ikni.SINYA_KOJO_JIKANSU) AS SINYA_KOJO_JIKANSU,
    DECODE(iknj.TYUSYOKU_HOJO_FLG, '1', '0', NULL) AS tyusyoku_hojo,
    
    -- 工作比率
    mufe.kinmu_wariai AS kinmu_wariai_input_flag
FROM
    IHR_KINMU_NITIJI_JOHO iknj
    LEFT JOIN IHR_KINMU_NITIJI_INPUT ikni ON iknj.KINMUBI = ikni.KINMUBI
    LEFT JOIN FND_LOOKUP_VALUES flv ON ikni.SINSEI_SYUBETU = flv.LOOKUP_CODE
    LEFT JOIN PER_ALL_PEOPLE_F papf ON iknj.PERSON_ID = papf.PERSON_ID
    LEFT JOIN IHR_KINMU_ENTYO_SYUKEIJKOHO ikes ON iknj.PERSON_ID = ikes.PERSON_ID
    LEFT JOIN IHR_KINMU_KAIGAI_SYUKEIJKOHO ikas ON iknj.PERSON_ID = ikas.PERSON_ID
WHERE
    iknj.PERSON_ID = :1
    AND iknj.KINMUBI BETWEEN :2 AND :3
    AND flv.LOOKUP_TYPE(+) = 'IHR_KINMU_SINSEI_SYUBETU'
    AND flv.LANGUAGE = USERENV('LANG')
ORDER BY
    iknj.KINMUBI DESC;




    if(10mnFunc2.getConst("C_KEIYAKU_SYAIN_SYOKUSYU3").equalsIgnoreCase(recordRow.get Syokusyu3()))|