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
#include "motor_setting.h"

void motor0_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable) {
	unsigned long * pwm_0_en = (unsigned long *) PWM_0_EN;
	unsigned long * pwm_0_period = (unsigned long *) PWM_0_PERIOD;
	unsigned long * pwm_0_phase1 = (unsigned long *) PWM_0_PHASE1;
	unsigned long * pwm_0_phase2 = (unsigned long *) PWM_0_PHASE2;
	unsigned long * pwm_0_duty1 = (unsigned long *) PWM_0_DUTY1;
	unsigned long * pwm_0_duty2 = (unsigned long *) PWM_0_DUTY2;

	*pwm_0_en = enable;
	*pwm_0_period = period;
	*pwm_0_phase1 = phase1;
	*pwm_0_phase2 = phase2;
	*pwm_0_duty1 = duty1;
	*pwm_0_duty2 = duty2;
}

void motor1_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable) {
	unsigned long * pwm_1_en = (unsigned long *) PWM_1_EN;
	unsigned long * pwm_1_period = (unsigned long *) PWM_1_PERIOD;
	unsigned long * pwm_1_phase1 = (unsigned long *) PWM_1_PHASE1;
	unsigned long * pwm_1_phase2 = (unsigned long *) PWM_1_PHASE2;
	unsigned long * pwm_1_duty1 = (unsigned long *) PWM_1_DUTY1;
	unsigned long * pwm_1_duty2 = (unsigned long *) PWM_1_DUTY2;

	*pwm_1_en = enable;
	*pwm_1_period = period;
	*pwm_1_phase1 = phase1;
	*pwm_1_phase2 = phase2;
	*pwm_1_duty1 = duty1;
	*pwm_1_duty2 = duty2;
}

void motor2_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable) {
	unsigned long * pwm_2_en = (unsigned long *) PWM_2_EN;
	unsigned long * pwm_2_period = (unsigned long *) PWM_2_PERIOD;
	unsigned long * pwm_2_phase1 = (unsigned long *) PWM_2_PHASE1;
	unsigned long * pwm_2_phase2 = (unsigned long *) PWM_2_PHASE2;
	unsigned long * pwm_2_duty1 = (unsigned long *) PWM_2_DUTY1;
	unsigned long * pwm_2_duty2 = (unsigned long *) PWM_2_DUTY2;

	*pwm_2_en = enable;
	*pwm_2_period = period;
	*pwm_2_phase1 = phase1;
	*pwm_2_phase2 = phase2;
	*pwm_2_duty1 = duty1;
	*pwm_2_duty2 = duty2;
}

void motor3_setting(unsigned long phase1, unsigned long duty1,
		unsigned long phase2, unsigned long duty2, unsigned long period,
		unsigned long enable) {
	unsigned long * pwm_3_en = (unsigned long *) PWM_3_EN;
	unsigned long * pwm_3_period = (unsigned long *) PWM_3_PERIOD;
	unsigned long * pwm_3_phase1 = (unsigned long *) PWM_3_PHASE1;
	unsigned long * pwm_3_phase2 = (unsigned long *) PWM_3_PHASE2;
	unsigned long * pwm_3_duty1 = (unsigned long *) PWM_3_DUTY1;
	unsigned long * pwm_3_duty2 = (unsigned long *) PWM_3_DUTY2;

	*pwm_3_en = enable;
	*pwm_3_period = period;
	*pwm_3_phase1 = phase1;
	*pwm_3_phase2 = phase2;
	*pwm_3_duty1 = duty1;
	*pwm_3_duty2 = duty2;
}
