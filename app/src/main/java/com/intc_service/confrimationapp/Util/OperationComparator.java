package com.intc_service.confrimationapp.Util;

import com.intc_service.confrimationapp.Util.OperationDataUtil.OpeItem;

import java.util.Comparator;

/**
 * Created by takashi on 2016/10/15.
 */

public class OperationComparator implements Comparator<OpeItem> {
    @Override
    public int compare(OpeItem o1, OpeItem o2) {
        return o1.in_sno < o2.in_sno ? -1 : 1 ;
    }
}
