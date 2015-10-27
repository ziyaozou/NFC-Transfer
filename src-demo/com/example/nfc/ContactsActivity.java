package com.example.nfc;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactsActivity extends Activity {

	Context mContext = null;

	/** 在表中找 显示名称 电话号码 头像ID 联系人ID 这4个数据 **/
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID };
	/** 显示联系人名称 **/
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;
	/** 电话号码 **/
	private static final int PHONES_NUMBER_INDEX = 1;
	/** 头像ID **/
	private static final int PHONES_PHOTO_ID_INDEX = 2;
	/** 联系人ID **/
	private static final int PHONES_CONTACT_ID_INDEX = 3;
	/** 联系人名称 **/
	private ArrayList<String> mContactsName = new ArrayList<String>();
	/** 联系人号码 **/
	private ArrayList<String> mContactsNumber = new ArrayList<String>();
	/** 联系人头像 **/
	private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();

	ListView mListView = null;
	MyListAdapter myAdapter = null;
	EditText searchNameEV;
	Button searchButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		mContext = this;
		mListView = (ListView) findViewById(R.id.listView);
		searchNameEV = (EditText) findViewById(R.id.searchName);
		searchButton = (Button) findViewById(R.id.searchButton);
		// mListView = this.getListView();
		/** 得到手机通讯录联系人信息 **/
		getPhoneContacts();
		myAdapter = new MyListAdapter(this);
		mListView.setAdapter(myAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// 响应点击
				Log.i("电话号码：", mContactsNumber.get(position));
				Intent intent = new Intent(ContactsActivity.this,
						SendContacts.class);
				intent.putExtra("mContactsName", mContactsName.get(position));
				intent.putExtra("mContactsNumber",
						mContactsNumber.get(position));
				startActivity(intent);

			}
		});

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchName = searchNameEV.getText().toString().trim();
				try {
					ContactsActivity.this.getContactsByName(searchName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	/** 得到手机通讯录联系人信息 **/
	private void getPhoneContacts() {
		ContentResolver resolver = mContext.getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				// 得到联系人名称
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);
				// 得到联系人ID
				Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);
				// 得到联系人头像ID
				Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);
				// 得到联系人头像Bitamp
				Bitmap contactPhoto = null;
				// photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				} else {
					contactPhoto = BitmapFactory.decodeResource(getResources(),
							R.drawable.contact_photo);
				}
				mContactsName.add(contactName);
				mContactsNumber.add(phoneNumber);
				mContactsPhonto.add(contactPhoto);
			}
			phoneCursor.close();
		}
	}

	/** 得到手机SIM卡联系人信息(暂时没用到) **/
	private void getSIMContacts() {
		ContentResolver resolver = mContext.getContentResolver();
		// 获取Sim卡联系人
		Uri uri = Uri.parse("content://icc/adn");
		Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
				null);
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				// 得到联系人名称
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);
				// Sim卡中没有联系人头像
				mContactsName.add(contactName);
				mContactsNumber.add(phoneNumber);
			}
			phoneCursor.close();
		}
	}

	private void getContactsByName(String uName) {
		mContactsName.clear();
		mContactsNumber.clear();
		mContactsPhonto.clear();
		ContentResolver reContentResolverol = getContentResolver();
		Uri contactData = Phone.CONTENT_URI;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
				+ " LIKE '%" + uName + "%'";
		@SuppressWarnings("deprecation")
		Cursor cursor = reContentResolverol.query(contactData,
				PHONES_PROJECTION, selection, null, null);
		while (cursor.moveToNext()) {		
			String username = cursor.getString(PHONES_DISPLAY_NAME_INDEX);
			String usernumber = cursor.getString(PHONES_NUMBER_INDEX);
			Long photoid = cursor.getLong(PHONES_PHOTO_ID_INDEX);
			// 得到联系人头像Bitamp
			Bitmap userPhoto = null;
			Long contactid = cursor.getLong(PHONES_CONTACT_ID_INDEX);
			// photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
			if (photoid > 0) {
				Uri uri = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI, contactid);
				InputStream input = ContactsContract.Contacts
						.openContactPhotoInputStream(reContentResolverol, uri);
				userPhoto = BitmapFactory.decodeStream(input);
			} else {
				userPhoto = BitmapFactory.decodeResource(getResources(),
						R.drawable.contact_photo);
			}
			Log.i("userName", usernumber + " (" + username + ")");
			mContactsName.add(username);
			mContactsNumber.add(usernumber);
			mContactsPhonto.add(userPhoto);
		}
		myAdapter.refresh();

	}

	class MyListAdapter extends BaseAdapter {
		public MyListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			// 设置绘制数量
			return mContactsName.size();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iamge = null;
			TextView title = null;
			TextView text = null;
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.colorlist, null);
			iamge = (ImageView) convertView.findViewById(R.id.color_image);
			title = (TextView) convertView.findViewById(R.id.color_title);
			text = (TextView) convertView.findViewById(R.id.color_text);
			// 绘制联系人名称
			title.setText(mContactsName.get(position));
			// 绘制联系人号码
			text.setText(mContactsNumber.get(position));
			// 绘制联系人头像
			iamge.setImageBitmap(mContactsPhonto.get(position));
			return convertView;
		}
		
		public void refresh(){
			notifyDataSetChanged();			
		}
	}
}
