package com.intc_service.confrimationapp;

import android.app.FragmentManager;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.intc_service.confrimationapp.Util.DataStructureUtil;
import com.intc_service.confrimationapp.Util.SettingPrefUtil;

import java.io.Serializable;
import java.util.ArrayList;

/*
 *  K-01 指示待ち画面
*/

public class WaitActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener {
    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private String mPanelNo = "0";        // 画面番号
    private boolean mRecieved = false;  // 手順書データ受信
    private String mProcedure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        // [P] 画面起動ログ取得

    }

    @Override
    protected void onStart() {
        super.onStart();

        // [P] 起動電文を作成
        DataStructureUtil ds = new DataStructureUtil();
        String mData = ds.makeSendData("20","");

        // [P] 起動を通知
        // TransmissionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // [P] 起動通知を送信
        sendFragment.send(mData);


        // TODO:[P] ログ取得

    }
    private static final String CMD61 = "61";
    private static final String CMD62 = "62";
    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals(CMD61)){ //起動応答
            if(bdRecievedData.getString("format").equals("TEXT")) {
                mPanelNo = bdRecievedData.getString("text");  // 画面番号を取得
            }

        }else if (cmd.equals(CMD62)){  // 手順書応答
            if(bdRecievedData.getString("format").equals("JSON")) {
                //ArrayList arrTejun = (ArrayList)bdRecievedData.getParcelableArrayList("tejun"); //手順書データを取り出す
                mProcedure=data;
                mRecieved=true;  // 手順書受信済みを設定
            }
        }

        onFinishRecieveProgress();   // 受信状況判定

        // TODO: [P] ログを取得

    }

    @Override
    public void onFinishTransmission(String data){

    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
        // System.out.println("ReqRecieved:"+data);
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        String mData = "";
        if(cmd.equals(CMD61)) { //起動応答
            if (bdRecievedData.getString("format").equals("TEXT")) {
                mPanelNo = bdRecievedData.getString("text");  // 画面番号を取得
                mData = dsHelper.makeSendData("50","");
            }
        }

        return mData;
    }
    @Override
    public void onFinishRecieveProgress(){
        // 受信状況判定

        DataStructureUtil dsHelper = new DataStructureUtil();
        //System.out.println(mPanelNo+":"+mRecieved);
        if(mPanelNo.equals("0") && mRecieved){
            // 画面番号が "0:指示待ち画面で、手順書受信済みの時 サーバーからの通知を待つ
            recieveFragment.listen();
        }
        else if(mPanelNo.equals("1") && mRecieved){
            // 画面番号が "1:手順書画面で、手順書受信済みの時 手順書画面へ

            recieveFragment.closeServer();
            Intent intent = new Intent(this, ProcedureActivity.class);

            intent.putExtra("proc", mProcedure);
            startActivity(intent);
        }
        //else if(mPanelNo.equals("1") && !mRecieved){
        else if(!mRecieved){
            // 手順書未受信のときは 21を送信
            String mData = dsHelper.makeSendData("21","");
            sendFragment.send(mData);
        }

    }
}
