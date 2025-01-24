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




CREATE OR REPLACE FUNCTION GET_HOTEGAI_HANTE120(
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

    SELECT SUM(kg.HOTEGAI_GOKE), SUM(NVL(ikkj.HOTEGAI_GOKE_KAIGAI, 0)) INTO in_hotegai_goke
    FROM IHR_KINMU_GETUJI_JOHO kg
    LEFT JOIN THR_KHS_KINMU_JOHO ikkj
    ON kg.PERSON_ID = ikkj.PERSON_ID
    AND kg.TAISYO_YM = ikkj.TAISYO_YM
    AND kg.SAIRYO_FLAG = ikkj.SAIRYO_FLAG
    AND kg.KANRI_KBN = ikkj.KANRI_KBN
    AND ikkj.SYUKET_KBN = 1
    AND ikkj.ENABLED_FLAG = 'Y'
    WHERE kg.PERSON_ID = in_person_id
    AND kg.TAISYO_YM = iv_kinmubi
    GROUP BY kg.PERSON_ID, kg.TAISYO_YM, kg.KANRI_KBN, kg.SAIRYO_FLAG;

    IF in_hotegai_goke > TO_NUMBER(THR_KINMU_COMMON.GET_KINMU_ENV_VALUE('KINMU_TYOKA', 'HOTEGAI_GOKE', 'TOGETU', 'TYOKA_WARNING_TIME')) * 60 THEN
        ov_result := '1';
    END IF;

    RETURN ov_result;
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END GET_HOTEGAI_HANTE120;