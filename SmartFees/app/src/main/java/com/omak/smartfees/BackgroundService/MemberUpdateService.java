package com.omak.smartfees.BackgroundService;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Logger;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Model.Staff;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;
import com.omak.smartfees.Parser.JsonParser;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class MemberUpdateService extends IntentService {

    /**
     * Starts this service to perform action update . If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startupdateAction(Context context, String id) {
        Intent intent = new Intent(context, MemberUpdateService.class);
        intent.putExtra("Id",id);
        context.startService(intent);
    }

    public MemberUpdateService() {
        super("MemberUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String id = intent.getStringExtra("Id");
            ArrayList<Customer> customers = Customer.getAllMemberListInGymNotInsterted(this, id);
            Logger.e("New count : " + customers.size());
            for (Customer model:customers) {
                if(Utils.checkNetwork(this)) {

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
                            Customer.updateDetails(this, model);
                        }

                    } catch (Exception e) {
                    }
                }
            }

            customers = Customer.getAllMemberListInGymDeleted(this, id);
            Logger.e("Delete count : " + customers.size());
            for (Customer model:customers) {
                if(Utils.checkNetwork(this)) {

                    String param = "gymtag=deletemember&mem_id=" + model.memberId +"&gym_id=" + id;
                    try {
                        String response = RestClient.httpPost(Url.MEMBER_URL, param);
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject = jsonObject.getJSONObject("response");
                        if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                            Customer.deleteCustomer(this, model.regNum);
                        } else {
                        }

                    } catch (Exception e) {
                    }
                }
            }

            customers = Customer.getAllMemberListInGymNotUpdated(this, id);
            Logger.e("Update count : " + customers.size());
            for (Customer model:customers) {
                if(Utils.checkNetwork(this)) {
                    //s.blocked = staffJson.getString("mem_status").equalsIgnoreCase("1") ? "No" : "Yes";
                    String status = model.blocked.equalsIgnoreCase("No") ? "1" : "2";
                    String param = "gymtag=editmember&txtname=" + model.name + "&txtphone=" + model.phone + "&txtdob=" + model.age
                            + "&txtregno=" + model.regNum + "&txtweight=" + model.weight + "&txtaddress=" + model.address + "&txtjoindate=" + model.date
                            + "&staff_id=" + "" + "&staff_type=" + "" + "&gym_id=" + model.gymId + "&mem_id=" + model.memberId + "&txtstatus=" + status;
                    param = param.replace(" ", "%20");
                    try {
                        String response = RestClient.httpPost(Url.MEMBER_URL, param);
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject = jsonObject.getJSONObject("response");
                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            model.stored = "Yes";
                            Logger.e("Update");
                            int k  = Customer.updateDetails(this, model);
                            Logger.e("Update After  " + k);
                        }

                    } catch (Exception e) {
                    }
                }
            }

            if(Utils.checkNetwork(this)) {
                String callTime = Utils.getStringSharedPreference(getApplicationContext(), "LastUpdatedTime");
                if(callTime.length() < 2) {
                    callTime = "0";
                }
                String param = "gymtag=searchmember&gym_id=" + id+"&lastcalltime=" + callTime;
                param = param.replace(" ", "%20");
                Logger.e("params     " + param);
                ArrayList<Customer> members = new ArrayList<Customer>();
                try {
                    String response = RestClient.httpPost(Url.MEMBER_URL, param);
                    Logger.e(response);
                    members = JsonParser.parseMemberList(response);
                    for (Customer model : members) {
                        model.gymId = id;
                        model.stored = "yes";
                        if(Customer.insertMember(this,model) < 0){
                            Customer.updateDetails(this,model);
                        }
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String time = dateFormat.format(new Date());
                    Utils.setStringSharedPreference(getApplicationContext(),"LastUpdatedTime",time);
                    Logger.e(time);
                } catch (Exception e) {
                }
            }

        }
    }
}
