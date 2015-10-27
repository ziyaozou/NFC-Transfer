package com.example.nfc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

public class WifiConfig {
	final int WIFI_AP_STATE_DISABLING = 10;
	final int WIFI_AP_STATE_DISABLED = 11;
	final int WIFI_AP_STATE_ENABLING = 12;
	final int WIFI_AP_STATE_ENABLED = 13;
	final int WIFI_AP_STATE_FAILED = 14;
	WifiManager manager;
	Handler handler;
	Handler closeHandler;
	Runnable closeThread;
	Runnable startThread;
	Runnable openWifiThread;

	String name;
	String password;

	public WifiConfig(Context context, String name, String password) {
		manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		handler = new Handler();
		this.name = name;
		this.password = password;
	}

	public void connectToWifi() {
		if (!manager.isWifiEnabled()) {
			manager.setWifiEnabled(true);
		}

		openWifiThread = new WifiThread();
		Thread th = new Thread(openWifiThread);
		th.start();
	}

	public void closeWifiAp() {
		try {
			Method method = manager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			if(getWifiApState()==WIFI_AP_STATE_ENABLED){
				method.invoke(manager, null, false);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeWifiConnect() {
		WifiInfo info = manager.getConnectionInfo();
		manager.removeNetwork(info.getNetworkId());
		manager.setWifiEnabled(false);

	}

	public void startWifiAp() {
		handler = new Handler();
		if (manager.isWifiEnabled()) {
			closeHandler = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					startWifiApThread();
					super.handleMessage(msg);
				}

			};

			closeThread = new CloseWifiThread();
			Thread th = new Thread(closeThread);
			th.start();
		} else {
			startWifiApThread();
		}

	}

	class WifiThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int state = manager.getWifiState();
			if (state == WifiManager.WIFI_STATE_DISABLED
					|| state == WifiManager.WIFI_STATE_DISABLING
					|| state == WifiManager.WIFI_STATE_ENABLING
					|| state == WifiManager.WIFI_STATE_UNKNOWN) {

				handler.postDelayed(openWifiThread, 1000);
				System.out.println("checking wifi state");

			} else if (state == WifiManager.WIFI_STATE_ENABLED) {
				WifiConfiguration config = new WifiConfiguration();
				config.SSID = "\"" + name + "\"";
				config.preSharedKey = "\"" + password + "\"";
				manager.addNetwork(config);

				System.out.println("name-> " + config.SSID);
				List<WifiConfiguration> list = manager.getConfiguredNetworks();

				for (WifiConfiguration i : list) {
					System.out.println("ssid-> " + i.SSID);
					if (i.SSID != null && i.SSID.equals("\"" + name + "\"")) {
						System.out.println( "ssid is " + name + " connecting.." );
						manager.disableNetwork(i.networkId);
						manager.enableNetwork(i.networkId, true);
						manager.reconnect();
						return;
					}
				}

				handler.postDelayed(openWifiThread, 1000);
			}

		}

	}

	class CloseWifiThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int state = manager.getWifiState();
			if (state == WifiManager.WIFI_STATE_ENABLED) {
				manager.setWifiEnabled(false);
				closeHandler.postDelayed(closeThread, 1000);

			} else if (state == WifiManager.WIFI_STATE_DISABLING) {
				closeHandler.postDelayed(closeThread, 1000);
			} else if (state == WifiManager.WIFI_STATE_DISABLED) {
				closeHandler.sendEmptyMessage(0);
			}
		}

	}

	private void startWifiApThread() {
		startThread = new StartThread(name, password);
		Thread th = new Thread(startThread);
		th.start();
	}

	class StartThread implements Runnable {

		private String name;
		private String password;

		public StartThread(String name, String password) {
			this.name = name;
			this.password = password;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int state = getWifiApState();
			if (state == WIFI_AP_STATE_DISABLED) {
				openWifiAp(true);
				System.out.println("wifi ap is opening...");
				handler.postDelayed(startThread, 1000);
			} else if (state == WIFI_AP_STATE_ENABLING
					|| state == WIFI_AP_STATE_FAILED) {
				handler.postDelayed(startThread, 1000);
				System.out.println("wifi ap is not enabled..checking...");
			} else if (state == WIFI_AP_STATE_ENABLED) {
				openWifiAp(true);
				String name = getApName();

				// while( (name = getApName())==null);
				System.out.println("Ap Name-> " + name);
				if (name == null) {
					handler.postDelayed(startThread, 1000);
					return;
				}
				if (!name.equals(name)) {
					openWifiAp(false);
					System.out.println("Wifi AP restarting!");
					openWifiAp(true);
				}
			}
		}

	}

	public void openWifiAp(boolean flag) {

		String manufatory = android.os.Build.MANUFACTURER;

		if (manufatory.equals("HTC")) {
			openHtcWifiAp(flag);
			System.out.println("this is " + manufatory);
		} else {
			openOtherWifiAp(flag);
		}
	}

	private void openHtcWifiAp(boolean flag) {
		if (flag) {
			WifiConfiguration config = new WifiConfiguration();
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

			config.SSID = name;
			config.preSharedKey = password;

			Field localField1;

			try {

				localField1 = WifiConfiguration.class
						.getDeclaredField("mWifiApProfile");

				localField1.setAccessible(true);

				Object localObject2 = localField1.get(config);

				localField1.setAccessible(false);

				if (localObject2 != null) {

					Field localField5 = localObject2.getClass()
							.getDeclaredField("SSID");

					localField5.setAccessible(true);

					localField5.set(localObject2, config.SSID);

					localField5.setAccessible(false);

					Field localField4 = localObject2.getClass()
							.getDeclaredField("key");

					localField4.setAccessible(true);

					localField4.set(localObject2, config.preSharedKey);

					localField4.setAccessible(false);
					System.out.println("HTC set wifi ap enabled 0.0..");

				}

				System.out.println("HTC set wifi ap enabled.0.1.");
				Method method = manager.getClass().getMethod(
						"setWifiApEnabled", WifiConfiguration.class,
						boolean.class);

				method.invoke(manager, config, true);
				System.out.println("HTC set wifi ap enabled..");

			} catch (Exception e) {

				e.printStackTrace();

			}
		} else {
			try {
				Method method = manager.getClass().getMethod(
						"setWifiApEnabled", WifiConfiguration.class,
						boolean.class);
				method.invoke(manager, null, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void openOtherWifiAp(boolean flag) {
		Method method = null;
		// Method method2 = null;

		System.out.println("using invoke method to open wifi ap...");
		try {
			method = manager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			// method2 = manager.getClass().getMethod("setWifiApConfiguration",
			// WifiConfiguration.class);
			if (flag) {
				WifiConfiguration config = new WifiConfiguration();
				config.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.CCMP);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.TKIP);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.TKIP);

				config.SSID = name;
				config.preSharedKey = password;

				// method2.invoke(manager, config );

				method.invoke(manager, config, true);
			} else {
				method.invoke(manager, null, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getWifiApState() {
		try {
			Method method = manager.getClass().getMethod("getWifiApState");
			int i = (Integer) method.invoke(manager);
			System.out.println("Wifi Ap state is " + i);
			return i;
		} catch (Exception e) {
			System.out.println("Can not get Wifi AP state.");
			e.printStackTrace();
			return -1;
		}
	}

	private String getApName() {
		if (android.os.Build.MANUFACTURER.equals("HTC")) {
			Field localField1;
			WifiConfiguration config = new WifiConfiguration();
			try {

				localField1 = WifiConfiguration.class
						.getDeclaredField("mWifiApProfile");

				localField1.setAccessible(true);

				Object localObject2 = localField1.get(config);

				localField1.setAccessible(false);

				if (localObject2 != null) {

					Field localField5 = localObject2.getClass()
							.getDeclaredField("SSID");
					String ssid = "";
					ssid = (String)localField5.get(localObject2);
					return ssid;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Method method = manager.getClass().getMethod(
						"getWifiApConfiguration");
				WifiConfiguration config = (WifiConfiguration) method
						.invoke(manager);
				return config.SSID;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}
}
