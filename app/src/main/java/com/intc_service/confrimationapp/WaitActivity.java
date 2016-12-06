package com.intc_service.confrimationapp;

import android.app.FragmentManager;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.intc_service.confrimationapp.Util.DataStructureUtil;
import com.intc_service.confrimationapp.Util.alertDialogUtil;

/*
 *  K-01 指示待ち画面
*/

public class WaitActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        View.OnClickListener{
    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private String mPanelNo = "0";        // 画面番号
    private boolean mRecieved = false;  // 手順書データ受信
    private String mProcedure;

    private Button mBtnUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        // 画面更新ボタン
        mBtnUpdate = (Button) findViewById(R.id.btn_update);
        mBtnUpdate.setOnClickListener(this);

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

        if (cmd.equals(CMD61)) { //起動応答
            if (bdRecievedData.getString("format").equals("TEXT")) {
                mPanelNo = bdRecievedData.getString("text");  // 画面番号を取得
            }
            onFinishRecieveProgress(data);   // 受信状況判定
        } else if (cmd.equals(CMD62)) {  // 手順書応答
            if (bdRecievedData.getString("format").equals("JSON")) {
                //ArrayList arrTejun = (ArrayList)bdRecievedData.getParcelableArrayList("tejun"); //手順書データを取り出す
                mProcedure = data;
                mRecieved = true;  // 手順書受信済みを設定
            }
            onFinishRecieveProgress(data);   // 受信状況判定
        }else if (cmd.equals("9N")) {  // 画面更新（正常）
            // 受信待機済みのため 何もしない
        }else if (cmd.equals("9Q")) {  // 画面更新（異常）
            // 受信待機済みのため 何もしない
        }else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }


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
        String mData = "";
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals(CMD61)) { //起動応答
            if (bdRecievedData.getString("format").equals("TEXT")) {
                mPanelNo = bdRecievedData.getString("text");  // 画面番号を取得
                mData = dsHelper.makeSendData("50", "");
            }
        } else if (cmd.equals("9C")) {  // 電源OFF画面
            mData = "50@$";
        } else if (cmd.equals("91")) {  // 受信エラー処理 onFinishRecieveProgress で処理
            mData = "";
        } else if (cmd.equals("92")) {  // タイムアウト onFinishRecieveProgress で処理
            mData = "";
        }

        return mData;
    }
    @Override
    public void onFinishRecieveProgress(String data){
        // 受信状況判定

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        }else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※");
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※");
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }else {

            if (mPanelNo.equals("0") && mRecieved) {
                // 画面番号が "0:指示待ち画面で、手順書受信済みの時 サーバーからの通知を待つ
                recieveFragment.listen();
            } else if (mPanelNo.equals("1") && mRecieved) {
                // 画面番号が "1:手順書画面で、手順書受信済みの時 手順書画面へ

                recieveFragment.closeServer();
                Intent intent = new Intent(this, ProcedureActivity.class);

                intent.putExtra("proc", mProcedure);
                startActivity(intent);
            }
            //else if(mPanelNo.equals("1") && !mRecieved){
            else if (!mRecieved) {
                // 手順書未受信のときは 21を送信
                String mData = dsHelper.makeSendData("21", "");
                sendFragment.send(mData);
            }
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.btn_update: // 画面更新ボタンクリック
                // サーバーへ画面更新[90]送信
                DataStructureUtil dsHelper = new DataStructureUtil();
                String mData = dsHelper.makeSendData("90", "");
                sendFragment.send(mData);
        }
    }
}
