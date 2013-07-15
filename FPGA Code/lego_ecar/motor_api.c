#include "motor_api.h"
#include "motor_setting.h"
#include <stdio.h>

int left_throttle(int power) {
	if (power >= 0 && power <= DIVIDER) { // move forwards
		motor0_setting(0, power * MAX_FREQUENCY / DIVIDER, 0, 0,
				MAX_FREQUENCY, 3);
		motor1_setting(0, 0, 0, power * MAX_FREQUENCY / DIVIDER,
				MAX_FREQUENCY, 3);
	} else if (power >= -DIVIDER) { // move backwards
		motor0_setting(0, 0, 0, -power * MAX_FREQUENCY / DIVIDER,
				MAX_FREQUENCY, 3);
		motor1_setting(0, -power * MAX_FREQUENCY / DIVIDER, 0, 0,
				MAX_FREQUENCY, 3);
	} else {
		// something wrong!
		return -1;
	}
	return 0;
}

int right_throttle(int power) {
	if (power >= 0 && power <= DIVIDER) { // move forwards
		motor2_setting(0, 0, 0, power * MAX_FREQUENCY / DIVIDER,
				MAX_FREQUENCY, 3);
		motor3_setting(0, power * MAX_FREQUENCY / DIVIDER, 0, 0,
				MAX_FREQUENCY, 3);
	} else if (power >= -DIVIDER) { // move backward
		motor2_setting(0, -power * MAX_FREQUENCY / DIVIDER, 0, 0,
				MAX_FREQUENCY, 3);
		motor3_setting(0, 0, 0, -power * MAX_FREQUENCY / DIVIDER,
				MAX_FREQUENCY, 3);
		printf("Motor 2: %d\n", (-power * MAX_FREQUENCY / DIVIDER));
		printf("Motor 3: %d\n", (-power * MAX_FREQUENCY / DIVIDER));
	} else {
		// something wrong!
		return -1;
	}
	return 0;
}

void stop(){
	motor0_setting(0, 0, 0, 0, MAX_FREQUENCY, 3);
	motor1_setting(0, 0, 0, 0, MAX_FREQUENCY, 3);
	motor2_setting(0, 0, 0, 0, MAX_FREQUENCY, 3);
	motor3_setting(0, 0, 0, 0, MAX_FREQUENCY, 3);
}
