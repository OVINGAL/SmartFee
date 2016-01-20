package com.omak.smartfees.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.omak.smartfees.DB.ContentProviderDb;

import java.util.ArrayList;


public class Customer {
	
	public static final String TABLE_CUSTOMER_DB = "TABLE_CUSTOMER_DB";
	
	public String _id;
	public static final String ID = "_id";
	
	public String rollNo;
	public static final String ROLL_NO = "rollNo";
	
	public String firstName;
	public static final String FIRST_NAME = "firstName";
	
	public String lastName;
	public static final String LAST_NAME = "lastName";
	
	public String fatherName;
	public static final String FATHER_NAME = "fatherName";
	
	public String motherName;
	public static final String MOTHER_NAME = "motherName";
	
	public String address;
	public static final String ADDRESS = "address";
	
	public String contactNumber;
	public static final String NUMBER = "contactNumber";
	
	public String classDivision;
	public static final String CLASS_DIV = "classDivision";
	
	public boolean attStatus = true;
	
	
	public static final String createCustomerDb() {
		StringBuilder createStatment = new StringBuilder("CREATE TABLE ").append(TABLE_CUSTOMER_DB).append(" (").append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
				.append(ROLL_NO).append(" TEXT,").append(FIRST_NAME).append(" TEXT,").append(LAST_NAME).append(" TEXT,").append(FATHER_NAME).append(" TEXT,")
				.append(MOTHER_NAME).append(" TEXT,").append(ADDRESS).append(" TEXT,").append(NUMBER).append(" TEXT,").append(CLASS_DIV).append(" TEXT").append(")");
		return createStatment.toString();
	}

//	private static final String[] profileDbProjections = new String[] { ID, PROFILE_ID, PROFILE_NAME, PROFILE_TYPE, CHECKIN, RUNS, USER_TYPE, IMAGE_URL, IMAGE_THUMBNAIL_URL};

	private static ContentValues convertToContentValues(Customer stud) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(ROLL_NO, stud.rollNo);
		contentvalues.put(FIRST_NAME, stud.firstName);
		contentvalues.put(LAST_NAME, stud.lastName);
		contentvalues.put(FATHER_NAME, stud.fatherName);
		contentvalues.put(MOTHER_NAME, stud.motherName);
		contentvalues.put(ADDRESS, stud.address);
		contentvalues.put(NUMBER, stud.contactNumber);
		contentvalues.put(CLASS_DIV, stud.classDivision);
		return contentvalues;
	}

	private static Customer getValueFromCursor(Cursor cursor) {
		Customer model = new Customer();
		model._id = cursor.getString(cursor.getColumnIndex(ID));
		model.rollNo = cursor.getString(cursor.getColumnIndex(ROLL_NO));
		model.firstName = cursor.getString(cursor.getColumnIndex(FIRST_NAME));
		model.lastName = cursor.getString(cursor.getColumnIndex(LAST_NAME));
		model.fatherName = cursor.getString(cursor.getColumnIndex(FATHER_NAME));
		model.motherName = cursor.getString(cursor.getColumnIndex(MOTHER_NAME));
		model.address = cursor.getString(cursor.getColumnIndex(ADDRESS));
		model.contactNumber = cursor.getString(cursor.getColumnIndex(NUMBER));
		model.classDivision = cursor.getString(cursor.getColumnIndex(CLASS_DIV));
		
		return model;
	}

	public static final long insertStudent(Context context, Customer stud) {
		ContentValues initialvalues = convertToContentValues(stud);
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		Uri resultUri = context.getContentResolver().insert(contentUri, initialvalues);
		return Long.parseLong(ContentProviderDb.getPath(resultUri));
	}
	
	public static ArrayList<Customer> getAllStudentList(Context context) {
		ArrayList<Customer> studentList = new ArrayList<Customer>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}
	
	public static ArrayList<Customer> getAllStudentListInClass(Context context,String selectedClass) {
		ArrayList<Customer> studentList = new ArrayList<Customer>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String selection = CLASS_DIV+ " = '" +selectedClass+"'";
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, FIRST_NAME + " ASC");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				studentList.add(getValueFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return studentList;
	}
	
	public static ArrayList<String> getDistinctClass(Context context) {
		ArrayList<String> distinctList = new ArrayList<String>();
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String[] values = new String[] { " DISTINCT " + CLASS_DIV };
		Cursor cursor = context.getContentResolver().query(contentUri, values, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				distinctList.add(cursor.getString(cursor.getColumnIndex(CLASS_DIV)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return distinctList;
	}
	
	public static CursorLoader getStudentListInClassCursor(Context context,String selectedClass) {
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		String selection = CLASS_DIV+ " = '" +selectedClass+"'";
		return new CursorLoader(context, contentUri, null, selection, null, null);
	}
	
	public static Customer getStudentDetails(Context context,String id) {
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
	
	public static int deleteStudent(Context context,Customer s) {
		Uri contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CUSTOMER_DB);
		int resultUri = context.getContentResolver().delete(contentUri, ID+"=?", new String[] {s._id});
		return resultUri;
	}

}
