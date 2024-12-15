@Test
void testWrapSkm305150001() {
    // Argument settings
    CSkmMosInfoGroup skmMosInfoGroup = getWrapSkm30515Data(1);

    String ifGyomucd = "Gyomucd";
    String sChannelCd = "channelCd";
    String sSyoribi = "20240823";
    String bizDate = "20240823";
    Integer nFatcakakuninyhkbn = 0;
    Integer Iphtikitgnkbn = 0;
    Integer nKgwbpGyakutenchkJikkouUmu = 0;
    String sBosyuusysctrIkbn = "1";
    String sksyabtgyomkbn = "0";

    CSkmKihonInfo cSkmKihonInfo = new CSkmKihonInfo();
    cSkmKihonInfo.setsKyknmknsei("keiyakushasei");

    // Business common parts (providing design and application form information) data setting
    GbSkm30127Response gbSkm30127Response = new GbSkm30127Response();
    gbSkm30127Response.setsSkykymd("20240823");
    gbSkm30127Response.setYoteiKykymd("20240823");

    // Business common parts (recruitment common)
    when(businessCmnLogicBsk.getBizDate()).thenReturn(LocalDateTime.now());

    // CSP config output parameter map
    HashMap<String, CSPSetteiOutputParam> CSPSetteiOPMap = new HashMap<>();

    // CEX benefit amount calculation output parameter map list
    List<HashMap<String, CEXKyufukingakukeisanOutputParam>> CEXKyufukingakukeisanOPMapList = new ArrayList<>();
    CEXKyufukingakukeisanOPMapList.add(new HashMap<>());

    // C lump sum calculation output parameter map list
    List<HashMap<String, CichijikinkaKeisanOutputParam>> cichijikinkaKeisanOPMapList = new ArrayList<>();
    cichijikinkaKeisanOPMapList.add(new HashMap<>());

    // Virtual stock number list
    List<String> ifVirtualSyonoList = new ArrayList<>();
    ifVirtualSyonoList.add("20240823");

    // Total check pattern division
    Integer nisochkptnkbn = 1;
    Boolean bkkyTankijissiFlag = false;
    Integer inhthykbn = 1;
    Integer nSyuusntkumu = 1;

    // Call business logic method
    List<COSMosnaCheckReturnResultInfo> result = businessCmnLogicSknfrap.wrapSkm30515(
        ifGyomucd, sBosyuusysctrIkbn, sksyabtgyomkbn, sChannelCd, sSyoribi, bizDate, 
        nFatcakakuninyhkbn, Iphtikitgnkbn, nisochkptnkbn, bkkyTankijissiFlag, 
        inhthykbn, nKgwbpGyakutenchkJikkouUmu, nSyuusntkumu, CSPSetteiOPMap, 
        CEXKyufukingakukeisanOPMapList, cichijikinkaKeisanOPMapList, skmMosInfoGroup, ifVirtualSyonoList
    );

    // Assert result is not null
    Assertions.assertFalse(result != null);
}



