package com.travelbook.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.User;
import com.travelbook.utility.FlushedInputstream;
import com.travelbook.utility.IpUtil;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Map extends Activity implements LocationListener{
	MarkerOptions markeroption;
	
	//login user info
	private String userName = null;
	private String email = null;
	private int loginType;
	private String password = null;
	
	private final int inform = 0;
	private final int library = 1;
	private final int police = 2;
	private final int motel = 3;
	private final int food = 4;
	private final int park = 5;
	private final int aquarium = 6;
	private final int art_gallery = 7;
	private final int gym = 8;
	private final int hospital = 9;
	private final int movie_theater = 10;
	private final int night_club = 11;
	private final int amusement_park = 12;
	// nearPlaceSearch keywords
	private String organization[] = {"police","hospital","establishment","library","city_hall","embassy","local_goverment_office"};
	private String stay[] = {"motel","guesthouse"};
	private String restaurant = "restaurant";
	private String tour[] = {"amusement_park","aquarium","art_gallery"};
	private String entertainment[] = {"gym","movie_theater","night_club","park"};
	
	String[] txt_search_keyword = new String[1];
	
	//search gap qualification(before - newLocation gap)
	private Location beforeLocation = new Location("dmdkdkdk");
	private float locationGap;
	
	//map left side menu bar(category)
	private LinearLayout sideMenu;
	
	//animation when show menu
	private Animation sideShow, sideHide;
	
	//category buttons
	private Vector<Button> btns;
	
	//Google Map
	private GoogleMap map;
	private MarkerOptions marker = new MarkerOptions();
	private MarkerOptions clickMarker;
	
	//marker icon
	private Bitmap bitmapOrg;
	private Bitmap resizedBitmap; 
	
	//set MyLocation at least
	private Location myLocation;
	
	//Infowindow intent
	private Intent bookView_intent;
	
	private ProgressDialog dialog;
	
	Bitmap resizedIcon;
	
	//setting search bar
	private EditText searchEditText;
	private Button searchButton;
	private ListView keywordList;
	private boolean searchFlag;
	private static int DATABASE_VERSION = 1;
	private static String databaseName = "PersonalDB";
	private static String tableName = "search_history";
	private SQLiteDatabase db;
	private ArrayList<String> historyArr;
	private HistoryAdapter hisAdapter;
	private Button clearBtn;
	
	private boolean isDetailCome = false;
	
	private HashMap<Integer, SoftReference<Bitmap>> ibtkMap = new HashMap<Integer, SoftReference<Bitmap>>();
	
	private MyProgressDialog pd;
	
	private MyProgressDialog sd;
	private boolean isFirstResume = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		if(getIntent().getIntExtra("isHotPlace",0)==0)
		{
			Toast toast = Toast.makeText(this, "You long-clicked map can be written", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		Button books = (Button) findViewById(R.id.btn_books);
		books.setBackgroundResource(R.drawable.books_pressed);
		books.setHint("true");
		
		email = getIntent().getStringExtra("email");
		loginType = getIntent().getIntExtra("logintype", 0);
		userName = getIntent().getStringExtra("username");
		
		loadButtons();
		dialog = new ProgressDialog(Map.this);
		dialog.setTitle("loading...");
		bookView_intent = new Intent(this,BookView.class);
		beforeLocation.setLatitude(0);
		beforeLocation.setLongitude(0);
		
		//setting search features
		keywordList = (ListView)findViewById(R.id.search_history_list);
		clearBtn = (Button)findViewById(R.id.btn_clear);
		clearBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchEditText.setText("");
				keywordList.setVisibility(View.GONE);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
			}
		});
		searchEditText = (EditText)findViewById(R.id.edit_search_keyword);
		searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		searchEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.length()==0){
					System.out.println("렝스가 0, x버튼 사라짐");
					clearBtn.setVisibility(View.INVISIBLE);
				}else{
					System.out.println("렝스가 1이상, x버튼 생김");
					clearBtn.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		searchEditText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				historyDBSelect();
				hisAdapter = new HistoryAdapter();
				keywordList.setAdapter(hisAdapter);

				if(sideMenu.getVisibility()==View.VISIBLE){
					sideMenu.setVisibility(View.GONE);
				}
				keywordList.setVisibility(View.VISIBLE);
				keywordList.bringToFront();
				if(searchFlag){
					keywordList.setVisibility(View.GONE);
					searchFlag=false;
				}
			}
		});
		
		searchEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_SEARCH){
					String[] keyword = new String[1];
					keyword[0]=searchEditText.getText().toString();
					if(!keyword[0].equals("")){
						if(keywordList.getVisibility() == View.VISIBLE){
							keywordSearch(keyword);
							searchFlag=true;
						}
					}else{
						//if dose not have searchKeyword
						Toast.makeText(Map.this, "Please input search Keyword", Toast.LENGTH_SHORT).show();
					}
				}
				return false;
			}
		});
		searchButton = (Button)findViewById(R.id.btn_search_keyword);
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchEditText = (EditText)findViewById(R.id.edit_search_keyword);
				txt_search_keyword[0]=searchEditText.getText().toString();
				if(!txt_search_keyword[0].equals("")){
					keywordSearch(txt_search_keyword);
				}else{
					//if dose not have searchKeyword
					Toast.makeText(Map.this, "Please input search Keyword", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		keywordList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String[] keyword = new String[1];
				TextView txt = (TextView) ((LinearLayout)view).getChildAt(0);
				keyword[0]=txt.getText().toString();
				if(position==0 && keyword[0].startsWith("remove keywords..")){
					db.execSQL("DROP TABLE IF EXISTS '"+tableName+"'");
					db.execSQL("CREATE TABLE search_history (_id integer primary key autoincrement, keyword text);");
					searchEditText.setText("");
					historyDBSelect();
					hisAdapter.notifyDataSetChanged();
				}else if(position==0 && keyword[0].startsWith("there are no")){
					
				}else{
					keywordSearch(keyword);
					searchEditText.setText(keyword[0]);
				}
			}
		});
		
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mark);
		int width = icon.getWidth(); 
        int height = icon.getHeight(); 
        int newWidth = 70; 
        int newHeight = 70; 
        
        // calculate the scale - in this case = 0.4f 
        float scaleWidth = ((float) newWidth) / width; 
        float scaleHeight = ((float) newHeight) / height; 
        
        // create a matrix for the manipulation 
        Matrix matrix = new Matrix();
        // resize the bit map 
        matrix.postScale(scaleWidth, scaleHeight); 
        // rotate the Bitmap 
        matrix.postRotate(0); 
        
        // recreate the new Bitmap 
        resizedIcon = Bitmap.createBitmap(icon, 0, 0, 
                          width, height, matrix, true);
		
	}
	
	@Override
	public void onBackPressed() {
		if(! isDetailCome)
		{
			if(sideMenu.getVisibility()==View.VISIBLE){
				sideMenu.setVisibility(View.GONE);
				sideMenu.startAnimation(sideHide);
			}
			else if(keywordList.getVisibility()==View.VISIBLE){
				keywordList.setVisibility(View.GONE);
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Want to Exit?");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						finish();
					}
				});
				builder.setNegativeButton("NO", null);
				builder.show();
			}
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		setMyLocation();
		
		setMap();
		
		clickMarker = new MarkerOptions();
		
		if(dialog!=null)
		{
			dialog.dismiss();
		}
		
		setUX();
		if(loginType==0)
			new FindUser(email).execute();
		
		if(getIntent().getIntExtra("isHotPlace",0)==1)
		{
			isDetailCome = true;
			markeroption = new MarkerOptions();
			markeroption.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark));
			Double latitude = getIntent().getDoubleExtra("latitude", 0);
			Double longitude = getIntent().getDoubleExtra("longitude", 0);
			
			System.out.println("latitude @@@@ map ::::" + latitude);
			System.out.println("longitude @@@@ map :: " + longitude);
			final LatLng ll = new LatLng(latitude, longitude);
			markeroption.position(ll);
			String placename = getIntent().getStringExtra("placename");
			markeroption.title(placename);
			
			Button books = (Button) findViewById(R.id.btn_books);
			books.setBackgroundResource(R.drawable.books_normal);
			books.setHint("false");
			
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					if(map!=null)
					{
						map.addMarker(markeroption);
						map.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 13));
					}
				}});
		}
		new bookSearch().execute();
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
		System.gc();
	}


	@Override
	protected void onStop() {
		super.onStop();
		map=null;
		
		System.gc();
	}
	
	public void setMyLocation()
	{
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		//setting require low
		criteria.setAccuracy(Criteria.NO_REQUIREMENT);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		String provider = locationManager.getBestProvider(criteria, true);
		
		//refresh 300s
		locationManager.requestLocationUpdates(provider, 300000, 0, this);
		
		//load lastLocation
		if(provider!=null)
		{
			myLocation = locationManager.getLastKnownLocation(provider);
		}
		
	}
	
	public void setUX()
	{
		setLayout();
	}
	
	public void setLayout()
	{
		sideMenu = (LinearLayout)findViewById(R.id.side_menu);
		
		sideShow = AnimationUtils.loadAnimation(this, R.anim.left_in);
		sideHide = AnimationUtils.loadAnimation(this, R.anim.left_out);
		
		
		LinearLayout menu_bar = (LinearLayout) findViewById(R.id.menu_bar);
		menu_bar.bringToFront();
		
		loadButtons();	
	
		Button btn = (Button)findViewById(R.id.btn_category);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sideMenu.bringToFront();
				if(sideMenu.getVisibility()==View.VISIBLE){
					sideMenu.setVisibility(View.GONE);
					sideMenu.startAnimation(sideHide);
				}else{
					if(keywordList.getVisibility()==View.VISIBLE){
						keywordList.setVisibility(View.GONE);
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
					}
					sideMenu.setVisibility(View.VISIBLE);
					sideMenu.startAnimation(sideShow);
				}
			}
		});
	}
		
	class ibtkDownloader extends AsyncTask<Void,Void, Bitmap>
	{
		int key;
		public ibtkDownloader(int key)
		{
			this.key = key;
		}
		@Override
		protected Bitmap doInBackground(Void... params) {
			if(ibtkMap.get(key) == null || ibtkMap.get(key).get()== null)
			{
			try {
				StringBuilder builder = new StringBuilder();
				URL url = new URL("http://api.ibtk.kr/openapi/publicAssistanceFigurecoverDetail_api.do?picseqno="+key);
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String inputLine;
				while((inputLine = in.readLine()) != null)
				{
					builder.append(inputLine);
				}
				in.close();
				JSONObject obj = new JSONObject(builder.toString());
				JSONArray ob = obj.getJSONArray("ksp_list");
				obj = ob.getJSONObject(0);
				String fileName = obj.getString("fileName");
				BitmapFactory.Options option = new BitmapFactory.Options();
				option.inSampleSize=1;
				option.inPurgeable=true;
				option.inDither = true;
				
				bitmapOrg = BitmapFactory.decodeStream(new FlushedInputstream(new URL(fileName).openStream()), null, option);
				int width = bitmapOrg.getWidth(); 
		        int height = bitmapOrg.getHeight(); 
		        int newWidth = 40; 
		        int newHeight = 40; 
		        
		        // calculate the scale - in this case = 0.4f 
		        float scaleWidth = ((float) newWidth) / width; 
		        float scaleHeight = ((float) newHeight) / height; 
		        
		        // create a matrix for the manipulation 
		        Matrix matrix = new Matrix();
		        // resize the bit map 
		        matrix.postScale(scaleWidth, scaleHeight); 
		        // rotate the Bitmap 
		        matrix.postRotate(0); 
		        
		        // recreate the new Bitmap 
		        resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, 
		                          width, height, matrix, true);
//		        bitmapOrg.recycle();
		        ibtkMap.put(key, new SoftReference<Bitmap>(resizedBitmap));
				return resizedBitmap;
//				return bitmapOrg;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			return ibtkMap.get(key).get();
		}
		
	}
	
	public Bitmap scaledMarkerImage(int key)
	{
		String url = null;
			
		Bitmap bitmap = null;
		
		try {
			switch(key)
			{
				case police:
					bitmap = new ibtkDownloader(6).execute().get();
					break;
				case inform:
					bitmap = new ibtkDownloader(101).execute().get();
					break;
				case library:
					bitmap = new ibtkDownloader(125).execute().get();
					break;
				case motel:
					bitmap = new ibtkDownloader(138).execute().get();
					break;
				case food:
					bitmap = new ibtkDownloader(41).execute().get();
					break;
				case park:
					bitmap = new ibtkDownloader(144).execute().get();
					break;
				case aquarium:
					bitmap = new ibtkDownloader(150).execute().get();
					break;
				case art_gallery:
					bitmap = new ibtkDownloader(93).execute().get();
					break;
				case gym:
					bitmap = new ibtkDownloader(170).execute().get();
					break;
				case hospital:
					bitmap = new ibtkDownloader(216).execute().get();
					break;
				case movie_theater:
					bitmap = new ibtkDownloader(92).execute().get();
					break;
				case night_club:
					bitmap = new ibtkDownloader(163).execute().get();
					break;
				case amusement_park:
					bitmap = new ibtkDownloader(147).execute().get();
					break;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
        return bitmap;
	}
	public void setMap()
	{
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.getUiSettings().setRotateGesturesEnabled(true);
		map.getUiSettings().setScrollGesturesEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.getUiSettings().setZoomGesturesEnabled(true);
		
		//set marker icon (now, temp resizeBitmap(food50))
		marker.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
		
		//if can't find user location, default setting 37.49449, 127.027488(bit computer building)
		LatLng tempLL = new LatLng(37.49449, 127.027488);
		if(myLocation != null)
		{
			if(myLocation.getLatitude()!=0&&myLocation.getLongitude()!=0)
			{
				tempLL = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
			}
		}
		
		if(! isFirstResume)
		{
			//set zoomlevel and animate to position
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(tempLL, 13);
			map.animateCamera(cameraUpdate);
			isFirstResume = true;
		}
		
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			@Override
			public View getInfoWindow(Marker marker) {
				// TODO Auto-generated method stub
				return null;
			}
			
			//set infowindow layout
			@Override
			public View getInfoContents(Marker marker) {
				LayoutInflater inf = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inf.inflate(R.layout.infowindow, null);
				LinearLayout info = (LinearLayout)view.findViewById(R.id.infowindow_Layout);
				LinearLayout info_inner=(LinearLayout)info.getChildAt(1);
				TextView title = (TextView)info_inner.getChildAt(0);
				TextView dec = (TextView)info_inner.getChildAt(1);
				title.setText(marker.getTitle());
				dec.setText(marker.getSnippet());
				return info;
			}
		});
		
		//show detail info when click marker
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if(((Button)findViewById(R.id.btn_books)).getHint().equals("true"))
				{
					bookView_intent = new Intent(Map.this,Book.class);
					bookView_intent.putExtra("title", marker.getTitle());
					bookView_intent.putExtra("snippet",marker.getSnippet());
					bookView_intent.putExtra("username", userName);
					bookView_intent.putExtra("email", email);
					bookView_intent.putExtra("password",password);
					bookView_intent.putExtra("logintype", loginType);
					bookView_intent.putExtra("latitude", marker.getPosition().latitude);
					bookView_intent.putExtra("longitude", marker.getPosition().longitude);
					startActivity(bookView_intent);
					return true;
				}
				return false;
			}
		});
		
		//start bookview activity when infowindow clicked
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				bookView_intent = new Intent(Map.this,BookView.class);
				bookView_intent.putExtra("title", marker.getTitle());
				bookView_intent.putExtra("snippet",marker.getSnippet());
				bookView_intent.putExtra("username", userName);
				bookView_intent.putExtra("email", email);
				bookView_intent.putExtra("password",password);
				bookView_intent.putExtra("logintype", loginType);
				bookView_intent.putExtra("latitude", marker.getPosition().latitude);
				bookView_intent.putExtra("longitude", marker.getPosition().longitude);
				pd = MyProgressDialog.show(Map.this);
				startActivity(bookView_intent);
			}
		});
		
		map.setOnMapLongClickListener(new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(final LatLng point) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
				builder.setMessage("want to write review?");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Map.this,WriteReview.class);
						intent.putExtra("username", userName);
						intent.putExtra("email", email);
						intent.putExtra("logintype", loginType);
						intent.putExtra("latitude", point.latitude);
						intent.putExtra("longitude", point.longitude);
						Geocoder coder = new Geocoder(Map.this);
						String placename = null;
						List<Address> add;
						try {
							add = coder.getFromLocation(point.latitude, point.longitude, 1);
							if(add.size()!=0)
								placename = add.get(0).getAddressLine(0);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(placename==null || placename.equals(""))
						{
							placename="Unknown Place";
						}
						intent.putExtra("placename",placename);
						pd = MyProgressDialog.show(Map.this);
						startActivity(intent);
					}
				});
				builder.setNegativeButton("NO", null);
				builder.show();
			}
		});
		
		//close All menu
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				if(sideMenu!=null)
				{
					sideMenu.setVisibility(View.GONE);
				}
			}
		});
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition position) 
			{
				LatLng target = position.target;
				Location afterLoc = new Location("afterdfdf");
				afterLoc.setLatitude(target.latitude);
				afterLoc.setLongitude(target.longitude);
				
				locationGap += beforeLocation.distanceTo(afterLoc);
				//nearPlaceSearch when move distance>3000
				if(locationGap>15000)
				{
					locationGap=0;
					beforeLocation = afterLoc;
					if(btns.get(0).getHint().equals("true"))
					{
					}
					if(btns.get(1).getHint().equals("true"))
					{
						String[] keyword = organization;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), target.latitude, target.longitude, 0, keyword, 0).execute();
					}
					if(btns.get(2).getHint().equals("true"))
					{
						String keyword[] = stay;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), target.latitude, target.longitude, 0, keyword, 0).execute();
					}
					if(btns.get(3).getHint().equals("true"))
					{
						String keyword = "restaurant";
						sd = MyProgressDialog.show(Map.this, true);
						new NearRestaurantSearch(new StringBuilder(), target.latitude, target.longitude, 0, keyword, 0).execute();
					}
					if(btns.get(4).getHint().equals("true"))
					{
						String keyword[] = tour;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), target.latitude, target.longitude, 0, keyword, 0).execute();
					}
					if(btns.get(5).getHint().equals("true"))
					{
						String keyword[] = entertainment;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), target.latitude, target.longitude, 0, keyword, 0).execute();
					}
				}
			}
		});
	}
	
	public void onLocationChanged(Location location) {
		if(location!=null)
		{
			myLocation = location;
		}
	}
	
	//parsing nearPlaceSearch result
	public boolean addSearchMarker(StringBuilder responseBuilder,int markerKeyword)
	{
		JSONObject json;
		try {
			json = new JSONObject(responseBuilder.toString());
			json = json.getJSONObject("responseData");
			JSONArray jArray = json.getJSONArray("results");
			for(int i=0;i<jArray.length();i++)
			{
				JSONObject obj = jArray.getJSONObject(i);
					String title = obj.getString("titleNoFormatting");
					Double lat = obj.getDouble("lat");
					Double lng = obj.getDouble("lng");
					String number = null;
					String address = obj.getString("streetAddress");
				if(obj.getJSONArray("phoneNumbers")!=null)
				{
					JSONArray JarrObj = obj.getJSONArray("phoneNumbers");
					if(JarrObj!=null)
					{
						JSONObject obj2 = JarrObj.getJSONObject(0);
						if(obj2!=null)
						{
							number = obj2.getString("number");
						}
					}
				}
				marker = new MarkerOptions();
				
				marker.icon(BitmapDescriptorFactory.fromBitmap(scaledMarkerImage(markerKeyword)));
				marker.title(title);
				LatLng ll = new LatLng(lat, lng);
				marker.position(ll);
				marker.snippet(address);
				if(number!=null)
				{
					marker.snippet(number);
				}
				if(map!=null)
				{
					map.addMarker(marker);
				}
			}
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	class bookSearch extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();
			String url = IpUtil.getIp() + "bookSearch.do";
			HttpPost post = new HttpPost(url);
			HttpResponse response;
			try {
				response = client.execute(post);
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(response.getEntity().getContent()));
				
				int count =0;
				
				Double latitude = null;
				Double longitude = null;
				String address;
				while(parser.next() != XmlPullParser.END_DOCUMENT)
				{
					String name = parser.getName();
					System.out.println("parser name :: " +name);
					if(name!=null && name.equals("latitude"+count))
					{
						latitude = Double.parseDouble(parser.nextText());
						System.out.println("latitude :: " + latitude);
					}
					if(name!=null && name.equals("longitude"+count))
					{
						longitude = Double.parseDouble(parser.nextText());
						System.out.println("longitude :: " + longitude);
					}
					if(name!=null && name.equals("address"+count))
					{
						address = parser.nextText();
						LatLng ll = new LatLng(latitude, longitude);
						final MarkerOptions option = new MarkerOptions();
						
						option.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
						option.position(ll);
						option.title(address);
						option.snippet("");
						runOnUiThread(new Runnable(){

							@Override
							public void run() {
								if(map!=null)
									if(((Button)findViewById(R.id.btn_books)).getHint().equals("true"))
										map.addMarker(option);
							}});
						count++;
					}
				}
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
	class NearPlaceSearch extends AsyncTask<Void, Void, Void>
	{
		StringBuilder responseBuilder;
		double latitude;
		double longitude;
		String[] keywords;
		int i;
		int cnt;
		public NearPlaceSearch(StringBuilder responseBuilder, double latitude, double longitude,int i,String[] keywords,int cnt)
		{
			this.responseBuilder = responseBuilder;
			this.latitude = latitude;
			this.longitude = longitude;
			this.keywords = keywords;
			this.i=i;
			this.cnt=cnt;
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if(i==0)
				{
				}
				//Google Search API request
				URL url = new URL("http://ajax.googleapis.com/ajax/services/search/local?v=1.0"
				+"&q="+ URLEncoder.encode(keywords[i], "UTF-8")
						+ "&sll="+latitude+","+longitude
						+ "&hl=en" + "&rsz=4" + "&start="+ (cnt*8));
				//responseBuilder append result
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			    String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			    	responseBuilder.append(inputLine);
			    }
			    in.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			int key = 0;
			if(keywords[i].equals("city_hall") || keywords[i].equals("embassy") || keywords[i].equals("establishment")
					|| keywords[i].equals("local_goverment_office"))
			{
				key = inform;
			}
			else if(keywords[i].equals("police"))
			{
				key = police;
			}
			else if(keywords[i].equals("library"))
			{
				key = library;
			}
			else if(keywords[i].equals("motel")||keywords[i].equals("guesthouse"))
			{
				key = motel;
			}
			else if(keywords[i].equals("amusement_park"))
			{
				key = amusement_park;
			}
			else if(keywords[i].equals("aquarium"))
			{
				key = aquarium;
			}
			else if(keywords[i].equals("art_gallery"))
			{
				key = art_gallery;
			}
			else if(keywords[i].equals("gym"))
			{
				key = gym;
			}
			else if(keywords[i].equals("hospital"))
			{
				key = hospital;
			}
			else if(keywords[i].equals("movie_theater"))
			{
				key = movie_theater;
			}
			else if(keywords[i].equals("night_club"))
			{
				key = night_club;
			}
			else if(keywords[i].equals("park"))
			{
				key = park;
			}
			addSearchMarker(responseBuilder,key);
			if(cnt<4)
			{
				new NearPlaceSearch(new StringBuilder(),latitude,longitude,i,keywords,cnt+1);
			}
			if(i < keywords.length-1)
			{
				new NearPlaceSearch(new StringBuilder(),latitude,longitude,i+1,keywords,0).execute();
			}
			else
			{
				if(sd!=null)
				{
					sd.dismiss();
				}
			}
			super.onPostExecute(result);
		}
		
		
		
	}
	class NearRestaurantSearch extends AsyncTask<Void, Void, Void>
	{
		StringBuilder responseBuilder;
		double latitude;
		double longitude;
		int i;
		String keyword;
		int cnt;
		public NearRestaurantSearch(StringBuilder responseBuilder,double latitude, double longitude,int i,String keyword,int cnt) {
			super();
			this.responseBuilder = responseBuilder;
			this.latitude = latitude;
			this.longitude = longitude;
			this.i=i;
			this.keyword = keyword;
			this.cnt = cnt;
		}
		@Override
		protected Void doInBackground(Void... params) {
			nearPlaceSearch();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			if(addSearchMarker(responseBuilder,food))
			{
				new NearRestaurantSearch(new StringBuilder(),latitude,longitude,i+1,keyword,cnt+1).execute();
			}
			if(sd!=null)
			{
				sd.dismiss();
			}
			super.onPostExecute(result);
		}
		public void nearPlaceSearch()
		{
			try {
				//Google Search API request
				URL url = new URL("http://ajax.googleapis.com/ajax/services/search/local?v=1.0"
				+"&q="+ URLEncoder.encode(keyword, "UTF-8")
						+ "&sll="+latitude+","+longitude
						+ "&hl=en" + "&rsz=4" + "&start="+ (i*8));
				//responseBuilder append result
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			    String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			    	responseBuilder.append(inputLine);
			    }
			    in.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void categoryClick(View v){
		String keyword[];
		Button btn_books = (Button)findViewById(R.id.btn_books);
		Button btn_organization = (Button) findViewById(R.id.btn_organization);
		Button btn_stay = (Button)findViewById(R.id.btn_stay);
		Button btn_restaurant = (Button)findViewById(R.id.btn_restaurant);
		Button btn_tour = (Button)findViewById(R.id.btn_tour);
		Button btn_entertainment = (Button)findViewById(R.id.btn_ent);
			switch (v.getId()) {
				case R.id.btn_books:
					if(map!=null)
					{
						map.clear();
					}
					if(((Button)v).getHint().equals("true")){
						v.setBackgroundResource(R.drawable.books_normal);
						((Button)v).setHint("false");
						break;
					}
					else
					{
						new bookSearch().execute();
						//sideMenu.setVisibility(View.GONE);
						v.setBackgroundResource(R.drawable.books_pressed);
						((Button)v).setHint("true");
						
						btn_organization.setBackgroundResource(R.drawable.organization_normal);
						btn_stay.setBackgroundResource(R.drawable.stay_normal);
						btn_restaurant.setBackgroundResource(R.drawable.restaurant_normal);
						btn_tour.setBackgroundResource(R.drawable.tour_normal);
						btn_entertainment.setBackgroundResource(R.drawable.entertainment_normal);
						
						btn_organization.setHint("false");
						btn_stay.setHint("false");
						btn_restaurant.setHint("false");
						btn_tour.setHint("false");
						btn_entertainment.setHint("false");
						break;
					}
				case R.id.btn_organization:
					if(map!=null) map.clear();
					if(((Button)v).getHint().equals("true")){
						v.setBackgroundResource(R.drawable.organization_normal);
						((Button)v).setHint("false");
						break;
					}
					else
					{
						//sideMenu.setVisibility(View.GONE);
						keyword = organization;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 0, keyword,0).execute();
						v.setBackgroundResource(R.drawable.organization_pressed);
						((Button)v).setHint("true");
						btn_books.setBackgroundResource(R.drawable.books_normal);
						btn_stay.setBackgroundResource(R.drawable.stay_normal);
						btn_restaurant.setBackgroundResource(R.drawable.restaurant_normal);
						btn_tour.setBackgroundResource(R.drawable.tour_normal);
						btn_entertainment.setBackgroundResource(R.drawable.entertainment_normal);
						
						btn_books.setHint("false");
						btn_stay.setHint("false");
						btn_restaurant.setHint("false");
						btn_tour.setHint("false");
						btn_entertainment.setHint("false");
						break;
					}
				case R.id.btn_stay:
					if(map!=null) map.clear();
					if(((Button)v).getHint().equals("true")){
						v.setBackgroundResource(R.drawable.stay_normal);
						((Button)v).setHint("false");
						break;
					}
					else
					{
						//sideMenu.setVisibility(View.GONE);
						keyword = stay;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 0, keyword,0).execute();
						v.setBackgroundResource(R.drawable.stay_pressed);
						((Button)v).setHint("true");
						
						btn_books.setBackgroundResource(R.drawable.books_normal);
						btn_organization.setBackgroundResource(R.drawable.organization_normal);
						btn_restaurant.setBackgroundResource(R.drawable.restaurant_normal);
						btn_tour.setBackgroundResource(R.drawable.tour_normal);
						btn_entertainment.setBackgroundResource(R.drawable.entertainment_normal);
						
						btn_books.setHint("false");
						btn_organization.setHint("false");
						btn_restaurant.setHint("false");
						btn_tour.setHint("false");
						btn_entertainment.setHint("false");
						break;
					}
				case R.id.btn_restaurant:
					if(map!=null) map.clear();
					if(((Button)v).getHint().equals("true")){
						v.setBackgroundResource(R.drawable.restaurant_normal);
						((Button)v).setHint("false");
						break;
					}
					else
					{
						//sideMenu.setVisibility(View.GONE);
						sd = MyProgressDialog.show(Map.this, true);
						new NearRestaurantSearch(new StringBuilder(), map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 0, restaurant,0).execute();
						v.setBackgroundResource(R.drawable.restaurant_pressed);
						((Button)v).setHint("true");
						btn_books.setBackgroundResource(R.drawable.books_normal);
						btn_stay.setBackgroundResource(R.drawable.stay_normal);
						btn_organization.setBackgroundResource(R.drawable.organization_normal);
						btn_tour.setBackgroundResource(R.drawable.tour_normal);
						btn_entertainment.setBackgroundResource(R.drawable.entertainment_normal);
						
						btn_books.setHint("false");
						btn_stay.setHint("false");
						btn_organization.setHint("false");
						btn_tour.setHint("false");
						btn_entertainment.setHint("false");
						break;
					}
				case R.id.btn_tour:
					if(map!=null) map.clear();
					if(((Button)v).getHint().equals("true")){
						v.setBackgroundResource(R.drawable.tour_normal);
						((Button)v).setHint("false");
						break;
					}
					else
					{
						//sideMenu.setVisibility(View.GONE);
						keyword = tour;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 0, keyword,0).execute();
						v.setBackgroundResource(R.drawable.tour_pressed);
						((Button)v).setHint("true");
						
						btn_books.setBackgroundResource(R.drawable.books_normal);
						btn_stay.setBackgroundResource(R.drawable.stay_normal);
						btn_organization.setBackgroundResource(R.drawable.organization_normal);
						btn_restaurant.setBackgroundResource(R.drawable.restaurant_normal);
						btn_entertainment.setBackgroundResource(R.drawable.entertainment_normal);
						
						btn_books.setHint("false");
						btn_stay.setHint("false");
						btn_organization.setHint("false");
						btn_restaurant.setHint("false");
						btn_entertainment.setHint("false");
						break;
					}
				case R.id.btn_ent:
					if(map!=null) map.clear();
					if(((Button)v).getHint().equals("true")){
						v.setBackgroundResource(R.drawable.entertainment_normal);
						((Button)v).setHint("false");
						break;
					}
					else
					{
						//sideMenu.setVisibility(View.GONE);
						keyword = entertainment;
						sd = MyProgressDialog.show(Map.this, true);
						new NearPlaceSearch(new StringBuilder(), map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 0, keyword,0).execute();
						v.setBackgroundResource(R.drawable.entertainment_pressed);
						((Button)v).setHint("true");
						
						btn_books.setBackgroundResource(R.drawable.books_normal);
						btn_stay.setBackgroundResource(R.drawable.stay_normal);
						btn_organization.setBackgroundResource(R.drawable.organization_normal);
						btn_restaurant.setBackgroundResource(R.drawable.restaurant_normal);
						btn_tour.setBackgroundResource(R.drawable.tour_normal);
						
						btn_books.setHint("false");
						btn_stay.setHint("false");
						btn_organization.setHint("false");
						btn_restaurant.setHint("false");
						btn_tour.setHint("false");
						break;
					}

				default:
					break;
				}
	}
	
	public void loadButtons(){
		btns = new Vector<Button>();
		btns.add((Button)findViewById(R.id.btn_books));
		btns.add((Button)findViewById(R.id.btn_organization));
		btns.add((Button)findViewById(R.id.btn_stay));
		btns.add((Button)findViewById(R.id.btn_restaurant));
		btns.add((Button)findViewById(R.id.btn_tour));
		btns.add((Button)findViewById(R.id.btn_ent));
	}
	public void viewHotPlace(View v)
	{
		Intent intent = new Intent(Map.this,HotPlace.class);
		pd = MyProgressDialog.show(Map.this);
		startActivity(intent);
	}
	
	public void viewMyBook(View v)
	{
		Intent intent = new Intent(Map.this,MyBook.class);
		intent.putExtra("username", userName);
		intent.putExtra("email",email);
		intent.putExtra("password",password);
		intent.putExtra("logintype",loginType);
		pd = MyProgressDialog.show(Map.this);
		startActivity(intent);
	}
	public void viewProfile(View v)
	{
		if(loginType==0)
		{
			Intent intent = new Intent(Map.this,MyInfo.class);
			intent.putExtra("username", userName);
			intent.putExtra("email",email);
			intent.putExtra("password",password);
			intent.putExtra("logintype",loginType);
			pd = MyProgressDialog.show(Map.this);
			startActivity(intent);
		}
		else if(loginType==1)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("facebook user only supported logout!");
			builder.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					DatabaseHelper dbHelper;
					SQLiteDatabase db;
					dbHelper = new DatabaseHelper(Map.this);
					db = dbHelper.getWritableDatabase();
					String tableName = "personal";
					ContentValues recordValues = new ContentValues();
					recordValues.put("logintype", 123123);
					db.update(tableName, recordValues, "rowid="+1, null);
					pd = MyProgressDialog.show(Map.this);
					Intent intent = new Intent(Map.this,Main.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			});
			builder.setNegativeButton("Cancel", null);
			builder.show();
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
	
	public void keywordSearch(String[] keyw){
		
		if(map!=null){
			map.clear();
			LatLng position = map.getCameraPosition().target; 
			if(myLocation==null){
			}else{
				Button btn_books = (Button)findViewById(R.id.btn_books);
				btn_books.setBackgroundResource(R.drawable.books_normal);
				btn_books.setHint("false");
				new NearPlaceSearch(new StringBuilder(), position.latitude, position.longitude, 0, keyw,0).execute();
			}
		}
		System.out.println("keyword[0]이머임..."+keyw[0]);
		db = openOrCreateDatabase(databaseName, MODE_WORLD_WRITEABLE, null);
		db.execSQL("insert into "+tableName+" (_id, keyword) values (null, '"+keyw[0]+"');");
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
		keywordList.setVisibility(View.GONE);

	}
	class HistoryAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return historyArr.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater li = Map.this.getLayoutInflater();
			LinearLayout ll = (LinearLayout) li.inflate(R.layout.keyword_list_item, null);
			TextView text = (TextView) ll.getChildAt(0);
			text.setText(historyArr.get(position));
			return ll;
		}
	}
	
	public void historyDBSelect(){
		historyArr = new ArrayList<String>();
		db = openOrCreateDatabase(databaseName, MODE_WORLD_WRITEABLE, null);
		Cursor cursor = db.rawQuery("select _id, keyword from "+tableName+" order by _id desc", null);
		historyArr.add("remove keywords..("+cursor.getCount()+")");
		while(cursor.moveToNext()){
			historyArr.add(cursor.getString(1));
		}
		if(historyArr.size()==1){
			historyArr.remove(0);
			historyArr.add("there are no history..");
		}
		
	}
}
	
