package com.travelbook.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.facebook.Session;
import com.travelbook.adapter.ImageAdapter;
import com.travelbook.cache.BitmapCache;
import com.travelbook.cache.DrawableCache;
import com.travelbook.customview.ImgSlide;
import com.travelbook.customview.MyProgressDialog;
import com.travelbook.holder.User;
import com.travelbook.utility.CustomMultiPartEntity;
import com.travelbook.utility.IpUtil;
import com.travelbook.utility.UriUtil;
import com.travelbook.utility.CustomMultiPartEntity.ProgressListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemLongClickListener;

public class WriteReview extends Activity{
	AlertDialog imageDialog;
	private static final int TAKE_CAMERA = 5844;
	private static final int TAKE_GALLERY = 5882;
	private static final int TAKE_GALLERY_DUAL = 5884;
	private static final int TAKE_VIDEO = 7474;
	private static final int TAKE_GALLERY_VIDEO = 824;
	private static final int TAKE_EDIT_GALLERY = 5844824;
	private static final int IMAGE_CROP = 123456789;
	private Vector<Bitmap> bitmapList;
	private ArrayList<String> pathList = new ArrayList<String>();
	private ImageAdapter adapter;
	private Dialog uploadDialog;
	private ImageView write_review_video;
	private boolean haveVideo = false;
	private boolean haveImage = false;
	private String videoThumbnailPath;
	private String dir;
	
	private String userName;
	private String email;
	private String password;
	private int loginType;
	private Double latitude;
	private Double longitude;
	private String placename;
	
	private MyProgressDialog pd;
	
	private String addressLine;
	
	private Uri copyPicture;
	private int editPicturePosition;
	@Override
	protected void onPause() {
		super.onPause();
		if(pd!=null)
			pd.dismiss();
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_review);
		
		email = getIntent().getStringExtra("email");
		userName = getIntent().getStringExtra("username");
		loginType = getIntent().getIntExtra("logintype", 0);
		latitude = getIntent().getDoubleExtra("latitude", 0);
		longitude = getIntent().getDoubleExtra("longitude", 0);
		placename = getIntent().getStringExtra("placename");
		
		if(loginType==0)
			new FindUser(email).execute();
		
