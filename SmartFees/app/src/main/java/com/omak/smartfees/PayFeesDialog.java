package com.omak.smartfees;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

/**
 * Created by afsal on 13/2/16.
 */
public class PayFeesDialog extends Dialog {

    EditText fees,remark;
    String url = "";
    Customer member;
    boolean isEdit;
    String month;
    public PayFeesDialog(final Context context,boolean isEdit,Customer customer,String mon) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pay_fee);
        this.isEdit = isEdit;
        this.member = customer;
        this.month = mon;
        fees = (EditText)findViewById(R.id.fee_amount);
        remark = (EditText)findViewById(R.id.fee_remark);

        if(isEdit) {
            fees.setText(customer.fees.amount);
            remark.setText(customer.fees.remark);
            ((Button)findViewById(R.id.pay_last)).setText("Edit Fees");
        }

        findViewById(R.id.pay_last).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fees.length() > 0) {
                    FeespayTask feespayTask = new FeespayTask();
                    feespayTask.execute(member.memberId,fees.getText().toString(),month,remark.getText().toString());
                } else {
                    Toast.makeText(context,"Please add fees",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public class FeespayTask extends AsyncTask<String, Void, String> {

        String param;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getContext());
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Updating...");
            dialog.show();
        }

        @Override
        public String doInBackground(String... params) {
            String gymTag = "addfees";
            if(isEdit) {
                gymTag = "editfees";
            }

            if(Utils.getStringSharedPreference(getContext(),Constants.SHARED_STAFF_TYPE).equalsIgnoreCase("staff")) {
                param = "gymtag="+gymTag + "&gym_id=" + Utils.getStringSharedPreference(getContext(), Constants.SHARED_GYM_ID)
                        +"&staff_id=" + Utils.getStringSharedPreference(getContext(), Constants.SHARED_STAFF_ID)
                        +"&staff_type=staff"
                        +"&mem_id=" + params[0]
                        +"&txtamount=" + params[1]
                        +"&txtmonth=" + params[2]
                        +"&txtremark=" + params[3];
                param = param.replace(" ","%20");
                url = Url.FEES_URL;
            } else {
                param = "gymtag="+gymTag + "&gym_id=" + Utils.getStringSharedPreference(getContext(), Constants.SHARED_GYM_ID)
                        +"&mem_id=" + params[0]
                        +"&txtamount=" + params[1]
                        +"&txtmonth=" + params[2]
                        +"&txtremark=" + params[3];
                param = param.replace(" ","%20");
                url = Url.FEES_URL;
            }

            param = param.replace(" ","%20");
            try {
                String response = RestClient.httpPost(url,param);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    return jsonObject.getString("status");
                } else {
                    return jsonObject.getString("status_msg");
                }

            } catch (Exception e) {
                return e.getMessage();
            }


        }

        @Override
        protected void onPostExecute(final String success) {
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            dismiss();
            Toast.makeText(getContext(),success,Toast.LENGTH_SHORT).show();
        }

    }



}
