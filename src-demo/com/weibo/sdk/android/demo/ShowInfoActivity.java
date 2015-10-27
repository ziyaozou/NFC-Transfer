package com.weibo.sdk.android.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.nfc.R;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowInfoActivity extends Activity {

	private ImageView icon;
	private ImageView genderImage;
	private TextView name;
	private TextView loca;
	private TextView followers;
	private TextView friends;
	private TextView statuses;
	private TextView introduction;
	private Button followBtn;
	private String followFlag;
	private String uid;
	private String screen_name;
	private String gender;
	private String image;
	private String location;
	private String description;
	private String followers_count;
	private String friends_count;
	private String statuses_count;

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
		// TODO Auto-generated method stub
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String message = new String(msg.getRecords()[0].getPayload());

		// message is what you transfer...
		String[] content = message.split("\n");
		uid = content[0];
		screen_name = content[1];
		gender = content[2];
		image = content[3];
		System.out.println(image);
		location = content[4];
		description = content[5];
		followers_count = content[6];
		friends_count = content[7];
		statuses_count = content[8];

		icon.setImageBitmap(returnBitMap(image));
		name.setText(screen_name);
		loca.setText(location);
		followers.setText(followers_count);
		friends.setText(friends_count);
		statuses.setText(statuses_count);
		introduction.setText(description);
		if (gender.equals("f")) {
			genderImage.setImageResource(R.drawable.sex_female);
		} else {
			genderImage.setImageResource(R.drawable.sex_male);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
																		// 就包括了磁盘读写和网络I/O
				.penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
				.penaltyLog() // 打印logcat
				.penaltyDeath().build());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_info);
		icon = (ImageView) findViewById(R.id.icon);

		genderImage = (ImageView) findViewById(R.id.genderImage);

		name = (TextView) findViewById(R.id.uId);

		loca = (TextView) findViewById(R.id.location);

		followers = (TextView) findViewById(R.id.fans);

		friends = (TextView) findViewById(R.id.attention);

		statuses = (TextView) findViewById(R.id.weibo);

		introduction = (TextView) findViewById(R.id.description);

		followBtn = (Button) findViewById(R.id.attentionButton);
		followBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (WeiBoActivity.accessToken == null) {
					Intent intent = new Intent();
					intent.setClass(ShowInfoActivity.this, WeiBoActivity.class);
					startActivity(intent);
				} else {
					FriendshipsAPI friendships = new FriendshipsAPI(
							WeiBoActivity.accessToken);
					friendships.create(Long.valueOf(uid), screen_name,
							new FollowListener());
					while (followFlag == null || followFlag.equals("")) {
					}
					;
					Toast.makeText(getApplicationContext(), followFlag,
							Toast.LENGTH_LONG).show();
					finish();
				}
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_show_info, menu);
		return true;
	}

	public Bitmap returnBitMap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	class FollowListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			// TODO Auto-generated method stub
			if (response != null && !response.equals(""))
				followFlag = "关注成功！";
			else
				followFlag = "关注失败";
		}

		@Override
		public void onIOException(IOException e) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),
					"Follow error : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			followFlag = "已经关注！";
			// Toast.makeText(getApplicationContext(), "Follow error : " +
			// e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

}
