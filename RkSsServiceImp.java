/**
 * 育休新規申請DB更新
 *
 * @param dto 新規申請DTO
 * @return メッセージリスト
 */
@Transactional(readOnly = true) // 独自にコミットを実施
@Override
public List<Pair<? extends MessageId, Object[]>> ikukyuShinkiShinsei(SsShinkiShinseiDto dto) {

    MailSendDto mailSendDto = new MailSendDto();
    EntityManager entityManager = factory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();

    try {
        SsShinseiJohokanri ssShinseiJohokanri = createShinseiJohokanri(dto);
        ssShinseiJohokanriRepository.insertSsShinseiJohokanri(entityManager, ssShinseiJohokanri);

        SsShinseiJoho ssShinseiJoho = createShinseiJohoshinkiShinsei(dto, ssShinseiJohokanri);
        ssShinseiJohoRepository.insertSsShinseiJoho(entityManager, ssShinseiJoho);

        kinmuhyoHanei(entityManager, ssShinseiJoho, DairiFlg.JINJI.equals(dto.getDairiFlg()));

        if (ShoninStatus.KANRYO.equals(ssShinseiJohokanri.getShoninStatus())) {
                // TODO: 2023/02/21 中西 短期育児休業承認時に出産日報告を不要にするか検討
                mailSendDto.add(rkMailService.saveShoninMail(entityManager, ssShinseiJohokanri, ssShinseiJoho));
                rkKkttService.shonin(entityManager, ssShinseiJohokanri, ssShinseiJoho);
        }
        transaction.commit();
    } catch (Exception e) {
        transaction.rollback();
        throw e;
    } finally {
        entityManager.close();
    }

    if (mailSendDto.hasMail()) {
        rkMailService.sendMail(mailSendDto);
    }

    return mailSendDto.getWarningMessageList();
}



/**
 * 承認登録、差し戻しと通常承認とをまとめたメソッド
 *
 * @param userId ユーザID
 * @param userName 承認者ユーザ名
 * @param ssShinseiJohokanriList 産休・育休申請情報管理リスト
 * @param sashimodoshiFlg 差し戻しフラグ
 * @param sashimodoshiComment 差戻しコメント
 * @param userOperation 職責
 * @return メッセージリスト
 */
@Transactional(readOnly = true) // 独自にコミットを実施
@Override
public List<Pair<? extends MessageId, Object[]>> ssshoninSashimodoshi(Long userId, String userName,
        List<SsShinseiJohokanri> ssShinseiJohokanriList, boolean sashimodoshiFlg, String sashimodoshiComment,
        String userOperation) {
    if (sashimodoshiFlg) {
        return ssSashimodoshi(userId, ssShinseiJohokanriList, sashimodoshiComment);
    } else {
        return ssShonin(userId, userName, ssShinseiJohokanriList, userOperation);
    }
}

