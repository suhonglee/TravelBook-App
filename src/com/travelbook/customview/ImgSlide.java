package com.travelbook.customview;

import android.content.Context;
import android.graphics.Camera;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class ImgSlide extends Gallery{
	private Context mContext;
	private static Camera mCamera;
	
	public ImgSlide(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ImgSlide(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ImgSlide(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext= context;
		mCamera = new Camera();
		setSpacing(30);
		setLayoutParams(new LayoutParams(60, 60));
		// TODO Auto-generated constructor stub
	}
	protected boolean getChildStaticTransforamtion(View child, Transformation t){
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}
 
}
