public class Auth2ServiceImpl implements Auth2Service {

    /** {@link UserRepository} */
    @Autowired
    private UserRepository userRepository;

    /** {@link GenericCodeRepository} */
    @Autowired
    private GenericCodeRepository genericCodeRepository;

    /** {@link ButenRepository} */
    @Autowired
    private ButenRepository butenRepository;

    /** {@link KaGroupRepository} */
    @Autowired
    private KaGroupRepository kaGroupRepository;

    /** {@link SanshoButenAuthorityRepository} */
    @Autowired
    private SanshoButenAuthorityRepository sanshoButenAuthorityRepository;

    /** {@link GenericAuthorityRepository} */
    @Autowired
    private GenericAuthorityRepository genericAuthorityRepository;

    /** {@link ZaisekiSoshikiRepository} */
    @Autowired
    private ZaisekiSoshikiRepository zaisekiSoshikiRepository;

    /** {@link GenericAuthorityRirekiRepository} */
    @Autowired
    private GenericAuthorityRirekiRepository genericAuthorityRirekiRepository;

    /** {@link SanshoButenAuthorityRirekiRepository} */
    @Autowired
    private SanshoButenAuthorityRirekiRepository sanshoButenAuthorityRirekiRepository;

    /** {@link CustomizedKintainaiUserRepository} */
    @Autowired
    private CustomizedKintainaiUserRepository customizedKintainaiUserRepository;

    /** 数値:0 */
    private static final Integer ZERO = 0;

    /** 数値:8 */
    private static final Integer EIGHT = 8;

    /** 数値:3000 */
    private static final Integer THREE_THOUSAND = 3000;

    /** 数値:6000 */
    private static final Integer SIX_THOUSAND = 6000;

    /** 数値:7000 */
    private static final Integer SEVEN_THOUSAND = 7000;

    /** 数値:7200 */
    private static final Integer SEVEN_THOUSAND_TWO_HUNDRED = 7200;

    /** 部店CD:6910 */
    private static final String BUTENCD_BK = "6910";

    /** 部店CD:0126 */
    private static final String BUTENCD_SC = "0126";

    /** 部店CD:000400 */
    private static final String BUTENCD_TR = "000400";

    /** 課グループCD:41001 */
    private static final String KAGROUPCD_BK = "41001";

    /** 課グループCD:0016 */
    private static final String KAGROUPCD_TR = "0016";

    /** 正式名称:人事Gr */
    private static final String SEISHIKI_MEISHO_JINJI_GR = "人事Gr";

    /** データパターン:日付 */
    private static final List<Pattern> DATE_PATTERNS = Arrays.asList(
            Pattern.compile("^(\\d{4})/(\\d{1,2})/(\\d{1,2})$"),
            Pattern.compile("^(\\d{4})年(\\d{1,2})月(\\d{1,2})日$"),
            Pattern.compile("^(\\d{4}) (\\d{2}) (\\d{2})$"));

    /** 汎用コードセットID: 「GSRK00053:職責(労務管理)」 */
    private static final String GSRK00053 = "GSRK00053";

    /** 1:上司権限自動付与対象 */
    private static final String JOSHI_KENGEN = "1";

    /** 2:承認権限自動付与対象 */
    private static final String SHONIN_KENGEN = "2";

    /** 3:人事部権限自動付与対象 */
    private static final String JINJIBU_KENGEN = "3";

    /** 付与区分:「02: 権限付与」または「03: メンテナンス付与」 */
    private static final List<FuyoKbn> KENGENFUYO_OR_MENTEFUYO = List.of(
            FuyoKbn.KENGENFUYO,
            FuyoKbn.MENTEFUYO);

    /** 付与区分: すべて */
    private static final List<FuyoKbn> FUYO_KBN_ALL = List.of(
            FuyoKbn.HATSUREI,
            FuyoKbn.KENGENFUYO,
            FuyoKbn.MENTEFUYO);

    /** 主務兼務区分:「0:主務」「1:兼務1」「99:管理店番」 */
    private static final List<ShumuKenmuKbn> SHUMU_OR_KENMUT_OR_KANRI_TENBAN = List.of(
            ShumuKenmuKbn.SHUMU,
            ShumuKenmuKbn.KENMU1,
            ShumuKenmuKbn.KANRI_TENBAN);

    /** 主務兼務区分:「0:主務」「99:管理店番」 */
    private static final List<ShumuKenmuKbn> SHUMU_OR_KANRI_TENBAN = List.of(
            ShumuKenmuKbn.SHUMU,
            ShumuKenmuKbn.KANRI_TENBAN);

