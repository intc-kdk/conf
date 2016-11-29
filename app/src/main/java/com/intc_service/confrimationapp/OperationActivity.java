package com.intc_service.confrimationapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.intc_service.confrimationapp.Util.DataStructureUtil;
import com.intc_service.confrimationapp.Util.OperationDataUtil.OpeItem;
import com.intc_service.confrimationapp.Util.alertDialogUtil;

/*
 *  K-03 盤操作画面
*/

public class OperationActivity extends AppCompatActivity
        implements OperationFragment.OnListFragmentInteractionListener, TransmissionFragment.TransmissionFragmentListener,
        ReceptionFragment.ReceptionFragmentListener, View.OnClickListener{
    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private Bundle mBundleCur = new Bundle();
    private Bundle mBundlePair = new Bundle();

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private OperationFragment mOpeFragment;

    private int returnSno = 0;
    private String returnGs;
    private String returnTxGs;
    private String returnTime;

    private String mGs="0";
    private Button mBtnGs;
    private boolean noTap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        noTap = true;

        //  手順書フラグメントの取得
        mOpeFragment = (OperationFragment)getSupportFragmentManager()
                .findFragmentById(R.id.OperetionList);
        // ヘッダ部へ設定

        // ヘッダ部へ設定
        setTextView();

        // TransmissionFragment/ReceptionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // 現場差異ボタン
        mBtnGs = (Button) findViewById(R.id.btn_gs_ope);
        mBtnGs.setOnClickListener(this);

        recieveFragment.listen();

        // 現場差異の指示を確認
        Intent intent = getIntent();
        mGs = intent.getStringExtra("gsmode");
        // 現場差異表示
        setGenbaSai();

    }
    private void setTextView() {

        // ヘッダー部へ設定
        TextView tvNo = (TextView)findViewById(R.id.title_proc_no);
        TextView tvPlace = (TextView)findViewById(R.id.title_proc_place);
        TextView tvAction = (TextView)findViewById(R.id.title_proc_action);
        TextView tvRemarks = (TextView)findViewById(R.id.title_proc_remarks);

        OpeItem item = mOpeFragment.getCurrentItem();

        tvNo.setText(item.tx_sno);
        tvPlace.setText(item.tx_s_l);
        tvAction.setText(item.tx_action);
        tvRemarks.setText(item.tx_biko);


    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    @Override
    public void onListFragmentInteraction(OpeItem item) {

    }
    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        // コマンド[22]応答受信
        if (cmd.equals("60")) {
            returnTime = bdRecievedData.getString("ts_b");
            sendFragment.halt("99@$");

        } else if (cmd.equals("6N")) {
            // コマンド[23]応答受信
            if (bdRecievedData.getString("format").equals("TEXT")) {
                if (mGs.equals("1")) {  // SKIP
                    // スキップの時はそのまま戻る
                    OpeItem item = mOpeFragment.getCurrentItem();
                    returnSno = item.in_sno;
                    returnTime = "";
                    returnGs = "True";
                    returnTxGs = "スキップ";

                    sendFragment.halt("99@$");

                } else if (mGs.equals("2")) {  //追加
                    // bo_gs tx_gs を更新して、盤操作可にして継続
                    mOpeFragment.updateGs("追加");
                    mGs = "0";
                } else {  //  キャンセル  # ここは通らない

                }

                // メッセージ消す
                mGs = "0";
                setGenbaSai();
                noTap=true; //連続タップ抑止解除
            }
        }else if (cmd.equals("6R")) { //現場差異応答(拒否）
            noTap = true; //連続タップ抑止解除
        }else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("99")) {  // サーバークローズ
            recieveFragment.closeServer(); //待ち受けを中止する。
            returnProcedureActivity();
        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment ,getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment , getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }

        // TODO: [P] ログを取得


    }
    @Override
    public void onFinishTransmission(String data){

    }
    @Override
    public void onListItemClick(OpeItem item){
        // 操作（右）ボタンが押された
        System.out.println("CLICK!:"+item.in_sno);

        if(mGs.equals("0")) { // 現場差異がなしの時
            // コマンド[22]送信
            DataStructureUtil ds = new DataStructureUtil();
            String mData = ds.makeSendData("22", "{\"手順書番号\":\"" + String.valueOf(item.in_sno) + "\"}");

            returnSno = item.in_sno;
            returnGs = item.bo_gs;
            returnTxGs = item.tx_gs;
            sendFragment.send(mData);

        }

    }
    // 手順一覧画面へ戻る
    private void returnProcedureActivity(){
        Intent intent = new Intent(this,OperationActivity.class);

        intent.putExtra("in_sno",returnSno);
        intent.putExtra("ts_b",returnTime);
        intent.putExtra("status","7");
        intent.putExtra("bo_gs",returnGs);
        intent.putExtra("tx_gs",returnTxGs);
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_gs_ope: // 現場差異確認ボタンクリック
                if(!mGs.equals("0")){ // 現場差異の指示があるとき
                    if(noTap) {
                        // 対象の手順を取得
                        //String in_sno = mOpeFragment.getCurrentSno();
                        OpeItem item = mOpeFragment.getCurrentItem();
                        // サーバーへ現場差異確認[23]送信
                        DataStructureUtil dsHelper = new DataStructureUtil();
                        String mData = dsHelper.makeSendData("23","{\"in_sno\":\""+item.in_sno+"\"}");
                        sendFragment.send(mData);
                        noTap=false; // 連続タップ防止のフラグ
                    }
                }
                break;
        }

    }

    @Override
    public String onRequestRecieved(String data) {
        // サーバーからの要求（data）を受信
        //System.out.println("ReqRecieved:"+data);
        String mData = "";
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("64")) { //現場差異指令
            if (bdRecievedData.getString("format").equals("JSON")) {
                mData = dsHelper.makeSendData("50", "");
            }
        } else if (cmd.equals("9C")) {  // 電源OFF画面
            mData="50@$";
        } else if (cmd.equals("99")) {
            mData = "99@$";
        } else if (cmd.equals("91")) {  // 受信エラー処理 onFinishRecieveProgress で処理
            mData = "";
        } else if (cmd.equals("92")) {  // タイムアウト onFinishRecieveProgress で処理
            mData = "";
        }
        return mData;
    }

    @Override
    public void onFinishRecieveProgress(String data) {
        // サーバー発呼のコマンド送受信後の処理
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("64")) { //現場差異指令
            if (bdRecievedData.getString("format").equals("JSON")) {
                mGs = bdRecievedData.getString("Com"); // 指示フラグを退避
                // 現場差異表示
                setGenbaSai();
                // サーバーからの指示を待機
                recieveFragment.listen();
            }
        } else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("99")) { // accept キャンセル
            // ここでは何もせず、応答の"99"受信で処理

        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else {
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        }

    }

    private void setGenbaSai(){
        // ボタン色を変更、メッセージを変更
        mBtnGs = (Button) findViewById(R.id.btn_gs_ope);
        TextView messageBox = (TextView) findViewById(R.id.text_message_ope);
        Resources res = getResources();

        if(mGs.equals("1")) {
            messageBox.setText(R.string.genbasai_text_skip);
            mBtnGs.setBackgroundResource(R.drawable.bg_diff_on);
            mBtnGs.setTextColor(res.getColor(R.color.colorTextBlack));
        }else if (mGs.equals("2")){
            messageBox.setText(R.string.genbasai_text_add);
            mBtnGs.setBackgroundResource(R.drawable.bg_diff_on);
            mBtnGs.setTextColor(res.getColor(R.color.colorTextBlack));
        }else{
            messageBox.setText(""); // キャンセル
            mBtnGs.setBackgroundResource(R.drawable.bg_diff_off);
            mBtnGs.setTextColor(res.getColor(R.color.colorTextLightGray));
        }

    }

}
