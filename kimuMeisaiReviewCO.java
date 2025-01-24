public class KinmuMeisaiReviewCO extends OASecondController

public static final String RCS ID="$Header$";

public static final boolean RCS ID RECORDED = VersionInfo.recordClassVersion (RCS ID, "Xpackagename%");

//勤務環境変数

private static final String KINMU ENV CI TYOKA "KINMU TYOKA":

private static final String KINMU_ENV_C2_TYOKA "HOTEGAI GOKEI

private static final String KINMU ENV C3 TYOKA TOGETU

private static final String KINMU ENV CAT_TYOKA "TYOKA WARNING TIME":

private static final String KINMU ENV C42 TYOKA private static final String KINMU ENV C43 TYOKA TYOKA LIMIT TIME: TEKIYO KAIST DATE":

//申請種類

private static final String Meisai "1"; private static final String Entyo = "4";

//就業区分

private static final String Syukin

private static final String Ode = "3":

//ワーニングフラグ

//新規申請

//特別延長申請

private static final String Warn = "1"; //ワーニングあり

/11

Layout and page setup logic for a region.

@param pageContext the current D4 page context

@param webBean the web bean corresponding to the region

public void processRequestX(OAPageContext pageContext, OAWebBean webBean)

try

//パーソンIDの取得

String personld ProxyManager.getPersonId(pageContext);

int person_id= Integer.parseInt(personId);

KinmuAMImp myAm (KinmuAMImpl)pageContext.getApplicationModule(webBean);

//行員番号取得

String koin no myAm.getkoinNo (personld);

boolean butentyoCheckFig myAm.bushi tsutentyoChk (pageContext):


if(!butentyoCheckFlg)

butentys Check Fig myAm.kikakünk (1

ViewObject inputVO myäm.getKinmuinputVOL):

Kinmuinput/ VORowIncl inputValue (Kinmulnput VORowImp1) inputV0.first():

int tetMonth Integer.parseInt(inputValue.getTgtMonth());

MessageStyledTextBean bean (OAMessageStyledTextBean) webBean, find IndexedChi IdRecursive("Ihr Kinul ist Shihanki"); bean.setLabel (myAm.getTgtShikanki (tetMonth)):

OMessageStyledTextBean beanő (OAMessageStyledTextBean) web Bean, find Indexed Chi IdRecursivel Thrkinmushihankikaigai):

beans.setLabel (myAm.getTetShikanki (tatMonth)):

//警告時間の取得

//上限時間の取得

String strearningho myân, get inmuEnvValue(KINMJ ENV CI TYDKA, KINMI ENV C2 TYDKA KINMAU ENV C3 TYDKA, KINMU ENV C41 TYDKA):

String strLimitNo myAm.getKinmuEn Value(KINMU_ENV_CI_TYOKA, KINMU ENV_C2 TYOKA,KINMU ENV_CO_TYOKA, KINMU_ENV_042_TYOKA):

String strLimitNo2 myAn.getKinnuEnvValue (KINMU ENV_C1_TYOKA, KINMU ENV C2 TYOKA, KINMU ENV C3 TYOKA, KINMU ENV C42, ΤYΟΚΑ):

//上限時間2の取得

//警告メッセージに可変値(警告時間,上限時間)をセット Message Token[] token new Message Token [3];

token[0] new Message Token (JIKANT strWarningho):

