package org.usfirst.frc.team5461.robot.commands;

import org.slf4j.Logger;
import org.usfirst.frc.team5461.robot.EventLogging;
import org.usfirst.frc.team5461.robot.EventLogging.Level;
import org.usfirst.frc.team5461.robot.Robot;
import org.usfirst.frc.team5461.robot.sensors.I2CUpdatableAddress.NACKException;
import org.usfirst.frc.team5461.robot.sensors.VL53L0XSensors.NotInitalizedException;

import edu.wpi.first.wpilibj.command.PIDCommand;

/**
 *
 */
public class DriveWithVL53L0X extends PIDCommand {
	private int m_address;
	private int distanceFromTarget;
	public int lastValue = 0;
	static Logger logger = EventLogging.getLogger(DriveWithVL53L0X.class, Level.INFO);
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
    	
    	int result;
		try {
			result = Robot.distance.readRangeSingleMillimeters(m_address);
		} catch (NACKException NACKEx) {
			logger.info("NACKException: Returning last value: " + Integer.toString(lastValue));
			result = lastValue;
		} catch (NotInitalizedException NotInitEx) {
			logger.info("VL53L0X not initalized!!!!! Returning Neutral Distance from target: " + Integer.toString(distanceFromTarget));
			return distanceFromTarget;
		}
    	
		// Cap return result to max distance of concern
    	if (result > DistanceContract.max_distance){
    		result = DistanceContract.max_distance;
    		logger.info("Result greater than max distance:");
    	}

		logger.info("Address," + Integer.toString(m_address) + ", Input," + Integer.toString(result));
    	lastValue = result;
    	return result;
    }
   
    @Override
    protected boolean isFinished() {
    	boolean isOnTarget = getPIDController().onTarget();
    	if (isOnTarget) {
    		logger.info("DriveWithVL53L0X is on target: "  + Integer.toString(m_address));
    	}
    	return isOnTarget;
    }
	@Override
	protected void usePIDOutput(double power) {
		logger.info("Address, " + Integer.toString(m_address) + ", Power, " + Double.toString(power));
	}
}
