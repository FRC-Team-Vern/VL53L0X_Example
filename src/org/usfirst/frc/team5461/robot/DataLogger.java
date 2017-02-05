package org.usfirst.frc.team5461.robot;

import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;

public class DataLogger {
	
	/*
	public static void main(String [] args)
	{
		DataLogger test1 = new DataLogger(new File("./test"));
		test1.setMinimumInterval(150);
		{
		test1.addDataItem("stuffD", 1.0);
		test1.addDataItem("stuffI", 3);
		test1.addDataItem("stuffB", false);
		test1.addDataItem("stuffS", "value");
		test1.saveDataItems();
		}
		try {
			Thread.sleep(300);
			} catch (InterruptedException e) {
				// cannot happen
				
			}
		{
		test1.addDataItem("stuffD", 1.2);
		test1.addDataItem("stuffI", 34);
		test1.addDataItem("stuffB", true);
		test1.addDataItem("stuffS", "should not show up");
		test1.saveDataItems();
		}
		try {
		Thread.sleep(2000);
		} catch (InterruptedException e) {
			// cannot happen
			
		}
		{
		test1.addDataItem("stuffD", 1.2);
		test1.addDataItem("stuffI", 34);
		test1.addDataItem("stuffB", true);
		test1.addDataItem("stuffS", "*should* show up");
		test1.saveDataItems();
		}
		//System.out.println(test1.getDate());
	}
	*/
	
	File parentDirectory;
	long minimumInterval = 500;
	
	public DataLogger(File directory) {
		parentDirectory = directory;
	}
	
	List<String> dataNames = new ArrayList<>();
	List<String> dataValues = new ArrayList<>();
	public void addDataItem(String name, double value)
	{
		String valueString = String.valueOf(value);
		dataNames.add(name);
		dataValues.add(valueString);
	}
	
	public void addDataItem(String name, int value)
	{
		String valueString = String.valueOf(value);
		dataNames.add(name);
		dataValues.add(valueString);
	}
	
	public void addDataItem(String name, boolean value)
	{
		String valueString = String.valueOf(value);
		dataNames.add(name);
		dataValues.add(valueString);
	}
	
	public void addDataItem(String name, String value)
	{
		String valueString = String.valueOf(value);
		dataNames.add(name);
		dataValues.add(valueString);
	}
	
	PrintStream ps;
	long startTime;
	long timeUpdated;
	long timeSinceLog;
	
	public boolean shouldLogData() {
		long now = System.currentTimeMillis();
		if((ps==null) || (now - timeSinceLog) > minimumInterval)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public void saveDataItems()
	{
		if(shouldLogData())
		{
			try
			{
				if (ps == null)
				{
					synchronized (this) {
						if (ps == null) {
							String timestampString = LogTimestamp.getTimestampString();
							if (timestampString != null) {
								File logFile = new File(parentDirectory, timestampString + ".csv");
								ps = new PrintStream(new FileOutputStream(logFile));
								ps.print("time,timeSinceStart");
								writelist(ps, dataNames);
								startTime = System.currentTimeMillis();
							}
						}
					}
				}
				if (ps != null)
				{
					timeUpdated = (System.currentTimeMillis()-startTime);
					ps.print(getDate());
					ps.print(',');
					ps.print(timeUpdated);
					writelist(ps, dataValues);
					ps.flush();
					timeSinceLog = System.currentTimeMillis();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		dataValues.clear();
		dataNames.clear();
		
	}
	
	private void writelist(PrintStream stream, List<String> list)
	{
		for(int i = 0;i < list.size(); i++)
		{
			stream.print(',');
			stream.print(list.get(i));
		}
		stream.println();
	}
	
	public String getDate()
	{
		Date curDate = new Date();
		String DateToStr = format.format(curDate);
		return DateToStr;
	}
	
	SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SS");
	
	public void setMinimumInterval(long minimumInterval) {
		this.minimumInterval = minimumInterval;
	}
}
