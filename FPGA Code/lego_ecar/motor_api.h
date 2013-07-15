#ifndef MOTOR_API_H_
#define MOTOR_API_H_

#define MAX_FREQUENCY 5000
#define DIVIDER 99

/**
 * Motor 0 and 1 are on the left
 *
 */
int left_throttle(int power);

/**
 * Motor 2 and 3 are on the right
 *
 */
int right_throttle(int power);

/**
 * For emergencies.
 */
void stop();

#endif /* MOTOR_API_H_ */