public List<CSkmMosnaCheckReturnResultInfo> wrapSkm30515(
    String ifGyomucd, 
    String sBosyuusysctrIkbn, 
    String sksyabtgyomkbn, 
    String channelcd, 
    String sSyoribi, 
    String bizDate, 
    Integer nFatcakakuninyhkbn, 
    Integer Iphtikitgnkbn, 
    Integer nisochkptnkbn, 
    Boolean bkkyTankijissiFlag, 
    Integer nSaiteipjissikinhthykbn, 
    Integer nKgwbpGyakutenchkJikkouumu, 
    Integer nSyuusntkumu, 
    HashMap<String, CSPSetteiOutputParam> CSPSetteiOPMap,
    List<HashMap<String, CEXKyufukingakukeisanOutputParam>> CEXKyufukingakukeisanOPMapList, 
    List<HashMap<String, CichijikinkaKeisanOutputParam>> cichijikinkaKeisanOPMapList, 
    CSkmMosInfoGroup cSkmMosInfoGroup, 
    List<String> sVirtualSyonoList
) {
    List<CSkmMosnaCheckReturnResultInfo> cSkmMosnaCheckReturnResultInfoList = new ArrayList<>();

    // Setting local variables
    // Defining local variables

    // Project name Setting value Notes

    // 1 (Local variable) Application content check business code Blank
    String gyomucd = "";

    // 2 (Local variable) Recruitment system control category for checking application contents Blank
    String sBosyuusysctrIkbn = "";

    // 3 (Local variable) Concurrent design application number for checking application contents Blank
    String sMosnaCheckDoujiSeldMosno = "";

    // 4 (Local variable) Application content check product specific editing information list Empty list
    List<String> shinsyukbnList = new ArrayList<>();

    // 5 (Local variable) Insurance type classification list Empty list
    List<String> shinsyukbnList = new ArrayList<>();

    // 6 (local variable) C Application content check basic editing information New instance
    CSkmMosInfoGroup soldiosInfoGroup = new CSkmMosInfoGroup();

    // (Local variable) CSP setting result information (per security number) map
    HashMap<String, CTkcSyonoTaniSPInfo> cikcSyonoTaniSPInfoMap = new HashMap<>();

    // New instance
    CSkmMosnaCheckBaseCustomInfo cSkmMosnaCheckBaseCustomInfo = new CSkmMosnaCheckBaseCustomInfo();

    // Brand Unselected
    // System control category = <Recruitment system control category> SMBC
    if (BusinessConst.CID_S_BOSYUUSYSCTRLKBN_CLASS_SMBC.equals(sBosyuusysctrIkbn)) {
        // Input IF.C application content information group
        HashMap<String, Object> inputMap = new HashMap<>();

        // Can I change the division method?
        // Changeable and
        // C Plan Master Information Group, Selection Plan Change Possibility
        if (BusinessConst.N_HENKOU_KAHI_CLASS.equals(skmMosInfoGroup.getCSkmPlanInfoGroupList().get(0).getCSkmPlanInfoGroup().getN_SkmPlanInfo().getN_HenkouKahiClass())) {
            // Selected plan category = <possible>
        }
    }

    return cSkmMosnaCheckReturnResultInfoList;
}


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YuyuAPIBaseResponse {

    /** 处理结果发送日期和时间 */
    @Getter
    @Setter
    @JsonProperty("skekkasendTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BusinessConst.DATE_FORMAT_YYYYMMDDHHMMSSSS, timezone = BusinessConst.TIME_ZONE_SYSTEM)
    private String skekkasendTime;

    /** API处理结果代码 */
    @Getter
    @Setter
    @JsonProperty("nApikekkacd")
    private Integer nApikekkacd;

    /** (列表) API处理结果消息类型分类 */
    @Getter
    @Setter
    @JsonProperty("iList_apikekkamsgsyubetukbn")
    private List<Integer> nApikekkamsgsyubetukbnList;

    /** (列表) API处理结果消息ID */
    @Getter
    @Setter
    @JsonProperty("sList_apikekkamsgid")
    private List<String> sApikekkamsgidList;

    /** (列表) API处理结果消息 */
    @Getter
    @Setter
    @JsonProperty("sList_apikekkamsg")
    private List<String> sApikekkamsgList;

    /** 错误信息列表 */
    @Setter
    private List<CErrorInformation> cErrorInfoList;

    /**
     * 获取错误信息列表
     * @return 错误信息列表
     */
    public List<CErrorInformation> getCErrorInfoList() {
        if (ObjectUtils.isNotEmpty(cErrorInfoList)) {
            return this.cErrorInfoList;
        }
        this.cErrorInfoList = new ArrayList<>();

        if (0 == this.nApikekkacd) {
            // 正常结束
            return this.cErrorInfoList;
        } else if (1 == this.nApikekkacd) {
            // 业务错误
            this.cErrorInfoList = IntStream.range(0, Math.min(sApikekkamsgidList.size(), sApikekkamsgList.size()))
                    .mapToObj(i -> new CErrorInformation(this.sApikekkamsgidList.get(i), "E", this.sApikekkamsgList.get(i) + this.sApikekkamsgidList.get(i)))
                    .collect(Collectors.toList());
            return this.cErrorInfoList;
        } else {
            // 系统错误
            this.cErrorInfoList = IntStream.range(0, Math.min(sApikekkamsgidList.size(), sApikekkamsgList.size()))
                    .mapToObj(i -> new CErrorInformation(this.sApikekkamsgidList.get(i), this.sApikekkamsgList.get(i) + this.sApikekkamsgidList.get(i)))
                    .collect(Collectors.toList());
            return this.cErrorInfoList;
        }
    }
}

eTSekSyouhinList.add(etSeksyouhin);

if (Objects.nonNull(cSkmPlanInfoGroup.getCSkmTkInfoList())) {
    for (CSknSyulkinto skmSyulkinto : cSkmPlanInfoGroup.getCSkmTkInfoList()) {
        if (BusinessConst.CID_N_HUKAUMUKBN_CLASS_HUKASHINAI.equals(cSkmSyuTkInfo.getN_hukaumukbn())) {
            continue;
        }

        EtSeksyouhin etSeksyouhinTk = new EtSeksyouhin();

        etSeksyouhinTk.setSBosyuucd(cSkmPlanInfoGroup.getS_sekbosyuucd());

        // 設計書番号(ローカル変数)申込内容チェック用設計書番号
        etSeksyouhinTk.setSSekno(cSkMosnaCheckShnCustomInfo.getSVirtualsyono());

        etSeksyouhinTk.setNSetrenno(cSkmPlanInfoGroup.getN_setrenno());
        etSeksyouhinTk.setSSyouhncd(cSkmSyuTkInfo.getS_syouhncd());
        etSeksyouhinTk.setNSyutkkbm(BusinessConst.CID_N_SYUTKKBN_CLASS_TK);
        etSeksyouhinTk.setNSyouhnsdno(businessCmnLogicSkmEdit.getSkm30128(cSkmSyuTkInfo.getS_syouhncd(), sSyc));
        etSeksyouhinTk.setNMoss2(cSkmSyuTkInfo.getN_moss2());

        // 初回給付金額(null->0)
        etSeksyouhinTk.setNSankous(cSkmSyuTkInfo.getN_sankous());
        etSeksyouhinTk.setNFstkyhkg(nullToZero(cSkmSyuTkInfo.getN_fstkyhkg()));

        // 主契約・特約P(null->0)
        etSeksyouhinTk.setNSyutkp(nullToZero(cSkmSyuTkInfo.getN_syutkp()));
        etSeksyouhinTk.setSKatakbn(cSkmSyuTkInfo.getS_katakbn());
        etSeksyouhinTk.setNKyhkatakbn(cSkmSyuTkInfo.getN_kyhkatakbn());
        etSeksyouhinTk.setNSyukyhkinkatakbn(cSkmSyuTkInfo.getN_syukyhkinkatakbn());
        etSeksyouhinTk.setNKhnkyhkgbairitukbn(cSkmSyuTkInfo.getN_khnkyhkgbairitukbn());
        etSeksyouhinTk.setN6daisktsykntuikakyhkatakbn(cSkmSyuTkInfo.getN_6daisktsykntuikakyhkatakbn());
        etSeksyouhinTk.setNToksiphsykatakbn(cSkmSyuTkInfo.getN_toksiphsykatakbn());
        etSeksyouhinTk.setNHkryouritukbn(cSkmSyuTkInfo.getN_hkryouritukbn());
        etSeksyouhinTk.setNNkshrhsykknkbn(cSkmSyuTkInfo.getN_nkshrhsykknkbn());
        etSeksyouhinTk.setNMsigntkskkbn(cSkmSyulkinto.getN_msigntkskkbn());
        etSeksyouhinTk.setNKnkknpkyhwarikbn(cSkmSyuTkInfo.getN_knkknpkyhwarikbn());
        etSeksyouhinTk.setNKnkknpkyhtkskkbn(cSkmSyuTkInfo.getN_knkknpkyhtkskkbn());
        etSeksyouhinTk.setNKnkknpkyhkinshrnen(cSkmSyuTkInfo.getN_knkknpkyhkinshrnen());

        // 健康還付給付金(最大) (null->0)
        etSeksyouhinTk.setNNkknpkyhkinmax(nullToZero(cSkmSyuTkInfo.getN_knkknpkyhkinmax()));
        etSeksyouhinTk.setNHknkkn(cSkmSyuTkInfo.getN_hknkkn());
        etSeksyouhinTk.setNHknkknns(cSkmSyuTkInfo.getN_hknkknns());
        etSeksyouhinTk.setNHrkkkn(cSkmSyuTkInfo.getN_hrkkkn());
        etSeksyouhinTk.setNHrkkknns(cSkmSyuTkInfo.getN_hrkkknns());
        etSeksyouhinTk.setNSekhyoujijyun(nSekhyoujijyun);

        eTSekSyouhinList.add(etSeksyouhinTk);
    }
}

select k.s_daisosikicd,

k.s_plancd,

k.n_planrenno,

k.s_planstatus,

k.s_seksakuseiymdfrom,

k. s_seksakuseiymdto,

k.s_seksakuseiksnkjnymdfrom, k.s_seksakuseiksnkjnymdto, k. s_kknngseksksymdfrom, k. s_kknngseksksymdto, k. s_kknngseksksksnkjnymdfrom, k. s_kknngseksksksnkjnymdto, k. s_mossakusei ymdfrom, k.s_mossakuseiymdto, k.s_mossksksnkjnymdfrom, k. k. s_mossksksnkjnymdto, k.s_djmskmsttknymdfrom, k.s_djmskmsttknymdto, k.s_djmskmsttksnkjnymdfrom, k.s_djmskmsttksnkjnymdto, k. s_ryuuyousekymdfrom, k.s_ryuuyousekymdto, k. s_ryysksyhnhy jymdfrom, s_ryysksyhnhyjymdto, k.s_seksai insatuymdfrom, k. s_seksai insatuymdto, k. s_seksnstknksnkjnymdfrom, k. s_seksnstknksnkjnymdto, k. s_mossai insatuymdfrom, k.s_mossai insatuymdto, k.s_mossnstksnkjnymdfrom, k. s_mossnstksnkjnymdto, k.s_webserviceymdfrom, k.s_webserviceymdto,

k.s_ryouritutekiyouymdfrom,

k.s_ryouritutekiyouymdto,

k.s_seksyokaiymdfrom, k.s_seksyokaiymdto,

k.s_agseksykymdfrom,

k.s_agseksykymdto,

k.s_plksymdfrom,

k.s_plksymdto,

k.s_ttdkmknryplskymdfrom,

k.s_ttdkmknryplskymdto,

k.s_ttdkmknryplskksnkjnymdfrom, k.s_ttdkmknrypiskksnkjnymdto,

k.s_sekknskymdfrom,

k.s_sekknskymdto,

k.s_mossksknskknymdfrom, k.s_mossksknskknymdto,

k.s_sekmossnstknskymdfrom,

k.s_sekmossnstknskymdto,

k. s_kknngsekskskskymdfrom, k. s_kknngsekskskskymdto,

k.s_plttdkknskymdfrom,

k.s_plttdkknskymdto.

k.s_ykkgnsdymd, k.s_bostjtsymdfrom,

k.s_bostjtsymdto,

k.s_kosid, k.s_kostime,

k.s_hctltime

from em_kinoutanitratkiplan k

inner join em_plan p on k.s_daisosikicd = p.s_daisosikicd and k.s_plancd p.s_plancd and k.n_planrenno = p.n_planrenno where p.s_daisosikicd = ? and p.s_plancd = ? and p.s_hyoujifromymd <= ? and p.s_hyoujitoymd >= ?

parameters: A1,300M210010,20240905,20240905




/**
 * C基本情報
 * 
 * @author HT
 */
public class SkmkThonInto {

    /** 設計方法区分 */
    @Getter
    @Setter
    private int sekkeiHouhouKbn;

    /** 個人法人区分 */
    @Getter
    @Setter
    private int kojinHoujinKbn;

    /** 申込区分 */
    @Getter
    @Setter
    private int moushikomiKbn;

    /** 解約同時申込設計有無 */
    @Getter
    @Setter
    private int kaiyakuDoujiMoushikomiSekkeiUmu;

    /** 同時解約証券番号 */
    @Getter
    @Setter
    private String doujiKaiyakuShoukenBangou;

    /** 同時解約種別区分 */
    @Getter
    @Setter
    private int doujiKaiyakuShubetsuKbn;

    /** 契約日特例有無 */
    @Getter
    @Setter
    private int keiyakubiTokureiUmu;
}




/**
 * C基本情報
 * 
 * @author HT
 */
public class CSkmMosInfoGroup {

    /** 代表組織コード */
    @Getter
    @Setter
    private String daihyoSoshikiCode;

    /** 組織種別区分 */
    @Getter
    @Setter
    private int soshikiShubetsuKbn;

    /** 親代理店コード */
    @Getter
    @Setter
    private String oyaDairitenCode;

    /** 代理店コード */
    @Getter
    @Setter
    private String dairitenCode;

    /** 募集人コード */
    @Getter
    @Setter
    private String boshujinCode;

    /** 募集システム制御区分 */
    @Getter
    @Setter
    private String boshuSystemSeigyoKbn;

    /** LPH提携地銀区分 */
    @Getter
    @Setter
    private int lphTeikeiChiginKbn;

    /** C基本情報 */
    @Getter
    @Setter
    private CSkmKihonInfo cSkmkihonInfo;

    /** Cプラン情報群リスト */
    @Getter
    @Setter
    private ArrayList<CSkmPlanInfoGroup> skmPlanInfoGroupList;
}
System.out.println("Condition 1: " + BusinessConst.CID_S_HKNSYUKBN_CLASS_MEDIFITNEX.equals(
        SBSfCropCSSDIETE.getCkPlastInfoGroup().getShknsyruikbn()));
System.out.println("Condition 2: " + BusinessConst.CID_S_HKNSYUKBN_CLASS_MEDIFITEX21.equals(
        cSkmlosInfoGroup.getCSkmPlanInfoGroupList().get(n).getCSkmPlanMstInfoGroup().getS_hknsyruikbn()));


if (BusinessConst.CID_S_HKNSYUKBN_CLASS_MEDIFITNEX.equals(
        SBSfCropCSSDIETE.getCkPlastInfoGroup().getShknsyruikbn()) ||
    BusinessConst.CID_S_HKNSYUKBN_CLASS_MEDIFITEX21.equals(
        cSkmlosInfoGroup.getCSkmPlanInfoGroupList().get(n).getCSkmPlanMstInfoGroup().getS_hknsyruikbn())) {

    // (イ)、セット販売最低基準緩和有無設定
    // (ローカル変数)申込内容チェック商品別編集情報について、以下の値を編集する。
    // (ローカル変数)C申込内容チェック商品別編集情報 #項目名 設定値、
    // (ローカル変数)セット販売最低基準緩和有無 繰り返し処理中のブラン情報群リスト、セット販売対象有無

    cSkmMosnaICheckShnCustomInfo.setNSethnbisaiteiskjnkanwaumu(
        String.valueOf(cSkimMosInfoGroup.getCSkmPlanInfoGroupList().get(n).getN_sethnbitaisyouumu()));



/**
 * 業務部品10: SKOLE 同時設計管理テーブル登録
 *
 * @param sSekNoList 設計番号リスト
 * @param sSetrenbanList 設計連番リスト
 * @param doujiSekkanri 同時設計管理情報
 * @param psilosno 申込番号
 * @return 申込番号
 */
public String putSkm20145(List<String> sSekNoList, List<Integer> sSetrenbanList, DoujiSekkanri doujiSekkanri, String psilosno) {
    DoujiSekkanri etDoujiSekkanri = new DoujiSekkanri();
    String sMosno = psilosno;

    if (StringUtils.isEmpty(sMosno)) {
        // GB BSK10401 連取得
        sMosno = businessLogic.getSerialNo(BusinessConst.N_IKANSYORIKBN_CLASS_APPL_NO);
    }

    // 同時設計連番一
    int doujiSekrenno = 1;

    for (int i = 0; i < sSekNoList.size(); i++) {
        etDoujiSekkanri.setSilosno(sMosno);
        etDoujiSekkanri.setDoujiSekrenno(doujiSekrenno);
        etDoujiSekkanri.setSSekno(sSekNoList.get(i));
        etDoujiSekkanri.setSetrenno(sSetrenbanList.get(i));
        etDoujiSekkanri.setSSyono(null);

        doujiSekkanriDao.insertSelective(etDoujiSekkanri);

        // 同時設計連番一、同時設計連番+1
        doujiSekrenno++;
    }

    // 申込番号を返す
    return sMosno;
}


/**
 * 业务组件10: SKOLE 同时设计管理表注册
 *
 * @param sSekNoList 设计编号列表
 * @param sSetrenbanList 设计连番列表
 * @param doujiSekkanri 同时设计管理信息
 * @param psilosno 申请编号
 * @return 申请编号
 */

public String putSkm20145(List<String> sSekNoList, List<Integer> sSetrenbanList, DoujiSekkanri doujiSekkanri, String psilosno) {
    DoujiSekkanri etDoujiSekkanri = new DoujiSekkanri();
    String sMosno = psilosno;

    if (StringUtils.isEmpty(sMosno)) {
        // GB BSK10401 获取序列号
        sMosno = businessLogic.getSerialNo(BusinessConst.N_IKANSYORIKBN_CLASS_APPL_NO);
    }

    // 初始化同时设计连番
    int doujiSekrenno = 1;

    for (int i = 0; i < sSekNoList.size(); i++) {
        etDoujiSekkanri.setSilosno(sMosno);
        etDoujiSekkanri.setDoujiSekrenno(doujiSekrenno);
        etDoujiSekkanri.setSSekno(sSekNoList.get(i));
        etDoujiSekkanri.setSetrenno(sSetrenbanList.get(i));
        etDoujiSekkanri.setSSyono(null);

        doujiSekkanriDao.insertSelective(etDoujiSekkanri);

        // 同时设计连番递增
        doujiSekrenno++;
    }

    // 返回申请编号
    return sMosno;
}










import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

    private YkSkm10303Response getSekknsk(String bizCode, YkSkm10303Request ykSkm10303Request,
                                          DesignListInquiryServiceRequestDataset designListInquiryServiceRequestDataset,
                                          ProcessContext processContext, DateTimeFormatter dtf,
                                          ArrayList<EtSekkihonkey> etSekkihonkeyList, SimpleDateFormat sdf, String sysDate) {

        YkSkm10303Response ykSkm10303Response = new YkSkm10303Response();

        // 2. 検索条件設定

        // 変数_顧客名(カナ)の設定
        String skoknmkn = BusinessConst.EMPTY;

        // 設計書一覧照会サービスリクエストDataset. 顧客名(カナ) 名≠ブランクかつ
        // 入力IF.設計書一覧照会サービスリクエストDataset. 顧客名(カナ) 姓≠ブランクの場合
        if (!StringUtils.isEmpty(designListInquiryServiceRequestDataset.getCustNameKnFirstName()) &&
            !StringUtils.isEmpty(designListInquiryServiceRequestDataset.getCustNameKnFamilyName())) {

            // 設計書・申込書【GB SKM30106 姓名結合編集】を実行
            skoknmkn = businessCmnLogicSkmEdit.putSkm30106(
                    designListInquiryServiceRequestDataset.getCustNameKnFamilyName(),
                    designListInquiryServiceRequestDataset.getCustNameKnFirstName());

        } else if (StringUtils.isEmpty(designListInquiryServiceRequestDataset.getCustNameKnFirstName()) &&
                   !StringUtils.isEmpty(designListInquiryServiceRequestDataset.getCustNameKnFamilyName())) {
            // 設計書一覧照会サービスリクエストDataset. 顧客名(カナ) 名= ブランクかつ
            // 設計書一覧照会サービスリクエストDataset. 顧客名(カナ) 姓≠ブランクの場合
            skoknmkn = designListInquiryServiceRequestDataset.getCustNameKnFamilyName();
        } else {
            // 上記以外はブランクを設定
            skoknmkn = BusinessConst.EMPTY;
        }

        // 変数_親代理店コードの値を以下の様に設定する。
        String soyadrtncd = BusinessConst.EMPTY;

        // プロセスコンテキスト、イベントID = <イベントID>設計情報照会の場合
        if (BusinessConst.CID_S_MENULINKKAHI_CLASS_SEK_HYOUJI.equals(processContext.getEventId())) {
            soyadrtncd = BusinessConst.EMPTY;
        } else {
            soyadrtncd = designListInquiryServiceRequestDataset.getOperatorPaAgencyCode();
        }


          // 変数 設計書印刷済の値を以下の様に設定する。
        Integer nSekprtsumi;

        if (BusinessConst.CID_S_MENULINKKAHI_CLASS_MAKEMOS.equals(processContext.getEventId())) {
            nSekprtsumi = BusinessConst.CID_N_OUTSUMIKBN_CLASS_OUTSUMI;
        } else if (BusinessConst.CID_S_MENULINKKAHI_CLASS_KAKUNGSEKSAKUSEI.equals(processContext.getEventId())) {
            nSekprtsumi = BusinessConst.CID_N_OUTSUMIKBN_CLASS_MIOUT;
        } else {
            nSekprtsumi = null;
        }

        // 変数 支店コードの値を以下の様に設定する。
        String sSitencd = designListInquiryServiceRequestDataset.getHanBranchCode();

        // 変数_検索保険種類設定
        String sknskhknsyurui = BusinessConst.EMPTY;

        if (StringUtils.isEmpty(designListInquiryServiceRequestDataset.getInsuranceType())) {
            StringBuilder sbknskhknsyurui = new StringBuilder();
            for (int i = 0; i < ykSkm10303Request.getDesignListInquiryServiceRequestPlanCodeList().size(); i++) {
                if (sbknskhknsyurui.length() > 0) {
                    sbknskhknsyurui.append("'");
                    sbknskhknsyurui.append(",");
                    sbknskhknsyurui.append("'");
                }
                sbknskhknsyurui.append(ykSkm10303Request.getDesignListInquiryServiceRequestPlanCodeList().get(i).getPlanCode());
            }
            sknskhknsyurui = sbknskhknsyurui.toString();
        } else {
            sknskhknsyurui = designListInquiryServiceRequestDataset.getInsuranceType();
        }
 // 変数_取扱行員コードの設定
        String sTratkikouinned = BusinessConst.EMPTY;

        if (!StringUtils.isEmpty(designListInquiryServiceRequestDataset.getHanBankerCode())) {
            if (BusinessConst.CID_S_BOSYUUSYSCTRLKBN_CLASS_SMBC.equals(ykSkm10303Request.getPageControlInfo().getRcrtmntSystemControlClass())) {
                sTratkikouinned = designListInquiryServiceRequestDataset.getHanBankerCode();
            } else if (BusinessConst.CID_S_BOSYUUSYSCTRLKBN_CLASS_MADOHAN.equals(ykSkm10303Request.getPageControlInfo().getRcrtmntSystemControlClass()) &&
                       BusinessConst.CID_S_KSBTGYOUMUSYUBETUKBN_CLASS_FFG.equals(ykSkm10303Request.getPageControlInfo().getIndividualCompanyBizCatClass())) {
                sTratkikouinned = designListInquiryServiceRequestDataset.getHanBankerCode();
            } else {
                sTratkikouinned = businessCmnLogicSkmEdit.putSkm30107(
                        ykSkm10303Request.getPageControlInfo().getRcrtmntSystemControlClass(),
                        designListInquiryServiceRequestDataset.getHanBankerCode());
            }
        }

        // 検索ステータス区分
        List<String> sStatuskbnList = new ArrayList<>();

        if ((BusinessConst.CID_S_MENULINKKAHI_CLASS_SEK_HYOUJI.equals(processContext.getEventId()) ||
             BusinessConst.CID_S_MENULINKKAHI_CLASS_MAKEMOS.equals(processContext.getEventId())) &&
            BusinessConst.CID_S_PLRIYOUKTAIKEN_CLASS_ARI.equals(processContext.getAuthenticationInfo().getSPIsysriyoukbn())) {

            if (StringUtils.isEmpty(designListInquiryServiceRequestDataset.getPIStatusClass())) {
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_MISENTAKU.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_HORYU.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_MOS_TTDK.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_PLOTTOK_SENTAKUZUMI.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_MOS_TTORTYU.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_NAIBU_KANRI_MISYOUNIN.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_TTDK_MISOUSIN.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_TTDK_SOUSINZUMI.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_TORISAGE_MISYOUNIN.toString());
                sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_TORISAGE.toString());
            }
        }

        // プロセスコンテキスト、イベントID = <イベントID> 申込書作成の場合
        else if (BusinessConst.CID_S_MENULINKKAHI_CLASS_MAKEMOS.equals(processContext.getEventId())) {
            sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_MISENTAKU.toString());
            sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_HORYU.toString());
            sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_MOS_TTDK.toString());
        }

        // プロセスコンテキスト、イベントID = <イベントID>確認後設計書作成の場合
        else if (BusinessConst.CID_S_MENULINKKAHI_CLASS_KAKUNGSEKSAKUSEI.equals(processContext.getEventId())) {
            sStatuskbnList.add(BusinessConst.CID_N_PLSTATUSKBN_CLASS_MISENTAKU.toString());
        }

        // カンマ区切りにする
        String sStatuskbn = String.join(",", sStatuskbnList);

        // 上記以外の場合
        else {
            sStatuskbn = Integer.toString(designListInquiryServiceRequestDataset.getPIStatusClass());
        }

        // 変数_取扱募集人コードの設定
        String sBosyuucd = BusinessConst.EMPTY;
        String sTratkibosyuucd = BusinessConst.EMPTY;

        if (Integer.parseInt(processContext.getAuthenticationInfo().getSousatnatkbn()) == BusinessConst.CID_N_SOUSATNMTKBN_CLASS_MOBILE) {
            sTratkibosyuucd = designListInquiryServiceRequestDataset.getAgentCode();
        }

        if (BusinessConst.CID_S_KSBTGYOUMUSYUBETUKBN_CLASS_MIZUHOBANK.equals(ykSkm10303Request.getPageControlInfo().getIndividualCompanyBizCatClass()) &&
            BusinessConst.CID_S_GYOUSYUCD_CLASS_BOSYUUNIN.equals(processContext.getAuthenticationInfo().getSSyoksycd())) {
            sBosyuucd = designListInquiryServiceRequestDataset.getAgentCode();
        }

        // 変数検索基準日の設定
        String searchYmdString = businessCmnLogicBsk.setRekijitsukeisan(sysDate, 0, 0, BEFORE_DAYS_180);
        LocalDate kensakuDateLocal = LocalDate.parse(searchYmdString, DateTimeFormatter.ofPattern(BusinessConst.DATE_FORMAT_YYYYMMDD_NON_SLASH));
        String sknskkinyed = DateTimeFormatter.ofPattern(BusinessConst.DATE_FORMAT_YYYYMMDD).format(kensakuDateLocal);

        // DB取得
        GbSkm10151Request gbSkm10151Request = new GbSkm10151Request();

        // 募集システム制御区分
        gbSkm10151Request.setBosyuuSusCtrlKbn(ykSkm10303Request.getPageControlInfo().getRcrtmntSystemControlClass());

        // 支店コード
        gbSkm10151Request.setSSitencd(sSitencd);

        // CIFコード
        gbSkm10151Request.setKensakusCifcd(designListInquiryServiceRequestDataset.getCifCode());

        // 顧客名(カナ)
        gbSkm10151Request.setKokNmKn(skoknmkn);

        // 取扱行員コード
        gbSkm10151Request.setSTratkikouinncd(sTratkikouinned);

        // 検索保険種類
        gbSkm10151Request.setKensakuHknsyruiKbn(sknskhknsyurui);

        // ステータス区分
        gbSkm10151Request.setKensakuNstatuskbn(sStatuskbn);

        // 作成日(自)
        gbSkm10151Request.setKensakusSakuseiYmdfrom(designListInquiryServiceRequestDataset.getCreateDateFrom());

        // 作成日(至)
        gbSkm10151Request.setKensakusSakuseiYmdto(designListInquiryServiceRequestDataset.getCreateDateTo());

        // 検索基準日
        gbSkm10151Request.setKensakukijunDate(sknskkinyed);

        // 親代理店コード
        gbSkm10151Request.setSoyadrtenCd(soyadrtncd);

        // 取扱募集人コード
        gbSkm10151Request.setSTratkibosyuucd(sTratkibosyuucd);

        // 募集人コード
        gbSkm10151Request.setSBosyuucd(sBosyuucd);

        // 設計書印刷済
        gbSkm10151Request.setNSekprtsumi(nSekprtsumi);

        // 操作端末区分
        gbSkm10151Request.setSSousatnmtkbn(processContext.getAuthenticationInfo().getSSousatnmtkbn());

        // 職種コード
        gbSkm10151Request.setSSyoksycd(processContext.getAuthenticationInfo().getSSyoksycd());

        gbSkm10151Request.setPsKsbtGyoumuSyubetukbn(ykSkm10303Request.getPageControlInfo().getIndividualCompanyBizCatClass());

        logger.info("request123 : {}", gbSkm10151Request.toString());

        // C設計書検索情報にGB_SKM10151の取得結果を格納する。
        List<CSkmSksks> cSkmSksksList = businessCmnLogicSkCall.getSkm10151(gbSkm10151Request);

        // 取得したC設計書検索情報のリストが0件の場合
        if (cSkmSksksList == null || cSkmSksksList.size() == 0) {
            throw new BusinessException(ErrorCode.EA0005.getCode(), ErrorCode.EA0004.getMessage(applicationProperties.getSearchResultsMaxCount()), Severity.E);
        } else if (cSkmSksksList.size() > Integer.parseInt(applicationProperties.getSearchResultsMaxCount())) {
            throw new BusinessException(ErrorCode.EA0005.getCode(), ErrorCode.EA0005.getMessage(applicationProperties.getSearchResultsMaxCount()), Severity.E);
        }

        // 設計書一覧照会サービスレスポンスデータセットリスト
