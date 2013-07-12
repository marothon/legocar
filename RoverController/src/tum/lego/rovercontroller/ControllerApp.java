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
		private Socket socket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		
		public CameraStream() {
		}

		public Void doInBackground(Void... params) {
			EditText ipAddress = (EditText) act.findViewById(R.id.ipAddress);

			Bitmap image;
			try {
				InetAddress inetAddress = InetAddress.getByName(ipAddress
						.getText().toString());
				socket = new Socket(inetAddress, 3333);
				in = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream());
			} catch (UnknownHostException e) {
				setReceivedText("No such host");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				setReceivedText("Couldn't open socket");
				e.printStackTrace();
				return null;
			}

			if (socket == null || !socket.isConnected()) {
				Log.e("FPGA",
						"Socket failed to create or is otherwise not connected.");
				return null;
			}
			/* Found host, hide connection window */
			hideConnectionMenu(true);
			
			// String receivedText;
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

		private void showImage(final byte img[]) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Bitmap image = BitmapFactory.decodeByteArray(img, 0,
							img.length);
					ImageView iv = (ImageView) act
							.findViewById(R.id.cameraFrame);
					iv.setImageBitmap(image);
				}
			});
		}
	}
	/* Sends commands as soon as the values on the slides change */
	class CommandStream extends AsyncTask<Void, Void, Void>{
		ObjectOutputStream out;
		public CommandStream(ObjectOutputStream o){
			out = o;
		}
		@Override
		protected Void doInBackground(Void... params) {
			VerticalSeekBar left_vsb = (VerticalSeekBar) act.findViewById(R.id.leftControl);
			VerticalSeekBar right_vsb = (VerticalSeekBar) act.findViewById(R.id.rightControl);
			left_vsb.setOnDragListener(new CommandListener(out, true));
			right_vsb.setOnDragListener(new CommandListener(out, false));
			return null;
		}
		
	}
	
	class CommandListener implements OnDragListener{
		ObjectOutputStream out;
		CommandInterface comm;
		boolean isLeft;
		
		public CommandListener(ObjectOutputStream out, boolean left){
			this.out = out;
			isLeft = left;
			comm = new CommandInterface();
		}
		
		@Override
		public boolean onDrag(View self, DragEvent draggy) {
			if(draggy.getAction() != draggy.ACTION_DRAG_ENDED) return false;
			
			int leftVal, rightVal;
			if(isLeft){ 
				leftVal = (((VerticalSeekBar) self).getProgress()-50)*2;
				rightVal = (((VerticalSeekBar) act.findViewById(R.id.rightControl)).getProgress()-50)*2;
			}else{
				rightVal = (((VerticalSeekBar) self).getProgress()-50)*2;
				leftVal = (((VerticalSeekBar) act.findViewById(R.id.leftControl)).getProgress()-50)*2;
			}
			sendInput(leftVal, rightVal);
			return false;
		}
		
		private void sendInput(int leftVal, int rightVal){
			short command = comm.createCommand(leftVal, rightVal, true);
			try {
				out.writeShort(command);
			} catch (IOException e) {
				Log.d("ROVER", "Failed to send command: "+e.toString());
				e.printStackTrace();
			}
		}
		
	}
}
