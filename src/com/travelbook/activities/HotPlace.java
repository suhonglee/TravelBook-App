package com.travelbook.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObject;

import com.travelbook.cache.ImageDownloader;
import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.FlagToString;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class HotPlace extends Activity{
	
	private ImageButton favoriteBtn;	//찜하기버튼
	private boolean favoriteFlag=false;
	private ImageButton placeMarkerBtn;	//위치보기 버튼
	int cccc =0;
	//ViewFlipper
	private ViewFlipper m_viewFlipper;
	private int m_nPreTouchPosX = 0;
	
	ArrayList<Integer> coll = new ArrayList<Integer>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		for(int i=0;i<197;i++)
		{
			coll.add(i);
		}
		Collections.shuffle(coll);
		
		setContentView(R.layout.flipper_layout);
		
		m_viewFlipper = (ViewFlipper) findViewById(R.id.m_viewFlipper);
		m_viewFlipper.setOnTouchListener(MyTouchListener);
		
		LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout hotplace = (LinearLayout) inf.inflate(R.layout.hotplace_row, null);
		TextView hotplace_name = (TextView) hotplace.findViewById(R.id.txt_hotplace_name);
		
		ImageView hotplace_image = (ImageView) hotplace.findViewById(R.id.Gallery_hotplace);
		
		placeMarkerBtn = (ImageButton) hotplace.findViewById(R.id.btn_location);
		m_viewFlipper.addView(hotplace);
		new searchPlace(hotplace,hotplace_name,hotplace_image,placeMarkerBtn).execute();
		
	}//onCreate
	
	private void MoveNextView(){
		m_viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
		m_viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
		if(coll.size()!=0)
		{
			if(m_viewFlipper.getDisplayedChild() == m_viewFlipper.getChildCount()-1 || m_viewFlipper.getChildCount()==0)
			{
				LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout hotplace = (LinearLayout) inf.inflate(R.layout.hotplace_row, null);
				TextView hotplace_name = (TextView) hotplace.findViewById(R.id.txt_hotplace_name);
				
				ImageView hotplace_image = (ImageView) hotplace.findViewById(R.id.Gallery_hotplace);
				placeMarkerBtn = (ImageButton) hotplace.findViewById(R.id.btn_location);
				
				m_viewFlipper.addView(hotplace);
				new searchPlace(hotplace,hotplace_name,hotplace_image,placeMarkerBtn).execute();
			}
			
			m_viewFlipper.showNext();
		}
	}
	private void MovewPreviousView(){
	    	m_viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,	R.anim.appear_from_left));
			m_viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
	    	m_viewFlipper.showPrevious();
	 }

	
	View.OnTouchListener MyTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				m_nPreTouchPosX = (int) event.getX();
			}
			if(event.getAction() == MotionEvent.ACTION_UP){
				int nTouchPosX = (int) event.getX();
				
				if(nTouchPosX < m_nPreTouchPosX){
					MoveNextView();
				}else if(nTouchPosX > m_nPreTouchPosX){
					MovewPreviousView();
				}
				m_nPreTouchPosX = nTouchPosX;
			}
			
			return true;
		}
	};//OnTouchListener
	class searchPlace extends AsyncTask<Void, Void, Void>
	{
		TextView hotplace_name;
		ImageView hotplace_image;
		ImageButton placeMarkerBtn;
		LinearLayout hotplace;
		int ranNum;
		public searchPlace(LinearLayout hotplace,TextView hotplace_name, ImageView hotplace_image,
				ImageButton placeMarkerBtn) {
			this.hotplace = hotplace;
			this.hotplace_name = hotplace_name;
			this.hotplace_image = hotplace_image;
			this.placeMarkerBtn = placeMarkerBtn;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			Bitmap bm = new ImageDownloader().download(IpUtil.getIp()+"HotPlace/"+coll.get(0)+".jpg");
			hotplace_image.setImageBitmap(bm);
			coll.remove(0);
		}


		@Override
		protected Void doInBackground(Void... params) {
			try {
				if(coll.size()>0)
				{
					HttpClient client = new DefaultHttpClient();
					String url = IpUtil.getIp()+"HotPlace/"+coll.get(0)+".txt";
					
					HttpPost post = new HttpPost(url);
					HttpResponse response;
					response = client.execute(post);
					HttpEntity entity = response.getEntity();
					Scanner scanner = new Scanner(entity.getContent());
					String body ="";
					while(scanner.hasNext())
					{
						body+=scanner.nextLine();
					}
					final JSONObject obj = new JSONObject(body);
					runOnUiThread(new Runnable(){
	
						@Override
						public void run() {
							try {
								hotplace_name.setText(obj.getString("name"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}});
					placeMarkerBtn.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							try {
								Intent intent = new Intent(HotPlace.this, Map.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								intent.putExtra("latitude",obj.getDouble("latitude"));
								intent.putExtra("longitude",obj.getDouble("longitude"));
								intent.putExtra("placename",obj.getString("name"));
								intent.putExtra("isHotPlace",1);
								startActivity(intent);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}//onClick
						
					});//setOnClickListener
				}
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
			return null;
		}
		
	}
}
