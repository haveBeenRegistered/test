@Override
public void koshin(EntityManager entityManager) {
    for (IktkJikangaiCsvRecord record : dto.getIktkJikangaiCsvRecordList()) {
        RkUserV user = userVRepository.getByKaishaCdAndJugyoinNo(dto.getKaishaCd(), record.getJugyoinNo());

        KjKinmuNichijiJoho registered = getKjKinmuNichijiJoho(entityManager, user.getRkUserVId().getUserId(), user.getKaishaCd(), record);
        Consumer<KjKinmuNichijiJoho> nichijiEditor = createKjKinmuNichijiJohoEditor(record, user);

        if (Objects.isNull(registered)) {
            // TODO: 2020/01/14 篠崎
            // rk_ki_kinmu_nichiji_joho_tbl_YYYYMM が存在しない年月を指定すると
            // Exception が発生 (オンライン画面、取込実行の時点でチェック?)
            kjKinmuNichijiJohoRepository.insertKjKinmuNichijiJoho(entityManager, nichijiEditor);
        } else {
            kjKinmuNichijiJohoRepository.updateKjKinmuNichijiJoho(entityManager, registered.getKjKinmuNichijiJohoId(), nichijiEditor);
        }

        List<KjKinmuGetsujiJoho> kjKinmuGetsujiJohoList = kjKinmuGetsujiJohoRepository.getByKaishaCdAndUserIdAndTaishoYmAndDeleteFlgFalse(
            entityManager, user.getKaishaCd(), user.getRkUserVId().getUserId(), record.getTaishoYm()
        );

        if (ObjectUtils.isEmpty(kjKinmuGetsujiJohoList)) {
            Consumer<KjKinmuGetsujiJoho> getsujiEditor = createKjKinmuGetsujiJohoEditor(record, user);
            kjKinmuGetsujiJohoRepository.insertKjKinmuGetsujiJoho(entityManager, getsujiEditor);
        } else {
            for (KjKinmuGetsujiJoho g : kjKinmuGetsujiJohoList) {
                Consumer<KjKinmuGetsujiJoho> getsujiEditor = kjKinmuGetsujiJoho -> {
                    kjKinmuGetsujiJoho.setIkatsuFlg(Boolean.TRUE);
                };
                kjKinmuGetsujiJohoRepository.updateKjKinmuGetsujiJoho(entityManager, g.getKjKinmuGetsujiJohoId(), getsujiEditor);
            }
        }

        List<KjKinmuNichijiJoho> kjKinmuNichijiJohoList = kjKinmuNichijiJohoRepository.getByKaishaCdAndUserIdAndKinmubiBetweenAndMeisaiKbnAndDeleteFlgFalse(
            entityManager, user.getKaishaCd(), user.getRkUserVId().getUserId(), record.getFirstDateOfTaishoYm(), record.getLastDateOfTaishoYm(), MeisaiKbn.TSUJO
        );

        for (KjKinmuNichijiJoho n : kjKinmuNichijiJohoList) {
            Consumer<KjKinmuNichijiJoho> kknjEditor = kjKinmuNichijiJoho -> {
                kjKinmuNichijiJoho.setIkatsuFlg(Boolean.TRUE);
            };
            kjKinmuNichijiJohoRepository.updateKjKinmuNichijiJoho(entityManager, n.getKjKinmuNichijiJohoId(), kknjEditor);

            if (Objects.nonNull(n.getKjShinseiJohokanri())) {
                Consumer<KjShinseiJohokanri> ksjkEditor = kjShinseiJohokanri -> {
                    kjShinseiJohokanri.setSaishuShoninshaUserId(dto.getUserId());
                };
                kjShinseiJohokanriRepository.updateKjShinseiJohokanri(entityManager, n.getKjShinseiJohokanri().getKjShinseiJohokanriId(), ksjkEditor);
            }
        }

        Consumer<IktkTeatekaisuJoho> itkjEditor = createIktkTeatekaisuJohoEditor(record, user);

        if (Objects.isNull(registered)) {
            iktkTeatekaisuJohoRepository.insertIktkTeatekaisuJoho(entityManager, itkjEditor);
        } else {
            iktkTeatekaisuJohoRepository.updateIktkTeatekaisuJoho(entityManager, registered.getIktkTeatekaisuJohoId(), itkjEditor);
        }

        addTaishosha(user, record.getTaishoYm());
    }

    kyotsukoshin(entityManager);
}



