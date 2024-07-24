package ac.tcu.yc.pb.server;

//　①　説明文
/*
 * プログラミング演習Ｂ　　　プロジェクト課題用
 *
 * 【　サーバー側プログラム　】
 *
 * ＝＝＝＝＝  クライアントごとの「解答のボタンの順番の正しさ」を判定するプログラム  ＝＝＝＝＝
 * 
 *                                                                    
 * (動作の解説）
 *                                                                   
 * １．起動するとJFrameが現れる。
 *                                                                   
 * ２．スレッドとして起動したMultiCastSender.java（マルチキャスト送信プログラム）は、
 *     解答のため待機しているクライアントに対し、一定時間ごとに繰り返して、
 *     サーバ(自分）のIPアドレスと、解答用のUDP通信用のポート番号を送信する。
 *                                                                   
 * ３．「開始」ボタンを押すと，マルチキャスト通信によって、クライアントに対して
 * 　　開始合図が送られる（各クライアントでは、合図を受信するとクイズが表示され
 *     解答処理に入る）。
 *                                                                   
 * ４．開始時刻から「　TIME_LIMIT　」秒の間、クライアントからの解答を待つ
 *    （Timerを使用）。
 *                                                                   
 * ５．解答を受信すると、「受信時刻」・「クライアントIPアドレス」・「解答文字列」を、
 *    それぞれ配列変数に保存する。
 *                                                                   
 * ６．TIME_LIMIT 秒が経過したら、受信を中止し、集計処理に移る（正解者表示など）。
 *                                                                   
 * ７．for文を使ってすべてのクライアントの解答を調べて、結果を表示する。
 *    （なお、配列のデータの順番は、解答を早く受け取った順番でもあることでもある。）
 *                                                                   
 * ８．表示の状態を維持する。
 *                                                                   
 */
//　②　インポート文
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.*;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;

import org.jfree.chart.*;
import org.jfree.data.general.*;
import org.jfree.data.category.*;
import org.jfree.chart.plot.*;


public class Server_1a extends Application implements Runnable {

//　③　フィールド変数
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@    要変更（ここから）　　 要変更（ここから）@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    ///////////////////////////////////////////////////////////////////////////
    // まず、UDP＿PORTの数字を、自分の学年・学籍番号によって下記の数値に書き換えること。
    // 解答送信用 UDPポート番号を（ 55000 + 学年×1000　＋　学番下３桁）　に設定する。
    // （例）2年生で、学番の下3桁が　123　である学生の場合：
    //       int UDP_PORT = 55000 + 2*1000 + 123;
    int UDP_PORT = 55000 + 4 * 1000 + 49;  //これは教員用の設定です．必ず変更しましょう．
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@    要変更（ここまで）　　 要変更（ここまで）@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //
    //　サーバの情報用変数
    InetAddress ia;
    //
    //　UDP_HOST : サーバのIPアドレス用変数
    String UDP_HOST = "";
    //
    // 解答回収用のバッファのサイズ（受信データの臨時保存用）
    private final static int BUFSIZE = 512;
    //
    //
    //マルチキャスト送信部品
    MultiCastSender mcs;
    //
    // 自分をスレッドとして動作させるための部品
    Thread kick;
    //
    //Image im[]; // 画像ファイルから読み込んだ画像を入れて使用するためのオブジェクト （今回は配列として用意）
    //ImageView imv[];   // 画像をアイコンとして使うための入れ物(アイコン部品）
    //AudioClip ac[]; // 音声ファイルを入れるためのオブジェクト（今回は配列として用意）
    //

    //　全解答者数用変数　numberOfAnswers
    int numberOfAnswers = 0;
    //
    int maxMember = 100;  //最大人数の設定
    // isQuizStarted : クイズが開始したかどうかを表すブーリアン型変数。
    boolean[] StartQuiz;
    String sendtext;
    String[] users=new String[maxMember];
    int[] mcorrectstore=new int[maxMember];
    int[][] qyans=new int[maxMember][5];
    int[] correct={2,3,1,1,4};
    // JFreeChart 部品
    JFreeChart chart;
    ChartFrame frame;
    
