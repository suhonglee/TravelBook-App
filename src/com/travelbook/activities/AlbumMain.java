package com.travelbook.activities;


import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.travelbook.cache.DrawableCache;
import com.travelbook.holder.ThumbImageInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout;

public class AlbumMain extends Activity implements ListView.OnScrollListener, GridView.OnItemClickListener
{
	private int TAKE_GALLERY_DUAL = 58445882;
  boolean mBusy = false;
  ProgressDialog mLoagindDialog;
  GridView mGvImageList;
  ImageAdapter mListAdapter;
  ArrayList<ThumbImageInfo> mThumbImageInfoList;
  ArrayList<ThumbImageInfo> showThumbImages;
  LayoutInflater mLayoutInflater;
  ArrayList<String> rowDatas = new ArrayList<String>();
  ThumbImageInfo rowData;
  
  ImageViewHolder imgHolder;
  Vector<Bitmap> bmps= new Vector<Bitmap>();
  Bitmap bmp;
  Vector<View> views = new Vector<View>(); 
  
  ArrayList<String> rowDatasPath = new ArrayList<String>();
  ArrayList<String> folders = new ArrayList<String>();
  Button btn;
  RelativeLayout relView;
  int check;
  int maxCheck;
  Intent resultIntent = new Intent();
  static final int VISIBLE = 0x00000000; 
  static final int INVISIBLE = 0x00000004; 
  
  
//  DoFindImageList list;
  int j;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.image_list_view);
    
    maxCheck = 10 - (getIntent().getIntExtra("nowImageCount", 0));
    System.out.println(maxCheck+"泥댄겕媛�닔!!!!");
    btn = (Button) findViewById(R.id.confirm);
    
    btn.setText("�뺤씤("+0+"/"+maxCheck+")");
    btn.setClickable(false);
    btn.setTextColor(Color.GRAY);
    
    mThumbImageInfoList = getIntent().getParcelableArrayListExtra("ss");
    folders = getIntent().getStringArrayListExtra("folders");
    showThumbImages = new ArrayList<ThumbImageInfo>();
    mGvImageList = (GridView)findViewById(R.id.gvImageList);
    mGvImageList.setOnScrollListener(this);
    mGvImageList.setOnItemClickListener(this);
    
    
    
    
	for(int i=0; i<mThumbImageInfoList.size(); i++){
	  showThumbImages.add(mThumbImageInfoList.get(i));
	}
	View tmpView=null;
	LayoutInflater mLiInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	for(int i=0; i<showThumbImages.size(); i++){
		tmpView=null;
		if (tmpView == null)
	      {
			tmpView = mLiInflater.inflate(R.layout.image_cell,null, false);
	        ImageViewHolder holder = new ImageViewHolder();
	        
	        holder.ivImage = (ImageView) tmpView.findViewById(R.id.ivImage);
	        holder.chkImage = (CheckBox) tmpView.findViewById(R.id.chkImage);
	        
	        tmpView.setTag(holder);
	      }
	      views.add(tmpView);
	}
	
	updateUI();
    
    new DoFindImageList(0).execute();
  }  
  // �붾㈃���대�吏�뱾��肉뚮젮以�떎.
  private void updateUI()
  {
    mListAdapter = new ImageAdapter (this, R.layout.image_cell, showThumbImages);
    mGvImageList.setAdapter(mListAdapter);
  }
  
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
  {}

  // �ㅽ겕濡��곹깭瑜��먮떒�쒕떎.
  // �ㅽ겕濡��곹깭媛�IDLE ��寃쎌슦(mBusy == false)�먮쭔 �대�吏��대뙌�곗쓽 getView�먯꽌
  // �대�吏�뱾��異쒕젰�쒕떎.
  public void onScrollStateChanged(AbsListView view, int scrollState)
  {
    switch (scrollState)
    {
  
      case OnScrollListener.SCROLL_STATE_IDLE:
        mBusy = false;
        
        mListAdapter.notifyDataSetChanged();
        
        break;
  
      case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        mBusy = true;
    
        break;

      case OnScrollListener.SCROLL_STATE_FLING:
        mBusy = true;
        
        break;
    }
  }
  
  // �꾩씠��泥댄겕���꾩옱 泥댄겕�곹깭瑜�媛�졇��꽌 諛섎�濡�蹂�꼍(true -> false, false -> true)�쒗궎怨�  // 洹�寃곌낵瑜��ㅼ떆 ArrayList��媛숈� �꾩튂���댁븘以�떎
  // 洹몃━怨��대뙌�곗쓽 notifyDataSetChanged() 硫붿꽌�쒕� �몄텧�섎㈃ 由ъ뒪�멸� �꾩옱 蹂댁씠��  // 遺�텇���붾㈃���ㅼ떆 洹몃━湲��쒖옉�섎뒗��getView �몄텧) �대윭硫댁꽌 蹂�꼍��泥댄겕�곹깭瑜�
  // �뚯븙�섏뿬 泥댄겕諛뺤뒪��泥댄겕/�몄껜�щ� 泥섎━�쒕떎. 
  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
  {
    ImageAdapter adapter = (ImageAdapter) arg0.getAdapter();
    rowData = (ThumbImageInfo)adapter.getItem(position);
    boolean curCheckState = rowData.getCheckedState();
    
    //select
    if(check<maxCheck&&!rowData.getCheckedState()){
	    rowData.setCheckedState(!curCheckState);
	    mThumbImageInfoList.set(position, rowData);
	    rowDatas.add(rowData.getId());
	    rowDatasPath.add(rowData.getData());
	    check++;       
    }
    //unselect
    else if(rowData.getCheckedState()){    
    	rowData.setCheckedState(!curCheckState);
    	mThumbImageInfoList.set(position, rowData);
    	rowDatas.remove(rowData.getId());
    	rowDatasPath.remove(rowData.getData());
    	check--;
    	System.out.println(check);
    }
    if(check==0){
    	btn.setClickable(false);
    	btn.setEnabled(false);
    	btn.setOnClickListener(null);
    	btn.setTextColor(Color.GRAY);
    }else{
    	btn.setClickable(true);
    	btn.setEnabled(true);
    	btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(TAKE_GALLERY_DUAL, resultIntent.putStringArrayListExtra("images", rowDatas));
				setResult(TAKE_GALLERY_DUAL, resultIntent.putStringArrayListExtra("path", rowDatasPath));
				AlbumMain.this.finish();
			}
		});
    	btn.setTextColor(Color.BLACK);
    }
    btn.setText("�뺤씤("+check+"/"+maxCheck+")");
    
    adapter.notifyDataSetChanged();
  }
  
  // ***************************************************************************************** //
  // Image Adapter Class 
  // ***************************************************************************************** //
  static class ImageViewHolder
  {
    ImageView ivImage;
    CheckBox chkImage;
  }
  
  private class ImageAdapter extends BaseAdapter
  {
    static final int VISIBLE = 0x00000000; 
    static final int INVISIBLE = 0x00000004; 
    private Context mContext;
    private int mCellLayout;
    private LayoutInflater mLiInflater;
    private ArrayList<ThumbImageInfo> mThumbImageInfoList;
    
    
    public ImageAdapter(Context c, int cellLayout, ArrayList<ThumbImageInfo> thumbImageInfoList)
    {
      mContext = c;
      mCellLayout = cellLayout;
      mThumbImageInfoList = thumbImageInfoList;
      
      mLiInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      
    }
    public int getCount()
    {
    	System.out.println();
      return mThumbImageInfoList.size();
    }

    public Object getItem(int position)
    {
      return mThumbImageInfoList.get(position);
    }

    public long getItemId(int position)
    {
      return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
    	final ImageViewHolder holder = (ImageViewHolder) views.get(position).getTag();
        
        if (((ThumbImageInfo) mThumbImageInfoList.get(position)).getCheckedState())
          holder.chkImage.setChecked(true);
        else
          holder.chkImage.setChecked(false);

      return views.get(position);
    }
  }
  // ***************************************************************************************** //
  // Image Adapter Class End
  // ***************************************************************************************** //
  
  // ***************************************************************************************** //
  // AsyncTask Class 
  // ***************************************************************************************** //
  private class DoFindImageList extends AsyncTask<String, Integer, Long>
  {
	  
	  int cnt=0;
	  SoftReference<Drawable> softRef;
	  
	  
	  
    public DoFindImageList(int cnt) {
		super();
		this.cnt = cnt;
	}

	@Override
    protected void onPreExecute()
    {
      super.onPreExecute();
    }
    
    @Override
    protected Long doInBackground(String... arg0)
    {
    	
		String path = ((ThumbImageInfo) mThumbImageInfoList.get(cnt)).getData();
		//�대�吏�퉴吏�쓽 濡쒖뺄 �⑥뒪 媛�졇��        // bmp 媛앹껜瑜�罹먯돩�먯꽌 媛�졇 �붾떎硫�洹멸쾬��洹몃�濡��ъ슜�섍퀬
    	softRef =new DrawableCache(AlbumMain.this).get(Integer.parseInt(mThumbImageInfoList.get(cnt).getId()));
      return 0l;
    }

    @Override
    protected void onPostExecute(Long result)
    {
    	
    	imgHolder=(ImageViewHolder)views.get(cnt).getTag();
		String path = ((ThumbImageInfo) mThumbImageInfoList.get(cnt)).getData();
		if(softRef!=null && softRef.get()!=null)
			imgHolder.ivImage.setImageDrawable(softRef.get());
		if(cnt+1<views.size())
			new DoFindImageList(cnt+1).execute();
    }

    @Override
    protected void onCancelled()
    {
      super.onCancelled();
    }
  }
  // ***************************************************************************************** //
  // AsyncTask Class End
  // ***************************************************************************************** //
}