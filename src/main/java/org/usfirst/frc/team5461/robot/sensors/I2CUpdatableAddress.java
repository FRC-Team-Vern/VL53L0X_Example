/*----------------------------------------------------------------------------*/
/* Copyright (c) 2008-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5461.robot.sensors;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.hal.HAL;
import edu.wpi.first.wpilibj.hal.I2CJNI;
import edu.wpi.first.wpilibj.util.BoundaryException;

import static java.util.Objects.requireNonNull;

/**
 * I2C bus interface class.
 *
 * <p>This class is intended to be used by sensor (and other I2C device) drivers. It probably should
 * not be used directly.
 */
public class I2CUpdatableAddress {
    public enum Port {
        kOnboard(0), kMXP(1);

        @SuppressWarnings("MemberName")
        public final int value;

        Port(int value) {
            this.value = value;
        }
    }

    private final int m_port;
    private int m_defaultAddress;
    private int m_deviceAddress;

    /**
     * Constructor.
     *
     * @param port          The I2C port the device is connected to.
     * @param deviceAddress The address of the device on the I2C bus.
     */
    public I2CUpdatableAddress(Port port, int defaultAddress, int deviceAddress) throws NACKException {
        m_port = port.value;
        m_defaultAddress = defaultAddress;
        m_deviceAddress = defaultAddress;

        I2CJNI.i2CInitialize((byte) port.value);
        setAddress(deviceAddress);
        HAL.report(tResourceType.kResourceType_I2C, deviceAddress);
    }

    private final int setAddress(int new_address) throws NACKException {
        //NOTICE: CHANGING THE ADDRESS IS NOT STORED IN NON-VOLATILE MEMORY
        // POWER CYCLING THE DEVICE REVERTS ADDRESS BACK TO 0x29
        if (m_defaultAddress == new_address || new_address > 127)
        {
            return m_defaultAddress;
        }

        boolean aborted = write(VL53L0X_Constants.I2C_SLAVE_DEVICE_ADDRESS.value, new_address & 0x7F);
        if (!aborted) {
            m_deviceAddress = new_address;
        }

        return new_address;
    }

    /**
     * Destructor.
     */
    public void free() {
        I2CJNI.i2CClose(m_port);
    }

