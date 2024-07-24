package ac.tcu.yc.pb.client;

/*
 *  プロジェクト用　マルチキャスト通信の＜受信側＞プログラム
 *   MultiCastReceiver.jav
 *
 * 動作について：
 * 　　このプログラムは、サーバからネットワーク経由で送られるIPアドレスと、
 *    開始合図を受け取り、自分の呼び出し元の　Client_1a.java　に、
 *    その情報を伝えるプログラムです。
 * 
 * 
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

class MultiCastReceiver extends Thread {

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@    要変更（ここから）　　 要変更（ここから）@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // マルチキャスト用のMPORTの数字を、自分の学年・学籍番号によって下記の数値に書き換えること。
    // マルチキャスト用ポート番号を（ 60000 + 学年×1000　＋　学番下３桁）　に設定する）
    // （例）2年生で、学番の下3桁が　123　である学生の場合：
    //       int MPORT = 60000 + 2＊1000 + 123;
    int MPORT = 60000 + 4 * 1000 + 49;   //これは教員用の設定です．必ず変更しましょう．
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@    要変更（ここまで）　　 要変更（ここまで）@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    static final String McastAddr = "224.0.0.0";        // マルチキャストIPアドレス
    static final int BUFSIZE = 1024;                    // バッファサイズ
    String[] UDP_HOST = new String[1];
    boolean[] StartQuiz;

    public MultiCastReceiver(String[] UDP_HOST, boolean[] StartQuiz) {
        this.UDP_HOST[0] = UDP_HOST[0];
        this.StartQuiz = StartQuiz;
    }

    @Override
    public void run() {
        String recvData;                                // 受信データ
        byte[] buf = new byte[BUFSIZE];                // 受信バッファ
        int len;                                        // データ長

        boolean loop = true;

        try {                                           // IPアドレス設定
            InetAddress mAddr = InetAddress.getByName(McastAddr);
            try (MulticastSocket soc = new MulticastSocket(MPORT) // ソケットを開く
            ) {
                DatagramPacket recvPacket = new DatagramPacket(buf, BUFSIZE);
                // データグラムパケット設定
                soc.joinGroup(mAddr);                       // マルチキャスト内に参加する
                System.out.println("マルチキャスト " + McastAddr + " に参加します");
                while (loop) {
                    soc.receive(recvPacket);                // サーバからデータを受信
                    recvData = new String(recvPacket.getData());
                    recvData = recvData.trim();             // トリミング
                    
                    // System.out.println("recvData: " + recvData);
                    if (recvData.contains("StartQuiz!")) {
                        if (StartQuiz[0] == false) {
                            StartQuiz[0] = true;
                        }
                    } else {
                        if (UDP_HOST[0] == null) {
                            UDP_HOST[0] = recvData.substring(0);
                            System.out.println("UDP_HOST[0]: " + UDP_HOST[0]);           // 画面に出力
                        }
                    }
                }   soc.leaveGroup(mAddr); // マルチキャスト内から離れる
                // ソケットを閉じる
            }
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
    }
}
