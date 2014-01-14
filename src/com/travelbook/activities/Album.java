package com.travelbook.activities;


import java.util.ArrayList;

import com.travelbook.holder.ThumbImageInfo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Album extends Activity{
	private int TAKE_GALLERY_DUAL = 58445882;
	ArrayList<String> arr = new ArrayList<String>();
	ArrayList<String> folders = new ArrayList<String>();
	ArrayList<String> directorys = new ArrayList<String>();
	ArrayList<ThumbImageInfo> mThumbImageInfoList;

	Cursor imageCursor;
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == TAKE_GALLERY_DUAL)
		{
			if(data!=null)
			{
				if(data.getStringArrayListExtra("images")!=null&&data.getStringArrayListExtra("path")!=null)
				{
					setResult(TAKE_GALLERY_DUAL, data.putStringArrayListExtra("images", data.getStringArrayListExtra("images")));
					setResult(TAKE_GALLERY_DUAL, data.putStringArrayListExtra("path", data.getStringArrayListExtra("path")));
				}
			}
			finish();
		}
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album);
		mThumbImageInfoList=new ArrayList<ThumbImageInfo>();
		ListView albumList = (ListView) findViewById(R.id.album_list);
		findFolderList();
		albumList.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1, folders));
		
		albumList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				findThumbList(position);
				Intent i = new Intent(Album.this, AlbumMain.class);
				i.putParcelableArrayListExtra("ss", (ArrayList<? extends Parcelable>) mThumbImageInfoList);
				i.putStringArrayListExtra("folders", folders);
				i.putExtra("folderPosition", position);
				i.putExtra("nowImageCount", getIntent().getIntExtra("nowImageCount", 0));
				startActivityForResult(i, TAKE_GALLERY_DUAL);
			}
		
		});
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}



	private void findFolderList(){
		String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
	    
	    // 쿼리 수행
	    imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " desc ");
	    if (imageCursor != null && imageCursor.getCount() > 0) 
	    {
	      // 컬럼 인덱스
	      int imageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID); 
	      int imageDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
	      // 커서에서 이미지의 ID와 경로명을 가져와서 ThumbImageInfo 모델 클래스를 생성해서
	      // 리스트에 더해준다.
	      while (imageCursor.moveToNext())
	      {
			String[] sdf = imageCursor.getString(imageDataCol).split("/");
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<sdf.length-1; i++){
				sb.append(sdf[i]+"/");
				//startWith로 비교할 경로
			}
			
			if(directorys.size()==0){
				directorys.add(sb.toString());
				folders.add(sdf[sdf.length-2]);
			}else{
				int i=0;
				for(; i<directorys.size(); i++){
					if(directorys.get(i).equals(sb.toString())){
						break;
					}
				}
				if(folders.size()==i){
					directorys.add(sb.toString());
					folders.add(sdf[sdf.length-2]);
					System.out.println(sb.toString());
				}
			}
	      }
	    }
	}
	
	private long findThumbList(int position)
	  {
	    long returnValue = 0;
	    
	    // Select 하고자 하는 컬럼
	    String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
	    
	    // 쿼리 수행
	    imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " desc ");
	    if (imageCursor != null && imageCursor.getCount() > 0) 
	    {
	      // 컬럼 인덱스
	      int imageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID); 
	      int imageDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
	      // 커서에서 이미지의 ID와 경로명을 가져와서 ThumbImageInfo 모델 클래스를 생성해서
	      // 리스트에 더해준다.
	      while (imageCursor.moveToNext())
	      {
			if(imageCursor.getString(imageDataCol).startsWith(directorys.get(position))){
				ThumbImageInfo thumbInfo = new ThumbImageInfo();
				thumbInfo.setId(imageCursor.getString(imageIDCol));
				thumbInfo.setData(imageCursor.getString(imageDataCol));
				thumbInfo.setCheckedState(false);
				mThumbImageInfoList.add(thumbInfo);
				
				returnValue++;
			}
	      }
	    }
	    return returnValue;
	  }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		imageCursor.close();
	}
	
	
	
	
	
}
