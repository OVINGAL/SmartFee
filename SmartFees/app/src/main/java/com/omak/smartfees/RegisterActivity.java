package com.omak.smartfees;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Logger;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private EditText mMobile;
    private EditText mAge,mweight,mRegnum,mAddress;
    private static EditText mDate;
    private EditText mName;
    private MemberRegisterTask mAuthTask;
    private File file;
    private String filePath,filename,encodedString = null;
    private ProgressDialog dialog;
    private Bitmap bitmap;
    private String memID;
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

        findViewById(R.id.dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(takePictureIntent, "Choose Profile");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                if (pickIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(chooserIntent, 1001);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (resultCode == Activity.RESULT_OK && null != data) {
                Uri selectedImageUri = null;
                switch (requestCode) {

                    case 1001:
                        selectedImageUri = data.getData();
                        Logger.e(selectedImageUri + "      URI");
                        filePath = getPath(selectedImageUri, MediaStore.Images.Media.DATA);
                        Logger.e(filePath + "     PATH");
                        file = new File(filePath);
                        UploadDoc uploadDoc = new UploadDoc();
                        uploadDoc.execute();
                        break;

                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public String getPath(Uri uri, String projectionVal) {
        // just some safety built in
        if (uri == null) {
            return null;
        }
//        // try to retrieve the image from the media store first
//        // this will only work for images selected from gallery
//        String[] projection = {projectionVal};
//        Cursor cursor = managedQuery(uri, projection, null, null, null);
//        if (cursor != null) {
//            int column_index = cursor
//                    .getColumnIndexOrThrow(projectionVal);
//            cursor.moveToFirst();
//            return cursor.getString(column_index);
//        }
//        // this is our fallback here
//        return uri.getPath();

        Uri selectedImage = uri;
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        // Get the cursor
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgPath = cursor.getString(columnIndex);
            cursor.close();

            // Get the Image's file name
            String fileNameSegments[] = imgPath.split("/");
            filename = fileNameSegments[fileNameSegments.length - 1];
            // Put file name in Async Http Post Param which will used in Php web app
            return imgPath;
        }
        return uri.getPath();
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
                        model.memberId = jsonObject.getString("mem_id");
                        memID = model.memberId;
                        Customer.insertMember(RegisterActivity.this, model);
//                        upload();
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

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    public class UploadDoc extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        String serverResponse = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(RegisterActivity.this);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Uploading...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            BitmapFactory.Options options = null;
            options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(filePath, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Must compress the Image to reduce image size to make upload easy
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byte_arr = stream.toByteArray();
            // Encode Image to String
            encodedString = Base64.encodeToString(byte_arr, Base64.DEFAULT);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Logger.e(" serverResponse :  " + serverResponse);
            dialog.dismiss();
        }
    }

    // Make Http call to upload Image to Php server
//    public void makeHTTPCall() {
//        RequestParams params = new RequestParams();
//        params.put("image",encodedString);
//        AsyncHttpClient client = new AsyncHttpClient();
//        // Don't forget to change the IP address to your LAN address. Port no as well.
//        client.post("http://gymapp.oddsoftsolutions.com/gym_photo.php",
//                params, new AsyncHttpResponseHandler() {
//                    // When the response returned by REST has Http
//                    // response code '200'
//                    @Override
//                    public void onSuccess(String response) {
//                        // Hide Progress Dialog
//                        Logger.e(response);
//                    }
//
//                    // When the response returned by REST has Http
//                    // response code other than '200' such as '404',
//                    // '500' or '403' etc
//                    @Override
//                    public void onFailure(int statusCode, Throwable error,
//                                          String content) {
//                        // Hide Progress Dialog
//                        // When Http response code is '404'
//                        if (statusCode == 404) {
//                            Toast.makeText(getApplicationContext(),
//                                    "Requested resource not found",
//                                    Toast.LENGTH_LONG).show();
//                        }
//                        // When Http response code is '500'
//                        else if (statusCode == 500) {
//                            Toast.makeText(getApplicationContext(),
//                                    "Something went wrong at server end",
//                                    Toast.LENGTH_LONG).show();
//                        }
//                        // When Http response code other than 404, 500
//                        else {
//                            Toast.makeText(
//                                    getApplicationContext(),
//                                    "Error Occured n Most Common Error: n1. Device not connected to Internetn2. Web App is not deployed in App servern3. App server is not runningn HTTP Status code : "
//                                            + statusCode, Toast.LENGTH_LONG)
//                                    .show();
//                        }
//                    }
//                });
//    }

    private void upload() throws IOException,JSONException {
        String object = new String();
        object = "image=" + encodedString ;
        Logger.e(filename);
        String gym_id = Utils.getStringSharedPreference(RegisterActivity.this, Constants.SHARED_GYM_ID);
        URL url = new URL("http://gymapp.oddsoftsolutions.com/gym_member.php?gymtag=addphoto&mem_id=" + memID +
                "&gym_id=" + gym_id +"&staff_id=2&staff_type=staff&filename=" + filename);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setChunkedStreamingMode(1024);

        httpURLConnection.setRequestProperty("Content-Type", "bitmap;charset=utf-8");
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Transfer-Encoding", "chunked");

        OutputStream outStream = httpURLConnection.getOutputStream();
        if (outStream != null) {
            if (encodedString.length() > 0) {
                outStream.write(object.toString().getBytes());
            }

            outStream.flush();
            outStream.close();
            outStream = null;
        }
        httpURLConnection.connect();
        int response = httpURLConnection.getResponseCode();
        Logger.e(" Code  " + response);
        InputStream is;
        if(response > 400) {
            is = httpURLConnection.getErrorStream();
        }
        else {
            is = httpURLConnection.getInputStream();
        }
        String contentAsString = RestClient.readIt(is);
        Logger.e("The response is: " + response + "  " + contentAsString);
    }

}

