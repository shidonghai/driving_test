package com.assistant.drivingtest.db;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.assistant.drivingtest.R.string;
import com.assistant.drivingtest.domain.ThirdSubject;
import com.assistant.drivingtest.domain.ThirdTestItem;
import com.assistant.drivingtest.utils.LogUtil;
import com.baidu.platform.comapi.b.e;

public class DBManager {

	private final String TAG = "zxh";

	private DBHelper mDbHelper;

	private SQLiteDatabase mDatabase;

	private static DBManager mInstance;

	private DBManager() {

	}

	public static DBManager getInstance() {
		if (null == mInstance) {
			mInstance = new DBManager();
		}

		return mInstance;
	}

	public void init(Context context) {
		mDbHelper = new DBHelper(context);
		mDatabase = mDbHelper.getWritableDatabase();
	}

	public int getSubjectSize() {
		int size = 0;

		Cursor cursor = mDatabase.query(ThirdSubjectTable.TABLE_NAME, null,
				null, null, null, null, null);
		if (null != cursor) {
			size = cursor.getCount();
			cursor.close();
		}

		return size;
	}

	public long getThirdSubjectTableMaxId() {
		long id = -1;
		String sql = "select max(id) AS maxId from "
				+ ThirdSubjectTable.TABLE_NAME;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (null != cursor) {
			cursor.moveToFirst();
			id = cursor.getLong(cursor.getColumnIndex("maxId"));
			cursor.close();
		}

		return id;
	}

	public List<ThirdSubject> getThirdSubjects() {
		List<ThirdSubject> list = new ArrayList<ThirdSubject>();
		Cursor cursor = mDatabase.query(ThirdSubjectTable.TABLE_NAME, null,
				null, null, null, null, null);

		if (null != cursor) {
			try {
				ThirdSubject subject;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					subject = new ThirdSubject();
					subject.id = cursor.getLong(cursor
							.getColumnIndex(ThirdSubjectTable.Columns._ID));
					subject.name = cursor.getString(cursor
							.getColumnIndex(ThirdSubjectTable.Columns.NAME));

					list.add(subject);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}

		return list;
	}

	public List<ThirdTestItem> getThirdTestItems(long id) {
		List<ThirdTestItem> items = new ArrayList<ThirdTestItem>();

		Cursor cursor = mDatabase.query(ThirdSubjectItemTable.TABLE_NAME, null,
				ThirdSubjectItemTable.Columns.SUBJECT_ID + " = ? ",
				new String[] { String.valueOf(id) }, null, null, null, null);

		if (null != cursor) {
			try {
				ThirdTestItem item;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					item = new ThirdTestItem();
					item.name = cursor
							.getString(cursor
									.getColumnIndex(ThirdSubjectItemTable.Columns.NAME));
					item.latitude = cursor
							.getDouble(cursor
									.getColumnIndex(ThirdSubjectItemTable.Columns.LATITUDE));
					item.longitude = cursor
							.getDouble(cursor
									.getColumnIndex(ThirdSubjectItemTable.Columns.LONGITUDE));
					item.voice = cursor
							.getString(cursor
									.getColumnIndex(ThirdSubjectItemTable.Columns.VOICE));
					item.speed = cursor
							.getString(cursor
									.getColumnIndex(ThirdSubjectItemTable.Columns.SPEED));
					item.type = cursor
							.getInt(cursor
									.getColumnIndex(ThirdSubjectItemTable.Columns.TYPE));

					LogUtil.d(TAG, "getThirdTestItems:" + item.name + " "
							+ item.type + " " + item.latitude + " "
							+ item.longitude + " " + item.voice + " "
							+ item.speed);

					items.add(item);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}

		return items;
	}

	public boolean saveNewLine(ThirdSubject subject) {
		if (null == subject) {
			return false;
		}

		ContentValues values = new ContentValues();
		values.put(ThirdSubjectTable.Columns.NAME, subject.name);
		long id = mDatabase.insert(ThirdSubjectTable.TABLE_NAME, null, values);
		LogUtil.d(TAG, "id:" + id);

		List<ThirdTestItem> testItems = subject.items;
		Log.d(TAG, "testItems:" + testItems.size());
		try {
			for (ThirdTestItem item : testItems) {
				// mDatabase.execSQL("INSERT INTO "
				// + ThirdSubjectItemTable.TABLE_NAME
				// + " VALUES(null, ?, ?, ?, ?, ? ,?)", new Object[] { id,
				// item.name, item.longitude, item.latitude, item.voice,
				// item.speed });

				values = new ContentValues();
				values.put(ThirdSubjectItemTable.Columns.SUBJECT_ID, id);
				values.put(ThirdSubjectItemTable.Columns.NAME, item.name);
				values.put(ThirdSubjectItemTable.Columns.TYPE, item.type);
				values.put(ThirdSubjectItemTable.Columns.LONGITUDE,
						item.longitude);
				values.put(ThirdSubjectItemTable.Columns.LATITUDE,
						item.latitude);
				values.put(ThirdSubjectItemTable.Columns.VOICE, item.voice);
				values.put(ThirdSubjectItemTable.Columns.SPEED, item.speed);

				mDatabase
						.insert(ThirdSubjectItemTable.TABLE_NAME, null, values);
			}
		} catch (Exception exception) {
			Log.e(TAG, "save error:" + exception.toString());
		} finally {
		}

		return true;
	}

	public void deleteLine(long id) {
		mDatabase.delete(ThirdSubjectTable.TABLE_NAME, "id = ?",
				new String[] { String.valueOf(id) });
	}

	public void close() {
		mDatabase.close();
	}

}