/**
 * CSVデータ反映処理
 *
 * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
 * @param iktkKanrild 一括取込管理ID
 */
@Async // ※非同期実行
@Transactional(readOnly = true) // ※独自の commit を行うため readOnly = true をセット
@Override
public void execIkatsuTorikomiAsync(IktkTorikomishoriDto iktkTorikomishoriDto, Long iktkkanrild) {
    if (log.isDebugEnabled()) {
        log.debug("★★ {} Enter", RkLoggingUtils.getCurrentMethodName());
    }

    EntityManager entityManager = null;
    EntityTransaction transaction = null;

    // RK_GSRK00018_CD_1 の else で処理されるのは RK_GSRK00018_CD_2のみ、
    // RK_GSRK00018_CD_3 は別途 execKyukaJohoTorikomiAsync で処理
    IkatsuTorikomiBase ikatsuTorikomi = RkConstants.RK_GSRK00018_CD_1.equals(iktkTorikomishoriDto.getTorikomiTaisho())
        ? new JikangaiIkatsuTorikomi(iktkTorikomishoriDto)
        : new ShinyaJikangaiIkatsuTorikomi(iktkTorikomishoriDto);

    try {
        // 「業務エラー」 時は IkatsuTorikomiCheckException を throw
        ikatsuTorikomi.check();

        entityManager = entityManagerFactory.createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        ikatsuTorikomi.koshin(entityManager);
        ikatsuTorikomi.normalFinish(entityManager, iktkkanrild);

        transaction.commit();
    } catch (Exception e) {
        if (Objects.nonNull(transaction)) {
            transaction.rollback();
        }
        // 「業務エラー」以外
        if (!(e instanceof IkatsuTorikomiCheckException)) {
            ikatsuTorikomi.setSystemError(e);
        }
        // 本メソッドの entityManager から独立した commit コントロール
        ikatsuTorikomi.errorFinish(iktkkanrild);
            // 记录异常的堆栈追踪信息
    log.error("Exception occurred during execIkatsuTorikomiAsync", e);
    } finally {
        if (Objects.nonNull(entityManager)) {
            entityManager.close();
        }
        if (log.isDebugEnabled()) {
            log.debug("★★ {} Exit", RkLoggingUtils.getCurrentMethodName());
        }
    }
}



} catch (Exception e) {

    // TODO: スタックトレースの扱い
    log.error(e.getMessage(), e);

    if (transaction != null) {
        transaction.rollback();
    }

    status.addSystemErrorMessage("システムエラーが発生しました。DBの更新はありません。システム管理者に問い合わせてください。");

    String message = e.getMessage();
    if (StringUtils.isEmptyOrWhitespace(message)) {
        message = e.getClass().getName();
    }
    status.addSystemErrorMessage(message);

    torikomiError(
        iktkkanrild,
        status.getErrorMessageList(),
        status.getWarningMessageList(),
        status.getKensuErrorMessageList(),
        status.getSystemErrorMessageList(),
        iktkTorikomishoriDto,
        status.getShoriCount(),
        status.getNgCount(),
        status.getKensuShoriCount(),
        status.getKensuNgCount()
    );

    if (entityManager != null) {
        entityManager.close();
    }

    if (log.isDebugEnabled()) {
        log.debug("★★ {} Exit", RkLoggingUtils.getCurrentMethodName());
    }
}

