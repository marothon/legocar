package tum.lego.fpgacam;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.*;
import java.net.*;

/**
 * 
 * 	App for the onboard phone to capture video feed and send to controller phone.
 * 
 */
public class CameraServer extends Activity {
	private static Activity act;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		act = this;
		setContentView(R.layout.activity_lego_car_controller);
		new connectTask().execute("");
	}
	
	/**
	 * 
	 * Starts the camera stream.
	 * 
	 */
	class connectTask extends AsyncTask<String, String, CameraStream> {
		@Override
		protected CameraStream doInBackground(String... params) {
			new CameraStream().start();
			return null;
		}
	}
	
	/**
	 * 
	 * Thread for TCP/IP socket to the controller phone. Sends the preview
	 * frames of the camera to the controller. 
	 *
	 */
	class CameraStream extends Thread {
		ServerSocket providerSocket;
		Socket connection = null;
		ObjectOutputStream out;

		/**
		 * Initializes the camera.
		 */
		public void cam() {
			Camera cam = Camera.open();
			Camera.Parameters param = cam.getParameters();

			param.setPreviewFpsRange(10000, 10000);
			param.setRotation(90);
			cam.setParameters(param);

			for (int i = 0; i < 500; i++)
				cam.addCallbackBuffer(new byte[(int)((float) cam.getParameters()
						.getPreviewSize().height
						* cam.getParameters().getPreviewSize().width * 1.5)]);
			cam.setPreviewCallbackWithBuffer(new Snapshot(out));
			cam.startPreview();
		}
		
		/**
		 * 
		 * Establish socket connection.
		 * 
		 */
		public void run() {
			try {
				providerSocket = new ServerSocket(3333);
				console("Waiting for connection");
				connection = providerSocket.accept();
				console("Connection received from "
						+ connection.getInetAddress().getHostName());
				out = new ObjectOutputStream(connection.getOutputStream());
				cam();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * Prints a message to the screen.
	 * 
	 * @param msg The message.
	 */
	public void console(final String msg){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				final ScrollView sv = (ScrollView) act.findViewById(R.id.scrollView1);
				TextView tv = ((TextView)sv.findViewById(R.id.console));
				tv.setText(tv.getText()+"\n"+msg);
				sv.post(new Runnable(){
					@Override
					public void run() {
						sv.fullScroll(View.FOCUS_DOWN);
					}
					
				});
			}
		});
	}

	/**
	 * Class to capture "preview" frames of camera, i.e. camera frames before
	 * capturing an image
	 */
	class Snapshot implements Camera.PreviewCallback {
		int pw, ph;
		ObjectOutputStream out;
		long start;
		ImageView iv;

		public Snapshot(ObjectOutputStream out) {
			this.out = out;
		}
		
		Parameters parameters;
		Camera.Size size;
		YuvImage yuvImage;
		byte[] imageBytes;
		ByteArrayOutputStream outsie = new ByteArrayOutputStream();
		
		/**
		 * 
		 * Writes the preview frame to the provided socket.
		 * 
		 */
		public void onPreviewFrame(byte[] data, Camera camera) {
			outsie.reset();
			parameters = camera.getParameters();
			int imageFormat = parameters.getPreviewFormat();
			size = parameters.getPreviewSize();

			ph = size.height;
			pw = size.width;

			yuvImage = new YuvImage(data, imageFormat, pw, ph, null);
			camera.addCallbackBuffer(data);
			yuvImage.compressToJpeg(new Rect(0, 0, pw, ph), 100, outsie);
			imageBytes = outsie.toByteArray();
			try {
				out.writeObject(imageBytes);
				out.flush();
			} catch (Exception e) {
				Log.d("FPGA", "Could not send image: "+e.toString());
				console("Lost connection. Restarting wait.");
				camera.release();
				new connectTask().execute("");
			}

		}
	}
}
