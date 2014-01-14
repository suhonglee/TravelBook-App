package com.travelbook.cache;


import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.provider.MediaStore;
import android.util.Log;

public class ImageDownloaderCache extends HashMap<String, SoftReference<Bitmap>>{
	Bitmap cacheImage;
	public ImageDownloaderCache(){}
	@Override
	public SoftReference<Bitmap> get(Object obj) {
		String url = (String)obj;
		String key = url.substring(49,url.length());
		if(super.get(key)==null || super.get(key).get()==null){
			synchronized (url) {
				final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		        final HttpGet getRequest = new HttpGet(url);
		        try{
		            HttpResponse response = client.execute(getRequest);
		            final int statusCode = response.getStatusLine().getStatusCode();
		            if (statusCode != HttpStatus.SC_OK){ 
		                Log.w("ImageDownloader :: ", "Error " + statusCode + " bitmap from " + url); 
		                return null;
		            }
		            final HttpEntity entity = response.getEntity();
		            if (entity != null) {
		                InputStream inputStream = null;
		                try {
		                    inputStream = entity.getContent(); 
		                    BitmapFactory.Options options = new BitmapFactory.Options();
		                    options.inSampleSize=8;
		                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
		                    put(key, new SoftReference<Bitmap>(bitmap));
		                } finally 
		                {
		                    if (inputStream != null)
		                        inputStream.close();  
		                    entity.consumeContent();
		                }
		            }
		        } catch (Exception e) {
		            getRequest.abort();
		            Log.w("ImageDownloader :: ", "Error " + url +"_Exception :::"+ e.toString());
		        } finally {
		            if (client != null)
		                client.close();
		        }
			}
		}
		return super.get(key);
	}
	
}
