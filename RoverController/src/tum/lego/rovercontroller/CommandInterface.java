package tum.lego.rovercontroller;

import android.annotation.SuppressLint;

public class CommandInterface {
	/**
	 * 
	 * L:+11:R:-34
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
