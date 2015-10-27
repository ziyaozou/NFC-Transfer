package com.weibo.sdk.android.demo;

import java.io.IOException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nfc.R;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.util.Utility;
/**
 * 
 * @author liyan (liyan9@staff.sina.com.cn)
 */
public class WeiBoActivity extends Activity implements CreateNdefMessageCallback,
OnNdefPushCompleteCallback {

    private Weibo mWeibo;
    private static final String CONSUMER_KEY = "966056985";/*"966056985"*/;// 替换为开发者的appkey，例如"1646212860";
    private static final String REDIRECT_URL = "http://www.sina.com";
    private Button authBtn, cancelBtn;
    private TextView mText;
    private TextView uidText;
    private TextView screenNameText;
    private long uid;
    private String screen_name;
    private String gender;
	private String image;
	private String location;
	private String description;
	private String followers_count;
	private String friends_count;
	private String statuses_count;
	private NfcAdapter mNfcAdapter;
    public static Oauth2AccessToken accessToken;
    public static final String TAG = "sinasdk";
    public static final String RESPONSE_UID = "\"uid\"";
    public static final String RESPONSE_SCREEN_NAME = "\"screen_name\"";
    public static final String RESPONSE_GENDER = "\"gender\"";
    public static final String RESPONSE_LOCATION = "\"location\"";
    public static final String RESPONSE_DESCRIPTION = "\"description\"";
    public static final String RESPONSE_IMAGE = "\"avatar_large\"";
    public static final String RESPONSE_FOLLOWERS_COUNT = "\"followers_count\"";
    public static final String RESPONSE_FRIENDS_COUNT = "\"friends_count\"";
    public static final String RESPONSE_STATUSES_COUNT = "\"statuses_count\"";
    public static final String SPLIT_STR = ":";
    
    public String getInfo()
    {
    	String total = String.valueOf(uid) + "\n" +
    					screen_name + "\n" +
    					gender + "\n" + 
    					image + "\n" + 
    					location + "\n" +
    					description + "\n" + 
    					followers_count + "\n" + 
    					friends_count + "\n" + 
    					statuses_count + "\n";
    	return total;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);
        mWeibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
           System.out.println( "nfc is null ");
        } else {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
        
        authBtn = (Button) findViewById(R.id.auth);
        authBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mWeibo.authorize(WeiBoActivity.this, new AuthDialogListener());
            }
        });
        cancelBtn = (Button) findViewById(R.id.apiCancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessTokenKeeper.clear(WeiBoActivity.this);
                authBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.INVISIBLE);
                mText.setText("");
                uidText.setText("");
                screenNameText.setText("");
            }
        });

        mText = (TextView) findViewById(R.id.show);
        WeiBoActivity.accessToken = AccessTokenKeeper.readAccessToken(this);
        if (WeiBoActivity.accessToken.isSessionValid()) {
            Weibo.isWifi = Utility.isWifi(this);
            try {
                Class sso = Class.forName("com.weibo.sdk.android.api.WeiboAPI");// 如果支持weiboapi的话，显示api功能演示入口按钮
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
                Log.i(TAG, "com.weibo.sdk.android.api.WeiboAPI not found");

            }
            authBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            String date = new java.text.SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
                    .format(new java.util.Date(WeiBoActivity.accessToken
                            .getExpiresTime()));
            
            /*mText.setText("access_token 仍在有效期内,无需再次登录: \naccess_token:"
                    + WeiBoActivity.accessToken.getToken() + "\n有效期：" + date);*/
            AccountAPI account = new AccountAPI(WeiBoActivity.accessToken);
			account.getUid(new UidDialogListener());
			uidText = (TextView)findViewById(R.id.uid);
			while(uid == 0){};
			//uidText.setText(String.valueOf(uid));
			UsersAPI user = new UsersAPI(WeiBoActivity.accessToken);
			user.show(uid, new ScreenNameListener());
			screenNameText = (TextView)findViewById(R.id.screen_name);
			while(screen_name == null || screen_name.equals("")){};
			screenNameText.setText("昵称：" + screen_name + "\r\n"); /*+ 
					"性别：" + gender + "\r\n" + 
					"头像：" + image + "\r\n" + 
					"地区：" + location + "\r\n" + 
					"简介：" + description + "\r\n" + 
					"粉丝数：" + followers_count + "\r\n" + 
					"关注数：" + friends_count + "\r\n" + 
					"微博数：" + statuses_count);*/
        } else {
            /*mText.setText("使用SSO登录前，请检查手机上是否已经安装新浪微博客户端，目前仅3.0.0及以上微博客户端版本支持SSO；如果未安装，将自动转为Oauth2.0进行认证");*/
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_weibo, menu);
        return true;
    }

    class AuthDialogListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            WeiBoActivity.accessToken = new Oauth2AccessToken(token, expires_in);
            if (WeiBoActivity.accessToken.isSessionValid()) {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(new java.util.Date(WeiBoActivity.accessToken
                                .getExpiresTime()));
                /*mText.setText("认证成功: \r\n access_token: " + token + "\r\n"
                        + "expires_in: " + expires_in + "\r\n有效期：" + date);*/
                try {
                    Class sso = Class
                            .forName("com.weibo.sdk.android.api.WeiboAPI");// 如果支持weiboapi的话，显示api功能演示入口按钮
                } catch (ClassNotFoundException e) {
                    // e.printStackTrace();
                    Log.i(TAG, "com.weibo.sdk.android.api.WeiboAPI not found");

                }
                cancelBtn.setVisibility(View.VISIBLE);
                AccessTokenKeeper.keepAccessToken(WeiBoActivity.this,
                        accessToken);
                Toast.makeText(WeiBoActivity.this, "认证成功", Toast.LENGTH_SHORT)
                        .show();
                AccountAPI account = new AccountAPI(WeiBoActivity.accessToken);
				account.getUid(new UidDialogListener());
				uidText = (TextView)findViewById(R.id.uid);
				while(uid == 0){};
				//uidText.setText("UID: " + String.valueOf(uid));
				UsersAPI user = new UsersAPI(WeiBoActivity.accessToken);
				user.show(uid, new ScreenNameListener());
				screenNameText = (TextView)findViewById(R.id.screen_name);
				while(screen_name == null || screen_name.equals("")){};
				screenNameText.setText("昵称：" + screen_name + "\r\n"); /*+ 
										"性别：" + gender + "\r\n" + 
										"头像：" + image + "\r\n" + 
										"地区：" + location + "\r\n" + 
										"简介：" + description + "\r\n" + 
										"粉丝数：" + followers_count + "\r\n" + 
										"关注数：" + friends_count + "\r\n" + 
										"微博数：" + statuses_count);*/
            }
        }

        @Override
        public void onError(WeiboDialogError e) {
            Toast.makeText(getApplicationContext(),
                    "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "Auth cancel",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(getApplicationContext(),
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

    }
    
    class UidDialogListener implements RequestListener
    {

		@Override
		public void onComplete(String response) {
			// TODO Auto-generated method stub
			String[] contents = response.split("\r\n");
			String uidStr = null;
			for(String line : contents)
			{
				if(line == null || line.equals(""))
					continue;
				line = line.replace("{", "").replace("}", "").trim();
				if(line.contains(RESPONSE_UID))
				{
					String[] substrs = line.split(SPLIT_STR);
					uidStr = substrs[1].replace("\"", "").trim();
				}
			}
			uid = Long.valueOf(uidStr);
		}

		@Override
		public void onIOException(IOException e) {
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(), "Get Uid error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			System.out.println(e.getMessage());
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(), "Get Uid error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			System.out.println(e.getMessage());
		}
    	
    }
    
    class ScreenNameListener implements RequestListener
    {

		@Override
		public void onComplete(String response) {
			// TODO Auto-generated method stub
			response = response.replace("{", "").replace("}", "").trim();
			String[] contents = response.split(",");
			String screen_nameStr = null;
			String genderStr = null;
			String imageStr = null;
			String locationStr = null;
			String descriptionStr = null;
			String followers = null;
			String friends = null;
			String statuses = null;
			for(String line : contents)
			{
				if(line == null || line.equals(""))
					continue;
				line = line.trim();
				if(line.contains(RESPONSE_SCREEN_NAME))
				{
					String[] substrs = line.split(SPLIT_STR);
					screen_nameStr = substrs[1].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_GENDER))
				{
					String[] substrs = line.split(SPLIT_STR);
					genderStr = substrs[1].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_IMAGE))
				{
					String[] substrs = line.split(SPLIT_STR);
					imageStr = substrs[1].replace("\"", "").trim() + ":" + substrs[2].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_LOCATION))
				{
					String[] substrs = line.split(SPLIT_STR);
					locationStr = substrs[1].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_DESCRIPTION))
				{
					String[] substrs = line.split(SPLIT_STR);
					descriptionStr = substrs[1].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_FOLLOWERS_COUNT))
				{
					String[] substrs = line.split(SPLIT_STR);
					followers = substrs[1].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_FRIENDS_COUNT))
				{
					String[] substrs = line.split(SPLIT_STR);
					friends = substrs[1].replace("\"", "").trim();
				}else if(line.contains(RESPONSE_STATUSES_COUNT))
				{
					String[] substrs = line.split(SPLIT_STR);
					statuses = substrs[1].replace("\"", "").trim();
				}
			}
			screen_name = screen_nameStr;
			gender = genderStr;
			image = imageStr;
			location = locationStr;
			description = descriptionStr;
			followers_count = followers;
			friends_count = friends;
			statuses_count = statuses;
		}

		@Override
		public void onIOException(IOException e) {
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(), "Get Screen_name error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			System.out.println(e.getMessage());
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(), "Get Screen_name error : " + e.getMessage(), Toast.LENGTH_LONG).show();
			System.out.println(e.getMessage());
		}
    	
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		// TODO Auto-generated method stub
		String message = getInfo();
		NdefRecord record2 = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"application/com.ziyao.nfc.follow".getBytes(), new byte[] {},
				message.getBytes());

		NdefMessage msg = new NdefMessage(new NdefRecord[] {record2});

		return msg;
	}

}
