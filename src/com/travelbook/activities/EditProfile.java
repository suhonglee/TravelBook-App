package com.travelbook.activities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.ObjectOutputStream.PutField;
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

import com.travelbook.cache.ImageDownloader;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.User;
import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditProfile extends Activity{
	private final int EDIT_PROFILE =9999;
	private final int TAKE_GALLERY=2000;
	private static int DATABASE_VERSION = 1;
	private static String databaseName = "PersonalDB";
	private static String tableName = "personal";
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	private EditText txtName;
	private TextView txtEmail;
	private EditText txtPassword;
	private EditText txtPasswordConfirm;
	private Button btnOk;
	private Button btnCancel;
	
	private String updateName;
	private String updateEmail;
	private String updatePassword;
	
	private String name;
	private String email;
	private String password;
	
	private ImageView photo;
	private Uri currImageURI;
	private String path;
	
	private MyProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profile);
		
		photo = (ImageView)findViewById(R.id.img_btn_profile);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		
		boolean isOpen = openDatabase();
		
		txtName = (EditText) findViewById(R.id.txt_personal_name);
		txtEmail = (TextView) findViewById(R.id.txt_personal_email);
		txtPassword = (EditText) findViewById(R.id.txt_personal_password);
		txtPasswordConfirm = (EditText) findViewById(R.id.txt_personal_password_confirm);
		
		//여기다 작업
		new FindUser(getIntent().getStringExtra("email")).execute();
		
		btnOk = (Button) findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(txtName.getText().length()<2)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
					builder.setMessage("check Name length!");
					builder.setPositiveButton("OK", null);
					builder.show();
				}
				else if(txtPassword.getText().toString().equals(txtPasswordConfirm.getText().toString())&&txtPassword.getText().length()>7)
				{
					updateName = txtName.getText().toString();
					System.out.println("updateName@@@"+updateName);
	
					
					updateEmail = txtEmail.getText().toString();
					System.out.println("updateEmail@@@"+updateEmail);
					
					
					updatePassword = txtPassword.getText().toString();
					System.out.println("updatePhone@@@"+updatePassword);
					
			
					
					Intent intent = new Intent(EditProfile.this, MyInfo.class);
					
					intent.putExtra("updatePhoto", path);
					intent.putExtra("updateName", updateName);
					intent.putExtra("updateEmail", updateEmail);
					intent.putExtra("updatePassword", updatePassword);
					
					EditProfile.this.setResult(RESULT_OK, intent);
					finish();
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
					builder.setMessage("check Password!");
					builder.setPositiveButton("OK", null);
					builder.show();
				}
			}
		});//onClicklistener
		
		photo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent( Intent.ACTION_PICK ) ;
				intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE ) ;
				pd = MyProgressDialog.show(EditProfile.this);
				startActivityForResult( intent, TAKE_GALLERY ) ;
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
	}//onCreate
	
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
	}

	private boolean openDatabase(){
		
		System.out.println("@@@@openDatabase()ȣ��!!!");
		dbHelper = new DatabaseHelper(this);
		db = dbHelper.getWritableDatabase();
		
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data!=null)
		{
			 currImageURI = data.getData( ) ;
			path = getPath(EditProfile.this, currImageURI ) ;
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize =4;
			Bitmap bm = BitmapFactory.decodeFile(path, opts);
			if(bm!=null)
				photo.setImageBitmap(bm);
		}
		
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
			System.out.println("EditProfile�� DatabaseHelper>>>>>>>onOpen>>>>����.");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			System.out.println("DatabaseHelper>>>>onUpgrade>>>>>oldVersion::"+oldVersion+", newVersion::"+newVersion);
		}
		
	}//DatabaseHelper
	public static String getPath(Activity activity , Uri uri)
	{
	    String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
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
					txtPasswordConfirm.setText(user.getPassword());
					if(bm!=null)
						photo.setImageBitmap(bm);
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
}//Class
