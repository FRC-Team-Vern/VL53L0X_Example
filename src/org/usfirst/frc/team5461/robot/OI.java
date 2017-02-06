package org.usfirst.frc.team5461.robot;

import org.usfirst.frc.team5461.robot.commands.DriveToCollectGear;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;


/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {
	Joystick leftStick;
	JoystickButton getGearButton;
	
	public OI() {
//		leftStick = new Joystick(0);
//		getGearButton = new JoystickButton(leftStick, 3);
//		
//		getGearButton.whenPressed(new DriveToCollectGear());
	}
}
