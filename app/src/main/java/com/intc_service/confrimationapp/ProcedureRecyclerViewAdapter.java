package com.intc_service.confrimationapp;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intc_service.confrimationapp.ProcedureFragment.OnListFragmentInteractionListener;
import com.intc_service.confrimationapp.Util.DataStructureUtil.ProcItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProcItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ProcedureRecyclerViewAdapter extends RecyclerView.Adapter<ProcedureRecyclerViewAdapter.ViewHolder> {

    private final List<ProcItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ProcedureRecyclerViewAdapter(List<ProcItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0){
            return new ProcedureViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_procedure, parent, false), this);
        }else{
            // コメント行のView
            return new CommentViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_procedure_comment, parent, false), this);
        }

    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if(mValues.get(position).tx_sno.equals("C") || mValues.get(position).tx_sno.equals("")){
            viewType = 1;
        }

        return viewType;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                //Log.d("onBindViewHolder",  "viewType: 1 position: " + position);
                ((ProcedureViewHolder)holder).onBindItemViewHolder(mValues.get(position));
                break;
            case 1:
                //Log.d("onBindViewHolder",  "viewType: 2 position: " + position);
                ((CommentViewHolder)holder).onBindItemViewHolder(mValues.get(position));
                break;
        }
    }

    // 操作ボタンクリック
    private void onButtonClick(View v, int position){

        mListener.onListItemClick(mValues.get(position));

        Resources res = v.getResources();
        int lockColor = res.getColor(R.color.colorYellowButton);
        v.setBackgroundColor(lockColor);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ProcedureViewHolder extends ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mNumberView;
        public final TextView mPlaceView;
        public final TextView mOperationView;
        public final TextView mRemarksView;
        public ProcItem mItem;

        private ProcedureRecyclerViewAdapter mAdapter;

        public ProcedureViewHolder(View view, ProcedureRecyclerViewAdapter adapter) {
            super(view);
            mView = view;
            mNumberView = (TextView) view.findViewById(R.id.proc_number);
            mPlaceView = (TextView) view.findViewById(R.id.proc_place);
            mOperationView = (TextView) view.findViewById(R.id.proc_operation);
            mRemarksView = (TextView) view.findViewById(R.id.proc_remarks);

            mAdapter = adapter;
            mOperationView.setOnClickListener(this);

        }
        public void onClick(View view){

            mAdapter.onButtonClick(mView, getAdapterPosition());
        }
        @Override
        public String toString() {
            return super.toString() + " '" + mOperationView.getText() + "'";
        }

        private int getColorInt(String code){
            int color = Color.rgb(
                    Integer.valueOf( code.substring( 0, 2 ), 16 ),
                    Integer.valueOf( code.substring( 2, 4 ), 16 ),
                    Integer.valueOf( code.substring( 3, 6 ), 16 ) );
            return color;
        }
        public void onBindItemViewHolder(final ProcItem data) {

            this.mItem = data;
            this.mNumberView.setText(data.tx_sno);
            this.mPlaceView.setText(data.tx_s_l);
            this.mOperationView.setText(data.tx_action);
            //this.mRemarksView.setText(data.tx_bname);

            Resources res = this.mView.getResources();
            int bgColor;
            int btnColor;
            if(data.cd_status.equals("1")){
                bgColor = res.getColor(R.color.colorYellowButton);
            }else {
                bgColor = res.getColor(R.color.colorBackgroudDefault);
            }
            if(data.cd_status.equals("7")){
                btnColor = getColorInt(data.tx_clr2);
            }else{
                btnColor = res.getColor(R.color.colorInstructButton);
            }

            this.mView.setBackgroundColor(bgColor);
            this.mOperationView.setBackgroundColor(btnColor);


            this.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        //mListener.onListFragmentInteraction(data);
                    }
                }
            });
        }
    }

    public class CommentViewHolder extends ViewHolder {
        public final TextView mComment;

        public CommentViewHolder(View view, ProcedureRecyclerViewAdapter adapter) {
            super(view);
            mComment = (TextView) itemView.findViewById(R.id.proc_comment);
        }

        public void onBindItemViewHolder(ProcItem data) {
            this.mComment.setText(data.tx_com);
        }
    }

    public void setActivate(int position, String status){
        // 該当の手順の状態を更新する

        mValues.get(position).cd_status = status;

    }
    public ProcItem getItem(int position){
        // 該当の手順を取得する
        return mValues.get(position);

    }
    public ProcItem getPairItem(String sno, String swno){
        System.out.println("getPairItem");
        for(ProcItem item : mValues){
            if(item.in_swno.equals(swno) && !item.in_sno.equals(sno)){
                return item;
            }
        }
        return mValues.get(0);
    }
}
