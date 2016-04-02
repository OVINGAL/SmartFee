package com.omak.smartfees.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Logger;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.MemberDetailActivity;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;
import com.omak.smartfees.PayFeesDialog;
import com.omak.smartfees.R;
import com.omak.smartfees.RegisterActivity;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by afsal on 8/6/15.
 */
public class MemberFeeAdapter extends RecyclerView.Adapter<MemberFeeAdapter.ViewHolder> {
    Context context;
    private ArrayList<Customer> memberArrayList;
    private int lastPosition = -1;
    private boolean isPaid;
    String month;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MemberFeeAdapter(ArrayList<Customer> customers, Context mContext,boolean status,String mon) {
        this.memberArrayList = customers;
        this.context = mContext;
        this.isPaid = status;
        this.month = mon;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_fee_cell, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View view, final int pos) {
                switch (view.getId()){
                    case R.id.pay_fees:
                        PayFeesDialog dialog = new PayFeesDialog(context,isPaid,memberArrayList.get(pos),month);
                        dialog.show();
                        break;
                }
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mName.setText(memberArrayList.get(position).name);
        holder.mPhone.setText(memberArrayList.get(position).phone);
        holder.mAddress.setText(memberArrayList.get(position).address);
        if(memberArrayList.get(position).photo != null && memberArrayList.get(position).photo.length() > 0 ){
            String imageurl = "http://gymapp.oddsoftsolutions.com/uploadedimages/" + memberArrayList.get(position).photo;
            Logger.e(" Glide  " + imageurl);
            Glide.with(context).load(imageurl)
                    .error(R.drawable.ic_launcher)
                    .into(holder.db);
        } else {
            holder.db.setImageResource(R.drawable.ic_launcher);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return memberArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mName,mPhone,mAddress;
        public Button mPayFee;
        public View layout;
        public ImageView db;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            mListener = listener;
            mName = (TextView) v.findViewById(R.id.name_mlist);
            mPhone = (TextView) v.findViewById(R.id.mobile_mlist);
            mAddress = (TextView) v.findViewById(R.id.address_mlist);
            mPayFee = (Button) v.findViewById(R.id.pay_fees);
            db = (ImageView)v.findViewById(R.id.member_dp);
            layout = v.findViewById(R.id.outer);

            layout.setOnClickListener(this);
            mPayFee.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPosition = getAdapterPosition();
            mListener.onItemClick(view, itemPosition);
        }

        public interface IMyViewHolderClicks {
            void onItemClick(View view, int pos);
        }
    }

    public class DeleteStaffAsync extends AsyncTask<String,Void,String> {

        String param;
        ProgressDialog dialog;
        Context ctx;
        String id;

        public DeleteStaffAsync(Context context,String staffid){
            ctx = context;
            id = staffid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ctx);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            param = "gymtag=deletemember&mem_id=" + id +"&gym_id=" + Utils.getStringSharedPreference(context,Constants.SHARED_GYM_ID);
            try {
                String response = RestClient.httpGet(Url.MEMBER_URL + "?" + param);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    return jsonObject.getString("status");
                } else {
                    return jsonObject.getString("status");
                }

            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
        }
    }
}
