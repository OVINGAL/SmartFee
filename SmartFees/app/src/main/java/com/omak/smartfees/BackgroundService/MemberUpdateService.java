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

import java.util.ArrayList;

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
                            if(model.blocked.equalsIgnoreCase("No")) {
                                model.stored = "yes";
                            }
                            Customer.insertMember(this, model);
                        }

                    } catch (Exception e) {
                    }
                }
            }
            if(Utils.checkNetwork(this)) {
                String param = "gymtag=searchmember&gym_id=" + id;
                ArrayList<Customer> members = new ArrayList<Customer>();
                try {
                    String response = RestClient.httpPost(Url.MEMBER_URL, param);
                    members = JsonParser.parseMemberList(response);
                    for (Customer model : members) {
                        model.gymId = id;
                        if(Customer.insertMember(this,model) < 0){
                            Customer.updateDetails(this,model);
                        }
                    }
                } catch (Exception e) {
                }
            }

        }
    }
}
