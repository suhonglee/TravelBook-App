package com.travelbook.cache;


import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.travelbook.activities.R;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

public class BitmapCache extends HashMap<String, SoftReference<Bitmap>>{
	Context mContext;
	Bitmap cacheImage;

	public BitmapCache(){}
	public BitmapCache(Context mContext){
		this.mContext = mContext;
		cacheImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_bg);
	}
	 public SoftReference<Bitmap> get(Uri key) {
		String id = key.getPath();
	 try {
	  if(super.get(id) == null || super.get(id).get() == null){
	   //cache is empty. create a thumbnail image.
	   Bitmap bitmap;
	   BitmapFactory.Options option = new BitmapFactory.Options();
	   option.inSampleSize= 4;
	   System.out.println("id������"+id);
		bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(key), null, option);
	   if(bitmap!=null)
	   {
		   put(id, new SoftReference<Bitmap>(bitmap));
	   }
	  }
	 } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 return super.get(id);
	 }
}
