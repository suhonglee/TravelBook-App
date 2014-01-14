package com.travelbook.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.travelbook.cache.ImageDownloader;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.User;
import com.travelbook.utility.CustomMultiPartEntity;
import com.travelbook.utility.IpUtil;
import com.travelbook.utility.CustomMultiPartEntity.ProgressListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract.Helpers;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MyInfo extends Activity{
	
	private final int EDIT_PROFILE = 9999;
	

	private static int DATABASE_VERSION = 1;
	private static String databaseName = "PersonalDB";
	private static String tableName = "personal";
	
	boolean databaseCreated = false;
	boolean tableCreated = false;
	
	private MyProgressDialog pd;
//	@Override
//	public void onBackPressed() {
//		Intent intent = new Intent(MyInfo.this,Map.class);
//		intent.putExtra("username", getIntent().getStringExtra("username"));
//		intent.putExtra("email",getIntent().getStringExtra("email"));
//		intent.putExtra("password",getIntent().getStringExtra("email"));
//		intent.putExtra("logintype", getIntent().getIntExtra("logintype", 0));
//		startActivity(intent);
//		finish();
//	}

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	private String name;
	private String email;
	private String password;
	private String photo;
	
	private TextView txtName;
	private TextView txtEmail ;
	private TextView txtPassword ;
	private ImageView imgProfile;
	BitmapFactory.Options opts = new BitmapFactory.Options();
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == EDIT_PROFILE){
				if(openDatabase())
				{
					updateRecordParam(data);
					txtName.setText(data.getStringExtra("updateName"));
					txtEmail.setText(data.getStringExtra("updateEmail"));
					txtPassword.setText(data.getStringExtra("updatePassword"));
					opts.inSampleSize=4;
					photo = data.getStringExtra("updatePhoto");
					System.out.println("포토가머임;;"+photo);
					new uploadInfo().execute();
					Bitmap bitmap = BitmapFactory.decodeFile(photo, opts);
					imgProfile.setImageBitmap(bitmap);
				}
			}
		}
	}
	class FindUser extends AsyncTask<Void, Void, Void>{
		String email;
		User user;
		public FindUser(String email)
		{
			this.email = email;
			user = new User();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			ImageDownloader downloader = new ImageDownloader();
			final Bitmap bm = downloader.download(IpUtil.getIp()+"profile/"+user.getEmail()+"/profile.jpg");
			
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					txtName.setText(user.getName());
					txtEmail.setText(user.getEmail());
					txtPassword.setText(user.getPassword());
					if(bm!=null)
						imgProfile.setImageBitmap(bm);
				}});
			
			System.out.println("작업다끝났는데...ㅠ");
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			String result = null;
			
			try {
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp()+"userInfo.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("email",email));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	            XmlPullParser parser = factory.newPullParser();
	            parser.setInput(new InputStreamReader(entity.getContent()));
	            while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                String name=parser.getName();
	                System.out.println("name?????"+name);
	                if( name!= null && name.equals("name"))
	               	  user.setName(parser.nextText());
	                else if( name!= null && name.equals("email"))
	                	user.setEmail(parser.nextText());
	                else if( name!= null && name.equals("password"))
	                	user.setPassword(password = parser.nextText());
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
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

	}
	class uploadInfo extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			try{
			HttpClient httpClient = new DefaultHttpClient();
			String url= IpUtil.getIp() + "uploadProfile.do";
			HttpPost post = new HttpPost(url); 
			CustomMultiPartEntity entity = new CustomMultiPartEntity(new ProgressListener() {
				
				@Override
				public void transferred(long num) {
					publishProgress((int)((num/(float)100)*100));
				}
			});
			post.addHeader("email", txtEmail.getText().toString());
			entity.addPart("username", new StringBody(txtName.getText().toString(),Charset.forName("UTF-8")));
			entity.addPart("email", new StringBody(txtEmail.getText().toString(),Charset.forName("UTF-8")));
			entity.addPart("password", new StringBody(txtPassword.getText().toString(),Charset.forName("UTF-8")));
			
			if(photo!=null)
			{
				File file = new File(photo);
				String length = ""+file.length();
					
				entity.addPart("filename",new StringBody("profilePicture"));
				entity.addPart("length",new StringBody(""+length,Charset.forName("UTF-8")));
				FileBody bin = new FileBody(file);
				entity.addPart("profile",bin);
			}
			post.setEntity(entity);
			String result = null;
			HttpResponse response = httpClient.execute(post);
			HttpEntity responseEntity = response.getEntity();
             XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
             XmlPullParser parser = factory.newPullParser();
             parser.setInput(new InputStreamReader(responseEntity.getContent()));
             while ( parser.next() != XmlPullParser.END_DOCUMENT) {
                 String name=parser.getName();
                 System.out.println("name?????"+name);
                  if ( name != null && name.equals("result"))
                     result=parser.nextText();
              }
             if(result.equals("success"))
             {
            	 finish();
             }
             else if(result.equals("failed"))
             {
            	 AlertDialog.Builder builder = new AlertDialog.Builder(MyInfo.this);
            	 builder.setMessage("update failed");
            	 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
             }
             httpClient.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		}
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_information);
		txtName  = (TextView) findViewById(R.id.txt_personal_name);
		txtEmail = (TextView) findViewById(R.id.txt_personal_email);
		txtPassword = (TextView) findViewById(R.id.txt_personal_password);
		imgProfile = (ImageView)findViewById(R.id.img_profile);
		boolean isOpen = openDatabase();
		
		if(isOpen){
		}
		
		new FindUser(getIntent().getStringExtra("email")).execute();
		
	}
	public void mOnClick(View v){
		if(v.getId() == R.id.btn_modify){
			Intent intent = new Intent(MyInfo.this, EditProfile.class);
			intent.putExtra("email", getIntent().getStringExtra("email"));
			db.close();
			dbHelper.close();
			pd = MyProgressDialog.show(MyInfo.this);
			startActivityForResult(intent, EDIT_PROFILE);
		}
		else if(v.getId() == R.id.btn_logout)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(MyInfo.this);
			builder.setMessage("really want to logout?");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(MyInfo.this,Main.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					ContentValues recordValues = new ContentValues();
					recordValues.put("logintype", 123123);
					System.out.println("업데이트결과...:"+db.update(tableName, recordValues, "rowid="+1, null));
					db.close();
					dbHelper.close();
					pd = MyProgressDialog.show(MyInfo.this);
					startActivity(intent);
					finish();
				}
			});
			builder.setNegativeButton("NO", null);
			builder.show();
		}
	}

	private boolean openDatabase(){
		System.out.println(">>>>>>>>>>openDatabase()>>>databaseName::"+databaseName);
		
		dbHelper = new DatabaseHelper(this);
		System.out.println("dbHelper"+dbHelper);
		db = dbHelper.getWritableDatabase();
		
		return true;
	}

	private void executeRawQuery(){
		System.out.println(">>>>executeRawQuery() called.\n");
		
		Cursor c1 = db.rawQuery("select count(*) as Total from "+tableName, null);
		System.out.println(">>>>cursor count:"+c1.getCount());
		
		
		c1.moveToNext();
		System.out.println(">>>>>record count : "+c1.getInt(0));
		c1.close();
		
	}

