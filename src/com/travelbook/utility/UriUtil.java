package com.travelbook.utility;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class UriUtil {
	// 실제 경로 찾기
		public static String getPath(Activity activity , Uri uri)
		{
		    String[] projection = { MediaStore.Images.Media.DATA };
		    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		}
		 
		// 파일명 찾기
		public static String getName(Activity activity ,Uri uri)
		{
		    String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME };
		    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		    int column_index = cursor
		            .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		}
		 
		// uri 아이디 찾기
		public static String getUriId(Activity activity ,Uri uri)
		{
		    String[] projection = { MediaStore.Images.ImageColumns._ID };
		    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		}
}
