package com.example.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.repository.RkIfsskekinRepository;
import com.example.repository.RkKkssShinseiJohoRepository;
import com.example.repository.SsShinseiJohoRepository;
import com.example.util.MessageSourceUtils;
import com.example.util.RkIfssBatchLogMessageId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RkIfssCreatekekinFileTasklet implements Tasklet {

    public static final String TASKLET_NAME = "rkIfssCreatekekinFileTasklet";
    private static final String SHORI_NAME = "欠勤情報ファイル作成処理";

    private static final Logger log = LoggerFactory.getLogger(RkIfssCreatekekinFileTasklet.class);

    @Autowired
    private RkIfsskekinRepository rkIfsskekinRepository;

    @Autowired
    private RkKkssShinseiJohoRepository rkKkssShinseiJohoRepository;

    @Autowired
    private SsShinseiJohoRepository ssShinseiJohoRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info(MessageSourceUtils.getMessage(RkIfssBatchLogMessageId.IFSS101001, SHORI_NAME));

        List<RkIfsskekinDto> soshinData = new ArrayList<>();
        soshinData.addAll(getKekinSosinTaisho(KaishaCd.BK.getCode()));
        soshinData.addAll(getKekinSosinTaisho(KaishaCd.SC.getCode()));
        soshinData.addAll(getKekinSosinTaisho(KaishaCd.TR.getCode()));
        soshinData.addAll(getKekinSosinTaisho("400"));

        insertRkIfsskekin(soshinData);
        updateKkssShinsei(soshinData);

        log.info(MessageSourceUtils.getMessage(RkIfssBatchLogMessageId.IFSS101002, SHORI_NAME));
        return RepeatStatus.FINISHED;
    }

    private List<RkIfsskekinDto> getKekinSosinTaisho(String kaishaId) {
        List<Object[]> resultKkss = rkIfsskekinRepository.kekinNativeQuery(kaishaId, KuniCd.DEFAULT.getCode());
        List<Object[]> resultSs = rkIfsskekinRepository.sankyulkukyuNativeQuery(kaishaId);

        List<RkIfsskekinDto> soshinData = new ArrayList<>();

        soshinData.addAll(setRkIfssKekinFile(resultKkss));
        soshinData.addAll(setRkIfsskekinFile(resultSs));

        return soshinData;
    }

    /**
     * 欠勤情報ファイルの作成
     * 
     * @param result レコード
     * @return 欠勤情報ファイル ([@link RkIfsskekinDto]) リスト
     */
    private List<RkIfsskekinDto> setRkIfsskekinFile(List<Object[]> result) {
        List<RkIfsskekinDto> sosinData = new ArrayList<>();

        result.stream().forEach(e -> {
            RkIfsskekinDto record = new RkIfsskekinDto();

            record.setCtrlInfo(String.class.cast(e[0]));
            record.setKaishaCd(String.class.cast(e[1]));
            record.setJugyoinNo(String.class.cast(e[2]));
            record.setSequenceNo(String.class.cast(e[3]));
            record.setKanriJokyo(String.class.cast(e[4]));
            record.setKekinKaishibi(String.class.cast(e[5]));
            record.setFirstEigyobiFlg(String.class.cast(e[6]));
            record.setKekinShuryobi(String.class.cast(e[7]));
            record.setLastEigyobiFlg(String.class.cast(e[8]));
            record.setKekinJiyu(String.class.cast(e[9]));
            if (Objects.nonNull(e[10])) {
                record.setByomei(String.class.cast(e[10]).replaceAll("\\\\r\\\\n|\\\\r|\\\\n", ""));
            }
            record.setKaigokazokuShimei(String.class.cast(e[11]));
            record.setKaigokazokuTsuzukigara(String.class.cast(e[12]));
            record.setShusanbi(String.class.cast(e[13]));
            record.setShusanYoteibi(String.class.cast(e[14]));
            record.setSanzenTokukyukaishibi(String.class.cast(e[15]));
            record.setSanzenTokukyuShuryobi(String.class.cast(e[16]));
            record.setSanzenkyumukaishibi(String.class.cast(e[17]));
            record.setSanzenkyumuShuryobi(String.class.cast(e[18]));
            record.setSangokyumukaishibi(String.class.cast(e[19]));
            record.setSangokyumuShuryobi(String.class.cast(e[20]));
            record.setIkukyukaishibi(String.class.cast(e[21]));
            record.setIkukyuShuryobi(String.class.cast(e[22]));
            record.setToshoUketsukebi(String.class.cast(e[23]));
            record.setTataiNinshinkbn(String.class.cast(e[24]));
            record.setIkukyukazokuShimei(String.class.cast(e[25]));
            record.setIkukyukazokuTsuzukigara(String.class.cast(e[26]));
            record.setKkssShinseiJohoId(String.class.cast(e[27]));
            record.setSsShinseiJohoId(String.class.cast(e[28]));
            record.setSoshingoShorikbn(String.class.cast(e[29]));

            sosinData.add(record);
        });

        return sosinData;
    }

    /**
     * 送信用テーブルに登録する
     *
     * @param soshinData 送信データ
     */
    private void insertRkIfsskekin(List<RkIfsskekinDto> soshinData) {
        soshinData.stream().forEach(e -> {
            // キー項目が重複するレコードが既に存在するなら、物理削除する
            RkIfsskekin henkomae = rkIfsskekinRepository.getByCtrlInfoAndKaishaCdAndJugyoinNoAndSequenceNoAndKekinJiyu(
                    e.getCtrlInfo(), e.getKaishaCd(), e.getJugyoinNo(), e.getSequenceNo(), e.getKekinJiyu());

            if (!ObjectUtils.isEmpty(henkomae)) {
                LockUtils.optimisticForceIncrement(henkomae);
                rkIfsskekinRepository.delete(henkomae);
                rkIfsskekinRepository.flush();
            }

            RkIfsskekin rkIfsskekin = new RkIfsskekin();
            rkIfsskekin.setCtrlInfo(e.getCtrlInfo());
            rkIfsskekin.setKaishaCd(e.getKaishaCd());
            rkIfsskekin.setJugyoinNo(e.getJugyoinNo());
            rkIfsskekin.setSequenceNo(e.getSequenceNo());
            rkIfsskekin.setKanriJokyo(e.getKanriJokyo());
            rkIfsskekin.setKekinKaishibi(e.getKekinKaishibi());
            rkIfsskekin.setFirstEigyobiFlg(e.getFirstEigyobiFlg());
            rkIfsskekin.setKekinShuryobi(e.getKekinShuryobi());
            rkIfsskekin.setLastEigyobiFlg(e.getLastEigyobiFlg());
            rkIfsskekin.setKekinJiyu(e.getKekinJiyu());
            rkIfsskekin.setByomei(e.getByomei());
            rkIfsskekin.setKaigokazokuShimei(e.getKaigokazokuShimei());
            rkIfsskekin.setKaigokazokuTsuzukigara(e.getKaigokazokuTsuzukigara());
            rkIfsskekin.setShusanbi(e.getShusanbi());
            rkIfsskekin.setShusanYoteibi(e.getShusanYoteibi());
            rkIfsskekin.setSanzenTokukyukaishibi(e.getSanzenTokukyukaishibi());
            rkIfsskekin.setSanzenTokukyuShuryobi(e.getSanzenTokukyuShuryobi());
            rkIfsskekin.setSanzenkyumukaishibi(e.getSanzenkyumukaishibi());
            rkIfsskekin.setSanzenkyumuShuryobi(e.getSanzenkyumuShuryobi());
            rkIfsskekin.setSangokyumukaishibi(e.getSangokyumukaishibi());
            rkIfsskekin.setSangokyumuShuryobi(e.getSangokyumuShuryobi());
            rkIfsskekin.setIkukyukaishibi(e.getIkukyukaishibi());
            rkIfsskekin.setIkukyuShuryobi(e.getIkukyuShuryobi());
            rkIfsskekin.setToshoUketsukebi(e.getToshoUketsukebi());
            rkIfsskekin.setTataiNinshinkbn(e.getTataiNinshinkbn());
            rkIfsskekin.setIkukyukazokuShimei(e.getIkukyukazokuShimei());
            rkIfsskekin.setIkukyukazokuTsuzukigara(e.getIkukyukazokuTsuzukigara());
            rkIfsskekin.setSoshinZumiKon(SoshinZumiKon.MISHORI);

            LockUtils.optimisticForceIncrement(rkIfsskekin);
            rkIfsskekinRepository.save(rkIfsskekin);
        });
    }

    /**
     * 送信用CSV作成後、労務管理、欠勤情報テーブルの送信処理済区分を1:処理済に更新する
     *
     * @param sosinData 送信データ
     */
    private void updateKkssShinsei(List<RkIfsskekinDto> sosinData) {
        sosinData.stream().forEach(e -> {
            IfSoshinZumiKbn ifSoshinZumiKbn = null;

            if (e.getSoshingoShoriKbn().equals(IfSoshinZumiKbn.SHORIZUMI.getCode())) {
                ifSoshinZumiKbn = IfSoshinZumiKbn.SHORIZUMI;
            } else {
                ifSoshinZumiKbn = IfSoshinZumiKbn.TAISHOGAI;
            }

            if (Objects.nonNull(e.getKkssShinseiJohoId())) {
                KkssShinseiJoho kkssShinseiJoho = rkKkssShinseiJohoRepository
                        .getById(Long.valueOf(e.getKkssShinseiJohoId()));
                kkssShinseiJoho.setIfSoshinZumiKbn(ifSoshinZumiKbn);
                LockUtils.optimisticForceIncrement(kkssShinseiJoho);
                rkKkssShinseiJohoRepository.save(kkssShinseiJoho);
            }

            if (Objects.nonNull(e.getSsShinseiJohoId())) {
                SsShinseiJoho ssShinseiJoho = ssShinseiJohoRepository.getById(Long.valueOf(e.getSsShinseiJohoId()));
                ssShinseiJoho.setIfSoshinZumiKbn(ifSoshinZumiKbn);
                LockUtils.optimisticForceIncrement(ssShinseiJoho);
                ssShinseiJohoRepository.save(ssShinseiJoho);
            }
        });
    }

}

