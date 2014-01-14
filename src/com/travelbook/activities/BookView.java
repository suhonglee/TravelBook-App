package com.travelbook.activities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.travelbook.adapter.BookReviewAdapter;
import com.travelbook.adapter.ImageAdapter;
import com.travelbook.cache.ImageDownloader;
import com.travelbook.customview.ImgSlide;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.User;
import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BookView extends Activity implements OnScrollListener, OnClickListener{
	
	//my intent
	private Intent intent;
	
	//start intent
	private Intent imageFlip_intent;
	
	//review List
	private ListView reviewList;
	
	//imgSlide
	private ImgSlide mImgSlide;
	
	//review Adapter
	private BookReviewAdapter mAdapter;
	//lock when loading review(drag up)
	private boolean mLockListView;
	
	
	private Button writeReview;
	
	private Vector<String> bodyList;
	private Vector<String> fileList;
	
	private HashMap<Integer,Bitmap> bitmapList;
	//visible review count
	private int nowViewCount =0;
	private ProgressBar prog;
	
	private Vector<String> bodyV;
	private Vector<Vector<String>> fileV;
	private Vector<String> fileVchild;
	private Vector<String> timeV;
	private Vector<String> emailV;
	private Vector<Integer> loginTypeV;
	private Vector<String> isVideoV;
	private Vector<Bitmap> downloadBitmap;
	private ImageAdapter imageAdapter;
	private HashMap<Integer, Integer> likeMap;
	private HashMap<Integer, Integer> commentMap;
	private Vector<String> writerNameV;
	private Vector<String> placenameV;
	
	private ImageDownloader downloader = new ImageDownloader();
	
	private boolean isExecute = false;
	
	String userName;
	String email;
	private String password;
	int loginType;
	Double latitude;
	Double longitude;
	LinearLayout bookview;
	private MyProgressDialog pd;
	String placename;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.booklayout);
		
		bookview = (LinearLayout) getLayoutInflater().inflate(R.layout.bookview, null);
		//load location info
		intent = getIntent();
		
		email = intent.getStringExtra("email");
		userName = intent.getStringExtra("username");
		
		loginType = intent.getIntExtra("logintype", 0);
		latitude = intent.getDoubleExtra("latitude", 0);
		longitude = intent.getDoubleExtra("longitude",0);
		
		if(loginType==0)
			new FindUser(email).execute();
		
		String title = intent.getStringExtra("title");
		String snippet = intent.getStringExtra("snippet");
		
		imageFlip_intent = new Intent(BookView.this,ImageFlip.class);
		
		TextView bookviewTitle = (TextView) bookview.findViewById(R.id.bookviewTitle);
		if(snippet!=null)
		{
			if(snippet.contains(title))
			{
				placename = snippet;
				bookviewTitle.setText(snippet);
			}
			else
			{
				placename = title+"\r\n"+snippet;
				bookviewTitle.setText(placename);
			}
		}
		else
		{
			bookviewTitle.setText(title);
		}
		
		downloadBitmap = new Vector<Bitmap>();
		
		
		DownloadStreetImage dsi = new DownloadStreetImage(downloadBitmap);
		Thread t1 = new Thread(dsi);
		t1.start();
		
		imageAdapter = new ImageAdapter(this,downloadBitmap);
		
		for(int i=0;i<downloadBitmap.size();i++)
		{
			ImageView img = new ImageView(this);
			img.setImageBitmap(downloadBitmap.get(i));
		}
		
		mImgSlide = (ImgSlide)bookview.findViewById(R.id.bookview_imgslide);
		mImgSlide.setUnselectedAlpha(1.0f);
		mImgSlide.setAdapter(imageAdapter);
		
		//show large size image
		mImgSlide.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				pd = MyProgressDialog.show(BookView.this);
				startActivity(imageFlip_intent);
			}
		});
		
		reviewList = (ListView) findViewById(R.id.reviewList);
		writeReview = (Button)bookview.findViewById(R.id.writeReview_btn);
		
		
		reviewList.addHeaderView(bookview);
		mLockListView = true;
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//temp... will deleted
		bodyV= new Vector<String>();
		fileV = new Vector<Vector<String>>();
		fileVchild = new Vector<String>();
		timeV = new Vector<String>();
		emailV = new Vector<String>();
		loginTypeV = new Vector<Integer>();
		isVideoV = new Vector<String>();
		
		bodyList = new Vector<String>();
		fileList = new Vector<String>();
		bitmapList = new HashMap<Integer, Bitmap>(); 
		
		likeMap = new HashMap<Integer, Integer>();
		commentMap = new HashMap<Integer, Integer>();
		
		writerNameV = new Vector<String>();
		placenameV = new Vector<String>();
		nowViewCount = 0;
		
		mAdapter = new BookReviewAdapter(downloader,getWindowManager(), this, bodyList,bitmapList,fileV, userName, email, timeV, emailV, loginTypeV, isVideoV, likeMap, commentMap, loginType, writerNameV, placenameV);
		prog = new ProgressBar(this);
		reviewList.addFooterView(prog);
		
		reviewList.setAdapter(mAdapter);
		System.out.println("이게뭐지?? : "+mAdapter);
		reviewList.setOnScrollListener(this);
		
		writeReview.setOnClickListener(this);
		
		isExecute = true;
		new ReadReview().execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
		reviewList.removeFooterView(prog);
	}

	class DownloadStreetImage implements Runnable
	{
		Vector<Bitmap> downloadBitmap;
		public DownloadStreetImage(Vector<Bitmap> downloadBitmap)
		{
			this.downloadBitmap = downloadBitmap;
		}
		public void downloadBitmap()
		{
			String url1 = new String("http://maps.googleapis.com/maps/api/streetview?size=500x350&location="+intent.getDoubleExtra("latitude", 0)+","+intent.getDoubleExtra("longitude", 0)+"&heading=0&fov=300&pitch=+20&sensor=false");
			String url2 = new String("http://maps.googleapis.com/maps/api/streetview?size=500x350&location="+intent.getDoubleExtra("latitude", 0)+","+intent.getDoubleExtra("longitude", 0)+"&heading=80&fov=300&pitch=+20&sensor=false");
			String url3 = new String("http://maps.googleapis.com/maps/api/streetview?size=500x350&location="+intent.getDoubleExtra("latitude", 0)+","+intent.getDoubleExtra("longitude", 0)+"&heading=160&fov=300&pitch=+20&sensor=false");
			String url4 = new String("http://maps.googleapis.com/maps/api/streetview?size=500x350&location="+intent.getDoubleExtra("latitude", 0)+","+intent.getDoubleExtra("longitude", 0)+"&heading=240&fov=300&pitch=+20&sensor=false");
			imageFlip_intent.putExtra("url0", url1);
			imageFlip_intent.putExtra("url1", url2);
			imageFlip_intent.putExtra("url2", url3);
			imageFlip_intent.putExtra("url3", url4);
			imageFlip_intent.putExtra("urlsize",4);
			ImageDownloader downloader = new ImageDownloader();
			
			downloadBitmap.add(new SoftReference<Bitmap>(downloader.scaledDownload(url1)).get());
			notifyAdapter();
			downloadBitmap.add(new SoftReference<Bitmap>(downloader.scaledDownload(url2)).get());
			notifyAdapter();
			downloadBitmap.add(new SoftReference<Bitmap>(downloader.scaledDownload(url3)).get());
			notifyAdapter();
			downloadBitmap.add(new SoftReference<Bitmap>(downloader.scaledDownload(url4)).get());
			notifyAdapter();
			
		}
		
		public void notifyAdapter()
		{
			BookView.this.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					imageAdapter.notifyDataSetChanged();
				}});
		}
		@Override
		public void run() {
			downloadBitmap();
		} 
	} 
	
	
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		int count = totalItemCount-visibleItemCount;
		if(firstVisibleItem >= count && totalItemCount != 0 && mLockListView == false && visibleItemCount-1 != nowViewCount+1 && ! isExecute)
		{
			addItems(5);
		}
	}



	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private void addItems(int size){

		mLockListView = true;
		Runnable run;
		
		//when show all review
		if((bodyV.size()-nowViewCount)<size && (bodyV.size() - nowViewCount) != 0)
		{
			ImageDownloader downloader = new ImageDownloader(fileV,bitmapList,mAdapter,BookView.this);
			System.out.println("downloader nowViewCount :: " + nowViewCount);
			downloader.download(nowViewCount,bodyV.size() - nowViewCount);
			final int viewSize = bodyV.size()-nowViewCount;
			run = new Runnable() {
	
				@Override
				public void run() {
					reviewList.removeFooterView(prog);
					if(fileV.size()>nowViewCount)
					{
						for(int i=0;i<viewSize;i++){
							if(bodyV.size()>nowViewCount)
								bodyList.add(bodyV.get(nowViewCount));
							if(fileV.size()>nowViewCount)
								if(fileV.get(nowViewCount)!=null)
									if(fileV.get(nowViewCount).size()>0)
										fileList.add(IpUtil.getIp() + (String)(fileV.get(nowViewCount)).get(0));
							mAdapter.addItem();
							nowViewCount++;
						}
					}
					mAdapter.notifyDataSetChanged();
					mLockListView = false;
				}
				
			};
		}
		else
		{
			ImageDownloader downloader = new ImageDownloader(fileV,bitmapList,mAdapter,BookView.this);
			System.out.println("downloader nowViewCount :: " + nowViewCount);
			downloader.download(nowViewCount,nowViewCount + 5);
			run = new Runnable()
			{

				@Override
				public void run() 
				{
					for(int i=0;i<5;i++)
					{
						System.out.println("북뷰에 왜안띄워져");
						if(fileV.size()>nowViewCount)
						{
							if(bodyV.size()>nowViewCount)
								bodyList.add(bodyV.get(nowViewCount));
							if(fileV.get(nowViewCount)!=null)
								if(fileV.get(nowViewCount).size()>0)
									fileList.add(IpUtil.getIp() + (String)(fileV.get(nowViewCount)).get(0));
							mAdapter.addItem();
							nowViewCount++;
						}
					}
					mAdapter.notifyDataSetChanged();
					mLockListView = false;
				}
			};
		}

		Handler handler = new Handler();
		handler.postDelayed(run, 500);
	}
	
	
	

	
	public void mOnClick(View v)
	{
//		if(v.getId()==R.id.btn_more_review)
//		{
//			Button btn = (Button)v;
//			Intent intent = new Intent(this,DetailBookView.class);
//			int position = Integer.parseInt((String) btn.getHint());
//			intent.putExtra("name", nameList.get(position));
//			intent.putExtra("body", bodyList.get(position));
//			startActivity(intent);
//		}
	}
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.writeReview_btn){
			Intent intent = new Intent(this,WriteReview.class);
			intent.putExtra("placename",placename);
			intent.putExtra("username", userName);
			intent.putExtra("email", email);
			intent.putExtra("logintype", loginType);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			pd = MyProgressDialog.show(BookView.this);
			startActivity(intent);
		}
	}
	
	class ReadReview extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPostExecute(Void result) {
			addItems(5);
			isExecute = false;
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			String url = IpUtil.getIp() + "readReview.do";
			HttpPost post = new HttpPost(url);
			List info = new ArrayList();
			info.add(new BasicNameValuePair("latitude",""+latitude));
			info.add(new BasicNameValuePair("longitude",""+longitude));
			
			String result = null;
			String userName = null;
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					bookview.findViewById(R.id.bookview_nothing).setVisibility(View.GONE);
				}});
			try {
				post.setEntity(new UrlEncodedFormEntity(info, HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				if(response.getStatusLine().getStatusCode()==200)
				{
					HttpEntity entity = response.getEntity();
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					XmlPullParser parser = factory.newPullParser();
					parser.setInput(new InputStreamReader(entity.getContent()));
					
					int count = 0;
					while(parser.next() != XmlPullParser.END_DOCUMENT)
					{
						String name = parser.getName();
						if(name != null && name.equals("reviewcount"))
						{
							int reviewcount = Integer.parseInt(parser.nextText());
						}
						if(name != null && name.equals("email"+count))
						{
							emailV.add(parser.nextText());
						}
						if(name !=null && name.equals("writername"+count))
						{
							writerNameV.add(parser.nextText());
							System.out.println(writerNameV.get(0));
						}
						if(name != null && name.equals("text"+count))
						{
							bodyV.add(parser.nextText());
						}
						if(name != null && name.equals("time"+count))
						{
							timeV.add(parser.nextText());
						}
						if(name != null && name.equals("logintype"+count))
						{
							loginTypeV.add(Integer.parseInt(parser.nextText()));
						}
						if(name != null && name.equals("isvideo"+count))
						{
							isVideoV.add(parser.nextText());
						}
						if(name != null && name.equals("like"+count)){
							likeMap.put(count, Integer.parseInt(parser.nextText()));
						}
						if(name != null && name.equals("comment"+count))
						{
							commentMap.put(count, Integer.parseInt(parser.nextText()));
						}
						if(name != null && name.equals("placename"+count))
						{
							placenameV.add(parser.nextText());
						}
						if(name != null && name.equals("filelist"+count))
						{
							parser.next();
							parser.next();
							int filecount = Integer.parseInt(parser.nextText());
							parser.next();
							fileVchild = new Vector<String>();
							for(int zzz=0;zzz<filecount;zzz++)
							{
								parser.next();
								fileVchild.add(parser.nextText());
								parser.next();
							}
							fileV.add(fileVchild);
							count++;
						}
					}
				}
				else if(response.getStatusLine().getStatusCode()==500)
				{
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							reviewList.removeFooterView(prog);
							bookview.findViewById(R.id.bookview_nothing).setVisibility(View.VISIBLE);
							Typeface tf = Typeface.createFromAsset(BookView.this.getAssets(), "fonts/SeoulHangangM.ttf");
							((TextView)bookview.findViewById(R.id.bookview_nothing_txt)).setTypeface(tf);
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