    //　GUIのパネル用部品の宣言。
    BorderPane bp = new BorderPane();
    BorderPane p1 = new BorderPane();
    BorderPane p2 = new BorderPane();
    BorderPane p3 = new BorderPane();
    BorderPane p4 = new BorderPane();
    Label l1 = new Label("変な家");
    TextArea ta = new TextArea();
    Canvas cv = new Canvas(400, 300);
    Button bt=new Button();
    Button bt2=new Button();
    Button bt3=new Button();
    Timer timer;
    TimerTask tt;
    int time=0;
    // 
    
    //　④　コンストラクタ 最初に呼ばれるメソッド
    public Server_1a() {
        // このプログラムでは何もしていません．
    }

    //　⑤　開始メソッド　start()
    @Override
    public void start(Stage stage) {
        ta.setEditable(false);
        //（ア）用意しておいた画像をimgに読み込み、プログラム中で利用できるようにする。
        //im=new Image[1];
        //Path pathi1=Paths.get("./images/p0001.jpg");
        //String imageURL1=pathi1.toUri().toString();
        //im[0]=new Image(imageURL1);
        //imv=new ImageView[1];
        //imv[0]=new ImageView(im[0]);
        // 用意しておいた音声をac[]に読み込み、プログラム中で利用できるようにする。
        //ac = new AudioClip[2];
        //Path path1=Paths.get("./sounds/h0001.mp3");
        //Path path2=Paths.get("./sounds/h0002.mp3");
        //String soundURL1=path1.toUri().toString();
        //String soundURL2=path2.toUri().toString();
        //ac[0]=new AudioClip(soundURL1);
        //ac[1]=new AudioClip(soundURL2);
        ////////////////////////////////
        //
        // 1. 自分のIPアドレス取得など
        //////////////////////////////
        // ホストアドレス、ポート番号の表示
        try {
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("getLocalHost Error!!");
        }
        UDP_HOST = ia.getHostAddress();
        //
        //////////////////////////////
        //解答用配列の初期化
        StartQuiz = new boolean[1];
        StartQuiz[0] = false;
        l1.setFont(Font.font("Serif",30));
        l1.setTextFill(Color.WHITE);
        // 2. GUI画面構築
        //////////////////////////////
        //
        VBox vb = new VBox();
        vb.getChildren().add(l1);
        vb.getChildren().add(ta);
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(10);
        //vb.getChildren().add(imv[0]);
        p1.setCenter(vb);
        bp.setTop(p1);
        HBox hb=new HBox();
        hb.getChildren().add(bt);
        hb.getChildren().add(bt2);
        hb.getChildren().add(bt3);
        hb.setSpacing(20);
        bt.setOnAction(new buttonputEventHandler());
        bt.setText("問題別正答率");
        bt2.setOnAction(new buttonputEventHandler2());
        bt2.setText("問題別選択肢選択率");
        bt3.setOnAction(new buttonputEventHandler3());
        bt3.setText("ユーザ別正解数");
        hb.setAlignment(Pos.CENTER);
        bp.setBottom(hb);
        
        bp.setStyle("-fx-background-color:black");
        //
        //表示サイズ変更
        Scene sc = new Scene(bp, 600, 500);

        //ステージへの追加
        stage.setScene(sc);

        //ステージの表示
        stage.setX(10);
        stage.setY(10);
        stage.setTitle("Server 2024");
        stage.show();

        // 3.　マルチキャストプログラムをスレッドとして起動
        ////////////////////////////////
        // マルチキャスト通信を使って、サーバのIPアドレスと、UDP通信用ポート番号を送る。
        mcs = new MultiCastSender(UDP_HOST, UDP_PORT, StartQuiz);
        //　スレッドとして起動する。
        mcs.start();

        //

        //

        //
        // 5. 自分をスレッドとして起動
        ////////////////////////////////
        //自分用スレッドの準備
        if (kick == null) {
            kick = new Thread(this); // 自分をThreadとして登録し起動。
            kick.start();
        }
    }

