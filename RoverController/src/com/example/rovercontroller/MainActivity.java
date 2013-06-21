package com.example.rovercontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final int K_PORT = 3333;
	
	private Activity act = this;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupConnection();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		
		
		

		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void setReceivedText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView receivedText = (TextView) act.findViewById(R.id.receivedText);
				receivedText.setText(receivedText.getText() + "\n" + text);
				final ScrollView scrollView = (ScrollView) act.findViewById(R.id.receivedTextScroll);
				
				receivedText.post(new Runnable() {	
					@Override
					public void run() {
						scrollView.fullScroll(View.FOCUS_DOWN);
					}
				});
			}
		});
	}

	public void setupConnection() {
		Button connectButton = (Button) act.findViewById(R.id.connectButton);

		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncTask<Void, Void, Void>() {
					public Void doInBackground(Void... params) {
						EditText ipAddress = (EditText) act.findViewById(R.id.ipAddress);
						ParcelFileDescriptor pfd=null;
						try {
							InetAddress inetAddress = InetAddress.getByName(ipAddress.getText().toString());
							socket = new Socket(inetAddress, 3333);
							pfd = ParcelFileDescriptor.fromSocket(socket);
//							out = new PrintWriter(socket.getOutputStream(),true);
//							in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

						} catch (UnknownHostException e) {
							setReceivedText("No such host");
							e.printStackTrace();
						} catch (IOException e) {
							setReceivedText("Couldn't open socket");
							e.printStackTrace();
						}

						if (socket == null || !socket.isConnected()) {
							Log.e("FPGA", "Socket failed to create or is otherwise not connected.");
							return null;
						}
						MediaPlayer mMediaPlayer = new MediaPlayer();
						SurfaceView sv = (SurfaceView) act.findViewById(R.id.surfaceView1);
						
						try {
							mMediaPlayer.setDataSource(pfd.getFileDescriptor());
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					    SurfaceHolder sh = sv.getHolder();
					    synchronized (this) {
					       	mMediaPlayer.setDisplay(sv.getHolder());
					        try {
								mMediaPlayer.prepare();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    }
						
						mMediaPlayer.start();
						
//						while(true);
//						String receivedText;
//						try {
//							while (true) {
//								receivedText = in.readLine();
//								if (receivedText != null) {
//									setReceivedText(receivedText);
//									Log.d("FPGA", "Got a message: " + receivedText);
//								} else {
//									break;
//								}
//							}
//						} catch (IOException e) {
//							setReceivedText("Something went wrong...");
//							e.printStackTrace();
//						}
						
						return null;
					}
				}.execute();

			}
		});
	}
}
