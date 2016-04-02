package com.omak.smartfees;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.omak.smartfees.Adapter.MemberAdapter;
import com.omak.smartfees.Adapter.MemberFeeAdapter;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Model.Fees;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PayFeeActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private static TextView mDate;
    ArrayList<Customer> membersList;
    MemberFeeAdapter adapter;
    ArrayList<Customer> paidMembersList;
    ArrayList<Customer> notPaidMembersList;
    Spinner status;
    boolean fistTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_fee);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(PayFeeActivity.this, Constants.SHARED_GYM_NAME));

        mRecyclerView = (RecyclerView)findViewById(R.id.member_fee_list);
        mLayoutManager = new LinearLayoutManager(PayFeeActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        status = (Spinner) findViewById(R.id.fees_status);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        mDate = (TextView)findViewById(R.id.month_selection);
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH) + 1;
        int y = c.get(Calendar.YEAR);
        mDate.setText(m + "/" + y);
        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("","selection");
                    if(status.getSelectedItemPosition() == 0) {
                        adapter = new MemberFeeAdapter(paidMembersList, PayFeeActivity.this,true,mDate.getText().toString());
                        mRecyclerView.setAdapter(adapter);
                    } else {
                        adapter = new MemberFeeAdapter(notPaidMembersList, PayFeeActivity.this,false,mDate.getText().toString());
                        mRecyclerView.setAdapter(adapter);
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(), "date");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        membersList = new ArrayList<Customer>();
        notPaidMembersList = new ArrayList<Customer>();
        paidMembersList = new ArrayList<Customer>();
        membersList = Customer.getAllMemberListInGym(PayFeeActivity.this,Utils.getStringSharedPreference(PayFeeActivity.this,Constants.SHARED_GYM_ID));
        if(membersList.size() > 0) {
            for(int i = 0; i < membersList.size(); i++){
                ArrayList<Fees> feesList = Fees.isPayFeesOnMonth(PayFeeActivity.this,membersList.get(i).gymId,membersList.get(i).regNum,mDate.getText().toString());
                if(feesList != null && feesList.size() > 0){
                    Customer c = membersList.get(i);
                    c.fees =feesList.get(0);
                    paidMembersList.add(c);
                } else {
                    notPaidMembersList.add(membersList.get(i));
                }
            }
            if(status.getSelectedItemPosition() == 0) {
                adapter = new MemberFeeAdapter(paidMembersList, PayFeeActivity.this,true,mDate.getText().toString());
                mRecyclerView.setAdapter(adapter);
            } else {
                adapter = new MemberFeeAdapter(notPaidMembersList, PayFeeActivity.this,false,mDate.getText().toString());
                mRecyclerView.setAdapter(adapter);
            }
        }
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
            mDate.setText((month+1)+"/"+year);
        }
    }
}
