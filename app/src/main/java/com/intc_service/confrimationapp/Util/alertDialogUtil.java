package com.intc_service.confrimationapp.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.intc_service.confrimationapp.R;
import com.intc_service.confrimationapp.TransmissionFragment;

/**
 * Created by takashi on 2016/11/13.
 */

public class alertDialogUtil {

    private alertDialogUtil(){}

    public static void show(Activity activity, final TransmissionFragment sendFragment, String title, String msg){
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(R.drawable.error)
                .setPositiveButton( "OK",
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if(sendFragment!=null){
                                    sendFragment.resend();
                                }
                            }
                        }).show();
    }
}
