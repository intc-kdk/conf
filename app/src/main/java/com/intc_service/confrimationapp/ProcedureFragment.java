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

import com.intc_service.confrimationapp.Util.DataStructureUtil;
import com.intc_service.confrimationapp.Util.DataStructureUtil.ProcItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ProcedureFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private int mCurrentPos = 0;
    private OnListFragmentInteractionListener mListener;
    private ProcedureRecyclerViewAdapter mRecyclerViewAdapter;
    private List<ProcItem> mItems;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProcedureFragment() {
    }

    @SuppressWarnings("unused")
    public static ProcedureFragment newInstance(int columnCount) {
        ProcedureFragment fragment = new ProcedureFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 手順データを取得
        Intent intent = getActivity().getIntent();
        String resultSt = intent.getStringExtra("proc");

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(resultSt);
        Bundle tmpBundle = dsHelper.getRecievedData(); // 渡したデータを解析し、tejunを取り出す

        View view = inflater.inflate(R.layout.fragment_procedure_list, container, false);

        // アダプターにセット
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new ProcedureRecyclerViewAdapter(dsHelper.ITEMS, mListener));

            // Adapterへの参照
            mRecyclerViewAdapter = (ProcedureRecyclerViewAdapter)recyclerView.getAdapter();
            mItems = dsHelper.ITEMS;
        }
        //setProcStatus(0,"1");
        return view;
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

        void onListFragmentInteraction(Bundle bundle);
        void onListItemClick(ProcItem item);
    }
    public int getCurrentPos(){
        return mCurrentPos;

    }
    public void setProcStatus(int pos, String status){

        // 対象の指示を取得
        mRecyclerViewAdapter.setActivate(pos,status);
        Bundle rcBundle = new Bundle();
        rcBundle.putString("tx_sno",mItems.get(pos).tx_sno);
        rcBundle.putString("tx_s_l",mItems.get(pos).tx_s_l);
        rcBundle.putString("tx_action",mItems.get(pos).tx_action);
        rcBundle.putString("tx_b_l",mItems.get(pos).tx_b_l);
        rcBundle.putString("tx_action",mItems.get(pos).tx_action);
        rcBundle.putString("tx_biko",mItems.get(pos).tx_biko);
        rcBundle.putString("cd_status",mItems.get(pos).cd_status);


        ((OnListFragmentInteractionListener)getActivity()).onListFragmentInteraction(rcBundle);

    }
    public void updateProcedure(){
        int nextPos = mCurrentPos + 1;

        if(mItems.get(nextPos).tx_sno.equals("C")){
            nextPos++;  // 次がコメントの時は一つ進める
        }
        // 対象の指示を更新
        mItems.get(nextPos).cd_status="1";
        mRecyclerViewAdapter.setActivate(nextPos,mItems.get(nextPos).cd_status);

        Bundle rcBundle = new Bundle();
        rcBundle.putString("tx_sno",mItems.get(nextPos).tx_sno);
        rcBundle.putString("tx_s_l",mItems.get(nextPos).tx_s_l);
        rcBundle.putString("tx_action",mItems.get(nextPos).tx_action);
        rcBundle.putString("tx_b_l",mItems.get(nextPos).tx_b_l);
        rcBundle.putString("tx_action",mItems.get(nextPos).tx_action);
        rcBundle.putString("tx_biko",mItems.get(nextPos).tx_biko);
        rcBundle.putString("cd_status",mItems.get(nextPos).cd_status);

        mCurrentPos=nextPos;
        mRecyclerViewAdapter.notifyDataSetChanged();
        ((OnListFragmentInteractionListener)getActivity()).onListFragmentInteraction(rcBundle);

    }
    public List<ProcItem> getCurrentProcedure()
    {
        List<ProcItem> arrProc = new ArrayList<>();;

        ProcItem data = mRecyclerViewAdapter.getItem(mCurrentPos);

        arrProc.add(data);
        arrProc.add(mRecyclerViewAdapter.getPairItem(data.in_sno, data.in_swno));

        return arrProc;
    }

    /*
    RecycerView recyclerView = (RecyclerView)findViewById(R.id.your_recyclerview);
YourViewHolder viewHolder = (YourViewHolder)recyclerView.findViewHolderForAdapterPosition(position);
    * */
}