/**
 * 渡された勤務日が属する週のF区分、管理監督者外週次、管理監督者週次、
 * 割増対象時間外時間、法定外合計を計算し、勤務日次情報リストを返す
 * ※独自のトランザクションコントロールを行う場合に使用
 *
 * @param entityManager エンティティマネージャ
 * @param userId ユーザID
 * @param kinmubi 勤務日
 * @return 日次情報リスト
 */
List<KjKinmuNichijiJoho> calcWeek(EntityManager entityManager, Long userId, LocalDate kinmubi);

/**
 * 渡された日次情報リストのF区分、管理監督者外週次、管理監督者週次、
 * 割増対象時間外時間、法定外合計を計算する
 * ※独自のトランザクションコントロールを行う場合に使用
 *
 * @param entityManager エンティティマネージャ
 * @param nichijiJohos 日次情報リスト
 */
@Transactional(readOnly = true)
@Override
public <T extends BaseKinmuNichijiJoho> void calcWeek(EntityManager entityManager, List<T> nichijiJohos) {
    if (ObjectUtils.isEmpty(nichijiJohos)) {
        return;
    }

    KaishaCd kaishaCd = RkListUtils.first(nichijiJohos).getKaishaCd();
    Long userId = RkListUtils.first(nichijiJohos).getUserId();
    LocalDate taishobiFrom = RkListUtils.first(nichijiJohos).getKinmubi();

    // TODO: 2024/01/18 篠崎 taishobiTo に対して RkListUtils.first は適切?
    // 一日分のみであるなら、明示的にコメントを付与して、keisan(･･･、の引数に taishobiFrom を2つセットすべき
    LocalDate taishobiTo = RkListUtils.first(nichijiJohos).getKinmubi();

    keisan(entityManager, nichijiJohos, userId, kaishaCd, taishobiFrom, taishobiTo);
}




@Transactional(readOnly = true)
@Override
public List<Pair<? extends MessageId, Object[]>> checkShoninIchiranMeisaiShokai(KjktShoninIchiranDto dto) {
    List<Pair<? extends MessageId, Object[]>> messages = new ArrayList<>();

    // checkShoninIchiranShonin と同等のチェック
    kjKinmuGetsujiJohoCheck(dto, messages);

    KjktKinmuTeiseiShinseiJokyo kjktKinmuTeiseiShinseiJokyo = getKjktKinmuTeiseiShinseiJokyo(dto.getKjShinseiJohokanriId());

    List<KjktKinmuNichijiJohoTeiseiBefore> kjktKinmuNichijiJohoTeiseiBeforeList = kjktKinmuNichijiJohoTeiseiBeforeRepository
        .getByKaishaCdAndUserIdAndTaishoYmAndTeiseiJohoSequenceNoAndDeleteFlgFalse(
            dto.getKaishaCd(), dto.getUserId(), dto.getTaishoYm(), kjktKinmuTeiseiShinseiJokyo.getSequenceNo()
        );

    LocalDate gesshobi = RkDateUtils.getFirstDateOfMonth(dto.getTaishoYm());

    List<KjKinmuNichijiJoho> kjKinmuNichijiJohoList = kjKinmuNichijiJohoRepository
        .getByKaishaCdAndUserIdAndKinmubiBetweenAndDeleteFlgFalse(
            dto.getKaishaCd(), dto.getUserId(), gesshobi, RkDateUtils.getLastDateOfMonth(gesshobi)
        );

    // checkShoninIchiranShonin と同等のチェック
    // checkShoninIchiranShonin と異なるチェック
    if (SHONINMACHI_JOSHI_OR_SHONINMACHI_SHONINSHA_OR_SHONINMACHI_JINJI.contains(kjktKinmuTeiseiShinseiJokyo.getShinseiStatus())) {
        for (KjktKinmuNichijiJohoTeiseiBefore kjktKinmuNichijiJohoTeiseiBefore : kjktKinmuNichijiJohoTeiseiBeforeList) {
            for (KjKinmuNichijiJoho kjKinmuNichijiJoho : kjKinmuNichijiJohoList) {
                if (kjKinmuNichijiJoho.getKinmubi().equals(kjktKinmuNichijiJohoTeiseiBefore.getKinmubi())) {
                    if (!kjKinmuNichijiJoho.getShugyokbn().equals(kjktKinmuNichijiJohoTeiseiBefore.getShugyokbn())) {
                        messages.add(Pair.of(KjktValidationErrorCode.KJKT20W005, null));
                    }
                }
            }
        }
    }

    // TODO: 2021/11/22 checkShoninIchiranShonin ではチェック無し
    // KJKT20W006 | 受入出向者の場合、出向元に訂正後の勤務表を必ず提出してください。
    // 職種3チェックのTODO箇所解決まで保留

    return messages;
}