@Transactional(readOnly = true) // 独自にコミットを実施
private List<Pair<? extends MessageId, Object[]>> ssShonin(Long userId, String userName,
        List<SsShinseiJohokanri> ssShinseiJohokanriList, String userOperation) {

    MailSendDto mailSendDto = new MailSendDto();
    EntityManager entityManager = factory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();

    try {
        for (SsShinseiJohokanri temp : ssShinseiJohokanriList) {
            SsShinseiJohokanri ssShinseiJohokanri = ssShinseiJohokanriRepository
                    .getBySsShinseiJohokanriId(entityManager, temp.getSsShinseiJohokanriId());
            SsShinseiJoho ssShinseiJoho = getSsShinseiJoho(entityManager, ssShinseiJohokanri);

            SsShinseiJoho ssShinseiJohoBefore = null;

            if (RkSsService.HR.equals(userOperation)) {
                // TODO: 2023/02/21 中西 短期育児休業承認時に出産日報告を不要にするか検討
                if (!SsShinseiKbn.SHINKI.equals(ssShinseiJoho.getSsShinseiKbn())) {
                    ssShinseiJohoBefore = ssShinseiJohoRepository
                            .getBySsShinseiJohoId(entityManager, ssShinseiJoho.getTaishoSsShinseiJohokanriId());
                    ssShinseiJohoBefore.delete();
                    ssShinseiJohoRepository.updateSsShinseiJoho(entityManager, ssShinseiJohoBefore);
                }
                ssShinseiJoho.setJinjibuShonin();
                ssShinseiJohoRepository.updateSsShinseiJoho(entityManager, ssShinseiJoho);
                ssShinseiJohokanri.setShonin(userId, userName, ssShinseiJoho.getAllTorikeshiFlg().getCode());
                ssShinseiJohokanriRepository.updateSsShinseiJohokanri(entityManager, ssShinseiJohokanri);

                if (ShoninStatus.KANRYO.equals(ssShinseiJohokanri.
                )) {
                )) {
                    if (SsShinseiKbn.TORIKESHI.equals(ssShinseiJoho.getSsShinseiKbn())
                            || SsShinseiKbn.ZENTORIKESHI.equals(ssShinseiJoho.getSsShinseiKbn())) {
                        kinmuhyoHanei(entityManager, ssShinseiJoho, false);
                    }
                    mailSendDto.add(rkMailService.saveShoninMail(entityManager, ssShinseiJohokanri, ssShinseiJoho));
                    if (!SsShinseiKbn.ZENTORIKESHI.equals(ssShinseiJoho.getSsShinseiKbn())) {
                        rkKkttService.shonin(entityManager, ssShinseiJohokanri, ssShinseiJoho);
                    }
                }
            }
        }
        transaction.commit();
    } catch (Exception e) {
        transaction.rollback();
        throw e;
    } finally {
        entityManager.close();
    }

    if (mailSendDto.hasMail()) {
        rkMailService.sendMail(mailSendDto);
    }

    return mailSendDto.getWarningMessageList();
}




/**
 * 週次・月次計算
 *
 * @param taishoYm 対象年月
 * @param kaishaCd 会社コード
 * @param userId ユーザID
 * @param gensekiKaishaCd 原籍会社コード
 * @param gensekiUserId 原籍ユーザID
 */
@Transactional
@Override
public void getsujiCalcWeek(String taishoYm, KaishaCd kaishaCd, Long userId, KaishaCd gensekiKaishaCd, Long gensekiUserId) {

    LocalDate kinmubiFrom = RkDateUtils.getDate(taishoYm + "01");
    LocalDate kinmubiTo = RkDateUtils.getLastDateOfMonth(kinmubiFrom);
    List<LocalDate> dateList = RkDateUtils.getDateList(kinmubiFrom, kinmubiTo);

    // TODO: 2022/9/22 篠崎 これ以降、calcWeekList は未使用、メソッド名の「週次」は何?
    // TODO: 2024/1/18 篠崎 kjCalcWeekService.calcWeek の戻りは「編集済の勤務日次情報リスト」だが
    // for ループの毎回の最後で更新が必要か?
    // calcWeekList から計算後の値を取り出したり、勤務日次情報の更新を行っていない

    List<KjKinmuNichijiJoho> calcWeekList = new ArrayList<>();
    List<LocalDate> kinmubiList = new ArrayList<>();

    for (LocalDate d : dateList) {
        if (kinmubiList.contains(null)) {
            continue;
        }
        if (RkListUtils.contains(calcWeekList, calcWeek -> RkDateUtils.isEqual(calcWeek.getKinmubi(), d))) {
            continue;
        }
        calcWeekList = kjCalcWeekService.calcWeek(userId, d);
            // calcWeekListに、値がnullの日次情報が入る場合があるため整理
            calcWeekList = calcWeekList.stream()
            .filter(calcWeek -> !ObjectUtils.isEmpty(calcWeek.getKinmubi()))
            .collect(Collectors.toList());

            kinmubiList = calcWeekList.stream()
            .map(calcWeek -> calcWeek.getKinmubi())
            .collect(Collectors.toList());
    }
    Consumer<String> getsujiKeisan = argTaishoYm -> {
        kinmuhyoShukeiService.kinmuGetsujiKeisan(kaishaCd, userId, argTaishoYm, gensekiKaishaCd, gensekiUserId);
        kinmuhyoShukeiService.kinmuGetsujiShukei(kaishaCd, userId, argTaishoYm);
    };

    getsujiKeisan.accept(taishoYm);

    // 月跨ぎでF区分が発生する場合があるため、対象年月翌月分も月次計算・集計
    getsujiKeisan.accept(RkDateUtils.getYearMonth(kinmubiFrom.plusMonths(1)));
}




