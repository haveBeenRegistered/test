@Override
public List<Object[]> testi(KaishaCd kaishaCd, Long userId, LocalDate kijunbi) {

    // パラメータの設定
    // 创建一个存储参数的列表，使用 Pair 存储参数名称和值
    List<Pair<String, Object>> parameterList = new ArrayList<>();

    // select句
    // 创建一个 StringBuilder 用于构建 SELECT 子句
    StringBuilder selectSb = new StringBuilder();
    selectSb.append("SELECT ")
            .append("ruv.yakushoku_kbn, ")
            .append("ruv.shokushu_corse, ")
            .append("CASE ")
            .append("    WHEN ruv.shokushu2 = '9' THEN '' ")
            .append("    WHEN ruv.shokushu2 IN ('0', '9') ")
            .append("         AND ruv.shikaku IN ('19', '16') ")
            .append("         AND bst.zokusei4 = '07' THEN 'Ex' ")
            .append("    ELSE ruv.shikaku_to ")
            .append("END AS shikakuRank, ")
            .append("ruv.koinkbn, ")
            .append("ruv.jugyosha_meisho, ")
            .append("ruv.course_meisho, ")
            .append("ruv.shumu_yakushoku_metsho, ")
            .append("ruv.jugyoin_no, ")
            .append("ruv.user_name ");

    // query句
    // 创建一个 StringBuilder 用于构建 FROM 和 JOIN 子句
    StringBuilder querySb = new StringBuilder();
    querySb.append("FROM ")
           .append("RK_USER_V ruv ")
           .append("LEFT OUTER JOIN SHORU bst ON ruv.user_id = bst.user_id ")
           .append("    AND ruv.valid_period_sd BETWEEN bst.valid_period_sd AND bst.valid_period_ed ");

    // where句
    // 创建一个 StringBuilder 用于构建 WHERE 子句
    StringBuilder whereSb = new StringBuilder();
    whereSb.append("WHERE ")
           .append("ruv.kaisha_cd = :kaishaCd ")
           .append("AND ruv.user_id = :userId ")
           .append("AND ruv.valid_period_sd <= :kijunbi ")
           .append("AND ruv.valid_period_ed >= :kijunbi ");

    // 添加参数到列表
    parameterList.add(Pair.of("kaishaCd", kaishaCd));
    parameterList.add(Pair.of("userId", userId));
    parameterList.add(Pair.of("kijunbi", kijunbi));

    // select作成
    // 将 SELECT、FROM、JOIN 和 WHERE 子句组合成完整的查询语句
    String selectSql = selectSb.toString() + querySb.toString() + whereSb.toString();

    // 创建 Query 对象，用于执行查询
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

/**
 * ユーザ情報取得(ユーザID、システム日付検索)
 *
 * @param kaishaCd 会社コード
 * @param userId   ユーザID
 * @return {@link RkUserY}
 */
@Transactional(readOnly = true)
@Override
public List<Object[]> getUserInfoNew(KaishaCd kaishaCd, Long userId) {
    return rkUserRepository.test1(kaishaCd, userId, DateUtils.getCurrentDate());
}

private void setIchiRanUserInfo(SairyoInputForm sairyoInputForm) {
    
    /**
     * 
     /* private void setDoiShoshutsuryokusaireInputFor(SairyoInputForm sairyoInputForm, SrkaitoJokyo srkaitoJokyo) {
        sairyoInputForm.setSrSairanDtoList().stream().forEach(r ->{
    */

    // その他情報はユーザビューから取得
    List<RkUserV> userInfo = rkSrService.getUserInfoNew(sairyoInputForm.getKaishaCd(), sairyoInputForm.getTaisholUserId());

    sairyoInputForm.setShokushuCourse(userInfo.getCourseMeisho());
    sairyoInputForm.setShikakuRank(userInfo.getShikakuRank());

    sairyoInputForm.setKoinkbn(userInfo.getKainkbnikelsho());
}




/**
 * 休日一括登録申請入力画面表示制御の処理
 *
 * @param kyujitsuIkatsuForm 休日一括登録申請のForm
 */
private void setShinseiInputControl(KyujitsuIkatsuForm kyujitsuIkatsuForm) {

    // 対象年月の一日
    LocalDate taishobi = RkDateUtils.getDate(
        kyujitsuIkatsuForm.getKtShinseiInputDto().getYmYear(),
        kyujitsuIkatsuForm.getKtShinseiInputDto().getYmMonth(),
        ONEDAY
    );

    // 契約社員の取得
    List<KtKeiyakuJohoGetDto> ktKeiyakuJohoDtoDomainList = rkKtService.getKtKeiyakuJoho(
        kyujitsuIkatsuForm.getKaishaCd(),
        kyujitsuIkatsuForm.getTaishoUserId(),
        taishobi
    );

    // 変換処理 Domain -> Dto
    List<KtKeiyakuJohoDto> dtoList = ktKeiyakuJohoDtoDomainList.stream()
        .map(e -> modelMapper.map(e, KtKeiyakuJohoDto.class))
        .collect(Collectors.toList());

    // 契約情報を取得した場合
    if (!CollectionUtils.isEmpty(dtoList)) {
        dtoList.forEach(s -> {

            // 10年再雇用日の翌月取得
            LocalDate saikoyoYokugetSum = null;

            if (Objects.nonNull(s.getSaikoyoDate())) {
                saikoyoYokugetSum = s.getSaikoyoDate().plusMonths(1L);

                // 定年再雇用が「1: 定年再雇用」の場合
                if (Saikoyoflag.SAIKOYO.equals(s.getSaikoyoFlg())) {

                    // 勤務日数が「1:週1日」、「2:週2日」、「3:週3日」、「4:週4日」のいずれかの場合
                    if (KINMU_NISSU_1.equals(s.getKinmuNissu()) ||
                        KINMU_NISSU_2.equals(s.getKinmuNissu()) ||
                        KINMU_NISSU_3.equals(s.getKinmuNissu()) ||
                        KINMU_NISSU_4.equals(s.getKinmuNissu())) {

                        // 退職予定日部
                        if (RkDateUtils.isAscendingEqual(saikoyoYokugetSum, taishobi)) {

                            // 職種内区分が「B1: CSL」の場合
                            if (SHOKUSHU_NAI_KEN_B1.equals(s.getShokushuNaibm())) {
                                // 非表示セット
                                this.setKtKeiyakuJoholtiDe(kyujitsuIkatsuForm);
                            } else {
                                // 上記以外の場合
                                // 必要な処理をここに追加
                            }

                            // TaishokuYoteibiDateFlagをセット
                            kyujitsuIkatsuForm.setTaishokuYoteibiFlag(true);

                            // 退職予定日部の活性有無
                            this.setKtTaishokuYoteibi(kyujitsuIkatsuForm);

                            // メッセージ内容部処理
                            this.setKtMessageNaiyo(kyujitsuIkatsuForm, taishobi, s.getKinmuNissu());

                        } else {
                            // 定年再雇用日の翌月≦画面、対象年月以外の場合

                            // 非表示セット
                            this.setKtKeiyakuJoholtiDe(kyujitsuIkatsuForm);
                        }

                    } else {
                        // 勤務日数が他の値の場合
                        // 必要な処理をここに追加
                    }

                }

            } else {
                // 契約情報を取得していない場合

                // 非表示セット
                this.setKtKeiyakuJohoHide(kyujitsuIkatsuForm);
            }

        });
    }

}



@Transactional(readOnly = true)
@Override
public List<KtKeiyakuJohoGetDto> getKeiyakuJohoList(KaishaId kaishald, Long userId, LocalDate taishobi) {

    // 契約社員情報
    List<Object[]> result = repository.get(kaishald, userId, taishobi);

    List<KtKeiyakuJohoGetDto> dtoList = new ArrayList<>();

    if (!ObjectUtils.isEmpty(result)) {

        // DTOに変換
        result.stream().forEach(s -> {
            KtKeiyakuJohoGetDto dto = new KtKeiyakuJohoGetDto();

            dto.setKiralIssu(String.class.cast(s[0]));
            dto.setSaikoyoFlg(Saikoyoflag.class.cast(s[1]));
            dto.setSaikoyoDate(LocalDate.class.cast(s[2]));
            dto.setShokushuNaillon(String.class.cast(s[3]));

            dtoList.add(dto);
        });
    }

    return dtoList;
}