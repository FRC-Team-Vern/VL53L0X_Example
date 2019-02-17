package sensors;

import edu.wpi.first.wpilibj.DigitalOutput;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 */
public class VL53L0XSensors {
    private boolean initialized = false;
    private List<VL53L0X> vl53l0xArray = new ArrayList<>();
    // Set up synchronized results buffers
    private List<ArrayBlockingQueue<Integer>> arrayBlockingQueueList = null;
    // Respective DIO
    private final List<DigitalOutput> doArray;
    private Timer distanceTimer = null;


    public VL53L0XSensors(List<DigitalOutput> outputArr) {
        doArray = outputArr;
        for (DigitalOutput dO : doArray) {
            dO.set(false);
        }
    }

    public boolean init() {
        boolean result = false;
        for (int i = 0; i < doArray.size(); ++i) {
            doArray.get(i).set(true);
            // Allow some time for the xshut bit to settle
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            VL53L0X vl53L0X = null;
            try {
                vl53L0X = new VL53L0X(i + 1);
            } catch (I2CUpdatableAddress.NACKException nack) {
                System.out.println("VL53L0X Nack Exception on startup position: " + Integer.toString(i));
            }
            if (vl53L0X != null) {
                try {
                    result = vl53L0X.init(true);
                } catch (I2CUpdatableAddress.NACKException e) {
                    System.out.println("VL53L0X Nack Exception on init position: " + Integer.toString(i));
                }
            }
            // Exit from VL53L0X initialization if any sensors do not initialize properly
            if (!result) {
                break;
            } else {
                vl53l0xArray.add(vl53L0X);
            }
        }
        initialized = result;

        if (initialized) {
            List<ArrayBlockingQueue<Integer>> blockingQueueList = new ArrayList<>();
            for (int i = 0; i < doArray.size(); ++i) {
                blockingQueueList.add(new ArrayBlockingQueue<>(2));
            }
            arrayBlockingQueueList = Collections.synchronizedList(blockingQueueList);
            distanceTimer = new java.util.Timer();
            System.out.println("---!!!Starting VL530L0X timer!!!---");
            distanceTimer.schedule(new DistanceBackgroundTask(this), 0L, 20L);
        }
        return result;
    }

    private int getNumberOfSensors() {
        return doArray.size();
    }

    private class DistanceBackgroundTask extends TimerTask {

        private VL53L0XSensors vl53L0XSensors;

        DistanceBackgroundTask(VL53L0XSensors vl53L0XSensors) {
            this.vl53L0XSensors = vl53L0XSensors;
        }

        @Override
        public void run() {
            Vector<Integer> results = new Vector<>();
            try {
                results = vl53L0XSensors.readRangeSingleMillimeters();
            } catch (I2CUpdatableAddress.NACKException nackEx) {
                System.out.println("VL53L0X Sensors NACK:");
            } catch (VL53L0XSensors.NotInitalizedException NotIinitEx) {
                System.out.println("VL53L0X Not Initialized");
            }

            if (results.isEmpty()) {
                for (int i = 0; i < vl53L0XSensors.getNumberOfSensors(); ++i) {
                    results.add(8190);
                }
            }

            int resultsNum = 0;
            for (Integer result : results) {
                ArrayBlockingQueue<Integer> curQueue = arrayBlockingQueueList.get(resultsNum++);
                // If the queue is full take the oldest one from the head.
                if (curQueue.remainingCapacity() == 0) {
                    curQueue.poll();
                }
                try {
                    curQueue.put(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private Vector<Integer> readRangeSingleMillimeters() throws I2CUpdatableAddress.NACKException, NotInitalizedException {
        if (!initialized) {
            throw new NotInitalizedException();
        }

        Vector<Integer> results = new Vector<>();
        for (VL53L0X vl53L0x : vl53l0xArray) {
            int result = vl53L0x.readRangeSingleMillimeters();
            results.add(result);
        }
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

    public class NotInitalizedException extends IOException {
    }

}

