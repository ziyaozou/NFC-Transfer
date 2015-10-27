package com.example.nfc;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NFCShare extends Activity implements CreateNdefMessageCallback,
		OnNdefPushCompleteCallback {

	private TextView textView;
	private Button cancle;
	private Button send;
	private String name;
	private String password;
	private final int TEXT = 1;
	private final int FILE = 2;
	private int style;
	private Intent intent;
	private String message;
	private String fileName;
	private String filePath;
	
	private NfcAdapter mNfcAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		LinearLayout layout = (LinearLayout)findViewById( R.id.share_layout );
		layout.setVisibility(View.VISIBLE );
		layout = (LinearLayout)findViewById( R.id.share_gone );
		layout.setVisibility(View.GONE) ;
		textView = (TextView) findViewById(R.id.message);
		cancle = (Button) findViewById(R.id.cancle);
		send = (Button) findViewById(R.id.send);
		textView.setMovementMethod(ScrollingMovementMethod.getInstance());
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
           System.out.println( "nfc is null ");
        } else {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
		
		name = getRandomString();
		password = getRandomString();

		intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.equals("text/plain")) {
				System.out.println("text");
				message = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (message != null) {
					style = TEXT;

					textView.setText(message);
							
				} else {
					style = FILE;
					Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

					fileName = uri.getLastPathSegment();
					filePath = uri.getPath();

					
					textView.setText("文件名: " + fileName + "\n\n"
							+ "文件路径: " + filePath);
				}
			} else {
				System.out.println("file");
				style = FILE;

				Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

				fileName = uri.getLastPathSegment();
				filePath = uri.getPath();
				
				textView.setText("FILE: " + fileName + "\n\n"
						+ "PATH: " + filePath);
			}
		}

		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				switch (style) {
				case TEXT:
					LinearLayout layout = (LinearLayout) findViewById(R.id.share_layout);
					layout.setVisibility(View.GONE);
					layout = (LinearLayout) findViewById(R.id.share_gone);
					layout.setVisibility(View.VISIBLE);
					break;
				case FILE:
					// String path = intent.getStringExtra(Intent.EXTRA_TEXT);
					Intent in = new Intent();
					// in.putExtra("fileName", fileName);
					WifiConfig config = new WifiConfig( NFCShare.this, name, password );
					config.startWifiAp();
					in.putExtra("name", name);
					in.putExtra("password", password);
					in.putExtra("filePath", filePath);
					in.setClass(NFCShare.this, FileUploadActivity.class);
					startActivity(in);
					break;
				}
			}

		});
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// TODO Auto-generated method stub
		System.out.println("text share message is send..");
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		// TODO Auto-generated method stub
		NdefRecord record1 = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"application/com.ziyao.nfc.file".getBytes(), new byte[] {},
				"text".getBytes());
		NdefRecord record2 = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"application/com.ziyao.nfc.file".getBytes(), new byte[] {},
				message.getBytes());

		NdefMessage msg = new NdefMessage(new NdefRecord[] { record1, record2});

		return msg;
	}
	
	public static String getRandomString() { // length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

}
