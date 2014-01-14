package com.travelbook.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


final public class ImageManager implements ImageDelegate {
    final static private boolean IS_DEBUG = true;
    static private ImageManager instance;
    
    static public ImageManager getInstance(Context context) {
        if(instance == null) instance = new ImageManager(context);
        return instance;
    }
    static public void destoryInstance() {
        instance.destory();
        instance = null;
    }
    
    
    // Soft Cache에 저장해놓는 Drawable의 양.
    // 안드로이드의 메모리에 저장되는 cache이므로 너무 많으면 램을 많이 차지하게 된다.
    final static private int SOFTCACHE_SIZE = 5;
    // 동시에 다운로드 받을 수 있는 한도.
    final static private int DOWNLOAD_COUNT = 5;
    
    private Context context;
    private TreeMap<String, SoftReference<Drawable>> softCacheTable;
    private HashMap<String, ImageLoader> asyncTable;
    private HashMap<String, ArrayList<WeakReference<ImageView>>> imageViewTable;
    private LinkedList<String> downloadQueue;
    
    public ImageManager(Context context) {
        initialize(context);
    }
    private void initialize(Context context) {
        this.context = context;
        softCacheTable = new TreeMap<String, SoftReference<Drawable>>();
        asyncTable = new HashMap<String, ImageLoader>();
        imageViewTable = new HashMap<String, ArrayList<WeakReference<ImageView>>>();
        downloadQueue = new LinkedList<String>();
    }
    public void destory() {
        context = null;
        
        softCacheTable.clear(); softCacheTable = null;
        
        for(final ImageLoader loader : asyncTable.values())
            loader.cancel(true);
        asyncTable.clear(); asyncTable = null;
        
        for(final ArrayList<WeakReference<ImageView>> list : imageViewTable.values())
            list.clear();
        imageViewTable.clear(); imageViewTable = null;
        downloadQueue.clear(); downloadQueue = null;
    }
    
    public boolean getImage(String image_path, ImageView view) {
        // 1. soft cache를 확인하여 있으면 처리한다.
        Drawable drawable = loadImageFromSoftCache(image_path);
        if(drawable != null) {
            view.setImageDrawable(drawable);
            log("Loaded from soft cache: " + image_path);
            return true;
        }
        // 2. hard cache를 확인하여 있으면 처리한다.
        drawable = loadImageFromHardCache(image_path);
        if(drawable != null) {
            view.setImageDrawable(drawable);
            log("Loaded from hard cache: " + image_path);
            return true;
        }
        // 3. 다운로드가 진행중인 AsyncTask가 없으면, 만들어서 시작하도록 한다.
        if(!imageViewTable.containsKey(image_path)) {
            if(imageViewTable.size() >= DOWNLOAD_COUNT) {
                // 3-1. 다운로드 개수가 가득 찼을 경우엔 Queue에 넣고 다운로드는 아직 하지 않는다.
                downloadQueue.add(image_path);
                log("Saved to download queue: " + image_path);
            } else {
                // 3-2. 다운로드 개수가 아직 차지 않았을 경우에는 다운로드를 진행한다.
                startDownload(image_path);
                log("Start download: " + image_path);
            }
        }
        
        // 4. 다운로드가 진행중인, 혹은 진행 예정인 table에 ImageView를 넣는다.
        if(!imageViewTable.containsKey(image_path))
            imageViewTable.put(image_path, new ArrayList<WeakReference<ImageView>>());
        final ArrayList<WeakReference<ImageView>> list = imageViewTable.get(image_path);
        list.add(new WeakReference<ImageView>(view));
        log("Add ImageView: " + image_path);
        
        return false;
    }
    
    public void removeImageView(String image_path, ImageView view) {
        if(imageViewTable.containsKey(image_path)) {
            final ArrayList<WeakReference<ImageView>> list = imageViewTable.get(image_path);
            for(WeakReference<ImageView> ref : list)
                if(ref.get() == view) list.remove(ref);
        }
    }
    
    
    
    private void startDownload(String file_path) {
        final ImageLoader loader = new ImageLoader();
        loader.start(file_path, this);
        asyncTable.put(file_path, loader);
    }
    
