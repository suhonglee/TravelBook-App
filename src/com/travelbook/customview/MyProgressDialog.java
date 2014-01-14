package com.travelbook.customview;


import com.travelbook.activities.R;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class MyProgressDialog extends Dialog { 
 
 
	public static MyProgressDialog show(Context context) {
	 MyProgressDialog dialog = new MyProgressDialog(context); 
     dialog.setCancelable(false); 
     /* The next line will add the ProgressBar to the dialog. */ 
     dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
     dialog.show(); 
  
     return dialog; 
	}
	
	public static MyProgressDialog show(Context context,boolean tt) {
		 MyProgressDialog dialog = new MyProgressDialog(context); 
	     dialog.setCancelable(true); 
	     /* The next line will add the ProgressBar to the dialog. */ 
	     dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
	     dialog.show(); 
	  
	     return dialog; 
		}
public static MyProgressDialog show(Context context, CharSequence title, 
         CharSequence message) { 
     return show(context, title, message, false); 
 } 
  
 public static MyProgressDialog show(Context context, CharSequence title, 
         CharSequence message, boolean indeterminate) { 
     return show(context, title, message, indeterminate, false, null); 
 } 
  
 public static MyProgressDialog show(Context context, CharSequence title, 
         CharSequence message, boolean indeterminate, boolean cancelable) { 
     return show(context, title, message, indeterminate, cancelable, null); 
 } 
 
  
 public static MyProgressDialog show(Context context, CharSequence title, 
         CharSequence message, boolean indeterminate, 
         boolean cancelable, OnCancelListener cancelListener) { 
     MyProgressDialog dialog = new MyProgressDialog(context); 
     dialog.setTitle(title); 
     dialog.setCancelable(cancelable); 
     dialog.setOnCancelListener(cancelListener); 
     /* The next line will add the ProgressBar to the dialog. */ 
     dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
     dialog.show(); 
  
     return dialog; 
 } 
  
 public MyProgressDialog(Context context) { 
     super(context, R.style.NewDialog); 
 } 
}