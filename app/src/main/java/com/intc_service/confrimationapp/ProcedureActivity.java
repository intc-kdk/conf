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
import com.intc_service.confrimationapp.Util.DataStructureUtil.ProcItem;
import com.intc_service.confrimationapp.Util.alertDialogUtil;
/*
 *  K-02 手順書画面
*/

public class ProcedureActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        ProcedureFragment.OnListFragmentInteractionListener, View.OnClickListener{

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final int REQUEST_CODE_OPERATION = 1;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private ProcedureFragment mProcFragment;

    private String mGs = "0";
    private Button mBtnGs;
    private Button mBtnUpdate;
    private boolean noTap;
    private boolean endProcedure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure);

        noTap=true;
        endProcedure=false;

        //  手順書フラグメントの取得
        mProcFragment = (ProcedureFragment)getSupportFragmentManager()
                .findFragmentById(R.id.ProcedureList);

        // 最初の手順をセット
        mProcFragment.setFirstProcedure();

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
        mBtnGs = (Button) findViewById(R.id.btn_gs);
        mBtnGs.setOnClickListener(this);

        // 画面更新ボタン
        mBtnUpdate = (Button) findViewById(R.id.btn_update);
        mBtnUpdate.setOnClickListener(this);
        mBtnUpdate.setVisibility(View.INVISIBLE);
        // 現場差異の指示を確認
        String gsmode = checkGsmode();
        if(gsmode.equals("5")) {  // スキップ
            mGs="1"; // 現場差異フラグに 1：スキップを設定
        }else if(gsmode.equals("6")){  // 追加
            // 追加確認済みか判定
            ProcItem item = mProcFragment.getCurrentItem();
            if(!item.tx_gs.equals("追加")){
                // tx_gsが追加ではない場合、現場差異フラグに 2：追加を設定
                mGs = "2";
            }
        }
        if(checkPctl()){
            // 盤操作画面からの開始のとき
            startUpOperation();
            mGs="0";  // 現場差異モードは初期化
        }else{
            // サーバーからの指示を待機
            recieveFragment.listen();
        }

        // 現場差異表示
        setGenbaSai();


    }

    private boolean checkPctl(){
        // 手順データをIntentから取得
        Intent intent = getIntent();
        String resultSt = intent.getStringExtra("proc");

        // 手順データを解析し、tejunを取り出す
        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(resultSt);
        Bundle tmpBundle = dsHelper.getRecievedData().getBundle("t_sno");

        if(tmpBundle.getString("cd_pctl").equals("1")){
            return true;
        }else{
            return false;
        }

    }
    private String checkGsmode(){
        // 手順データをIntentから取得
        Intent intent = getIntent();
        String resultSt = intent.getStringExtra("proc");

        // 手順データを解析し、tejunを取り出す
        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(resultSt);
        Bundle tmpBundle = dsHelper.getRecievedData().getBundle("t_sno");
        // 現場差異の状態（cd_gsmode)を返す
        return tmpBundle.getString("cd_gsmode");
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick (View v){
        int id = v.getId();

        switch (id){
            case R.id.btn_gs: // 現場差異ボタンクリック
                if(!mGs.equals("0")){
                    if(noTap) {
                        // 現在の手順を取得
                        String in_sno = mProcFragment.getCurrentSno();
                        // サーバーへ現場差異確認[23]送信
                        DataStructureUtil dsHelper = new DataStructureUtil();
                        String mData = dsHelper.makeSendData("23", "{\"in_sno\":\"" + in_sno + "\"}");
                        sendFragment.send(mData);
                        noTap=false; // 連続タップ防止のフラグ
                    }
                }
                break;
            case R.id.btn_update: // 画面更新ボタンクリック
                if(noTap) {
                    // サーバーへ画面更新[90]送信
                    DataStructureUtil dsHelper = new DataStructureUtil();
                    String mData = dsHelper.makeSendData("90", "");
                    sendFragment.send(mData);
                    noTap=false; // 連続タップ防止のフラグ
                }
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(Bundle rcBundle) {
        // Fragmentからの通知で、ヘッダの表示を更新する
        if(rcBundle.getString("cd_status").equals("1")) {   // 状態が実行通の時
            TextView tvNo = (TextView) findViewById(R.id.title_proc_no);
            TextView tvPlace = (TextView) findViewById(R.id.title_proc_place);
            TextView tvAction = (TextView) findViewById(R.id.title_proc_action);
            TextView tvRemarks = (TextView) findViewById(R.id.title_proc_remarks);

            tvNo.setText(rcBundle.getString("tx_sno"));
            tvPlace.setText(rcBundle.getString("tx_s_l"));
            tvAction.setText(rcBundle.getString("tx_action"));
            tvRemarks.setText(rcBundle.getString("tx_biko"));
        }
    }
    @Override
    public void onListItemClick(ProcItem item){
        // 操作ボタンのイベント
        //System.out.println("CLICK!:"+item.tx_sno);
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("6N")) { //現場差異応答
            if (bdRecievedData.getString("format").equals("TEXT")) {
                int position = mProcFragment.getCurrentPos();
                String tx_gs = "";
                String status = "";
                // cd_status スキップは"7", 追加は "0"
                if (mGs.equals("1")) {  // SKIP
                    status = "7";
                    tx_gs = "スキップ";
                } else if (mGs.equals("2")) {  //追加
                    status = "1";
                    tx_gs = "追加";
                } else {  //  キャンセル  # ここは通らない

                }
                int in_sno = Integer.parseInt(mProcFragment.getCurrentSno()); // ステータス更新前に、in_snoを取得しておく

                mProcFragment.setProcStatus(position, status, "", "True", tx_gs);   // 対象のエントリの更新

                if (mGs.equals("1")) {  // SKIP
                    // 最終の手順か判定
                    if (mProcFragment.getLastInSno() > in_sno) {
                        // 継続の時は次のエントリへ進める
                        mProcFragment.updateProcedure();
                    } else {
                        // 最終手順のスキップであることを記録
                        endProcedure = true;
                        // 最終手順の時、listenを停止して終了画面表示へ
                        sendFragment.halt("99@$");
                    }
                } else {
                    mProcFragment.addProcedure();   // 追加はそのままの手順で待機
                }
                // メッセージ消す
                mGs = "0";
                setGenbaSai();
                noTap = true; //連続タップ抑止解除
            }
        }else if (cmd.equals("6R")) { //現場差異応答(拒否）
            noTap = true; //連続タップ抑止解除
        }else if (cmd.equals("9N")) {  // 画面更新（正常）
            noTap = true; //連続タップ抑止解除
        }else if (cmd.equals("9Q")) {  // 画面更新（異常）
            noTap = true; //連続タップ抑止解除
        }else if (cmd.equals("9C")) {  // 電源OFF画面
            Intent intent = new Intent(this, EndOffActivity.class);
            startActivity(intent);
        } else if (cmd.equals("99")) {  // サーバークローズ
            recieveFragment.closeServer(); //待ち受けを中止する。
            if(endProcedure) {
                // 最終手順のスキップだった時、終了画面表示
                Intent intent = new Intent(this, EndActivity.class);
                startActivity(intent);
            }
        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // 送信完了

    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
         //System.out.println("ReqRecieved:"+data);
        String mData = "";
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("63")) { //指示命令
            if (bdRecievedData.getString("format").equals("TEXT")) {
                mData = dsHelper.makeSendData("50", "");
            }
        } else if (cmd.equals("64")) { //現場差異指令
            if (bdRecievedData.getString("format").equals("JSON")) {
                mData = dsHelper.makeSendData("50", "");
            }
        } else if (cmd.equals("9C")) {  // 電源OFF画面 onFinishRecieveProgress で処理
            mData = "50@$";
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // 盤操作画面からの戻り
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;
        Bundle resultBundle = data.getExtras();

        if(!resultBundle.containsKey("in_sno")) return;
        String status = resultBundle.getString("status");
        int in_sno = resultBundle.getInt("in_sno");
        String ts_b = resultBundle.getString("ts_b");
        String bo_gs = resultBundle.getString("bo_gs");
        String tx_gs = resultBundle.getString("tx_gs");

        if(requestCode == REQUEST_CODE_OPERATION) {
            // 該当操作のステータスを更新
            int position = mProcFragment.getCurrentPos();
            mProcFragment.setProcStatus(position, status, ts_b, bo_gs, tx_gs);   // 対象のエントリの更新

            if(mProcFragment.getLastInSno() > in_sno) {
                mProcFragment.updateProcedure();                 // 次のエントリへ進める

                // サーバーからの指示を待機
                recieveFragment.listen();
            }else{

                // 最終手順の時、終了画面表示
                Intent intent = new Intent(this,EndActivity.class);
                startActivity(intent);

            }

        }
    }
    private void startUpOperation(){
        // 待ち受けを停止する
        recieveFragment.closeServer();

        //Intent生成
        Intent intent = new Intent(this, OperationActivity.class);

        // 対象の盤情報を取得し、intentへ設定
        intent.putExtra("current",mProcFragment.getCurrentBoard());
        intent.putExtra("gsmode",mGs);
        //盤操作画面を起動
        startActivityForResult(intent, REQUEST_CODE_OPERATION);
    }

    @Override
    public void onFinishRecieveProgress(String data) {
        // サーバー発呼のコマンド送受信後の処理
        DataStructureUtil dsHelper = new DataStructureUtil();
        mBtnUpdate.setVisibility(View.INVISIBLE);
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す
        if (cmd.equals("63")) { //指示命令
            if (bdRecievedData.getString("format").equals("TEXT")) {
                //盤操作画面を起動
                startUpOperation();

            }
        } else if (cmd.equals("64")) { //現場差異指令
            if (bdRecievedData.getString("format").equals("JSON")) {
                mGs = bdRecievedData.getString("Com");
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
            System.out.println("※※※※　受信エラー ※※※");
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            mBtnUpdate.setVisibility(View.VISIBLE);
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※");
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            mBtnUpdate.setVisibility(View.VISIBLE);
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else {
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        }
    }
    private void setGenbaSai(){
        // ボタン色を変更、メッセージを変更
        mBtnGs = (Button) findViewById(R.id.btn_gs);
        TextView messageBox = (TextView) findViewById(R.id.text_message);
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
