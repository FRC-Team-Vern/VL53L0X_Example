package org.usfirst.frc.team5461.robot.commands;


import edu.wpi.first.wpilibj.command.PIDCommand;

/**
 *
 */
public class DriveWithVL53L0X extends PIDCommand {
	private int m_address;
	private int distanceFromTarget;
	public int lastValue = 0;
    public DriveWithVL53L0X(int address, int distanceFromTarget) {
    	super(DistanceContract.kP_real, DistanceContract.kI_real, DistanceContract.kD_real);
    	m_address = address;
    	this.distanceFromTarget = distanceFromTarget;
       	setInputRange(0, DistanceContract.max_distance);
    }
    
    @Override
    protected void initialize() {
    	System.out.println("Intializing DriveWithVL53L0X");
    	getPIDController().setToleranceBuffer(3);
    	getPIDController().setAbsoluteTolerance(10);
    	getPIDController().reset();
    	// Converting from inches to mm
    	setSetpoint((int)(distanceFromTarget * 25.4));
    	getPIDController().enable();
    }
    
    @Override
    protected double returnPIDInput() {
    	
    	int result = 0;
//		try {
//			result = Robot.distance.readRangeSingleMillimeters(m_address);
//		} catch (NACKException NACKEx) {
//			result = lastValue;
//		} catch (NotInitalizedException NotInitEx) {
//			return distanceFromTarget;
//		}
//
//		// Cap return result to max distance of concern
//    	if (result > DistanceContract.max_distance){
//    		result = DistanceContract.max_distance;
//    	}
//
//    	lastValue = result;
    	return result;
    }
   
    @Override
    protected boolean isFinished() {
    	boolean isOnTarget = getPIDController().onTarget();
    	if (isOnTarget) {
			System.out.println("DriveWithVL53L0X is on target: "  + Integer.toString(m_address));
    	}
    	return isOnTarget;
    }
	@Override
	protected void usePIDOutput(double power) {
        System.out.println("Address, " + Integer.toString(m_address) + ", Power, " + Double.toString(power));
	}
}
