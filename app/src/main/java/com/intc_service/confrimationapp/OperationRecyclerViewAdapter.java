package com.intc_service.confrimationapp;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intc_service.confrimationapp.OperationFragment.OnListFragmentInteractionListener;
import com.intc_service.confrimationapp.Util.OperationDataUtil.OpeItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OpeItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OperationRecyclerViewAdapter extends RecyclerView.Adapter<OperationRecyclerViewAdapter.ViewHolder> {

    private final List<OpeItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public OperationRecyclerViewAdapter(List<OpeItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_operatoin, parent, false);
        return new ViewHolder(view, this);
    }

    private int getColorInt(String code){
        int color = Color.rgb(
                Integer.valueOf( code.substring( 0, 2 ), 16 ),
                Integer.valueOf( code.substring( 2, 4 ), 16 ),
                Integer.valueOf( code.substring( 3, 6 ), 16 ) );
        return color;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNumberView.setText(mValues.get(position).tx_sno);
        holder.mBeforeView.setText(mValues.get(position).tx_b_l);
        holder.mAfterView.setText(mValues.get(position).tx_b_r);

        Resources res = holder.mView.getResources();
        int bgColor;
        if(mValues.get(position).cd_status.equals("1")){
            bgColor = res.getColor(R.color.colorYellowButton);
        }else {
            bgColor = res.getColor(R.color.colorBackgroudDefault);
        }
        int beforeColor =getColorInt(mValues.get(position).tx_clr1);
        int afterColor = getColorInt(mValues.get(position).tx_clr2);

        holder.mView.setBackgroundColor(bgColor);
        holder.mBeforeView.setBackgroundColor(beforeColor);
        holder.mAfterView.setBackgroundColor(afterColor);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }
    // 操作ボタン(右）クリック
    private void onButtonClick(View v, int position){
        // 対象の操作の時のみ、Activityへ通知
        if(mValues.get(position).cd_status.equals("1")) {
            mListener.onListItemClick(mValues.get(position));
        }else if(mValues.get(position).bo_gs.equals("True")){
            mListener.onListItemClick(mValues.get(position));
        }
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mNumberView;
        public final TextView mBeforeView;
        public final TextView mAfterView;
        public OpeItem mItem;

        private OperationRecyclerViewAdapter mAdapter;

        public ViewHolder(View view, OperationRecyclerViewAdapter adapter) {
            super(view);
            mView = view;
            mNumberView = (TextView) view.findViewById(R.id.proc_number);
            mBeforeView = (TextView) view.findViewById(R.id.action_before);
            mAfterView = (TextView) view.findViewById(R.id.action_after);

            mAdapter = adapter;
            mAfterView.setOnClickListener(this);  // 操作ボタン（右）へのリスナー設定
        }
        public void onClick(View view){

            mAdapter.onButtonClick(mView, getAdapterPosition());
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mBeforeView.getText() + "'";
        }
    }
}