WITH saishin_ss_shinsei

AS (
    SELECT
        *
    FROM
        rk_ss_shinsei_joho_tbl ss
    WHERE
        ss.shinsei_status = '2'

AND COALESCE(ss.if_soshin_zumi_kbn, '0') = '0'
        AND ss.kaisha_cd = 100
        AND ss.delete_flg = false
),

henko_mae AS (
    -- 変更前のレコードがIF送信済であれば削除のため取得する
    SELECT
        henko_mae.*
    FROM
        saishin_ss_shinsei saishin,
        rk_ss_shinsei_joho_tbl henko_mae
    WHERE
        saishin.user_id = henko_mae.user_id
        AND saishin.sequence_no = henko_mae.sequence_no

AND COALESCE(henko_mae.if_soshin_zumi_kbn, '0') = '1'
),

soshin_taisho_ss AS (
    -- 最新のデータと変更前のデータをUNION
    SELECT
        CASE
            WHEN saishin.ss_shinsei_kbn = '9' THEN 'D'
            ELSE 'C'
        END ctrl_info,
        '1' soshingo_shori_kbn,
        saishin.*
    FROM
        saishin_ss_shinsei saishin
    LEFT OUTER JOIN henko_mae
    ON saishin.user_id = henko_mae.user_id
    AND saishin.sequence_no = henko_mae.sequence_no
    WHERE

NOT (henko_mae.ss_shinsei_joho_id IS NULL
        AND saishin.ss_shinsei_kbn = '9')
    UNION
    SELECT
        'D' ctrl_info,
        '99' soshingo_shori_kbn,
        henko_mae.*
    FROM
        henko_mae,
        saishin_ss_shinsei saishin
    WHERE
        saishin.user_id = henko_mae.user_id
        AND saishin.sequence_no = henko_mae.sequence_no
        AND saishin.ss_shinsei_kbn != '9'
),

