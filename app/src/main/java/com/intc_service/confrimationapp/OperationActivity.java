package com.intc_service.confrimationapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.intc_service.confrimationapp.Util.DataStructureUtil;
import com.intc_service.confrimationapp.Util.OperationDataUtil;
import com.intc_service.confrimationapp.Util.OperationDataUtil.OpeItem;

import java.util.ArrayList;
import java.util.List;

/*
 *  K-03 盤操作画面
*/

public class OperationActivity extends AppCompatActivity
        implements OperationFragment.OnListFragmentInteractionListener, TransmissionFragment.TransmissionFragmentListener{
    private static final String TAG_TRANS = "No_UI_Fragment1";

    private Bundle mBundleCur = new Bundle();

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private int returnSno = 0;
    private String returnGs;
    private String returnTxGs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        Intent intent = getIntent();
        mBundleCur = intent.getBundleExtra("current");

        // ヘッダ部へ設定
        setTextView();

        // TransmissionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行
    }
    private void setTextView() {

        // ヘッダー部へ設定
        TextView tvNo = (TextView)findViewById(R.id.title_proc_no);
        TextView tvPlace = (TextView)findViewById(R.id.title_proc_place);
        TextView tvAction = (TextView)findViewById(R.id.title_proc_action);
        TextView tvRemarks = (TextView)findViewById(R.id.title_proc_remarks);

        tvNo.setText(mBundleCur.getString("tx_sno"));
        tvPlace.setText(mBundleCur.getString("tx_s_l"));
        tvAction.setText(mBundleCur.getString("tx_action"));
        tvRemarks.setText(mBundleCur.getString("tx_biko"));

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
        if(cmd.equals("60")) {

            String date = bdRecievedData.getString("ts_b");
            String[] arrDate = date.split(" ");

            Intent intent = new Intent(this,OperationActivity.class);

            intent.putExtra("in_sno",returnSno);
            intent.putExtra("ts_b",arrDate[1]);
            intent.putExtra("status","7");
            intent.putExtra("bo_gs",returnGs);
            intent.putExtra("tx_gs",returnTxGs);
            setResult(RESULT_OK, intent);
            finish();
        }


        // TODO: [P] ログを取得


    }
    @Override
    public void onFinishTransmission(String data){

    }
    @Override
    public void onListItemClick(OpeItem item){
        // 操作（右）ボタンが押された
        //System.out.println("CLICK!:"+item.in_sno);

        // コマンド[22]送信
        DataStructureUtil ds = new DataStructureUtil();
        String mData = ds.makeSendData("22","{\"手順書番号\":\""+String.valueOf(item.in_sno)+"\"}");

        returnSno = item.in_sno;
        returnGs = item.bo_gs;
        returnTxGs = item.tx_gs;
        sendFragment.send(mData);
        //  TODO: 手順一覧へ渡すデータ作成

        //  TODO: 手順一覧へ戻る


    }
}
