package com.omak.smartfees.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Staff;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;
import com.omak.smartfees.R;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by afsal on 8/6/15.
 */
public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
    Context context;
    private ArrayList<Staff> staffArrayList;
    private int lastPosition = -1;

    // Provide a suitable constructor (depends on the kind of dataset)
    public StaffAdapter(ArrayList<Staff> staffs, Context mContext) {
        this.staffArrayList = staffs;
        this.context = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staff_cell, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View view, final int pos) {
                switch (view.getId()){
                    case R.id.update:
                        break;
                    case R.id.delete:
                        String title = "Delete " + staffArrayList.get(pos).name + " ?";
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setMessage(title);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(Utils.checkNetwork(context)){
                                    DeleteStaffAsync deleteStaffAsync = new DeleteStaffAsync(context,staffArrayList.get(pos).staffId);
                                    deleteStaffAsync.execute("");
                                    staffArrayList.remove(pos);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(context,"No network connection available",Toast.LENGTH_SHORT).show();
                                }

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
                    case R.id.block:
                        final AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context);
                        String title2 = "Activate " + staffArrayList.get(pos).name + " ?";
                        if(staffArrayList.get(pos).isActive) {
                            title2 = "Block " + staffArrayList.get(pos).name + " ?";
                        }
                        alertDialogBuilder2.setMessage(title2);
                        alertDialogBuilder2.setCancelable(false);
                        alertDialogBuilder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(Utils.checkNetwork(context)){
                                    BlockStaffAsync blockStaffAsync = new BlockStaffAsync(context,staffArrayList.get(pos).staffId,staffArrayList.get(pos).isActive);
                                    blockStaffAsync.execute("");
                                    staffArrayList.get(pos).isActive = !staffArrayList.get(pos).isActive;
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(context,"No network connection available",Toast.LENGTH_SHORT).show();
                                }
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
        holder.mName.setText(staffArrayList.get(position).name);
        holder.mPhone.setText(staffArrayList.get(position).number);
        if(Utils.getBooleanSharedPreference(context, Constants.SHARED_PREF_IS_OWNER)) {
            holder.layout.setVisibility(View.VISIBLE);
        } else {
            holder.layout.setVisibility(View.GONE);
        }
        if(staffArrayList.get(position).isActive){
            holder.mBlock.setText("Block");
        }else {
            holder.mBlock.setText("Activate");
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return staffArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public EditText mName,mPhone;
        public Button mUpdate,mDelete,mBlock;
        public View layout;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            mListener = listener;
            mName = (EditText) v.findViewById(R.id.name_staff_list);
            mPhone = (EditText) v.findViewById(R.id.mobile_staff_list);
            mUpdate = (Button) v.findViewById(R.id.update);
            mDelete = (Button) v.findViewById(R.id.delete);
            mBlock = (Button) v.findViewById(R.id.block);
            layout = v.findViewById(R.id.options);

            mUpdate.setOnClickListener(this);
            mDelete.setOnClickListener(this);
            mBlock.setOnClickListener(this);

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
            param = "gymtag=deletestaff&staff_id=" + id +"&gym_id=" + Utils.getStringSharedPreference(context,Constants.SHARED_GYM_ID);
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
            String tag = "activatestaff";
            if(toblock){
                tag = "blockstaff";
            }
            param = "gymtag="+tag+"&staff_id=" + id +"&gym_id=" + Utils.getStringSharedPreference(context,Constants.SHARED_GYM_ID);
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
