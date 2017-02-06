
package org.usfirst.frc.team5461.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.io.File;
import java.util.TimeZone;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usfirst.frc.team5461.robot.commands.DriveToCollectGear;
import org.usfirst.frc.team5461.robot.sensors.I2CUpdatableAddress.NACKException;
import org.usfirst.frc.team5461.robot.sensors.VL53L0XSensors;
import org.usfirst.frc.team5461.robot.sensors.VL53L0XSensors.NotInitalizedException;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	public static OI oi;
	public static VL53L0XSensors distance;

	Command autonomousCommand;
	Command driveCommand;
	SendableChooser<Command> chooser = new SendableChooser<>();
	static Logger logger = LoggerFactory.getLogger(Robot.class);
    DataLogger dataLogger;
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
		
		File logDirectory = null;
		if (logDirectory == null) logDirectory = findLogDirectory(new File("/u"));
		if (logDirectory == null) logDirectory = findLogDirectory(new File("/v"));
		if (logDirectory == null) logDirectory = findLogDirectory(new File("/x"));
		if (logDirectory == null) logDirectory = findLogDirectory(new File("/y"));
		if (logDirectory == null) {
			logDirectory = new File("/home/lvuser/logs");
		    if (!logDirectory.exists())
		    {
			    logDirectory.mkdir();
		    }
		}
		if (logDirectory != null && logDirectory.isDirectory())
		{
			String logMessage = String.format("Log directory is %s\n", logDirectory);
			System.out.print (logMessage);
			EventLogging.writeToDS(logMessage);
			EventLogging.setup(logDirectory);
			dataLogger = new DataLogger(logDirectory);
			dataLogger.setMinimumInterval(1000);
		}

		logger.info ("Starting robotInit");
		distance = new VL53L0XSensors();
		boolean success = false;
		try {
			success = distance.init();
			System.out.println("Distance sensors intialized!!!!!");
		} catch (NACKException e) {
			logger.info("NACKException: VL53L0X sensors not initialized!!!!!");
			System.out.println("VL53L0X sensors not initialized!!!!!");
		}

		oi = new OI();
		SmartDashboard.putData("Auto mode", chooser);
		driveCommand = new DriveToCollectGear();
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
	 */
	@Override
	public void disabledInit() {

	}

	@Override
	public void disabledPeriodic() {
		if (!driveCommand.isCanceled()) {
			driveCommand.cancel();
		}
		Scheduler.getInstance().run();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		autonomousCommand = chooser.getSelected();

		/*
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */

		// schedule the autonomous command (example)
		if (autonomousCommand != null)
			autonomousCommand.start();
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();
	}

	@Override
	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (autonomousCommand != null)
			autonomousCommand.cancel();
		logger.info("Starting DriveToCollectGear:");
		driveCommand.start();
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
//		Vector<Integer> results;
//		try {
//			results = distance.readRangeSingleMillimeters();
//		} catch (NACKException nackEx) {
//			System.out.println("VL53L0X sensors NACK:");
//			return;
//		} catch (NotInitalizedException NotIinitEx) {
//			System.out.println();
//			logger.info("VL53L0X sensors Not Initialized:");
//			return;
//		}
//		StringBuilder sb = new StringBuilder();
//		sb.append("Range1,");
//		sb.append(Integer.toString(results.get(0)));
//		sb.append(",Range2,");
//		sb.append(Integer.toString(results.get(1)));
//		sb.append("\n");
//		logger.info(sb.toString());
//		try {
//			Thread.sleep(5);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		results.clear();
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
	public File findLogDirectory (File root) {
		// does the root directory exist?
		if (!root.isDirectory()) return null;
		
		File logDirectory = new File(root, "logs");
		if (!logDirectory.isDirectory()) return null;
		
		return logDirectory;
	}
}
