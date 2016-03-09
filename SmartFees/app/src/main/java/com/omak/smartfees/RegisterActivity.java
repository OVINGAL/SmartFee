package com.omak.smartfees;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
//import org.apache.commons.codec.binary.Base64;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Logger;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.ArrayList;
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
    private Customer member;
    private boolean isupdate = false;
    private ImageView db;
    private String mSavedpath = "";
    Customer model;
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
                fragment.show(getSupportFragmentManager(), "date");
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerMember();
            }
        });
        db = (ImageView)findViewById(R.id.dp);
        db.setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmap == null) {
                    bitmap = ((BitmapDrawable)db.getDrawable()).getBitmap();
                }
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                try {
                    Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    bitmap.recycle();
                    bitmap = bmRotated;
                    db.setImageBitmap(bmRotated);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        });
//        http://gymapp.oddsoftsolutions.com/uploadedimages/14571164952016-03-04-15-27-09-470.jpg
        if(getIntent().hasExtra("Member")) {
            isupdate = true;
            member = (Customer)getIntent().getSerializableExtra("Member");
            mName.setText(member.name);
            mMobile.setText(member.phone);
            mAge.setText(member.age);
            mweight.setText(member.weight);
            mRegnum.setText(member.regNum);
            mAddress.setText(member.address);
            mDate.setText(member.date);
            ((Button)findViewById(R.id.register)).setText("Update");
            if(member.photo != null && member.photo.length() > 0 ){
                String imageurl = "http://gymapp.oddsoftsolutions.com/uploadedimages/" + member.photo;
                Logger.e(" Glide  " + imageurl);
                Glide.with(RegisterActivity.this).load(imageurl)
                        .error(R.drawable.ic_launcher)
                        .into(db);
            } else {
                db.setImageResource(R.drawable.ic_launcher);
            }
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getOrientation(String path){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        return orientation;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (resultCode == Activity.RESULT_OK && null != data) {
                Uri selectedImageUri = null;
                switch (requestCode) {

                    case 1001:
                        if(data.getData() != null) {
                            selectedImageUri = data.getData();
                            Logger.e(selectedImageUri + "      URI");
                            filePath = getPath(selectedImageUri, MediaStore.Images.Media.DATA);
                            Logger.e(filePath + "     PATH");
                            file = new File(filePath);
                            BitmapFactory.Options options = null;
                            options = new BitmapFactory.Options();
                            bitmap = BitmapFactory.decodeFile(filePath, options);
//                            bitmap = rotateBitmap(bitmap, getOrientation(filePath));
//                            FileUpload fileUpload = new FileUpload();
//                            mSavedpath = fileUpload.SaveImage(bitmap, filename);
//                            Logger.e("Path saved " + mSavedpath);
                            db.setImageBitmap(bitmap);
                        } else if(data.getExtras() != null) {
                            Bundle bundle = data.getExtras();
                            bitmap = (Bitmap) bundle.get("data");
                            filename = "Camera_" + Calendar.getInstance().getTimeInMillis();
                            FileUpload fileUpload = new FileUpload();
                            mSavedpath = fileUpload.SaveImage(bitmap, filename);
                            filePath = mSavedpath;
                            db.setImageBitmap(bitmap);
                        }
                        break;

                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong  " + e.getMessage() , Toast.LENGTH_LONG)
                    .show();
        }
    }

    public String getPath(Uri uri, String projectionVal) {
        // just some safety built in
        if (uri == null) {
            return null;
        }

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
             model = new Customer();

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
                model.photo = filename;
            if(isupdate) {
                model.memberId = member.memberId;
                model._id = member._id;
            }

            if(Utils.checkNetwork(RegisterActivity.this)) {
                String param = "";
                if(isupdate) {
                    String status = model.blocked.equalsIgnoreCase("No") ? "1" : "2";
                    param = "gymtag=editmember&txtname=" + model.name + "&txtphone=" + model.phone + "&txtdob=" + model.age
                            + "&txtregno=" + model.regNum + "&txtweight=" + model.weight + "&txtaddress=" + model.address + "&txtjoindate=" + model.date
                            + "&staff_id=" + "" + "&staff_type=" + "" + "&gym_id=" + model.gymId + "&mem_id=" + model.memberId + "&txtstatus=" + status;
                }else {
                    param = "gymtag=addmember&txtname=" + model.name + "&txtphone=" + model.phone + "&txtdob=" + model.age
                            + "&txtregno=" + model.regNum + "&txtweight=" + model.weight + "&txtaddress=" + model.address + "&txtjoindate=" + model.date
                            + "&staff_id=" + "" + "&staff_type=" + "" + "&gym_id=" + model.gymId;
                }
                param = param.replace(" ", "%20");
                try {
                    String response = RestClient.httpPost(Url.MEMBER_URL, param);
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("response");
                    if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                        model.stored = "yes";
                        if(!isupdate) {
                            model.memberId = jsonObject.getString("mem_id");
                            memID = model.memberId;
                        } else {
                            memID = model.memberId;
                        }
                        if(isupdate) {
                            Customer.updateDetails(RegisterActivity.this, model);
                        } else {
                            Customer.insertMember(RegisterActivity.this, model);
                        }
//                        upload();
                        return jsonObject.getString("status");
                    } else {
                        return jsonObject.getString("status_msg");
                    }

                } catch (Exception e) {
                    return e.getMessage();
                }
            } else {
                if(isupdate) {
                    Customer.updateDetails(RegisterActivity.this, model);
                } else {
                    Customer.insertMember(RegisterActivity.this, model);
                }
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
            if(bitmap != null) {
                UploadDoc uploadDoc = new UploadDoc();
                uploadDoc.execute();
            } else {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    public class UploadDoc extends AsyncTask<Void, Void, String> {

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
        protected String doInBackground(Void... voids) {
            try {
                upload();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            finish();
            Logger.e(" serverResponse :  " + aVoid);
            dialog.dismiss();
        }
    }

    private void upload() throws IOException,JSONException {
        String gym_id = Utils.getStringSharedPreference(RegisterActivity.this, Constants.SHARED_GYM_ID);
        String url = "http://gymapp.oddsoftsolutions.com/gym_member.php?gymtag=addphoto&mem_id=" + memID +
                "&gym_id=" + gym_id +"&staff_id=2&staff_type=staff&filename=" + filename;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte [] ba = bao.toByteArray();
        String image_str = Base64.encodeToString(ba, Base64.DEFAULT);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("image", image_str));


        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        String contentAsString = RestClient.readIt(is);
        Logger.e(contentAsString + " resp ");
        JSONObject jsonObject = new JSONObject(contentAsString);
        jsonObject = jsonObject.getJSONObject("response");
        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
            model.photo = jsonObject.getString("mem_photo").substring(jsonObject.getString("mem_photo").lastIndexOf("/") + 1);
        }
        if(isupdate) {
            Customer.updateDetails(RegisterActivity.this, model);
        } else {
            Customer.insertMember(RegisterActivity.this, model);
        }
        Logger.e(contentAsString);
    }

}

