package com.omak.smartfees;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via Mobile/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mNumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mNumberView = (EditText) findViewById(R.id.mobile_number);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        findViewById(R.id.register_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);

            }
        });
        findViewById(R.id.forgot_password).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePassword changePassword = new ChangePassword(LoginActivity.this,false);
                changePassword.setCanceledOnTouchOutside(false);
                changePassword.show();

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

        // Store values at the time of the login attempt.
        String number = mNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
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
            if(Utils.checkNetwork(LoginActivity.this)) {
                showProgress(true);
                mAuthTask = new UserLoginTask(number, password);
                mAuthTask.execute((Void) null);
            }else {
                Snackbar.make(mNumberView, "No network connection available", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
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
        private String param;

        UserLoginTask(String email, String password) {
            mNumber = email;
            mPassword = password;
        }

        @Override
        public String doInBackground(Void... params) {
            param = "gymtag=login&txtphone=" + mNumber +"&txtpass=" + mPassword;
            try {
                String response = RestClient.httpPost(Url.BASE_URL, param);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    Utils.setStringSharedPreference(LoginActivity.this, Constants.SHARED_GYM_ID,jsonObject.getString("gym_id"));
                    Utils.setStringSharedPreference(LoginActivity.this, Constants.SHARED_GYM_NAME, jsonObject.getString("gym_name"));
                    Utils.setBooleanSharedPreference(LoginActivity.this, Constants.SHARED_PREF_IS_LOGGED_IN, true);
                    Utils.setStringSharedPreference(LoginActivity.this, Constants.SHARED_STAFF_ID, jsonObject.getString("staff_id"));
                    Utils.setStringSharedPreference(LoginActivity.this, Constants.SHARED_STAFF_TYPE, jsonObject.getString("staff_type"));
                    Utils.setStringSharedPreference(LoginActivity.this, Constants.SHARED_GYM_STATUS, jsonObject.getString("gym_status"));
                    if(jsonObject.getString("staff_type").equalsIgnoreCase("owner")) {
                        Utils.setBooleanSharedPreference(LoginActivity.this,Constants.SHARED_PREF_IS_OWNER,true);
                    } else {
                        Utils.setBooleanSharedPreference(LoginActivity.this,Constants.SHARED_PREF_IS_OWNER,false);
                    }
                    return jsonObject.getString("status");
                } else {
                    return jsonObject.getString("error_msg");
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
                Intent intent = new Intent(LoginActivity.this,MainHomeActivity.class);
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

