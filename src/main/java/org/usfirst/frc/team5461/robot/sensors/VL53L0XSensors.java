package org.usfirst.frc.team5461.robot.sensors;

import java.util.Vector;
import edu.wpi.first.wpilibj.DigitalOutput;
import org.usfirst.frc.team5461.robot.sensors.VL53L0X.NACKException;

/**
 *
 */
public class VL53L0XSensors {

	// Sensors
	VL53L0X vl53l0x1;
	VL53L0X vl53l0x2;
	
	boolean initialized = false;
	
	// Respective DIO
	DigitalOutput do1 = new DigitalOutput(0);
	DigitalOutput do2 = new DigitalOutput(1);
	
	public VL53L0XSensors() {
		do1.set(false);
		do2.set(false);
	}
	
	public boolean init() throws NACKException {
		do1.set(true);
		// Allow some time for the xshut bit to settle
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		vl53l0x1 = new VL53L0X(1);
		boolean result1 = vl53l0x1.init(true);
		do2.set(true);
		// Allow some time for the xshut bit to settle
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		vl53l0x2 = new VL53L0X(2);
		
		boolean result2 = vl53l0x2.init(true);
		initialized = result1 && result2;
		return initialized;
	}
	
	public Vector<Integer> readRangeSingleMillimeters() throws NACKException, NotInitalizedException{
		if (!initialized){
			throw new NotInitalizedException();
		}
		
		Vector<Integer> results = new Vector<>();
		int result1 = vl53l0x1.readRangeSingleMillimeters();
		results.add(result1);
		// Give a little wait between reads
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int result2 = vl53l0x2.readRangeSingleMillimeters();
		results.add(result2);
		return results;
	}
	
	public int readRangeSingleMillimeters(int address) throws NACKException, NotInitalizedException{
		if (!initialized){
			throw new NotInitalizedException();
		}
		int result = -1;
		if (address == 1){
			result = vl53l0x1.readRangeSingleMillimeters();
			
		} else if (address == 2){
			result = vl53l0x2.readRangeSingleMillimeters();
		}
		// Give a little wait between reads
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }

    public class NotInitalizedException extends Exception{}
    
}

