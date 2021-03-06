package com.intc_service.confrimationapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intc_service.confrimationapp.Util.OperationComparator;
import com.intc_service.confrimationapp.Util.OperationDataUtil;
import com.intc_service.confrimationapp.Util.OperationDataUtil.OpeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class OperationFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private OperationRecyclerViewAdapter mRecyclerViewAdapter;

    private List<OpeItem> ITEMS = new ArrayList<>();

    private final static int MAX_ROW = 6;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OperationFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OperationFragment newInstance(int columnCount) {
        OperationFragment fragment = new OperationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }
    private void makeOperatonList() {
        // 手順データを取得
        Intent intent = getActivity().getIntent();
        Bundle bdCur = intent.getBundleExtra("current");
        ArrayList<Bundle> items = bdCur.getParcelableArrayList("operation");
        for( Bundle i : items){
            ITEMS.add(OperationDataUtil.toList(i));
        }
        //  in_sno でソート
        Collections.sort(ITEMS, new OperationComparator());

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operation_list, container, false);
        makeOperatonList();
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new OperationRecyclerViewAdapter(ITEMS, mListener));
            // Adapterへの参照
            mRecyclerViewAdapter = (OperationRecyclerViewAdapter)recyclerView.getAdapter();

            // 表示位置の決定
            int pos = getCurrentPos();

            if(pos >= MAX_ROW){
                // スクロール (前2手順を表示）
                ((LinearLayoutManager)recyclerView.getLayoutManager()).scrollToPositionWithOffset(pos,0);

            }

        }
        return view;
    }

    private int getCurrentPos(){
        Iterator<OpeItem> i = ITEMS.iterator();
        int cnt=0;
        // ステータスが"1" の操作を取得
        while(i.hasNext()){
            OpeItem item = i.next();
            if(item.cd_status.equals("1")){
                return cnt;
            }
            cnt++;
        }
        return cnt;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(OpeItem item);
        void onListItemClick(OpeItem item);
    }

    public OpeItem getCurrentItem(){
        return mRecyclerViewAdapter.getCurrentItem();
    }

    public void updateGs(String txGs){
        mRecyclerViewAdapter.updateGs(txGs);
    }
}
