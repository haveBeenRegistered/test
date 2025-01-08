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

    



}