/**
 * 月締め申請事前更新
 *
 * @param preShinseiDto 事前月締申請DTO
 */
@Transactional
@Override
public void preTsukijimeShinsei(TjPreTsukijimeShinseiDto preShinseiDto) {

    LocalDate kinmubiFrom = RkDateUtils.getDate(preShinseiDto.getTaishoYm() + "01");
    LocalDate kinmubiTo = RkDateUtils.getLastDateOfMonth(kinmubiFrom);

    List<KjKinmuNichijiJoho> kinmuNichijiJohos = kjKinmuNichijiJohoRepository
            .getByKaishaCdAndUserIdAndKinmubiBetweenAndDeleteFlgFalse(
                    preShinseiDto.getKaishaCd(),
                    preShinseiDto.getTaishoUserId(),
                    kinmubiFrom,
                    kinmubiTo
            );

    for (KjKinmuNichijiJoho kinmuNichijiJoho : kinmuNichijiJohos) {

        boolean needsSave = false;

        // TODO: 確認画面表示時に退職日は更新されないため、確認画面表示後に再度初めから処理を行うと値が不整合になる。

        if (!ObjectUtils.isEmpty(preShinseiDto.getTaishokubi())
                && RkDateUtils.isAscendingThan(preShinseiDto.getTaishokubi(), kinmuNichijiJoho.getKinmubi())) {

            rkKjService.setKinmuNashi(kinmuNichijiJoho);
            needsSave = true;
        }

        if (Objects.equals(kinmuNichijiJoho.getKinmuShoninJotai(), KinmuShoninJotai.MITEISHUTSU)) {
            kinmuNichijiJoho.setKinmuShoninJotai(KinmuShoninJotai.ICHIJIHOZON);
            needsSave = true;
        }

        if (needsSave) {
            kjKinmuNichijiJohoRepository.save(kinmuNichijiJoho);
        }
    }

    getsujiCalcWeek(
            preShinseiDto.getTaishoYm(),
            preShinseiDto.getKaishaCd(),
            preShinseiDto.getTaishoUserId(),
            preShinseiDto.getGensekiKaishaCd(),
            preShinseiDto.getGensekiUserId()
    );
}





/**
 * @param values DB参照結果
 * @param kaishaCd 会社コード
 * @param rkUserVRepository ユーザー情報ビューリポジトリ
 * @param teService 特別延長サービス
 */
