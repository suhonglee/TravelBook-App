package com.travelbook.activities;

import java.net.URI;

import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.MediaController;
import android.widget.VideoView;

public class MyVideoPlayer extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.myvideoplayer);
		
		System.out.println("받아온거임 ㅋ"+ getIntent().getStringExtra("source"));
		final VideoView player = (VideoView) findViewById(R.id.video_player);
		Uri uri = Uri.parse(IpUtil.getIp()+getIntent().getStringExtra("source"));
		player.setVideoURI(uri);
		final MediaController controller = new MediaController(this);
		controller.setAnchorView(player);
		player.setMediaController(controller);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				player.start();
				controller.show();
			}
		}, 1000);
	}
	
	
}
