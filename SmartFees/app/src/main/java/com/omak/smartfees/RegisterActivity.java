package com.omak.smartfees;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private EditText mMobile;
    private EditText mAge,mweight,mRegnum,mAddress;
    private static EditText mDate;
    private EditText mName;
    private MemberRegisterTask mAuthTask;

    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(RegisterActivity.this, Constants.SHARED_GYM_NAME));

        mName = (EditText)findViewById(R.id.name_member);
        mMobile = (EditText)findViewById(R.id.phone);
        mAge = (EditText)findViewById(R.id.age);
        mweight = (EditText)findViewById(R.id.weight);
        mRegnum = (EditText)findViewById(R.id.regnum);
        mAddress = (EditText)findViewById(R.id.address);
        mDate = (EditText)findViewById(R.id.date);

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(),"date");
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerMember();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mDate.setText(day+"/"+(month+1)+"/"+year);
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(RegisterActivity.this);
            dialog.setMessage("Adding...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        public String doInBackground(String... params) {
            Customer model = new Customer();
            model.gymId = Utils.getStringSharedPreference(RegisterActivity.this, Constants.SHARED_GYM_ID);
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

            if(Utils.checkNetwork(RegisterActivity.this)) {

                String param = "gymtag=addmember&txtname=" + model.name + "&txtphone=" + model.phone + "&txtdob=" + model.age
                        + "&txtregno=" + model.regNum + "&txtweight=" + model.weight + "&txtaddress=" + model.address + "&txtjoindate=" + model.date
                        + "&staff_id=" + "" + "&staff_type=" + "" + "&gym_id=" + model.gymId;
                param = param.replace(" ", "%20");
                try {
                    String response = RestClient.httpPost(Url.MEMBER_URL, param);
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("response");
                    if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                        model.stored = "yes";
                        Customer.insertMember(RegisterActivity.this, model);
                        return jsonObject.getString("status");
                    } else {
                        return jsonObject.getString("status_msg");
                    }

                } catch (Exception e) {
                    return e.getMessage();
                }
            } else {
                Customer.insertMember(RegisterActivity.this, model);
                return "success";
            }
        }

        @Override
        protected void onPostExecute(final String success) {
            mAuthTask = null;
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            Toast.makeText(RegisterActivity.this, success, Toast.LENGTH_SHORT).show();
            if(success.equalsIgnoreCase("success")) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

