package com.omak.smartfees;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    // UI references.
    private EditText mNumberView;
    private EditText mPasswordView,mPasswordViewCnf,mEmail,mAddress,mOwner;
    private EditText mName;
    private View mProgressView;
    private View mLoginFormView;
    private UserUpdateTask mAuthTask;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(ProfileActivity.this, Constants.SHARED_GYM_NAME));

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mName = (EditText)findViewById(R.id.name);
        Utils.makeNonEditable(mName);
        mName.setText(Utils.getStringSharedPreference(ProfileActivity.this, Constants.SHARED_GYM_NAME));
        mNumberView = (EditText)findViewById(R.id.mobile);
        Utils.makeNonEditable(mNumberView);
        mPasswordView = (EditText)findViewById(R.id.password_sign);
        mPasswordView.setVisibility(View.GONE);
        mPasswordViewCnf = (EditText)findViewById(R.id.password_conf);
        mPasswordViewCnf.setVisibility(View.GONE);
        mEmail = (EditText)findViewById(R.id.email_sign);
        Utils.makeNonEditable(mEmail);
        mAddress = (EditText)findViewById(R.id.address_sign);
        Utils.makeNonEditable(mAddress);
        mOwner = (EditText)findViewById(R.id.owner);
        Utils.makeNonEditable(mOwner);
        Button button = (Button)findViewById(R.id.register);
        button.setText("Change Password");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePassword changePassword = new ChangePassword(ProfileActivity.this,true);
                changePassword.setCanceledOnTouchOutside(false);
                changePassword.show();
            }
        });

        final Button buttonEdt = (Button)findViewById(R.id.edit_profile);
        buttonEdt.setVisibility(View.VISIBLE);
        buttonEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonEdt.getText().toString().equalsIgnoreCase("Update")) {
                    attemptLogin();
                } else {
                    buttonEdt.setText("Update");
                    Utils.remakeEditable(mName);
                    Utils.remakeEditable(mNumberView);
                    Utils.remakeEditable(mEmail);
                    Utils.remakeEditable(mAddress);
                    Utils.remakeEditable(mOwner);
                }
            }
        });


    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mNumberView.setError(null);
        mPasswordView.setError(null);
        mPasswordViewCnf.setError(null);
        mName.setError(null);

        // Store values at the time of the login attempt.
        String name = mName.getText().toString();
        String number = mNumberView.getText().toString();
        String email = mEmail.getText().toString();
        String address = mAddress.getText().toString();
        String owner = mOwner.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(number)) {
            mNumberView.setError(getString(R.string.error_field_required));
            focusView = mNumberView;
            cancel = true;
        } else if (!Utils.isNumberValid(number)) {
            mNumberView.setError(getString(R.string.error_invalid_email));
            focusView = mNumberView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if(Utils.checkNetwork(ProfileActivity.this)) {
                mAuthTask = new UserUpdateTask(number,name, owner, email, address);
                mAuthTask.execute((Void) null);
            } else {
                Snackbar.make(mNumberView, "No network connection available", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//         Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Utils.setStringSharedPreference(ProfileActivity.this, Constants.SHARED_GYM_ID, "");
            Utils.setStringSharedPreference(ProfileActivity.this, Constants.SHARED_GYM_NAME, "");
            Utils.setStringSharedPreference(ProfileActivity.this, "LastUpdatedTime", "");
            Customer.deleteAllCustomer(ProfileActivity.this);
            Utils.setBooleanSharedPreference(ProfileActivity.this, Constants.SHARED_PREF_IS_LOGGED_IN, false);
            Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        if(item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class UserUpdateTask extends AsyncTask<Void, Void, String> {

        private final String mNumber;
        private final String mName;
        private final String mOwner;
        private final String mEmail;
        private final String mAddress;
        private String param;

        UserUpdateTask(String number,String name,String owner,String email,String address) {
            mNumber = number;
            mName = name;
            mOwner = owner;
            mEmail = email;
            mAddress = address;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ProfileActivity.this);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Updating...");
            dialog.show();
        }

        @Override
        public String doInBackground(Void... params) {
            param = "gymtag=editgym&txtphone=" + mNumber +"&gym_id=" + Utils.getStringSharedPreference(ProfileActivity.this,Constants.SHARED_GYM_ID)
                    +"&txtname=" + mName + "&txtmail=" + mEmail +"&txtperson=" + mOwner +"&txtaddress=" + mAddress;
            param = param.replace(" ","%20");
            try {
                String response = RestClient.httpPost(Url.BASE_URL, param);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    Utils.setStringSharedPreference(ProfileActivity.this, Constants.SHARED_GYM_NAME, mName);
                    return jsonObject.getString("status");
                } else {
                    return jsonObject.getString("status");
                }

            } catch (Exception e) {
                return e.getMessage();
            }


        }

        @Override
        protected void onPostExecute(final String success) {
            mAuthTask = null;
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                getSupportActionBar().setTitle(Utils.getStringSharedPreference(ProfileActivity.this, Constants.SHARED_GYM_NAME));
                Snackbar.make(mNumberView, success, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mNumberView.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

