package com.travelbook.activities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

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

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.internal.e;
import com.travelbook.adapter.ImageAdapter;
import com.travelbook.cache.ImageDownloader;
import com.travelbook.customview.ImgSlide;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.ReviewHolder;
import com.travelbook.holder.User;
import com.travelbook.utility.IpUtil;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class DetailBookView extends Activity{
	LayoutInflater mInflater;
	LinearLayout ll;
	Intent getIntent;
	EditText comment;
	ListView reviewList;
	ImageView playBtn;
	
	private String email;
	private String password;
	private String userName;
	private int loginType;
	private String writeremail;
	private String timetext;
	private String writertime;
	private String placename;
	String userId;
	
	ImageDownloader downloader = new ImageDownloader();
	
	private MyProgressDialog pd;
	private ImageDownloader profileDownloader = new ImageDownloader();
	private Vector<String> commentEmail;
	private Vector<String> commentName;
	private Vector<String> commentTime;
	private Vector<String> commentComment;
	MyAdapter adapter;
	
	private Double latitude;
	private Double longitude;
	class findLatLng extends AsyncTask<Void, Void, Void>
	{
		
		@Override
		protected void onPostExecute(Void result) {
			ImageView location_place = (ImageView) findViewById(R.id.btn_location_place);
			location_place.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DetailBookView.this, Map.class);
					System.out.println("latitude Detail::::: " + latitude);
					System.out.println("longitude Detail::::: " + longitude);
					intent.putExtra("latitude",latitude);
					intent.putExtra("longitude",longitude);
					intent.putExtra("isHotPlace",1);
					intent.putExtra("placename",placename);
					startActivity(intent);
				}
			});
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp()+"findLatLng.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("email",getIntent().getStringExtra("writeremail")));
				info.add(new BasicNameValuePair("time",getIntent().getStringExtra("writertime")));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	            XmlPullParser parser = factory.newPullParser();
	            parser.setInput(new InputStreamReader(entity.getContent()));
	            String latitudeString = null;
	    		String longitudeString = null;
	            while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                String name=parser.getName();
	                System.out.println("name?????"+name);
	                if( name!= null && name.equals("latitude"))
	                	latitudeString = parser.nextText();
	                if( name!= null && name.equals("longitude"))
	                	longitudeString = parser.nextText();
	             }
	            if(latitudeString != null && latitudeString != "")
	            {
	            	latitude = Double.parseDouble(latitudeString);
	            }
	            if(longitudeString != null && longitudeString != "")
	            {
	            	longitude = Double.parseDouble(longitudeString);
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_review_item);
		
		getIntent = getIntent();
		
		commentEmail = new Vector<String>();
		commentName = new Vector<String>();
		commentComment = new Vector<String>();
		commentTime = new Vector<String>();
		
		mInflater =(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		adapter = new MyAdapter(this);
		email = getIntent.getStringExtra("email");
		loginType = getIntent.getIntExtra("logintype", 0);
		userName = getIntent.getStringExtra("username");
		writeremail = getIntent.getStringExtra("writeremail");
		timetext = getIntent.getStringExtra("timetext");
		writertime = getIntent().getStringExtra("writertime");
		placename = getIntent().getStringExtra("placename");
		
		if(loginType==0){
			new FindUser(email).execute();
		}
		else if(loginType==1)
		{
			Session.openActiveSession(DetailBookView.this, true, new Session.StatusCallback() {
				
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					// TODO Auto-generated method stub
					Request.executeMeRequestAsync(session, new GraphUserCallback() {
						
						@Override
						public void onCompleted(GraphUser user, Response response) {
							// TODO Auto-generated method stub
							if(user!=null)
								email = user.getId();
							System.out.println("OnCompleted Email: :::: " + email);
						}
					});
				}
			});
		}
		
		new findLatLng().execute();
		
		adapter = new MyAdapter(this);
		reviewList = (ListView)findViewById(R.id.detail_review_comment);
		mInflater =(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.detail_review, null);
		TextView detail_name = (TextView) layout.findViewById(R.id.txt_detail_name);
		detail_name.setText(getIntent.getStringExtra("writername"));
		TextView detail_body = (TextView) layout.findViewById(R.id.txt_detail_review);
		detail_body.setText(getIntent.getStringExtra("body"));
		TextView detail_time = (TextView) layout.findViewById(R.id.txt_detail_time);
		detail_time.setText(timetext);
		Typeface tf = Typeface.createFromAsset(DetailBookView.this.getAssets(), "fonts/SeoulHangangM.ttf");
		detail_name.setTypeface(tf);
		detail_body.setTypeface(tf);
		detail_time.setTypeface(tf);
		
		if(email.equals(writeremail))
		{
			layout.findViewById(R.id.btn_detail_delete).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.btn_detail_delete).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(DetailBookView.this);
					builder.setMessage("want to delete review?");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							new deleteReview().execute();
						}
					});
					builder.setNegativeButton("NO", null);
					builder.show();
				}
			});
		}
		
		ProfilePictureView ppv = (ProfilePictureView)layout.findViewById(R.id.facebook_review_pic);
		ImageView profile = (ImageView) layout.findViewById(R.id.detail_review_writer_icon);
		
		if(writeremail.contains("@")&&writeremail.contains(".")){
			ppv.setVisibility(View.GONE);
			profile.setVisibility(View.VISIBLE);
			Bitmap profileBitmap = profileDownloader.profileDownload(IpUtil.getIp()+"profile/"+writeremail+"/profile.jpg");
			if(profileBitmap!=null)
				profile.setImageBitmap(profileBitmap);
			else
				profile.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.noprofile3));
		}else{
			System.out.println("facebook profile");
			ppv.setProfileId(writeremail);
			ppv.setVisibility(View.VISIBLE);
			profile.setVisibility(View.GONE);
			ppv.invalidate();
		}
		
		if(getIntent.getStringExtra("isvideo").equals("true")){
			playBtn = (ImageView)layout.findViewById(R.id.detail_review_play_img);
			playBtn.setVisibility(View.VISIBLE);
			playBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(DetailBookView.this,MyVideoPlayer.class);
					intent.putExtra("source", getIntent.getStringExtra("file1"));
					pd = MyProgressDialog.show(DetailBookView.this);
					DetailBookView.this.startActivity(intent);
				}
			});
		}
		if(reviewList.getHeaderViewsCount()==0){
			reviewList.addHeaderView(layout);
		}
		reviewList.setAdapter(adapter);
		
		final Vector<Bitmap> fileList = new Vector<Bitmap>();
		ImageDownloader downloader = new ImageDownloader();
		for(int i=0;i<getIntent.getIntExtra("filesize", 0);i++)
		{
			if(getIntent().getStringExtra("isvideo").equals("false"))
			{
				String filepath = getIntent.getStringExtra("file"+i);
				String[] files = filepath.split("/");
				files[files.length-1] = "Thumbnail_"+files[files.length-1];
				String filename = IpUtil.getIp();
				for(int gg=0;gg<files.length-1;gg++)
				{
					filename+=files[gg]+"/";
				}
				filename+=files[files.length-1];
				fileList.add(downloader.download(filename));
			}
			else if(getIntent().getStringExtra("isvideo").equals("true"))
			{
				String filepath = getIntent.getStringExtra("file"+0);
				String filename = IpUtil.getIp();
				filename+=filepath;
				fileList.add(downloader.download(filename));
				break;
			}
		}
		
		ImageAdapter imgAdapter = new ImageAdapter(this,fileList, true, false);
		
		ImgSlide imgList = (ImgSlide) layout.findViewById(R.id.detail_review_img_list);
		imgList.setUnselectedAlpha(1.0f);
		imgList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(DetailBookView.this,ImageFlip.class);
				intent.putExtra("urlsize", getIntent.getIntExtra("filesize", 0));
				for(int i=0;i<getIntent.getIntExtra("filesize", 0);i++)
				{
					intent.putExtra("url"+i,IpUtil.getIp()+getIntent.getStringExtra("file"+i));
				}
				startActivity(intent);
			}
		});
		imgList.setAdapter(imgAdapter);
		imgAdapter.notifyDataSetChanged();
		new readComment().execute();
	}
	
	
	@Override
	protected void onPause() {
		if(pd!=null)
			pd.dismiss();
		super.onPause();
	}


	@Override
	protected void onResume() {
		super.onResume();
	}


	public void mOnClick(View v)
	{
		if(v.getId()==R.id.review_comment_submit)
		{
			comment = (EditText) findViewById(R.id.review_comment_txt);
			new UploadComment().execute();
		}
	}
	
	class deleteReview extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp() + "deleteReview.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("time",getIntent().getStringExtra("writertime")));
				info.add(new BasicNameValuePair("email",email));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(entity.getContent()));
				
				int count = 0;
				while(parser.next() != XmlPullParser.END_DOCUMENT)
				{
					String name = parser.getName();
					System.out.println("name!!!"+ name);
					if(name!=null && name.equals("result"))
					{
						String result = parser.nextText();
						if(result.equals("success"))
						{
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									DetailBookView.this.finish();
								}});
						}
						else if(result.equals("failed"))
						{
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(DetailBookView.this);
									builder.setMessage("Delete Failed");
									builder.show();
								}});
						}
					}
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
	class readComment extends AsyncTask<Void,Void,Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				String time = getIntent.getStringExtra("writertime");
				String writerEmail = getIntent.getStringExtra("writeremail");
				
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp() + "readComment.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("time",time));
				info.add(new BasicNameValuePair("email",writerEmail));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(entity.getContent()));
				
				int count = 0;
				while(parser.next() != XmlPullParser.END_DOCUMENT)
				{
					String name = parser.getName();
					System.out.println("name!!!"+ name);
					if(name!=null && name.equals("email"+count))
					{
						commentEmail.add(parser.nextText());
						System.out.println(commentEmail.get(count));
					}
					if(name!=null && name.equals("name"+count))
					{
						commentName.add(parser.nextText());
						System.out.println(commentName.get(count));
					}
					if(name!=null && name.equals("time"+count))
					{
						commentTime.add(parser.nextText());
						System.out.println(commentTime.get(count));
					}
					if(name!=null && name.equals("comment"+count))
					{
						commentComment.add(parser.nextText());
						System.out.println(commentComment.get(count));
						count++;
					}
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
 
		@Override
		protected void onPostExecute(Void result) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}
		
		
	}
	class UploadComment extends AsyncTask<Void,Void,Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				String url = IpUtil.getIp() + "writeComment.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("name", userName));
				info.add(new BasicNameValuePair("email", email));
				info.add(new BasicNameValuePair("comment",comment.getText().toString()));
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
				info.add(new BasicNameValuePair("time",""+format.format(new Date())));
				info.add(new BasicNameValuePair("writeremail",getIntent.getStringExtra("writeremail")));
				info.add(new BasicNameValuePair("writertime",writertime));
				
				
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				System.out.println("comment email = "+ email);
				HttpResponse response = httpClient.execute(post);
				System.out.println("comment write execute end...");
				HttpEntity entity = response.getEntity();
				
				String result = null;
				
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				
				parser.setInput(new InputStreamReader(entity.getContent()));
				while( parser.next() != XmlPullParser.END_DOCUMENT)
				{
					String name = parser.getName();
					if(name!=null && name.equals("result"))
						result = parser.nextText();
				}
				
				final AlertDialog.Builder builder = new AlertDialog.Builder(DetailBookView.this);
				builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				if(result==null)
				{
					builder.setMessage("sorry, server error");
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							builder.show();
						}});
				}
				else if(result.equals("failed"))
				{
					builder.setMessage("Network Error, please retry");
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							builder.show();
						}});
				}
				else if(result.equals("success"))
				{
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							comment.setText("");
							
							commentEmail = new Vector<String>();
							commentName = new Vector<String>();
							commentComment = new Vector<String>();
							
							new readComment().execute();
						}
						
					});
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
	class MyAdapter extends BaseAdapter{

		Context context;
		int commentId;
		
		public MyAdapter() {
			super();
			// TODO Auto-generated constructor stub
		}
		public MyAdapter(Context context){
			this.context=context;
		}
		public MyAdapter(Context context, int commentId){
			this.context = context;
			this.commentId = commentId;
		}

		@Override
		public int getCount() {
			if(commentComment!=null)
				return commentComment.size();
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			if(convertView==null)
			{
				convertView = mInflater.inflate(R.layout.comment, null);
				ReviewHolder reviewHolder = new ReviewHolder(DetailBookView.this);
				reviewHolder.name = (TextView) convertView.findViewById(R.id.txt_comment_name);
				reviewHolder.body = (TextView) convertView.findViewById(R.id.txt_comment_body);
				reviewHolder.image = (ImageView) convertView.findViewById(R.id.image_photo);
				
				Typeface tf = Typeface.createFromAsset(DetailBookView.this.getAssets(), "fonts/SeoulHangangM.ttf");
				reviewHolder.name.setTypeface(tf);
				reviewHolder.body.setTypeface(tf);
				convertView.setTag(reviewHolder);
			}
			ReviewHolder reviewHolder = (ReviewHolder) convertView.getTag();
			
			ImageView delete = (ImageView) convertView.findViewById(R.id.btn_comment_delete);
			
			delete.setVisibility(View.INVISIBLE);
			
			if(commentEmail.get(position).equals(email))
			{
				delete.setVisibility(View.VISIBLE);
				delete.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(DetailBookView.this);
						builder.setMessage("want to delete comment?");
						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new deleteComment(position).execute();
							}
						});
						builder.setNegativeButton("NO", null);
						builder.show();
					}
				});
			}
			if(commentName!=null && commentComment != null)
			{
				reviewHolder.name.setText(commentName.get(position));
				reviewHolder.body.setText(commentComment.get(position));
				
				System.out.println("comment email "+ commentEmail.get(position));
				ProfilePictureView ppv = (ProfilePictureView)convertView.findViewById(R.id.facebook_profile_pic);
				
				if(commentEmail.get(position).contains("@")&&commentEmail.get(position).contains("."))
				{
					Bitmap profileImg = downloader.profileDownload(IpUtil.getIp()+"profile/"+commentEmail.get(position)+"/profile.jpg");
					if(profileImg!=null){
						reviewHolder.image.setImageBitmap(profileImg);
					}else{
						reviewHolder.image.setImageResource(R.drawable.noprofile3);
					}
					ppv.setVisibility(View.GONE);
					reviewHolder.image.setVisibility(View.VISIBLE);
				}
				else
				{
					if(! commentEmail.get(position).equals(ppv.getProfileId()))
						ppv.setProfileId(commentEmail.get(position));
					ppv.setVisibility(View.VISIBLE);
					reviewHolder.image.setVisibility(View.GONE);
				}
			}
			return convertView;
		}
		
	}
	
	class deleteComment extends AsyncTask<Void, Void, Void>
	{
		int pos;
		
		public deleteComment(int pos) {
			this.pos = pos;
		}

		@Override
		protected Void doInBackground(Void... params) 
		{
			try {
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp() + "deleteComment.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("writertime",writertime));
				System.out.println("writertime :::: " + writertime);
				info.add(new BasicNameValuePair("writeremail",writeremail));
				info.add(new BasicNameValuePair("email",email));
				info.add(new BasicNameValuePair("time",commentTime.get(pos)));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(entity.getContent()));
				
				int count = 0;
				while(parser.next() != XmlPullParser.END_DOCUMENT)
				{
					String name = parser.getName();
					System.out.println("name!!!"+ name);
					if(name!=null && name.equals("result"))
					{
						String result = parser.nextText();
						if(result.equals("success"))
						{
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									commentComment.remove(pos);
									commentEmail.remove(pos);
									commentName.remove(pos);
									adapter.notifyDataSetChanged();
								}});
						}
						else if(result.equals("failed"))
						{
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(DetailBookView.this);
									builder.setMessage("Delete Failed");
									builder.show();
								}});
						}
					}
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
	class FindUser extends AsyncTask<Void, Void, Void>{
		String findEmail;
		User user;
		public FindUser(String findEmail)
		{
			this.findEmail = findEmail;
			user = new User();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			email = user.getEmail();
			userName = user.getName();
			password = user.getPassword();
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
}
