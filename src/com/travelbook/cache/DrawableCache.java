package com.travelbook.cache;


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

public class DrawableCache extends HashMap<String, SoftReference<Drawable>>{
	Context mContext;
	Bitmap cacheImage;

	public DrawableCache(){}
	public DrawableCache(Context mContext){
		this.mContext = mContext;
		cacheImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_bg);
	}
	 public SoftReference<Drawable> get(Integer key) {
	  String id = key.toString();
	  if(super.get(id) == null || super.get(id).get() == null){
	   //cache is empty. create a thumbnail image.
	   Bitmap bitmap;
	   if((Integer)key > 0){
	    bitmap = MediaStore.Images.Thumbnails.getThumbnail(
	      mContext.getContentResolver(), (Integer)key, MediaStore.Images.Thumbnails.MICRO_KIND, null);
	   }
	   else{
	    //fail to create a thumbnail. so, just use a default image. 
	    bitmap = cacheImage;
	   }
	   if(bitmap != null){
	    put(id, new SoftReference<Drawable>(new BitmapDrawable(bitmap)));
	   }
	  }
	  return super.get(id);
	 }

}
