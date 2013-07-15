/*
 * input parameters:
 * phase: range from
 * duty cycle:
 * period: according to the freq of cpu,
 *         normally the period should be set to the value
 *         that makes the freq of pwm waveform to be 15 k
 *         e.x for 50MHz, value should be 3333(0xD05)
 * enable:'0' represents off, '1' is on
 */

#ifndef MOTOR_SETTING_H_
#define MOTOR_SETTING_H_

#include "system.h"

#define PWM_0_EN PWM_GEN_0_BASE
#define PWM_0_PERIOD PWM_GEN_0_BASE+1
#define PWM_0_DUTY1 PWM_GEN_0_BASE+2
#define PWM_0_DUTY2 PWM_GEN_0_BASE+3
#define PWM_0_PHASE1 PWM_GEN_0_BASE+4
#define PWM_0_PHASE2 PWM_GEN_0_BASE+5

#define PWM_1_EN PWM_GEN_1_BASE
#define PWM_1_PERIOD PWM_GEN_1_BASE+1
#define PWM_1_DUTY1 PWM_GEN_1_BASE+2
#define PWM_1_DUTY2 PWM_GEN_1_BASE+3
#define PWM_1_PHASE1 PWM_GEN_1_BASE+4
#define PWM_1_PHASE2 PWM_GEN_1_BASE+5

#define PWM_2_EN PWM_GEN_2_BASE
#define PWM_2_PERIOD PWM_GEN_2_BASE+1
#define PWM_2_DUTY1 PWM_GEN_2_BASE+2
#define PWM_2_DUTY2 PWM_GEN_2_BASE+3
#define PWM_2_PHASE1 PWM_GEN_2_BASE+4
#define PWM_2_PHASE2 PWM_GEN_2_BASE+5

#define PWM_3_EN PWM_GEN_3_BASE
#define PWM_3_PERIOD PWM_GEN_3_BASE+1
#define PWM_3_DUTY1 PWM_GEN_3_BASE+2
#define PWM_3_DUTY2 PWM_GEN_3_BASE+3
#define PWM_3_PHASE1 PWM_GEN_3_BASE+4
#define PWM_3_PHASE2 PWM_GEN_3_BASE+5

void motor0_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable);

void motor1_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable);

void motor2_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable);

void motor3_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable);

#endif /* MOTOR_SETTING_H_ */