    /**
     * Generic transaction.
     *
     * <p>This is a lower-level interface to the I2C hardware giving you more control over each
     * transaction.
     *
     * @param dataToSend   Buffer of data to send as part of the transaction.
     * @param sendSize     Number of bytes to send as part of the transaction.
     * @param dataReceived Buffer to read data into.
     * @param receiveSize  Number of bytes to read from the device.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public synchronized boolean transaction(byte[] dataToSend, int sendSize,
                                            byte[] dataReceived, int receiveSize) throws NACKException {
        if (dataToSend.length < sendSize) {
            throw new IllegalArgumentException("dataToSend is too small, must be at least " + sendSize);
        }
        if (dataReceived.length < receiveSize) {
            throw new IllegalArgumentException(
                    "dataReceived is too small, must be at least " + receiveSize);
        }
        boolean aborted = I2CJNI.i2CTransactionB(m_port, (byte) m_deviceAddress, dataToSend,
                (byte) sendSize, dataReceived, (byte) receiveSize) < 0;
        if (aborted) {
            throw new NACKException();
        }
        return false;
    }

    /**
     * Generic transaction.
     *
     * <p>This is a lower-level interface to the I2C hardware giving you more control over each
     * transaction.
     *
     * @param dataToSend   Buffer of data to send as part of the transaction.
     * @param sendSize     Number of bytes to send as part of the transaction.
     * @param dataReceived Buffer to read data into.
     * @param receiveSize  Number of bytes to read from the device.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public synchronized boolean transaction(ByteBuffer dataToSend, int sendSize,
                                            ByteBuffer dataReceived, int receiveSize) throws NACKException {
        if (dataToSend.hasArray() && dataReceived.hasArray()) {
            return transaction(dataToSend.array(), sendSize, dataReceived.array(), receiveSize);
        }
        if (!dataToSend.isDirect()) {
            throw new IllegalArgumentException("dataToSend must be a direct buffer");
        }
        if (dataToSend.capacity() < sendSize) {
            throw new IllegalArgumentException("dataToSend is too small, must be at least " + sendSize);
        }
        if (!dataReceived.isDirect()) {
            throw new IllegalArgumentException("dataReceived must be a direct buffer");
        }
        if (dataReceived.capacity() < receiveSize) {
            throw new IllegalArgumentException(
                    "dataReceived is too small, must be at least " + receiveSize);
        }

        boolean aborted = I2CJNI.i2CTransaction(m_port, (byte) m_deviceAddress, dataToSend,
                (byte) sendSize, dataReceived, (byte) receiveSize) < 0;

        if (aborted) {
            throw new NACKException();
        }
        return false;
    }

    /**
     * Attempt to address a device on the I2C bus.
     *
     * <p>This allows you to figure out if there is a device on the I2C bus that responds to the
     * address specified in the constructor.
     *
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public boolean addressOnly() throws NACKException {
        return transaction(new byte[0], (byte) 0, new byte[0], (byte) 0);
    }

    /**
     * Execute a write transaction with the device.
     *
     * <p>Write a single byte to a register on a device and wait until the transaction is complete.
     *
     * @param registerAddress The address of the register on the device to be written.
     * @param data            The byte to write to the register on the device.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public synchronized boolean write(int registerAddress, int data) throws NACKException {
        byte[] buffer = new byte[2];
        buffer[0] = (byte) registerAddress;
        buffer[1] = (byte) data;
        boolean aborted = I2CJNI.i2CWriteB(m_port, (byte) m_deviceAddress, buffer,
                (byte) buffer.length) < 0;
        if (aborted) {
            throw new NACKException();
        }
        return false;
    }

    /**
     * Execute a write transaction with the device.
     *
     * <p>Write multiple bytes to a register on a device and wait until the transaction is complete.
     *
     * @param data The data to write to the device.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public synchronized boolean writeBulk(byte[] data) throws NACKException {
        return writeBulk(data, data.length);
    }

    /**
     * Execute a write transaction with the device.
     *
     * <p>Write multiple bytes to a register on a device and wait until the transaction is complete.
     *
     * @param data The data to write to the device.
     * @param size The number of data bytes to write.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public synchronized boolean writeBulk(byte[] data, int size) throws NACKException {
        if (data.length < size) {
            throw new IllegalArgumentException(
                    "buffer is too small, must be at least " + size);
        }
        boolean aborted = I2CJNI.i2CWriteB(m_port, (byte) m_deviceAddress, data, (byte) size) < 0;
        if (aborted) {
            throw new NACKException();
        }
        return false;
    }

    /**
     * Execute a write transaction with the device.
     *
     * <p>Write multiple bytes to a register on a device and wait until the transaction is complete.
     *
     * @param data The data to write to the device.
     * @param size The number of data bytes to write.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public synchronized boolean writeBulk(ByteBuffer data, int size) throws NACKException {
        if (data.hasArray()) {
            boolean aborted = writeBulk(data.array(), size);
            try {
                destroyDirectByteBuffer(data);
            }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return aborted;
        }
        if (!data.isDirect()) {
            throw new IllegalArgumentException("must be a direct buffer");
        }
        if (data.capacity() < size) {
            throw new IllegalArgumentException(
                    "buffer is too small, must be at least " + size);
        }

        boolean aborted = I2CJNI.i2CWrite(m_port, (byte) m_deviceAddress, data, (byte) size) < 0;
        try {
            destroyDirectByteBuffer(data);
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return aborted;
    }

    /**
     * Execute a read transaction with the device.
     *
     * <p>Read bytes from a device. Most I2C devices will auto-increment the register pointer
     * internally allowing you to read consecutive registers on a device in a single transaction.
     *
     * @param registerAddress The register to read first in the transaction.
     * @param count           The number of bytes to read in the transaction.
     * @param buffer          A pointer to the array of bytes to store the data read from the device.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public boolean read(int registerAddress, int count, byte[] buffer) throws NACKException {
        requireNonNull(buffer, "Null return buffer was given");

        if (count < 1) {
            throw new BoundaryException("Value must be at least 1, " + count + " given");
        }
        if (buffer.length < count) {
            throw new IllegalArgumentException("buffer is too small, must be at least " + count);
        }

        byte[] registerAddressArray = new byte[1];
        registerAddressArray[0] = (byte) registerAddress;

        return transaction(registerAddressArray, registerAddressArray.length, buffer, count);
    }

    private ByteBuffer m_readDataToSendBuffer = null;

    /**
     * Execute a read transaction with the device.
     *
     * <p>Read bytes from a device. Most I2C devices will auto-increment the register pointer
     * internally allowing you to read consecutive registers on a device in a single transaction.
     *
     * @param registerAddress The register to read first in the transaction.
     * @param count           The number of bytes to read in the transaction.
     * @param buffer          A buffer to store the data read from the device.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public boolean read(int registerAddress, int count, ByteBuffer buffer) throws NACKException {
        if (count < 1) {
            throw new BoundaryException("Value must be at least 1, " + count + " given");
        }

        if (buffer.hasArray()) {
            boolean aborted = read(registerAddress, count, buffer.array());

            try {
                destroyDirectByteBuffer(buffer);
            }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return aborted;
        }

        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("must be a direct buffer");
        }
        if (buffer.capacity() < count) {
            throw new IllegalArgumentException("buffer is too small, must be at least " + count);
        }

        synchronized (this) {
            if (m_readDataToSendBuffer == null) {
                m_readDataToSendBuffer = ByteBuffer.allocateDirect(1);
            }
            m_readDataToSendBuffer.put(0, (byte) registerAddress);

            return transaction(m_readDataToSendBuffer, 1, buffer, count);
        }
    }

    /**
     * Execute a read only transaction with the device.
     *
     * <p>Read bytes from a device. This method does not write any data to prompt the device.
     *
     * @param buffer A pointer to the array of bytes to store the data read from the device.
     * @param count  The number of bytes to read in the transaction.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public boolean readOnly(byte[] buffer, int count) throws NACKException {
        requireNonNull(buffer, "Null return buffer was given");
        if (count < 1) {
            throw new BoundaryException("Value must be at least 1, " + count + " given");
        }
        if (buffer.length < count) {
            throw new IllegalArgumentException("buffer is too small, must be at least " + count);
        }

        boolean aborted = I2CJNI.i2CReadB(m_port, (byte) m_deviceAddress, buffer,
                (byte) count) < 0;

        if (aborted) {
            throw new NACKException();
        }

        return false;
    }

    /**
     * Execute a read only transaction with the device.
     *
     * <p>Read bytes from a device. This method does not write any data to prompt the device.
     *
     * @param buffer A pointer to the array of bytes to store the data read from the device.
     * @param count  The number of bytes to read in the transaction.
     * @return Transfer Aborted... false for success, true for aborted.
     */
    public boolean readOnly(ByteBuffer buffer, int count) throws NACKException {
        if (count < 1) {
            throw new BoundaryException("Value must be at least 1, " + count
                    + " given");
        }

        if (buffer.hasArray()) {
            boolean aborted = readOnly(buffer.array(), count);
            try {
                destroyDirectByteBuffer(buffer);
            }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return aborted;
        }

        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("must be a direct buffer");
        }
        if (buffer.capacity() < count) {
            throw new IllegalArgumentException("buffer is too small, must be at least " + count);
        }

