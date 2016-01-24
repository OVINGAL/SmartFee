package com.omak.smartfees.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.omak.smartfees.Model.Customer;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "SmartFees.db";
	public static final int DATABASE_VERSION = 2;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(Customer.createCustomerDb());
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		dropAllTables(database);
		onCreate(database);
	}
	
	private void dropAllTables(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + Customer.TABLE_CUSTOMER_DB);
	}

}
