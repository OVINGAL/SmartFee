package com.omak.smartfees;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

public class MemberDetailActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mMobile;
    private EditText mAge,mweight,mRegnum,mAddress;
    private EditText mDate;
    private EditText mName;
    private Customer member;
    MemberRegisterTask mAuthTask;
    boolean isupdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(MemberDetailActivity.this, Constants.SHARED_GYM_NAME));

        mName = (EditText)findViewById(R.id.member_name);
        mMobile = (EditText)findViewById(R.id.member_phone);
        mAge = (EditText)findViewById(R.id.member_age);
        mweight = (EditText)findViewById(R.id.member_weight);
        mRegnum = (EditText)findViewById(R.id.member_reg);
        mAddress = (EditText)findViewById(R.id.member_address);
        mDate = (EditText)findViewById(R.id.member_date);
        findViewById(R.id.delete_btn).setOnClickListener(this);
        findViewById(R.id.block_btn).setOnClickListener(this);
        findViewById(R.id.pay_btn).setOnClickListener(this);
        findViewById(R.id.update_btn).setOnClickListener(this);
        if(getIntent().hasExtra("Member")) {
            member = (Customer)getIntent().getSerializableExtra("Member");
            mName.setText(member.name);
            mMobile.setText(member.phone);
            mAge.setText(member.age);
            mweight.setText(member.weight);
            mRegnum.setText(member.regNum);
            mAddress.setText(member.address);
            mDate.setText(member.date);
            if(member.blocked.equalsIgnoreCase("Yes")) {
                ((Button)findViewById(R.id.block_btn)).setText("Activate");
            } else {
                ((Button)findViewById(R.id.block_btn)).setText("Block");
            }
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.update_btn:
                registerMember();
                break;
            case R.id.pay_btn:

                break;
            case R.id.delete_btn:
                String title = "Delete " + member.name + " ?";
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MemberDetailActivity.this);
                alertDialogBuilder.setMessage(title);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(Utils.checkNetwork(MemberDetailActivity.this) && member.memberId.length() > 0){
                            DeleteStaffAsync deleteStaffAsync = new DeleteStaffAsync(MemberDetailActivity.this,member.memberId);
                            deleteStaffAsync.execute("");
                            Customer.deleteCustomer(MemberDetailActivity.this, member.regNum);
                        } else {
                            Customer s = member;
                            s.deleted = "Yes";
                            s.stored = "No";
                            if(s.memberId.length() > 0){
                                Customer.deleteCustomer(MemberDetailActivity.this, s.regNum);
                            } else {
                                Customer.updateDetails(MemberDetailActivity.this, s);
                            }
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
            case R.id.block_btn:
                final AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(MemberDetailActivity.this);
                String title2 = "Activate " + member.name + " ?";
                if(member.blocked.equalsIgnoreCase("NO")) {
                    title2 = "Block " + member.name + " ?";
                }
                alertDialogBuilder2.setMessage(title2);
                alertDialogBuilder2.setCancelable(false);
                alertDialogBuilder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(Utils.checkNetwork(MemberDetailActivity.this) && member.memberId.length() > 0){
                            BlockStaffAsync blockStaffAsync =
                                    new BlockStaffAsync(MemberDetailActivity.this,member.memberId,member.blocked.equalsIgnoreCase("No"));
                            blockStaffAsync.execute("");
                            member.stored = "Yes";
                        } else {
                            member.stored = "No";
                        }
                        member.blocked = member.blocked.equalsIgnoreCase("No") ? "Yes" : "No";
                        Customer.updateDetails(MemberDetailActivity.this, member);
                        if(member.blocked.equalsIgnoreCase("Yes")) {
                            ((Button)findViewById(R.id.block_btn)).setText("Activate");
                        } else {
                            ((Button)findViewById(R.id.block_btn)).setText("Block");
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
            param = "gymtag=deletemember&mem_id=" + id +"&gym_id=" + Utils.getStringSharedPreference(MemberDetailActivity.this,Constants.SHARED_GYM_ID);
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
            finish();
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
            param = "gymtag="+tag+"&mem_id=" + id +"&gym_id=" + Utils.getStringSharedPreference(MemberDetailActivity.this,Constants.SHARED_GYM_ID);
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

    private void registerMember(){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mMobile.setError(null);
        mRegnum.setError(null);
        mDate.setError(null);
        mName.setError(null);
        mweight.setError(null);
        mAddress.setError(null);
        mAge.setError(null);

        // Store values at the time of the login attempt.
        String name = mName.getText().toString();
        String number = mMobile.getText().toString();
        String regNum = mRegnum.getText().toString();
        String date = mDate.getText().toString();
        String weight = mweight.getText().toString();
        String address = mAddress.getText().toString();
        String age = mAge.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.


        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        if (TextUtils.isEmpty(number)) {
            mMobile.setError(getString(R.string.error_field_required));
            focusView = mMobile;
            cancel = true;
        }

        if (TextUtils.isEmpty(regNum)) {
            mRegnum.setError(getString(R.string.error_field_required));
            focusView = mRegnum;
            cancel = true;
        }

        if (TextUtils.isEmpty(date)) {
            mDate.setError(getString(R.string.error_field_required));
            focusView = mDate;
            cancel = true;
        }

        if (TextUtils.isEmpty(weight)) {
            mweight.setError(getString(R.string.error_field_required));
            focusView = mweight;
            cancel = true;
        }

        if (TextUtils.isEmpty(address)) {
            mAddress.setError(getString(R.string.error_field_required));
            focusView = mAddress;
            cancel = true;
        }

        if (TextUtils.isEmpty(age)) {
            mAge.setError(getString(R.string.error_field_required));
            focusView = mAge;
            cancel = true;
        }



        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new MemberRegisterTask();
            mAuthTask.execute(name,number,age,weight,regNum,address,date);
        }
    }

    public class MemberRegisterTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MemberDetailActivity.this);
            dialog.setMessage("Updating...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        public String doInBackground(String... params) {
            Customer model = new Customer();

            model.gymId = Utils.getStringSharedPreference(MemberDetailActivity.this, Constants.SHARED_GYM_ID);
            model.regNum = params[4];
            model.name = params[0];
            model.phone = params[1];
            model.address = params[5];
            model.age = params[2];
            model.date = params[6];
            model.weight = params[3];
            model.blocked = "No";
            model.deleted = "No";
            model.stored = "No";

            if(isupdate) {
                model.memberId = member.memberId;
                model._id = member._id;
            }

            if(Utils.checkNetwork(MemberDetailActivity.this)) {
                String param = "";
                if(isupdate) {
                    param = "gymtag=editmember&txtname=" + model.name + "&txtphone=" + model.phone + "&txtdob=" + model.age
                            + "&txtregno=" + model.regNum + "&txtweight=" + model.weight + "&txtaddress=" + model.address + "&txtjoindate=" + model.date
                            + "&staff_id=" + "" + "&staff_type=" + "" + "&gym_id=" + model.gymId + "&mem_id=" + model.memberId;
                }else {
                    param = "gymtag=addmember&txtname=" + model.name + "&txtphone=" + model.phone + "&txtdob=" + model.age
                            + "&txtregno=" + model.regNum + "&txtweight=" + model.weight + "&txtaddress=" + model.address + "&txtjoindate=" + model.date
                            + "&staff_id=" + "" + "&staff_type=" + "" + "&gym_id=" + model.gymId;
                }
                param = param.replace(" ", "%20");
                try {
                    String response = RestClient.httpPost(Url.MEMBER_URL, param);
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("response");
                    if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                        model.stored = "yes";
                        if(!isupdate) {
                            model.memberId = jsonObject.getString("mem_id");
                        } else {
                        }
                        if(isupdate) {
                            Customer.updateDetails(MemberDetailActivity.this, model);
                        } else {
                            Customer.insertMember(MemberDetailActivity.this, model);
                        }
//                        upload();
                        return jsonObject.getString("status");
                    } else {
                        return jsonObject.getString("status_msg");
                    }

                } catch (Exception e) {
                    return e.getMessage();
                }
            } else {
                if(isupdate) {
                    Customer.updateDetails(MemberDetailActivity.this, model);
                } else {
                    Customer.insertMember(MemberDetailActivity.this, model);
                }
                return "success";
            }
        }

        @Override
        protected void onPostExecute(final String success) {
            mAuthTask = null;
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            Toast.makeText(MemberDetailActivity.this, success, Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
