package com.travelbook.activities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.travelbook.customview.MyProgressDialog;
import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Login extends Activity{

	EditText email;
	EditText password;
	
	ProgressDialog dialog;
	
	private static int DATABASE_VERSION = 1;
	private static String databaseName = "PersonalDB";
	private static String tableName = "personal";
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	private MyProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		dialog  = new ProgressDialog(Login.this);
		email = (EditText) findViewById(R.id.login_email);
		password = (EditText) findViewById(R.id.login_password);
		
		if(getIntent().getStringExtra("autologin")!=null)
		{
			if(getIntent().getStringExtra("autologin").equals("true"))
			{
				boolean isOpen = openDatabase();
				
				if(isOpen)
				{
					dialog.setMessage("Auto login...");
					dialog.setCanceledOnTouchOutside(false);
					dialog.show();
//					new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							new Handler().post(new Runnable(){
//
//								@Override
//								public void run() {
//									
//								}});
//						}
//					}).start();
					email.setText(getIntent().getStringExtra("email"));
					password.setText(getIntent().getStringExtra("password"));
					new login().execute();
				}
			}
		}
	}
	
	public void mOnClick(View v)
	{
		if(v.getId()==R.id.btn_login)
		{
			boolean isOpen = openDatabase();
			
			if(isOpen){
				//executeRawQuery();
//				executeRawQueryParam();
				System.out.println("DB OPEN 됨!!!");
			}
			new login().execute();
		}
		else if(v.getId()==R.id.btn_account)
		{
			Intent intent = new Intent(this,Account.class);
			pd = MyProgressDialog.show(Login.this);
			startActivity(intent);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
	}

	class login extends AsyncTask<Void, Void, Void>
	{
		AlertDialog alertdialog;
		@Override
		protected Void doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();
			String url = IpUtil.getIp() + "userLogin.do";
			HttpPost post = new HttpPost(url);
			List info = new ArrayList();
			info.add(new BasicNameValuePair("email",email.getText().toString()));
			info.add(new BasicNameValuePair("password",password.getText().toString()));
			
			String result = null;
			String userName = null;
			try {
				post.setEntity(new UrlEncodedFormEntity(info, HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				System.out.println("Login execute complete...");
				HttpEntity entity = response.getEntity();
	             XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	             XmlPullParser parser = factory.newPullParser();
	             parser.setInput(new InputStreamReader(entity.getContent()));
	             while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                 String name=parser.getName();
	                 System.out.println("name?????"+name);
	                  if ( name != null && name.equals("result"))
	                     result=parser.nextText();
	                  else if( name!= null && name.equals("username"))
	                	  userName = parser.nextText();
	              }
				final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
				if(result==null)
				{
					builder.setMessage("sorry, server error");
				}
				else if(result.equals("success"))
				{
					ContentValues recordValues = new ContentValues();
					recordValues.put("email", email.getText().toString());
					recordValues.put("password", password.getText().toString());
					recordValues.put("logintype", 0);
					
					System.out.println("업데이트결과...:"+db.update(tableName, recordValues, "rowid="+1, null));
					
					Intent intent = new Intent(Login.this,Map.class);
					intent.putExtra("username", userName);
					intent.putExtra("email", email.getText().toString());
					intent.putExtra("password",password.getText().toString());
					intent.putExtra("logintype", 0);
					db.close();
					dbHelper.close();
					dialog.dismiss();
					startActivity(intent);
					finish();
				}
				else
				{
					if(result.equals("failed"))
					{
						builder.setMessage("Network Error, please retry");
					}
					else if(result.equals("failed_email"))
					{
						builder.setMessage("email not exist");
					}
					else if(result.equals("failed_password"))
					{
						builder.setMessage("password not correct");
					}
					runOnUiThread(new Runnable(){
	
						@Override
						public void run() {
							alertdialog = builder.show();
					}});
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private boolean openDatabase(){
		System.out.println(">>>>>>>>>>openDatabase()>>>databaseName::"+databaseName);
		
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
	
}
