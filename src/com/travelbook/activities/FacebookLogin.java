package com.travelbook.activities;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.travelbook.customview.MyProgressDialog;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

public class FacebookLogin extends Activity{
	
	private static int DATABASE_VERSION = 1;
	private static String databaseName = "PersonalDB";
	private static String tableName = "personal";
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	private MyProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Session.openActiveSession(this,true,new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if(session.isOpened())
				{
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user, Response response) {
							boolean isOpen = openDatabase();
							if(isOpen)
							{
								ContentValues recordValues = new ContentValues();
								recordValues.put("logintype", 1);
								System.out.println("업데이트결과...:"+db.update(tableName, recordValues, "rowid="+1, null));
								Intent intent = new Intent(FacebookLogin.this,Map.class);
								intent.putExtra("username", user.getName());
								intent.putExtra("email", user.getId());
								intent.putExtra("logintype", 1);
								pd = MyProgressDialog.show(FacebookLogin.this);
								startActivity(intent);
								finish();
							}
						}
					});
				}
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
		}

	private boolean openDatabase(){
		dbHelper = new DatabaseHelper(this);
		db = dbHelper.getWritableDatabase();
		
		
		return true;
	}
	private class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
	}//DatabaseHelper
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
}