public KmjsJikangaiKobetsuIchiRanhyoDto(Object[] values, KaishaCd kaishaCd, RkUserVRepository rkUserVRepository, RkTeService teService) {

    // CustomizedRkKmjsJikangaiKobetsuIchiRanhyoRepository.getJikangaiKobetsuIchiRanhyoの結果を格納
    int i = 0;
    Long userId = getLong(values[i++]);
    String bushitsuTenban = getString(values[i++]);
    String bushitsuTenmei = getString(values[i++]);
    String kaGroupMei = getString(values[i++]);

    RkUserV rkUserV = rkUserVRepository.getByRkUserKensaku(kaishaCd, userId, DateUtils.getCurrentDate());
    List<Object[]> userInfoList = rkUserVRepository.searchRkUserVByKaishaCdUserIdKijunbi(kaishaCd, userId, DateUtils.getCurrentDate());

    String jugyoshaMeisho;
    String courseMeisho;
    String yakushoku;
    String shikakuto;
    String jugyoinNo;
    String userName;

    if (!userInfoList.isEmpty()) {
        Object[] rkUserVInfo = userInfoList.get(0);
        jugyoshaMeisho = (String) rkUserVInfo[3];
        courseMeisho = (String) rkUserVInfo[4];
        yakushoku = (String) rkUserVInfo[5];
        shikakuto = (String) rkUserVInfo[1];
        jugyoinNo = (String) rkUserVInfo[6];
        userName = (String) rkUserVInfo[7];
// jugyoshaMeisho = rkUserV.getJugyoshaMeisho();

// courseMeisho = rkUserV.getCourseMeisho();

// yakushoku = rkUserV.getYakushokukbn(); // TODO: 役職デコード?

// shikakuto = rkUserV.getShikakuto();

// jugyoinNo = rkUserV.getJugyoinNo();

// userName = rkUserV.getUserName();
    } else {
        jugyoshaMeisho = null;
        courseMeisho = null;
        yakushoku = null;
        shikakuto = null;
        jugyoinNo = null;
        userName = null;
    }

    Shumukenmukbn shumukenmukon = getEnumCode(values[i++], Shumukenmukbn.class);
    String kenmuNaiyo = getString(values[i++]);
    Getsuji getsuji = new Getsuji(values, i);
    String nengetsu = getsuji.getNengetsu();
    Integer nendo = RkDateUtils.getNendo(RkDateUtils.getDate(nengetsu.getYear(), nengetsu.getMonthValue(), 1));

    SaburokukbnMst shokiSaburokukbnMst = teService.getShokiSaburokukbn(kaishaCd, userId, RkDateUtils.getLastDateOfNendo(nendo));
    String saburokukyoteiKubunShoki = getSaburokukbnMeisho(shokiSaburokukbnMst);

    TeShinseiJoho ikagetsuTeShinseiJoho = teService.getIkagetsuTeShinseiJoho(kaishaCd, userId, nendo, nengetsu.getMonthValue());
    SaburokukbnMst ikagetsuSaburokukbnMst = ObjectUtils.isEmpty(ikagetsuTeShinseiJoho) ? null : teService.getSaburokukbnMst(ikagetsuTeShinseiJoho);
    String saburokukyoteiKubunIkagetsuTokubetsu = getSaburokukbnMeisho(ikagetsuSaburokukbnMst);

    TeShinseiJoho ichinenTeShinseiJoho = teService.getIchinenTeShinseiJoho(kaishaCd, userId, nendo);
    SaburokukbnMst ichinenSaburokukbnMst = ObjectUtils.isEmpty(ichinenTeShinseiJoho) ? null : teService.getSaburokukbnMst(ichinenTeShinseiJoho);
    String saburokukyoteiKubunIchinenTokubetsu = getSaburokukbnMeisho(ichinenSaburokukbnMst);

    // TODO: 36協定対象時間
    String saburokukyoteiKubunTaishoJikan;

    // 労働時間合計
    Double rodoJikanHoteinai = getsuji.getHoteinai();
    Double rodoJikanHoteigai = getsuji.getHoteigai();
    Double rodoJikanKyujitsu = getsuji.getKyujitsu();
    Double rodoJikanSyuji = getsuji.getSyuji();
    Double rodoJikanRokujyuJikanChou = getsuji.getRokujyuJikanChou();
    Double rodoJikanHeijitsuSinya = getsuji.getHeijitsuSinya();
    Double rodoJikanKyujitsuSinya = getsuji.getKyujitsuSinya();
    Double rodoJikanJikangaiGokei = getsuji.getJikangaiGokei();
    Double rodoJikanHoteigaiGokei = getsuji.getHoteigaiGokei();
    Double rodoJikanHoteigaiIkagetsuRuikei = getsuji.getHoteigaiIkagetsuRuikei();
    Integer rodoJikanIkagetsuEnchokaisu;
    Double rodoJikanHoteigaiIchinenRuikei = getsuji.getHoteigaiIchinenRuikei();
    Double rodoJikanHoteigaiGokeikaigai = getsuji.getHoteigaiGokeikaigai();
    Double rodoJikanHoteigaiIchinenRuikeikaigai = getsuji.getHoteigaiIchinenRuikeikaigai();

    if (!isZeroHourMinute(rodoJikanHeijitsuSinya) || !isZeroHourMinute(rodoJikanKyujitsuSinya)) {
        String keikoku1 = "ASTERISK";
    }

    Integer hoteigaiIkagetsuRuikeiInt = getsuji.getHoteigaiIkagetsuRuikeiInt();
    Integer hoteigaiIchinenRuikeiInt = getsuji.getHoteigaiIchinenRuikeiInt();

    Integer ikagetsuGendo = getIkagetsuGendo(shokiSaburokukbnMst, ikagetsuSaburokukbnMst);
    if (!ObjectUtils.isEmpty(ikagetsuGendo) && !ObjectUtils.isEmpty(hoteigaiIkagetsuRuikeiInt) && RkNumberUtils.isAscendingThan(0, hoteigaiIkagetsuRuikeiInt)) {
        Double ikagetsuGendoTtsDbl = twoThirds(ikagetsuGendo);
        Double hoteigaiIkagetsuRuikeiDbl = Double.valueOf(hoteigaiIkagetsuRuikeiInt.toString());
        if (RkNumberUtils.isAscendingThan(ikagetsuGendoTtsDbl, hoteigaiIkagetsuRuikeiDbl)) {
            String keikoku2 = "ASTERISK";
        }
    }

    Integer ichinenGendo = getIchinenGendo(shokiSaburokukbnMst, ichinenSaburokukbnMst);
    if (!ObjectUtils.isEmpty(ichinenGendo) && !ObjectUtils.isEmpty(hoteigaiIchinenRuikeiInt) && RkNumberUtils.isAscendingThan(0, hoteigaiIchinenRuikeiInt)) {
        Double ichinenGendoTtsDbl = twoThirds(ichinenGendo);
        Double hoteigaiIchinenRuikeiDbl = Double.valueOf(hoteigaiIchinenRuikeiInt.toString());
        if (RkNumberUtils.isAscendingThan(ichinenGendoTtsDbl, hoteigaiIchinenRuikeiDbl)) {
            String keikoku3 = "ASTERISK";
        }
        if (RkNumberUtils.isAscendingThan(ichinenGendo, hoteigaiIchinenRuikeiInt)) {
            String keikoku4 = "ASTERISK";
        }
    }

    Integer shokiIkagetsuGendo = getIkagetsuGendo(shokiSaburokukbnMst);
    String keikoku5 = acquireKeikoku56(ikagetsuTeShinseiJoho, shokiIkagetsuGendo, ikagetsuGendo, hoteigaiIkagetsuRuikeiInt);

    Integer shokiIchinenGendo = getIchinenGendo(shokiSaburokukbnMst);
    String keikoku6 = acquireKeikoku56(ichinenTeShinseiJoho, shokiIchinenGendo, ichinenGendo, hoteigaiIchinenRuikeiInt);

    String kanrikbn = getsuji.getKanriKbn();
    String sairyoflg = getsuji.getSairyoFlg();
}





