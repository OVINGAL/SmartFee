package com.omak.smartfees;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.omak.smartfees.BackgroundService.MemberUpdateService;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONObject;

public class MainHomeActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        findViewById(R.id.member_btn).setOnClickListener(this);
        findViewById(R.id.staff_btn).setOnClickListener(this);
        findViewById(R.id.profile_btn).setOnClickListener(this);
        findViewById(R.id.share_btn).setOnClickListener(this);
        findViewById(R.id.about_btn).setOnClickListener(this);
        findViewById(R.id.pay_fees).setOnClickListener(this);
        findViewById(R.id.web_app).setOnClickListener(this);
        if(Utils.checkNetwork(MainHomeActivity.this)){
            MemberUpdateService.startupdateAction(MainHomeActivity.this,Utils.getStringSharedPreference(MainHomeActivity.this,Constants.SHARED_GYM_ID));
        }
        HomeStatusAsync homeStatusAsync = new HomeStatusAsync();
        homeStatusAsync.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(this, Constants.SHARED_GYM_NAME));
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.member_btn:
                intent = new Intent(MainHomeActivity.this,HomeActivity.class);
                startActivity(intent);
                break;
            case R.id.staff_btn:
                intent = new Intent(MainHomeActivity.this,Staffs.class);
                startActivity(intent);
                break;
            case R.id.profile_btn:
                intent = new Intent(MainHomeActivity.this,ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.share_btn:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out Smart Fee App on Google Play! https://play.google.com/store/apps/details?id=com.omak.smartfees");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.about_btn:
                intent = new Intent(MainHomeActivity.this,ContactActivity.class);
                intent.putExtra("webApp",false);
                startActivity(intent);
                break;
            case R.id.pay_fees:
                intent = new Intent(MainHomeActivity.this,PayFeeActivity.class);
                startActivity(intent);
                break;
            case R.id.web_app:
                intent = new Intent(MainHomeActivity.this,ContactActivity.class);
                intent.putExtra("webApp",true);
                startActivity(intent);
                break;
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
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainHomeActivity.this);
            alertDialogBuilder.setTitle("Log Out");
            alertDialogBuilder.setMessage("Log out now?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Utils.setStringSharedPreference(MainHomeActivity.this, "username","");
                    Utils.setStringSharedPreference(MainHomeActivity.this, "pass","");
                    Utils.setStringSharedPreference(MainHomeActivity.this, Constants.SHARED_GYM_ID, "");
                    Utils.setStringSharedPreference(MainHomeActivity.this, Constants.SHARED_GYM_NAME, "");
                    Utils.setStringSharedPreference(MainHomeActivity.this, "LastUpdatedTime", "");
                    Customer.deleteAllCustomer(MainHomeActivity.this);
                    Utils.setBooleanSharedPreference(MainHomeActivity.this, Constants.SHARED_PREF_IS_LOGGED_IN, false);
                    Intent intent = new Intent(MainHomeActivity.this,LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            alertDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class HomeStatusAsync extends AsyncTask<String,Void,String> {

        String param;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainHomeActivity.this);
            dialog.setMessage("Loading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            param = "?gymtag=gym_status" +"&staff_id=" + Utils.getStringSharedPreference(MainHomeActivity.this,Constants.SHARED_STAFF_ID)
                    +"&gym_id=" + Utils.getStringSharedPreference(MainHomeActivity.this,Constants.SHARED_GYM_ID);
            param = param.replace(" " ,"%20");
            try {
                String response = RestClient.httpGet(Url.BASE_URL + param);
                JSONObject jsonObject = new JSONObject(response);
                jsonObject = jsonObject.getJSONObject("response");
                return jsonObject.toString();
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            try {
                JSONObject responseJ = new JSONObject(response);
                if(responseJ.getString("gym_status").equalsIgnoreCase("blocked") || responseJ.getString("staff_status").equalsIgnoreCase("blocked")) {
                    Toast.makeText(MainHomeActivity.this,"Please contact Oddsoft team... ",Toast.LENGTH_LONG).show();
                    Utils.setStringSharedPreference(MainHomeActivity.this, "username","");
                    Utils.setStringSharedPreference(MainHomeActivity.this, "pass", "");
                    Utils.setStringSharedPreference(MainHomeActivity.this, Constants.SHARED_GYM_ID, "");
                    Utils.setStringSharedPreference(MainHomeActivity.this, Constants.SHARED_GYM_NAME, "");
                    Utils.setStringSharedPreference(MainHomeActivity.this, "LastUpdatedTime", "");
                    Customer.deleteAllCustomer(MainHomeActivity.this);
                    Utils.setBooleanSharedPreference(MainHomeActivity.this, Constants.SHARED_PREF_IS_LOGGED_IN, false);
                    Intent intent = new Intent(MainHomeActivity.this,LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e){

            }
        }
    }
}
