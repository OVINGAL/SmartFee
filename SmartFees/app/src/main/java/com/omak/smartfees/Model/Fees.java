package com.omak.smartfees.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.omak.smartfees.DB.ContentProviderDb;
import com.omak.smartfees.Global.Logger;

import java.io.Serializable;
import java.util.ArrayList;


public class Fees implements Serializable{

	public static final String TABLE_FEES_DB = "TABLE_FEES_DB";

	public String _id;
	public static final String ID = "_id";

	public String gymId = "";
	public static final String GYM_ID = "gym_id";

	public String memberId = "";
	public static final String MEMBER_ID = "member_id";

	public String regNum = "";
	public static final String REG_NO = "regnumber";

	public String amount = "";
	public static final String AMOUNT = "Amount";

	public String date = "";
	public static final String DATE = "date";

	public String stored = "";
	public static final String STORED = "modify";

	public String remark = "";
	public static final String REMARK = "remark";



	public Fees(){
		gymId = "";
		memberId = "";
		regNum = "";
		amount = "";
		date = "";
		stored = "NO";
		remark = "";
	}

	public static final String createFeesDb() {
		StringBuilder createStatment = new StringBuilder("CREATE TABLE ").append(TABLE_FEES_DB).append(" (").append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
				.append(MEMBER_ID).append(" TEXT,").append(GYM_ID).append(" TEXT,").append(REG_NO).append(" TEXT,").append(AMOUNT).append(" TEXT,")
				.append(DATE).append(" TEXT,").append(STORED).append(" TEXT,").append(REMARK).append(" TEXT,").append("unique (regnumber) on conflict fail)");
		return createStatment.toString();
	}

//	private static final String[] profileDbProjections = new String[] { ID, PROFILE_ID, PROFILE_NAME, PROFILE_TYPE, CHECKIN, RUNS, USER_TYPE, IMAGE_URL, IMAGE_THUMBNAIL_URL};

	private static ContentValues convertToContentValues(Fees stud) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(ID, stud._id);
		contentvalues.put(MEMBER_ID, stud.memberId);
		contentvalues.put(GYM_ID, stud.gymId);
		contentvalues.put(REG_NO, stud.regNum);
		contentvalues.put(AMOUNT, stud.amount);
		contentvalues.put(DATE, stud.date);
		contentvalues.put(REMARK, stud.remark);
		contentvalues.put(STORED, stud.stored.toLowerCase());
		return contentvalues;
	}

	private static Fees getValueFromCursor(Cursor cursor) {
		Fees model = new Fees();
		model._id = cursor.getString(cursor.getColumnIndex(ID));
		model.memberId = cursor.getString(cursor.getColumnIndex(MEMBER_ID));
		model.gymId = cursor.getString(cursor.getColumnIndex(GYM_ID));
		model.regNum = cursor.getString(cursor.getColumnIndex(REG_NO));
		model.amount = cursor.getString(cursor.getColumnIndex(AMOUNT));
		model.date = cursor.getString(cursor.getColumnIndex(DATE));
		model.remark = cursor.getString(cursor.getColumnIndex(REMARK));
		model.stored = cursor.getString(cursor.getColumnIndex(STORED));
		
		return model;
	}

	public static final long insertFees(Context context, Fees stud) {
		ContentValues initialvalues = convertToContentValues(stud);
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		Uri resultUri = context.getContentResolver().insert(contentUri, initialvalues);
		return Long.parseLong(ContentProviderDb.getPath(resultUri));
	}


	public static ArrayList<Fees> isPayFeesOnMonth(Context context,String gym_id,String regNum,String date) {
		ArrayList<Fees> studentList = new ArrayList<Fees>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + REG_NO + " = '" + regNum + "' AND " + DATE + " = '" + date + "'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, DATE + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}

	public static ArrayList<Fees> getUserFeesDetails(Context context,String gym_id,String regNum) {
		ArrayList<Fees> studentList = new ArrayList<Fees>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + REG_NO + " = '" + regNum + "'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, DATE + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}

	public static ArrayList<Fees> getAllMemberListPaided(Context context,String gym_id,String date,String regNum) {
		ArrayList<Fees> studentList = new ArrayList<Fees>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + REG_NO + " = '" + regNum + "' AND " + DATE + "= '" + date + "'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, AMOUNT + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}

	public static ArrayList<Fees> getAllMemberListInGymNotStored(Context context,String gym_id) {
		ArrayList<Fees> studentList = new ArrayList<Fees>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		String selection = GYM_ID+ " = '" +gym_id+"' AND " + STORED + " = 'no'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, AMOUNT + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}


	
	public static final int updateDetails(Context context,Fees s) {
		ContentValues initialvalues = convertToContentValues(s);
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		int resultUri = context.getContentResolver().update(contentUri, initialvalues, ID+"=?", new String[] {s._id});
		Logger.e("Update status  " + resultUri);
		return resultUri;
	}
	
	public static int deleteFees(Context context,String regNum) {
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		int resultUri = context.getContentResolver().delete(contentUri, REG_NO + "=?", new String[]{regNum});
		return resultUri;
	}

	public static int deleteAllFees(Context context) {
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FEES_DB);
		int resultUri = context.getContentResolver().delete(contentUri,null,null);
		return resultUri;
	}

}