List<DesignListInquiryServiceResponseDataset> designListInquiryServiceResponseList = new ArrayList<>();

// 設計書基本テーブルキー
EtSekkihonkey etSekkihonkey = new EtSekkihonkey();

// 変数、親代理店マップの初期化
Map<String, String> daiRiTenMap = new HashMap<>();

// C設計書検索情報のリストの件数分繰り返す。
for (CskmSksks cSkmSksks : cSkmSksksList) {
    DesignListInquiryServiceResponseDataset designListInquiryServiceResponseDataset = new DesignListInquiryServiceResponseDataset();

    // 変数_主契約・特約P合計金額の設定
    BigDecimal nSyutkpkei = BigDecimal.ZERO;

    // C設計書検索情報リスト、セット販売区分 = <セット販売区分>セット販売のとき
    if (BusinessConst.CID_N_SETHANKBN_CLASS_SET.equals(cSkmSksks.getNSethanbaikbn())) {
        nSyutkpkei = businessCmnLogicSkmCall.getSkm10137(cSkmSksks.getSBosyuucd(), cSkmSksks.getSSekno());
    } else {
        nSyutkpkei = cSkmSksks.getNSyutkpkei();
    }

    // 変数 取扱行員コードの設定
    if (BusinessConst.CID_S_BOSYUUSYSCTRLKBN_CLASS_MADOHAN.equals(ykSkm10303Request.getPageControlInfo().getRcrtmntSystemControlClass())) {
        sTratkikouinned = businessCmnLogicSkmEdit.putSkm30108(
                ykSkm10303Request.getPageControlInfo().getIndividualCompanyBizCatClass(),
                designListInquiryServiceRequestDataset.getBankerCodeDigit(),
                cSkmSksks.getSTratkikouinncd());
    } else {
        sTratkikouinned = cSkmSksks.getSTratkikouinncd();
    }

    // 設計書検索情報の値を設計書一覧照会サービスレスポンスリストDatasetに詰める。
    designListInquiryServiceResponseDataset.setAgentCode(cSkmSksks.getSBosyuucd());
    designListInquiryServiceResponseDataset.setHanAgentCode(cSkmSksks.getSTratkibosyuucd());
    designListInquiryServiceResponseDataset.setCreateDate(cSkmSksks.getSSakuseiYmd());
    designListInquiryServiceResponseDataset.setDesignNo(cSkmSksks.getSSekno());
    designListInquiryServiceResponseDataset.setHanBankerCode(sTratkikouinned);
    designListInquiryServiceResponseDataset.setAgentFullName(getBosyuInfo(cSkmSksks.getSBosyuucd()));
    designListInquiryServiceResponseDataset.setCifCode(cSkmSksks.getSCifcd());
    designListInquiryServiceResponseDataset.setContrNameKj(cSkmSksks.getSKyknmkj());
    designListInquiryServiceResponseDataset.setInsuredNameKj(cSkmSksks.getSHhknnmkj());
    designListInquiryServiceResponseDataset.setPlanNameKj(cSkmSksks.getSPlannmkj());
    designListInquiryServiceResponseDataset.setMainCntrctRiderPTotalAmt(nSyutkpkei);
    designListInquiryServiceResponseDataset.setIndexTotalClass(cSkmSksks.getNSakuintsnkbn());
    designListInquiryServiceResponseDataset.setIndexTotalSpApproClass(cSkmSksks.getNSakuintsntknkbn());
    designListInquiryServiceResponseDataset.setProcMethClass(cSkmSksks.getNTtdkhouhoukbn());
    designListInquiryServiceResponseDataset.setPIStatusClass(cSkmSksks.getNPIstatuskbn());
    designListInquiryServiceResponseDataset.setPolicyNo(cSkmSksks.getSSyono());
    designListInquiryServiceResponseDataset.setSelect(cSkmSksks.getSBosyuucd() + SEP + cSkmSksks.getSSekno() + SEP + cSkmSksks.getNSakuintsnkbn() + SEP + cSkmSksks.getNSakuintsntknkbn());
    designListInquiryServiceResponseDataset.setAcceptControlId(cSkmSksks.getSUktkid());
    designListInquiryServiceResponseDataset.setPaAgency(getAgbosyuuInfo(cSkmSksks.getSBosyuucd(), cSkmSksks.getSSekno(), Integer.valueOf(SETRENNO_1), sysDate, daiRiTenMap));

    designListInquiryServiceResponseList.add(designListInquiryServiceResponseDataset);

    // テーブルキーリクエストに募集人コード、設計書番号、セット連番を設定
    etSekkihonkey.setSBosyuucd(cSkmSksks.getSBosyuucd());
    etSekkihonkey.setSSekno(cSkmSksks.getSSekno());
    etSekkihonkey.setNSetrenno(SETRENNO_1);

    etSekkihonkeyList.add(etSekkihonkey);
}

