package communication;

import java.io.IOException;

import jssc.*;

public class SerialComm {

	SerialPort port;

	private boolean debug;  // Indicator of "debugging mode"
	
	// This function can be called to enable or disable "debugging mode"
//	void setDebug(boolean mode) {
//		debug = mode;
//	}	
//	
//
//	// Constructor for the SerialComm class
//	public SerialComm(String name) throws SerialPortException {
//		port = new SerialPort(name);		
//		port.openPort();
//		port.setParams(SerialPort.BAUDRATE_9600,
//			SerialPort.DATABITS_8,
//			SerialPort.STOPBITS_1,
//			SerialPort.PARITY_NONE);
//		
//		debug = false; // Default is to NOT be in debug mode
//	}
		
	// TODO: Add writeByte() method from Studio 4
	
	// TODO: Add available() method
	
	// TODO: Add readByte() method	
	
	// TODO: Add a main() method
	
	void setDebug(boolean mode) {
		debug = mode;
	}
	

	// Constructor for the SerialComm class
	public SerialComm(String name) throws SerialPortException {	
		port = new SerialPort(name);
		port.openPort();
		port.setParams(SerialPort.BAUDRATE_9600,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
		
		 debug = false; // Default is to NOT be in debug mode
	}
		
		// TODO: Add writeByte() method to write data to serial port (Studio Section 5)
	public void writeByte(byte b) throws SerialPortException {
			port.writeByte(b);
			if(debug) {
				System.out.print("<0x" + String.format("%02x",b)+">");
			}
	}

	// TODO: Add available() method (Studio Section 6)
		public boolean available() throws SerialPortException {
			int input = port.getInputBufferBytesCount();
			if(input >= 0) {
				return true;
			}
			else {
				return false;
			}					
		}
		
//	 TODO: Add readByte() method (Studio Section 6)
	public byte readByte() throws SerialPortException, IOException {
		byte[] b = port.readBytes(1);
		byte firstbyte = b[0];
//		int a = firstbyte & 0xff;
//		if(a < 0) {
//			System.out.println("this is the byte firstbyte before adding 256 " + firstbyte);
//			a = ~a + 1;
			
//			System.out.println("This is the integer which was reversed " + a);
//		}
//		System.out.println("This is the integer being casted to byte a after adding 256 " + (byte)(a));
//		a = a & 0xff;
		if(debug) {
			System.out.print("[" + String.format("%02x",firstbyte) + "]");
		}
		return firstbyte;
	} 
	
	public static void main (String[] args) throws SerialPortException, IOException {
		SerialComm port;
		port = new SerialComm("COM3");
		port.setDebug(true);
		int readinput1 = 0;
		int readinput2 = 0;
		while(true) {
			while(port.available() == true) {
//				port.writeByte(readinput1);
				readinput1 = (port.readByte() & 0xff);
				readinput2 = (port.readByte() & 0xff);
				int val1 = readinput1;
				val1 = val1 << 8;
				int val2 = readinput2;
//				val2 = val2 >> 8;
				System.out.println(val1 + val2);
//				System.out.println(val2);
			}
		}	
}
}