/**
 * 入退館日時反映
 *
 * @param kaishaCd 会社コード
 * @param taishobi 基準日
 * @return 更新件数
 */
@Transactional
@Override
public int reflectGateInGateOut(KaishaCd kaishaCd, String jobName, LocalDate taishobi) {

    LocalDate currentDate = DateUtils.getCurrentDate();
    int result = 0;

    LocalDate dateFrom = getZenkaiDateTo(kaishaCd, jobName).plusDays(1L);
    LocalDate dateTo = taishobi;

    for (LocalDate targetDate = dateFrom; RkDateUtils.isAscendingEqual(targetDate, dateTo); targetDate = targetDate.plusDays(1L)) {
        List<RjkkGateInOutLogDto> logs = getGateInOutLogs(kaishaCd, targetDate);

        for (RjkkGateInOutLogDto log : logs) {
            entityManager.clear();

            KjKinmuHyoHojo kinmuHyoHojo = kjKinmuHyoHojoRepository
                .getByKaishaCdAndUserIdAndKijunbiAndDeleteFlgFalse(kaishaCd, log.getUserId(), targetDate);

            if (ObjectUtils.isEmpty(kinmuHyoHojo)) {
                // TODO: 除外ユーザが存在する可能性あり
                UserJoho user = rkUserJohoRepository
                    .getByUserIdAndValidPeriodSdLessThanEqualAndValidPeriodEdGreaterThanEqualAndDeleteFlgFalse(
                        log.getUserId(), currentDate, currentDate);

                if (ObjectUtils.isEmpty(user)) {
                    continue;
                }

                kinmuHyoHojo = new KjKinmuHyoHojo();
                kinmuHyoHojo.setKaishaCd(kaishaCd);
                kinmuHyoHojo.setUserId(user.getUserId());
                kinmuHyoHojo.setUserName(user.getLoginUserId());
                kinmuHyoHojo.setKijunbi(targetDate);
                kinmuHyoHojo.setLogonCheck(KairiCheck.MICHECK.getCode());
                kinmuHyoHojo.setLogoffCheck(KairiCheck.MICHECK.getCode());
                kinmuHyoHojo.setLogonCheckMobile(KairiCheck.MICHECK.getCode());
                kinmuHyoHojo.setLogoffCheckMobile(KairiCheck.MICHECK.getCode());
            }

            kinmuHyoHojo.setGateIn(log.getGateIn());
            kinmuHyoHojo.setGateOut(log.getGateOut());
            kinmuHyoHojo.setGateInCheck(KairiCheck.MICHECK.getCode());
            kinmuHyoHojo.setGateOutCheck(KairiCheck.MICHECK.getCode());

            saveAndFlush(kjKinmuHyoHojoRepository, kinmuHyoHojo);
            result++;
        }
    }

    saveDateTo(kaishaCd, jobName, dateTo);
    return result;
}


















/**
 * 月締承認
 *
 * @param kaishaCd 会社コード
 * @param userId ユーザーID
 * @param dateFrom 指定日From
 * @param dateTo 指定日To
 */
