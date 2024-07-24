package ac.tcu.yc.pb.client;

// ①　説明文
/*
 * プログラミング演習Ｂ　　プロジェクト課題用
 *
 *  ＜＜　クライアント側プログラム　＞＞ 　Client_1a.java
 *
 * ＝＝＝＝＝  解答ボタンを押す順番や選択肢を、文字列としてサーバへ送信するプログラム  ＝＝＝＝＝
 *
 *                                                                    
 * 
 * (動作の解説）
 * １．起動後に、待機用画面が表示される。
 * ２．スレッドとして MultiCastReceiver（マルチキャスト通信の受信プログラム）を起動し、
 *     サーバ情報（IPアドレスとポート番号）を受信するモードに入り、受信したら保存する。
 * ３．さらに、マルチキャスト通信のポートを監視して、開始合図の情報を待つ。
 * ４．サーバ側で、開始ボタンが押されると、マルチキャスト通信で、
 *     開始合図の文字列"StartQuiz!"がクライアントに一斉に送られてくる。
 * ５．開始合図を受けとったら、クイズを表示する状態に切り替える。
 * ６．解答者がボタンをクリックして解答する（配布例では2問からなる）
 *　　　1）　問1：　正解と思う順に，ボタンを順番に２つクリックする．
 *　　　2）　問2：　正解と思う方のボタンをクリックする．   
 *　　　3)   上記の回答をすると，自動的に回答を送信する．　　
 * ７．解答情報（今回は「ボタンの番号を表す数字 3文字」）を，UDP通信によってサーバへ送信する。
 * （８．その後、サーバ側で、結果が集計される。）
 * 　　以上
 */

// ②　インポート文
//import java.awt.Toolkit;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.text.FontWeight;
import javafx.animation.*;
import javafx.util.Duration;
/**
 *
 * @author yokoi
 */
public class Client_1a extends Application implements Runnable {

// ③　フィールド変数
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@    要変更（ここから）　　 要変更（ここから）@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    ///////////////////////////////////////////////////////////////////////////
    // まず、UDP＿PORTの数字を、自分の学年・学籍番号によって下記の数値に書き換えること。
    // 解答送信用 UDPポート番号を（ 55000 + 学年×1000　＋　学番下３桁）　に設定する。
    // （例）2年生で、学番の下3桁が　123　である学生の場合：
    //       int UDP_PORT = 55000 + 2*1000 + 123;
    int UDP_PORT = 55000 + 4 * 1000 + 49;  
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@    要変更（ここまで）　　 要変更（ここまで）@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //
    // 接続先ホスト情報の設定文字列変数
    String[] UDP_HOST = new String[1];
    boolean[] StartQuiz = new boolean[1];
    int answer = -1;    //問題の正答番号
    int you=-1;
    //　一度解答を送信したら、二度送りしないために、この変数sentに送信済みであることを記録する。
    boolean sent = false;
    
    //マルチキャスト送信部品
    MultiCastReceiver mcr;

    Thread kick; // 自分をスレッドとして動作させるための部品
    //AudioClip ac[];
    //Image im[];
    //ImageView imv[];
    Timer timer;
    TimerTask tt;
    int time=20;//クイズの制限時間を設定する

    ////////////////////////////////////
    //　クライアント画面の構成
    //
    //　土台のパネル
    BorderPane bp = new BorderPane();
    // クイズ用部品を載せるパネル（開始までは不可視にする部品）
    BorderPane bp2 = new BorderPane();
    //ここでは部品を設定している
    Label l1 = new Label("変な家");//タイトル用のラベル
    TextArea tsa=new TextArea();//問題文を表示するテキストエリア
    TextField tfa=new TextField();//ユーザ名を入力するためのフィールド
    ToggleGroup tg;
    RadioButton[] rb=new RadioButton[4];//本プログラムでは、ラジオボタンを設定する
    Canvas cv=new Canvas(600,0);
    GraphicsContext gc=cv.getGraphicsContext2D();
    // 
    //　クイズ表示用のテキストエリア
    TextArea ta = new TextArea();
    // 
    //
    //　解答用ボタンを２個用意。

    Button btstart=new Button();
    