        boolean aborted =  I2CJNI.i2CRead(m_port, (byte) m_deviceAddress, buffer, (byte) count) < 0;

        if (aborted) {
            throw new NACKException();
        }

        try {
            destroyDirectByteBuffer(buffer);
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Send a broadcast write to all devices on the I2C bus.
     *
     * <p>This is not currently implemented!
     *
     * @param registerAddress The register to write on all devices on the bus.
     * @param data            The value to write to the devices.
     */
    // public void broadcast(int registerAddress, int data) {
    // }

    /**
     * Verify that a device's registers contain expected values.
     *
     * <p>Most devices will have a set of registers that contain a known value that can be used to
     * identify them. This allows an I2C device driver to easily verify that the device contains the
     * expected value.
     *
     * @param registerAddress The base register to start reading from the device.
     * @param count           The size of the field to be verified.
     * @param expected        A buffer containing the values expected from the device.
     * @return true if the sensor was verified to be connected
     * @pre The device must support and be configured to use register auto-increment.
     */
    public boolean verifySensor(int registerAddress, int count,
                                byte[] expected) throws NACKException {
        // TODO: Make use of all 7 read bytes
        byte[] dataToSend = new byte[1];

        byte[] deviceData = new byte[4];
        for (int i = 0, curRegisterAddress = registerAddress;
             i < count; i += 4, curRegisterAddress += 4) {
            int toRead = count - i < 4 ? count - i : 4;
            // Read the chunk of data. Return false if the sensor does not
            // respond.
            dataToSend[0] = (byte) curRegisterAddress;
            if (transaction(dataToSend, 1, deviceData, toRead)) {
                return false;
            }

            for (byte j = 0; j < toRead; j++) {
                if (deviceData[j] != expected[i + j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
        cleanerMethod.setAccessible(true);
        Object cleaner = cleanerMethod.invoke(toBeDestroyed);
        Method cleanMethod = cleaner.getClass().getMethod("clean");
        cleanMethod.setAccessible(true);
        cleanMethod.invoke(cleaner);
    }

    public class NACKException extends IOException{}
}