ykSkm10303Response.setDesignListInquiryServiceResponseList(designListInquiryServiceResponseList);

// 出力IFを返却
return ykSkm10303Response;


GbSkm10151Request [
    bosyuuSusCtrlKbn=S,
    sSitencd=null,
    kensakusCifcd=null,
    kokNmKn=,
    sTratkikouinncd=,
    kensakuHknsyrui Kbn=510M311010, 510M352010, 510M370010, 510M320010,
    kensakuNstatuskbn=1, 10, 2, 3, 4, 5, 6, 7, 8, 9,
    kensakus Sakusei ymdform=18730918,
    kensakusSakusei ymdto=20241002,
    kensakuKi junDate=2024-04-05,
    s0yadrtenCd=,
    sTratkibosyuucd=,
    sBosyuucd=,
    nSekprtsumi=null,
    sSousatnmtkbn=1,
    sSyoksycd=01,
    psKsbtGyoumuSyubetukbn=99
]



/**
 * 設計書基本情報リスト取得_設計書検索
 * 
 * @param etSekkihon 設計書基本情報
 * @param kensakuNstatuskbn 検索ステータス区分
 * @param kensakuHknsyuri 検索保険種類
 * @param s_creatYmdFrom 検索用 作成日(自)
 * @param s_creatYmdTo 検索用 作成日(至)
 * @param soprateCd 操作端末区分コード
 * @param sCareerCd キャリアコード
 * @param pkratkiAmiSearchFlg 取扱行員コードに対してあいまい検索フラグ
 * @return 設計書検索情報リスト
 */
