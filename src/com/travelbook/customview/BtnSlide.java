package com.travelbook.customview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class BtnSlide extends Gallery{
	private Context mContext;
	
	public BtnSlide(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public BtnSlide(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public BtnSlide(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext= context;
		setLayoutParams(new LayoutParams(60, 60));
		// TODO Auto-generated constructor stub
	}
	protected boolean getChildStaticTransforamtion(View child, Transformation t){
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	
	
 
}