@Service
public class RkKkttServiceImpl implements RkKkttService {

    // PDF 定義変更時の対応

    // ・PDF 定義ファイルを別名 (･･･ YYYYMMDD.xml,vN.xml etc.) で新規作成

    // ・上記に併せて *** PDF DEFINE_PATH を別名で追加
    // ・可変項目に変更ありの場合、上記に併せて pdf***Map 用の ***PdfDto を新規作成

    // ・新 xxxPdfDto に併せて buildPdf Values を修正

    // pdfDefinePathGetter のリターンは最新の *** PDF_DEFINE_PATHに変更(旧は含めない)

    // pdfxxxMap は新・旧両方のキーを定義

    // ※日付等を条件に pdfDefinePathGetter のリターンを切り替えることも可能だが、避けた方が良い
    // *rk_kktt_kakunin_ketei_tsuchi_tbl.pdf_define_path に旧の値が存在しなくなるまで旧は残す

    private static final String KAIGO_SHINSEI_PDF_DEFINE_PATH = "/rk/kktt/KaigoShinseiPdfDefine.xml";

    private static final String SANKYU_IKUKYU_SHINSEI_PDF_DEFINE_PATH = "/rk/kktt/SankyulkukyuShinseiPdfDefine.xml";

    private static final String TANJIKAN_SHINSEI_IKUJI_PDF_DEFINE_PATH = "/rk/kktt/TanjikanShinseiIkujiPdfDefine.xml";

    private static final String TANJIKAN_SHINSEI_KAIGO_PDF_DEFINE_PATH = "/rk/kktt/TanjikanShinseikaigoPdfDefine.xml";

