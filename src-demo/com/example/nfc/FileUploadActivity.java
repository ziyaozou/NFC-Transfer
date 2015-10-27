package com.example.nfc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.nfc.CircleProgress;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FileUploadActivity extends Activity implements
CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	private TextView fileName;
	private TextView fileSize;
	private TextView progress;
	private String filePath;
	private Socket server;
	private SendHandler sendHandler;
	ServerSocket serverSocket;
	private String name;
	private String password;
	private NfcAdapter mNfcAdapter;
	
	private ImageView fileType;
	
	private CircleProgress mCircleProgressBar1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_file_upload);
        
        
        mCircleProgressBar1 = (CircleProgress) findViewById(R.id.roundBar1);
        fileName = (TextView)findViewById( R.id.file_name_label);
        fileSize = (TextView)findViewById( R.id.file_size_label );
        progress = (TextView)findViewById( R.id.percentage );
        fileType = (ImageView)findViewById( R.id.file_type_label );
        
	    Intent intent = this.getIntent();
	    filePath = intent.getStringExtra("filePath");
	    name = intent.getStringExtra("name");
        password = intent.getStringExtra("password");
	    
	    mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
           System.out.println( "nfc is null ");
        } else {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
        
        
	    sendHandler = new SendHandler();
	    Thread newThread = new Thread(new MyThread());
	    newThread.start();
	    
    }

    
    class MyThread implements Runnable{
    	
    	private FileInputStream input;
    	private DataOutputStream output;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//tv1.setText("sending.......");
			sendFile();
			//tv1.setText("sucess ,file sent!!!");
		}
		public void sendFile(){
			
			try{
				File file = new File(filePath);
				
				
				
				//FileUploadActivity.this.sendHandler.sendMessage("");
				serverSocket = new ServerSocket(8090);
				while(true){
					
					server = serverSocket.accept();
					if(server != null)break;
				}
				input = new FileInputStream(file);
				long totalSize = file.length();
				sendMsg( file.getName(), (long)totalSize );
				long currentSize = 0;
				output = new DataOutputStream(server.getOutputStream());
				
				//�����ļ�
				output.writeUTF(file.getName());      //д���ļ���
				output.writeLong(file.length());      //д���ļ�����
				int bufferSize = 10240;                    //�������鳤��
				byte buffer[] = new byte[bufferSize];
				
				while(true){
					int readBuffer = 0;
					if(input != null)
						readBuffer = input.read(buffer);       //�Ӵ��̶������
					
					if(readBuffer == -1)break;
					output.write(buffer,0,readBuffer); 
					currentSize+=readBuffer;
					sendProgress( currentSize, totalSize, 0 );
				}
				sendProgress(0,totalSize,1);
				
				output.flush();
				output.close();
				input.close();
				serverSocket.close();
			}catch(Exception e){
				Log.e("tag",e.toString());
			}finally{
				try{
					server.close();
					serverSocket.close();
					
				}catch( Exception e){
					e.printStackTrace();
				}
			}
			
			
		}
		
		public void sendProgress( long c, long t,int tag ){
			Message m = sendHandler.obtainMessage();
			if( tag==0 ){
				m.what = 1;
				Bundle data = new Bundle();
				data.putLong("total", t);
				data.putLong( "c",c );
				m.setData(data);
			}else{
				m.what = 3;
			}
			sendHandler.sendMessage(m);
		}
		public void sendMsg(String name, long size){      //��handler����string������Ϣ
			Message m = sendHandler.obtainMessage();
			m.what = 2;
			Bundle b =new Bundle();
			b.putString("name", name);
			b.putString("size", getSize(size) );
			m.setData(b);
			sendHandler.sendMessage(m);
		}
    	
    }
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
    	try{
    		if(serverSocket != null){
    			serverSocket.close();
    		}
    	}catch(Exception e){
    		
    	}
		super.onDestroy();
	}


	class SendHandler extends Handler{
    	public SendHandler(){
    		
    	}
    	public SendHandler(Looper l){
    		
    		super(l);
    		Looper.prepare();
    	}
    	@Override
		public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		switch( msg.what ){
    		case 1:
    			
    			Bundle data = msg.getData();
    			long c = data.getLong("c");
    			long t = data.getLong("total");
    			int percent = (int)(c*100/t);
    			mCircleProgressBar1.setMainProgress(percent);
    			progress.setText( ""+percent + "%" );
    			break;
    		case 2:
    			Bundle b = msg.getData();
        		String name = b.getString("name");
        		String size = b.getString("size");
        		fileSize.setText( size );
        		fileName.setText( getPre(name) );
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
    			break;
    		case 3:
    			new AlertDialog.Builder(FileUploadActivity.this)
				.setTitle("Message")
				.setMessage("文件已传输！!")
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).show();
    			mCircleProgressBar1.setMainProgress(100);
    			break;
    		case 4:
    			System.out.println( "case is 4");
    			break;
    		}
    		
    	}
    }

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// TODO Auto-generated method stub
		System.out.println( "name and password " + name + "   "  +  password );
		Message msg = sendHandler.obtainMessage();
		msg.what=4;
		sendHandler.sendMessage(msg);
		
		//bar.setVisibility( View.VISIBLE );
	}


	@Override
	public NdefMessage createNdefMessage(NfcEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println( "NFC IS TRANSFERING.." + name + "   " + password );
		NdefRecord record0 = new NdefRecord( NdefRecord.TNF_MIME_MEDIA,
        		"application/com.ziyao.nfc.file".getBytes(),
        		new byte[]{},"key".getBytes());
		NdefRecord record1 = new NdefRecord( NdefRecord.TNF_MIME_MEDIA,
        		"application/com.ziyao.nfc.file".getBytes(),
        		new byte[]{},name.getBytes());
		NdefRecord record2 = new NdefRecord( NdefRecord.TNF_MIME_MEDIA,
        		"application/com.ziyao.nfc.file".getBytes(),
        		new byte[]{},password.getBytes());
        NdefMessage msg = new NdefMessage(new NdefRecord[]{record0,record1,record2}
        );
        System.out.println( "NFC IS Done" + name + "   " + password );
        return msg;
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
	
	private boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}
}