    /** 主務兼務区分:「0:主務」「1:兼務1」 */
    private static final List<ShumuKenmuKbn> SHUMU_OR_KENMU1 = List.of(
            ShumuKenmuKbn.SHUMU,
            ShumuKenmuKbn.KENMU1);

    /** 主務兼務区分:「0:主務」 */
    private static final List<ShumuKenmuKbn> SHUMU = List.of(
            ShumuKenmuKbn.SHUMU);

    /** 労務管理権限のリスト */
    private static final List<String> RK_ALL_AUTHORITY = Arrays.asList(
            AuthorizeGroup.getAuthStrings(
                    AuthorizeGroup.RK_EMPLOYEE,
                    AuthorizeGroup.RK_BOSS,
                    AuthorizeGroup.RK_AUTHORIZER,
                    AuthorizeGroup.RK_HR_ALL,
                    AuthorizeGroup.RK_KAIGAI));

    /** 汎用権限:従業員、従業員(海外) */
    private static final List<String> RK_EMPLOYEE_ROLE = List.of(
            Authorize.RESP_MUBK_RK_EMPLOYEE.getCode(),
            Authorize.RESP_MUBK_RK_EMPLOYEE_KAIGAI.getCode());

    /** 権限委譲表示対象:1(承認者委譲表示対象) */
    private static final String HYOJITAISHO_SHONINSHA = "1";

    /** 権限委譲表示対象:2(人事部委譲表示対象) */
    private static final String HYOJITAISHO_JINJIBU = "2";

    /** 権限委譲元対象:1(委譲元対象) */
    private static final String IJOFROMTAISHO_IJOFROM = "1";

    /** DB最大日付 */
    private static final LocalDate DATE_MAX = LocalDate.of(9999, 12, 31);

