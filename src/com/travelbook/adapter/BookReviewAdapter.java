package com.travelbook.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.travelbook.activities.DetailBookView;
import com.travelbook.activities.MyVideoPlayer;
import com.travelbook.activities.OtherBook;
import com.travelbook.activities.R;
import com.travelbook.cache.ImageDownloader;
import com.travelbook.holder.ReviewHolder;
import com.travelbook.holder.User;
import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class BookReviewAdapter extends BaseAdapter{
	private ImageDownloader downloader;
	private ImageDownloader profileDownloader = new ImageDownloader();
	boolean isdownloading = false;
	private ArrayList<ReviewHolder> downHolder = new ArrayList<ReviewHolder>();
	private ArrayList<String> downurl = new ArrayList<String>();
	private LayoutInflater mInflater;
	private Vector<String> bodyList;
	private Vector<Vector<String>> fileList;
	private HashMap<Integer,Bitmap> bitmapList = new HashMap<Integer, Bitmap>();
	
	private Vector<String> placenameV;
	private HashMap<Integer,String> nameV;
	private Vector<String> timeV;
	private Vector<String> emailV;
	private Vector<Integer> loginTypeV;
	private Vector<String> isVideoV;
	private HashMap<Integer, Integer> likeMap;
	private HashMap<Integer, Integer> commentMap;
	private Vector<String> writerNameV;
	private int loginType;
	
	private boolean isLikeClicked = false;
	
	private Activity context;
	private WindowManager window;
	private String userName;
	private String email;
	private String password;
	private ImageView playBtn;
	private boolean isFirst = false;
	
	//like, comment txt
	private TextView txtLike;
	private TextView txtComment;
	
	private int viewCount = 0;
	
	public BookReviewAdapter(ImageDownloader downloader,WindowManager window,Activity context, Vector bodyList, HashMap bitmapList, Vector<Vector<String>> fileList,String userName,String email,Vector<String> timeV, Vector<String> emailV, Vector<Integer> loginTypeV,Vector<String> isVideoV, HashMap<Integer, Integer> likeMap,HashMap<Integer, Integer> commentMap, int loginType, Vector<String> writerNameV, Vector<String> placenameV) {
		this.downloader = downloader;
		this.window = window;
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.bodyList = bodyList;
		this.bitmapList = bitmapList;
		this.fileList = fileList;
		this.userName = userName;
		this.email = email;
		this.timeV = timeV;
		this.emailV = emailV;
		this.loginTypeV = loginTypeV;
		this.isVideoV = isVideoV;
		this.likeMap = likeMap;
		this.commentMap = commentMap;
		this.loginType = loginType;
		this.writerNameV = writerNameV;
		this.placenameV = placenameV;
		nameV = new HashMap<Integer, String>();
	}

	public void addItem()
	{
		viewCount+=1;
	}
	@Override
	public int getCount() {
		return viewCount;
	}


	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(! isFirst)
			new FindUser().execute(0);
		ReviewHolder holder;
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.book_row, null);
			
			holder = new ReviewHolder(context);
			holder.body = (TextView) convertView.findViewById(R.id.book_row_content);
			holder.image =  (ImageView) convertView.findViewById(R.id.book_row_img);
			holder.imageLine = (ImageView)convertView.findViewById(R.id.book_row_img_line);
			holder.name = (TextView) convertView.findViewById(R.id.book_row_writer);
			holder.profile = (ImageView) convertView.findViewById(R.id.book_row_writer_icon);
			holder.like = (TextView)convertView.findViewById(R.id.book_row_like_txt); 
			holder.comment = (TextView)convertView.findViewById(R.id.book_row_comment_txt);
			holder.time = (TextView)convertView.findViewById(R.id.book_row_time);
			
			holder.setFont();
			
			convertView.setTag(holder);
		}
		
		txtLike = (TextView) convertView.findViewById(R.id.book_row_like_txt);
		txtComment = (TextView) convertView.findViewById(R.id.book_row_comment_txt);
		ImageButton btnLike = (ImageButton)convertView.findViewById(R.id.book_row_like_btn);
		btnLike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					System.out.println("isLikeClicked ::: " + isLikeClicked);
					if(! isLikeClicked)
					{
						isLikeClicked = true;
						new clickLike().execute(position).get();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BookReviewAdapter.this.notifyDataSetChanged();
			}
		});
		
		ImageButton share = (ImageButton) convertView.findViewById(R.id.book_row_share_btn);
		share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("want to scrap my book?");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						new scrapMyBook(emailV.get(position), timeV.get(position)).execute();
					}
				});
				builder.setNegativeButton("NO", null);
				builder.show();
			}
		});
		
		holder = (ReviewHolder) convertView.getTag();
		
		String time = timeV.get(position);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String date = format.format(new Date());
		
		holder.time.setText("");
		
		String textTime;
		if(date.substring(0,8).equals(time.substring(0,8)))
		{
			String nowtime = date.substring(9,15);
			String reviewtime = time.substring(9,15);
			
			int hour = Integer.parseInt(nowtime.substring(0,2));
			int minute = Integer.parseInt(nowtime.substring(2,4));
			
			int reviewhour = Integer.parseInt(reviewtime.substring(0,2));
			int reviewminute = Integer.parseInt(reviewtime.substring(2,4));
			
			int nownow = hour*60 + minute;
			int reviewreview = reviewhour*60 + reviewminute;
			
			int distanceTime = nownow - reviewreview;
			int distancehour = distanceTime/60;
			
			if(distancehour==0)
			{
				textTime = distanceTime%60+" minute ago";
				if(distanceTime%60 == 0)
				{
					textTime = "now";
				}
			}
			else
			{
				textTime = distancehour+ " hour ago";
			}
			
			holder.time.setText(textTime);
		}
		else
		{
			String nowDate = date.substring(0,8);
			String reviewDate = time.substring(0,8);
			
			int nowyear = Integer.parseInt(nowDate.substring(0,4));
			int nowmonth = Integer.parseInt(nowDate.substring(4,6));
			int nowdate = Integer.parseInt(nowDate.substring(6,8));
			
			int reviewyear = Integer.parseInt(reviewDate.substring(0,4));
			int reviewmonth = Integer.parseInt(reviewDate.substring(4,6));
			int reviewdate = Integer.parseInt(reviewDate.substring(6,8));
			
			if(nowyear - reviewyear > 0 )
			{
				textTime = nowyear - reviewyear + " years ago";
			}
			else
			{
				Calendar cal = Calendar.getInstance ( );
				cal.setTime ( new Date() );// 오늘로 설정. 

				Calendar cal2 = Calendar.getInstance ( );
				cal2.set ( Integer.parseInt(time.substring(0, 4)), Integer.parseInt(time.substring(4,6)), Integer.parseInt(time.substring(6,8)) ); // 기준일로 설정. month의 경우 해당월수-1을 해줍니다.
				
				int count = 1;
				while ( !cal2.after ( cal ) )
				{
				count++;
				cal2.add ( Calendar.DATE, 1 ); // 다음날로 바뀜

				}
				textTime = count + "days ago";
			}
			holder.time.setText(textTime);
		}
		
		final String timetext = textTime.toString();
		View.OnClickListener detail_listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context,DetailBookView.class);
				intent.putExtra("body", bodyList.get(position));
				intent.putExtra("filesize", fileList.get(position).size());
				intent.putExtra("writertime",timeV.get(position));
				intent.putExtra("logintype", loginType);
				intent.putExtra("writeremail",emailV.get(position));
				intent.putExtra("isvideo",isVideoV.get(position));
				intent.putExtra("writername", writerNameV.get(position));
				intent.putExtra("username",userName);
				intent.putExtra("email",email);
				intent.putExtra("timetext",timetext);
				intent.putExtra("placename",placenameV.get(position));
				
				System.out.println("Adapter nameV::: "+nameV.get(position));
				System.out.println("Adapter writernameV ::: " + writerNameV.get(position));
				System.out.println("Adapter writerTimeV @@@@@@@@ " + timeV.get(position));
				for(int i=0;i<fileList.get(position).size();i++)
				{
					intent.putExtra("file"+i, fileList.get(position).get(i));
				}
				context.startActivity(intent);
			}
		};
		
		if(likeMap.get(position)!=0){
			holder.like.setVisibility(View.VISIBLE);
			holder.like.setText("·"+likeMap.get(position) + " likes");
		}
		else{
			holder.like.setVisibility(View.GONE);
		}
		
		if(commentMap.get(position)!=0)
		{
			holder.comment.setVisibility(View.VISIBLE);
			holder.comment.setText("·"+commentMap.get(position) + " comments");
		}
		else
		{
			holder.comment.setVisibility(View.INVISIBLE);
		}
		holder.comment.setOnClickListener(detail_listener);
		
		holder.image.setImageBitmap(null);
		holder.image.setVisibility(View.GONE);
		holder.image.setOnClickListener(detail_listener);
		
		holder.imageLine.setVisibility(View.GONE);
		
		playBtn = (ImageView)convertView.findViewById(R.id.book_row_play_img);
		playBtn.setVisibility(View.GONE);
		ProfilePictureView ppv = (ProfilePictureView)convertView.findViewById(R.id.facebook_profile_pic);
		
		OnClickListener otherBookListener = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context,OtherBook.class);
				intent.putExtra("otherEmail", emailV.get(position));
				intent.putExtra("username", userName);
				intent.putExtra("email", email);
				intent.putExtra("password",password);
				intent.putExtra("logintype", loginType);
				context.startActivity(intent);
			}
		};
		holder.profile.setOnClickListener(otherBookListener);
		ppv.setOnClickListener(otherBookListener);
		holder.name.setOnClickListener(otherBookListener);
		
		if(loginTypeV.get(position)==0){
			ppv.setVisibility(View.GONE);
			holder.profile.setVisibility(View.VISIBLE);
			Bitmap profileBitmap = profileDownloader.profileDownload(IpUtil.getIp()+"profile/"+emailV.get(position)+"/profile.jpg");
			if(profileBitmap!=null)
				holder.profile.setImageBitmap(profileBitmap);
			else
				holder.profile.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.noprofile3));
		}else{
			ppv.setProfileId(emailV.get(position));
			ppv.setVisibility(View.VISIBLE);
			holder.profile.setVisibility(View.GONE);
		}
		holder.name.setText("");
		
		if(loginTypeV.get(position)==0){
			if(nameV.get(position)!=null)
				holder.name.setText(nameV.get(position));
		}else{
			holder.name.setText(writerNameV.get(position));
		}
		
		String body = bodyList.get(position);
		body = body.replace("\n", " ");
		
		if((holder.body.getTextSize() * body.length()) / window.getDefaultDisplay().getWidth()>=3)
		{
			String msg = body.substring(0, (int) (window.getDefaultDisplay().getWidth()*2/holder.body.getTextSize()));
			holder.body.setText(msg+"...");
		}
		else
		{
			holder.body.setText(body);
		}
		
		holder.body.setOnClickListener(detail_listener);
		
		if(bitmapList.get(position)!=null)
		{
			holder.image.setImageBitmap(bitmapList.get(position));
			holder.imageLine.setVisibility(View.VISIBLE);
			if(isVideoV.get(position).equals("true"))
			{
				playBtn = (ImageView)convertView.findViewById(R.id.book_row_play_img);
				playBtn.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context,MyVideoPlayer.class);
						intent.putExtra("source", fileList.get(position).get(1));
						context.startActivity(intent);
					}
				});
			}
			LayoutParams lp = holder.image.getLayoutParams();
			lp.width = 500;
			lp.height = 500;
			holder.image.setLayoutParams(lp);
			holder.image.setVisibility(View.VISIBLE);
		}
		
		ImageButton btn_comment = (ImageButton) convertView.findViewById(R.id.book_row_comment_btn);
		btn_comment.setOnClickListener(detail_listener);
		return convertView;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}
	
	class scrapMyBook extends AsyncTask<Void, Void, Void>
	{
		String writerEmail;
		String writeTime;
		public scrapMyBook(String writerEmail, String writeTime)
		{
			this.writerEmail = writerEmail;
			this.writeTime = writeTime;
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();
			String url = IpUtil.getIp() + "scrapReview.do";
			HttpPost post = new HttpPost(url);
			List info = new ArrayList();
			info.add(new BasicNameValuePair("email", email));
			info.add(new BasicNameValuePair("writeremail", writerEmail));
			info.add(new BasicNameValuePair("writetime", writeTime));
			
			String result = null;
			
			try {
				post.setEntity(new UrlEncodedFormEntity(info, HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(entity.getContent()));
				
				while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                 String name=parser.getName();
	                  if ( name != null && name.equals("result"))
	                     result=parser.nextText();
	              }
	             if(result.equals("success"))
	             {
	            	 final AlertDialog.Builder builder = new AlertDialog.Builder(context);
	            	 builder.setMessage("scrap success!");
	            	 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
	            	 context.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								builder.show();
							}});
	             }
	             else if(result.equals("failed"))
	             {
	            	 final AlertDialog.Builder builder = new AlertDialog.Builder(context);
	            	 builder.setMessage("scrap failed!");
	            	 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
	            	 context.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								builder.show();
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
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
	class FindUser extends AsyncTask<Integer, Void, Void>{
		User user;
		int count;
		public FindUser()
		{
			user = new User();
			isFirst = true;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			nameV.put(count,user.getName());
			if(count<emailV.size()-1)
			{
				new FindUser().execute(count+1);
			}
			else
			{
				context.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						BookReviewAdapter.this.notifyDataSetChanged();
					}});
			}
		}

		@Override
		protected Void doInBackground(Integer... arg0) {
			count = arg0[0];
			String result = null;
			
			try {
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp()+"userInfo.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("email",emailV.get(count)));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	            XmlPullParser parser = factory.newPullParser();
	            parser.setInput(new InputStreamReader(entity.getContent()));
	            while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                String name=parser.getName();
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
	
	class clickLike extends AsyncTask<Integer, Void, Integer>
	{
		int position;
		
		@Override
		protected void onPostExecute(Integer result) {
			System.out.println("result ::::::::: "+result);
			if(result==1)
			{
				likeMap.put(position, likeMap.get(position)+1);
			}
			else if(result== -1)
			{
				likeMap.put(position, likeMap.get(position)-1);
			}
			else
			{
				likeMap.put(position, 0);
			}
			isLikeClicked = false;
			context.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					notifyDataSetChanged();
				}});
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			System.out.println("Like click@@@@@@@2");
			position = arg0[0];
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(IpUtil.getIp()+"likeReview.do");
			List info = new ArrayList();
			info.add(new BasicNameValuePair("email", emailV.get(position)));
			info.add(new BasicNameValuePair("time", timeV.get(position)));
			info.add(new BasicNameValuePair("liker_email",email));
			try {
				post.setEntity(new UrlEncodedFormEntity(info));
				HttpResponse response = client.execute(post);
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(response.getEntity().getContent(), "UTF-8");
				
				while(parser.next() != XmlPullParser.END_DOCUMENT){
					String name = parser.getName();
					if(name !=null && name.equals("result")){
						String result = parser.nextText();
						if(result.equals("plus_success"))
						{
							return 1;
						}
						else if(result.equals("minus_success"))
						{
							return -1;
						}
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
