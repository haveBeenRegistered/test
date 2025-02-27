/**
 * 従業員個人休暇情報マスタDTO作成
 * ※従業員=非契約社員
 *
 * @param userJoho ユーザ情報
 * @param nendo 年度
 * @param kijunbi 基準日
 * @return 個人休暇情報マスタDTO
 */
@Transactional(readOnly = true)
@Override
public KojinKyukaJohoMstDto createJugyoinKojinKyukaJohoMstDto(
    UserJoho userJoho, 
    Integer nendo,
    LocalDate kijunbi
) {
    KaishaCd kaishaCd = userJoho.getKaishaCd();
    Long userId = userJoho.getUserId();

    // TODO: 資格が「FE職」の場合は、論理的な繰越繰越限度値が存在しないため、
    // yukyu_sendo、hozon_sendoに「999」を代入
    /*
    SELECT COUNT(*) INTO l_sikaku_cnt
    FROM PER_ALL_ASSIGNMENTS_F
    WHERE PERSON_ID = l_person_id
    AND l_kisyo_date BETWEEN EFFECTIVE_START_DATE AND EFFECTIVE_END_DATE
    AND ASS_ATTRIBUTE1 = 'C_COURCE_FE';
    s_err_pos := '01610';
    IF l_sikaku_cnt >= 1 THEN
        l_yukyu_sendo := 999;
        l_hozon_sendo := l_hozon_gendo_master;
    ELSE
        l_yukyu_sendo := l_yukyu_sendo_master;
        l_hozon_sendo := l_hozon_sendo_master;
    END IF;
    */

// 创建一个个人休假信息DTO对象，传入当前处理的年度(nendo)
// 同时初始状态设置为不进行有給休假付与（Boolean.FALSE）
KojinKyukaJohoMstDto dto = new KojinKyukaJohoMstDto(nendo, Boolean.FALSE);

// 调用isJugyoinExcludedYukyuFuyo方法判断，是否需要排除有給休假付与的处理
// 如果返回true，dto内部相应状态也会被设置，此时直接返回dto，后续的处理将不会执行
if (dto.setWithoutFuyo(isJugyoinExcludedYukyuFuyo(userJoho, nendo))) {
    return dto;
}

// 当年度数据处理：定义一个Runnable，用于封装当年度个人休假主表记录的处理逻辑
Runnable setFromHonnendoKojinKyukaJohoMst = () -> {
    // 从服务中获取当前年度（nendo）的个人休假信息主表记录
    // 参数：公司代码kaishaCd、用户ID userId和年度nendo
    KojinKyukaJohoMst kojinKyukaJohoMst = kojinKyukaJohoMstService.getTaishoNendoMst(
        kaishaCd,
        userId,
        nendo
    );

    // 如果获取的记录不为空，则继续设置dto中的相关字段
    // 使用Objects.isNull判断kojinKyukaJohoMst是否为null，若不为null则进入if块内部
    if (!dto.setWithoutFuyo(Objects.isNull(kojinKyukaJohoMst))) {
        // 设置当年度有給休假付与状态，使用枚举HonnendoYukyuFuyoSumi中的FUYO_MISAI的code值
        dto.setHonnendoYukyuFuyoSumi(HonnendoYukyuFuyoSumi.FUYO_MISAI.getCode());
        // 设置当年度保存休假（HozonKyuka）的标识，调用nta方法对原始数据进行转换
        dto.setHonnendoHozonKyukaAri(nta(kojinKyukaJohoMst.getHonnendoHozonKyukaAri()));
        // 设置有給休假截止日期（结束日）
        dto.setFuyoTaishoShuryobi(kojinKyukaJohoMst.getFuyoTaishoShuryobi());
        // 设置有給休假起始日期（开始日）
        dto.setFuyoTaishoKaishibi(kojinKyukaJohoMst.getFuyoTaishoKaishibi());
        // 设置当年度付与金额（或相关标识），转换原始数据（使用nta方法）
        dto.setHonnendoKekinAri(nta(kojinKyukaJohoMst.getHonnendoKekinAri()));
        // 设置下次预定付与日期（下次有給日期）
        dto.setJikaiFuyobi(kojinKyukaJohoMst.getJikaiFuyobi());
        // 设置制度规定的休假日数，转换原始数据（使用ntz方法）
        dto.setHonnendoSeidoNissu(ntz(kojinKyukaJohoMst.getHonnendoSeidoNissu()));
        // 设置制度内休假销账日数，转换原始数据（使用ntz方法）
        dto.setHonnendoSeidoShokaNissu(ntz(kojinKyukaJohoMst.getHonnendoSeidoShokaNissu()));
        // 设置契约标识，转换原始数据（使用ntf方法）
        dto.setKeiyakuFlg(ntf(kojinKyukaJohoMst.getKeiyakuFlg()));
        // 设置付与休假基准日数（初始值），转换原始数据（使用ntz方法）
        dto.setFuyoSanteiNissuMoto(ntz(kojinKyukaJohoMst.getFuyoSanteiNissuMoto()));
        // 设置实际的有給休假日数，转换原始数据（使用ntz方法）
        dto.setFuyoSanteiNissu(ntz(kojinKyukaJohoMst.getFuyoSanteiNissu()));
        // 设置上一年度有給休假未使用的日数，转换原始数据（使用ntz方法）
        dto.setZennendoYukyuMishokaNissu(ntz(kojinKyukaJohoMst.getZennendoYukyuMishokaNissu()));
        // 设置备注信息，将主表中的备注赋值给DTO
        dto.setBiko(kojinKyukaJohoMst.getBiko());
    }
};

// 执行之前定义的当年度处理逻辑，更新dto中当年度的休假信息
setFromHonnendoKojinKyukaJohoMst.run();

// 如果当前dto已标记为无需进行休假付与处理，则直接返回dto
if (dto.isWithoutFuyo()) {
    return dto;
}

// 获取当前年度休假时间管理记录，参数包括：
// 公司代码(kaishaCd)、用户ID(userId)、处理年度(nendo)以及休假种类(C_NENJI_YUKYU)
KsJikanKyukaKanri ksJikanKyukaKanri = kyukaJikansuCalcService.getNendomatsuKsJikanKyukaKanri(
    kaishaCd,
    userId,
    nendo,
    KyukaShurui.C_NENJI_YUKYU
);

// 如果当前年度的休假时间管理记录为空，则将dto标记为无需处理后返回
if (dto.setWithoutFuyo(Objects.isNull(ksJikanKyukaKanri))) {
    return dto;
}

// 计算上一年度（zennendo），即当年度减1
Integer zennendo = nendo - 1;

// 获取上一年度的个人休假主表记录
KojinKyukaJohoMst zennendoKojinKyukaJohoMst = kojinKyukaJohoMstService.getTaishoNendoMst(
    kaishaCd,
    userId,
    zennendo
);

// 如果上一年度的休假主表记录不存在，则标记dto为无需处理后返回
if (dto.setWithoutFuyo(Objects.isNull(zennendoKojinKyukaJohoMst))) {
    return dto;
}

// 获取上一年度的休假时间管理记录，与当年度方式类似
KsJikanKyukaKanri zennendoKsJikanKyukaKanri = kyukaJikansuCalcService.getNendomatsuKsJikanKyukaKanri(
    kaishaCd,
    userId,
    zennendo,
    KyukaShurui.C_NENJI_YUKYU
);

// 如果上一年度的休假时间管理记录为空，则将dto标记为无需处理后返回
if (dto.setWithoutFuyo(Objects.isNull(zennendoKsJikanKyukaKanri))) {
    return dto;
}   

// 以下部分计算一些与用户个人休假信息本身无关的基础数值

// 计算当前规定的有給日数（年休），由工作制度决定，传入制度类型Tsujo（通常制）
int yukyuGendoNissu = getJugyoinYukyuGendoNissu(KyukaGendoNissuKin.TSUJO);

// 获取保存休假日数（hozon），依据公司代码和制度类型得出
int hozonGendoNissu = getHozonGendoNissu(kaishaCd, KyukaGendoNissuKin.TSUJO);

// 根据公司休假管理数据计算当年度执行休假日数，
// RkDateUtils.getFirstDateOfNendo(nendo)返回当前年度第一天 
int honnendoJikankyuGendoNissu = kyukaJikansuCalcService.getJikankyuGendoNissu(
    kaishaCd,
    RkDateUtils.getFirstDateOfNendo(nendo)
);

// TODO: 对退职者、休职者的情况进行判断，计算当年度可付与的有給休假日数
int honnendoFuyoYukyuNissu = calcHonnendoFuyoYukyuNissu(
    userJoho,
    nendo,
    dto.isTaishokusha(),
    dto.isKyushokusha()
);

// 使用上一年度的休假主表数据计算前一年度休假销账（日数）
// 计算公式: 上年度已批准的有給休假日数 - 上年度实际销账的休假日数
float yukyuShokaNissu = ntz(zennendoKojinKyukaJohoMst.getHonnendoYukyuShokaNissu())
    - ntz(zennendoKojinKyukaJohoMst.getHonnendoJikankyuShokaNissu());

// 从上一年度的休假时间管理记录中获取计算得出的日数（可能为累计或取得的日数）
BigDecimal jikankyuShokaNissu = ntz(zennendoKsJikanKyukaKanri.getNendoNaiShutokuNissu());

// 将上一年度剩余的休假时间（小时）转换为日数，作为有給休假“繰越”日数
BigDecimal jikankyuKurikoshiNissu = kyukaJikansuCalcService.convertJikansuToNissu(
    kaishaCd,
    ntz(zennendoKsJikanKyukaKanri.getShoteJikan()),
    ntz(zennendoKojinKyukaJohoMst.getHonnendoYukyuKurikoshiJikan())
);

// 同样地，将上一年度剩余的休假小时转换为日数，得到“剩余”日数
BigDecimal jikankyuZanNissu = kyukaJikansuCalcService.convertJikansuToNissu(
    kaishaCd,
    ntz(zennendoKsJikanKyukaKanri.getShoteJikan()),
    ntz(zennendoKojinKyukaJohoMst.getHonnendoJikankyuZanJikan())
);

// 计算上一年度未使用的有給休假日数，公式: 上年度总有給休假日数 - 上年度销账日数
float zenkimatsuzanYukyuNissu = ntz(zennendoKojinKyukaJohoMst.getHonnendoYukyuNissu())
    - ntz(zennendoKojinKyukaJohoMst.getHonnendoYukyuShokaNissu());

// 计算上一年度保存休假剩余日数，公式: 上年度保存休假总日数 - 上年度实际使用的保存休假日数
int zenkimatsuzanHozonNissu = ntz(zennendoKojinKyukaJohoMst.getHonnendoHozonNissu())
    - ntz(zennendoKojinKyukaJohoMst.getHonnendoHozonShokaNissu());

// 将上一年度剩余的休假时间（以小时计）转换为日数，用于后续计算
int zenkimatsuzanYukyuJikan = ntz(zennendoKojinKyukaJohoMst.getHonnendoJikankyuZanJikan());

// 声明当年度的有給休假“繰越”留存部分、转换后的日数，以及休假切捨（舍去）值变量
float honnendoYukyuKurikoshiNissu;
BigDecimal jikankyuKurikoshiNissu;
float yukyuKirisuteNissu;




// 如果上一段条件成立，则进入if分支，采用特定算法对当年度有給休假“繰越”进行数值判断和计算
if (RkNumberUtils.isAscendingEqual(
    // 左侧：上一年度休假销账日数（yukyuShokaNissu）加上上一年度休假时间管理中的取得日数（转换为float）
    (yukyuShokaNissu + jikankyuShokaNissu.floatValue()),
    // 右侧：上一年度休假主表中登记的休假“繰越”日数（转换后）加上上一年度剩余休假时间（转换为float）
    (ntz(zennendoKojinKyukaJohoMst.getHonnendoYukyuKurikoshiNissu())
        + jikankyuKurikoshiNissu.floatValue())
)) {
    // 当左右两边数值一致时，说明上一年度休假“繰越”部分满足特定条件
    // 将当年度休假“繰越”日数设为上一年度付与的有給休假日数（转换后的值）
    honnendoYukyuKurikoshiNissu = ntz(zennendoKojinKyukaJohoMst.getHonnendoFuyoYukyuNissu());
    // 同时，转换部分置为零
    jikankyuKurikoshiNissu = BigDecimal.ZERO;
    // 同时计算休假切捨（日数）: 即上一年度未完全继承的休假日数
    yukyuKirisuteNissu = (zenkimatsuzanYukyuNissu + jikankyuZanNissu.floatValue())
        - ntz(zennendoKojinKyukaJohoMst.getHonnendoFuyoYukyuNissu());
} else {
    // 如果上述条件不成立，则采用另一分支的计算方式
    // 将当年度休假“繰越”初始值设为上一年度未使用的休假日数
    honnendoYukyuKurikoshiNissu = zenkimatsuzanYukyuNissu;
    // 并且待转换的休假时间设为上一年度剩余的休假时间（转换为日数）
    jikankyuKurikoshiNissu = jikankyuZanNissu;
    // 初始时休假切捨（日数）设为0
    yukyuKirisuteNissu = 0;
    
    // 如果现有当年度休假“繰越”（和转换的部分）之和超过当前规定的有給休假日数，则需要进行调整
    if (RkNumberUtils.isAscendingEqual(
        Float.valueOf(yukyuGendoNissu),
        // 当年度休假“繰越”与转换后休假的合计值
        (honnendoYukyuKurikoshiNissu + jikankyuKurikoshiNissu.floatValue())
    )) {
        // 计算超出的休假日数，并累加到休假切捨中
        yukyuKirisuteNissu += (honnendoYukyuKurikoshiNissu + jikankyuKurikoshiNissu.floatValue())
            - yukyuGendoNissu;
        // 超出部分置为0
        jikankyuKurikoshiNissu = BigDecimal.ZERO;
        // 将当年度休假“繰越”调整为规定的有給休假日数
        honnendoYukyuKurikoshiNissu = yukyuGendoNissu;
    }
}

// 最终当年度的有給休假日数由当年度付与和调整后的“繰越”之和决定
float honnendoYukyuNissu = honnendoFuyoYukyuNissu + honnendoYukyuKurikoshiNissu;

// 将上一年度剩余的休假“繰越”小时转换为当年度对应的休假日数
int honnendoYukyukurikoshiNissu = kyukaJikansuCalcService.getYukyuJikansu(
    kaishaId,
    ntz(ksJikankyukakanri.getShoteJikan()),
    jikankyukurikosiNissu
);

// ※此处暂定将有給休假“残”日数设置为当年度有給休假的基本数值，后续可根据需要更新
int honnendoYukyuJikankyuzanNissu = honnendoYukyuJikan;

// 将上一年度保存休假剩余小时转换为休假日数（浮点型转整型）
// 例如，根据转换规则将小时数转为天数
int honnendoFuyoHozonNissu = Float.valueOf(yukyukiRisuteNissu).intValue();

// 计算当年度保存休假的最终日数，根据规定与实际剩余情况进行调整
int honnendoHozonNissu;
if (RkNumberUtils.isAscendingEqual(
        hozonGendoNissu,
        // 比较规定的保存休假日数和实际保存休假日数总和
        (honnendoFuyoHozonNissu + zenkimatsuzanHozonNissu)
)) {
    // 如果保存休假总和大于或等于规定值，则休假切捨为多余部分
    yukyuKirisuteNissu = (honnendoFuyoHozonNissu + zenkimatsuzanHozonNissu) - hozonGendoNissu;
    // 同时将保存休假日数调整为规定值
    honnendoHozonNissu = hozonGendoNissu;
} else {
    // 如果实际保存休假总和不足规定，则直接累计实际值
    yukyuKirisuteNissu = 0;
    honnendoHozonNissu = honnendoFuyoHozonNissu + zenkimatsuzanHozonNissu;
}

// 初始化当年度中承认（已批准）的有給休假相关数据，后续方法会对这些数据进行编辑
float honnendoYukyuShokaNissu = 0;
int honnendoHannichiKyuka = 0;
int honnendoJikankyuShokaNissu = 0;

// 初始化当年度中承认（已批准）的保存休假相关数据，后续方法会对这些数据进行编辑
int honnendoHozonShokaNissu = 0;

// 将前述计算得到的各项数值设置到dto中，构成当年度完整的休假信息DTO对象
dto.setHonnendoFuyoYukyuNissu(honnendoFuyoYukyuNissu); // 当年度可付与的有給休假日数
dto.setHonnendoFuyoHozonNissu(honnendoFuyoHozonNissu); // 当年度可付与的保存休假日数
dto.setZenkimatsuzanYukyuNissu(zenkimatsuzanYukyuNissu); // 上年度剩余未销账的有給休假日数（转换后）
dto.setZenkimatsuzanHozonNissu(zenkimatsuzanHozonNissu); // 上年度剩余的保存休假日数（转换后）
dto.setHonnendoYukyuKurikoshiNissu(honnendoYukyuKurikoshiNissu); // 当年度有給休假“繰越”日数
dto.setYukyuKirisuteNissu(yukyuKirisuteNissu); // 当年度休假切捨（日数补正）
dto.setHonnendoYukyuNissu(honnendoYukyuNissu); // 当年度总有給休假日数（付与+繰越）
dto.setHonnendoHozonNissu(honnendoHozonNissu); // 当年度保存休假日数（最终确定）
dto.setHonnendoYukyuShokaNissu(honnendoYukyuShokaNissu); // 当年度已批准的有給休假销账日数，后续调整
dto.setHonnendoHannichiKyuka(honnendoHannichiKyuka); // 当年度已批准的有給休假累计日数，后续调整
dto.setHonnendoHozonShokaNissu(honnendoHozonShokaNissu); // 当年度已批准的保存休假销账日数，后续调整
dto.setHonnendoYukyuFuyoSumi(HonnendoYukyuFuyoSumi.FUYO_SUMI.getCode());  // 固定设定：有給休假付与执行状态
dto.setZenkimatsuzanYukyuJikan(zenkimatsuzanYukyuJikan); // 上年度剩余休假时间（以小时计）转换的日数
dto.setZenkimatsuShoteJikan(ntz(zennendoKsJikanKyukaKanri.getShoteJikan())); // 上年度休假时间管理中的所定工作时间
dto.setHonnendoYukyuKurikoshiJikan(honnendoYukyuKurikoshiJikan); // 当年度有給休假“繰越”时间（以小时计）
dto.setYukyuKirisuteJikan(0);  // 固定设定：休假切捨时间为0（可能后续调整）
dto.setHonnendoYukyuJikan(honnendoYukyuJikan); // 当年度有給休假时间（以小时计）
dto.setHonnendoJikankyuGendoNissu(honnendoJikankyuGendoNissu); // 当年度休假日数基准（实际计算值）
dto.setHonnendoJikankyuShokaNissu(honnendoJikankyuShokaNissu); // 当年度已销账的休假日数（实际计算值）
dto.setHonnendoJikankyuZanJikan(honnendoJikankyuZanJikan); // 当年度剩余休假时间（以小时计）的日数转换部分

// 获取基于用户所属MCN及基准日的用户ID集合，考虑跨部门或业态出向等情况
List<Long> userIdList = RkUserUtils.getUserIdList(userJoho.getMcn(), kijunbi);

// 查询当前年度中已批准的有給休假申请记录，适用于跨部门出向情况
List<KsShinseiJoho> ksShinseiJohoList = rkKsService.getKsShinseiJoho(
    userIdList, 
    nendo, 
    KyukaShurui.C_NENJI_YUKYU, 
    EXCLUDE_JIKANTANI, 
    ShinseiJotai.SHONINZUMI, 
    TorikeshiFlg.SHINKI
);

// 将查询结果反映到dto中，更新对应的已批准有給休假日数信息
reflectYukyukyuka(
    dto, 
    ksJikanKyukaKanri, 
    ksShinseiJohoList, 
    honnendoYukyuJikan
);

// 查询当前年度中已批准的保存休假申请记录（跨部门出向适用）
List<HzKsShinseiJoho> hzKsShinseiJohoList = rkHzKsService.getHzKsShinseiJohos(
    userIdList,
    Long.valueOf(nendo),
    ShinseiJotai.SHONINZUMI,
    TorikeshiFlg.SHINKI.getCode()
);

// 将查询到的保存休假记录反映到dto中，更新对应的保存休假数据
reflectHozonkyuka(dto, hzKsShinseiJohoList);

// 返回处理完成的dto对象，包含当年度及上年度相关休假信息
return dto;



















/**
 * 中途入社従業員保存休暇計上
 * 该方法用于为中途入社的员工计算保存休假，并将计算结果入账。
 */
public void exec() {
    // 获取jobName对应的上一回処理日（上一次的处理日期）
    // 例如，上次计算保存休假时的日期，用作数据查询的起始点。
    LocalDate zenkaiShoribi = getZenkaiShoribi(jobName);

    // 遍历所有公司代码，针对每个公司进行处理
    for (KaishaCd kaishacd : KaishaCd.values()) {
        // 获取符合条件的用户ID列表
        // 参数说明：
        //   当前公司代码(kaishacd)
        //   上次处理日加1天(开始日期)
        //   当前处理日(shoribi)作为结束日期
        List<Long> userIdList = ksYukyuFuyoService.getChutoNyushaHozonNissuFuyoTaisho(
            kaishacd,
            zenkaiShoribi.plusDays(1L),
            shoribi
        );

        // 清除主EntityManager的缓存，确保查询结果为最新数据
        entityManager.clear();

        // TODO: 测试用用户指定
        // 为了测试，指定了一个固定的用户ID列表，只保留这些用户参与后续处理
        List<Long> LIMITED_USER_ID_LIST = List.of(52093L, 52094L);
        Predicate<Long> testUser = e -> LIMITED_USER_ID_LIST.contains(e);
        userIdList = userIdList.stream().filter(testUser).collect(Collectors.toList());
        // 1000: ↑ 测试用用户指定

        // 遍历获取到的用户ID列表，对每个用户进行保存休假计算处理
        for (Long userId : userIdList) {
            EntityTransaction transaction = null;
            try {
                // 获取用户信息，根据公司代码和用户ID查询用户数据
                UserJoho userJoho = userJohoService.getUserJoho(kaishacd, userId);
                // 获取用户入社日期，该日期用于后续保存休假计算中判断入社时长等条件
                LocalDate nyukobi = userJoho.getNyukobi();

                // 获取用于修改数据的EntityManager事务，用于数据库更新操作
                transaction = entityManagerForModify.getTransaction();
                transaction.begin();

                // 调用保存休假计算逻辑方法hozonkyukakeiJou
                // 参数包括：修改用的EntityManager、用户ID、公司代码、入社日期、及可能的额外期间(null表示使用默认)
                // 如果处理成功，则提交事务、计数，并跳过当前循环进入下一个用户
                if (hozonkyukakeiJou(entityManagerForModify, userId, kaishacd, nyukobi, null)) {
                    transaction.commit();
                    counter.count(CNT_KOSHIN);
                    continue;
                }
                // 如果处理未成功，则回滚事务，以保证数据一致性
                transaction.rollback();
            } catch (Exception e) {
                // 出现异常时，若事务已开启，则回滚事务
                if (Objects.nonNull(transaction)) {
                    transaction.rollback();
                }
                // 获取异常信息，并输出日志记录错误
                String exceptionMessage = exceptionMessage(e);
                log.warn(MessageSourceUtils.getMessage(
                    RkKsBatchLogMessageId.KS20W003,
                    SHORI_NAME,
                    RkKaishaCdUtils.getKaishaName(kaishacd),
                    userId,
                    exceptionMessage
                ));
                log.warn(exceptionMessage, e);
            } finally {
                // 最终，无论成功或异常，都清理修改用及主EntityManager的缓存
                entityManagerForModify.clear();
                entityManager.clear();
            }
        }
        // 对于当前公司处理完后，保存当前处理日信息，
        // 通常用于记录任务最后的执行日期，防止重复执行或数据遗漏
        saveShoribi(jobName, shoribi);

        // 打印日志信息，显示当前任务的处理统计数据
        // 注：警告检查未执行，因此CNT_WARN的数据始终为0
        log.info(MessageSourceUtils.getMessage(
            RkKsBatchLogMessageId.KS201002,
            SHORI_NAME,
            counter.get(CNT_KOSHIN),
            counter.get(CNT_WARN)
        ));
    }
}





/**
 * 保存休暇計上処理
 *
 * 此方法用于计算保存休假的逻辑，主要流程如下：
 *   1. 根据传入的期间（dateFrom～dateTo）确定需要处理的年度范围。
 *   2. 获取该期间内对应每一年度的用户个人休假主记录集合。
 *   3. 从集合中取出最新一年度的记录，作为当前要更新的记录。
 *   4. 根据当前年度记录中剩余的有給休假日数来计算可保存休假天数；
 *      如果剩余日数为0或计算结果非正数，则不进行保存休假的入账处理。
 *   5. 否则，更新本年度记录中的保存休假、付与有給休假、以及有給休假繰越（累积）的数值，
 *      并调用更新方法保存记录。
 *
 * @param entityManagerForModify 用于修改数据的EntityManager
 * @param userId   目标用户ID
 * @param kaishaCd 目标公司的代码
 * @param dateFrom 计算期间（开始日期）
 * @param dateTo   计算期间（结束日期），如果dateTo为null则取当前年度
 * @return 如果保存休假入账成功则返回true，否则返回false
 */
private boolean hozonkyukakeiJou(EntityManager entityManagerForModify, Long userId, KaishaCd kaishaCd,
                                 LocalDate dateFrom, LocalDate dateTo) {
    // 计算期间的起始年度
    Integer nendoFrom = RkDateUtils.getNendo(dateFrom);
    // 如果结束日期为null，则使用当前年度，否则计算结束年度
    Integer nendoTo = Objects.isNull(dateTo) ? RkDateUtils.getKonNendo() : RkDateUtils.getNendo(dateTo);

//
// 1. 使用 IntStream.rangeClosed(nendoFrom, nendoTo) 生成一个整数流，
//    包含从 nendoFrom 到 nendoTo（包括两端）的所有整数，
//    每个整数都代表一个处理年度。
//
// 2. 对这个整数流调用 mapToObj() 方法，将每个年度nendo映射为对应的KojinkyukaJohoMst对象。
//    映射过程调用了 kojinkyukaJohoMstService.getTaishoNendoMst(kaishaCd, userId, nendo)，
//    通过传入公司代码(kaishaCd)、用户ID(userId)和当前年度(nendo)
//    来获取该年度的个人休假主记录。
//
// 3. 最后使用 .collect(Collectors.toList()) 将映射后的对象流转换为一个 List，
//    并赋值给变量 kojinkyukaJohoMstList，这个列表包含了从 nendoFrom 到 nendoTo 年度内的所有记录。
    List<KojinkyukaJohoMst> kojinkyukaJohoMstList = IntStream.rangeClosed(nendoFrom, nendoTo)
        .mapToObj(nendo -> kojinkyukaJohoMstService.getTaishoNendoMst(kaishaCd, userId, nendo))
        .collect(Collectors.toList());

    // 取得最新（今年度）的记录，通常记录顺序为按年度排列，取最后一条记录
    KojinkyukaJohoMst konnendoRecord = RkListUtils.last(kojinkyukaJohoMstList);

    // 从今年度记录中获得去年剩余的有給休假未使用日数
    Float yukyuMishokaNissu = konnendoRecord.getZennendoYukyuMishokaNissu();

// 这段代码是对剩余未使用有給休假日数进行判断。
// 其中，yukyuMishokaNissu 表示从今年度记录中获取的上年度剩余的有給休假日数。
// 调用方法 RkNumberUtils.isAscendingThan(yukyuMishokaNissu, 0f) 用来判断这个剩余值是否 ≤ 0（即不大于 0）。
// 如果剩余为 0 或更小，说明没有未使用的休假可供累积保存，
// 因此直接返回 false，不进行后续的保存休假处理。
if (RkNumberUtils.isAscendingThan(yukyuMishokaNissu, 0f)) {
    return false;
}

    // TODO: 2021/10/20 计算时间休假（暂待实现）

    // 计算从入行日到今年度全部期间内消化的有給休假日数累计，
    // 通过遍历所有年度的记录，并将其中“已消化有給休假日数”相加
    Float ruikeiYukyukyuka = kojinkyukaJohoMstList.stream()
        .map(KojinkyukaJohoMst::getHonnendoYukyuShokaNissu)
        .reduce(0f, Float::sum);

    // 计算保存休假日数 = 去年度剩余有給休假日数 - 全期消化的有給休假累计日数
    Float hozonNissu = yukyuMishokaNissu - ruikeiYukyukyuka;

    // 如果计算后的保存休假日数不大于0，则不进行保存休假入账，直接返回false
    if (!RkNumberUtils.isAscendingThan(0f, hozonNissu)) {
        return false;
    }

    // 更新今年度记录的保存休假数据
    // ① 将保存休假日数累加到原有的“保存休假日数”中，并转换为Float类型
    hozonNissu = Float.valueOf(konnendoRecord.getHonnendoHozonNissu() + hozonNissu);
    // ② 同步更新付与保存休假日数（已付与的保存休假日数加上本次入账的保存休假日数）
    Float fuyoHozonNissu = Float.valueOf(konnendoRecord.getHonnendoFuyoHozonNissu() + hozonNissu);

    // 计算今年度有給休假“繰越”日数的更新，
    // 通过将原有的“有給休暇繰越日数”减去本次保存入账的日数
    Float yukyukurikoshiNissu = konnendoRecord.getHonnendoYukyukurikoshiNissu() - hozonNissu;

    // 计算今年度实际有效的有給休假日数更新，
    // 即从原有的付与有給休假日数中扣除本次保存休假入账的日数
    Float yukyuNissu = konnendoRecord.getHonnendoFuyoYukyuNissu() - hozonNissu;

    // 将更新后的数据设置回今年度的记录中：
    // ① 更新保存休假日数（转换为整型存储）
    konnendoRecord.setHonnendoFuyoHozonNissu(fuyoHozonNissu.intValue());
    // ② 更新有給休假繰越日数
    konnendoRecord.setHonnendoYukyukurikoshiNissu(yukyukurikoshiNissu);
    // ③ 更新付与有給休假日数
    konnendoRecord.setHonnendoYukyuNissu(yukyuNissu);
    // ④ 更新总的保存休假日数
    konnendoRecord.setHonnendoHozonNissu(hozonNissu.intValue());

    // 调用更新方法，将修改后的今年度记录持久化到数据库中
    kojinkyukaJohoMstService.update(entityManagerForModify, konnendoRecord);

    // 返回true表示保存休假入账处理成功
    return true;
}


/**
 * 获取上一次处理日期
 * @param jobName 作业名称
 * @return 上一次处理日期
 * @throws RkBatchException 当前处理日期不大于上次处理日期时抛出异常
 */
private LocalDate getZenkaiShoribi(String jobName) {
    // 获取上一次处理日期
    LocalDate zenkaiShoribi = getZenkaiShoribi(jobName);

    // 检查当前处理日期是否大于上次处理日期
    // 如果当前处理日期小于或等于上次处理日期，则抛出异常
    if (RkDateUtils.isAscendingEqual(shoribi, zenkaiShoribi)) {
        throw new RkBatchException(RkKsBatchErrorCode.KS10E002); 
    }

    return zenkaiShoribi;
}