    /**
     * 更新上司承認権限のメソッド
     *
     * @param kaishaCd   会社コード
     * @param kijunbi    基準日
     * @param jugyoinNo  従業員番号
     * @param log        ロガー
     * @return 更新結果 DTO
     */
    @Transactional
    @Override
    public JoshiShonInshakengenUpdateResultDto updateJoshiShonInshakengen(
            KaishaCd kaishaCd, String kijunbi, String jugyoinNo, Logger log) {

        JoshiShoninshakengenUpdateResultDto resultDto = new JoshiShoninshakengenUpdateResultDto();

        // 初期化
        resultDto.setResultStatus(JoshiShonInshakengenUpdateResultStatus.RTN_DEFAULT_999);
        resultDto.setTaishoUserCnt(0);
        resultDto.setInsertCnt(0);
        resultDto.setDeleteCnt(0);
        resultDto.setWarnCnt(0);

        // ワーク変数
        LocalDate wkkijunbi = null;
        Long wkUserId = null;
        int wkTaishoUserCnt = 0;
        int wkInsertCnt = 0;
        int wkDeleteCnt = 0;
        int wkWarnCnt = 0;

        // 引数チェック
        if (ObjectUtils.isEmpty(kaishaCd)) {
            resultDto.setResultStatus(JoshiShonInshakengenUpdateResultStatus.RTN_PARAM_ERR_010);
            return resultDto;
        }

        // 基準日コードチェック
        if (ObjectUtils.isEmpty(kijunbi) || !EIGHT.equals(kijunbi.length()) || !isDate(kijunbi)) {
            resultDto.setResultStatus(JoshiShonInshakengenUpdateResultStatus.RTN_PARAM_ERR_020);
            return resultDto;
        }

        // ワーク変数の設定
        wkkijunbi = getDate(kijunbi);

        // 基準日をString型に変更
        String kijunbiString = getSlashDate(wkKijunbi);

        // 従業員番号チェック処理
        if (!ObjectUtils.isEmpty(jugyoinNo)) {
            // ユーザID取得
            User user = userRepository.findByKaishaCdAndJugyoinNoAndDeleteFlgFalse(
                    kaishaCd, jugyoinNo);
            if (Objects.isNull(user)) {
                resultDto.setResultStatus(JoshiShonInshakengenUpdateResultStatus.RTN_PARAM_ERR_030);
                return resultDto;
            } else {
                wkUserId = user.getUserId();
            }
        }

        // 各種情報の取得
        // 判定用の参照部店権限情報の取得
        List<Pair<User, SanshoButenAuthority>> sanshoButenAuthorityJohoList = userRepository
                .getSanshoButenAuthorityJoho(kaishaCd, wkkijunbi, wkUserId);

        // 付与対象ロール用の汎用コードマスタ情報の取得
        List<GenericCode> genericCodeList = genericCodeRepository.find(
                GSRK00053, null, kaishaCd, false, false);

        // ログ出力用の部店情報の取得
        List<Buten> butenJohoList = butenRepository.getByKaishaCdAndValidPeriodStartDateLessThanEqualAndValidPeriodEndDateGreaterThanEqualAndDeleteFlgFalse(
                kaishaCd, wkkijunbi, wkkijunbi);

        // 判定用の在籍組織情報の取得
        ShumukenmuKbn shumukenmuKbn = null;
        LocalDate minkkijunbi = wkkijunbi.minusDays(1L);
        List<SoshikiKengenInfoDto> zaisekiSoshikiJohokijunbiList = new ArrayList<>();
        List<SoshikiKengenInfoDto> zaisekiSoshikiJohoZenkijunbiList = new ArrayList<>();

        // 引数・会社コード= 「100: 銀行」または「200:証券」の場合
        if (KaishaCd.BK.equals(kaishaCd) || KaishaCd.SC.equals(kaishaCd)) {
            // 在籍組織情報(基準日)取得
            zaisekiSoshikiJohokijunbiList = userRepository.getSoshikiKengenInfoByZaisekiSoshikiAndGenericCode(
                    kaishaCd, wkkijunbi, wkUserId, GenericCodeSetId.GSBC10002);
                    
            // 取得した組織権限情報に対応する部店コード、課グループコードを取得
            if (zaisekiSoshikiJohokijunbiList.size() !== 0) {
                zaisekiSoshikiJohokijunbiList = getButenCdAndKaGroupCd(zaisekiSoshikiJohokijunbiList);
            }

            // 在籍組織情報(前日)取得
            zaisekiSoshikiJohoZenkijunbiList = userRepository.getSoshikiKengenInfoByZaisekiSoshiki(
                    minkkijunbi, wkUserId, GenericCodeSetId.GSBC10002);

            // 取得した組織権限情報に対応する部店コード、課グループコードを取得
            if (zaisekiSoshikiJohoZenkijunbiList.size() !== 0) {
                zaisekiSoshikiJohoZenkijunbiList = getButenCdAndKaGroupCd(zaisekiSoshikiJohoZenkijunbiList);
            }

            // 「300: 信託」の場合
        } else if (KaishaCd.TR.equals(kaishaCd)) {
            // 在籍組織情報(基準日)取得
            zaisekiSoshikiJohokijunbiList = userRepository.getSoshikiKengenInfoByShoguAndZaisekiSoshiki(
                    kaishaCd, wkkijunbi, wkUserId, shumukenmuKbn);

            // 在籍組織情報(前日)取得
            zaisekiSoshikiJohoZenkijunbiList = userRepository.getSoshikiKengenInfoByShoguAndZaisekiSoshiki(
                    kaishaCd, minkkijunbi, wkUserId, shumukenmuKbn);
        }

        // 在籍組織情報(基準日)の承認権限区分を設定
        for (SoshikiKengenInfoDto zaisekiSoshikiJohokijunbi : zaisekiSoshikiJohokijunbiList) {
            Integer shokuiRank = null;
            if (Objects.nonNull(zaisekiSoshikiJohokijunbi.getShokuiRank())
                    && !"".equals(zaisekiSoshikiJohokijunbi.getShokuiRank())) {
                shokuiRank = Integer.parseInt(zaisekiSoshikiJohokijunbi.getShokuiRank());
                // 職位ランクに基づいて権限区分を取得
                ShokushuRankKengenKbnResultDto shokushuRankKengenKbnResultDto = getKengenKbnByShokushuRank(shokuiRank);
                zaisekiSoshikiJohokijunbi.setShoninKengenKbn(shokushuRankKengenKbnResultDto.getShoninKengenKbn().getCode());
            }
        }

        // 在籍組織情報(前日)の承認権限区分を設定
        for (SoshikiKengenInfoDto zaisekiSoshikiJohoZenkijunbi : zaisekiSoshikiJohoZenkijunbiList) {
            Integer shokuiRankZen = null;
            if (Objects.nonNull(zaisekiSoshikiJohoZenkijunbi.getShokuiRank())
                    && !"".equals(zaisekiSoshikiJohoZenkijunbi.getShokuiRank())) {
                shokuiRankZen = Integer.parseInt(zaisekiSoshikiJohoZenkijunbi.getShokuiRank());
                // 職位ランクに基づいて権限区分を取得
                ShokushuRankKengenKbnResultDto shokushuRankKengenKbnResultDto = getKengenKbnByShokushuRank(shokuiRankZen);
                zaisekiSoshikiJohoZenkijunbi.setShoninKengenKbn(shokushuRankKengenKbnResultDto.getShoninKengenKbn().getCode());
            }
        }

        // 在籍組織情報取得より不正データを検出
        if (Objects.nonNull(wkUserId)) {
            if (ZERO.equals(zaisekiSoshikiJohokijunbiList.size())
                    && ZERO.equals(zaisekiSoshikiJohoZenkijunbiList.size())) {
                log.warn(MessageSourceUtils.getMessage(
                        BizCommonDomainValidationErrorCode.DEBC00101, kijunbiString, jugyoinNo));
            }
        }

        // ワーニング件数
        resultDto.setWarnCnt(wkWarnCnt);

        // 在籍組織情報取得より対象ユーザを取得
        List<Long> wkUserIdList = new ArrayList<>();

        // 在籍組織情報 (基準日)よりユーザIDを取得
        for (SoshikiKengenInfoDto zaisekiSoshikiJohokijunbi : zaisekiSoshikiJohokijunbiList) {
            wkUserIdList.add(zaisekiSoshikiJohokijunbi.getUserId());
        }

        // 在籍組織情報 (前日) よりユーザIDを取得
        for (SoshikiKengenInfoDto zaisekiSoshikiJohoZenkijunbi : zaisekiSoshikiJohoZenkijunbiList) {
            wkUserIdList.add(zaisekiSoshikiJohoZenkijunbi.getUserId());
        }

        // ユーザIDリストを昇順に並び替え、グループ化
        List<Long> uniqueList = wkUserIdList.stream()
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        // 労務管理上司承認者権限更新
        for (Long userIdUnique : uniqueList) {
            // ワーク変数、剥奪対象フラグ
            boolean wkHakudatsuTaishoFlg = false;

            // ワーク変数・上司ロール
            String skJoshiRole = "";

            // ワーク変数、承認者ロール
            String wkShoninshaRole = "";

            // 上司付与対象部店リスト
            List<Long> wkJoshiFuyoTaishoButenList = new ArrayList<>();

            // 上司削除対象部店リスト
            List<Long> wkJoshiDeleteTaishoButenList = new ArrayList<>();

            // 承認者付与対象部店リスト
            List<Long> wkShoninshaFuyoTaishoButenList = new ArrayList<>();

            // 承認者削除対象部店リスト
            List<Long> wkShoninshaDeleteTaishoButenList = new ArrayList<>();

            // ワーク変数、有効期間(開始)
            LocalDate wkValidPeriodSd = null;

            // ワーク変数、有効期間(終了)
            LocalDate wkValidPeriodEd = null;

            // 対象ユーザの在籍組織情報を取得
            // 在籍組織情報取得(基準日)(対象ユーザ)
            List<SoshikiKengenInfoDto> zaisekiSoshikiJohokijunbiTaishoUserList = new ArrayList<>();

            // 在籍組織情報取得(前日)(対象ユーザ)
            List<SoshikiKengenInfoDto> zaisekiSoshikiJohoZenkijunbiTaishoUserList = new ArrayList<>();

            // 在籍組織情報取得(基準日)より対象のユーザ情報を指定
            for (SoshikiKengenInfoDto zaisekiSoshikiJohokijunbi : zaisekiSoshikiJohokijunbiList) {
                if (userIdUnique.equals(zaisekiSoshikiJohokijunbi.getUserId())) {
                    zaisekiSoshikiJohokijunbiTaishoUserList.add(zaisekiSoshikiJohokijunbi);
                }
            }

            // 在籍組織情報取得(前日)より対象のユーザ情報を指定
            for (SoshikiKengenInfoDto zaisekiSoshikiJohoZenkijunbi : zaisekiSoshikiJohoZenkijunbiList) {
                if (userIdUnique.equals(zaisekiSoshikiJohoZenkijunbi.getUserId())) {
                    zaisekiSoshikiJohoZenkijunbiTaishoUserList.add(zaisekiSoshikiJohoZenkijunbi);
                }
            }

           // 権限更新対象を取得。

// 在籍組織情報取得(基準日) (対象ユーザ) = 0件かつ在籍組織情報取得(前日) (対象ユーザ) >0件の場合、剥奪対象。
if (ZERO == zaisekiSoshikiJohokijunbiTaishoUserList.size()
    && ZERO < zaisekiSoshikiJohoZenkijunbiTaishoUserList.size()) {
    wkHakudatsuTaishoFlg = true;
}
// 在籍組織情報取得(基準日) (対象ユーザ) >0件かつ在籍組織情報取得(前日) (対象ユーザ) =0件の場合、付与対象を判定。
else if (ZERO < zaisekiSoshikiJohokijunbiTaishoUserList.size() 
         && ZERO == zaisekiSoshikiJohoZenkijunbiTaishoUserList.size()) {
    for (SoshikikengenInfoDto zaisekiSoshikiJohokijunbiTaishoUser : zaisekiSoshikiJohokijunbiTaishoUserList) {
        // 在籍組織情報取得(基準日)(対象ユーザ) (n)・承認権限区分 = 「01: 上司」の場合
        if (ShoninkengenKbn.JOSHI.getCode()
            .equals(zaisekiSoshikiJohokijunbiTaishoUser.getShoninkengenKbn())) {
            // 上司付与対象部店リストに追加
            wkJoshiFuyoTaishoButenList.add(zaisekiSoshikiJohokijunbiTaishoUser.getButenId());
            // 在籍組織情報取得(基準日) (対象ユーザ) (n)、承認権限区分 = 「02:承認者」の場合
            if (ShoninkengenKbn.SHONINSHA.getCode()
                .equals(zaisekiSoshikiJohokijunbiTaishoUser.getShoninkengenKbn())) {
            // 承認者付与対象部店リストに追加
                wkShoninshaFuyoTaishoButenList.add(zaisekiSoshikiJohokijunbiTaishoUser.getButenId());
            }
        }
    }
} else {
    // 剥奪対象を洗い出す。
    // 初始化两个SoshikikengenInfoDto对象kijunbiShumuDto和kijunbikenmuDto，用于存储主务和兼务1的部门信息。
    SoshikikengenInfoDto kijunbiShumuDto = new SoshikiKengenInfoDto();
    SoshikikengenInfoDto kijunbikenmuDto = new SoshikikengenInfoDto();

    // 在籍組織情報取得(基準日)(対象ユーザ)
    for (SoshikikengenInfoDto zaisekiSoshikiJohokijunbiTaishoUser : zaisekiSoshikiJohokijunbiTaishoUserList) {
        if (ShumukenmuKbn.SHUMU.equals(zaisekiSoshikiJohokijunbiTaishoUser.getShumukenmuKbn())) {
            // 在籍組織情報取得(基準日) (対象ユーザ)、主務兼務区分 「0:主務」
            kijunbiShumuDto = zaisekiSoshikiJohokijunbiTaishoUser;
            // 在籍組織情報取得(基準日) (対象ユーザ)、主務兼務区分 = 「1:兼務1」
            if (ShumukenmuKbn.KENMU1.equals(zaisekiSoshikiJohokijunbiTaishoUser.getShumukenmuKbn())) {
                kijunbikenmuDto = zaisekiSoshikiJohokijunbiTaishoUser;
            }
        }

        // 主務の部店を比較!
        // 比较主务部门ID和承认权限区分，如果这两项有其中一项与前日的数据不一致，设置剥夺对象wkHakudatsuTaishoFlg为true。
        if (!Objects.equals(kijunbiShumuDto.getButenId(), zenkiJunbiShumuDto.getButenId()) ||
            !Objects.equals(kijunbiShumuDto.getShoninkengenKbn(), zenkiJunbiShumuDto.getShoninkengenKbn())) {
            wkHakudatsuTaishoFlg = true;
        }

        // 兼務1の部店を比較。
        // 引数、会社コード「100:銀行」の場合
        // 如果公司代码为“BK”（银行），进一步比较兼务1的部门信息，与前日的数据不一致时，同样设置wkHakudatsuTaishoFlg为true。
        if (KaishaCd.BK.equals(kaishaCd)) {
            if (!Objects.equals(kijunbikenmuDto.getButenId(), zenkiJunbiKenmuDto.getButenId()) ||
                !Objects.equals(kijunbikenmuDto.getShoninkengenKbn(), zenkiJunbiKenmuDto.getShoninkengenKbn())) {
                wkHakudatsuTaishoFlg = true;
            }
        }
    }

    // 付与対象を洗い出す。

// 剥奪対象あり(ワーク、剥奪対象フラグ = true) の場合。
if (wkHakudatsuTaishoFlg) {
    for (SoshikikengenInfoDto zaisekiSoshikiJohokijunbiTaishoUser : zaisekiSoshikiJohokijunbiTaishoUserList) {
        // 在籍組織情報取得 (基準日) (対象ユーザ)、承認権限区分 = 「01:上司」の場合
        if (ShoninkengenKbn.JOSHI.getCode()
                .equals(zaisekiSoshikiJohokijunbiTaishoUser.getShoninkengenKbn()) 
            && judgeJoshiFuyoTaisho(zaisekiSoshikiJohokijunbiTaishoUser.getShikakuCd(), 
                                    zaisekiSoshikiJohokijunbiTaishoUser.getYakushokuCd()
                                    )) {
            wkJoshiFuyoTaishoButenList.add(zaisekiSoshikiJohokijunbiTaishoUser.getButenId());
        }
        // 在籍組織情報取得(基準日) (対象ユーザ)、承認権限区分 = 「02: 承認者」の場合
        if (ShoninkengenKbn.SHONINSHA.getCode()
                .equals(zaisekiSoshikiJohokijunbiTaishoUser.getShoninkengenKbn()) 
            && judgeShoninshaFuyoTaisho(zaisekiSoshikiJohokijunbiTaishoUser.getYakushokuCd(),
                                    zaisekiSoshikiJohokijunbiTaishoUser.getButenCd()                                
                                    )) {
            wkShoninshaFuyoTaishoButenList.add(zaisekiSoshikiJohokijunbiTaishoUser.getButenId());
        }
    }
}

} else {
    // 在籍組織情報取得 (基準日) (対象ユーザ)より兼務情報を取得。

    // 処理対象グループ(基準日)。
    List<ButenkengenTargetDto> shoriTaishokijunbiList = new ArrayList<>();

    // 処理対象グループ(前日)
    List<ButenkengenTargetDto> shoriTaishoZenkiJunbiList = new ArrayList<>();

    // 引数、会社コード= 「100: 銀行」の場合
    if (Kaishald.BK.equals(kaishaCd)) {
        for (SoshikikengenInfoDto zaisekiSoshikiJohokijunbiTaishoUser : zaisekiSoshikiJohokijunbiTaishoUserList) {
            // 在籍組織情報取得(基準日) (対象ユーザ)、主務兼務区分不等于 「0:主務」「1:兼務」「99:管理店番」
            if (!SHUMU_OR_KENMUI_OR_KANRI_TENBAN.contains(zaisekiSoshikiJohokijunbiTaishoUser.getShumukenmuKbn())) {
                // 処理対象グループ(基準日)↓
                ButenkengenTargetDto dto = new ButenkengenTargetDto();
                dto.setButenId(zaisekiSoshikiJohokijunbiTaishoUser.getButenId());
                dto.setShoninkengenKbn(zaisekiSoshikiJohokijunbiTaishoUser.getShoninkengenKbn());
                shoriTaishokijunbiList.add(dto);
            }
        }
    } else {
        for (SoshikikengenInfoDto zaisekiSoshikiJohokijunbiTaishoUser : zaisekiSoshikiJohokijunbiTaishoUserList) {
            // 在籍組織情報取得 (基準日) (対象ユーザ)、主務兼務区分不等于 「0:主務」「99:管理店番」
            if (!SHUMU_OR_KANRI_TENBAN.contains(zaisekiSoshikiJohokijunbiTaishoUser.getShumukenmuKbn())) {
                // 処理対象グループ(基準日)
                ButenkengenTargetDto dto = new ButenkengenTargetDto();
                dto.setButenId(zaisekiSoshikiJohokijunbiTaishoUser.getButenId());
                dto.setShoninkengenKbn(zaisekiSoshikiJohokijunbiTaishoUser.getShoninkengenKbn());
                shoriTaishoJunbiList.add(dto);
            }
        }
    }
}

    /**
     * 労務管理上司権限付与判定処理
     *
     * @param shikakuCd   資格コード
     * @param yakushokuCd 役職コード
     * @return 判定結果
     */
    private boolean judgeJoshiFuyoTaisho(String shikakuCd, String yakushokuCd) {
        if (Objects.isNull(shikakuCd)) {
            return false;
        }

        try {
            int shikakuNum = Integer.valueOf(shikakuCd);
            int yakushokuNum = Integer.valueOf(yakushokuCd);

            if ((shikakuNum == 15 && yakushokuNum >= 330) || shikakuNum >= 19) {
                return true;
            }
        } catch (NumberFormatException e) {
            // 資格コード、役職コードは数値として比較を行う為数値以外が来た場合、判定結果はfalseとする。
            return false;
        }

        return false;
    }

    /**
     * 労務管理承認者権限付与判定処理
     *
     * @param yakushokuCd 役職コード
     * @param butenCd     部店コード
     * @return 判定結果
     */
    private boolean judgeShoninshaFuyoTaisho(String yakushokuCd, String butenCd) {
        List<String> HonbuFuyoTaishoYakushokuCd = List.of(
                "010", "020", "030", "032", "035", "037", "040",
                "050", "060", "083", "100", "204");
        List<String> HonbuIgaiFuyoTaishoYakushokuCd = List.of(
                "010", "020", "030", "032", "035", "037", "040",
                "050", "060", "083");

        if ((butenCd.startsWith("6") && butenCd.length() == 4) ||
                (butenCd.startsWith("71") && butenCd.length() == 4) ||
                (butenCd.startsWith("72") && butenCd.length() == 4)) {
            return HonbuFuyoTaishoYakushokuCd.contains(yakushokuCd);
        } else {
            return HonbuIgaiFuyoTaishoYakushokuCd.contains(yakushokuCd);
        }
    }private boolean j··udgeShoninshaFuyoTaisho(String yakushokuCd, String butenCd) {
    List<String> HonbuFuyoTaishoYakushokuCd = List.of(
        "010", "020", "030", "032", "035", "037", "040", 
        "050", "060", "083", "100", "204"
    );
    List<String> HonbuIgaiFuyoTaishoYakushokuCd = List.of(
        "010", "020", "030", "032", "035", "037", "040", 
        "050", "060", "083"
    );

    if ((butenCd.startsWith("6") && butenCd.length() == 4) ||
        (butenCd.startsWith("71") && butenCd.length() == 4) ||
        (butenCd.startsWith("72") && butenCd.length() == 4)) {
        return HonbuFuyoTaishoYakushokuCd.contains(yakushokuCd);
    } else {
        return HonbuIgaiFuyoTaishoYakushokuCd.contains(yakushokuCd);
    }
}

// 对这个函数进行修改。如果butenCd是6xxx或71xx或72xx的情况下，判断的列表是HonbuFuyoTaishoYakushokuCd，如果butenCd是0xxx或1xxx或2xxx的情况下，判断的列表是HonbuIgaiFuyoTaishoYakushokuCd

// List<String> fuyoTaishogaiYakushokuCd = List.of(
// "011", "013", "038", "041", "100", "110", "111",
// "200", "201", "202", "203", "204", "205", "210",
// "211", "220", "221", "230", "231", "235", "236",
// "240", "241", "250", "251"
// );

















/**
 * 有給付与
 *
 * @param batchMode バッチモード
 * @param kaishaCd 会社コード
 * @param shoriDate 処理日付
 * @param logger ロガー
 * @return 処理カウンター
 */
@Transactional(readOnly = true) // 独自にコミットを実施
@Override
public RkCounter yukyuFuyo(BatchMode batchMode, KaishaCd kaishaCd, LocalDate shoriDate, Logger logger) {
    Integer nendo = RkDateUtils.getNendo(shoriDate);
    LocalDate kijunbi = BatchMode.NICHIJI.equals(batchMode) ? shoriDate : RkDateUtils.getFirstDateOfNendo(nendo);

    // 以降は基準日 (≒処理日付)で処理を行う
    List<UserJoho> fuyoTaishoList = ksYukyuFuyoRepository.getFuyoTaishoUser(batchMode, kaishaCd, kijunbi);
    entityManager.clear();

    // TODO: ↓テスト用ユーザ指定
    List<Long> LIMITED_USER_ID_LIST = List.of(
        52321L, 52322L, 52323L, 52324L, 52325L, 
        52326L, 52327L, 52328L, 52329L, 52330L, 
        52331L, 52332L, 52333L, 52334L, 52335L, 
        52336L, 52337L, 28901L, 36383L, 52340L, 52341L
    );
    Predicate<UserJoho> testUser = e -> LIMITED_USER_ID_LIST.contains(e.getUserId());
    fuyoTaishoList = fuyoTaishoList.stream()
        .filter(testUser)
        .collect(Collectors.toList());
    // TODO: ↑テスト用ユーザ指定

    RkCounter counter = new RkCounter(
        CNT_SHORI, CNT_MODIFY_NORMAL, CNT_MODIFY_WARN, 
        CNT_FAILED, CNT_WITHOUT_MODIFY, CNT_WITHOUT_FUYO
    );
    counter.set(CNT_SHORI, fuyoTaishoList.size());

    List<UserJoho> jugyoinList = new ArrayList<>();
    List<UserJoho> keiyakuShainList = new ArrayList<>();

    for (UserJoho fuyoTaisho : fuyoTaishoList) {
        if (EXCLUDE_JUGYOIN_NO.contains(fuyoTaisho.getJugyoinNo())) {
            counter.add(CNT_SHORI, -1);
            continue;
        }

        if (RkUserUtils.isKeiyakuShain(fuyoTaisho)) {
            keiyakuShainList.add(fuyoTaisho);
        } else {
            jugyoinList.add(fuyoTaisho);
        }
    }

    // 独自の entityManager を使用して、更新に失敗しても処理を続行
    EntityManager entityManagerForModify = entityManagerFactory.createEntityManager();
    try {
        ikatsuTaishogaiKoshin(entityManagerForModify, nendo);
        
        for (UserJoho jugyoin : jugyoinList) {
            jugyoinKoshin(entityManagerForModify, jugyoin, nendo, kijunbi, counter, logger);
        }
        
        for (UserJoho keiyakuShain : keiyakuShainList) {
            keiyakuShainKoshin(entityManagerForModify, keiyakuShain, nendo, kijunbi, counter, logger);
        }
    } finally {
        entityManagerForModify.close();
    }

    return counter;
}



/**
 * 休暇付与保留対象対象外一括更新
 *
 * @param entityManagerForModify エンティティマネージャ
 * @param nendo 年度
 */
@Transactional(readOnly = true) // 独自にコミットを実施
private void ikatsuTaishogaiKoshin(EntityManager entityManagerForModify, Integer nendo) {
    EntityTransaction transaction = null;

    try {
        transaction = entityManagerForModify.getTransaction();
        transaction.begin();

        // 去年度以前の休暇付与状態の"付与未済"を全て"対象外"へ更新
        List<KsKyukaFuyoHoryuTaisho> ksKyukaFuyoHoryuTaishoList = 
            ksKyukaFuyoHoryuTaishoService.setKsKyukaFuyoHoryuTaishoIkatsuTaishogai(
                entityManagerForModify, 
                nendo
            );

        updateTaishogai(entityManagerForModify, ksKyukaFuyoHoryuTaishoList);

        transaction.commit();

    } catch (Exception e) {
        if (Objects.nonNull(transaction)) {
            transaction.rollback();
        }
        // 本メソッドの Exception は処理を中断
        throw new RuntimeException(e);
    } finally {
        entityManagerForModify.clear();
        entityManager.clear();
    }
}


/**
 * 年度取得
 *
 * @param source 対象日
 * @return 年度
 */
public static Integer getNendo(LocalDate source) {
    return Integer.valueOf(source.minusMonths(4 - 1).getYear());
}


public LocalDate minusMonths(long monthsToSubtract) {
    return (monthsToSubtract == Long.MIN_VALUE) 
        ? plusMonths(Long.MAX_VALUE).plusMonths(1) 
        : plusMonths(-monthsToSubtract);
}













/**
 * 従業員(非契約社員)更新
 *
 * @param entityManagerForModify エンティティマネージャ
 * @param nendo 年度
 * @param user ユーザ
 * @param kijunbi 基準日
 * @param counter 処理カウンタ
 * @param logger ロガー
 */
@Transactional(readOnly = true) // 独自にコミットを実施
private void jugyoinKoshin(
    EntityManager entityManagerForModify, 
    UserJoho user, 
    Integer nendo, 
    LocalDate kijunbi,
    RkCounter counter, 
    Logger logger
) {
    EntityTransaction transaction = null;

    try {
        KojinKyukaJohoMstDto kojinKyukaJohoMstDto = kyukaNissuCalcService.createJugyoinKojinKyukaJohoMstDto(
            user, nendo, kijunbi
        );

        // 警告判定
        checkWarn(kojinKyukaJohoMstDto, user, nendo, logger);

        transaction = entityManagerForModify.getTransaction();
        transaction.begin();

        // 既に個人休暇情報マスタが存在するなら初期化する
        kyukaNissuCalcService.initKojinKyukaMst(
            entityManagerForModify, 
            user.getKaishaCd(), 
            user.getUserId(),
            nendo
        );

        // Dtoにセットした値をDBに反映する
        kyukaNissuCalcService.updateKojinKyukaMst(
            entityManagerForModify, 
            user.getKaishaCd(), 
            user.getUserId(),
            nendo, 
            kojinKyukaJohoMstDto
        );

        // 個人連続休暇情報マスタにレコードが無ければ作成する
        insertKojinRenzokuKyukaJoho(
            entityManagerForModify, 
            user, 
            nendo, 
            UserType.JUGYOIN
        );

        // 退職者の場合"付与未済"を全て"対象外"へ更新
        List<KsKyukaFuyoHoryuTaisho> ksKyukaFuyoHoryuTaishoList = 
            ksKyukaFuyoHoryuTaishoService.getKsKyukaFuyoHoryuTaisho(
                user.getUserId(),
                KsKyukaFuyoJotai.FUYO_MIZAI
            );
        updateTaishogai(entityManagerForModify, ksKyukaFuyoHoryuTaishoList);

        transaction.commit();

        counter.count(kojinKyukaJohoMstDto.hasWarn() ? CNT_MODIFY_WARN : CNT_MODIFY_NORMAL);
        if (kojinKyukaJohoMstDto.isWithoutFuyo()) {
            counter.count(CNT_WITHOUT_FUYO);
        }
    } catch (Exception e) {
        if (Objects.nonNull(transaction)) {
            transaction.rollback();
        }
        String message = exceptionMessage(e);
        logger.warn(MessageSourceUtils.getMessage(
            KsLogMessageId.KS10W003,
            String.valueOf(Objects.nonNull(user) ? user.getUserId() : "null"),
            message
        ));
        logger.error(message, e);
        logger.error("##### STACK TRACE #####");
        counter.count(CNT_FAILED);
    } finally {
        entityManagerForModify.clear();
        entityManager.clear();
    }
}












