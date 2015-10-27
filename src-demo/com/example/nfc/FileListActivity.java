package com.example.nfc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FileListActivity extends ListActivity {

	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/";
	private TextView mPath;
	private String filename;
	private String name;
	private String password;
	private static final int ITEM1 = Menu.FIRST;    
	private static final int ITEM2 = Menu.FIRST+1;    
	private static final int ITEM3 = Menu.FIRST+2;

	private ArrayList<Map<String, Object>> mModelData = null;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		/* ����main.xml Layout */
		setContentView(R.layout.activity_file_list);
		
		name = getIntent().getStringExtra("name");
		password = getIntent().getStringExtra("password");
		
		mPath = (TextView) findViewById(R.id.mPath);

		mPath.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String parentPath = mPath.getText().toString();
				File f = new File(parentPath);
				parentPath = f.getParent();
				getFileDir(parentPath);
			}

		});
		
		getFileDir(rootPath);
		//为 ListView 的所有 item 注册 ContextMenu        
		registerForContextMenu(getListView());
	}

	/* ȡ���ļ��ܹ���method */
	private void getFileDir(String filePath) {
		/* �趨Ŀǰ����·�� */
		mModelData = new ArrayList<Map<String, Object>>();
		mPath.setText(filePath);
		mPath.setClickable(false);

		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();

		if (!filePath.equals(rootPath)) {
			/* ��һ���趨Ϊ[�ص���Ŀ¼] */
			// items.add("Back to "+rootPath);
			// paths.add(rootPath);
			/* �ڶ����趨Ϊ[���ϲ�] */
			// items.add("<<Return");
			// paths.add(f.getParent());
			mPath.setClickable(true);
		}
		/* �������ļ�����ArrayList�� */
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("name", file.getName());
			String fileName = file.getName();
			if (file.isDirectory()) {
				item.put("img", R.drawable.folder);
			}else if(checkEndsWithInStringArray(fileName, getResources().              //音频文件
	                getStringArray(R.array.fileEndingAudio))){
				item.put("img", R.drawable.music);
			}else if(checkEndsWithInStringArray(fileName, getResources().             //视频文件
	                getStringArray(R.array.fileEndingVideo))){
				item.put("img", R.drawable.video);
			}
			else {
				item.put("img", R.drawable.file);
			}
			mModelData.add(item);

			items.add(file.getName());
			paths.add(file.getPath());
		}

		/* ����һArrayAdapter��ʹ��file_row���Layout������Adapter�趨���ListActivity */
		// ArrayAdapter<String> fileList = new
		// ArrayAdapter<String>(this,R.layout.file_row, items);
		SimpleAdapter adpter = new SimpleAdapter(this, mModelData,
				R.layout.file_row, new String[] { "name", "img" }, new int[] {
						R.id.text1, R.id.img });
		setListAdapter(adpter);
	}

	/* �趨ListItem������ʱҪ���Ķ��� */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		File file = new File(paths.get(position));
		final String path = paths.get(position);
		if (file.canRead()) {
			if (file.isDirectory()) {
				/* ������ļ��о��ٽ�ȥ��ȡ */
				getFileDir(paths.get(position));
			} else {
				filename = file.getName();
				/* ������ļ����򵯳�AlertDialog */
				new AlertDialog.Builder(this)
						.setTitle("发送文件")
						.setMessage("发送文件 [ " + filename + " ] ?")
						.setPositiveButton("确定!",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent();

										intent.setClass(FileListActivity.this,
												FileUploadActivity.class);

										intent.putExtra("fileName", ""
												+ filename);

										intent.putExtra("filePath", "" + path);

										intent.putExtra("name", name);
										intent.putExtra("password", password);
										startActivity(intent);
									}
								})
						.setNegativeButton("取消!",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).show();
			}
		} else {
			/* ����AlertDialog��ʾȨ�޲��� */
			new AlertDialog.Builder(this)
					.setTitle("Message")
					.setMessage("无权限!")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
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
		
		//上下文菜单，本例会通过长按条目激活上下文菜单    
		@Override    
		public void onCreateContextMenu(ContextMenu menu, View view,  ContextMenuInfo menuInfo) { 
			menu.setHeaderTitle("操作");        //添加菜单项        
			menu.add(0, ITEM1, 0, "传文件");        
			menu.add(0, ITEM2, 0, "打开");        
			menu.add(0, ITEM3, 0, "属性");    
		}
		
		public boolean onContextItemSelected(MenuItem item){
			
			//取得List位置
            int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
            String path = paths.get(selectedPosition);
            File file = new File(path);
            String filename = file.getName();
			switch(item.getItemId()){
			case ITEM1:                               //传文件
				Intent intent = new Intent();
				intent.setClass(FileListActivity.this,
						FileUploadActivity.class);
				intent.putExtra("fileName", ""
						+ filename);
				
				intent.putExtra("filePath", "" + path);
				intent.putExtra("name", name);
				intent.putExtra("password", password);
				startActivity(intent);
				break;
			case ITEM2:                    //打开文件
				openFile(filename,path);
				break;
			case ITEM3:                   //文件属性
				
				long length = file.length();
				String size = "";
				if(length > 1000000)
					size = length/(1024*1024) + "MB";
				else if(length > 1000)
					size = length/1024 + "KB";
				else 
					size = length + "B";
				new AlertDialog.Builder(this)
				.setTitle("文件属性")
				.setMessage("文件名: " + filename + "\n" + "文件路径: " + path + "\n" + "文件大小: " + size)
				.setPositiveButton("好",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
				break;
			}
			return true;
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
		private String getPre(String name){
			char[] pre = new char[11];
			if(name.length()>10)
				name.getChars(0, 10, pre, 0);
			return pre.toString();
		}
}
