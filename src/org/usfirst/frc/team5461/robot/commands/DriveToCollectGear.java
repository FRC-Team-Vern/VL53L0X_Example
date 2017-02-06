package org.usfirst.frc.team5461.robot.commands;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class DriveToCollectGear extends CommandGroup {

    public DriveToCollectGear() {
    	System.out.println("Making DriveToCollectGear instance.");
    	addParallel(new DriveWithVL53L0X(1, 7));
    	addSequential(new DriveWithVL53L0X(2, 7));
    }
}
