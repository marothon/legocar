package tum.lego.rovercontroller;

public class CommandInterface {
	private int seq;
	
	public CommandInterface() {
		seq = 0;
	}

	/**
	 * 
	 * Creates a command from the given input, which is the power of each side
	 * of the car. The protocol has been defined as a short with the following
	 * bit meaning:
	 * 
	 * check command (sign val val val val val val) (sign val val val val val val)
	 * 
	 * Check is an error checking bit. It should change between each issued command.
	 * 
	 * The command bit is used for keeping the current command alive if no
	 * further commands have been issued.
	 * 
	 * The two identical sign val bit blocks describe the motor power. The sign
	 * bits are for direction while the val bits are for the value.
	 * 
	 * @return The created command
	 * @param powLeft Power of the left side of the car
	 * @param powRight Power of the right side of the car
	 * 
	 * 
	 */
	public short createCommand(int powLeft, int powRight, boolean interp) {
		short command = 0;
		if (powLeft < 0) {
			command |= 1 << 13;
			powLeft = -powLeft;
		}

		if (powRight < 0) {
			command |= 1 << 6;
			powRight = -powRight;
		}

		command |= powRight & 0x2f;
		command |= (powLeft & 0x2f) << 7;
		seq = ++seq % 32;
		return command;
	}
	/**
	 * 
	 * L:+11:R:-34
	 * 
	 */
	public String createCommand(int lValue, int rValue){
		String lSign = lValue > 0 ? "+" : "-";
		String rSign = rValue > 0 ? "+" : "-";
		
		lValue = Math.abs(lValue)%100;
		rValue = Math.abs(rValue)%100;
		
		return String.format("\nL:%s%02d:R:%s%02d\n", lSign, lValue, rSign, rValue);
	}
}
