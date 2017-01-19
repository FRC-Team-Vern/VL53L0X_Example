package org.usfirst.frc.team5461.robot.commands;

import java.nio.ByteBuffer;

import org.usfirst.frc.team5461.robot.sensors.VL53L0X_Constants;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.hal.I2CJNI;
import edu.wpi.first.wpilibj.util.BoundaryException;

public class VL53L0X extends I2C {
	
	private Port m_port = Port.kOnboard;
	//Store address given when the class is initialized.
	//This value can be changed by the changeAddress() function
	private int _i2caddress;
	
	private enum BYTE_SIZE {
		SINGLE(1),
		DOUBLE(2);
		
		public int value;
		
		private BYTE_SIZE(int value) {
			this.value = value;
		}
	};

	public VL53L0X(int deviceAddress) {
		super(Port.kOnboard, deviceAddress);
		this._i2caddress = deviceAddress;
	}
	
	public final int setAddress(int new_address) {
		//NOTICE: CHANGING THE ADDRESS IS NOT STORED IN NON-VOLATILE MEMORY
		// POWER CYCLING THE DEVICE REVERTS ADDRESS BACK TO 0X29
		if (_i2caddress == new_address)
		{
			return _i2caddress;
		}
		// Device addresses cannot go higher than 127
		if (new_address > 127)
		{
			return _i2caddress;
		}

		write(VL53L0X_Constants.I2C_SLAVE_DEVICE_ADDRESS.value, new_address);
		return getAddressFromDevice();
	}
	
	public final int getAddressFromDevice() {
		ByteBuffer deviceAddress = ByteBuffer.allocateDirect(BYTE_SIZE.SINGLE.value);
		read(VL53L0X_Constants.I2C_SLAVE_DEVICE_ADDRESS.value, BYTE_SIZE.SINGLE.value, deviceAddress);
		return deviceAddress.get();
	}
	
	@Override
	public synchronized boolean write(int registerAddress, int data) {
		ByteBuffer dataToSendBuffer = ByteBuffer.allocateDirect(3);
		dataToSendBuffer.putShort((short)registerAddress);
		dataToSendBuffer.put(2, (byte)data);

		return I2CJNI.i2CWrite((byte) m_port.value, (byte) _i2caddress, dataToSendBuffer,
				(byte) 3) < 0;
	}
	
	@Override
	public boolean read(int registerAddress, int count, ByteBuffer buffer) {
		if (count < 1) {
			throw new BoundaryException("Value must be at least 1, " + count +
					" given");
		}

		if (!buffer.isDirect())
			throw new IllegalArgumentException("must be a direct buffer");
		if (buffer.capacity() < count)
			throw new IllegalArgumentException("buffer is too small, must be at least " + count);

		ByteBuffer dataToSendBuffer = ByteBuffer.allocateDirect(2);
		dataToSendBuffer.putShort((short)registerAddress);

		return transaction(dataToSendBuffer, 2, buffer, count);
	}
}
