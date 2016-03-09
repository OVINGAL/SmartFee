package com.omak.smartfees;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

/**
 * Created by afsal on 13/2/16.
 */
public class ChangePassword extends Dialog {

    EditText oldPass,newPass,newPassCnf,mEmail;
    TextView head;
    boolean isChangePassword = true;
    String url = "";
    public ChangePassword(Context context,boolean type) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.change_password);
        mEmail = (EditText)findViewById(R.id.current_email);
        oldPass = (EditText)findViewById(R.id.current_pass);
        newPass = (EditText)findViewById(R.id.password_new);
        newPassCnf = (EditText)findViewById(R.id.password_conf_new);
        head = (TextView)findViewById(R.id.header);
        isChangePassword = type;
        if(isChangePassword){
            mEmail.setVisibility(View.GONE);
        } else {
            oldPass.setVisibility(View.GONE);
            newPassCnf.setVisibility(View.GONE);
            newPass.setVisibility(View.GONE);
            head.setText("Forgot Password");
        }

        findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             if(isChangePassword){
                 if(oldPass.getText().length() < 5) {
                     oldPass.setError(getContext().getString(R.string.error_field_required));
                 } else if(newPass.getText().length() < 5 ) {
                     newPass.setError(getContext().getString(R.string.error_invalid_password));
                 } else if(newPass.getText().toString().equalsIgnoreCase(newPassCnf.getText().toString())) {
                     if(Utils.checkNetwork(getContext())) {
                         changePassTask task = new changePassTask();
                         task.execute(newPass.getText().toString(), oldPass.getText().toString());
                     } else {
                         Toast.makeText(getContext(),"No network connection available",Toast.LENGTH_LONG).show();
                     }
                 } else {
                     newPassCnf.setError(getContext().getString(R.string.error_invalid_password));
                 }
             } else {
                    if(mEmail.getText().toString().isEmpty()) {
                        mEmail.setError(getContext().getString(R.string.error_field_required));
                    } else {
                        if(Utils.checkNetwork(getContext())) {
                            forgorPassTask forgorPassTask = new forgorPassTask();
                            forgorPassTask.execute(mEmail.getText().toString());
                        } else {
                            Toast.makeText(getContext(),"No network connection available",Toast.LENGTH_LONG).show();
                        }
                    }
             }
            }
        });
    }

    public class changePassTask extends AsyncTask<String, Void, String> {

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


            if(Utils.getStringSharedPreference(getContext(),Constants.SHARED_STAFF_TYPE).equalsIgnoreCase("staff")) {
                param = "gymtag=changepassstaff&txtpass=" + params[0] +"&gym_id=" + Utils.getStringSharedPreference(getContext(), Constants.SHARED_GYM_ID)
                +"&staff_id=" + Utils.getStringSharedPreference(getContext(), Constants.SHARED_STAFF_ID)
                +"&txtcurrpass=" + params[1];
                param = param.replace(" ","%20");
                url = Url.STAFF_URL;
            } else {
                param = "gymtag=changepassgym&txtpass=" + params[0] +"&gym_id=" + Utils.getStringSharedPreference(getContext(), Constants.SHARED_GYM_ID)
                +"&txtcurrpass=" + params[1];
                param = param.replace(" ","%20");
                url = Url.BASE_URL;
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

    public class forgorPassTask extends AsyncTask<String, Void, String> {

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

            url = "http://gymapp.oddsoftsolutions.com/gym_forgot.php?gymtag=forgotpass&txtmail=" + params[0];
            try {
                String response = RestClient.httpGet(url);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    return jsonObject.getString("status_msg");
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
