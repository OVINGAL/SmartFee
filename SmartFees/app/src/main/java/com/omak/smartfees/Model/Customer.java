package com.omak.smartfees.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.omak.smartfees.DB.ContentProviderDb;

import java.io.Serializable;
import java.util.ArrayList;


public class Customer implements Serializable{
	
	public static final String TABLE_CUSTOMER_DB = "TABLE_CUSTOMER_DB";
	
	public String _id;
	public static final String ID = "_id";

	public String gymId = "";
	public static final String GYM_ID = "gym_id";

	public String memberId = "";
	public static final String MEMBER_ID = "member_id";
	
	public String regNum = "";
	public static final String REG_NO = "regnumber";
	
	public String name = "";
	public static final String NAME = "Name";
	
	public String phone = "";
	public static final String PHONE = "phone";
	
	public String age = "";
	public static final String AGE = "age";
	
	public String weight = "";
	public static final String WEIGHT = "weight";
	
	public String address = "";
	public static final String ADDRESS = "address";
	
	public String date = "";
	public static final String DATE = "date";

	public String blocked = "";
	public static final String BLOCKED = "blocked";

	public String deleted = "";
	public static final String DELETED = "deleted";

	public String stored = "";
	public static final String STORED = "stored";

	public String photo = "";
	public static final String PHOTO = "photo";

	public Customer(){
		gymId = "";
		memberId = "";
		regNum = "";
		name = "";
		phone = "";
		age = "";
		weight = "";
		address = "";
		date = "";
		photo = "";
		blocked = "No";
		deleted = "No";
		stored = "No";

	}

	public static final String createCustomerDb() {
		StringBuilder createStatment = new StringBuilder("CREATE TABLE ").append(TABLE_CUSTOMER_DB).append(" (").append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
				.append(MEMBER_ID).append(" TEXT,").append(GYM_ID).append(" TEXT,").append(REG_NO).append(" TEXT,").append(NAME).append(" TEXT,").append(PHOTO).append(" TEXT,")
				.append(BLOCKED).append(" TEXT,").append(DELETED).append(" TEXT,").append(STORED).append(" TEXT,").append(PHONE).append(" TEXT,")
				.append(AGE).append(" TEXT,").append(ADDRESS).append(" TEXT,").append(DATE).append(" TEXT,").append(WEIGHT).append(" TEXT,").append("unique (regnumber))");
		return createStatment.toString();
	}

//	private static final String[] profileDbProjections = new String[] { ID, PROFILE_ID, PROFILE_NAME, PROFILE_TYPE, CHECKIN, RUNS, USER_TYPE, IMAGE_URL, IMAGE_THUMBNAIL_URL};

	private static ContentValues convertToContentValues(Customer stud) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(MEMBER_ID, stud.memberId);
		contentvalues.put(GYM_ID, stud.gymId);
		contentvalues.put(REG_NO, stud.regNum);
		contentvalues.put(NAME, stud.name);
		contentvalues.put(PHONE, stud.phone);
		contentvalues.put(ADDRESS, stud.address);
		contentvalues.put(PHOTO, stud.photo);
		contentvalues.put(AGE, stud.age);
		contentvalues.put(DATE, stud.date);
		contentvalues.put(WEIGHT, stud.weight);
		contentvalues.put(BLOCKED, stud.blocked.toLowerCase());
		contentvalues.put(DELETED, stud.deleted.toLowerCase());
		contentvalues.put(STORED, stud.stored.toLowerCase());
		return contentvalues;
	}

	private static Customer getValueFromCursor(Cursor cursor) {
		Customer model = new Customer();
		model._id = cursor.getString(cursor.getColumnIndex(ID));
		model.memberId = cursor.getString(cursor.getColumnIndex(MEMBER_ID));
		model.gymId = cursor.getString(cursor.getColumnIndex(GYM_ID));
		model.regNum = cursor.getString(cursor.getColumnIndex(REG_NO));
		model.name = cursor.getString(cursor.getColumnIndex(NAME));
		model.phone = cursor.getString(cursor.getColumnIndex(PHONE));
		model.address = cursor.getString(cursor.getColumnIndex(ADDRESS));
		model.photo = cursor.getString(cursor.getColumnIndex(PHOTO));
		model.age = cursor.getString(cursor.getColumnIndex(AGE));
		model.date = cursor.getString(cursor.getColumnIndex(DATE));
		model.weight = cursor.getString(cursor.getColumnIndex(WEIGHT));
		model.blocked = cursor.getString(cursor.getColumnIndex(BLOCKED));
		model.deleted = cursor.getString(cursor.getColumnIndex(DELETED));
		model.stored = cursor.getString(cursor.getColumnIndex(STORED));
		
		return model;
	}

	public static final long insertMember(Context context, Customer stud) {
		ContentValues initialvalues = convertToContentValues(stud);
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		Uri resultUri = context.getContentResolver().insert(contentUri, initialvalues);
		return Long.parseLong(ContentProviderDb.getPath(resultUri));
	}
	

	
	public static ArrayList<Customer> getAllMemberListInGym(Context context,String gym_id) {
		ArrayList<Customer> studentList = new ArrayList<Customer>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + DELETED + " != 'yes'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, NAME + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}

	public static ArrayList<Customer> getMemberListInGymSearch(Context context,String gym_id,String search) {
		ArrayList<Customer> studentList = new ArrayList<Customer>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + DELETED + " != 'yes'" + " AND " + NAME + " LIKE '%" + search +"%'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, NAME + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}

	public static ArrayList<Customer> getAllMemberListInGymNotInsterted(Context context,String gym_id) {
		ArrayList<Customer> studentList = new ArrayList<Customer>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + MEMBER_ID + " = ''";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, NAME + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}

//	public static CursorLoader getStudentListInClassCursor(Context context,String selectedClass) {
//		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
//		String selection = CLASS_DIV+ " = '" +selectedClass+"'";
//		return new CursorLoader(context, contentUri, null, selection, null, null);
//	}

	public static Customer getMemberDetails(Context context,String id) {
		Customer student = new Customer();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String selection = ID+ " = '" +id+"'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			student = getValueFromCursor(cursor);
		}
		cursor.close();
		return student;
	}
	
	public static final long updateDetails(Context context,Customer s) {
		ContentValues initialvalues = convertToContentValues(s);
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		int resultUri = context.getContentResolver().update(contentUri, initialvalues, ID+"=?", new String[] {s._id});
		return resultUri;
	}
	
	public static int deleteCustomer(Context context,String regNum) {
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		int resultUri = context.getContentResolver().delete(contentUri, REG_NO + "=?", new String[]{regNum});
		return resultUri;
	}

	public static int deleteAllCustomer(Context context) {
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		int resultUri = context.getContentResolver().delete(contentUri,null,null);
		return resultUri;
	}

}
