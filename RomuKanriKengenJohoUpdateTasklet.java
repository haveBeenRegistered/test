/**
 * 労務管理権限情報更新処理を行うタスクレット
 * 
 * @autor HITACHI-eitsubo
 */
public class RomukanriKengenJohoUpdateTasklet implements Tasklet {

    /** {@link Auth2Service} */
    @Autowired
    private Auth2Service auth2Service;

    private static final Logger log = MjfLoggerFactory.getLogger(RomukanriKengenJohoUpdateTasklet.class);

    /** バッチ引数:会社コード */
    private static final String BATCH_KEY_KAISHACD = "kaishaCd";

    /** バッチ引数:処理区分 */
    private static final String BATCH_KEY_SHORIKBN = "shorikbn";

    /** バッチ引数:基準日 */
    private static final String BATCH_KEY_KIJUNBI = "kijunbi";

    /** バッチ引数:従業員番号 */
    private static final String BATCH_KEY_JUGYOINNO = "jugyoinNo";

    /** 会社コード */
    private static final String KAISHACD_MESSAGE = "会社コード";

    /** 処理区分 */
    private static final String SHORIKBN_MESSAGE = "処理区分";

    /** 基準日 */
    private static final String KIJUNBI_MESSAGE = "基準日";

    /** 従業員番号 */
    private static final String JUGYOINNO_MESSAGE = "従業員番号";

    /** 処理区分:01:上司承認者権限 */
    private static final String JOSHISHONINSHA_KENGEN = "01";

    /** 処理区分:02:人事部権限 */
    private static final String JINJIBU_KENGEN = "02";

    /** 処理区分:03:従業員付与 */
    private static final String EMP_FUYO = "03";

    /** 数値:8 */
    private static final Integer EIGHT = 8;

    /** データパターン:日付 */
    private static final List<Pattern> DATE_PATTERNS = Arrays.asList(
        Pattern.compile("^(\\d{4})/(\\d{1,2})/(\\d{1,2})$"),
        Pattern.compile("^(\\d{4})年(\\d{1,2})月(\\d{1,2})日$"),
        Pattern.compile("^(\\d{4}) (\\d{2}) (\\d{2})$")
    );

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobParameters params = chunkContext.getStepContext().getStepExecution().getJobParameters();

        int taishoUserCnt = 0;
        int insertCnt = 0;
        int deleteCnt = 0;
        int warnCnt = 0;

        // 引数の会社コードを取得
        final String inputKaishaCd = params.getString(BATCH_KEY_KAISHACD);
        KaishaCd kaishaCd = EnumUtils.toEnum(KaishaCd.class, inputKaishaCd);

