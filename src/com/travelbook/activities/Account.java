package com.travelbook.activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class Account extends Activity{
	EditText name;
	EditText email;
	EditText password;
	EditText password_confirm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account);
		name = (EditText)findViewById(R.id.account_name);
		email = (EditText)findViewById(R.id.account_email);
		password = (EditText)findViewById(R.id.account_password);
		password_confirm = (EditText)findViewById(R.id.account_password_confirm);
	}
	
	public void mOnClick(View v)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		boolean emailCheck = true;
		if(v.getId()== R.id.btn_account)
		{
			int at = 0;
			int dot = 0;
			for(int i=0;i<email.getText().length();i++)
			{
				if(email.getText().charAt(i)=='@' && email.getText().charAt(0)!='@')
				{
					++at;
				}
				if(i!=0)
				{
					if(email.getText().charAt(i)=='.' && email.getText().charAt(i-1)!='@' && i != email.getText().length()-1)
					{
						++dot;
					}
					if(email.getText().charAt(i)=='.' && at != 1)
					{
						at+=999;
						break;
					}
				}
			}
			if(at != 1 || dot != 1)
			{
				emailCheck = false;
				builder.setMessage("check email!");
				builder.show();
			}
			
			else if((! password.getText().toString().equals(password_confirm.getText().toString())))
			{
				builder.setMessage("check password and password_confirm!");
				builder.show();
			}
			else if(password.getText().length()<8)
			{
				builder.setMessage("check password length!");
				builder.show();
			}
			else if(email.getText().length()<1)
			{
				builder.setMessage("check email!");
				builder.show();
			}
			else if(name.getText().length()<1)
			{
				builder.setMessage("check name!");
				builder.show();
			}
			else
			{
				if(emailCheck)
				{
					account account = new account();
					account.execute();
				}
			}
			
		}
	}
	
	class account extends AsyncTask<Void, Void, Void>
	{
		
		@Override
		protected Void doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			String url = IpUtil.getIp() + "userAccount.do";
			HttpPost post = new HttpPost(url);
			List info = new ArrayList();
			info.add(new BasicNameValuePair("name", name.getText().toString()));
			info.add(new BasicNameValuePair("email", email.getText().toString()));
			info.add(new BasicNameValuePair("password",password.getText().toString()));
			
			try {
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				
				String result = null;
				
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	             XmlPullParser parser = factory.newPullParser();
	             parser.setInput(new InputStreamReader(entity.getContent()));
	             while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                 String name=parser.getName();
	                  if ( name != null && name.equals("result"))
	                     result=parser.nextText();
	              }
	             
				final AlertDialog.Builder builder = new AlertDialog.Builder(Account.this);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				if(result==null)
				{
					builder.setMessage("sorry, server error");
				}
				else if(result.equals("failed"))
				{
					builder.setMessage("Network Error, please retry");
				}
				else if(result.equals("failed_email"))
				{
					builder.setMessage("already exist email, please check email");
				}
				else if(result.equals("success"))
				{
					builder.setMessage("Welcome! make your travel book.");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
				}
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						builder.show();
					}});
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
}
