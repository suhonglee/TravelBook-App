package com.travelbook.activities;

import java.util.ArrayList;
import java.util.Vector;

import com.travelbook.cache.ImageDownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

public class ImageFlip extends Activity{
	ViewFlipper flipper;
	int m_nPreTouchPosX = 0;
	Intent intent;
	ProgressDialog progressDialog;
	private void MoveNextView()
	{
		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
		flipper.showNext();
	}
	private void MovewPreviousView()
	{
		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
		flipper.showPrevious();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intent = getIntent();
		flipper = new ViewFlipper(this);
		setContentView(flipper);
		flipper.setBackgroundColor(Color.BLACK);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("loading...");
		progressDialog.show();
		DownloadStreetImage dsi = new DownloadStreetImage();
		Thread t1 = new Thread(dsi);
		t1.start();
		flipper.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					m_nPreTouchPosX = (int)event.getX();
				}
				if (event.getAction() == MotionEvent.ACTION_UP)
				{
					int nTouchPosX = (int)event.getX();
					if (nTouchPosX < m_nPreTouchPosX)
					{
						MoveNextView();
					}
					else if (nTouchPosX > m_nPreTouchPosX)
					{
						MovewPreviousView();
					}
					m_nPreTouchPosX = nTouchPosX;
				}
				return true;
			}
		});
	}
	class DownloadStreetImage implements Runnable
	{
		public DownloadStreetImage()
		{
		}
		public void downloadBitmap()
		{
			ImageDownloader downloader = new ImageDownloader();
			for(int i=0;i<intent.getIntExtra("urlsize", 0);i++)
			{
				if(i==0)
				{
					progressDialog.dismiss();
				}
				String url = intent.getStringExtra("url"+i);
				if(url!=null)
				{
					final ImageView iv = new ImageView(flipper.getContext());
					iv.setImageBitmap(downloader.download(url));
					ImageFlip.this.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							flipper.addView(iv);
						}});
				}
			}
			
		}
		@Override
		public void run() {
			downloadBitmap();
		}
	}
}
