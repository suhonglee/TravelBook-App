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

import com.travelbook.activities.BookView.ReadReview;
import com.travelbook.adapter.BookReviewAdapter;
import com.travelbook.adapter.ImageAdapter;
import com.travelbook.cache.ImageDownloader;
import com.travelbook.customview.ImgSlide;
import com.travelbook.customview.MenuListView;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.User;
import com.travelbook.utility.IpUtil;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class Book extends Activity implements AnimationListener,
		OnScrollListener, OnClickListener {

	private ImgSlide imgSlide;
	private Button btnTop;
	private RelativeLayout layout;

	private boolean mLockListView; // =>bookLockListView

	private ListView reviewList; // =>bookReviewList
	private BookReviewAdapter bookReviewAdapter;

	private LayoutInflater mInflater;

	Button slideHanButton;
	SlidingDrawer slidingDrawer;

	ImageAdapter imageAdapter; // =>bookImageAdapter

	private Intent bookFlip_intent;

	private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.ContentSample.menuDrawer";
	private static final String STATE_ACTIVE_POSITION = "net.simonvt.menudrawer.samples.ContentSample.activePosition";
	private static final String STATE_CONTENT_TEXT = "net.simonvt.menudrawer.samples.ContentSample.contentText";

	private MenuDrawerManager mMenuDrawer;

	private MenuAdapter menuAdapter;
	private MenuListView mList;

	private int mActivePosition = -1;
	private String mContentText;
	private TextView mContentTextView;

	private ProgressBar progressBar;
	int preTouchPosX;
	int preTouchPosY;
	//my intent
	private Intent intent;
	
	//start intent
	private Intent imageFlip_intent;
	
	//review List
	
	//imgSlide
	private ImgSlide mImgSlide;
	
	//review Adapter
	private BookReviewAdapter mAdapter;
	//lock when loading review(drag up)
	
	
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
	private Vector<String> placenameV;
	private Vector<Bitmap> downloadBitmap;
	
	private HashMap<Integer, Integer> likeMap;
	private HashMap<Integer, Integer> commentMap;
	
	private Vector<String> writerNameV;
	
	private ImageDownloader downloader = new ImageDownloader();
	
	private boolean isExecute = false;
	
	String userName;
	String email;
	int loginType;
	private String password;
	Double latitude;
	Double longitude;
	
	MyProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		userName = getIntent().getStringExtra("username");
		email = getIntent().getStringExtra("email");
		System.out.println("Book Email :: " + email);
		loginType = getIntent().getIntExtra("logintype",0);
		password = getIntent().getStringExtra("password");
		
		latitude = getIntent().getDoubleExtra("latitude", 0.0);
		longitude = getIntent().getDoubleExtra("longitude",0.0);
		if (savedInstanceState != null) {
			mActivePosition = savedInstanceState.getInt(STATE_ACTIVE_POSITION);
			mContentText = savedInstanceState.getString(STATE_CONTENT_TEXT);
		}

		mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);
		mMenuDrawer.setContentView(R.layout.book);
		
		TextView book_name = (TextView) findViewById(R.id.book_name);
		//here name!
		
		final Button btn_book_menu = (Button) findViewById(R.id.btn_book_menu);
		btn_book_menu.setText(" ");
		btn_book_menu.setHint("close");
		btn_book_menu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(btn_book_menu.getHint().equals("close"))
				{
					mMenuDrawer.openMenu();
					btn_book_menu.setHint("open");
				}
				else
				{
					mMenuDrawer.closeMenu();
					btn_book_menu.setHint("close");
				}
			}
		});
		List<Object> items = new ArrayList<Object>();
		
		items.add(new Item("Map",R.drawable.side_map));
		items.add(new Item("My Book",R.drawable.side_mybook));
		items.add(new Item("My Profile",R.drawable.side_myprofile));
		items.add(new Item("My Scrap",R.drawable.side_myscrap));
		items.add(new Item("Hot place",R.drawable.side_hotplace));

		// A custom ListView is needed so the drawer can be notified when it's
		// scrolled. This is to update the position
		// of the arrow indicator.
		mList = new MenuListView(this);
		
		mList.setBackgroundResource(R.drawable.pagebackground);
		
		menuAdapter = new MenuAdapter(items);
		mList.setAdapter(menuAdapter);
		mList.setOnItemClickListener(mItemClickListener);
		mList.setOnScrollChangedListener(new MenuListView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				mMenuDrawer.getMenuDrawer().invalidate();
			}
		});
		mMenuDrawer.setMenuView(mList);

		mInflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
		LinearLayout img_slide_layout = (LinearLayout) mInflater.inflate(
				R.layout.img_slide, null);



		reviewList = (ListView) findViewById(R.id.listView_book_review);
		reviewList.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					preTouchPosX = (int) event.getX();
					preTouchPosY = (int) event.getY();
				}
				if(event.getAction() == MotionEvent.ACTION_UP){
					int postTouchPosX = (int) event.getX();
					int postTouchPosY = (int) event.getY();
					
					int distanceX = Math.abs(preTouchPosX-postTouchPosX);
					int distanceY = Math.abs(preTouchPosY-postTouchPosY);
					System.out.println("distanceX 가 머임 ; ; "+distanceX);
					System.out.println("distanceY 가 머임 ; ; "+distanceY);
					if(distanceX > 150 && (distanceX - distanceY)>150)
					{
						if(preTouchPosX>postTouchPosX){
							mMenuDrawer.closeMenu();
						}else if(preTouchPosX<postTouchPosX){
							mMenuDrawer.openMenu();
						}
					}
				}
				return false;
			}
		});

		progressBar = new ProgressBar(this);
