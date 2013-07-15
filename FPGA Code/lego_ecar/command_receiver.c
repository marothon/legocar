#include <stdio.h>
#include <string.h>
#include "system.h"
#include "command_receiver.h"
#include "motor_api.h"

int command_receiver() {

	char buffer[9999] = { 0 };
	FILE* fp;

	fp = fopen("/dev/uart_wifi", "r");

	if (!fp) {
		printf("Can't access WiFi UART!\n");
		return -1;
	}

	char left_duty_cycle[3];
	char right_duty_cycle[3];

	int ret;

	while (1) {

		fscanf(fp, "%s", buffer);

		ret = sscanf(buffer, "L:%c%c%c:R:%c%c%c", &left_duty_cycle[0],
				&left_duty_cycle[1], &left_duty_cycle[2], &right_duty_cycle[0],
				&right_duty_cycle[1], &right_duty_cycle[2]);
		if (ret != 6) {
			continue;
		}

		int left_cycle =
				(left_duty_cycle[0] == '-' ? -1 : 1)
						* ((left_duty_cycle[1] - '0') * 10
								+ (left_duty_cycle[2] - '0'));
		int right_cycle = (right_duty_cycle[0] == '-' ? -1 : 1)
				* ((right_duty_cycle[1] - '0') * 10
						+ (right_duty_cycle[2] - '0'));

		left_throttle(left_cycle);
		right_throttle(right_cycle);
	}

	return 0;
}