@Transactional
@Override
public void tsukijimeShonin(KaishaCd kaishaCd, Long userId, LocalDate dateFrom, LocalDate dateTo) {
    boolean romukanriTaishosha = RkUserUtils.isRomukanriTaishosha(userId);

    if (romukanriTaishosha) {
        tsukijimeShoninRomukanriTaishosha(kaishaCd, userId, dateFrom, dateTo);
    } else {
        tsukijimeShoninNotRomukanriTaishosha(kaishaCd, userId, dateFrom, dateTo);
    }
}

@Transactional
private void tsukijimeShoninRomukanriTaishosha(KaishaCd kaishaCd, Long userId, LocalDate dateFrom, LocalDate dateTo) {
    // TODO: 2024/2/26 篠崎 要整理

    TsukijimeShoninEnv env = new TsukijimeShoninEnv();
    List<KjKinmuNichijiJoho> kjKinmuNichijiJohoList = kjKinmuNichijiJohoRepository
        .getByKaishaCdAndUserIdAndKinmubiBetweenAndMeisaiKbnAndKinmuShoninJotaiInAndDeleteFlgFalse(
            kaishaCd, userId, dateFrom, dateTo, MeisaiKbn.TSUJO, NOT_SHONINZUMI
        );

    for (KjKinmuNichijiJoho kjKinmuNichijiJoho : kjKinmuNichijiJohoList) {
        KjKinmuHyoHojo kjKinmuHyoHojo = kjKinmuNichijiJoho.getKjKinmuHyoHojo();

        if (Objects.nonNull(kjKinmuHyoHojo) &&
            (Objects.nonNull(kjKinmuHyoHojo.getGateIn()) || Objects.nonNull(kjKinmuHyoHojo.getGateOut()))) {
            modifyRomukanri(kjKinmuNichijiJoho, kjKinmuHyoHojo, env);
            solveRjkkTaisho(kjKinmuNichijiJoho, RomukanriTaishoKbn.SANE_IGYOBIIJONOMINYURYOKU);
            modifyRomukanriKojo(kjKinmuNichijiJoho);
        }
    }
}

// TODO: 2023/4/21 鉄村 労務管理環境変数テーブルから取得する予定

@Getter
class TsukijimeShoninEnv {
    // TODO: 2022/2/1 篠崎以下より取得
    // THR_KINMU_ENV(ROMU_KANRI, KINMU_KAIRI, GATE_LOG, BASE_TIME)
    private int kairiMinute = Integer.valueOf(60);
}





@Transactional
private Pair<KairiCheck, KairiCheck> assignGateInOutCheck(KjKinmuNichijiJoho kjKinmuNichijiJoho, KjKinmuHyoHojo kjKinmuHyoHojo, TsukijimeShoninEnv env) {

    Pair<LocalDateTime, LocalDateTime> p = romuJokyokanriService.getKinmuNichiji(kjKinmuNichijiJoho);

    LocalDateTime kinmuBgn = p.getLeft();
    LocalDateTime kinmuEnd = p.getRight();

    BiFunction<LocalDateTime, Supplier<Long>, KairiCheck> kairiHantei = (gateNichiji, diffMinutes) -> {
        if (Objects.isNull(gateNichiji)) {
            return KairiCheck.TAISHOGAI;
        } else if (RkNumberUtils.isAscendingEqual(Long.valueOf(env.getKairiMinute()), diffMinutes.get())) {
            return KairiCheck.KAIRI_ARI;
        } else {
            return KairiCheck.KAIRI_NASHI;
        }
    };

    KairiCheck gateInCheck = kairiHantei.apply(kjKinmuHyoHojo.getGateIn(), () -> ChronoUnit.MINUTES.between(kinmuBgn, kjKinmuHyoHojo.getGateIn()));
    KairiCheck gateOutCheck = kairiHantei.apply(kjKinmuHyoHojo.getGateOut(), () -> ChronoUnit.MINUTES.between(kjKinmuHyoHojo.getGateOut(), kinmuEnd));

    return Pair.of(gateInCheck, gateOutCheck);
}