package com.intc_service.confrimationapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.intc_service.confrimationapp.Util.DataStructureUtil;

/*
 *  K-04 終了画面
*/

public class EndActivity extends AppCompatActivity
        implements ReceptionFragment.ReceptionFragmentListener{

    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private ReceptionFragment recieveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        // ReceptionFragment を　生成
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        recieveFragment.listen();
    }

    @Override
    public String onRequestRecieved(String data) {
        // サーバーからの要求（data）を受信
        String mData = "";
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        if (cmd.equals("9C")) {  // 電源OFF画面
            mData="50@$";
         }
        return mData;
    }

    @Override
    public void onFinishRecieveProgress(String data) {
        // サーバー発呼のコマンド送受信後の処理
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        }
    }
}