        // 会社コードチェック処理
        // 引数、会社コードに値がない (Null値・ブランク・空文字)の場合
        if (ObjectUtils.isEmpty(kaishaCd)) {
            // 引数、会社コードが不正な場合エラーログを出力し処理を終了させる。
            log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00101, KAISHACD_MESSAGE));
            return RepeatStatus.FINISHED;
        }

        // 引数の処理区分を取得
        String inputShoriKbn = params.getString(BATCH_KEY_SHORIKBN);

        // 処理区分チェック処理
        // 引数、処理区分≠「01:上司承認者権限」かつ、「02:人事部権限」かつ、「03:従業員付与」の場合
        if (!JOSHISHONINSHA_KENGEN.equals(inputShoriKbn) && !JINJIBU_KENGEN.equals(inputShoriKbn) && !EMP_FUYO.equals(inputShoriKbn)) {
            // 引数、処理区分が不正な場合エラーログを出力し処理を終了させる。
            log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00101, inputShoriKbn, SHORIKBN_MESSAGE));
            return RepeatStatus.FINISHED;
        }

        // 引数の基準日を取得
        String inputKijunbi = params.getString(BATCH_KEY_KIJUNBI);
        LocalDate wkkijunbi = null;

        // 基準日チェック処理
        // 引数、基準日に値がない (Null値・ブランク・空文字)の場合
        if (ObjectUtils.isEmpty(inputKijunbi)) {
            // 引数、基準日が年月日形式ではない場合
            // ワーク、基準日にシステム日付を設定。
            wkkijunbi = DateUtils.getCurrentDate();
        } else if (!EIGHT.equals(inputKijunbi.length()) || !isDate(inputKijunbi)) {
            // 引数、基準日が不正な場合エラーログを出力し処理を終了させる。
            log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00101, inputKijunbi, KIJUNBI_MESSAGE));
            return RepeatStatus.FINISHED;
        } else {
            // ワーク、基準日に引数、基準日を設定。
            wkkijunbi = getDate(inputKijunbi);
        }

        // 引数の従業員番号を取得
        String inputJugyoinNo = params.getString(BATCH_KEY_JUGYOINNO);

        // 従業員番号チェック処理
        if (!ObjectUtils.isEmpty(inputJugyoinNo)) {
            // 引数、従業員番号が半角英数字ではない場合
            if (!isAlphanumericHankaku(inputJugyoinNo)) {
                // 引数、従業員番号が不正な場合エラーログを出力し処理を終了させる。
                log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00101, inputJugyoinNo, JUGYOINNO_MESSAGE));
                return RepeatStatus.FINISHED;
            }

            // 存在チェック
            if (!auth2Service.existsUserByJugyoinNoAndKijunbi(kaishaCd, inputJugyoinNo, wkkijunbi)) {
                // 引数、従業員番号に該当するユーザーが存在しない場合エラーログを出力し処理を終了させる。
                log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00003, kaishaCd.getCode(), inputJugyoinNo));
                return RepeatStatus.FINISHED;
            }
        }

        // 労務管理権限情報を作成
        // 基準日をString型に変換
        String kijunbi = DateTimeFormatter.ofPattern("yyyyMMdd").format(wkkijunbi);

        if (JOSHISHONINSHA_KENGEN.equals(inputShoriKbn)) {
            // 上司承認者権限の権限情報を更新
            JoshiShoninshakengenUpdateResultDto joshiShoninshakengenUpdateResultDto = auth2Service.updateJoshiShoninshakengen(kaishaCd, kijunbi, inputJugyoinNo, log);

            // 労務管理上司承認者権限情報更新処理結果Dto、処理結果=000(正常終了)の場合
            if (JoshiShoninshakengenUpdateResultStatus.RTN_NORMAL_000.equals(joshiShoninshakengenUpdateResultDto.getResultStatus())) {
                taishoUserCnt = joshiShoninshakengenUpdateResultDto.getTaishoUserCnt();
                insertCnt = joshiShoninshakengenUpdateResultDto.getInsertCnt();
                deleteCnt = joshiShoninshakengenUpdateResultDto.getDeleteCnt();
                warnCnt = joshiShoninshakengenUpdateResultDto.getWarnCnt();
            } else {
                log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00102, kaishaCd.getCode(), kijunbi, inputShoriKbn, inputJugyoinNo));
                return RepeatStatus.FINISHED;
            }
        } else if (JINJIBU_KENGEN.equals(inputShoriKbn)) {
    // 人事部権限の権限情報を更新。
    JinjibukengenUpdateResultDto jinjibukengenUpdateResultDto = auth2Service.updateJinjibukengen(kaishaCd, kijunbi, inputJugyoinNo, log);

    // 労務管理人事部権限更新処理結果Dto,処理結果=000(正常終了)の場合
    if (JinjibukengenUpdateResultStatus.RTN_NORMAL_000.equals(jinjibukengenUpdateResultDto.getResultStatus())) {
        taishoUserCnt = jinjibukengenUpdateResultDto.getTaishoUserCnt();
        insertCnt = jinjibukengenUpdateResultDto.getInsertCnt();
        deleteCnt = jinjibukengenUpdateResultDto.getDeleteCnt();
        warnCnt = jinjibukengenUpdateResultDto.getWarnCnt();
    } else {
        log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00102, kaishaCd.getCode(), kijunbi, inputShoriKbn, inputJugyoinNo));
        return RepeatStatus.FINISHED;
    }
} else if (EMP_FUYO.equals(inputShoriKbn)) {
    // 引数、処理区分 = 「03:従業員付与」
    // 従業員の権限情報を更新
    EmpKengenUpdateResultDto empKengenUpdateResultDto = auth2Service.updateEmpKengen(kaishaCd, kijunbi, inputJugyoinNo, log);

    // 労務管理従業員権限更新処理結果Dto,処理結果=000 (正常終了)の場合
    if (EmpKengenUpdateResultStatus.RTN_NORMAL_000.equals(empKengenUpdateResultDto.getResultStatus())) {
        taishoUserCnt = empKengenUpdateResultDto.getTaishoUserCnt();
        insertCnt = empKengenUpdateResultDto.getInsertCnt();
        deleteCnt = 0;
        warnCnt = 0;
    } else {
        log.warn(MessageSourceUtils.getMessage(BizCommonBatchValidationErrorCode.BEBC00102, kaishaCd.getCode(), kijunbi, inputShoriKbn, inputJugyoinNo));
        return RepeatStatus.FINISHED;
    }
}

log.info(MessageSourceUtils.getMessage(BcBatchLogMessageId.BMBC00101,
    String.valueOf(taishoUserCnt), String.valueOf(insertCnt), String.valueOf(deleteCnt), String.valueOf(warnCnt), kaishaCd.getCode(), kijunbi, inputJugyoinNo));

return RepeatStatus.FINISHED;
}