public List<CSkmSksks> selETSekKihon(
    EtSekkihon etSekkihon, 
    String kensakuNstatuskbn, 
    String kensakuHknsyuri, 
    String s_creatYmdFrom, 
    String s_creatYmdTo, 
    String soprateCd, 
    String sCareerCd, 
    boolean pkratkiAmiSearchFlg
) {
    return etSekkihonMapper.selETSekkihon(
        etSekkihon, 
        kensakuNstatuskbn, 
        kensakuHknsyuri, 
        s_creatYmdFrom, 
        s_creatYmdTo, 
        soprateCd, 
        sCareerCd, 
        pkratkiAmiSearchFlg
    );
}




/**
 * 業務部品ID: GB_SHS30404_主契約申込S2取得(保険料計算向け)
 *
 * @param cShsMoss2ToPkeisanInputInfo C申込S2(保険料計算向け) 入力情報
 * @return CShsMoss2ToPkeisanOutputInfo C申込S2(保険料計算向け) 出力情報
 */
public CShsMoss2ToPkeisanOutputInfo getShs30404KCShsMoss2ToPkeisan(CShsMoss2ToPkeisanInputInfo cShsMoss2ToPkeisanInputInfo) {
    CShsMoss2ToPkeisanOutputInfo cShsMoss2ToPkeisanOutputInfo = new CShsMoss2ToPkeisanOutputInfo();
    cShsMoss2ToPkeisanOutputInfo.setCErrorInformationList(new ArrayList<CErrorInformation>());

    // 1. 主契約申込S2の計算
    BigDecimal nMoss2 = BigDecimal.ZERO;

    // (1). 申込S2の取得
    if (StringUtils.equalsAny(cShsMoss2ToPkeisanInputInfo.getSSyouhntypekbn(),
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUD,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUF,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUI,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUK,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUL,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUN,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUM,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUS,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUT,
            BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUAD)) {

        // (ア) 申込S2の計算を行う
        if (StringUtils.equalsAny(01,
                BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUK,
                BusinessConst.CID_S_SYOHNTYPEKBN_CLASS_IRYOUS)) {

            // C基本給付金額倍率出力情報を取得
            CShsKyufukngkBirtSytrykInfo cShsKyufukngkBirtSytrykInfo = getShs30106(cShsMoss2ToPkeisanInputInfo.getNKhnkyhkgbairitukbn());

            if (!CommonUtil.isCollectionEmpty(cShsKyufukngkBirtSytrykInfo.getCErrorInformationList())) {
                // Cエラー情報リストに追加
                cShsMoss2ToPkeisanOutputInfo.getCErrorInformationList().addAll(cShsKyufukngkBirtSytrykInfo.getCErrorInformationList());

                if (cShsKyufukngkBirtSytrykInfo.getCErrorInformationList().stream()
                        .anyMatch(s -> BusinessConst.CID_S_ERRORKBN_CLASS_ERROR.equals(s.getErrorType()))) {
                    return cShsMoss2ToPkeisanOutputInfo;
                }
            }

            // 申込S2の設定、小数第1位を切り捨て
            nMoss2 = cShsMoss2ToPkeisanInputInfo.getNMoss().multiply(cShsKyufukngkBirtSytrykInfo.getNKhnkyhkgbairitullarimds()).setScale(0, RoundingMode.DOWN);
        } else {
            // 申込S2の設定、小数第1位を切り捨て
            nMoss2 = cShsMoss2ToPkeisanInputInfo.getNMoss().multiply(BigDecimal.valueOf(cShsMoss2ToPkeisanInputInfo.getNKhnkyhkgbairitukbn()));
        }
    }

    // 2. 出力情報の編集
    cShsMoss2ToPkeisanOutputInfo.setSSyouhncd(cShsMoss2ToPkeisanInputInfo.getSSyouhncd());
    cShsMoss2ToPkeisanOutputInfo.setNMoss2(nMoss2);

    return cShsMoss2ToPkeisanOutputInfo;
}




