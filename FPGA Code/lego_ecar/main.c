#include <stdio.h>

#include "command_receiver.h"
#include "motor_api.h"
#include "motor_test.h"

int main() {

	printf("On-board software is running!\n");

	// For testing purpose
	// motor_test();

	// Start receiving commands
	command_receiver();

	return 0;
}
