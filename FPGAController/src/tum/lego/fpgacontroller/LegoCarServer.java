package tum.lego.fpgacontroller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.*;
import java.net.*;
import java.util.List;

public class LegoCarServer extends Activity {
	// public static String wifi_mod_ip = "1.2.3.4";
	// public static int socket = 3003;
	private static Activity act;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		act = this;
		setContentView(R.layout.activity_lego_car_controller);
		new connectTask().execute("");
	}

	class connectTask extends AsyncTask<String, String, CameraServer> {
		@Override
		protected CameraServer doInBackground(String... params) {
			Log.d("FPGA", "THREAD START");
			new CameraServer().start();
			return null;
		}
	}

	class CameraServer extends Thread {
		ServerSocket providerSocket;
		Socket connection = null;
		ObjectOutputStream out;

		public void cam() {
			Log.d("FPGA", "Get cam");
			int[] fpsrange = new int[2];
			
			Camera cam = Camera.open();
			Camera.Parameters param = cam.getParameters();
//			List<int[]> fpslist = param.getSupportedPreviewFpsRange();
//			Log.d("camera", "size= " + fpslist.size());
//			for (int i=0;i < fpslist.size();i++) {
//			 Log.d("camera", i + " fps= " + fpslist.get(i)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]);
//			 Log.d("camera", i + " fps= " + fpslist.get(i)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
//			}
			param.setPreviewFpsRange(10000,10000);
			param.setRotation(90);
			cam.setParameters(param);
			
			for(int i=0; i<500; i++) cam.addCallbackBuffer(new byte[cam.getParameters().getPreviewSize().height*cam.getParameters().getPreviewSize().width*2]);
			cam.setPreviewCallbackWithBuffer(new Snapshot(out));
			cam.startPreview();
		}

		public void run() {
			Log.d("FPGA", "THREAD RUNNING 0");
			try {
				// 1. creating a server socket
				providerSocket = new ServerSocket(3333);
				// 2. Wait for connection
				System.out.println("Waiting for connection");
				connection = providerSocket.accept();
				System.out.println("Connection received from "
						+ connection.getInetAddress().getHostName());
				// 3. get Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				cam();
				while(true){
				}
				
			} catch (IOException ioException) {
				// ioException.printStackTrace();
			} finally {
				// 4: Closing connection
				try {
					// in.close();
					out.close();
					providerSocket.close();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}

		void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				System.out.println("server>" + msg);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	/*
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

		public Snapshot() {
		}
		
		Parameters parameters;
		Camera.Size size;
		YuvImage yuvImage;
		byte[] imageBytes;
		ByteArrayOutputStream outsie = new ByteArrayOutputStream();
		public void onPreviewFrame(byte[] data, Camera camera) {
			outsie.reset();
			parameters = camera.getParameters();
			int imageFormat = parameters.getPreviewFormat();
			size = parameters.getPreviewSize();
			Log.d("FPGA", "FRAME AVAILABLE");
			// TextureView tv;
			ph = size.height;
			pw = size.width;
			Log.d("FPGA", "ph: "+ph+"pw: "+pw);
			yuvImage = new YuvImage(data, imageFormat, pw, ph,
					null);
			camera.addCallbackBuffer(data);
			yuvImage.compressToJpeg(new Rect(0, 0, pw, ph), 50, outsie);
			imageBytes = outsie.toByteArray();
//			Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0,
//					imageBytes.length);
			try {
				out.writeObject(imageBytes);
				out.flush();
				Log.d("FPGA", "IMAGE SENT MAN");
			} catch (Exception e) {
				Log.d("FPGA", "FAILED TO SEND IMAGE, MAN");
				e.printStackTrace();
			}
			// iv.setImageBitmap(image);

		}
	}
}