kaigai AS (
    -- 海外勤務者を除外するため、期間中の渡航履歴があるレコードのIDを取得
    SELECT
        saishin.ss_shinsei_joho_id
    FROM
        saishin_ss_shinsei saishin,
        rk_toko_rireki_tbl toko
    WHERE
        toko.user_id = saishin.user_id
        AND (
            (toko.shukokubi < saishin.sankyu_ikukyu_shuryobi
            AND toko.nyukokubi > saishin.sankyu_ikukyu_kaishibi)
            OR toko.nyukokubi IS NULL
        )
        AND toko.delete_flg = false
),

kazoku_temp AS (
    SELECT
        soshin_ss.ss_shinsei_joho_id,
        kazoku_mst.kazoku_shimei_mei AS mei,
        kazoku_mst.tsuzukigara AS tsuzuki_gara
    FROM
        soshin_taisho_ss soshin_ss,
        rk_kazoku_joho_mst_tbl kazoku_mst
    WHERE
        soshin_ss.user_id = kazoku_mst.user_id
        AND soshin_ss.shusanbi = kazoku_mst.seinengapi
        AND CURRENT_DATE >= kazoku_mst.valid_period_sd
        AND CURRENT_DATE <= kazoku_mst.valid_period_ed
        AND kazoku_mst.delete_flg = false
    ORDER BY
        kazoku_mst.kazoku_joho_mst_id
),

