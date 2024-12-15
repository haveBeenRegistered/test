private void setIchiranUserInfo(SairyoInputForm sairyoInputForm) {
    
    // 其他信息从用户视图中获取
    List<RkUserV> userInfoList = rkSrService.getUserInfoNew(sairyoInputForm.getKaishaCd(), sairyoInputForm.getTaishoUserId());

    if (!userInfoList.isEmpty()) {
        RkUserV userInfo = userInfoList.get(0);
        
        sairyoInputForm.setShokushuCourse(userInfo.getCourseMeisho());
        sairyoInputForm.setShikakuRank(userInfo.getShikakuRank());
        sairyoInputForm.setKoinkbn(userInfo.getKoinkbnIlelSho());
    }
}



SELECT 
    ruv.shokushu_corse,
    CASE
        WHEN ruv.shokushu2 = '3' THEN ''
        WHEN ruv.shokushu2 IN ('0', '9') 
             AND ruv.shikaku_cd IN ('19', '15') 
             AND bst.zokusei4 = '07' THEN 'Ex'
        ELSE ruv.shikakuto 
    END AS shikakuRank,
    ruv.koin_kbn,
    ruv.jugyosha_meisho,
    ruv.course_meisho,
    ruv.shumu_yakushoku_meisho,
    ruv.jugyoin_no,
    ruv.user_name
FROM 
    RK_USER_V ruv
LEFT OUTER JOIN 
    BC_SHOGU_TBL bst
    ON ruv.user_id = bst.user_id
    AND ruv.valid_period_sd BETWEEN bst.valid_period_sd AND bst.valid_period_ed333