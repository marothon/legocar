package tum.lego.fpgacontroller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import java.io.*;
import java.net.*;

public class LegoCarServer extends Activity {
	//public static String wifi_mod_ip = "1.2.3.4";
	//public static int socket = 3003;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lego_car_controller);
		new connectTask().execute("");
	}
	
	class connectTask extends AsyncTask<String, String, Server>{
		@Override
		protected Server doInBackground(String... params) {
			new Server().run();
			return null;
		}
	}
	
	class Server extends Thread{
		ServerSocket providerSocket;
		Socket connection = null;
		ObjectOutputStream out;
		ObjectInputStream in;
		String message;
		
		public void run(){
				
			InetAddress ip;
			try { 
				ip = InetAddress.getLocalHost();
				Log.d("IP","Current IP address : " + ip.getHostAddress());
			}catch (UnknownHostException e) {
				e.printStackTrace();
			}
			while(true){
				try{
					//1. creating a server socket
					providerSocket = new ServerSocket(3333);
					//2. Wait for connection
					System.out.println("Waiting for connection");
					connection = providerSocket.accept();
					System.out.println("Connection received from " + connection.getInetAddress().getHostName());
					//3. get Input and Output streams
					out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					in = new ObjectInputStream(connection.getInputStream());
					sendMessage("Connection successful");
					//4. The two parts communicate via the input and output streams
					do{
						try{
							message = (String)in.readObject();
							System.out.println("client>" + message);
							if (message.equals("bye"))
								sendMessage("bye");
						}
						catch(ClassNotFoundException classnot){
							System.err.println("Data received in unknown format");
						}
					}while(!message.equals("bye"));
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
				finally{
					//4: Closing connection
					try{
						in.close();
						out.close();
						providerSocket.close();
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				}	
			}
		}
		void sendMessage(String msg)
		{
			try{
				out.writeObject(msg);
				out.flush();
				System.out.println("server>" + msg);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	
	}
	
}
