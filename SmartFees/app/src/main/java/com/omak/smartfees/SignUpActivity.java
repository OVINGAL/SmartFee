package com.omak.smartfees;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mNumberView;
    private EditText mPasswordView,mPasswordViewCnf,mEmail,mAddress,mOwner;
    private EditText mName;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mName = (EditText)findViewById(R.id.name);
        mNumberView = (EditText)findViewById(R.id.mobile);
        mPasswordView = (EditText)findViewById(R.id.password_sign);
        mPasswordViewCnf = (EditText)findViewById(R.id.password_conf);
        mEmail = (EditText)findViewById(R.id.email_sign);
        mAddress = (EditText)findViewById(R.id.address_sign);
        mOwner = (EditText)findViewById(R.id.owner);

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
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
        String password = mPasswordView.getText().toString();
        String passCnf = mPasswordViewCnf.getText().toString();
        String email = mEmail.getText().toString();
        String address = mAddress.getText().toString();
        String owner = mOwner.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!password.equalsIgnoreCase(passCnf) ) {
            mPasswordViewCnf.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordViewCnf;
            cancel = true;
        }

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
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
            mNumberView.setError(getString(R.string.error_field_required));
            focusView = mNumberView;
            cancel = true;
        } else if (!isNumberValid(number)) {
            mNumberView.setError(getString(R.string.error_invalid_email));
            focusView = mNumberView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(number,password,name,owner,email,address);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isNumberValid(String number) {
        return number.length() > 6;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(2000).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mNumber;
        private final String mPassword;
        private final String mName;
        private final String mOwner;
        private final String mEmail;
        private final String mAddress;
        private String param;

        UserLoginTask(String number, String password,String name,String owner,String email,String address) {
            mNumber = number;
            mPassword = password;
            mName = name;
            mOwner = owner;
            mEmail = email;
            mAddress = address;
        }

        @Override
        public String doInBackground(Void... params) {
            param = "gymtag=signup&txtphone=" + mNumber +"&txtpass=" + mPassword
                    +"&txtname=" + mName + "&txtmail=" + mEmail +"&txtperson=" + mOwner +"&txtaddress=" + mAddress;
            try {
                String response = RestClient.httpPost(Url.BASE_URL, param);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    Utils.setStringSharedPreference(SignUpActivity.this, Constants.SHARED_GYM_ID, jsonObject.getString("gym_id"));
                    Utils.setStringSharedPreference(SignUpActivity.this, Constants.SHARED_GYM_NAME,jsonObject.getString("gym_name"));
                    Utils.setBooleanSharedPreference(SignUpActivity.this,Constants.SHARED_PREF_IS_LOGGED_IN,true);
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
            showProgress(false);

            if (success.equalsIgnoreCase("success")) {
                Intent intent = new Intent(SignUpActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Snackbar.make(mNumberView, success, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mNumberView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}