    // 
    //クリック回数カウント用変数
    int nc = 0;
    //問題の正解数
    int correctans=0;
    //問題のそれぞれの解答の文字列
    String yourans="f";
    private String user;

// ④　コンストラクタ  (インスタンス生成の最初に実行)
    public Client_1a() {
        // サーバ情報の初期化
        UDP_HOST[0] = "";       //  サーバIPアドレス用
        StartQuiz[0] = false;   //  開始合図の有無　（有：true　／　無:false）
    }

//　⑤	開始メソッド　start()
    @Override
    public void start(Stage stage) {
        //
        // 1. マルチスレッドの起動　（サーバ情報と開始合図の取得用）
        //
        //　サーバからのマルチキャスト通信を受信するためのスレッドを用意する。
        //　　スレッドには、「サーバIPアドレス」と、「開始合図値(true)」 を受信させる。
        mcr = new MultiCastReceiver(UDP_HOST, StartQuiz);
        //　スレッドを起動する。
        mcr.start();
        //*本プログラムはgithubに載せますが、画像ファイル、音楽ファイルは、入れていません。なので、githubから本プログラムを持ってきた方は、画像、音楽ファイルを指定の場所に置いてから実行してください
        //im=new Image[2];
        //Path pathi1=Paths.get("./images/p0001.png");
        //Path pathi2=Paths.get("./images/p0002.jpg");
        //String imageURL1=pathi1.toUri().toString();
        //String imageURL2=pathi2.toUri().toString();
        //im[0]=new Image(imageURL1);
        //im[1]=new Image(imageURL2);
        //imv=new ImageView[2];
        //imv[0]=new ImageView(im[0]);
        //imv[1]=new ImageView(im[1]);
        
        //ac=new AudioClip[4];
        //Path path1=Paths.get("./sounds/h01.mp3");
        //Path path2=Paths.get("./sounds/h02.mp3");
        //Path path3=Paths.get("./sounds/h03.mp3");
        //Path path4=Paths.get("./sounds/h04.mp3");
        //String soundURL1=path1.toUri().toString();
        //String soundURL2=path2.toUri().toString();
        //String soundURL3=path3.toUri().toString();
        //String soundURL4=path4.toUri().toString();
        //ac[0]=new AudioClip(soundURL1);
        //ac[1]=new AudioClip(soundURL2);
        //ac[2]=new AudioClip(soundURL3);
        //ac[3]=new AudioClip(soundURL4);
        //ラジオボタンの設定
        tg=new ToggleGroup();
        for(int i=0;i<rb.length;i++){
            rb[i]=new RadioButton();
            rb[i].setToggleGroup(tg);
            rb[i].setStyle("-fx-background-color:white;-fx-pref-width:200px;-fx-pref-height:40px");
        }
        tsa.setEditable(false);
        tfa.setVisible(false);
        tfa.setManaged(false);
        l1.setFont(Font.font("Serif",30));
        l1.setTextFill(Color.WHITE);
        final String Content="こんにちは、雨穴です。\nこれからあなたには謎を解いてもらいます\n解けないと、・・・\n恐ろしいことが起こるかもしれません。";
        final Animation animation=new Transition(){
            {
                setCycleDuration(Duration.millis(10000));
                setCycleCount(1);
            }
            @Override
            protected void interpolate(double frac){
                final int length=Content.length();
                final int n=Math.round(length*(float)frac);
                tsa.setWrapText(true);
                tsa.setText(Content.substring(0,n));

            }
        };
        animation.setOnFinished(event->{
            btstart.setDisable(false);
        });
        animation.play();
        btstart.setText("クイズを開始する");
        btstart.setFont(Font.font(18));
        btstart.setDisable(true);
        // 3. GUI画面の構築
        /////////////////////////
        // 上側のパネル（タイトルや待機メッセージ）の設定
        VBox vb1 = new VBox();
        vb1.getChildren().add(l1);
        vb1.getChildren().add(cv);
        vb1.getChildren().add(tsa);
        vb1.getChildren().add(tfa);
        for(int i=0;i<rb.length;i++){
            vb1.getChildren().add(rb[i]);
            rb[i].setOnAction(new buttonputEventHandler());
            rb[i].setVisible(false);
            rb[i].setManaged(false);
        }
        //vb1.getChildren().add(imv[1]);
        vb1.setAlignment(Pos.CENTER);
        vb1.setSpacing(10);
        //　下側のパネル（問題文や、解答用ボタン用）の設定
        VBox vb2 = new VBox();
        vb2.getChildren().add(ta);  //  問題文表示用部品
        
        HBox hb=new HBox();
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(btstart);
        btstart.setOnAction(new startEventHandler());
        
        HBox hb2=new HBox();
        
        // 開始の合図までは、問題文と解答ボタンの土台パネルを不可視にする。
        //
        bp2.setVisible(false);
        bp2.setManaged(false);
        bp2.setCenter(vb2);
        bp.setBottom(hb);
        bp.setTop(vb1);
        bp.setCenter(bp2);
        bp.setStyle("-fx-background-color:black");
        //
        //表示サイズ変更
        Scene sc = new Scene(bp, 600, 600);

        //ステージへの追加
        stage.setScene(sc);

        //ステージの表示
        stage.setX(650);
        stage.setY(10);
        stage.setTitle("Client 2024");
        stage.show();

        // 6. スレッドとして、自分を起動
        //　　
        if (kick == null) {
            kick = new Thread(this);
            kick.start();
        }
    }
    class buttonputEventHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent e){
            btstart.setVisible(true);
            RadioButton tmp=(RadioButton)e.getSource();
            String selectedText=tmp.getText();
            for(int i=0;i<rb.length;i++){
                if(rb[i].getText().equals(selectedText)){
                    you=i+1;
                }
            }
        }
    }
    class startEventHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent e){
            System.out.println("btstartが押されました");
            tsa.setText("");
            nc++;
            clicked();
        }
    }