//	private int insertRecord(){
//		
//		System.out.println("@@@@insertRecord()@@@����");
//		
//		ContentValues recordValues = new ContentValues();
//		
//		recordValues.put("id", "agisora");
//		recordValues.put("name", "Anne Hathaway");
//		recordValues.put("age", "24");
//		recordValues.put("email", "anne@gmail.com");
//		recordValues.put("phone", "016-123-4567");
//		recordValues.put("nationality", "US");
//		
//		int rowPosition = (int) db.insert(tableName, null, recordValues);
//
//		if(rowPosition ==-1){
//			return 0;
//		}
//		else return 1;
//		
//	}
	
	
	
	private class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("DatabaseHelper>>>>onCreate>>>TABLE_NAME:::"+tableName);
			System.out.println("fksahgjahfksjahfkjsa�����¤ä��������äǶ�ʸ��ó�");
			
			String CREATE_SQL = "create table "+tableName+"("
													+"_id integer PRIMARY KEY autoincrement, " +
													"photo text,"+
													"name text, " +
													"email text, " +
													"password text);";
			
			try{
				db.execSQL(CREATE_SQL);
			}catch(Exception e){
				Log.e("Myinfo", "Exception in CREATE_SQL", e);
			}
			
			try{
				db.execSQL("insert into "+tableName+"(name, email, password) values ('"+getIntent().getStringExtra("name")+"', '"
																					+getIntent().getStringExtra("email")+"', '"
																					+getIntent().getStringExtra("password")+"');");
			}catch(Exception e){
				Log.e("myinfo", "Exception in insert SQL", e);
			}
			
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
	}//DatabaseHelper
	
	private int updateRecordParam(Intent intent){
		System.out.println("updating records");
		ContentValues recordValues = new ContentValues();
		recordValues.put("photo", intent.getStringExtra("updatePhoto"));
		recordValues.put("name", intent.getStringExtra("updateName"));
		recordValues.put("email", intent.getStringExtra("updateEmail"));
		recordValues.put("password", intent.getStringExtra("updatePassword"));
	
		String[] whereArgs = {email};
		
		int rowAffected = db.update(tableName, recordValues, "email = ?", whereArgs);
		
		return rowAffected;
	}
	
}
