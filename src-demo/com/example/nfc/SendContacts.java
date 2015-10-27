package com.example.nfc;


import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class SendContacts extends Activity implements CreateNdefMessageCallback,
OnNdefPushCompleteCallback{

	private String contactsName = null;
	private String contactsNumber = null;
	private Button send;
	private Button cancel;
	private TextView name;
	private TextView number;
	private String contract;
	private NfcAdapter mNfcAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_contacts);
		Bundle extras = getIntent().getExtras();
		contactsName = extras.getString("mContactsName");
		contactsNumber = extras.getString("mContactsNumber");
		send = (Button)findViewById(R.id.send);
		cancel = (Button)findViewById(R.id.cancel);
		name = (TextView)findViewById(R.id.contactsName);
		number = (TextView)findViewById(R.id.contactsNumber);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.linearLayout1);
		layout.setVisibility( View.VISIBLE );
		layout = (LinearLayout)findViewById(R.id.ll_01);
		layout.setVisibility( View.GONE );
		
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
           System.out.println( "nfc is null ");
        } else {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
		
		name.setText(contactsName);
		number.setText(contactsNumber);		
		send.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//发送名片信息message
				//////////////////////////////////////////////////
				contract = contactsName + "##" + contactsNumber;
				//////////////////////////////////////////////////
				LinearLayout layout = (LinearLayout)findViewById(R.id.linearLayout1);
				layout.setVisibility( View.GONE );
				layout = (LinearLayout)findViewById(R.id.ll_01);
				layout.setVisibility( View.VISIBLE );
			}
		});
		cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				finish();
			}
		});
	}


	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println( "contract is send.");
	}


	@Override
	public NdefMessage createNdefMessage(NfcEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println( "NFC IS TRANSFERING.." + contract  );
		NdefRecord record0 = new NdefRecord( NdefRecord.TNF_MIME_MEDIA,
        		"application/com.ziyao.nfc.file".getBytes(),
        		new byte[]{},"contract".getBytes());
		NdefRecord record1 = new NdefRecord( NdefRecord.TNF_MIME_MEDIA,
        		"application/com.ziyao.nfc.file".getBytes(),
        		new byte[]{},contract.getBytes());
		
        NdefMessage msg = new NdefMessage(new NdefRecord[]{record0,record1}
        );
        //System.out.println( "NFC IS Done" + name + "   " + password );
        return msg;
	}


}