<select id="selectBySearchConditionTratkikanoSouhin"
        parameterType="com.medicarelife.hibiki.skm.model.entity.tbl.EtSekkihon"
        resultType="com.medicarelife.hibiki.skm.model.entity.rt.CSkmDoujiMskmStInfo">
    <!-- WARNING @mbg.generated
         This element is automatically generated by MyBatis Generator, do not modify. -->
    select
        A.s_sakuseiymd AS sSakuseiymd,
        A.s_sekno AS sSekno,
        A.s_tratkikouinncd AS sTratkikouinncd,
        A.s_tratkibosyuunm AS sTratkibosyuunm,
        A.s_kyknmkj AS sKyknmkj,
        A.s_hhknnmkj AS sHhknnmkj,
        A.n_syutkpkei AS nSyutkpkei,
        A.s_bosyuucd AS sBosyuucd,
        A.n_setrenno AS nSetrenno,
        A.s_kykbosyuucd AS sKykbosyuucd,
        A.s_kykkokno AS sKykkokno,
        A.s_hhknbosyuucd AS sHhknbosyuucd,
        A.s_hhknkokno AS sHhknkokno,
        A.n_hrkkaisuu AS nHrkkaisuu,
        C.s_hanbainm AS sHanbainm,
        C.s_hknsyruikbn AS sHknsyruikbn
    from
        et_sekkihon A,
        et_seksyouhin B,
        em_plan C
    where
        A.s_bosyuucd = B.s_bosyuucd
        and A.s_sekno = B.s_sekno
        and A.n_setrenno = B.n_setrenno
        and A.s_plancd = C.s_plancd
        and A.s_daisosikicd = C.s_daisosikicd
        <if test="etKokkihon.sTratkibosyuucd != null and etKokkihon.sTratkibosyuucd != ''">
            and A.s_tratkibosyuucd = #{etKokkihon.sTratkibosyuucd,jdbcType=VARCHAR}
        </if>
        and A.n_planrenno = C.n_planrenno
        and B.n_syutkkbn = #{etSeksyouhin.nSyutkkbn,jdbcType=NUMERIC}
        and A.s_oyadrtencd = #{etKokkihon.sOyadrtencd,jdbcType=VARCHAR}
        and A.n_ttdkhouhoukbn = #{etKokkihon.nTtdkhouhoukbn,jdbcType=NUMERIC}
        and A.n_sekprtsumi = #{etKokkihon.nSekprtsumi,jdbcType=NUMERIC}
        <if test="etKokkihon.sTratkisitencd != null and etKokkihon.sTratkisitencd != ''">
            and A.s_tratkisitencd = #{etKokkihon.sTratkisitencd,jdbcType=VARCHAR}
        </if>
        <if test="etKokkihon.sKyknmkn != null and etKokkihon.sKyknmkn != ''">
            and (A.s_kyknmkn = #{etKokkihon.sKyknmkn,jdbcType=VARCHAR} or A.s_hhknnmkn = #{etKokkihon.sKyknmkn,jdbcType=VARCHAR})
        </if>
        <if test="etKokkihon.sTratkikouinncd != null and etKokkihon.sTratkikouinncd != ''">
            and A.s_tratkikouinncd = #{etKokkihon.sTratkikouinncd,jdbcType=VARCHAR}
        </if>
        and C.s_hknsyruikbn in
        <if test="oAryTratkikanoSouhinList != null and oAryTratkikanoSouhinList.size() > 0">
            <foreach collection="oAryTratkikanoSouhinList" open="(" item="sHknsyruikbn" separator="," close=")">
                #{sHknsyruikbn}
            </foreach>
        </if>
        <if test="etKokkihon.sSakuseiymd != null and etKokkihon.sSakuseiymd != ''">
            and A.s_sakuseiymd <![CDATA[ >= ]]> #{etKokkihon.sSakuseiymd,jdbcType=VARCHAR}
        </if>
        <if test="kensakusSakuseiymdto != null and kensakusSakuseiymdto != ''">
            and A.s_sakuseiymd <![CDATA[ <= ]]> #{kensakusSakuseiymdto,jdbcType=VARCHAR}
        </if>
        and DATE_TRUNC('day', A.s_kostime) <![CDATA[ >= ]]> #{etKokkihon.sKostime,jdbcType=DATE}
    order by
        A.s_sakuseiymd desc,
        A.s_sekno
</select>


EtSekkihon etSekkihon = new EtSekkihon(
    nTuujyoukanikbn = null,
    nKojinhjnkbn = null,
    nMoskbn = null,
    nIktsisanumu = null,
    nIktsisanbangoukbn = null,
    sSakuseiYmd = "20240929",
    sMossakuseiYmd = null,
    nYoteikykymdkbn = null,
    sYoteikykymd = null,
    nKykymdtkrumu = null,
    sMosymd = null,
    sSknnkaisiymd = null,
    sYkkigenYmd = null,
    sHhknbosyuucd = null,
    sHhknkokno = null,
    sHhknnmkn = "ホケンロハナコ",
    sHhknnmkj = null,
    sHhknseiymd = null,
    nHhknnen = null,
    nKyksei = null,
    sHjndainmkn = null,
    sHindainmkj = null,
    sHjndaihyousyaseiYmd = null,
    nHjndaiykkbn = null,
    nKykTdk = null,
    sKokuzeihjnno = null,
    nHjnktaikbn = null,
    sTsintelno = null,
    sTsinadr1kj = null,
    sTsinadr2kj = null,
    sTsinadr3kj = null,
    sTsinadr4kj = null,
    sDaiktgk = null,
    sTsinyno = null,
    nHhknsei = null,
    sHhknyno = null,
    sHhkntelno = null,
    sHhknadr1kj = null,
    sHhknadr2kj = null,
    sHhknadr3kj = null,
    sHhknadr4kj = null,
    nKykknkkbn = null,
    nKykKbn = null,
    sKykbosyuucd = null,
    sKykkokno = null,
    sKyknmkj = null,
    sKykseiymd = null,
    nKyknen = null,
    sKyknmkn = "ホケンロハナコ",
    sTsinnittyuutelno = null,
    sTsinmailadr = null,
    nHrkkaisuu = null,
    nHrkkeiro = null,
    nEsthrkhou = null,
    nSntkhoukbn = null,
    nTkbtkktumu = null,
    nKokutiatkihukakbn = null,
    sPlancd = null,
    nPlanrenno = null,
    nSdpdkbn = null,
    nLivhukakbn = null,
    sLivkbn = null,
    nPmnjtkhukakbn = null,
    sPmnjtkkbn = null,
    nSknnkaiSikitkhukakbn = null,
    nSknnkaisikitkkbn = null,
    nKougkwaribikigk = null,
    nItijikns = null,
    nHeikins = null,
    nSyutkpkei = null,
    nFsthrkpkei = null,
    nKougkwaribikiumu = null,
    nKougkwaribikikbn = null,
    nSyonendodrtentsry = null,
    nJinendodrtentsry = null,
    nDrtentesuuryou = null,
    nKnkknpkyhknssyki = null,
    nSiteidrumu = null,
    nIksbhkkuktumu = null,
    nSakuintsnkbn = null,
    nSakuintsntknkbn = null,
    sSakuintsntknymd = null,
    sIksytratkikjnkbn = null,
    nSakuseitaisyoukbn = null,
    nIkkinyukkntkrumu = null,
    sDaisosikicd = null,
    sOyadrtencd = "5100001",
    sTratkiagcd = null,
    sTratkiknyukkncd = null,
    sTratkisitencd = null,
    sTratkibosyuucd = null,
    sTratkibosyuunm = null,
    sTratkiagnm = null,
    sTratkisitennm = null,
    sTratkibosyuuyno = null,
    sTratkibosyuuadrl1kj = null,
    sTratkibosyuuadr2kj = null,
    sTratkibosyuuadr3kj = null,
    sTratkibosyuuadr4kj = null,
    sTratkibosyuutelno = null,
    sTratkikouinncd = null,
    nKyoudoubosyuumu = null,
    sKydbsydrtentrkno = null,
    sKydbsydrtenknyukkncd = null,
    sKydbsydrtensitencd = null,
    sKydbsydrtennm = null,
    sKydbsydrtenbsytrkno = null,
    sKydbsydrtenbsynm = null,
    sKydbsydrtennmgwrenkei = null,
    sKydbsydrtenbsyningwrenkei = null,
    sDoujiSekdaiSekno = null,
    sSyono = null,
    sAitesyono = null,
    nKaiyakudoujiSekumu = null,
    sDoujikaiyakusyono = null,
    nDoujikaiyakusyubetukbn = null,
    sCifcd = null,
    nSekprtsumi = 1,
    nMosprtsumi = null,
    nHeiyoumosprtsumi = null,
    sBosyuusysctrIkbn = null,
    sKsbtgyoumusyubetukbn = null,
    nRiyousyskbn = null,
    nGaibusiRyoskmotokbn = null,
    nSekikkatuumu = null,
    nSethnbitaisyouumu = null,
    nSetatsukitaisyouumu = null,
    nSoftplanumu = null,
    nSoftplanyuukoujyoutaikbn = null,
    sSoftplankbn = null,
    nSekhoukbn = null,
    nSelplankbrn = null,
    sSelplancd = null,
    nYkndensikakbn = null,
    nTtdkhouhoukbn = 0,
    nJuyoujkstmiukthouhoukbn = null,
    nPIstatuskbn = null,
    nMostrksumu = null,
    sNextkosymd = null,
    nNextkoshhknnen = null,
    nNextkosgohknkkn = null,
    nNextkosgopkei = null,
    sUktkid = null
);


kensakusSakuseiymdto = "20241013"
etSeksyouhin.nSyutkkbn = 1
etKokkihon.sTratkibosyuucd = []
etKokkihon.sKostime="2024-04-16"
oAryTratkikanoSouhinList=[CQ,CV,CS,CW]


select
        A.s_sakuseiymd AS sSakuseiymd,
        A.s_sekno AS sSekno,
        A.s_tratkikouinncd AS sTratkikouinncd,
        A.s_tratkibosyuunm AS sTratkibosyuunm,
        A.s_kyknmkj AS sKyknmkj,
        A.s_hhknnmkj AS sHhknnmkj,
        A.n_syutkpkei AS nSyutkpkei,
        A.s_bosyuucd AS sBosyuucd,
        A.n_setrenno AS nSetrenno,
        A.s_kykbosyuucd AS sKykbosyuucd,
        A.s_kykkokno AS sKykkokno,
        A.s_hhknbosyuucd AS sHhknbosyuucd,
        A.s_hhknkokno AS sHhknkokno,
        A.n_hrkkaisuu AS nHrkkaisuu,
        C.s_hanbainm AS sHanbainm,
        C.s_hknsyruikbn AS sHknsyruikbn
    from
        et_sekkihon A,
        et_seksyouhin B,
        em_plan C
    where
        A.s_bosyuucd = B.s_bosyuucd
        and A.s_sekno = B.s_sekno
        and A.n_setrenno = B.n_setrenno
        and A.s_plancd = C.s_plancd
        and A.s_daisosikicd = C.s_daisosikicd
        and A.n_planrenno = C.n_planrenno
        and B.n_syutkkbn = 1
        and A.s_oyadrtencd = '5100001'
        and A.n_ttdkhouhoukbn = 0
        and A.n_sekprtsumi = 1
        and A.s_tratkisitencd = ''
        and (A.s_kyknmkn = 'ホケンロハナコ' or A.s_hhknnmkn = 'ホケンロハナコ')
        // and A.s_tratkikouinncd = ''
        and C.s_hknsyruikbn in ('CQ', 'CV', 'CS', 'CW')
        and A.s_sakuseiymd <![CDATA[ >= ]]> '20240929'  ~
        and A.s_sakuseiymd <![CDATA[ <= ]]> '20241013'
        and DATE_TRUNC('day', A.s_kostime) <![CDATA[ >= ]]> '2024-04-16'
    order by
        A.s_sakuseiymd desc,
        A.s_sekno



        UPDATE em_plan C
SET C.s_hknsyruikbn = 'CV'
FROM et_sekkihon A+-+
+-+-+-+
JOIN et_seksyouhin B ON A.s_bosyuucd = B.s_bosyuucd
    AND A.s_sekno = B.s_sekno
    AND A.n_setrenno = B.n_setrenno
WHERE A.s_plancd = C.s_plancd
    AND A.s_daisosikicd = C.s_daisosikicd
    AND A.n_planrenno = C.n_planrenno
    AND B.n_syutkkbn = 1
    AND A.s_oyadrtencd = '5100001'
    AND A.n_ttdkhouhoukbn = 0
    AND A.n_sekprtsumi = 1
    AND A.s_tratkisitencd = ''
    AND (A.s_kyknmkn = 'ホケンロハナコ' OR A.s_hhknnmkn = 'ホケンロハナコ')
    AND C.s_hknsyruikbn IN ('CQ', 'CV', 'CS', 'CW')
    AND A.s_sakuseiymd >= '20240929'
    AND A.s_sakuseiymd <= '20241013'
    AND DATE_TRUNC('day', A.s_kostime) >= '2024-04-16'
    AND A.s_sekno = 'your_unique_identifier';



    UPDATE em_plan C
JOIN et_sekkihon A
    ON A.s_plancd = C.s_plancd
    AND A.s_daisosikicd = C.s_daisosikicd
    AND A.n_planrenno = C.n_planrenno
JOIN et_seksyouhin B
    ON A.s_bosyuucd = B.s_bosyuucd
    AND A.s_sekno = B.s_sekno
    AND A.n_setrenno = B.n_setrenno
SET C.s_hknsyruikbn = 'CQ'
WHERE B.n_syutkkbn = 14
    AND A.s_oyadrtencd = '5100001'
    AND A.n_ttdkhouhoukbn = 0
    AND A.n_sekprtsumi = 14
    AND (A.s_kyknmkn = 'HokenHanako' OR A.s_hhknnmkn = 'HokenHanako')
    AND C.s_hknsyruikbn IN ('CQ', 'CV', 'CS', 'CW')
    AND A.s_sekno = 'SSMBC0001741';

-- 开始事务
START TRANSACTION;

-- 执行更新操作
UPDATE em_plan
SET s_hknsyruikbn = 'CQ'
WHERE s_plancd IN (
    SELECT A.s_plancd
    FROM et_sekkihon A
    JOIN et_seksyouhin B
        ON A.s_bosyuucd = B.s_bosyuucd
        AND A.s_sekno = B.s_sekno
        AND A.n_setrenno = B.n_setrenno
    WHERE A.s_daisosikicd = em_plan.s_daisosikicd
        AND A.n_planrenno = em_plan.n_planrenno
        AND B.n_syutkkbn = 14
        AND A.s_oyadrtencd = '5100001'
        AND A.n_ttdkhouhoukbn = 0
        AND A.n_sekprtsumi = 14
        AND (A.s_kyknmkn = 'HokenHanako' OR A.s_hhknnmkn = 'HokenHanako')
        AND em_plan.s_hknsyruikbn IN ('CQ', 'CV', 'CS', 'CW')
        AND A.s_sekno = 'SSMBC0001741'
)
AND s_sekno = 'SSMBC0001741';




SELECT
    ruv.yakushoku_kbn,
    ruv.shokushu_corse,
    CASE
        WHEN ruv.shokushu2 = '3' THEN ''
        WHEN ruv.shokushu2 IN ('0', '9')
             AND ruv.shikaku IN ('19', '15')
             AND bst.zokusei4 = '07' THEN 'Ex'
        ELSE ruv.shikaku_to
    END AS shikakuRank,
    ruv.koin_kbn,
    ruv.jugyosha_meisho,
    ruv.course_meisho,
    ruv.SHUMU_YAKUSHOKU_METSHO,
    ruv.jugyoin_no,
    ruv.user_name
FROM
    RK_USER_V ruv
LEFT OUTER JOIN
    BC_SHOGU_TBL bst ON ruv.user_id = bst.user_id
    AND ruv.valid_period_sd BETWEEN bst.valid_period_sd AND bst.valid_period_ed
WHERE
    ruv.kaisha_cd = :kaishaCd
    AND ruv.user_id = :userId
    AND ruv.valid_period_sd <= :kijunbi
    AND ruv.valid_period_ed >= :kijunbi



    @Override
public RkUserv EketByRkUserKensaku(String kaishaCd, Long userId, LocalDate kijunbi) {

    EntityManager entityManager = JpaContext.getEntityManagerByManagedType(RkUserV.class);
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RkUserV> criteriaQuery = criteriaBuilder.createQuery(RkUserV.class);
    Root<RkUserV> root = criteriaQuery.from(RkUserV.class);

    // 検索条件
    List<Predicate> predicates = new ArrayList<>();

    // 会社コード
    predicates.add(criteriaBuilder.equal(root.get(RkUserV_.kaishaCd), kaishaCd));

    // ユーザーID
    ParameterExpression<Long> paramUserId = criteriaBuilder.parameter(Long.class);
    predicates.add(criteriaBuilder.equal(root.get(RkUserV_.userId), paramUserId));

    // 有効期間(開始)
    predicates.add(criteriaBuilder.lessThanOrEqualTo(
        root.get(RkUserV_.validPeriodSd), kijunbi));

    // 有効期間(終了)
    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
        root.get(RkUserV_.validPeriodEd), kijunbi));

    // select作成
    criteriaQuery.select(root)
                 .where(predicates.toArray(new Predicate[0]));

    TypedQuery<RkUserV> typedQuery = entityManager.createQuery(criteriaQuery);

    // パラメータ値(ユーザーID)
    typedQuery.setParameter(paramUserId, userId);

    // select実行
    List<RkUserV> result = typedQuery.getResultList();

    return ObjectUtils.isEmpty(result) ? null : result.get(0);
}

