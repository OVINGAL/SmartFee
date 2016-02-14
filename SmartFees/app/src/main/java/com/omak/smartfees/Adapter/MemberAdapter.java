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
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.MemberDetailActivity;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Model.Staff;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;
import com.omak.smartfees.R;
import com.omak.smartfees.RegisterActivity;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by afsal on 8/6/15.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    Context context;
    private ArrayList<Customer> memberArrayList;
    private int lastPosition = -1;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MemberAdapter(ArrayList<Customer> customers, Context mContext) {
        this.memberArrayList = customers;
        this.context = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_cell, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View view, final int pos) {
                switch (view.getId()){
                    case R.id.update_mlist:
                        Intent memberUpdate = new Intent(context, RegisterActivity.class);
                        memberUpdate.putExtra("Member", memberArrayList.get(pos));
                        context.startActivity(memberUpdate);
                        break;
                    case R.id.details:
                        Intent intent = new Intent(context, MemberDetailActivity.class);
                        intent.putExtra("Member",memberArrayList.get(pos));
                        context.startActivity(intent);
                        break;
                    case R.id.delete_mlist:
                        String title = "Delete " + memberArrayList.get(pos).name + " ?";
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setMessage(title);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(Utils.checkNetwork(context) && memberArrayList.get(pos).memberId.length() > 0){
                                    DeleteStaffAsync deleteStaffAsync = new DeleteStaffAsync(context,memberArrayList.get(pos).memberId);
                                    deleteStaffAsync.execute("");
                                    Customer.deleteCustomer(context, memberArrayList.get(pos).regNum);
                                } else {
                                    Customer s = memberArrayList.get(pos);
                                    s.deleted = "Yes";
                                    s.stored = "No";
                                    if(s.memberId.length() > 0){
                                        Customer.deleteCustomer(context, s.regNum);
                                    } else {
                                        Customer.updateDetails(context, s);
                                    }
                                }
                                memberArrayList.remove(pos);
                                notifyDataSetChanged();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog = alertDialogBuilder.create();
                        dialog.show();
                        break;
                    case R.id.block_mlist:
                        final AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context);
                        String title2 = "Activate " + memberArrayList.get(pos).name + " ?";
                        if(memberArrayList.get(pos).blocked.equalsIgnoreCase("NO")) {
                            title2 = "Block " + memberArrayList.get(pos).name + " ?";
                        }
                        alertDialogBuilder2.setMessage(title2);
                        alertDialogBuilder2.setCancelable(false);
                        alertDialogBuilder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(Utils.checkNetwork(context) && memberArrayList.get(pos).memberId.length() > 0){
                                    BlockStaffAsync blockStaffAsync =
                                            new BlockStaffAsync(context,memberArrayList.get(pos).memberId,memberArrayList.get(pos).blocked.equalsIgnoreCase("No"));
                                    blockStaffAsync.execute("");
                                    memberArrayList.get(pos).stored = "Yes";
                                } else {
                                    memberArrayList.get(pos).stored = "No";
                                }
                                memberArrayList.get(pos).blocked = memberArrayList.get(pos).blocked.equalsIgnoreCase("No") ? "Yes" : "No";
                                Customer.updateDetails(context,memberArrayList.get(pos));
                                notifyDataSetChanged();
                            }
                        });
                        alertDialogBuilder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog2 = alertDialogBuilder2.create();
                        dialog2.show();
                        break;
                }
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Utils.makeNonEditable(holder.mName);
        Utils.makeNonEditable(holder.mPhone);
        Utils.makeNonEditable(holder.mAddress);
        holder.mName.setText(memberArrayList.get(position).name);
        holder.mPhone.setText(memberArrayList.get(position).phone);
        holder.mAddress.setText(memberArrayList.get(position).address);

        if(memberArrayList.get(position).blocked.equalsIgnoreCase("NO")){
            holder.mBlock.setText("Block");
        }else {
            holder.mBlock.setText("Activate");
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

        public EditText mName,mPhone,mAddress;
        public Button mUpdate,mDelete,mBlock,mDetails;
        public View layout;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            mListener = listener;
            mName = (EditText) v.findViewById(R.id.name_mlist);
            mPhone = (EditText) v.findViewById(R.id.mobile_mlist);
            mAddress = (EditText) v.findViewById(R.id.address_mlist);
            mUpdate = (Button) v.findViewById(R.id.update_mlist);
            mDelete = (Button) v.findViewById(R.id.delete_mlist);
            mBlock = (Button) v.findViewById(R.id.block_mlist);
            mDetails = (Button) v.findViewById(R.id.details);
            layout = v.findViewById(R.id.outer);

            layout.setOnClickListener(this);
            mUpdate.setOnClickListener(this);
            mDelete.setOnClickListener(this);
            mBlock.setOnClickListener(this);
            mDetails.setOnClickListener(this);
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
                String response = RestClient.httpPost(Url.STAFF_URL, param);
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

    private class BlockStaffAsync extends AsyncTask<String,Void,String>{

        String param;
        ProgressDialog dialog;
        Context ctx;
        String id;
        boolean toblock;

        public BlockStaffAsync(Context context,String staffid,boolean toBlock){
            ctx = context;
            id = staffid;
            toblock=toBlock;
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
            String tag = "activatemember";
            if(toblock){
                tag = "blockmember";
            }
            param = "gymtag="+tag+"&mem_id=" + id +"&gym_id=" + Utils.getStringSharedPreference(context,Constants.SHARED_GYM_ID);
            try {
                String response = RestClient.httpPost(Url.STAFF_URL, param);
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
