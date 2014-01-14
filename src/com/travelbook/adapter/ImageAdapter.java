package com.travelbook.adapter;

import java.util.Vector;

import com.travelbook.activities.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ImageAdapter  extends BaseAdapter{

	int mImageItemBackground;
	private Context mContext;
	private Vector<ImageView> iv;
	private Vector<Bitmap> bitmapList;
	private Vector<Drawable> drawableList;
	boolean detailbook = false;
	boolean writeReview = false;
	public void freeImageAdapterMemory()
	{
		for(int i=0;i<iv.size();i++)
		{
			Drawable d = iv.get(i).getDrawable();
			Bitmap b = ((BitmapDrawable)d).getBitmap();
			b.recycle();
			b=null;
			d=null;
			System.gc();
		}
		mContext=null;
		iv=null;
	}
	public ImageAdapter(Context c){
		mContext = c;
		iv = new Vector<ImageView>();
		bitmapList = new Vector<Bitmap>();
	}
	
	public ImageAdapter(Context c,Vector<Bitmap> bitmapList){
		mContext = c;
		iv = new Vector<ImageView>();
		this.bitmapList = bitmapList;
	}
	
	public ImageAdapter(Context c,Vector<Bitmap> bitmapList,boolean detailbook,boolean writeReview){
		mContext = c;
		iv = new Vector<ImageView>();
		this.bitmapList = bitmapList;
		this.detailbook = detailbook;
		this.writeReview = writeReview;
	}
	public void addItem(Bitmap bitmap)
	{
		bitmapList.add(bitmap);
	}
	public void addItem(Drawable drawable)
	{
		drawableList.add(drawable);
	}
	@Override
	public int getCount() {
		return bitmapList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null)
		{
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView =inflater.inflate(R.layout.detailviewimage, null); 
		}
		convertView.findViewById(R.id.detailviewimagebackground).setVisibility(View.GONE);
		if(detailbook)
		{
			ImageView imgv = (ImageView) convertView.findViewById(R.id.detailviewimage);
			imgv.setImageBitmap(bitmapList.get(position));
			imgv.setLayoutParams(new RelativeLayout.LayoutParams(500, 500));
			imgv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			convertView.findViewById(R.id.detailviewimagebackground).setVisibility(View.VISIBLE);
			return convertView;
		}
		if(writeReview){

			ImageView imgv = (ImageView) convertView.findViewById(R.id.detailviewimage);
			imgv.setImageBitmap(bitmapList.get(position));
			imgv.setLayoutParams(new RelativeLayout.LayoutParams(250, 250));
			imgv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			ImageView back = (ImageView)convertView.findViewById(R.id.detailviewimagebackground);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) back.getLayoutParams();
			lp.width=260;
			lp.height=260;
			back.setLayoutParams(lp);
			back.setVisibility(View.VISIBLE);
			
			return convertView;
		}
		iv.add(new ImageView(mContext));
		iv.get(position).setImageBitmap(bitmapList.get(position));
		iv.get(position).setScaleType(ImageView.ScaleType.FIT_XY);
		iv.get(position).setLayoutParams(new Gallery.LayoutParams(200, 150));
		return iv.get(position);
	}

}