@Override
public RkUserV setByRkUserVKensaku(KaishaCd kaishaCd, Long userId, LocalDate kijunbi) {

    // 获取与 RkUserV 实体类关联的 EntityManager 实例
    EntityManager entityManager = JpaContext.getEntityManagerByManagedType(RkUserV.class);
    
    // 使用 EntityManager 获取 CriteriaBuilder，用于构建动态查询
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    
    // 创建一个 CriteriaQuery 实例，定义查询的结果类型为 RkUserV
    CriteriaQuery<RkUserV> criteriaQuery = criteriaBuilder.createQuery(RkUserV.class);
    
    // 定义查询的根实体，即 FROM 子句中的主要实体
    Root<RkUserV> root = criteriaQuery.from(RkUserV.class);

    // 创建一个用于存储查询条件的列表
    List<Predicate> predicates = new ArrayList<>();

    // 添加公司代码的过滤条件，等于传入的 kaishaCd
    predicates.add(criteriaBuilder.equal(root.get(RkUserV_.kaishaCd), kaishaCd));

    // 添加有效期间（开始）的过滤条件，小于或等于基准日期 kijunbi
    predicates.add(criteriaBuilder.lessThanOrEqualTo(
        root.get(RkUserV_.validPeriodSd), kijunbi));

    // 添加有效期间（结束）的过滤条件，大于或等于基准日期 kijunbi
    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
        root.get(RkUserV_.validPeriodEd), kijunbi));

    // 构建最终的查询，选择根实体并应用所有的过滤条件
    criteriaQuery.select(root)
                 .where(predicates.toArray(new Predicate[0]));

    // 创建一个类型安全的查询对象
    TypedQuery<RkUserV> typedQuery = entityManager.createQuery(criteriaQuery);

    // 执行查询并获取结果列表
    List<RkUserV> result = typedQuery.getResultList();

    // 如果结果列表为空，返回 null；否则返回列表中的第一个元素
    return ObjectUtils.isEmpty(result) ? null : result.get(0);
}

