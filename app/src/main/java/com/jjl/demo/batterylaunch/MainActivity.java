package com.jjl.demo.batterylaunch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	BatteryView mBatteryView;
	private boolean mPowerConnected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mBatteryView = (BatteryView) findViewById(R.id.battery_view);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		register();
		if(mPowerConnected){
			mBatteryView.setVisibility(View.VISIBLE);
			mBatteryView.startAnim();
		}else{
			mBatteryView.setVisibility(View.GONE);
			mBatteryView.stopAnim();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBatteryView.stopAnim();
		unregister();
	}

	private void register() {
		//获取当前是否连接
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		// 是否在充电
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		mPowerConnected = (status == BatteryManager.BATTERY_STATUS_CHARGING)
				|| (status == BatteryManager.BATTERY_STATUS_FULL);

		registerReceiver(powerConnectedReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
		registerReceiver(powerDisConnectedReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
		registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	private void unregister() {
		unregisterReceiver(batteryChangedReceiver);
		unregisterReceiver(powerConnectedReceiver);
		unregisterReceiver(powerDisConnectedReceiver);
	}

	// 接受广播
	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.d("Deom", "batteryChangedReceiver：:" );
			if (mPowerConnected) {
				if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
					int level = intent.getIntExtra("level", 0);
					int power = intent.getIntExtra("scale", 100);
					mBatteryView.setPower(level);
				}
			}
		}
	};

	private BroadcastReceiver powerConnectedReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			Log.d("Deom", "powerConnectedReceiver：:" );
			if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
				// 插上外部电源时发出的广播
				mPowerConnected = true;
				mBatteryView.setVisibility(View.VISIBLE);
				int level = intent.getIntExtra("level", 0);
				int power = intent.getIntExtra("scale", 100);
				mBatteryView.setPower(level);
			}
		}
	};
	private BroadcastReceiver powerDisConnectedReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			Log.d("Deom", "powerDisConnectedReceiver：:" );
			if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
				mPowerConnected = false;
				mBatteryView.setVisibility(View.GONE);
				mBatteryView.stopAnim();
			}
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		mBatteryView.stopAnim();
	};

}
