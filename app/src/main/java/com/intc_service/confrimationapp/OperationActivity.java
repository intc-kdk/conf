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
    private Bundle mBundlePair = new Bundle();

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private ProcedureFragment mProcFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        Intent intent = getIntent();
        mBundleCur = intent.getBundleExtra("current");
        mBundlePair = intent.getBundleExtra("piar");

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
        System.out.println(data);

        // TODO: [P] ログを取得
        Intent intent = new Intent(this,OperationActivity.class);
        intent.putExtra("in_sno","1");
        intent.putExtra("status","7");
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public void onFinishTransmission(String data){

    }
    @Override
    public void onListItemClick(OpeItem item){
        // 操作（右）ボタンが押された
        System.out.println("CLICK!:"+item.in_sno);


        // TODO: コマンド[22]送信
        DataStructureUtil ds = new DataStructureUtil();
        String mData = ds.makeSendData("22","{\"手順書番号\":\""+item.in_sno+"\"}");
        sendFragment.send(mData);
        //  TODO: 手順一覧へ渡すデータ作成

        //  TODO: 手順一覧へ戻る


    }
}