		Geocoder coder = new Geocoder(WriteReview.this);
		try {
			List<Address> add = coder.getFromLocation(latitude, longitude, 1);
			addressLine = add.get(0).getAddressLine(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//set ImgSlide Images (temp)
		ImgSlide imgSlide = (ImgSlide) findViewById(R.id.write_review_imgSlide);
		imgSlide.setUnselectedAlpha(1.0f);
		bitmapList = new Vector<Bitmap>();
		adapter = new ImageAdapter(this, bitmapList, false, true);
		imgSlide.setAdapter(adapter);
		imgSlide.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int i, long arg3) {
				editPicturePosition = i;
				final Dialog dialog = new Dialog(WriteReview.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.select_edit_image);
				Button b = (Button) dialog.findViewById(R.id.btn_edit_image);
				b.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent("com.android.camera.action.CROP");
						copyPicture = getCopyPicture();
						File file = new File(pathList.get(editPicturePosition));
						
						intent.setDataAndType(Uri.fromFile(file), "image/*");
						
						intent.putExtra("aspectX", 4);
						intent.putExtra("aspectY", 3);
						intent.putExtra("scale", true);
						intent.putExtra("output", copyPicture);
						startActivityForResult(intent, IMAGE_CROP);
						
						dialog.dismiss();
					}
				});
				Button bv = (Button) dialog.findViewById(R.id.btn_delete_image);
				bv.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						AlertDialog.Builder builder = new AlertDialog.Builder(WriteReview.this);
						builder.setMessage("Want to delete image?");
						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								bitmapList.remove(editPicturePosition);
								pathList.remove(editPicturePosition);
								adapter.notifyDataSetChanged();
								dialog.dismiss();
							}
						});
						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						imageDialog = builder.create();
						imageDialog.show();
					}
				});
				dialog.show();
			}
		});
		
		write_review_video = (ImageView) findViewById(R.id.write_review_video);
		write_review_video.setVisibility(View.GONE);
		write_review_video.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(WriteReview.this);
				builder.setMessage("Want to delete video?");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						write_review_video.setVisibility(View.GONE);
						pathList.remove(0);
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				imageDialog = builder.create();
				imageDialog.show();
				return false;
			}
		});
	}
	
	public Uri getCopyPicture(){
		Uri copyUri = null;
		try {
			File path = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/TravelBook/");
			
			if (!path.isDirectory()) {
				path.mkdirs();
			}
			
			path = new File(Environment.getExternalStorageDirectory().getPath()
					+ "/TravelBook/tempimage.jpg");
			
			path.createNewFile();
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			
			
			if(path.length()>8000000)
			{
				options.inSampleSize = 8;
			}
			else if(path.length()>4000000)
			{
				options.inSampleSize = 6;
			}
			else if(path.length()>2000000)
			{
				options.inSampleSize = 4;
			}
			else if(path.length()>1000000)
			{
				options.inSampleSize = 2;
			}
			Uri temp =Uri.parse(path.toString());
			Bitmap bm = BitmapFactory.decodeFile(pathList.get(editPicturePosition), options);
			FileOutputStream out = new FileOutputStream(path);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
			copyUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+"/TravelBook/tempimage.jpg"));
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return copyUri;
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode ==123456789){
			if(resultCode==-1)
			{
				Intent intent = new Intent(WriteReview.this,EditImageActivity.class);
				intent.putExtra("path", copyPicture.getPath());
				intent.putExtra("position", editPicturePosition);
				
				startActivityForResult(intent, TAKE_EDIT_GALLERY);
			}
		}
		
		if(requestCode == TAKE_EDIT_GALLERY)
		{
			if(data != null)
			{
				String filename = data.getStringExtra("filename");
				System.out.println("filename ::::::: " + filename);

				Bitmap picture = BitmapFactory.decodeFile(filename);
				
				bitmapList.set(editPicturePosition, picture);
				pathList.set(editPicturePosition, filename);
				
				adapter.notifyDataSetChanged();
				haveImage = true;
			}
		}
		if(requestCode == TAKE_CAMERA || requestCode == TAKE_GALLERY)
		{
			if(data!=null)
			{
				if(data.getData()!=null)
				{
					Uri selPhotoUri = data.getData(); 
					try {
						SoftReference<Bitmap> softRef = new BitmapCache(WriteReview.this).get(selPhotoUri);
						Bitmap picture = softRef.get();
						if(requestCode==TAKE_CAMERA){
							//Rotate taken picture
							String tmpPath=UriUtil.getPath(WriteReview.this, selPhotoUri);
							picture=rotateAndSave(picture, tmpPath);
						}
						bitmapList.add(picture);
						adapter.notifyDataSetChanged();
						haveImage = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					pathList.add(UriUtil.getPath(WriteReview.this, selPhotoUri));
				}
			}
		}
		if(requestCode == TAKE_GALLERY_DUAL)
		{
			if(data!=null)
			{
				ArrayList<String> images = data.getStringArrayListExtra("images");
				ArrayList<String> path = data.getStringArrayListExtra("path");
				if(images!=null)
				{
					for(int i=0;i<images.size();i++)
					{
						SoftReference<Drawable> softRef =new DrawableCache(WriteReview.this).get(Integer.parseInt(images.get(i)));
						bitmapList.add(((BitmapDrawable)softRef.get()).getBitmap());
					}
					adapter.notifyDataSetChanged();
					haveImage = true;
				}
				for(int i=0;i<path.size();i++)
				{
					pathList.add(path.get(i));
				}
			}
		}
		if(requestCode == TAKE_GALLERY_VIDEO)
		{
			if(data!=null)
			{
//				ImageView videoView = (ImageView) findViewById(R.id.write_review_video);
//				final VideoView videoView = new VideoView(this);
//				videoView.setVideoURI(data.getData());
//				videoView.setMediaController(new MediaController(WriteReview.this));
//				WriteReview.this.setContentView(videoView);
				write_review_video.setVisibility(View.VISIBLE);
				haveVideo= true;
				getVideoThumbnail(UriUtil.getPath(this,data.getData()));
				pathList.clear();
				pathList.add(UriUtil.getPath(this,data.getData()));
			}
		}
		if(requestCode == TAKE_VIDEO)
		{
			if(data!=null)
			{
				pathList.clear();
				pathList.add(UriUtil.getPath(this,data.getData()));
				getVideoThumbnail(UriUtil.getPath(this,data.getData()));
				write_review_video.setVisibility(View.VISIBLE);
				haveVideo = true;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}


	
	public void mOnClick(View v)
	{
		System.out.println("pathList size"+ pathList.size());
		if(v.getId()==R.id.write_review_ok)
		{
			EditText write_review_text = (EditText) WriteReview.this.findViewById(R.id.write_review_text);
			if(write_review_text.getText().length()<1)
			{
				new AlertDialog.Builder(WriteReview.this).setMessage("PLEASE WRITE TEXT!").show();
			}
			else
			{
				new UploadReview().execute();
			}
		}
		if(v.getId()==R.id.write_review_cancel)
		{
		}
		if(v.getId()==R.id.write_review_upload)
		{
			OnClickListener clicker = new OnClickListener() {
				
				@Override
				public void onClick(final View v) {
					if(uploadDialog!=null)
						uploadDialog.dismiss();
					if(v.getId()==R.id.camera || v.getId()==R.id.gallery_single || v.getId()==R.id.gallery_multi)
					{
						if(haveVideo)
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
							builder.setMessage("want overwrite attached file?");
							builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog, int which) {
									pathList.clear();
									write_review_video.setVisibility(View.GONE);
									haveVideo=false;
									dialog.dismiss();
									if(v.getId()==R.id.camera)
									{
										if(pathList.size()>=10)
										{
											AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
											builder.setMessage("you can upload images smaller than 10");
											builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													dialog.dismiss();
												}
											});
											builder.show();
										}
										else
										{
											Intent intent = new Intent();
							                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							                pd = MyProgressDialog.show(WriteReview.this);
											startActivityForResult(intent, TAKE_CAMERA);
										}
									}
									if(v.getId()==R.id.gallery_single)
									{
										if(pathList.size()>=10)
										{
											AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
											builder.setMessage("you can upload images smaller than 10");
											builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													dialog.dismiss();
												}
											});
											builder.show();
										}
										else
										{
											Intent intent = new Intent( Intent.ACTION_PICK ) ;
											intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE ) ;
											pd = MyProgressDialog.show(WriteReview.this);
											startActivityForResult( intent, TAKE_GALLERY) ;
										}
									}
									if(v.getId()==R.id.gallery_multi)
									{
										if(pathList.size()>=10)
										{
											AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
											builder.setMessage("you can upload images smaller than 10");
											builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													dialog.dismiss();
												}
											});
											builder.show();
										}
										else
										{
											Intent intent = new Intent(WriteReview.this,Album.class);
											intent.putExtra("nowImageCount", pathList.size());
											pd = MyProgressDialog.show(WriteReview.this);
											startActivityForResult(intent,TAKE_GALLERY_DUAL);
										}
									}
								}
							});
							builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
							builder.show();
						}
						else
						{
							if(v.getId()==R.id.camera)
							{
								if(pathList.size()>=10)
								{
									AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
									builder.setMessage("you can upload images smaller than 10");
									builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									});
									builder.show();
								}
								else
								{
									Intent intent = new Intent();
					                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
					                pd = MyProgressDialog.show(WriteReview.this);
									startActivityForResult(intent, TAKE_CAMERA);
								}
							}
							if(v.getId()==R.id.gallery_single)
							{
								if(pathList.size()>=10)
								{
									AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
									builder.setMessage("you can upload images smaller than 10");
									builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									});
									builder.show();
								}
								else
								{
									Intent intent = new Intent( Intent.ACTION_PICK ) ;
									intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE ) ;
									pd = MyProgressDialog.show(WriteReview.this);
									startActivityForResult( intent, TAKE_GALLERY) ;
								}
							}
							if(v.getId()==R.id.gallery_multi)
							{
								if(pathList.size()>=10)
								{
									AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
									builder.setMessage("you can upload images smaller than 10");
									builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									});
									builder.show();
								}
								else
								{
									Intent intent = new Intent(WriteReview.this,Album.class);
									intent.putExtra("nowImageCount", pathList.size());
									pd = MyProgressDialog.show(WriteReview.this);
									startActivityForResult(intent,TAKE_GALLERY_DUAL);
								}
							}
						}
					}
					if(v.getId()==R.id.video || v.getId()==R.id.gallery_video)
					{
						if(haveImage)
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
							builder.setMessage("want change images to video?");
							builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									haveImage = false;
									bitmapList.clear();
									runOnUiThread(new Runnable(){

										@Override
										public void run() {
											adapter.notifyDataSetChanged();
										}});
									dialog.dismiss();
									if(v.getId()==R.id.video)
									{
										Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
										pd = MyProgressDialog.show(WriteReview.this);
										startActivityForResult(intent,TAKE_VIDEO);
									}
									if(v.getId()==R.id.gallery_video)
									{
										Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
										intent.setType("video/*");
										pd = MyProgressDialog.show(WriteReview.this);
										startActivityForResult(intent, TAKE_GALLERY_VIDEO);
									}
								}
							});
							builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int arg1) {
									dialog.dismiss();
								}
							});
							builder.show();
						}
						else
						{
							if(v.getId()==R.id.video)
							{
								if(pathList.size()>=10)
								{
									System.out.println("여긴 열개 넘었을때잖아?");
								}
								else
								{
									System.out.println("비디오 안넘어감??");
									Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
									pd = MyProgressDialog.show(WriteReview.this);
									startActivityForResult(intent,TAKE_VIDEO);
								}
							}
							if(v.getId()==R.id.gallery_video)
							{
								if(pathList.size()>=10)
								{
									
								}
								else
								{
									Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
									intent.setType("video/*");
									pd = MyProgressDialog.show(WriteReview.this);
									startActivityForResult(intent, TAKE_GALLERY_VIDEO);
								}
							}
						}
					}
				}
			};
			uploadDialog = new Dialog(this);
			uploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			uploadDialog.setContentView(R.layout.gallery_selection);
			uploadDialog.findViewById(R.id.camera).setOnClickListener(clicker);
			uploadDialog.findViewById(R.id.gallery_single).setOnClickListener(clicker);
			uploadDialog.findViewById(R.id.gallery_multi).setOnClickListener(clicker);
			uploadDialog.findViewById(R.id.gallery_video).setOnClickListener(clicker);
			uploadDialog.findViewById(R.id.video).setOnClickListener(clicker);
			uploadDialog.show();
		}
	}
	
	class UploadReview extends AsyncTask<Void,Integer,Void>
	{
		private long totalSize;
		private ProgressDialog writeDialog;
		public UploadReview() {
			super();
		}
		
		

		@Override
		protected void onProgressUpdate(Integer... values) {
			writeDialog.setProgress((int)(values[0]));
			super.onProgressUpdate(values);
		}



		@Override
		protected void onPostExecute(Void result) {
			writeDialog.dismiss();
			super.onPostExecute(result);
		}


		@Override
		protected void onPreExecute() {
			writeDialog = new ProgressDialog(WriteReview.this);
			writeDialog.setMessage("Uploading files... please wait...");
			writeDialog.setIndeterminate(false);
			writeDialog.setMax(100);
			writeDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			writeDialog.setCancelable(false);
			writeDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				String url= IpUtil.getIp() + "writeReview.do";
				HttpPost post = new HttpPost(url); 
				CustomMultiPartEntity entity = new CustomMultiPartEntity(new ProgressListener() {
					
					@Override
					public void transferred(long num) {
						publishProgress((int)((num/(float)totalSize)*100));
					}
				});
				post.addHeader("email", email);
				entity.addPart("email", new StringBody(email,Charset.forName("UTF-8")));
				entity.addPart("logintype", new StringBody(""+loginType,Charset.forName("UTF-8")));
				entity.addPart("writername", new StringBody(""+userName,Charset.forName("UTF-8")));
				entity.addPart("latitude", new StringBody(""+latitude,Charset.forName("UTF-8")));
				entity.addPart("longitude", new StringBody(""+longitude,Charset.forName("UTF-8")));
				entity.addPart("placename",new StringBody(placename,Charset.forName("UTF-8")));
				
				if(addressLine!=null)
					entity.addPart("address", new StringBody(addressLine,Charset.forName("UTF-8")));
				
				EditText write_review_text = (EditText) WriteReview.this.findViewById(R.id.write_review_text);
				entity.addPart("body", new StringBody(""+write_review_text.getText().toString(),Charset.forName("UTF-8")));
				
				entity.addPart("size", new StringBody(""+pathList.size(),Charset.forName("UTF-8")));
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
				entity.addPart("time", new StringBody(""+format.format(new Date()),Charset.forName("UTF-8")));
				if(haveVideo){
					String filename=null;
					for(int i=0;i<pathList.size();i++)
					{ 
						File file = new File(pathList.get(i));
						String length = ""+file.length();
						if(haveVideo)
							filename = format.format(new Date())+"_"+i;
						entity.addPart("filename"+i,new StringBody(filename,Charset.forName("UTF-8")));
						entity.addPart("length"+i,new StringBody(""+length,Charset.forName("UTF-8")));
						FileBody bin = new FileBody(file);
						entity.addPart("images"+i,bin);
						if(haveVideo){
							File oldFile = new File(videoThumbnailPath);
							File newFile = new File(dir+filename+".jpg");
							oldFile.renameTo(newFile);
							videoThumbnailPath=dir+filename+".jpg";
							pathList.add(videoThumbnailPath);
							haveVideo=false;
							entity.addPart("isvideo",new StringBody("true",Charset.forName("UTF-8")));
						}
					}
				}else{
					for(int i=0;i<pathList.size();i++)
					{ 
						File file = new File(pathList.get(i));
						String length = ""+file.length();
						String filename = format.format(new Date())+"_"+i;
						entity.addPart("filename"+i,new StringBody(filename,Charset.forName("UTF-8")));
						entity.addPart("length"+i,new StringBody(""+length,Charset.forName("UTF-8")));
						FileBody bin = new FileBody(file);
						entity.addPart("images"+i,bin);
					}
				}
				totalSize = entity.getContentLength();
				post.setEntity(entity);
				String result = null;
				HttpResponse response = httpClient.execute(post);
				HttpEntity responseEntity = response.getEntity();
	             XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	             XmlPullParser parser = factory.newPullParser();
	             parser.setInput(new InputStreamReader(responseEntity.getContent()));
	             while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                 String name=parser.getName();
	                 System.out.println("name?????"+name);
	                  if ( name != null && name.equals("result"))
	                     result=parser.nextText();
	              }
	             if(result.equals("success"))
	             {
	            	 finish();
	             }
	             else if(result.equals("failed"))
	             {
	            	 AlertDialog.Builder builder = new AlertDialog.Builder(WriteReview.this);
	            	 builder.setMessage("write failed");
	            	 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
	             }
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
	
	
	public Bitmap rotateAndSave(Bitmap bitmap, String path) {
		if (bitmap != null) {
			Matrix m = new Matrix();
			int degrees = 0;
			ExifInterface exif = null;
			try{
				exif = new ExifInterface(path);
			}catch(Exception e){
				e.printStackTrace();
			}
			boolean flag = false;
			if(exif != null){
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
				if(orientation!=-1){
					switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						System.out.println("케이스 1");
						degrees = 90;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						System.out.println("케이스 2");
						flag=true;
						degrees = 180;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						System.out.println("케이스 3");
						degrees = 270;
						break;
					}
				}
			}
			m.setRotate(degrees);
			try {
				System.out.println((flag?bitmap.getWidth():bitmap.getHeight()) + " x "+(flag? bitmap.getHeight() : bitmap.getWidth()));
				
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(),bitmap.getHeight(), m, true);
				if (bitmap != converted) {
					bitmap = null;
					bitmap = converted;
					converted = null;
				}
			} catch (OutOfMemoryError ex) {
				Toast.makeText(getApplicationContext(), "메모리부족", 0).show();
			}
			File copyFile = new File(path);

			OutputStream out = null;
			        
			try {
			            
			    copyFile.createNewFile();
			    out = new FileOutputStream(copyFile);
			            
			    if ( bitmap.compress(CompressFormat.JPEG, 100, out) ){
			    	System.out.println("잘 됨 ");
			    }
			            
			    } catch (Exception e) {         
			    e.printStackTrace();
			    } finally {
			         try {
			             out.close();
			         } catch (IOException e) {
			             e.printStackTrace();
			    }
			}
		}
		return bitmap;
	}
	
	public void getVideoThumbnail(String videoPath){
		Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
		write_review_video.setImageBitmap(videoThumbnail);
		System.out.println(videoPath);
		String[] tmp = videoPath.split("/");
		dir = "/";
		for(int i=1; i<tmp.length-1; i++){
			dir+=tmp[i]+"/";
		}
		System.out.println("dir : "+dir);
		videoThumbnailPath =dir+tmp[tmp.length-1].split("[.]")[0]+".jpg";
		System.out.println("filePath = "+videoThumbnailPath);
		
		File fileCacheItem = new File(videoThumbnailPath);
		OutputStream out = null;
		try{
			fileCacheItem.createNewFile();
			out=new FileOutputStream(fileCacheItem);
			if(videoThumbnail.compress(CompressFormat.JPEG, 100, out)){
				System.out.println("썸네일 만들어짐");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(out!=null){
					out.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(fileCacheItem.exists()){
			System.out.println("파일 존재!");
		}
	}
	
	class FindUser extends AsyncTask<Void, Void, Void>{
		String findEmail;
		User user;
		public FindUser(String findEmail)
		{
			this.findEmail = findEmail;
			user = new User();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			email = user.getEmail();
			userName = user.getName();
			password = user.getPassword();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			String result = null;
			
			try {
				HttpClient client = new DefaultHttpClient();
				String url = IpUtil.getIp()+"userInfo.do";
				HttpPost post = new HttpPost(url);
				List info = new ArrayList();
				info.add(new BasicNameValuePair("email",email));
				post.setEntity(new UrlEncodedFormEntity(info,HTTP.UTF_8));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
	            XmlPullParser parser = factory.newPullParser();
	            parser.setInput(new InputStreamReader(entity.getContent()));
	            while ( parser.next() != XmlPullParser.END_DOCUMENT) {
	                String name=parser.getName();
	                System.out.println("name?????"+name);
	                if( name!= null && name.equals("name"))
	               	  user.setName(parser.nextText());
	                else if( name!= null && name.equals("email"))
	                	user.setEmail(parser.nextText());
	                else if( name!= null && name.equals("password"))
	                	user.setPassword(password = parser.nextText());
	             }
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

	}
}	