// ⑥	clicked（　）　（解答ボタンに共通の処理）
    //　ボタン１かボタン２が押されたときに呼ばれる共通のメソッド
    private void clicked() {
        String Content;
        Animation animation;
        //++++++++++++++++++++++++++++++++
        //　今回、複数問題の解答形式は、下記を仮定しておきます。
        //  １問めは、早い順で２クリック、　
        //　２問めは、択一問題で、１クリックとする。
        //　よって、３クリックめの時に「送信」となります。
        //　
        //++++++++++++++++++++++++++++++++
        switch (nc) {
      
            case 1:
                //ac[0].play();
                //imv[1].setVisible(false);
                //imv[1].setManaged(false);
                //ユーザ名を入力
                Content="まずは、ユーザ名を入力してください\nユーザ名は5文字以上でお願いします\n*個人情報はのせないでください";
                btstart.setVisible(false);
                animation=new Transition(){
                    {
                        setCycleDuration(Duration.millis(1000));
                        setCycleCount(1);
                    }
                    @Override
                    protected void interpolate(double frac){
                        final int length=Content.length();
                        final int n=Math.round(length*(float)frac);
                        tsa.setWrapText(true);
                        tsa.setText(Content.substring(0,n));
                    }
                };
                animation.setOnFinished(event->{
                   tfa.setVisible(true);
                   tfa.setManaged(true);
                   if(tfa.getText().length()>=5){
                        btstart.setVisible(true);
                    }
                });
                tfa.textProperty().addListener((observable,ov,nv)->{
                    UpdateButton(tfa,btstart);
                });
                animation.play();
                break;
            case 2:
                //ac[3].play();
                //一問目の問題
                user=tfa.getText();
                tfa.setVisible(false);
                tfa.setManaged(false);
                btstart.setVisible(false);
                Content="第一問\n変な家の作者の名前に含まれている天気はどれでしょう";
                quiztextdisplay(Content,"雪","雨","晴","霰",2);
                break;
            case 4:
                time=20;
                btstart.setVisible(false);
                //ac[3].play();
                Content="第2問\n2024年に映画化された変な家の主題歌は、誰が歌ったでしょう";
                quiztextdisplay(Content,"米津玄師","Official髭男dism","アイナ・ジ・エンド","Aimer",3);
            break;
            case 6:
                //ac[3].play();
                time=20;
                btstart.setVisible(false);
                Content="第3問\n変な家の作者の作品として正しいものは次のうちどれでしょう";
                quiztextdisplay(Content,"変な絵","変な森","変な空気","変な仏",1);
                break;
            case 8:
                //ac[3].play();
                time=20;
                btstart.setVisible(false);
                Content="第4問\n映画「変な家」に出演していないのは次のうち誰でしょう";
                quiztextdisplay(Content,"R-指定","DJ松永","間宮祥太郎","川栄李奈",1);
                break;
            case 10:
                //ac[3].play();
                time=20;
                btstart.setVisible(false);
                Content="第5問\n変な家に出てくる栗原さんの説明として正しいものはどれか?";
                quiztextdisplay(Content,"事件の鍵を握る謎の女性である","白い仮面を被っている","映画ではDJ松永さんが努めた","ミステリー愛好家である",4);
                break;
            case 11:
                btstart.setText("送信する");
            case 3:
            case 5:
            case 7:
            case 9:
                //解答結果
                tt.cancel();
                timer.cancel();
                //ac[1].play();
                cv.setHeight(0);
                for(int i=0;i<rb.length;i++){
                    rb[i].setVisible(false);
                    rb[i].setManaged(false);
                }
                yourans+=you;
                String judge;
                if(you==answer){
                    judge="正解";
                    correctans+=1;
                }else{
                    judge="不正解";
                }
                Content=judge+"です";
                animation=new Transition(){
                    {
                        setCycleDuration(Duration.millis(5000));
                        setCycleCount(1);
                    }
                    @Override
                    protected void interpolate(double frac){
                        final int length=Content.length();
                        final int n=Math.round(length*(float)frac);
                        tsa.setWrapText(true);
                        tsa.setText(Content.substring(0,n));
                    }
                };
                animation.play();
                break;
            case 12:
                //ac[2].play();
                //imv[1].setVisible(true);
                try{
                    InetAddress ia=InetAddress.getByName(UDP_HOST[0]);
                    DatagramSocket ds=new DatagramSocket();
                    String myMessage="1.user:"+user+"/2.answer:"+yourans+"/3.correct:"+correctans+"/";
                    byte buffer[]=myMessage.getBytes();
                        
                    DatagramPacket dp=new DatagramPacket(buffer,buffer.length,ia,UDP_PORT);
                    ds.send(dp);
                    sent=true;
                    Content="お疲れ様です"+user+"さん\nまた遊びに来てくださいね";
                    animation=new Transition(){
                    {
                        setCycleDuration(Duration.millis(5000));
                        setCycleCount(1);
                    }
                    @Override
                    protected void interpolate(double frac){
                        final int length=Content.length();
                        final int n=Math.round(length*(float)frac);
                        tsa.setWrapText(true);
                        tsa.setText(Content.substring(0,n));
                    }
                };
                    animation.play();
               }catch(IOException e){
                    System.out.println(e.getMessage());
               }                
               break;
            default:
                break;
        }
    }
    private void quiztextdisplay(String Content,String t1,String t2,String t3,String t4,int ans){
        Animation animation=new Transition(){
            {
                setCycleDuration(Duration.millis(5000));
                setCycleCount(1);
            }
            @Override
            protected void interpolate(double frac){
                final int length=Content.length();
                final int n=Math.round(length*(float)frac);
                tsa.setWrapText(true);
                tsa.setText(Content.substring(0,n));
            }
        };
        animation.setOnFinished(event->{
            rb[0].setText(t1);
            rb[1].setText(t2);
            rb[2].setText(t3);
            rb[3].setText(t4);
            solvetimer();
            answer=ans;
            for(int i=0;i<rb.length;i++){
                rb[i].setVisible(true);
                rb[i].setManaged(true);
            }
        });
        animation.play();
    }
    private void UpdateButton(TextField tfa,Button btstart){
        if(tfa.getText().length()>=5){
            btstart.setVisible(true);
            btstart.setText("次へ");
        }
    }
    void solvetimer(){
        cv.setHeight(40);
        timer=new Timer();
        tt=new TimerTask(){
            public void run(){
                if(time==0){
                    nc++;
                    tt.cancel();
                    timer.cancel();
                    clicked();
                }
                else{
                    time-=1;
                    drawTimebar();
                }
            }
        };
        timer.schedule(tt, 0,1000);
        
    }
    void drawTimebar(){
        double width=cv.getWidth();
        double height=cv.getHeight();
        double progress=(double)time/20;
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0,width,height);
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, width*progress, height);
    }
    // ⑦ run（　）　（開始合図の受信、開始後はアニメーション表示など）
    @Override
    public void run() {
        while (true) {

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            // (ウ)	以上を無限ループで継続
        }
    }
    @Override
    public void stop() throws Exception {
        System.out.println("stopped...");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
