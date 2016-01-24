package com.omak.smartfees;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

public class AddStaffActivity extends AppCompatActivity implements View.OnClickListener{

    private AddStaffAsync mAuthTask = null;

    // UI references.
    private EditText mPasswordView,mPasswordViewCnf,mPhone;
    private EditText mName;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(AddStaffActivity.this, Constants.SHARED_GYM_NAME));

        mName = (EditText)findViewById(R.id.name_staff);
        mPhone = (EditText)findViewById(R.id.mobile_staff);
        mPasswordView = (EditText)findViewById(R.id.password_staff);
        mPasswordViewCnf = (EditText)findViewById(R.id.password_conf_staff);

        findViewById(R.id.register_staff).setOnClickListener(this);
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
            case R.id.register_staff:
                addStaff();
                break;
        }
    }

    private void addStaff(){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhone.setError(null);
        mPasswordView.setError(null);
        mPasswordViewCnf.setError(null);
        mName.setError(null);

        // Store values at the time of the login attempt.
        String name = mName.getText().toString();
        String number = mPhone.getText().toString();
        String password = mPasswordView.getText().toString();
        String passCnf = mPasswordViewCnf.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!password.equalsIgnoreCase(passCnf) ) {
            mPasswordViewCnf.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordViewCnf;
            cancel = true;
        }

        if (TextUtils.isEmpty(password) || !Utils.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(number)) {
            mPhone.setError(getString(R.string.error_field_required));
            focusView = mPhone;
            cancel = true;
        } else if (!Utils.isNumberValid(number)) {
            mPhone.setError(getString(R.string.error_invalid_email));
            focusView = mPhone;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new AddStaffAsync();
            mAuthTask.execute(number,password,name);
        }
    }

    private class AddStaffAsync extends AsyncTask<String,Void,String>{

        String param;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AddStaffActivity.this);
            dialog.setMessage("Adding...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            param = "gymtag=addstaff&txtphone=" + strings[0] +"&txtpass=" + strings[1]
                    +"&txtname=" + strings[2]+"&gym_id=" + Utils.getStringSharedPreference(AddStaffActivity.this,Constants.SHARED_GYM_ID);
            param = param.replace(" " ,"%20");
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
            if (s.equalsIgnoreCase("success")) {
                finish();
            } else {
                Snackbar.make(mName, s, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mName.requestFocus();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }

        if(mAuthTask != null){
            mAuthTask.cancel(true);
        }

    }
}