kazoku AS (
    SELECT
        kazoku_temp.ss_shinsei_joho_id,
        STRING_AGG(kazoku_temp.mei, ',') AS mei,
        STRING_AGG(kazoku_temp.tsuzuki_gara, ',') AS tsuzuki_gara
    FROM
        kazoku_temp
    GROUP BY
        kazoku_temp.ss_shinsei_joho_id
)
SELECT
    ss.ctrl_info,
    ss.kaisha_cd,
    bc_user.jugyoin_no,
    ss.sequence_no,
    CASE
        WHEN

ss.ss_shinsei_kbn IN ('3', '9') THEN '0'
        ELSE '1'
    END AS kanri_jokyo,
    ss.shusanbi,
    ss.shusan_yoteibi,
    ss.sanzen_tokukyu_kaishibi,
    CASE
        WHEN ss.sanzen_tokukyu_kaishibi IS NULL THEN NULL
        ELSE ss.sanzen_kyumu_kaishibi
    END AS sanzen_tokukyu_shuryobi,
    ss.sanzen_kyumu_kaishibi,
    CASE
        WHEN ss.sanzen_kyumu_kaishibi IS NULL THEN NULL

ELSE COALESCE(ss.shusanbi, ss.shusan_yoteibi)
    END AS sanzen_kyumu_shuryobi,
    CASE
        WHEN ss.sango_kyumu_shuryo_yoteibi IS NULL
        AND ss.sango_kyumu_shuryobi IS NULL THEN NULL
        WHEN ss.shusanbi IS NULL THEN ss.shusan_yoteibi + 1
        ELSE ss.shusanbi + 1
    END AS sango_kyumu_kaishibi,
    COALESCE(ss.sango_kyumu_shuryobi, ss.sango_kyumu_shuryo_yoteibi) AS sango_kyumu_shuryobi,
    ss.ikukyu_kaishibi,
    COALESCE(ss.ikukyu_shuryobi, ss.ikukyu_shuryo_yoteibi) AS ikukyu_shuryobi,
    ss.sankyu_ikukyu_kaishibi AS tosho_kekin_kaishibi,
    ss.tatai_ninshin_flg,
    kazoku.mei AS ikuji_taisho_shimei,
    kazoku.tsuzuki_gara AS ikuji_taisho_tsuzukigara,
    ss.ss_shinsei_joho_id,
    ss.soshingo_shori_kbn