    //
    //　⑥　run( ) メソッド （開始ボタンのクリックで呼ばれる）
    // クイズのスタートボタンの処理
    @Override
    public void run() {
        /*
        timer=new Timer();
        tt=new TimerTask(){
          public void run(){
              if((time/10)%2==0){
                  ac[1].stop();
                  ac[0].play();
              }
              else{
                  ac[0].stop();
                  ac[1].play();
              }
              time+=1;
          }  
        };
        timer.schedule(tt,0,1000);
        */
        DatagramSocket ds=null;
        try{
            ds=new DatagramSocket(UDP_PORT);
            byte buffer[]=new byte[BUFSIZE];
            while(true){
                try{
                    DatagramPacket dp=new DatagramPacket(buffer,buffer.length);
                    ds.receive(dp);
                    sendtext=new String(dp.getData());
                    System.out.println(sendtext);
                    String userPattern="1.user:(.*?)/";
                    String youransPattern="2.answer:f(.*?)/";
                    String correctansPattern="3.correct:(.*?)/";
                    String user=extractPattern(userPattern,sendtext);
                    String yourans=extractPattern(youransPattern,sendtext);
                    String correctans=extractPattern(correctansPattern,sendtext);
                    int icorrectans=Integer.parseInt(correctans);
                    users[numberOfAnswers]=user;
                    mcorrectstore[numberOfAnswers]=icorrectans;
                    ta.appendText("No,"+numberOfAnswers+":"+users[numberOfAnswers]+"さんは、5問中"+mcorrectstore[numberOfAnswers]+"問正解することができました\n");
                    int iyourans=Integer.parseInt(yourans);
                    for(int i=0;i<5;i++){
                        qyans[numberOfAnswers][i]=iyourans/(int)Math.pow(10, 5-i-1);
                        iyourans-=qyans[numberOfAnswers][i]*(int)Math.pow(10,5-i-1);
                    }
                    numberOfAnswers++;
                    
                }catch(IOException e){
                    System.err.println(e.getMessage());
                }
            }
        }catch(SocketException e){
            System.err.println(e.getMessage());
        }
        
    }
    public static String extractPattern(String pattern,String text){
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(text);
        if(m.find()){
            return m.group(1);
        }
        return null;
    }

    class buttonputEventHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent e){
            int[] cquestionnum=new int[6];
            for(int i=0;i<numberOfAnswers;i++){
                cquestionnum[mcorrectstore[i]]++;
            }
            ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
            DefaultPieDataset dataset=new DefaultPieDataset();
            for(int i=0;i<6;i++){
                dataset.setValue(i+"問正解", cquestionnum[i]);
            }
            chart=ChartFactory.createPieChart(
                "問題別正解数",
                dataset,
                true,
                true,
                false
            );
            
            frame=new ChartFrame("問題別正解数",chart);
            frame.pack();
            frame.setLocation(10,300);
            frame.setVisible(true);
        }
    }
    class buttonputEventHandler2 implements EventHandler<ActionEvent>{
        public void handle(ActionEvent e){
            
            int[][] qselectnum=new int[5][4];
            //qyans[解答者番号][問題番号-1]には、問題に対するそれぞれの選択肢が格納されている。
            for(int i=0;i<numberOfAnswers;i++){
                for(int j=0;j<5;j++){
                    qselectnum[j][qyans[i][j]-1]++;
                }
            }
            String[] category=new String[5];
            String[] series=new String[4];
            for(int i=0;i<5;i++){
                category[i]=(i+1)+"問目";
            }
            for(int i=0;i<4;i++){
                series[i]="選択肢"+(i+1);
            }
            ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
            DefaultCategoryDataset dataset=new DefaultCategoryDataset();
            for(int i=0;i<5;i++){
                for(int j=0;j<4;j++){
                    dataset.addValue(qselectnum[i][j], series[j], category[i]);
                }
            }
            chart=ChartFactory.createBarChart(
                    "問題別選択肢の選択の詳細",
                    "問題",
                    "選択人数",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            frame=new ChartFrame("問題別選択肢選択の詳細",chart);
            frame.pack();
            frame.setLocation(10,300);
            frame.setVisible(true);
        }
    }
    class buttonputEventHandler3 implements EventHandler<ActionEvent>{
        public void handle(ActionEvent e){
            String series="正解数";
            ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
            DefaultCategoryDataset dataset=new DefaultCategoryDataset();
            for(int i=0;i<numberOfAnswers;i++){
                dataset.addValue(mcorrectstore[i], series, users[i]);
            }
            chart=ChartFactory.createBarChart(
                    "ユーザ別正解数",
                    "ユーザ",
                    "正解数",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            frame=new ChartFrame("ユーザ別正解数",chart);
            frame.pack();
            frame.setLocation(10,300);
            frame.setVisible(true);
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
//残りやること
//問題別選択肢選択率の目盛りを1ずつに変える
//ユーザ別正解数の目盛りを1ずつに変える。/そして最大の目盛りを5にする。棒グラフの棒の横幅を設定する
