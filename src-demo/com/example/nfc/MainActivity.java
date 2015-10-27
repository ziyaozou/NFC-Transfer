package com.example.nfc;

import java.util.Random;

import com.weibo.sdk.android.demo.WeiBoActivity;

import android.R.color;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener, OnTouchListener{
	private ImageView weibo;
	private WifiConfig wifiConf;
	private String ssid;
	private String password;
	private int touchId;
	private int oldTouchId;
	private int screenWidth;
	private int screenHeight;
	
	private ImageView clickView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
		.penaltyLog() //打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
		.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects() //探测SQLite数据库操作
		.penaltyLog() //打印logcat
		.penaltyDeath()
		.build()); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);
        
		DisplayMetrics  dm = new DisplayMetrics();
        //取得窗口属性
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        //窗口的宽度
        screenWidth = dm.widthPixels;
        
        //窗口高度
        screenHeight = dm.heightPixels;
        clickView = (ImageView)findViewById( R.id.clickView );
		
        clickView.setOnTouchListener( this );
        clickView.setOnClickListener(this);
		
		ssid = getRandomString();
		password = getRandomString();
		System.out.println( "ssid-> " + ssid + "    pwd-> " + password );
		
		
		wifiConf = new WifiConfig( this,ssid,password);
			
	}
	
	public static String getRandomString() { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";  
	    Random random = new Random();  
	    StringBuffer sb = new StringBuffer();  
	    for (int i = 0; i < 10; i++) {  
	        int number = random.nextInt(base.length());  
	        sb.append(base.charAt(number));  
	    }  
	    return sb.toString();  
	 }  

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		wifiConf.closeWifiConnect();
		wifiConf.closeWifiAp();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void setTouchId( MotionEvent arg1 ){
		float x = arg1.getRawX()-screenWidth/2;
		float y = arg1.getRawY()-screenHeight/2;
		oldTouchId = touchId;
		touchId = 0;
		if(x*x + y*y>1600 && x*x + y*y < 180*180 ){
			double k = getK( arg1.getRawX(), arg1.getRawY() );
			if( k>-1/2 && k<1 ){
				if( x>0 ){
					touchId = R.id.card;
				}else{
					touchId = R.id.followOthers;
				}
			}else{
				touchId = R.id.file;
			}
		}
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
		System.out.println( "touched......"+arg1.getAction());
		
		setTouchId( arg1 );
		System.out.println( "touchId-> " + touchId + "  oldId-> " + oldTouchId);
		
		switch( arg1.getAction() ){
		case MotionEvent.ACTION_DOWN:
			if( touchId != oldTouchId ){
				System.out.println( "oldtouchid.down.." + oldTouchId );
				if( oldTouchId!=0 ){
					smallImageView( oldTouchId );
				}
				
				if( touchId!=0 ){
					bigImageView( touchId );
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			System.out.println( "action up..");
			if( touchId!=0 ){
				smallImageView( touchId );
			}
			
			System.out.println( "oldtouchid.up.." + oldTouchId );
			switchActivity( touchId );
			break;
		}
		return true;
	}

	private double getK( float x, float y ){
		return (y-screenHeight/2) / Math.sqrt( (x-screenWidth/2)*(x-screenWidth/2)
				+(y-screenHeight/2)*(y-screenHeight/2) );
	}
	private void smallImageView(int id) {
		// TODO Auto-generated method stub
		ImageView img = (ImageView )findViewById( id );
		if( id==R.id.file ){
			img.scrollBy(0, 10 );
		}else if( id==R.id.followOthers ){
			img.scrollBy(8, -5 );
		}else if( id==R.id.card ){
			img.scrollBy(-8, -5);
		}
		clickView.getParent().bringChildToFront(clickView);
	}

	private void bigImageView(int id) {
		// TODO Auto-generated method stub
		ImageView img = (ImageView )findViewById( id );
		if( id==R.id.file ){
			img.scrollBy(0, -10 );
		}else if( id==R.id.followOthers ){
			img.scrollBy(-8, 5 );
		}else if( id==R.id.card ){
			img.scrollBy(8, 5);
		}
		clickView.getParent().bringChildToFront(clickView);
	}

	private void switchActivity( int id ){
		oldTouchId = touchId = 0;
		Intent intent;
		switch( id ){
		case R.id.file:
			wifiConf.startWifiAp();
			intent = new Intent();
			intent.putExtra("name", ssid);
			intent.putExtra("password", password );
			intent.setClass(MainActivity.this,FileListActivity.class );
			startActivity(intent);
			break;
		case R.id.followOthers:
			intent = new Intent();
			intent.setClass(MainActivity.this,WeiBoActivity.class );
			startActivity(intent);
			break;
		case R.id.card:
			intent = new Intent();
			intent.setClass(MainActivity.this,ContactsActivity.class );
			startActivity(intent);
			break;
		}
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	}

}
