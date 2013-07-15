#include <stdio.h>
#include "motor_api.h"
#include "motor_test.h"

int motor_test() {

	int power_flow[10] = { 0, 20, 40, 60, 20, 30, 0, -30, -63, -24 };

	int i = 0;
	int c = 0;
	while (i < 10) {
		c = 0;
		left_throttle(power_flow[i]);
		right_throttle(power_flow[i]);
		while (c < 1000000)
			c++;
		i++;
	}

	return 0;
}

