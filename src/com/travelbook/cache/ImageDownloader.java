package com.travelbook.cache;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.travelbook.utility.IpUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageDownloader{
	private Vector<Vector<String>> fileV;
	private HashMap<String,SoftReference<Bitmap>> profileList;
	private HashMap<Integer,Bitmap> bitmapList;
	private BaseAdapter adapter;
	private Activity context;
	private imageCache ic;
	public ImageDownloader(){
		profileList = new HashMap<String, SoftReference<Bitmap>>();
	}
	public ImageDownloader(Vector<Vector<String>> fileV,HashMap<Integer,Bitmap> bitmapList,BaseAdapter adapter,Activity context){
		this.fileV = fileV;
		this.bitmapList = bitmapList;
		System.out.println("imageDownloader가 생성되면서 인자로 받아온 bitmapList " + bitmapList);
		this.adapter = adapter;
		this.context = context;
		ic = new imageCache();
	}

	public void download(int start, int end)
	{
		new Downloader(start,end).execute(0);
	}
    
    class Downloader extends AsyncTask<Integer, Void, Bitmap> {
        private String url;
        
        private int start;
        private int end;
        public Downloader(int start,int end)
        {
        	this.start = start;
        	this.end = end;
        }
        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(Integer... params) {
             // params comes from the execute() call: params[0] is the url.
        	if(bitmapList.get(start)==null)
        	{
             return downloadBitmap();
        	}
        	return bitmapList.get(start);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            bitmapList.put(start, bitmap);
            context.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}});
            if(start<end-1)
            {
            	new Downloader(start+1,end).execute();
            }
        }
	    Bitmap downloadBitmap() {
	    	if(fileV.size()>start && fileV.get(start).size()>0)
	    	{
	    		String[] files = fileV.get(start).get(0).split("/");
				files[files.length-1] = "Thumbnail_"+files[files.length-1];
				String url = IpUtil.getIp();
				for(int gg=0;gg<files.length-1;gg++)
				{
					url+=files[gg]+"/";
				}
				url+=files[files.length-1];
		    	if(ic.get(fileV.get(start).get(0))==null || ic.get(fileV.get(start).get(0)).get()==null)
		    	{
			        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			        final HttpGet getRequest = new HttpGet(url);
			        try {
			            HttpResponse response = client.execute(getRequest);
			            final int statusCode = response.getStatusLine().getStatusCode();
			            if (statusCode != HttpStatus.SC_OK) { 
			                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
			                return null;
			            }
			            final HttpEntity entity = response.getEntity();
			            if (entity != null) {
			                InputStream inputStream = null;
			                try {
			                    inputStream = entity.getContent(); 
			                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//					                    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
			                    ic.put(fileV.get(start).get(0), new SoftReference<Bitmap>(bitmap));
			                    return bitmap;
			                } finally {
			                    if (inputStream != null) {
			                        inputStream.close();  
			                    }
			                    entity.consumeContent();
			                }
			            }
			        } catch (Exception e) {
			            // Could provide a more explicit error message for IOException or IllegalStateException
			            getRequest.abort();
			            Log.w("ImageDownloader", "Error while retrieving bitmap from " + url +";;;"+ e.toString());
			        } finally {
			            if (client != null) {
			                client.close();
			            }
			        }
		    	}
		    	return ic.get(fileV.get(start).get(0)).get();
	    	}
	    	return null;
	    }
    }
    
    class imageCache extends HashMap<String,SoftReference<Bitmap>>
    {
		public SoftReference<Bitmap> get(String key) {
			return super.get(key);
		}

		@Override
		public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value) {
			return super.put(key, value);
		}
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public Bitmap profileDownload(String url)
    {
    	BitmapDownloaderTask2 task = new BitmapDownloaderTask2();
    	try {
			return task.execute(url).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    class BitmapDownloaderTask2 extends AsyncTask<String, Void, Bitmap> {
        private String url;

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(String... params) {
        	if(profileList.get(params[0])==null || profileList.get(params[0]).get() ==null)
        	{
	        	final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
	            final HttpGet getRequest = new HttpGet(params[0]);
	            try {
	                HttpResponse response = client.execute(getRequest);
	                final int statusCode = response.getStatusLine().getStatusCode();
	                if (statusCode != HttpStatus.SC_OK) { 
	                    Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
	                    return null;
	                }
	                final HttpEntity entity = response.getEntity();
	                if (entity != null) {
	                    InputStream inputStream = null;
	                    try {
	                        inputStream = entity.getContent(); 
	                        BitmapFactory.Options options = new BitmapFactory.Options();
	                        options.inSampleSize=2;
	                        options.inScaled=true;
	                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
	                        profileList.put(params[0], new SoftReference<Bitmap>(bitmap));
	                    } finally {
	                        if (inputStream != null) {
	                            inputStream.close();  
	                        }
	                        entity.consumeContent();
	                    }
	                }
	            } catch (Exception e) {
	                // Could provide a more explicit error message for IOException or IllegalStateException
	                getRequest.abort();
	                Log.w("ImageDownloader", "Error while retrieving bitmap from " + url +";;;"+ e.toString());
	            } finally {
	                if (client != null) {
	                    client.close();
	                }
	            }
        	}
            return profileList.get(params[0]).get();
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
        }
    }
    
    
    
    public Bitmap download(String url) {
        BitmapDownloaderTask task = new BitmapDownloaderTask();
		try {
			return task.execute(url).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private String url;

    @Override
    // Actual download method, run in the task thread
    protected Bitmap doInBackground(String... params) {
         // params comes from the execute() call: params[0] is the url.
         return downloadBitmap(params[0]);
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
    }
}

static Bitmap downloadBitmap(String url) {
    final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
    final HttpGet getRequest = new HttpGet(url);
    try {
        HttpResponse response = client.execute(getRequest);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) { 
            Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
            return null;
        }
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream inputStream = null;
            try {
                inputStream = entity.getContent(); 
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=1;
                options.inScaled=true;
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
                return bitmap;
            } finally {
                if (inputStream != null) {
                    inputStream.close();  
                }
                entity.consumeContent();
            }
        }
    } catch (Exception e) {
        // Could provide a more explicit error message for IOException or IllegalStateException
        getRequest.abort();
        Log.w("ImageDownloader", "Error while retrieving bitmap from " + url +";;;"+ e.toString());
    } finally {
        if (client != null) {
            client.close();
        }
    }
    return null;
}

public Bitmap scaledDownload(String url) {
    BitmapDownloaderTask3 task = new BitmapDownloaderTask3();
	try {
		return task.execute(url).get();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
}
class BitmapDownloaderTask3 extends AsyncTask<String, Void, Bitmap> {
    private String url;

    @Override
    // Actual download method, run in the task thread
    protected Bitmap doInBackground(String... params) {
         // params comes from the execute() call: params[0] is the url.
         return downloadBitmap(params[0]);
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
    }
}

static Bitmap downloadScaledBitmap(String url) {
    final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
    final HttpGet getRequest = new HttpGet(url);
    try {
        HttpResponse response = client.execute(getRequest);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) { 
            Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
            return null;
        }
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream inputStream = null;
            try {
                inputStream = entity.getContent(); 
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=8;
                options.inScaled=true;
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
                return bitmap;
            } finally {
                if (inputStream != null) {
                    inputStream.close();  
                }
                entity.consumeContent();
            }
        }
    } catch (Exception e) {
        // Could provide a more explicit error message for IOException or IllegalStateException
        getRequest.abort();
        Log.w("ImageDownloader", "Error while retrieving bitmap from " + url +";;;"+ e.toString());
    } finally {
        if (client != null) {
            client.close();
        }
    }
    return null;
}

}

