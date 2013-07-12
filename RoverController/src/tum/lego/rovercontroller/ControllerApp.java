package tum.lego.rovercontroller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ControllerApp extends Activity {
	public static final int K_PORT = 3333;

	private Activity act = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_controllerapp);
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
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
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

	class CameraStream extends AsyncTask<Void, Void, Void> {
		private Socket cameraSocket, fpgaSocket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private static final String FPGA_IP="192.168.43.112";
		public CameraStream() {
		}

		public Void doInBackground(Void... params) {
			EditText ipAddress = (EditText) act.findViewById(R.id.ipAddress);
			String ipString = ipAddress.getText().toString().equals("") ? "192.168.43.1" : ipAddress.getText().toString();  
			Bitmap image;
			try {
				Log.d("Rover", ipString);
				InetAddress inetAddress = InetAddress.getByName(ipString);
				cameraSocket = new Socket(inetAddress, 3333);
				fpgaSocket = new Socket(FPGA_IP, 2000);
				
				in = new ObjectInputStream(cameraSocket.getInputStream());
				out = new ObjectOutputStream(fpgaSocket.getOutputStream());
			} catch (UnknownHostException e) {
				setReceivedText("No such host");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				setReceivedText("Couldn't open socket");
				e.printStackTrace();
				return null;
			}

			if (cameraSocket == null || !cameraSocket.isConnected() || fpgaSocket == null || !fpgaSocket.isConnected()) {
				Log.e("Rover",
						"Sockets have failed to be created or are otherwise not connected.");
				return null;
			}
			/* Found host, hide connection window */
			hideConnectionMenu(true);
			try {
				out.writeObject("I HAVE CONNECTED");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new CommandStream(out).start();
//			// String receivedText;
			byte img[] = null;
			try {
				while (true) {
					img = (byte[]) in.readObject();
					if (img != null) {
						showImage(img);
						Log.d("FPGA", "Got an image");
					} else {
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				hideConnectionMenu(false);
				/* Connection lost, restore connection button */
				setReceivedText("Lost connection to LEGO-car");
			}
			
			return null;
		}

		private void hideConnectionMenu(final boolean yes) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					View ipAddress = act.findViewById(R.id.ipAddress);
					View butt = act.findViewById(R.id.connectButton);
					View lCtrl = act.findViewById(R.id.leftControl);
					View rCtrl = act.findViewById(R.id.rightControl);
					View image = act.findViewById(R.id.cameraFrame);
					int vis = yes ? View.INVISIBLE : View.VISIBLE;
					ipAddress.setVisibility(vis);
					butt.setVisibility(vis);
					vis = !yes ? View.INVISIBLE : View.VISIBLE;
					image.setVisibility(vis);
					lCtrl.setVisibility(vis);
					rCtrl.setVisibility(vis);
				}
			});
		}

		Bitmap image = null;
		ImageView iv = null;
		private void showImage(final byte img[]) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					image = BitmapFactory.decodeByteArray(img, 0,
							img.length);
					iv = (ImageView) act
							.findViewById(R.id.cameraFrame);
					iv.setImageBitmap(image);
				}
			});
		}
	}
	/* Sends commands as soon as the values on the slides change */
	class CommandStream extends Thread{
		ObjectOutputStream out;
		public CommandStream(ObjectOutputStream o){
			out = o;
		}
		
		public void run(){
			VerticalSeekBar left_vsb = (VerticalSeekBar) act.findViewById(R.id.leftControl);
			VerticalSeekBar right_vsb = (VerticalSeekBar) act.findViewById(R.id.rightControl);
			left_vsb.setOnSeekBarChangeListener(new CommandListener(out, true));
			right_vsb.setOnSeekBarChangeListener(new CommandListener(out, false));
			Log.d("Rover", "Set the listeners");
		}
		
	}
	
	class CommandListener implements VerticalSeekBar.OnSeekBarChangeListener{
		ObjectOutputStream out;
		CommandInterface comm;
		boolean isLeft;
		String lastCommand;
		
		public CommandListener(ObjectOutputStream out, boolean left){
			this.out = out;
			isLeft = left;
			comm = new CommandInterface();
		}
		
		private void sendInput(int leftVal, int rightVal){
			String command = comm.createCommand(leftVal, rightVal);
			lastCommand = command;
			Log.d("Rover","Sending: "+command);
			try {
				out.writeObject(command);
				Log.d("Rover","Sent");
			} catch (IOException e) {
				Log.d("ROVER", "Failed to send command: "+e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void onProgressChanged(SeekBar self, int arg1, boolean arg2) {
			Log.d("Rover", "Progress change");
			int leftVal, rightVal;
			if(isLeft){ 
				leftVal = (((VerticalSeekBar) self).getProgress()-50)*2;
				rightVal = (((VerticalSeekBar) act.findViewById(R.id.rightControl)).getProgress()-50)*2;
			}else{
				rightVal = (((VerticalSeekBar) self).getProgress()-50)*2;
				leftVal = (((VerticalSeekBar) act.findViewById(R.id.leftControl)).getProgress()-50)*2;
			}
			sendInput(leftVal, rightVal);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
	}
}
