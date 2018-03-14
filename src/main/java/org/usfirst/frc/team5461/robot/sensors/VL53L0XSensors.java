package org.usfirst.frc.team5461.robot.sensors;

import java.io.IOException;
import java.util.Vector;
import edu.wpi.first.wpilibj.DigitalOutput;

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
	
	public boolean init() {
		do1.set(true);
		// Allow some time for the xshut bit to settle
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		boolean result1 = false;
		boolean result2 = false;

		try {
			vl53l0x1 = new VL53L0X(1);
		} catch (I2CUpdatableAddress.NACKException nackEx) {
            System.out.println("vl53l0x1 not initialized");
			result1 = false;
		}
////		vl53l0x1.setAddress(0x27 + 1);
        if (vl53l0x1 != null) {
            try {
                result1 = vl53l0x1.init(true);
            } catch (I2CUpdatableAddress.NACKException nackEx) {
                result1 = false;
            }
        }

		do2.set(true);
		// Allow some time for the xshut bit to settle
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        try {
            vl53l0x2 = new VL53L0X(2);
        } catch (I2CUpdatableAddress.NACKException nackEx) {
            System.out.println("vl53l0x2 not initialized");
            result2 = false;
        }

        if (vl53l0x2 != null) {
            try {
                result2 = vl53l0x2.init(true);
            } catch (I2CUpdatableAddress.NACKException nackEx) {
                result2 = false;
            }
        }

        initialized = result1 && result2;
		return initialized;
	}
	
	public Vector<Integer> readRangeSingleMillimeters() throws I2CUpdatableAddress.NACKException, NotInitalizedException{
		if (!initialized){
			throw new NotInitalizedException();
		}
		
		Vector<Integer> results = new Vector<>();
		int result1 = vl53l0x1.readRangeSingleMillimeters();
		results.add(result1);
		// Give a little wait between reads
//		try {
//			Thread.sleep(1);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		int result2 = vl53l0x2.readRangeSingleMillimeters();
		results.add(result2);
		return results;
	}
	
	public int readRangeSingleMillimeters(int address) throws I2CUpdatableAddress.NACKException, NotInitalizedException{
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

    public class NotInitalizedException extends IOException {}
    
}

