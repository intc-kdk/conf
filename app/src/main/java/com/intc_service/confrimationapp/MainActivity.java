package com.intc_service.confrimationapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.intc_service.confrimationapp.Util.SettingPrefUtil;

/*
 *  K-00 起動画面（デバッグ用）
*/

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //[P] 接続情報（送信先サーバー、受信ポートの取得
        //[P] ログの初期設定（継続？or 初期化?

    }

    @Override
    protected void onStart(){
        super.onStart();

        //[P] 起動されたことをログに記録する

    }
    // デバッグ用の タップイベント
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP){
            // 2重起動防止
            Intent intent = new Intent(this,WaitActivity.class);
            startActivity(intent);
        }
        return false;
    }
}