    private Drawable loadImageFromSoftCache(String image_path) {
        Drawable result = null;
        if(softCacheTable.containsKey(image_path)) {
            result = softCacheTable.get(image_path).get();
            if(result == null)
                softCacheTable.remove(image_path);
        }
        return result;
    }
    private boolean saveImageToSoftCache(String image_path, Drawable drawable) {
        // 이번 추가로 인해 Soft Cache의 개수가 지정된 양보다 많을 것 같으면
        // Soft Cache에 가장 처음 저장된 아이템을 지운다.
        if(softCacheTable.size() >= SOFTCACHE_SIZE)
            softCacheTable.remove(softCacheTable.firstKey());
        
        if(!softCacheTable.containsKey(image_path))
            softCacheTable.put(image_path, new SoftReference<Drawable>(drawable));
            
        return true;
    }
    private Drawable loadImageFromHardCache(String image_path) {
        Drawable result = null;
        try {
            final File file = new File(context.getCacheDir().toString() + "/" + md5(image_path));
            if(file.exists()) {
                result = Drawable.createFromPath(file.toString());
                saveImageToSoftCache(image_path, result);
            }
        } catch(Exception e) {}
        return result;
    }
    private boolean saveImageToHardCache(String image_path, byte[] data) {
        boolean result = false;
        
        try {
            final File file = new File(context.getCacheDir().toString() + "/" + md5(image_path)); 
            if(file.exists())
                file.delete();
            
            final FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            
            log("Saved to hard cache: " + image_path);
            return true;
        } catch(Exception e) {}
        
        return result;
    }
    
    
    
    @Override
    public void onCompleted(String image_path, byte[] data, Drawable drawable) {
        log("Download Completed: " + image_path);
        // 1. Drawable이 null이면 시망한 거임.
        if(drawable != null) {
            // 2. Soft Cache에 내용 저장.
            saveImageToSoftCache(image_path, drawable);
            
            // 3. Hard Cache에 파일 저장.
            saveImageToHardCache(image_path, data);
            
            // 4. 뷰들에 이미지를 보여줌.
            if(imageViewTable.containsKey(image_path)) {
                final ArrayList<WeakReference<ImageView>> list = imageViewTable.get(image_path);
                for(WeakReference<ImageView> ref : list) {
                    if(ref.get() != null)
                        ref.get().setImageDrawable(drawable);
                }
                log("Showed to ImageView: " + image_path);
                list.clear();
            }
        }
        
        // 5. 각 객체의 연결을 끊고 싹 지움.
        imageViewTable.remove(image_path);
        asyncTable.remove(image_path);
        
        // 6. Queue에 남은 다운로드가 있을 경우, 해당 다운로드를 동작시킨다.
        if(downloadQueue.size() > 0) {
            final String file_path = downloadQueue.removeFirst();
            startDownload(file_path);
            log("Start download from queue: " + file_path);
        }
    }
    
    
    private String md5(String s) {
        try {
            final MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            final byte messageDigest[] = digest.digest();
            
            final StringBuffer hexString = new StringBuffer(); final int length = messageDigest.length;
            for(int i=0; i<length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            
            return hexString.toString();
        } catch(Exception e) { }
        return null;
    }
    
    private void log(String msg) {
        if(IS_DEBUG) Log.d("ImageManager", msg);
    }
    
}



class ImageLoader extends AsyncTask<String, Void, byte[]> {
    private ImageDelegate delegate;
    private String image_path;
    
    public void start(String image_path, ImageDelegate delegate) {
        this.image_path = image_path;
        this.delegate = delegate;
        
        execute(image_path);
    }
    
    @Override
    protected byte[] doInBackground(String... params) {
        byte[] result = null;
        final HttpURLConnection conn = makeConnection(params[0], "GET");
        if(conn == null) return null;
        
        try {
            conn.connect();
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final InputStream is = conn.getInputStream();
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                
                int size = 0;
                byte[] buff = new byte[2048];
                while((size = is.read(buff)) != -1)
                    os.write(buff, 0, size);
                is.close();
                result = os.toByteArray();
                os.close();
            }
            conn.disconnect();
        } catch(Exception e) { }
        
        return result;
    }
    
    @Override
    protected void onPostExecute(byte[] result) {
        Drawable drawable = null;
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(result);
            drawable = Drawable.createFromStream(bais, image_path);
        } catch(Exception e) {}
        
        if(delegate != null) delegate.onCompleted(image_path, result, drawable);
    }
    
    final private HttpURLConnection makeConnection(String uri, String method) {
        try {
            final URL url = new URL(uri);
            HttpURLConnection conn = null;
            if(url.getProtocol().toLowerCase().equals("https")) {
                final HttpsURLConnection https = (HttpsURLConnection)url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection)url.openConnection();
            }
            conn.setRequestMethod(method);
            conn.setRequestProperty("User-Agent", "Opera/9.80 (Macintosh; Intel Mac OS X 10.7.0; U; en) Presto/2.9.168 Version/11.50");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            
            return conn;
        } catch(Exception e) {
            return null;
        }
    }
    final static private HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
                return true;
        }
    };
}

interface ImageDelegate {
    void onCompleted(String image_path, byte[] data, Drawable drawable);
}