//		reviewList.addHeaderView(img_slide_layout);

		//
		reviewList.setAdapter(bookReviewAdapter); 
		reviewList.setOnScrollListener(this);
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(loginType==0)
			new FindUser(email).execute();
		
		
		//temp... will deleted
		bodyV= new Vector<String>();
		fileV = new Vector<Vector<String>>();
		fileVchild = new Vector<String>();
		timeV = new Vector<String>();
		emailV = new Vector<String>();
		loginTypeV = new Vector<Integer>();
		isVideoV = new Vector<String>();
		placenameV = new Vector<String>();
		bodyList = new Vector<String>();
		fileList = new Vector<String>();
		bitmapList = new HashMap<Integer, Bitmap>();
		
		likeMap = new HashMap<Integer, Integer>();
		commentMap = new HashMap<Integer, Integer>();
		
		writerNameV = new Vector<String>();
		nowViewCount = 0;
		
		mAdapter = new BookReviewAdapter(downloader,getWindowManager(), this, bodyList,bitmapList,fileV, userName, email, timeV, emailV, loginTypeV, isVideoV, likeMap, commentMap, loginType, writerNameV, placenameV);
		prog = new ProgressBar(this);
		reviewList.addFooterView(prog);
		
		reviewList.setAdapter(mAdapter);
		reviewList.setOnScrollListener(this);
		
		isExecute = true;
		new ReadReview().execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		reviewList.removeFooterView(prog);
		if(pd!=null)
			pd.dismiss();
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
			String url = IpUtil.getIp() + "readNearReview.do";
			HttpPost post = new HttpPost(url);
			List info = new ArrayList();
			info.add(new BasicNameValuePair("latitude",""+latitude));
			info.add(new BasicNameValuePair("longitude",""+longitude));
			
			String result = null;
			String userName = null;
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					findViewById(R.id.book_nothing).setVisibility(View.GONE);
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
						System.out.println("name????"+name);
						if(name != null && name.equals("reviewcount"))
						{
							int reviewcount = Integer.parseInt(parser.nextText());
							System.out.println("리뷰카운트 가져옴 : "+reviewcount);
						}
						if(name != null && name.equals("email"+count))
						{
							emailV.add(parser.nextText());
						}
						if(name != null && name.equals("writername"+count))
						{
							writerNameV.add(parser.nextText());
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
							System.out.println("넥스트결과 : "+ parser.next());
							System.out.println("넥스트결과 : "+ parser.next());
							System.out.println("ㅇㄷ");
							int filecount = Integer.parseInt(parser.nextText());
							System.out.println("ㅇ?");
							System.out.println("넥스트결과 : "+ parser.next());
							System.out.println("ㅇ;");
							fileVchild = new Vector<String>();
							for(int zzz=0;zzz<filecount;zzz++)
							{
								parser.next();
								fileVchild.add(parser.nextText());
								parser.next();
							}
							System.out.println("fileVchild count" + fileVchild.size());
							for(int i=0;i<fileVchild.size();i++)
							{
								System.out.println("fileV for..."+ fileVchild.get(i));
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
							findViewById(R.id.book_nothing).setVisibility(View.VISIBLE);
							Typeface tf = Typeface.createFromAsset(Book.this.getAssets(), "fonts/SeoulHangangM.ttf");
							((TextView)findViewById(R.id.book_nothing_txt)).setTypeface(tf);
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
	private void addItems(int size){

		mLockListView = true;
		Runnable run;
		
		
		//when show all review
		if((bodyV.size()-nowViewCount)<size && (bodyV.size() - nowViewCount) != 0)
		{
			ImageDownloader downloader = new ImageDownloader(fileV,bitmapList,mAdapter,Book.this);
			downloader.download(nowViewCount,bodyV.size() - nowViewCount);
			final int viewSize = bodyV.size()-nowViewCount;
			System.out.println("Book viewSize >::: " + viewSize);
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
		else if(bodyV.size()==nowViewCount)
		{
			reviewList.removeFooterView(prog);
			run = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					
				}};
		}
		else
		{
			ImageDownloader downloader = new ImageDownloader(fileV,bitmapList,mAdapter,Book.this);
			System.out.println("downloader nowViewCount :: " + nowViewCount);
			downloader.download(nowViewCount,nowViewCount + 5);
			System.out.println("Book viewSize >::: 5555555");
			run = new Runnable()
			{

				@Override
				public void run() 
				{
					for(int i=0;i<5;i++)
					{
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
	@Override
	public void onAnimationEnd(Animation animation) {
		Log.i("GalleryViewTest", "onAnimationEnd");
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		int count = totalItemCount-visibleItemCount;
		if(firstVisibleItem >= count && totalItemCount != 0 && mLockListView == false && visibleItemCount-1 != nowViewCount+1 && ! isExecute)
		{
			System.out.println("스크롤됨?;;");
			addItems(5);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	// //////
	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mActivePosition = position;
			mMenuDrawer.setActiveView(view, position);
			mMenuDrawer.closeMenu();
		}
	};

	@Override
	protected void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
		mMenuDrawer.onRestoreDrawerState(inState
				.getParcelable(STATE_MENUDRAWER));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_MENUDRAWER,
				mMenuDrawer.onSaveDrawerState());
		outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
		outState.putString(STATE_CONTENT_TEXT, mContentText);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		final int drawerState = mMenuDrawer.getDrawerState();
		if (drawerState == MenuDrawer.STATE_OPEN
				|| drawerState == MenuDrawer.STATE_OPENING) {
			mMenuDrawer.closeMenu();
			return;
		}

		super.onBackPressed();
	}

	private static class Item {

		String mTitle;
		int mIconRes;
		
		Item(String title, int iconRes) {
			mTitle = title;
			mIconRes = iconRes;
		}
	}

	private class MenuAdapter extends BaseAdapter {

		private List<Object> mItems;

		MenuAdapter(List<Object> items) {
			mItems = items;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position) instanceof Item ? 0 : 1;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean isEnabled(int position) {
			return getItem(position) instanceof Item;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			Object item = getItem(position);

			if (v == null) {
				v = getLayoutInflater().inflate(R.layout.menu_row_item, parent,
						false);
			}
			
			
			TextView tv = (TextView) v;
			tv.setText(((Item) item).mTitle);
			tv.setTextColor(Color.BLACK);
			Bitmap bm = BitmapFactory.decodeResource(getResources(), ((Item)item).mIconRes);
			Drawable dr = new BitmapDrawable(bm);
			tv.setCompoundDrawablesWithIntrinsicBounds(dr, null, null, null);
			
			v.setTag(R.id.mdActiveViewPosition, position);

			if (position == mActivePosition) {
				mMenuDrawer.setActiveView(v, position);
			}
			switch(position)
			{
				case 0:
					v.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							finish();
						}
					});
					break;
				case 1:
					v.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(Book.this,MyBook.class);
							intent.putExtra("username", userName);
							intent.putExtra("email", email);
							intent.putExtra("password",password);
							intent.putExtra("logintype", loginType);
							pd = MyProgressDialog.show(Book.this);
							startActivity(intent);
						}
					});
					break;
				case 2:
					v.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(loginType==0)
							{
								Intent intent = new Intent(Book.this,MyInfo.class);
								intent.putExtra("username", userName);
								intent.putExtra("email", email);
								intent.putExtra("password",password);
								intent.putExtra("logintype", loginType);
								pd = MyProgressDialog.show(Book.this);
								startActivity(intent);
							}
							else if(loginType==1)
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(Book.this);
								builder.setMessage("facebook user only supported logout!");
								builder.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										
										DatabaseHelper dbHelper;
										SQLiteDatabase db;
										dbHelper = new DatabaseHelper(Book.this);
										db = dbHelper.getWritableDatabase();
										String tableName = "personal";
										ContentValues recordValues = new ContentValues();
										recordValues.put("logintype", 123123);
										db.update(tableName, recordValues, "rowid="+1, null);
										pd = MyProgressDialog.show(Book.this);
										startActivity(new Intent(Book.this,Main.class));
										finish();
										
									}
								});
								builder.setNegativeButton("Cancel", null);
								builder.show();
							}
						}
					});
					break;
				case 3:
					v.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(Book.this,MyScrap.class);
							intent.putExtra("username", userName);
							intent.putExtra("email", email);
							intent.putExtra("password",password);
							intent.putExtra("logintype", loginType);
							pd = MyProgressDialog.show(Book.this);
							startActivity(intent);
						}
					});
					break;
				case 4:
					v.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(Book.this,HotPlace.class);
							pd = MyProgressDialog.show(Book.this);
							startActivity(intent);
						}
					});
					break;
			}
			return v;
		}
	}
	private class DatabaseHelper extends SQLiteOpenHelper{
		
		static final int DATABASE_VERSION = 1;
		static final String databaseName = "PersonalDB";
		
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
