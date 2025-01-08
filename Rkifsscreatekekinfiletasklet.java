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
            e.getCtrlInfo(), e.getKaishaCd(), e.getJugyoinNo(), e.getSequenceNo(), e.getKekinJiyu()
        );

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
            KkssShinseiJoho kkssShinseiJoho = rkKkssShinseiJohoRepository.getById(Long.valueOf(e.getKkssShinseiJohoId()));
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