package com.travelbook.holder;

import android.app.Activity;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class ReviewHolder {
	public ImageView image;
	public ImageView imageLine;
	public TextView name;
	public TextView body;
	public ImageView profile;
	public VideoView video;
	public TextView like;
	public TextView comment;
	public TextView time;
	private Activity act;
	public ReviewHolder(Activity act) {
		super();
		this.act = act;
	}
	
	public void setFont()
	{
		Typeface tf = Typeface.createFromAsset(act.getAssets(), "fonts/SeoulHangangM.ttf");
		Typeface tf_b = Typeface.createFromAsset(act.getAssets(), "fonts/SeoulNamsanB.ttf");
		name.setTypeface(tf_b);
		body.setTypeface(tf_b);
		time.setTypeface(tf);
		like.setTypeface(tf);
		comment.setTypeface(tf);
	}
}
