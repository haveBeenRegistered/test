/**
 * 在籍組織ID
 */
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BC_ZAISEKI_SOSHIKI_SEQ")
@SequenceGenerator(name = "BC_ZAISEKI_SOSHIKI_SEQ", allocationSize = 1, schema = "schema_name")
@Column(name = "ZAISEKI_SOSHIKI_ID")
private Long zaisekiSoshikiId;

/** 主務兼務区分 */
@Column(name = "SHUMU_KENMU_KBN")
private ShumukenmuKbn shumukenmuKbn;

/** 役職コード */
@Column(name = "YAKUSHOKU_CD")
private String yakushokuCd;

/** 有効期間(開始) */
@Column(name = "VALID_PERIOD_SD")
private LocalDate validPeriodSd;

/** 有効期間(終了) */
@Column(name = "VALID_PERIOD_ED")
private LocalDate validPeriodEd;

/** 権限種別区分 */
@Column(name = "KENGEN_SHUBETSU_KBN")
private KengenShubetsuKbn kengenShubetsuKbn;

/** 在籍フラグ */
@Column(name = "ZAISEKI_FLG")
private Boolean zaisekiFlg;

/** 地域 */
@Column(name = "CHIKI")
private Chiki chiki;

/** 業務共通ユーザー */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "USER_ID")
private User user;

/** 業務共通部店 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "BUTEN_ID")
@Where(clause = "DELETE_FLG = false")
private Buten buten;

/** 業務共通課グループ */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "KA_GROUP_ID")
@Where(clause = "DELETE_FLG = false")
private KaGroup kaGroup;

@Override
public Supplier<Long> getIdSupplier() {
    return () -> zaisekiSoshikiId;
}