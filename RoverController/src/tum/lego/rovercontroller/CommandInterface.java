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
	 * (seq seq seq seq) interp command (sign val val val val) (sign val val val
	 * val)
	 * 
	 * The four highest bits for a group that describes the sequence number,
	 * i.e. the order of this command. This will be used for command order
	 * synchronization on the FPGA.
	 * 
	 * The interp bit is used to separate from continuous commands and emergency
	 * brake commands.
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
	 */
	public short createCommand(int powLeft, int powRight, boolean interp) {
		short command = 0x400;
		if (interp)
			command |= 0x800;

		command |= (seq & 0xf) << 12;

		if (powLeft < 0) {
			command |= 1 << 9;
			powLeft = -powLeft;
		}

		if (powRight < 0) {
			command |= 1 << 4;
			powRight = -powRight;
		}

		command |= powRight & 0xf;
		command |= (powLeft & 0xf) << 5;
		seq = ++seq % 32;
		return command;
	}

	public short keepalive() {
		short command = 0;
		command |= (seq & 0xf) << 12;
		seq = ++seq % 32;
		return command;
	}
}
