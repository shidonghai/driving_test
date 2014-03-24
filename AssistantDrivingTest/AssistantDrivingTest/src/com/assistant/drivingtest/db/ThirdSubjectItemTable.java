package com.assistant.drivingtest.db;public class ThirdSubjectItemTable {	public static final String TABLE_NAME = "third_subject_item";	public static final String CREATE_DB_TABLE = "CREATE TABLE IF NOT EXISTS "			+ TABLE_NAME + "(" + Columns._ID			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.SUBJECT_ID			+ " INTEGER, " + Columns.NAME + " VARCHAR, " + Columns.TYPE			+ " INTEGER, " + Columns.VOICE_LONGITUDE + " VARCHAR, "			+ Columns.VOICE_LATITUDE + " VARCHAR, " + Columns.START_LONGITUDE			+ " VARCHAR, " + Columns.START_LATITUDE + " VARCHAR, "			+ Columns.END_LONGITUDE + " VARCHAR, " + Columns.END_LATITUDE			+ " VARCHAR, " + Columns.VOICE + " VARCHAR, " + Columns.SPEED			+ " INTEGER " + ")";	public static final String DEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;	public static class Columns {		public static final String _ID = "id";		public static final String SUBJECT_ID = "subject_id";		public static final String NAME = "name";		public static final String TYPE = "type";		public static final String VOICE_LONGITUDE = "voice_longitude";		public static final String VOICE_LATITUDE = "voice_latitude";		public static final String START_LONGITUDE = "start_longitude";		public static final String START_LATITUDE = "start_latitude";		public static final String END_LONGITUDE = "end_longitude";		public static final String END_LATITUDE = "end_latitude";		public static final String VOICE = "voice";		public static final String SPEED = "speed";	}}