token[1] = new MessageToken("JIKAN2 strLimitNo):

token[2] new Message Token (JIKANG, striimitNo21:

String keikokullsg pageContext.getMessage (IHR IHR SELF KINMU HIDE 041, token);

OAStaticStyled TextBean msg06 (OAStaticStyledTextBean wetBean, findChildRecursive("Ihr Message00");

ms06.setText (keikokuNsg):

String kinmubi CmnFunc2.convertJboDomainToString(inputValue.getKinmbi Input());

String tatYM kinmubi.substring(0,0);

//適用開始日の取得

String startDate myAn.getKinmuEnv Value (KINMU_ENV_D1_TYOKA, KINMU_ENV_02_TYOKA, KINMU_ENV_C3_TYCKA KINMU_ENV_043_TYDKA)

//法定外合計が基準の値を超えた時、警告文言を表示(労務管理:80時間遵守対応) if Imyle, gethinmulHotega Häntei (person id, kimmubil.equals(0)

Integer.parseInt(startDate) Integer.parseInt(kinmubi)

ms06.setRendered(false):

ス

データ戦略第三部・日立製作所

//人事企画Gr勤務担当者、勤務承認者・スタッフ人事による代理申請以外 (((Proxy Manager, isProxy(pageContext) & myAm.getkikakuStaffChk))

行員フリー席

PETZALTZ

DAFRowLayoutBean bean4 (DARowLayoutBean) webBean, find IndexedChi IdRecursivel ihr Nest25"):

bean.setRendered(false): DAStaticStyledTextBean bean5 (OAStaticStyledTextBean) webBean, find IndexedChi IdRecursive(ThrMessage27");

beans.setRendered(false):

//替店長・企画Grによるオペレーションの場合、承認者入力項目を非表示とする。

if (butentyoCheckFig

CanFunc2.getConst(C_RESP MUSS).equalsIgnoreCase(myAn.getLoginResp()) || CenFunc2.getConst(CRESP STAFF JINJI").equalsIgnoreCase (myAn.getLoginReso())

CAMessageStyledTextBean bean2 (DAMessageStyledTextBean]webBean, find IndexedChi Idlecursive("ThrkinmuRevSyonin");

bean2.setRendered(false):

OAMessageLov InputBean bean? (OMessageLov InputBean) webBean, find IndexedChi IdRecursive ("IhrkinmuinputBusitu): bean3.setRendered(false):

if(koin no substring(0,1).equals("P"))

/*行員番号がPから始まる行員の場合は非表示/

DAMessageStyledTextBean kanribean (OAMessageStyindTextBean) webBean. find IndexedChi IdRecursive("IhrkinmuRevkanri): kanribean.setRendered(false); DAMessageStyledTextBean sairyobean (OAMessageStyledTextBean) webBean, tind Indexed Chi IdRecursivel Ihr KineuRevSairyo);

sairyobean.setRendered(false):

Kinmu Input RecordVORowImol record Row (Kinmuinput RecordVDRowImpi) myAm, getkinmu Input RecordVD() first(): //契約社員のパートタイマーで、申請月内の労働時間(含有休)が80時間を超えている場合にワーニングを返す

if(equals (CanFunc.nv1 (inputValue.getWarn()))

pageContext, putDialogMessage(new DAException("IHR", inoutValue.Entwarn(), null, GAException WARNING, null)):

//契約社員で、勤務時間が契約時間より短い場合にワーニングを返す if(!equalis (DenFunc.nv (inputValue.getWarn10())))

pageContext.putDialogMessage(new DAException("IHR, InputValue.getRain10(), null, DAException. WARNING, null));

//在宅勤務申請日に8時~20時外の勤務をしている場合、ワーニングを返す If equals(CanFunc.nv1 (inputValue.getWarn11())))

pageContext.putDialogMessaninew Exception", inputValue. getlam111), null, OAException. WARSIING, null));

ハワーニングメッセージの表示を制御する

equala(Denfunc.nell inputValue.getWarml())))

if (butentyoCheckFig

//2011/09/10 契約社員の場合

CamFunc2.getConst(CKEIYAKU SYAIN SYOKUSYU3").equalsIgnoreCase(recordRow.getSyokusyu())

//2011/09/10 契約 //2011/09/10 契約社員の場合

pageContext.putDialogMessage (new DAException("THR", "IHR SELF KINMU INPUT_RARN 09", null, DAException, WARNING, null)

eise

pageContext.putDialogMessage(new OAException("IHR", inputValue.getWarni(), null, OAExcention. WARNING, nal

11.equals(CenFunc.nvl(inputValue.getWarn7(331)

pageContext.putDialogMessage(rew DAException("IHR", inputValue.getWarn7(), null, OAException, WARNING, null));

CAMessageChoiceBean poplist (OAMessageChoiceflean) webBean, f indChi IdRecursivel ThrKineuSaburokukbn":

poolist.setPickListCacheEnabled(false);

poplist.setListDisplayAttributel Meaning):

poplist.setListValueAttribute("Saburokukbn");

poplist.setPickListViewUsageName("KinmuSaburokullbnListV01:

if(ConFunc2.getConst("C_KEIYAKU SYAIN SYOKUSYUQ").equalsIgnoreCase(recordłow.getSyokutyul)))

DADefaultSingleColumnBean entyoßean (DADefaultSingleColumnBean) webBean, find IndexedChildRecursive("threntvoll ingle

enty Bean.setRendered(false);

DAStack LayoutBean hideflean (OtStackLayout Bean) webBean, findChi Idlecursive Ihr EntyaHide01):

hideBean, setRendered(false):

1

else

OADefaultSingletolumbean entyolean (04DefaultSingleColumnBean webbean, findIndexedChi IdRecursive! IhrEntyalingle01")

antwoBean.setRendered(false);

GAStack Layout Bean hideBean (04Stack LayoutBean) wetBean, findChildRecursive Ihr Entyort(de01"):
 hideBean.setRendered(false):

(OAStackLayoutBean) weblean.findChi IdRecursive("Ihr EntyoHide0111

if(".equals(DenFunc.nvl(inputValue.getWarn2())))

pageContext.putDialogMessage(new OAException("THR", inputValue.getWarn2(), null, OException, WARNING, null)):

".equals(CenFunc.nvl(inputValue.getWarn3())))

pageContext.putDialogMessage(new OAException IHR", InputValue.getWarn313, null, OAException. WARNING, null));

(1.equals(CanFunc.nvl(inputValue.getWarn4())))

pageContext.putDialogMessage(new DAException("IHR", InputValue.getWarn41), null. OAException. WARNING, na111):

1.equals(CoinFunc.nvl(inputValue.getWarn5()))

pageContext.putDialogMessage(new OAException("IHR", inputValue.getWarn(), null, OAException. INFORMATION, null)):

11.equals(CenFunc.nvl(inputValue.getWarnd())))

pageContext.putDialogMessage(new OAException("IHR", inoutValue.getWarn8(), null, OAException, INFORMATION, null)):

CenFunc.setUnvalidated (webBean, "ThrReturn", true);

(CenFunc2.getConst("C KETYAKU SYAIN SYOKUSYU3").equalsIgnoreCase (recordRow.getSyokusyud()))

//通勤費を石寄せにする

LOAMessageStyledTextBean/webBean, findChi IdRecursivel IhrkinmuRevMonTuk inhi")).setDataType("NUMBER"):

//登食補助を石寄せにする (DAMessageStyledTextBean) webBean, findChi IdRecursivel IhrkineulRevMonTyusyoku")).setDataType("NUMBER): //2640002515 201

H・1区分の表の制御 /

OATableBean waritable (QATableBean) webbean. f ind IndexedChi IdRecursivel The Kinmul, istharinasiThi"):

デフォルトの表レイアウトの振る舞いを設定する */ waritable.prepareForRender ing (pageContext);

列のフォーマットリストを取得する DataObjectList waricolumnFormat waritable.getColumFormats():


刊のフォーマットリストを取得する

OstalbiectList waricolumnformat waritable.getColumFormats

区分の折り返しを禁止する

DictionaryOata Hiben (DictionaryOatalearicolumnFormat.getItem(pageContext.findChildIndexiwaritable. "Thikinmevarimashi Hikan put (CELL NO WRAP FORMAT KEY, Boolean, FALSE); Hut (COLUMN DATA FORMAT REY, ICON BUTTON FORMAT):

★1匹分の折り返しを禁止する

DictionaryData Ikho (Dictionary@atalwaricolumnFormat.getItem(pageContext.findChildIndex(waritable. "Thrkines Revkarimasi):

Ikan put (CELL NO WRAP FORMAT KEY, Boolean, FALSE); Ikon pot (COLUMN DATA FORMAT KEY, ICON BUTTON FORMAT): Kon.put(WIDTH REY, "140");

10mmFunc2.getConst "C KELYARU SYAIN SYOKUSYUQ").equalsIgnoreCase(recordRow.entSyokusyu()))

CAMestiageStyledTextbean telierbean (CAMessageStyledTextBean) webBean, find Indexed Chi IdRecursive("Shekim Reviellar"

tellerbean.setRendered(false): OMessageStyledTextBean tuk inhibean tuk inhibean.setRendered(false): (DAMessageStyledTextBean) webbean, f ind IndexedChi (dRecursive("The Kinmulev Tuk inhi

OAMessageStyledTextBean rodoj ikanMonbean rodoj kanMonbean.setRendered(false); CAMessageStyledTextBean teilerMonbean (DAMessageStyledTextBean]webBean find IndexedChi IdRecursive("Ihr KineuReMonRodolikan"); (DAMessageStyledTextBean) webblean, find IndexedChi Idlecursivel TheKinmuRe-MonTeller):

tellerMonbean.setRendered(false): GMessageStyledTextBean tukinhi Monbean tuk inhi Monbean.setRendered(false); MessageStyledTextBean tyusyokubean (OAMessageStyledTextBean) webBean, find IndexedChi IdRecursive("IhrkinmuRevMonTukinh): (DAMessageStyledTextBean)webBean. find IndexedChi Idlecursive thrkinmuRevTyusyoku"):

tyusyokubean, setRendered(false): DAMessageStyledTextBean TyusyokuMonbean (DAMessageStyledTextBean) webBean, f ind IndexedChi IdRecursive!" Thu KineRevMonTyusyoku": TyusyokuMonbean.setRendered(false):
