package ac.tcu.yc.pb.server;

/*
 *  プロジェクト用　マルチキャスト通信の＜送信側＞プログラム
 *   MultiCastSender.java
 * 
 * 動作について：
 * 　　このプログラムは、サーバーのIPアドレスと、開始の合図を
 * 　　クライアントに対してマルチキャスト通信で送信するプログラムです。
 * 
 * 
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

class MultiCastSender extends Thread {

    String UDP_HOST;
    int UDP_PORT;
    boolean[] StartQuiz;
    static final String McastAddr = "224.0.0.0"; // マルチキャストIPアドレス

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
    static final int BUFSIZE = 1024; // バッファサイズ

    public MultiCastSender(String UDP_HOST, int UDP_PORT, boolean[] StartQuiz) {
        this.UDP_HOST = UDP_HOST;
        this.UDP_PORT = UDP_PORT;
        this.StartQuiz = StartQuiz;
    }

    @Override
    public void run() {
        // 送信バッファ
        byte[] buf = new byte[BUFSIZE];
        // データ長
        int len;
        boolean loop = true;

        try {
            // IPアドレス設定
            InetAddress mAddr = InetAddress.getByName(McastAddr);
            // 送信データグラムパケット
            try ( // ソケットを開く DatagramPacket
                    MulticastSocket soc = new MulticastSocket()) {
                // 送信データグラムパケット
                DatagramPacket sendPacket = null;
                // 初期パケットの有効期間
                soc.setTimeToLive(1);
                System.out.println("マルチキャスト " + McastAddr + " にサーバー情報を送ります");
                while (loop) {
                    if (StartQuiz[0] != true) {
                        // 通常のパケットとして、UDPサーバーのIPアドレスを送る
                        String udpInfo = UDP_HOST;
                        // System.out.println("udpInfo: "+udpInfo);
                        
                        buf = udpInfo.getBytes();
                        len = buf.length;
                        sendPacket = new DatagramPacket(buf, len, mAddr, MPORT);
                        soc.send(sendPacket);
                    } else {
                        String startMessage = "StartQuiz!";
                        buf = startMessage.getBytes();
                        len = buf.length;
                        // System.out.println("startMessage: "+startMessage+" buf: "+buf+" len: "+len);
                        sendPacket = new DatagramPacket(buf, len, mAddr, MPORT);
                        soc.send(sendPacket);
                    }
                    
                    // 20ミリ秒休む
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                    }
                }
                // ソケットを閉じる
            }
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
    }
}
