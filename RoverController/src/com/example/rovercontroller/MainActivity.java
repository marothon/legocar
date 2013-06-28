package com.example.rovercontroller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
				TextView receivedText = (TextView) act
						.findViewById(R.id.receivedText);
				receivedText.setText(receivedText.getText() + "\n" + text);
				// final ScrollView scrollView = (ScrollView)
				// act.findViewById(R.id.receivedTextScroll);

				// receivedText.post(new Runnable() {
				// @Override
				// public void run() {
				// scrollView.fullScroll(View.FOCUS_DOWN);
				// }
				// });
			}
		});
	}

	public void setupConnection() {
		Button connectButton = (Button) act.findViewById(R.id.connectButton);

		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new CameraStream().execute();
			}
		});
	}

	class CameraStream extends AsyncTask<Void, Void, Void> implements
			SurfaceHolder.Callback {
		SurfaceView sv;
		SurfaceHolder sh;
		MediaPlayer mMediaPlayer; 

		public CameraStream() {
			sv = (SurfaceView) act.findViewById(R.id.surfaceView1);
			sh = sv.getHolder();
			sh.addCallback(this);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDisplay(sh);
		}

		public Void doInBackground(Void... params) {
			EditText ipAddress = (EditText) act.findViewById(R.id.ipAddress);
			ParcelFileDescriptor pfd = null;
			BufferedInputStream bis;
			PrintWriter pw;
			File f=null;
			try {
				InetAddress inetAddress = InetAddress.getByName(ipAddress
						.getText().toString());
				socket = new Socket(inetAddress, 3333);
				f = File.createTempFile("fpgastream", ".3gp", act.getCacheDir());
				bis = new BufferedInputStream(socket.getInputStream());
				pw = new PrintWriter(f);
				new BufferedVideoStream(pw, bis).start();
				Thread.sleep(5000);
				// out = new PrintWriter(socket.getOutputStream(),true);
				// in = new BufferedReader(new
				// InputStreamReader(socket.getInputStream()));

			} catch (UnknownHostException e) {
				setReceivedText("No such host");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				setReceivedText("Couldn't open socket");
				e.printStackTrace();
				return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}

			if (socket == null || !socket.isConnected()) {
				Log.e("FPGA",
						"Socket failed to create or is otherwise not connected.");
				return null;
			}
			Log.d("FPGA", f.getAbsolutePath());
			try {
				mMediaPlayer.setDataSource(f.getAbsolutePath());
				mMediaPlayer.prepareAsync();
				mMediaPlayer.start();
			}catch (Exception e) {
				e.printStackTrace();
			}

			// while(true);
			// String receivedText;
			// try {
			// while (true) {
			// receivedText = in.readLine();
			// if (receivedText != null) {
			// setReceivedText(receivedText);
			// Log.d("FPGA", "Got a message: " + receivedText);
			// } else {
			// break;
			// }
			// }
			// } catch (IOException e) {
			// setReceivedText("Something went wrong...");
			// e.printStackTrace();
			// }

			return null;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mMediaPlayer.setDisplay(sh);
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}
	}
	
	/**
	 * 
	 * 
	 * 
	 * @author marothon
	 *
	 */
	class BufferedVideoStream extends Thread{
		private PrintWriter _pw;
		private BufferedInputStream _bis;
			
		public BufferedVideoStream(PrintWriter pw, BufferedInputStream bis){
			_pw = pw;
			_bis = bis;
		}
		
		@Override
		public void run() {
			while(true){
				try {
					_pw.write(_bis.read());
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
	}
}
