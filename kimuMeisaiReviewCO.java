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

            // 他の処理...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 其他方法...

}