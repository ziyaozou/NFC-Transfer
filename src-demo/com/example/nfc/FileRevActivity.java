package com.example.nfc;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class FileRevActivity extends Activity {

	private TextView file_name;
	private TextView fileSize;
	private TextView progress;
	private ImageView fileType;
	private RelativeLayout mLayout;
	private RelativeLayout cLayout;
	private RelativeLayout fLayout;
	private WifiManager wifiManager;
	private DhcpInfo dhcpInfo;
	private String path;
	private String fileName;
	private String filePath;
	private RevHandler revHandler;
	private Handler handler;
	private WifiConfig con;
	private Socket client;
	Thread revThread;
	boolean isStop;
	private String name;
	private String password;
	private String message;

	private CircleProgress mCircleProgressBar1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flipper);
		
		mLayout = (RelativeLayout)findViewById( R.id.get_a_message );
		cLayout = (RelativeLayout)findViewById( R.id.get_a_contact );
		fLayout = (RelativeLayout)findViewById( R.id.get_a_file );

		mCircleProgressBar1 = (CircleProgress) findViewById(R.id.roundBar_rev);
		
		path = "/sdcard/NFC/";
		checkPath();
		
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub

				revThread = new Thread(new RevThread());
				revThread.start();
				super.handleMessage(msg);
			}

		};
		

		file_name = (TextView) findViewById(R.id.file_name_rev_label);
		fileSize = (TextView) findViewById(R.id.file_size_rev_label);
		progress = (TextView)findViewById( R.id.percentage_rev );
		fileType = (ImageView)findViewById( R.id.file_type_rev_label );
		
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

	}
	public void checkPath(){
		File dir = new File(path);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		setIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
		super.onResume();
	}

	private void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String type = new String( msg.getRecords()[0].getPayload());
		
		System.out.println( "type is " + type);
		if( type.equals("key")){
			name = new String(msg.getRecords()[1].getPayload());
			password = new String(msg.getRecords()[2].getPayload());

			System.out.println( "name and key:---> " + name + "  " + password );
			con = new WifiConfig(this, name, password);

			con.connectToWifi();
			fLayout.setVisibility(View.VISIBLE );
			mLayout.setVisibility(View.GONE );
			cLayout.setVisibility(View.GONE );
			
			revHandler = new RevHandler();

			revThread = new Thread(new RevThread());
			// revThread.start();
			revThread.start();
			
			
		}else if( type.equals("text")){
			message = new String( msg.getRecords()[1].getPayload() );
			mLayout.setVisibility(View.VISIBLE );
			fLayout.setVisibility(View.GONE );
			cLayout.setVisibility(View.GONE );
			TextView mText = (TextView)findViewById( R.id.get_message );
			mText.setMovementMethod(ScrollingMovementMethod.getInstance());
			mText.setText(message);
			
			
			//
		}else if( type.equals("contract")){
			String contract = new String( msg.getRecords()[1].getPayload());
			final String name = contract.split("##")[0];
			final String number = contract.split("##")[1];
			
			cLayout.setVisibility(View.VISIBLE );
			mLayout.setVisibility(View.GONE );
			fLayout.setVisibility(View.GONE );
			TextView n = (TextView)findViewById( R.id.name );
			TextView num = (TextView)findViewById( R.id.number );
			n.setText( name );
			num.setText( number );
			
			Button cancel = (Button)findViewById( R.id.contact_cancel );
			Button send = (Button)findViewById( R.id.contact_send );
			
			cancel.setOnClickListener( new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					finish();
				}
				
			});
			
			
			send.setOnClickListener( new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					addContract( name, number) ;
				}
				
			});
			
			//addContract()
			
			
			
			
		}
	}

	class RevThread implements Runnable {
		// private Socket client;
		private DataInputStream input;
		private FileOutputStream output;

		@Override
		public void run() {
			// tv.setText("receving...");
			revFile();
			// tv.setText("sucess!!!");
		}

		public void revFile() {
			WifiInfo info = wifiManager.getConnectionInfo();
			int temp = 0;
			if (info.getIpAddress() == 0) {
				System.out.println("ip address:  " + info.getIpAddress());
				handler.sendEmptyMessageDelayed(0, 1000);
				return;
			}
			System.out.println("ip address:  " + info.getIpAddress());
			// revHandler.postDelayed(revThread, 3000 );
			dhcpInfo = wifiManager.getDhcpInfo();

			String ip = Formatter.formatIpAddress(dhcpInfo.serverAddress);
			try {
				System.out.println("create socket....");
				client = new Socket(ip, 8090);

				if (client == null) {
					System.out.println("client is null");
					return;
				}
				input = new DataInputStream(new BufferedInputStream(
						client.getInputStream()));

				// int totalSize =
				int currentSize = 0;

				int bufferSize = 10240; // ��������
				byte buffer[] = new byte[bufferSize];
				fileName = input.readUTF(); // �����ļ���
				long totalSize = input.readLong(); 
				sendMsg(fileName,getSize(totalSize));
				filePath = path + fileName;
				output = new FileOutputStream(filePath);
				int i = 1;// 计数器
				// bar.setMax(totalSize);
				while (true) {
					int readBuffer = 0;
					if (input != null)
						readBuffer = input.read(buffer); // ��socket�����������
					if (readBuffer == -1)
						break;
					output.write(buffer, 0, readBuffer);

					currentSize += readBuffer;

					/*
					 * if( currentSize>totalSize*i/5 ){ sendProgress(
					 * currentSize, totalSize, 0 ); System.out.println(
					 * "send progress.."); i++; }
					 */
					sendProgress(currentSize, totalSize, 0);

				}
				isStop = true;
				// tv.setText("�ļ�������ɣ�·��Ϊ��"+ filePath);
				sendProgress(totalSize, totalSize, 1);
				output.close(); // �ر����������ʡ�ڴ濪��
				input.close();
				// client.close();

			} catch (Exception e) {
				Log.e("tag", e.toString());
			} finally {

				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		
		public void sendProgress(long c, long t, int tag) {
			Message m = revHandler.obtainMessage();

			if (tag == 0) {
				m.what = 1;
				Bundle b = new Bundle();
				b.putLong("c", c);
				b.putLong("t",t);
				m.setData(b);
			} else {
				m.what = 3;
			}
			FileRevActivity.this.revHandler.sendMessage(m);
		}

		public void sendMsg(String name,String size) { // ��handler����string������Ϣ
			Message m = revHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("name", name);
			b.putString("size", size);
			m.what = 2;
			m.setData(b);
			FileRevActivity.this.revHandler.sendMessage(m);
		}
	}
	

	class RevHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				Bundle b = msg.getData();
				long c = b.getLong("c");
				long t = b.getLong("t");
				int percent = (int)(100*c/t);
			    progress.setText( "" + percent + "%" );
			    mCircleProgressBar1.setMainProgress(percent);
				break;
			case 2:
				b = msg.getData();
				String name = b.getString("name");
				String size = b.getString("size");
				if(checkEndsWithInStringArray(name, getResources().              //音频文件
		                getStringArray(R.array.fileEndingAudio))){
					fileType.setImageResource(R.drawable.file_type_music);
				}else if(checkEndsWithInStringArray(name, getResources().             //视频文件
		                getStringArray(R.array.fileEndingVideo))){
					fileType.setImageResource(R.drawable.file_type_vedio);
				}
				else if(checkEndsWithInStringArray(name, getResources().             //视频文件
		                getStringArray(R.array.fileEndingText))
		                ||checkEndsWithInStringArray(name, getResources().             //视频文件
				                getStringArray(R.array.fileEndingPdf))){
					fileType.setImageResource(R.drawable.file_type_text);
				}else{
					fileType.setImageResource(R.drawable.file_type_unknown);
				}
				file_name.setText(getPre(name));
				fileSize.setText(size);
				
				break;
			case 3:
				mCircleProgressBar1.setMainProgress(100);
				progress.setText( "" + 100 + "%" );
				new AlertDialog.Builder(FileRevActivity.this)
				.setTitle("Open File")
				.setMessage("打开[ " + fileName + " ] ?")
				.setPositiveButton("好的，打开!",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								
								openFile(fileName,filePath);
								finish();
							}
						})
				.setNegativeButton("不!",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						}).show();
				break;
			}

		}
	}
	
	
	private void addContract( String name, String number ){
		
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = getBaseContext().getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
				.withValue("account_name", null).build();
		operations.add(op1);

		uri = Uri.parse("content://com.android.contacts/data");
		ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/name")
				.withValue("data2", name).build();
		operations.add(op2);

		ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/phone_v2")
				.withValue("data1", number).withValue("data2", "2")
				.build();
		operations.add(op3);

		try {
			resolver.applyBatch("com.android.contacts", operations);
			new AlertDialog.Builder(this)
			.setTitle("Message")
			.setMessage("Contact is on list.")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							finish();
						}
					}).show();
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			new AlertDialog.Builder(this)
			.setTitle("Message")
			.setMessage("Contact is added failed!")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}).show();
			
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			new AlertDialog.Builder(this)
			.setTitle("Message")
			.setMessage("Contact is added failed!")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}).show();
			
		}
	}
	
	private String getPre(String name){
		String pre = name;
		if(name.length()>5)
			pre = name.substring(0,5);
		return pre + "...";
	}
	private String getSize( long length ){
		String size = "";
		if(length > 1000000)
			size = length/(1024*1024) + "MB";
		else if(length > 1000)
			size = length/1024 + "KB";
		else 
			size = length + "B";
		return size;
	}

	//检查后缀
	private boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}
	private void openFile(String fileName,String path){     //传文件名和文件路径
		File currentPath = new File(path);
        Intent intent;
        if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingImage))){
            intent = OpenFiles.getImageFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingWebText))){
            intent = OpenFiles.getHtmlFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingPackage))){
            intent = OpenFiles.getApkFileIntent(currentPath);
            startActivity(intent);

        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingAudio))){
            intent = OpenFiles.getAudioFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingVideo))){
            intent = OpenFiles.getVideoFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingText))){
            intent = OpenFiles.getTextFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingPdf))){
            intent = OpenFiles.getPdfFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingWord))){
            intent = OpenFiles.getWordFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingExcel))){
            intent = OpenFiles.getExcelFileIntent(currentPath);
            startActivity(intent);
        }else if(checkEndsWithInStringArray(fileName, getResources().
                getStringArray(R.array.fileEndingPPT))){
            intent = OpenFiles.getPPTFileIntent(currentPath);
            startActivity(intent);
        }else
        {
        	new AlertDialog.Builder(this)
			.setTitle("Message")
			.setMessage("找不到可以打开的程序，请安装程序!")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}).show();
        }


	}
}
