package com.intc_service.confrimationapp.Util;

import java.util.Comparator;

import com.intc_service.confrimationapp.Util.DataStructureUtil.ProcItem;

/**
 * Created by takashi on 2016/10/15.
 */

public class ProcedureComparator implements Comparator<ProcItem> {
    @Override
    public int compare(ProcItem o1, ProcItem o2) {
        return o1.in_sno < o2.in_sno ? -1 : 1 ;
    }
}
