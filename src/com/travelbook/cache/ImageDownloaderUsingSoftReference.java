package com.travelbook.cache;
//package com.example.travelbook;
//
//import java.io.InputStream;
//import java.lang.ref.SoftReference;
//import java.lang.ref.WeakReference;
//import java.util.concurrent.ExecutionException;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.methods.HttpGet;
//
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
//import com.google.android.gms.maps.model.Marker;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.http.AndroidHttpClient;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//public class ImageDownloaderUsingSoftReference extends AsyncTask<String, Void, Bitmap>{
//	ImageView imgv;
//	String url;
//	public ImageDownloaderUsingSoftReference(){}
//	public ImageDownloaderUsingSoftReference(ImageView img)
//	{
//		imgv = img;
//	}
//    @Override
//    // Once the image is downloaded, associates it to the imageView
//    protected void onPostExecute(Bitmap bitmap) {
//    	BitmapDownloadCache cache = new BitmapDownloadCache();
//        if (isCancelled()) {
//            bitmap = null;
//        }
//        else if(imgv!=null)
//        {
//        	imgv.setImageBitmap(cache.get(url, bitmap).get());
//        }
//    }
//	@Override
//	protected Bitmap doInBackground(String... params) {
//		return downloadBitmap(params[0]);
//	}
//	Bitmap downloadBitmap(String url) {
//		this.url = url;
//		BitmapDownloadCache c = new BitmapDownloadCache();
//		if(c.get(url)==null)
//		{
//	        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
//	        final HttpGet getRequest = new HttpGet(url);
//	        try {
//	            HttpResponse response = client.execute(getRequest);
//	            final int statusCode = response.getStatusLine().getStatusCode();
//	            if (statusCode != HttpStatus.SC_OK) { 
//	                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
//	                return null;
//	            }
//	            
//	            final HttpEntity entity = response.getEntity();
//	            if (entity != null) {
//	                InputStream inputStream = null;
//	                try {
//	                    inputStream = entity.getContent(); 
//	                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//	                    c.put(url, new SoftReference<Bitmap>(bitmap));
//	                    return bitmap;
//	                } finally {
//	                    if (inputStream != null) {
//	                        inputStream.close();  
//	                    }
//	                    entity.consumeContent();
//	                }
//	            }
//	        } catch (Exception e) {
//	            // Could provide a more explicit error message for IOException or IllegalStateException
//	            getRequest.abort();
//	            Log.w("ImageDownloader", "Error while retrieving bitmap from " + url +";;;"+ e.toString());
//	        } finally {
//	            if (client != null) {
//	                client.close();
//	            }
//	        }
//		}
//        return null;
//    }
//
//    
//}
//
