package com.marginallyclever.robotoverlord.components.motors;

public class StepperMotorComponent extends MotorComponent {
    public static final String [] directionNames = {"Backward","Forward"};
    public static final int DIRECTION_BACKWARD=0;
    public static final int DIRECTION_FORWARD=1;

    private int direction=DIRECTION_FORWARD;
    private int stepCount=0;
    private int stepAbsolute=0;
    private int stepPerRevolution=200;
    private int microStepping=1;

    /**
     * @param direction either DIRECTION_BACKWARD or DIRECTION_FORWARD
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void step() {
        stepCount+=(direction*2)-1;
        stepAbsolute++;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void resetStepAbsolute() {
        stepAbsolute=0;
    }
    public int getStepAbsolute() {
        return stepAbsolute;
    }

    public void setStepPerRevolution(int stepPerRevolution) {
        this.stepPerRevolution = stepPerRevolution;
    }

    public int getStepPerRevolution() {
        return stepPerRevolution;
    }

    /**
     * @param microStepping must be power of two.  Must be greater than zero.
     */
    public void setMicroStepping(int microStepping) {
        if(microStepping<1) throw new IllegalArgumentException("microStepping must be greater than zero.");
        if(isNotPowerOf2(microStepping)) throw new IllegalArgumentException("microStepping must be power of two.");
        this.microStepping = microStepping;
    }

    private boolean isNotPowerOf2(int v) {
        return (v & (v - 1)) != 0;
    }

    public int getMicroStepping() {
        return microStepping;
    }
}
