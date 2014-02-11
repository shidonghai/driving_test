package com.assistant.drivingtest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.assistant.drivingtest.utils.LogUtil;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = DBHelper.class.getSimpleName();

	public static final String DB_NAME = "driving_test.db";
	private static final int DB_VERSION = 1;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LogUtil.d(TAG, "onCreate");
		db.execSQL(ThirdSubjectTable.CREATE_DB_TABLE);
		db.execSQL(ThirdSubjectItemTable.CREATE_DB_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

}
