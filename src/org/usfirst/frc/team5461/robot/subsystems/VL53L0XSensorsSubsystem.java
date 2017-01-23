package org.usfirst.frc.team5461.robot.subsystems;

import java.util.Vector;

import org.usfirst.frc.team5461.robot.sensors.VL53L0X;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class VL53L0XSensorsSubsystem extends Subsystem {

	// Sensors
	VL53L0X vl53l0x1;
	VL53L0X vl53l0x2;
	
	// Respective DIO
	DigitalOutput do1 = new DigitalOutput(0);
	DigitalOutput do2 = new DigitalOutput(1);
	
	public VL53L0XSensorsSubsystem() {
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
		return result1 && result2;
	}
	
	public Vector<Integer> readRangeSingleMillimeters() {
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

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    public void close() {
    	vl53l0x1.close();
    	vl53l0x2.close();
    }
}

