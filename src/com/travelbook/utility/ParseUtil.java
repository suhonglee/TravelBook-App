package com.travelbook.utility;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.Html;

public class ParseUtil {
	public static String parsingData(InputStream input,String keyword){
        String result=null;
        try {
             XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
             XmlPullParser parser = factory.newPullParser();
             parser.setInput(new InputStreamReader(input));
             while ( parser.next() != XmlPullParser.END_DOCUMENT) {
                 String name=parser.getName();
                 System.out.println("name?????"+name);
                  if ( name != null && name.equals(keyword))
                         result=parser.nextText();
              }
         }catch(Exception e){e.printStackTrace();}
         return result;
     }
}
