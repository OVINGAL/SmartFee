package com.omak.smartfees;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;

public class ProfileActivity extends AppCompatActivity {

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
        mName.setEnabled(false);
        mName.setText(Utils.getStringSharedPreference(ProfileActivity.this,Constants.SHARED_GYM_NAME));
        mNumberView = (EditText)findViewById(R.id.mobile);
        mNumberView.setEnabled(false);
        mPasswordView = (EditText)findViewById(R.id.password_sign);
        mPasswordView.setVisibility(View.GONE);
        mPasswordViewCnf = (EditText)findViewById(R.id.password_conf);
        mPasswordViewCnf.setVisibility(View.GONE);
        mEmail = (EditText)findViewById(R.id.email_sign);
        mEmail.setEnabled(false);
        mAddress = (EditText)findViewById(R.id.address_sign);
        mAddress.setEnabled(false);
        mOwner = (EditText)findViewById(R.id.owner);
        mOwner.setEnabled(false);
        Button button = (Button)findViewById(R.id.register);
        button.setText("Change Password");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        final Button buttonEdt = (Button)findViewById(R.id.edit_profile);
        buttonEdt.setVisibility(View.VISIBLE);
        buttonEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonEdt.setText("Update");
                mName.setEnabled(true);
                mNumberView.setEnabled(true);
                mEmail.setEnabled(true);
                mAddress.setEnabled(true);
                mOwner.setEnabled(true);
            }
        });

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
            Utils.setBooleanSharedPreference(ProfileActivity.this, Constants.SHARED_PREF_IS_LOGGED_IN, false);
            Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
