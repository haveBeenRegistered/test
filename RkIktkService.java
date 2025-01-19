public interface RkIktkService {

    /**
     * 一括取込開始チェック
     *
     * @param kaishaCd 会社コード
     */
    void torikomiStartCheck(KaishaCd kaishaCd);

    /**
     * 時間外一括取込ファイル設定
     *
     * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
     * @return IktkTorikomishoriDto
     */
    IktkTorikomishoriDto setJikangaiFile(IktkTorikomishoriDto iktkTorikomishoriDto);

    /**
     * 深夜時間外一括取込ファイル設定
     *
     * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
     * @return IktkTorikomishoriDto
     */
    IktkTorikomishoriDto setShinyaJikangaiFile(IktkTorikomishoriDto iktkTorikomishoriDto);

    /**
     * 休暇情報一括取込ファイル設定
     *
     * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
     * @return IktkTorikomishoriDto
     */
    IktkTorikomishoriDto setKyukaJohoFile(IktkTorikomishoriDto iktkTorikomishoriDto);

    /**
     * 一括取込管理insert処理
     *
     * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
     * @return 一括取込管理ID
     */
    Long saveIktkKanri(IktkTorikomishoriDto iktkTorikomishoriDto);

    /**
     * CSVデータ反映処理
     *
     * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
     * @param iktkkanriId 一括取込管理ID
     */
    void execIkatsuTorikomiAsync(IktkTorikomishoriDto iktkTorikomishoriDto, Long iktkkanriId);

    /**
     * 休暇情報一括取込CSVデータ反映
     *
     * @param iktkTorikomishoriDto {@link IktkTorikomishoriDto}
     * @param iktkkanriId 一括取込管理ID
     */
    void execKyukaJohoTorikomiAsync(IktkTorikomishoriDto iktkTorikomishoriDto, Long iktkkanriId);

    /**
     * 取込状況一覧取得
     *
     * @param kaishaCd 会社コード
     * @param torikomiTaisho 取込対象
     * @param uploadbi アップロード日
     * @return {@link List<IktkTorikomishoriIchiranDto>}
     */
    List<IktkTorikomishoriIchiranDto> getTorikomiJyokyoIchiran(KaishaCd kaishaCd, String torikomiTaisho, LocalDate uploadbi);

    /**
     * 取込ログ取得
     *
     * @param iktkLogId 一括取込ログID
     * @return {@link IktkLog}
     */
    IktkLog getIktkLog(Long iktkLogId);
}