FROM
    soshin_taisho_ss ss
LEFT OUTER JOIN kazoku
ON ss.ss_shinsei_joho_id = kazoku.ss_shinsei_joho_id
LEFT OUTER JOIN bc_user_tbl bc_user
ON ss.user_id = bc_user.user_id
WHERE
    soshin_ss.user_id = bc_user.user_id 
    AND NOT EXISTS (
        SELECT 
            * 
        FROM 
            kaigai 
        WHERE 
            soshin_ss.ss_shinsei_joho_id = kaigai.ss_shinsei_joho_id
    )
    AND bc_user.delete_flg = false
SELECT
    ss.ctrl_info AS ctrl_info,
    ss.kaisha_cd AS kaisha_cd,
    ss.jugyoin_no AS jugyoin_no,

CAST(ss.sequence_no AS character varying) AS sequence_no,
    ss.kanri_jokyo AS kanri_jokyo,
    NULL AS kekin_kaishibi,
    NULL AS first_eigyobi_fig,
    NULL AS kekin_shuryobi,
    NULL AS last_eigyobi_flg,
    '3' AS kekin_jiyu,
    '産休育休' AS byomei,
    NULL AS kaigo_taisho_shimei,
    NULL AS kaigo_taisho_tsuzukigara,
    TO_CHAR(ss.shusanbi, 'YYYYMMDD') AS shusanbi,
    TO_CHAR(ss.shusan_yoteibi, 'YYYYMMDD') AS shusan_yoteibi,
    TO_CHAR(ss.sanzen_tokukyu_kaishibi, 'YYYYMMDD') AS sanzen_tokukyu_kaishibi,
    TO_CHAR(ss.sanzen_tokukyu_shuryobi, 'YYYYMMDD') AS sanzen_tokukyu_shuryobi,
    TO_CHAR(ss.sanzen_kyumu_kaishibi, 'YYYYMMDD') AS sanzen_kyumu_kaishibi,
    TO_CHAR(ss.sanzen_kyumu_shuryobi, 'YYYYMMDD') AS sanzen_kyumu_shuryobi,
    TO_CHAR(ss.sango_kyumu_kaishibi, 'YYYYMMDD') AS sango_kyumu_kaishibi,
    TO_CHAR(ss.sango_kyumu_shuryobi, 'YYYYMMDD') AS sango_kyumu_shuryobi,
    TO_CHAR(ss.ikukyu_kaishibi, 'YYYYMMDD') AS ikukyu_kaishibi,
    TO_CHAR(ss.ikukyu_shuryobi, 'YYYYMMDD') AS ikukyu_shuryobi,
    TO_CHAR(ss.tosho_kekin_kaishibi, 'YYYYMMDD') AS tosho_kekin_kaishibi,
    ss.tatai_ninshin_flg AS tatai_ninshin_flg,
    ss.ikuji_taisho_shimei AS ikuji_taisho_shimei,
    ss.ikuji_taisho_tsuzukigara AS ikuji_taisho_tsuzukigara,
    NULL AS kkss_shinsei_joho_id,
    CAST(ss.ss_shinsei_joho_id AS character varying) AS ss_shinsei_joho_id,
    ss.soshingo_shori_kbn AS soshingo_shori_kbn
FROM
    SS
ORDER BY
    ss.kaisha_cd,
    ss.jugyoin_no,
    CAST(ss.sequence_no AS integer),
    ss.ctrl_info DESC;