@Override
public List<Object> ketktkeivakuJohd(KaishaCd kaishaCd, Long userId, LocalDate taishobi) {

    // パラメータの設定
    // 创建一个存储参数的列表，使用Pair存储参数名称和值
    List<Pair<String, Object>> parameterList = new ArrayList<Pair<String, Object>>();

    // select句
    // 创建一个StringBuilder用于构建SELECT子句
    StringBuilder selectSb = new StringBuilder();
    selectSb.append("select kj.kinmuNissu,")
            .append(" kj.saikoyoFlag,")
            .append(" kj.saikoyoDate,")
            .append(" kj.shokushuNaiKbn");

    // query句
    // 创建一个StringBuilder用于构建FROM和JOIN子句
    StringBuilder querySb = new StringBuilder();
    querySb.append(" from RkUserV rkuserv")
           .append(" inner join KeiyakuJoho kj")
           .append(" on kj.kaishaCd = rkuserv.kaishaCd")
           .append(" and kj.userId = rkuserv.rkUserVId.userId")
           .append(" and :taishobi between kj.tekiyouDateFrom and kj.tekiyouDateTo")
           .append(" and kj.deleteFlg = :deleteFlg");

    // where句
    // 创建一个StringBuilder用于构建WHERE子句
    StringBuilder whereSb = new StringBuilder();
    whereSb.append(" where");

    // 会社コード
    // 添加公司代码的过滤条件，并将参数添加到列表中
    whereSb.append(" rkuserv.kaishaCd = :kaishaCd");
    parameterList.add(Pair.of("kaishaCd", kaishaCd));

    // ユーザID
    // 添加用户ID的过滤条件，并将参数添加到列表中
    whereSb.append(" and rkuserv.rkUserVId.userId = :userId");
    parameterList.add(Pair.of("userId", userId));

    // 従業者区分
    // 添加従業者区分的过滤条件，并将参数添加到列表中
    whereSb.append(" and rkuserv.jugyoshakbn = :jugyoshakbn");
    parameterList.add(Pair.of("jugyoshakbn", RkConstants.RK_GSRK08858_CD_1));

    // 削除フラグ
    // 添加删除标志的过滤条件，并将参数添加到列表中
    whereSb.append(" and kj.deleteFlg = :deleteFlg");
    parameterList.add(Pair.of("deleteFlg", false));

    // 対象年月の一日
    // 添加基准日期的过滤条件，并将参数添加到列表中
    whereSb.append(" and :taishobi = :taishobi");
    parameterList.add(Pair.of("taishobi", taishobi));

    // select作成
    // 将SELECT、FROM、JOIN和WHERE子句组合成完整的查询语句
    String selectSql = selectSb.toString() + querySb.toString() + whereSb.toString();

    // 创建TypedQuery对象，用于执行查询
    TypedQuery<Object> query = entityManager.createQuery(selectSql, Object.class);

    // パラメータの設定
    // 遍历参数列表，设置查询参数
    for (Pair<String, Object> pair : parameterList) {
        query.setParameter(pair.getLeft(), pair.getRight());
    }

    // select実行
    // 执行查询并获取结果列表
    List<Object> resultList = query.getResultList();

    // 返回查询结果列表
    return resultList;
}


@Override
public List<Object> ketktkeivakuJohd(KaishaCd kaishaCd, Long userId, LocalDate taishobi) {

    // パラメータの設定
    // 创建一个存储参数的列表，使用Pair存储参数名称和值
    List<Pair<String, Object>> parameterList = new ArrayList<Pair<String, Object>>();

    // select句
    // 创建一个StringBuilder用于构建SELECT子句
    StringBuilder selectSb = new StringBuilder();
    selectSb.append("SELECT ")
            .append("ruv.shokushu_corse, ")
            .append("CASE ")
            .append("    WHEN ruv.shokushu2 = '3' THEN '' ")
            .append("    WHEN ruv.shokushu2 IN ('0', '9') ")
            .append("         AND ruv.shikaku IN ('19', '15') ")
            .append("         AND bst.zokusei4 = '07' THEN 'Ex' ")
            .append("    ELSE ruv.shikaku_to ")
            .append("END AS shikakuRank, ")
            .append("ruv.koin_kbn, ")
            .append("ruv.jugyosha_meisho, ")
            .append("ruv.course_meisho, ")
            .append("ruv.SHUMU_YAKUSHOKU_METSHO, ")
            .append("ruv.jugyoin_no, ")
            .append("ruv.user_name ");

    // query句
    // 创建一个StringBuilder用于构建FROM和JOIN子句
    StringBuilder querySb = new StringBuilder();
    querySb.append("FROM ")
           .append("RK_USER_V ruv ")
           .append("LEFT OUTER JOIN BC_SHOGU_TBL bst ON ruv.user_id = bst.user_id ")
           .append("    AND ruv.valid_period_sd BETWEEN bst.valid_period_sd AND bst.valid_period_ed ")
           .append("    AND :taishobi BETWEEN kj.tekiyouDateFrom AND kj.tekiyouDateTo ")
           .append("    AND kj.deleteFlg = :deleteFlg ");

    // where句
    // 创建一个StringBuilder用于构建WHERE子句
    StringBuilder whereSb = new StringBuilder();
    whereSb.append("WHERE ")
           .append("ruv.kaisha_cd = :kaishaCd ")
           .append("AND ruv.user_id = :userId ")
           .append("AND ruv.valid_period_sd <= :kijunbi ")
           .append("AND ruv.valid_period_ed >= :kijunbi ");

    // 添加参数到列表
    parameterList.add(Pair.of("kaishaCd", kaishaCd));
    parameterList.add(Pair.of("userId", userId));
    parameterList.add(Pair.of("kijunbi", taishobi));
    parameterList.add(Pair.of("taishobi", taishobi));
    parameterList.add(Pair.of("deleteFlg", false));

    // select作成
    // 将SELECT、FROM、JOIN和WHERE子句组合成完整的查询语句
    String selectSql = selectSb.toString() + querySb.toString() + whereSb.toString();

    // 创建TypedQuery对象，用于执行查询
    Query query = entityManager.createNativeQuery(selectSql);

    // パラメータの設定
    // 遍历参数列表，设置查询参数
    for (Pair<String, Object> pair : parameterList) {
        query.setParameter(pair.getLeft(), pair.getRight());
    }

    // select実行
    // 执行查询并获取结果列表
    List<Object> resultList = query.getResultList();

    // 返回查询结果列表
    return resultList;
}