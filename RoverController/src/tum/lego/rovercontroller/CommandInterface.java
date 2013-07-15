package tum.lego.rovercontroller;

import android.annotation.SuppressLint;

/**
 * Static class for generating command strings to the FPGA board.
 */
public class CommandInterface {
	/**
	 * 
	 * Generates a commandstring from two given values. The command has been defined
	 * as follows:
	 * \nL:(sign)(power with 2 decimals):R:(sign)(power with 2 decimals)\n
	 * L: signifying the left side of the car and R: the right.
	 * 
	 */
	@SuppressLint("DefaultLocale")
	public static String createCommand(int lValue, int rValue){
		String lSign = lValue > 0 ? "+" : "-";
		String rSign = rValue > 0 ? "+" : "-";
		
		lValue = Math.abs(lValue)%100;
		rValue = Math.abs(rValue)%100;
		
		return String.format("\nL:%s%02d:R:%s%02d\n", lSign, lValue, rSign, rValue);
	}
}
