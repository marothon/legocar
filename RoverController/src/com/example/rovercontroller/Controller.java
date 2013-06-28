package com.example.rovercontroller;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

public class Controller extends Activity {
	
	private Activity act = this;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	private SeekBar leftRightSeekBar;
	private SeekBar forwardBackwardSeekBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.controller);
	
	    leftRightSeekBar = (SeekBar) act.findViewById(R.id.LRSeekBar);
		forwardBackwardSeekBar = (SeekBar) act.findViewById(R.id.FBSeekBar);
		
		leftRightSeekBar.setOnSeekBarChangeListener(new MovementSeekbarChangedListener("LFTRGT"));
		forwardBackwardSeekBar.setOnSeekBarChangeListener(new MovementSeekbarChangedListener("FRWBCK"));
	}
	
	/// converts from 0-100 scale to -100 +100
	public int seekBarValueToMovementValue(int val) {
		return val - 100;
	}
	
	class MovementSeekbarChangedListener implements SeekBar.OnSeekBarChangeListener{
		
		// FORBCK for forward-backward movement
		// LFTRGT for left right
		public String label;
		
		public MovementSeekbarChangedListener(String label) {
			this.label = label;
		}

		@Override
		/// send commands
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			String message = label + String.valueOf(seekBarValueToMovementValue(progress)); 
			out.write(message);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
	}

}