    private static final Function<BaseDomain<Long>, String> pdfDefinePathGetter = shinseiJoho -> {
        if (shinseiJoho instanceof KkssShinseiJoho) {
            return KAIGO_SHINSEI_PDF_DEFINE_PATH;
        } else if (shinseiJoho instanceof SsShinseiJoho) {
            return SANKYU_IKUKYU_SHINSEI_PDF_DEFINE_PATH;
        } else if (shinseiJoho instanceof TsShinseiJoho) {
            if (TsShinseiShubetsu.IKUJI.equals(((TsShinseiJoho) shinseiJoho).getShinseiShubetsu())) {
                return TANJIKAN_SHINSEI_IKUJI_PDF_DEFINE_PATH;
            } else if (TsShinseiShubetsu.KAIGO.equals(((TsShinseiJoho) shinseiJoho).getShinseiShubetsu())) {
                return TANJIKAN_SHINSEI_KAIGO_PDF_DEFINE_PATH;
            }
            throw new RuntimeException("未サポート");
        } else {
            throw new RuntimeException("未サポート");
        }
    };

    private static final Map<String, String> pdfResourceNameMap = Map.of(
            KAIGO_SHINSEI_PDF_DEFINE_PATH, KkttkaigoShinseiPdfDto.RESOURCE_NAME,
            SANKYU_IKUKYU_SHINSEI_PDF_DEFINE_PATH, KkttSankyulkukyuShinseiPdfDto.RESOURCE_NAME,
            TANJIKAN_SHINSEI_IKUJI_PDF_DEFINE_PATH, KkttTanjikanShinseiPdfDto.RESOURCE_NAME,
            TANJIKAN_SHINSEI_KAIGO_PDF_DEFINE_PATH, KkttTanjikanShinseiPdfDto.RESOURCE_NAME
    );

    private static final Map<String, Class<?>> pdfDtoMap = Map.of(
            KAIGO_SHINSEI_PDF_DEFINE_PATH, KkttkaigoShinseiPdfDto.class,
            SANKYU_IKUKYU_SHINSEI_PDF_DEFINE_PATH, KkttSankyulkukyuShinseiPdfDto.class,
            TANJIKAN_SHINSEI_IKUJI_PDF_DEFINE_PATH, KkttTanjikanShinseiPdfDto.class,
            TANJIKAN_SHINSEI_KAIGO_PDF_DEFINE_PATH, KkttTanjikanShinseiPdfDto.class
    );

    private static final Map<KaishaCd, String> respHrMap = Map.of(
            KaishaCd.BK, "RESP_MUBK_HR",
            KaishaCd.SC, "RESP_MUMSS_HR",
            KaishaCd.TR, "RESP_MUTR_HR"
    );

    // TODO: 2022/2/26 篠崎 セパレータに指定した文字は、画面からは入力不可にする必要有
    // ※これを変更すると、変更以前のkkttkakuninketeiTsuchi.pdfValuesを正しくマッピングできなくなる

    private static final String SEP1 = "^";
    private static final String SEP2 = "`";

    /** {@link RkKkttkakuninketeiTsuchiRepository} */
    @Autowired
    private RkKkttkakuninketeiTsuchiRepository kkttkakuninketeiTsuchiRepository;

    /** {@link RkTsShinseiJohoRepository} */
    @Autowired
    private RkTsShinseiJohoRepository tsShinseiJohoRepository;

    /** {@link RkUserJohoRepository} */
    @Autowired
    private RkUserJohoRepository userJohoRepository;

    /** {@link ZaisekiSoshikiRepository} */
    @Autowired
    private ZaisekiSoshikiRepository zaisekiSoshikiRepository;

    /** {@link GenericCodeRepository} */
    @Autowired
    private GenericCodeRepository genericCodeRepository;

    /** {@link CodeRepository} */
    @Autowired
    private CodeRepository codeRepository;

    /** {@link RkPdfGeneratorFactory} */
    @Autowired
    private RkPdfGeneratorFactory pdfGeneratorFactory;
}

private String buildPdfValues(ApplicationDataDto source) {

    try {
        List<String> pdfValues = new ArrayList<>();

        for (Field field : source.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(BizStreamHeaderName.class)) {
                BizStreamHeaderName a = field.getAnnotation(BizStreamHeaderName.class);

                field.setAccessible(true);

                pdfValues.add(String.format("%s%s%s", a.value(), SEP1, nullToBlank(field.get(source))));
            }
        }

        return pdfValues.stream().collect(Collectors.joining(SEP2));

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}