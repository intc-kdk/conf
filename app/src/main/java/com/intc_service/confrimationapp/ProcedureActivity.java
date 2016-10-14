package com.intc_service.confrimationapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.intc_service.confrimationapp.Util.DataStructureUtil;
import com.intc_service.confrimationapp.Util.DataStructureUtil.ProcItem;

import java.util.List;
/*
 *  K-02 手順書画面
*/

public class ProcedureActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        ProcedureFragment.OnListFragmentInteractionListener{

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final int REQUEST_CODE_OPERATION = 1;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private ProcedureFragment mProcFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure);

        //  手順書フラグメントの取得
        mProcFragment = (ProcedureFragment)getSupportFragmentManager()
                .findFragmentById(R.id.ProcedureList);

        // 起動時のみ手順のカレント表示はマニュアル設定
        mProcFragment.setProcStatus(0,"1");

        // TransmissionFragment/ReceptionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // サーバーからの指示を待機
        recieveFragment.listen();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    public void onListFragmentInteraction(Bundle rcBundle) {

        if(rcBundle.getString("cd_status").equals("1")) {
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
        // 操作ボタンが押された
        System.out.println("CLICK!:"+item.tx_sno);
        Intent intent = new Intent(this,OperationActivity.class);
        //startActivity(intent);
        //fragment.test();
        setProcActivate();
    }

    private void setProcActivate(){
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        System.out.println(data);

        // TODO: [P] ログを取得

    }

    @Override
    public void onFinishTransmission(String data){

    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
         //System.out.println("ReqRecieved:"+data);
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals("63")) { //指示命令
            if (bdRecievedData.getString("format").equals("TEXT")) {
                // 操作対象を取得
                List<ProcItem> item = mProcFragment.getCurrentProcedure();

                Bundle bdCur = new Bundle();

                bdCur.putString("tx_sno", item.get(0).tx_sno);
                bdCur.putString("tx_s_l", item.get(0).tx_s_l);
                bdCur.putString("tx_action", item.get(0).tx_action);
                bdCur.putString("tx_b_l", item.get(0).tx_b_l);
                bdCur.putString("tx_b_r", item.get(0).tx_b_r);
                bdCur.putString("tx_clr1", item.get(0).tx_clr1);
                bdCur.putString("tx_clr2", item.get(0).tx_clr2);
                bdCur.putString("tx_biko", item.get(0).tx_biko);
                bdCur.putString("cd_status", item.get(0).cd_status);

                Bundle bdPair = new Bundle();

                bdPair.putString("tx_sno", item.get(1).tx_sno);
                bdPair.putString("tx_s_l", item.get(1).tx_s_l);
                bdPair.putString("tx_action", item.get(1).tx_action);
                bdPair.putString("tx_b_l", item.get(1).tx_b_l);
                bdPair.putString("tx_b_r", item.get(1).tx_b_r);
                bdPair.putString("tx_clr1", item.get(1).tx_clr1);
                bdPair.putString("tx_clr2", item.get(1).tx_clr2);
                bdPair.putString("cd_status", item.get(1).cd_status);


                //Intent生成
                Intent intent = new Intent(this, OperationActivity.class);

                intent.putExtra("current", bdCur);
                intent.putExtra("pair", bdPair);

                //盤操作画面を起動
                startActivityForResult(intent, REQUEST_CODE_OPERATION);
            }
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;
        System.out.println("RESULT OK");
        Bundle resultBundle = data.getExtras();

        if(!resultBundle.containsKey("in_sno")) return;
        System.out.println("RESULT OK!!!!!!!!!!!!!!!!!!!!");
        String status = resultBundle.getString("status");
        String in_sno = resultBundle.getString("in_sno");
        System.out.println("in_sno:"+in_sno);
        System.out.println("status:"+status);
        if(requestCode == REQUEST_CODE_OPERATION) {
            // 盤操作画面からの戻り
            // 該当操作のステータスを更新
            int position = mProcFragment.getCurrentPos();

            System.out.println("position:"+position);
            System.out.println("status:"+status);
            mProcFragment.setProcStatus(position, status);   // 対象のエントリの更新

            mProcFragment.updateProcedure(); // 次のエントリへ進める
            // サーバーからの指示を待機
            recieveFragment.listen();


        }
    }

}
