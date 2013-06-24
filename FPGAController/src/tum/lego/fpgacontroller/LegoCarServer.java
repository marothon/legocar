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

	class connectTask extends AsyncTask<String, String, Server> {
		@Override
		protected Server doInBackground(String... params) {
			Log.d("FPGA", "THREAD START");
			new Server().start();
			return null;
		}
	}

	class Server extends Thread {
		ServerSocket providerSocket;
		Socket connection = null;
		ObjectOutputStream out;
		ObjectInputStream in;
		String message;

		public void cam() {
			Log.d("FPGA", "Get cam");
			new LiveCamera();
//			Camera cam = Camera.open();
//			Log.d("FPGA", " " + cam.getParameters());
//			cam.setPreviewCallback(new Snapshot());
//			cam.startPreview();
		}

		public void run() {
			Log.d("FPGA", "THREAD RUNNING 0");
			cam();

			// try {
			// // 1. creating a server socket
			// providerSocket = new ServerSocket(3333);
			// // 2. Wait for connection
			// System.out.println("Waiting for connection");
			// connection = providerSocket.accept();
			// System.out.println("Connection received from "
			// + connection.getInetAddress().getHostName());
			// // 3. get Input and Output streams
			// // out = new ObjectOutputStream(connection.getOutputStream());
			// // out.flush();
			//
			// ParcelFileDescriptor pfd = ParcelFileDescriptor
			// .fromSocket(connection);
			// Camera mCamera = getCameraInstance();
			// SurfaceView mPreview = (SurfaceView) act
			// .findViewById(R.id.surfaceView1);
			//
			// MediaRecorder mMediaRecorder = new MediaRecorder();
			// mCamera.unlock();
			// mMediaRecorder.setCamera(mCamera);
			// mMediaRecorder
			// .setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			// mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			// // this is the unofficially supported MPEG2TS format, suitable
			// // for streaming (Android 3.0+)
			// mMediaRecorder
			// .setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			// mMediaRecorder
			// .setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			// mMediaRecorder
			// .setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
			// mMediaRecorder.setOutputFile(pfd.getFileDescriptor());
			// mMediaRecorder.setPreviewDisplay(mPreview.getHolder()
			// .getSurface());
			// mMediaRecorder.prepare();
			// mMediaRecorder.start();
			//
			// // in = new ObjectInputStream(connection.getInputStream());
			// sendMessage("Connection successful");
			// // 4. The two parts communicate via the input and output
			// // streams
			// // do {
			// // try {
			// // message = (String) in.readObject();
			// // Log.d("FPGA","client>" + message);
			// // if (message.equals("bye"))
			// // sendMessage("bye");
			// // } catch (ClassNotFoundException classnot) {
			// // System.err.println("Data received in unknown format");
			// // }
			// // } while (!message.equals("bye"));
			// } catch (IOException ioException) {
			// // ioException.printStackTrace();
			// } finally {
			// // 4: Closing connection
			// // try {
			// // in.close();
			// // out.close();
			// // providerSocket.close();
			// // } catch (IOException ioException) {
			// // ioException.printStackTrace();
			// // }
			// }
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
	
	 class LiveCamera implements TextureView.SurfaceTextureListener {
	      private Camera mCamera;
	      private TextureView mTextureView;
	      	
	      public LiveCamera(){
//	    	 mTextureView = (TextureView) act.findViewById(R.id.cameraFrame);
//	    	 mTextureView.setOpaque(false);
	    	  mTextureView = new TextureView(act);
	    	  mTextureView.setSurfaceTextureListener(this);
	      }
	      
	      public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
	          Log.d("FPGA", "OPENING CAMERA");
	          mCamera.setDisplayOrientation(90);
	    	  mCamera = Camera.open();
	    	  Log.d("SURF",width + " " + height);
	    	  try {
	              mCamera.setPreviewTexture(surface);
	              mCamera.startPreview();
	          } catch (IOException ioe) {
	              // Something bad happened
	          }
	      }

	      public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
	          // Ignored, Camera does all the work for us
	      }

	      public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
	          mCamera.stopPreview();
	          mCamera.release();
	          return true;
	      }

	      public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	    	  // Invoked every time there's a new Camera preview frame
	    	  Log.d("FPGA", "NEW FRAME");
	    	  Bitmap rgb = mTextureView.getBitmap();
	    	  ByteArrayOutputStream o = new ByteArrayOutputStream();
	    	  rgb.compress(CompressFormat.JPEG, 50, o);
	    	  byte[] bytt = o.toByteArray();
	    	  rgb = BitmapFactory.decodeByteArray(bytt , 0, bytt.length);
	    	  
	    	  try {
	    		   String path = Environment.getExternalStorageDirectory().toString();
	    	       File file = new File(path,"ompaloompa.png");
	    		   FileOutputStream out = new FileOutputStream(file);
	    	       rgb.compress(Bitmap.CompressFormat.PNG, 90, out);
	    	       Log.d("SURF", "PRINTING IMAGE: "+path);
	    	  } catch (Exception e) {
	    	      Log.d("SURF", "FAILED TO PRINT IMAGE: "+e.toString());

	    		  e.printStackTrace();
	    	  }


//	    	  iv.setImageBitmap(Bitmap.createScaledBitmap(rgb, 100, 100, false));
	      }

	  }


	/*
	 * Class to capture "preview" frames of camera, i.e. camera frames before
	 * capturing an image
	 */
	class Snapshot implements Camera.PreviewCallback {
		int pw = 100, ph = 100;
		ObjectOutputStream out;
		long start;
		ImageView iv;
		
		public Snapshot(ObjectOutputStream out) {
			this.out = out;
			iv = (ImageView) act.findViewById(R.id.cameraFrame);
//			start = System.currentTimeMillis();
		}
		
		public Snapshot() {
			iv = (ImageView) act.findViewById(R.id.cameraFrame);
//			start = System.currentTimeMillis();
		}

		public void onPreviewFrame(byte[] data, Camera camera) {
//			if (System.currentTimeMillis() - start > 500) {
//				return;
//			}
//			start = System.currentTimeMillis();
			Parameters parameters = camera.getParameters();
			int imageFormat = parameters.getPreviewFormat();
			Log.d("FPGA", "FRAME AVAILABLE");
//			TextureView tv;

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, pw,
					ph, null);
			yuvImage.compressToJpeg(new Rect(0, 0, pw, ph), 50, out);
			byte[] imageBytes = out.toByteArray();
			Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0,
					imageBytes.length);
			iv.setImageBitmap(image);

			if (imageFormat == ImageFormat.NV21) {

				// Rect rect = new Rect(0, 0, pw, ph);
				// YuvImage img = new YuvImage(data, ImageFormat.NV21, pw, ph,
				// null);
				// Bitmap image = BitmapFactory.decodeByteArray(data, 0,
				// data.length);
				// img.compressToJpeg(rect, 100, out);
				// iv.setImageBitmap(image);
				// try {
				// img.compressToJpeg(rect, 100, out);
				// out.flush();
				// } catch (FileNotFoundException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}
	}
}
