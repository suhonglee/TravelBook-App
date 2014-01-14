package com.travelbook.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.http.client.CircularRedirectException;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

//����
public class EditImageActivity extends GraphicsActivity implements
		OnClickListener, SeekBar.OnSeekBarChangeListener {
	private final int TAKE_GALLERY = 2000;
	private MyView img;
	private ImageView polo;
	private Bitmap bm;
	private Paint mPaint;
	private Uri currImageURI;
	private Calendar calendar;
	private int year;
	private int month;
	private int day;
	private int min;
	private int second;
	private OnColorChangedListener mListener;
	private String path;
	private SeekBar penSize;
	private int progress;
	private int co;
	private int number;
	private Drawable dr;
	private ColorPickerDialog d;
	private Intent intent;
	private Context con;
	private ArrayList<ArrayList<Vertex>> arVertexList = new ArrayList<ArrayList<Vertex>>();
	private ArrayList<Integer> strokeWidthList = new ArrayList<Integer>();
	private ArrayList<Vertex> arVertex;
	private Button btn_clear, btn_color, btn_line_size, btn_save, btn_option, haha;
	private LinearLayout select_color_layout;
	private RelativeLayout imageLayouts;
	private ImageView image, imgBlack, imgBlue, imgRed, imgDeepblue, imgDeepgreen, imgDeeporange,
			imgGreen, imgOrange, imgPink, imgPurple, imgSkyblue, imgWhite, imgYellow, imgPalette;
	private Bitmap bitmap;
	private boolean isPola = true;
	
	private ImageView imgShadow;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_draw_activity);
		btn_clear = (Button) findViewById(R.id.btn_clear);
		btn_color = (Button) findViewById(R.id.btn_color);
		btn_line_size = (Button) findViewById(R.id.btn_line_size);
		btn_save = (Button) findViewById(R.id.btn_save);
		btn_option = (Button) findViewById(R.id.btn_option);
		
		imgBlack = (ImageView) findViewById(R.id.black);
		imgBlue  = (ImageView) findViewById(R.id.blue);
		imgRed = (ImageView) findViewById(R.id.red);
		imgDeepblue = (ImageView) findViewById(R.id.deepblue);
		imgDeepgreen = (ImageView) findViewById(R.id.deepgreen);
		imgDeeporange = (ImageView) findViewById(R.id.deeporange);
		imgGreen = (ImageView) findViewById(R.id.green);
		imgOrange = (ImageView) findViewById(R.id.orange);
		imgPink = (ImageView) findViewById(R.id.pink);
		imgPurple = (ImageView) findViewById(R.id.purple);
		imgSkyblue = (ImageView) findViewById(R.id.skyblue);
		imgWhite = (ImageView) findViewById(R.id.white);
		imgYellow = (ImageView) findViewById(R.id.yellow);
		imgPalette = (ImageView) findViewById(R.id.palette);
		
		penSize = (SeekBar) findViewById(R.id.penSize);
		penSize.setOnSeekBarChangeListener(this);
		penSize.setProgress(50);
		
		select_color_layout = (LinearLayout) findViewById(R.id.select_color_layout);
		
		calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DATE);
		min = calendar.get(Calendar.MINUTE);
		second = calendar.get(Calendar.SECOND);
		imageLayouts = (RelativeLayout) findViewById(R.id.image_layout);
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth(); 
		int height = display.getHeight();
		
		img = new MyView(this);
		img.setLayoutParams(new LayoutParams(width, height));
		img.setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeFile(getIntent().getStringExtra("path"))));
		
		polo = new ImageView(this);
		polo.setBackgroundResource(R.drawable.frame);
		polo.setVisibility(View.GONE);
		imageLayouts.addView(img);
		imageLayouts.addView(polo);
	}

	protected void onResume() {
		super.onResume();
		
		arVertex = new ArrayList<Vertex>();
		arVertexList.add((ArrayList<Vertex>) arVertex);
		arVertex.clear();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public class Vertex {
		float x;
		float y;
		boolean Draw;
		int color;

		Vertex(float ax, float ay, boolean ad, int color) {
			x = ax;
			y = ay;
			Draw = ad;
			this.color = color;
		}
	}

	protected class MyView extends ImageView implements OnClickListener,
			OnColorChangedListener {
		boolean clear;

		

		public MyView(Context context) {
			super(context);
			con = context;
			mPaint = new Paint();
			mPaint.setStrokeWidth(5);
			strokeWidthList.add(5);
			mPaint.setAntiAlias(true);
			setBackgroundColor(Color.WHITE);
			
			clear = false;
			co = Color.BLACK;
			btn_clear.setText("x");
			btn_clear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					invalidate();
					for(int i = 0; i < arVertexList.size() ; i++){
						arVertexList.get(i).clear();
					}
				}
			});

			btn_color.setOnClickListener(this);
			btn_line_size.setOnClickListener(this);
			btn_save.setOnClickListener(this);
			btn_save.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					imageLayouts.setDrawingCacheEnabled(true);
					bm = imageLayouts.getDrawingCache();
					showSubScreen(bm);
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+Environment.getExternalStorageDirectory().getPath()
							+ "/TravelBook/"+year+month+day+min+second+".jpg")));
					
					Intent data = new Intent()
								.putExtra("filename", Environment.getExternalStorageDirectory().getPath()+"/TravelBook/"+year+month+day+min+second+".jpg")
								.putExtra("position", getIntent().getIntExtra("position",0));
					setResult(5844824, data);
					finish();
					((BitmapDrawable)img.getBackground()).getBitmap().recycle();
				}
			});
			btn_option.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isPola) {
						polo.setVisibility(View.VISIBLE);
						isPola = false;
					} else {
						polo.setVisibility(View.GONE);
						isPola = true;
					}
				}
			});
			
			imgBlack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					co = Color.BLACK;
					number = 0;
					colorImageChanged(number);
				}
			});
			
			imgBlue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.BLUE;
					number = 1;
					colorImageChanged(number);
				}
			});
			
			imgRed.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.RED;
					number = 2;
					colorImageChanged(number);
				}
			});
			
			imgDeepblue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(3, 0, 102);
					number = 3;
					colorImageChanged(number);
				}
			});
			
			imgDeepgreen.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(34, 116, 28);
					number = 4;
					colorImageChanged(number);
				}
			});
			
			imgDeeporange.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(226, 83, 51);
					number = 5;
					colorImageChanged(number);
				}
			});
			
			imgGreen.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.GREEN;
					number = 6;
					colorImageChanged(number);
				}
			});
			
			imgOrange.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(247, 147, 35);
					number = 7;
					colorImageChanged(number);
				}
			});
			
			imgPink.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(237, 24, 108);
					number = 8;
					colorImageChanged(number);
				}
			});
			
			imgPurple.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(166,20,197);
					number = 9;
					colorImageChanged(number);
				}
			});
			
			imgSkyblue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.rgb(27, 166, 159);
					number = 10;
					colorImageChanged(number);
				}
			});
			
			imgWhite.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.WHITE;
					number = 11;
					colorImageChanged(number);
				}
			});
			
			imgYellow.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					co = Color.YELLOW;
					number = 12;
					colorImageChanged(number);
				}
			});
			imgPalette.setOnClickListener(this);
		}

		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			for (int i = 0; i < arVertexList.size(); i++) {
				for (int j = 0; j < arVertexList.get(i).size(); j++) {
					if (arVertexList.get(i).get(j).Draw) {
						mPaint.setColor(arVertexList.get(i).get(j).color);
						mPaint.setStrokeWidth(strokeWidthList.get(i));
						canvas.drawLine(arVertexList.get(i).get(j - 1).x,
								arVertexList.get(i).get(j - 1).y, arVertexList
										.get(i).get(j).x, arVertexList.get(i)
										.get(j).y, mPaint);
					}
				}
			}
		}

		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				arVertex.add(new Vertex(event.getX(), event.getY(), false, co));
				return true;
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				arVertex.add(new Vertex(event.getX(), event.getY(), true, co));
				invalidate();//
				return true;
			}
			return false;
		}

		@Override
		public void onClick(View v) {
			if (v == btn_clear) {
				arVertex.clear();
				invalidate();
			} else if (v == imgPalette) {
				System.out.println(con);
				d = new ColorPickerDialog(con, this, mPaint.getColor());
				d.show();
				
			} else if (v == btn_line_size) {
				select_color_layout.setVisibility(View.GONE);
				penSize.setVisibility(View.VISIBLE);

			} else if (v == btn_color) {
				select_color_layout.setVisibility(View.VISIBLE);
				penSize.setVisibility(View.GONE);
			}

		}

		@Override
		public void colorChanged(int color) {
			
		}

	}

	private void showSubScreen(Bitmap bm) {
		try {
			File path = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/TravelBook/");
			if (!path.isDirectory()) {
				path.mkdirs();
			}
			path = new File(Environment.getExternalStorageDirectory().getPath()
					+ "/TravelBook/"+year+month+day+min+second+".jpg");
			path.createNewFile();
			FileOutputStream out = new FileOutputStream(path);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();
		} catch (FileNotFoundException e) {
			Log.d("FileNotFoundException:", e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//////////////////////////////////////////////////////////////////
	public interface OnColorChangedListener {
		void colorChanged(int color);
	}

	public class ColorPickerDialog extends Dialog {

		
		private int mInitialColor;
		private class ColorPickerView extends View {
		private Paint mPaint;
		private Paint mCenterPaint;
		private final int[] mColors;
		private OnColorChangedListener mListener;

		ColorPickerView(Context c, OnColorChangedListener l, int color) {
			super(c);
			
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			mListener = l;
			mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
					0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
			Shader s = new SweepGradient(0, 0, mColors, null);
			
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setShader(s);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(32);

			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(color);
			mCenterPaint.setStrokeWidth(5);
			
		}

		private boolean mTrackingCenter;
		private boolean mHighlightCenter;

		@Override
		protected void onDraw(Canvas canvas) {
			float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;

			canvas.translate(CENTER_X, CENTER_X);

			canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
			canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

			if (mTrackingCenter) {
				int c = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);
				

				if (mHighlightCenter) {
					mCenterPaint.setAlpha(0xFF);
				} else {
					mCenterPaint.setAlpha(0x80);
				}
				canvas.drawCircle(0, 0,
						CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
						mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(c);
				
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
		}

		private static final int CENTER_X = 100;
		private static final int CENTER_Y = 100;
		private static final int CENTER_RADIUS = 32;

		private int floatToByte(float x) {
			int n = java.lang.Math.round(x);
			return n;
		}

		private int pinToByte(int n) {
			if (n < 0) {
				n = 0;
			} else if (n > 255) {
				n = 255;
			}
			return n;
		}

		private int ave(int s, int d, float p) {
			return s + java.lang.Math.round(p * (d - s));
		}

		private int interpColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i + 1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);

		}

		private int rotateColor(int color, float rad) {
			float deg = rad * 180 / 3.1415927f;
			int r = Color.red(color);
			int g = Color.green(color);
			int b = Color.blue(color);

			ColorMatrix cm = new ColorMatrix();
			ColorMatrix tmp = new ColorMatrix();

			cm.setRGB2YUV();
			tmp.setRotate(0, deg);
			cm.postConcat(tmp);
			tmp.setYUV2RGB();
			cm.postConcat(tmp);
			final float[] a = cm.getArray();

			int ir = floatToByte(a[0] * r + a[1] * g + a[2] * b);
			int ig = floatToByte(a[5] * r + a[6] * g + a[7] * b);
			int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

			return Color.argb(Color.alpha(color), pinToByte(ir),
					pinToByte(ig), pinToByte(ib));
		}

		private static final float PI = 3.1415926f;

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - CENTER_X;
			float y = event.getY() - CENTER_Y;
			boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= CENTER_RADIUS;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTrackingCenter = inCenter;
				if (inCenter) {
					mHighlightCenter = true;
					invalidate();
					break;
				}
			case MotionEvent.ACTION_MOVE:
				if (mTrackingCenter) {
					if (mHighlightCenter != inCenter) {
						mHighlightCenter = inCenter;
						invalidate();
					}
				} else {
					float angle = (float) java.lang.Math.atan2(y, x);
					// need to turn angle [-PI ... PI] into unit [0....1]
					float unit = angle / (2 * PI);
					if (unit < 0) {
						unit += 1;
					}
					mCenterPaint.setColor(interpColor(mColors, unit));
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mTrackingCenter) {
					if (inCenter) {
						mListener.colorChanged(mCenterPaint.getColor());
						co = mCenterPaint.getColor();
						System.out.println("�̰Ű���");
					}
					mTrackingCenter = false; // so we draw w/o halo
					invalidate();
				}
				break;
			}
			return true;
		}
	}

	public ColorPickerDialog(Context context,
			OnColorChangedListener listener, int initialColor) {
		super(context);

		mListener = listener;
		mInitialColor = initialColor;
		
	}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			OnColorChangedListener l = new OnColorChangedListener() {
				public void colorChanged(int color) {
					mListener.colorChanged(color);
					
					dismiss();
				}
			};
			getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			setContentView(new ColorPickerView(getContext(), l, mInitialColor));
			
		}
	}


	@Override
	public void onClick(View v) {
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		this.progress = progress;
		arVertex = new ArrayList<EditImageActivity.Vertex>();
		arVertexList.add((ArrayList<Vertex>) arVertex);
		strokeWidthList.add(progress / 10);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	public void colorImageChanged(int number) {
		
		ImageView[] color = {imgBlack, imgBlue, imgRed, imgDeepblue, imgDeepgreen, imgDeeporange,
				imgGreen, imgOrange, imgPink, imgPurple, imgSkyblue, imgWhite, imgYellow};
		int[] colorClick = { R.drawable.black_clicked, R.drawable.blue_clicked, R.drawable.red_clicked,
				R.drawable.deepblue_clicked, R.drawable.deepgreen_clicked, R.drawable.deeporange_clicked,
				R.drawable.green_clicked, R.drawable.orange_clicked, R.drawable.pink_clicked,
				R.drawable.purple_clicked, R.drawable.skyblue_clicked, R.drawable.white_clicked, R.drawable.yellow_clicked};
		int[] colorBeforeClick = { R.drawable.black_normal, R.drawable.blue_normal, R.drawable.red_normal,
				R.drawable.deepblue_normal, R.drawable.deepgreen_normal, R.drawable.deeporange_normal,
				R.drawable.green_normal, R.drawable.orange_normal, R.drawable.pink_normal,
				R.drawable.purple_normal, R.drawable.skyblue_normal, R.drawable.white_normal, R.drawable.yellow_normal };

		for (int i = 0; i < color.length; i++) {
			color[i].setImageResource(colorBeforeClick[i]);
			if (i == number) {
				color[i].setImageResource(colorClick[i]);
			}
		}
	}

	public static String getPath(Activity activity, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity
				.managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	public void onBackPressed() {
		if(d!=null)
		{
			if(d.isShowing())
			{
				d.dismiss();
			}
		}
		else
		{
			super.onBackPressed();
		}
	}


	
}
