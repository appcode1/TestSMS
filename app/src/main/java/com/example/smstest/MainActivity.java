package com.example.smstest;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final int MY_PERMISSION_SMS = 111;
	private TextView sender;

	private TextView content;

	private EditText to;

	private EditText msgInput;

	private Button send;

	private IntentFilter receiveFilter;

	private MessageReceiver messageReceiver;

	private IntentFilter sendFilter;

	private SendStatusReceiver sendStatusReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sender = (TextView) findViewById(R.id.sender);
		content = (TextView) findViewById(R.id.content);
		to = (EditText) findViewById(R.id.to);
		msgInput = (EditText) findViewById(R.id.msg_input);
		send = (Button) findViewById(R.id.send);
		receiveFilter = new IntentFilter();
		receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		receiveFilter.setPriority(100);
		messageReceiver = new MessageReceiver();
		registerReceiver(messageReceiver, receiveFilter);
		sendFilter = new IntentFilter();
		sendFilter.addAction("SENT_SMS_ACTION");
		sendStatusReceiver = new SendStatusReceiver();
		registerReceiver(sendStatusReceiver, sendFilter);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SmsManager smsManager = SmsManager.getDefault();
				Intent sentIntent = new Intent("SENT_SMS_ACTION");
				PendingIntent pi = PendingIntent.getBroadcast(
						MainActivity.this, 0, sentIntent, 0);
				smsManager.sendTextMessage(to.getText().toString(), null,
						msgInput.getText().toString(), pi, null);
			}
		});

		checkAndRequestPermission();

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(messageReceiver);
		unregisterReceiver(sendStatusReceiver);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSION_SMS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission Granted
					Toast.makeText(this, "permission is granted successfully", Toast.LENGTH_SHORT).show();
				} else {
					// Permission Denied
					Toast.makeText(this, "permission is denied", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}


	}

	private void checkAndRequestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, "Please grant this App the permission to send SMS", Toast.LENGTH_LONG).show();
			ActivityCompat.requestPermissions( this,
					new String[] {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.BROADCAST_SMS },
					MY_PERMISSION_SMS );
		}

	}


	class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < messages.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			String address = messages[0].getOriginatingAddress();
			String fullMessage = "";
			for (SmsMessage message : messages) {
				fullMessage += message.getMessageBody();
			}
			sender.setText(address);
			content.setText(fullMessage);
			abortBroadcast();
		}

	}

	class SendStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (getResultCode() == RESULT_OK) {
				Toast.makeText(context, "Send succeeded", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(context, "Send failed", Toast.LENGTH_LONG)
						.show();
			}
		}

	}

}
