package com.travelbook.activities;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;

public class Main extends Activity {
	private static int DATABASE_VERSION = 1;
	private static String databaseName = "PersonalDB";
	private static String tableName = "personal";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startpage);
		
		SQLiteOpenHelper helper = new SQLiteOpenHelper(this,databaseName, null, DATABASE_VERSION) {
			
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				String CREATE_SQL = "create table "+tableName+"("
						+"_id integer PRIMARY KEY autoincrement, " +
						"logintype integer,"+
						"photo text,"+
						"name text, " +
						"email text, " +
						"password text);";
				String CREATE_HISTORY_SQL = "CREATE TABLE search_history (_id integer primary key autoincrement, keyword text);";
				try{
				db.execSQL(CREATE_SQL);
				db.execSQL(CREATE_HISTORY_SQL);
				System.out.println("생성됨??;");
				}catch(Exception e){
				Log.e("Myinfo", "Exception in CREATE_SQL", e);
				}
				try{
					db.execSQL("insert into "+tableName+"(logintype) values ("+1231231+");");
				}catch(Exception e){
					Log.e("myinfo", "Exception in insert SQL", e);
				}
			}
		};
		SQLiteDatabase db = helper.getReadableDatabase();
		
		String SQL = "select logintype from "+tableName;
		Cursor c1 = db.rawQuery(SQL, null);
		int recordCount = c1.getCount();
		System.out.println("cursor count ; "+recordCount+"\n");
		c1.moveToNext();
		int logintype = c1.getInt(0);
		System.out.println("로긴타입ㅋ"+logintype);
		if(logintype==0)
		{
			SQL = "select email,password from "+tableName;
			c1 = db.rawQuery(SQL, null);
			c1.moveToNext();
			
			Intent intent = new Intent(this,Login.class);
			intent.putExtra("email", c1.getString(0));
			intent.putExtra("password", c1.getString(1));
			intent.putExtra("autologin", "true");
			startActivity(intent);
			finish();
		}
		else if(logintype ==1)
		{
			Intent intent = new Intent(this,FacebookLogin.class);
			startActivity(intent);
			finish();
		}
		else
		{
			Handler handler = new Handler();
			handler.postDelayed(new Runnable(){

				@Override
				public void run() {
					LayoutInflater inflator=getLayoutInflater();
					setContentView(R.layout.main);
				}}, 3000);
		}
		db.close();
		helper.close();
		
		
	}
	
	public void mOnClick(View v)
	{
		if(v.getId()==R.id.btn_travelbook_login)
		{
			Intent intent = new Intent(this,Login.class);
			startActivity(intent);
		}
		else if(v.getId()==R.id.btn_facebook_login)
		{
			Intent intent = new Intent(this,FacebookLogin.class);
			startActivity(intent);
		}
	}
//	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
//	} 
}
