package com.omak.smartfees.Parser;

import com.omak.smartfees.Model.Customer;
import com.omak.smartfees.Model.Staff;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by afsal on 24/1/16.
 */
public class JsonParser {
    //{"response":{"status":"success","status_msg":"Successfully Listed"},"searchresult":[{"staff_id":"6","staff_name":"Anil","staff_phone":"9638527410","staff_status":"1"}]}

    public static ArrayList<Staff> parseStaffList(String response) throws JSONException{
        ArrayList<Staff> staffs = new ArrayList<Staff>();
        JSONObject jsonObject = new JSONObject(response);
        if(jsonObject.has("searchresult") && jsonObject.get("searchresult") instanceof JSONArray) {
            JSONArray result = jsonObject.getJSONArray("searchresult");
            for (int i = 0; i < result.length(); i++){
                Staff s = new Staff();
                JSONObject staffJson = result.getJSONObject(i);
                s.staffId = staffJson.getString("staff_id");
                s.name = staffJson.getString("staff_name");
                s.number = staffJson.getString("staff_phone");
                s.isActive = staffJson.getString("staff_status").equalsIgnoreCase("1");
                staffs.add(s);
            }
        }
        return staffs;
    }

    public static ArrayList<Customer> parseMemberList(String response) throws JSONException{
        ArrayList<Customer> customers = new ArrayList<Customer>();
        JSONObject jsonObject = new JSONObject(response);
        if(jsonObject.has("searchresult") && jsonObject.get("searchresult") instanceof JSONArray) {
            JSONArray result = jsonObject.getJSONArray("searchresult");
            for (int i = 0; i < result.length(); i++){
                Customer s = new Customer();
                JSONObject staffJson = result.getJSONObject(i);
                s.memberId = staffJson.getString("mem_id");
                s.regNum = staffJson.getString("mem_regno");
                s.name = staffJson.getString("mem_name");
                s.phone = staffJson.getString("mem_phone");
                s.date = staffJson.getString("mem_joindate");
                s.blocked = staffJson.getString("mem_status").equalsIgnoreCase("1") ? "No" : "Yes";
                customers.add(s);
            }
        }
        return customers;
    }
}
