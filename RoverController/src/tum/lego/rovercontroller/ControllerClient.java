package tum.lego.rovercontroller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * 
 * Android app to control lego car via a FPGA board.
 *
 */
public class ControllerClient extends Activity {
	public static final int K_PORT = 3333;

	private Activity act = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_controllerapp);
		setupConnection();
	}

	@Override
	protected void onStart() {
		super.onStart();
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
				new CameraStreamer().execute();
			}
		});
	}
	/**
	 * 
	 *	Client class to connect to onboard android phone and FPGA wifi module.
	 *
	 */
	class CameraStreamer extends AsyncTask<Void, Void, Void> {
		private Socket cameraSocket, fpgaSocket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private static final String FPGA_IP="192.168.43.112";

		public Void doInBackground(Void... params) {
			EditText ipAddress = (EditText) act.findViewById(R.id.ipAddress);
			String ipString = ipAddress.getText().toString().equals("") ? "192.168.43.1" : ipAddress.getText().toString();  
			try {
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
			
			hideConnectionMenu(true);
			new CommandStream(out).start();
			
			byte img[] = null;
			try {
				while (true) {
					img = (byte[]) in.readObject();
					if (img != null) {
						showImage(img);
					} else {
						break;
					}
				}
			} catch (Exception e) {
				Log.d("Rover", "Lost connection: "+e.toString());
				hideConnectionMenu(false);
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
	/**
	 * 
	 * Initialize android UI listeners for sending commands to FPGA.
	 * 
	 */
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
	
	/**
	 * 
	 * Android UI listeners to send input to the FPGA Wifi module.
	 * 
	 */
	class CommandListener implements VerticalSeekBar.OnSeekBarChangeListener{
		ObjectOutputStream out;
		boolean isLeft;
		String lastCommand;
		
		public CommandListener(ObjectOutputStream out, boolean left){
			this.out = out;
			isLeft = left;
		}
		
		private void sendInput(int leftVal, int rightVal){
			String command = CommandInterface.createCommand(leftVal, rightVal);
			lastCommand = command;
			Log.d("Rover","Sending: "+command);
			try {
				out.writeObject(command);
			} catch (IOException e) {
				Log.d("Rover", "Could not send command: "+e.toString());
			}
		}

		@Override
		public void onProgressChanged(SeekBar self, int arg1, boolean arg2) {
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
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
		
	}
}
