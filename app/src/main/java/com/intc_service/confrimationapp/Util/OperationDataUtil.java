package com.intc_service.confrimationapp.Util;

import android.os.Bundle;


/**
 * Created by takashi on 2016/10/13.
 */

public class OperationDataUtil {

    public static OpeItem toList(Bundle entry) {
        return new OpeItem(entry.getInt("in_sno"), entry.getString("tx_sno"),entry.getString("tx_b_l"), entry.getString("tx_b_r"), entry.getString("tx_clr1"), entry.getString("tx_clr2"),entry.getString("cd_status"),entry.getString("bo_gs"),entry.getString("tx_gs"));
    }
    /**
     * 手順書データのクラス
     */
    public static class OpeItem {
        public final int in_sno;
        public final String tx_sno;
        public final String tx_b_l;
        public final String tx_b_r;
        public final String tx_clr1;
        public final String tx_clr2;
        public String cd_status;
        public String bo_gs;
        public String tx_gs;

        public OpeItem(int in_sno ,String tx_sno ,String tx_b_l ,String tx_b_r ,String tx_clr1 ,String tx_clr2 ,String cd_status, String bo_gs, String tx_gs){
            this.in_sno = in_sno;
            this.tx_sno = tx_sno;
            this.tx_b_l = tx_b_l;
            this.tx_b_r = tx_b_r;
            this.tx_clr1 = tx_clr1;
            this.tx_clr2 = tx_clr2;
            this.cd_status = cd_status;
            this.bo_gs = bo_gs;
            this.tx_gs = tx_gs;
        }
    }
}
