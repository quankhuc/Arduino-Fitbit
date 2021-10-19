package communication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jssc.*;

public class MsgReceiver {
	


	final private SerialComm port;
	
	final byte MAGIC_NUMBER = 0x21;
	final byte INFO_KEY = 0x30;
	final byte ERROR_STRING = 0x31;
	final byte TIMESTAMP_KEY = 0x32;
	final byte PED_KEY = 0x35;
	final byte BUTTON_KEY = 0x36;
	final byte SLEEPTIME_KEY = 0x37;
	
	int stringindex = 0;
	int timesindex = 0;
	int stringorder = 0;
	long timestamp = 0;
	long wordlength = 0;
	long timesvalue = 0;
	int count = 0;
	int high_alarm_index = 0;
	int high_alarm_order = 0;
	long high_alarm_length = 0;
	int high_alarm_count = 0;
	byte button_status = 0;
	int steps = 0;
	int stepindex = 0;
	int currentStepCount = 0; 
	byte ped_value = 0;
	int ped_index = 0;
	byte ped_xvalue = 0;
	byte ped_yvalue = 0;
	byte ped_zvalue = 0;
	float x = 0;
	float y = 0;
	float z = 0;
	int k;
	int i;
	int j;
	byte b = 0;
	int c = 0;
	byte h = 0;
	int stepcount = 0;
	long sleepvalue = 0;
	int sleepindex = 0;
	float buffer[] = new float[500];
	int stepCount = 0;
	int buffer_step = 0;
	int step_index = 0;
	boolean mode = true;
	double SLEEP_XY_THRESHOLD = 0.1;
	double SLEEP_Z_THRESHOLD = 1.0; 
	long sleepTime = 0;
	boolean resetState = true;
	String lastword_sleep_reset;
	String sleep_reset;
	String info;
	String lastword_info;
	State state = State.READ_MAGIC;
	long startTime = 0;
	
	enum State {
		READ_MAGIC,
		DISPLAY_ERROR_BYTE,
		READ_KEY,
		READ_TIMESTAMP_VALUE,
		READ_INFO_LENGTH,
		READ_INFO_VALUE, 
		READ_HIGH_ALARM_LENGTH, 
		READ_HIGH_ALARM,
//		READ_ACCELEROMETER_X,
//		READ_ACCELEROMETER_Y,
//		READ_ACCELEROMETER_Z,
		READ_PEDOMETER,
		READ_BUTTON,
		READ_PED_RESET,
		READ_SLEEP_RESET,
		READ_SLEEP,
		READ_STEP_COUNT,
	}

	
	public MsgReceiver(String portname) throws SerialPortException {
		port = new SerialComm(portname);
		port.setDebug(false);
	}
	
	

	
	
	public void run() throws SerialPortException, IOException {
		// insert FSM code here to read msgs from port
		// and write to console
		
		while(true) {
			if(port.available()) {
//				byte b = port.readByte();
//				receiving a byte from the Arduino

				// Note, so long as we only read a byte in the above call, then this code is completely non-blocking.
				// If we were to call readByte again below, it would be a blocking call which would pause program
				// execution while waiting to receive a byte from the serial port.  We don't want to do this when
				// we write non-blocking code.  In order to read multiple bytes to compose our data value, we instead
				// define an index and value variable to keep track of our state during processing.  When we finally
				// receive a full voltage sample (timestamp + voltage value), then we can print it to the console.

				// Process the byte in a finite-state machine (FSM) according to our communication protocol.
				// Our protocol is as follows:
				//    1-byte header:     Magic number
				//    Variable payload:  Key-Value pair
				switch (state) {
				// Read the 1-byte header (i.e. the magic number).
				
				case READ_MAGIC:
					byte a = port.readByte();
//					System.out.println("THIS IS THE MAGIC NUMBER: " + a);
					if (a == MAGIC_NUMBER) {
						state = State.READ_KEY;
					}
					break;

				// Read the key portion of the payload.
				case READ_KEY:
					// Interpret our protocol key.
					b = port.readByte();
					switch (b) {
					case INFO_KEY:
//						System.out.print("INFO_KEY");
						state = State.READ_INFO_LENGTH;
						stringindex = 0;
						stringorder = 0;
						wordlength = 0;
						count = 0;
						break;
					case TIMESTAMP_KEY:
						state = State.READ_TIMESTAMP_VALUE;
						// Initialize our state variables here every time!
						timesindex = 0;
						timesvalue = 0;
						timestamp = 0;
						break;
					case ERROR_STRING:
						state = State.READ_HIGH_ALARM_LENGTH;
						high_alarm_index = 0;
						high_alarm_order = 0;
						high_alarm_length = 0;
						high_alarm_count = 0;
						break;
					case PED_KEY:
						state = State.READ_PEDOMETER;
						ped_index = 0;
						x = 0;
						y = 0;
						z = 0;
						break;
					case BUTTON_KEY:
						state = State.READ_BUTTON;
						button_status = 0;
						
						break;
					case SLEEPTIME_KEY:
						state = State.READ_SLEEP;
						break;
					default:
						state = State.READ_MAGIC;
						System.out.println();
						break;
					}
					break;

					
				//add index to read x, y, z value correspondingly
					//have an index varaible
					//need to convert the receinivg byte to float by multipying by 0.1
				case READ_SLEEP:
					long sleep = port.readByte();
					sleepvalue = (sleepvalue << 8) | (sleep & 0xff);
					++sleepindex;
					if (sleepindex == 4) {
						// We've read all 4 bytes, so save the timestamp.  We will print it later.
						sleepTime = sleepvalue;
						state = State.READ_MAGIC;
					}
					break;
				
				case READ_STEP_COUNT:
					stepcount = ((port.readByte() & 0xff) << 8);
					stepcount = (port.readByte() & 0xff);
					currentStepCount = stepcount;
					System.out.println("THIS IS THE NUMBER OF STEPS: " + currentStepCount);
					state = State.READ_MAGIC;
					break;
					
				case READ_PEDOMETER:
					c = (port.readByte() & 0xff);
					switch(ped_index) {
					case 0:
						x = (float) (c * 0.1);
						System.out.println("THIS IS X: " + x);
						break;
					case 1:
						y = (float) (c * 0.1);
						System.out.println("THIS IS Y: " + y);
						break;
					case 2:
						z = (float) (c * 0.1);
						countSteps(z);
						System.out.println("THIS IS Z: " + z);
						state = State.READ_MAGIC;
						break;
					}
					ped_index++;
					break;

				// Read the timestamp value in our payload.
				case READ_TIMESTAMP_VALUE:
					long e = port.readByte();
					timesvalue = (timesvalue << 8) | (e & 0xff);
					++timesindex;
					if (timesindex == 4) {
						// We've read all 4 bytes, so save the timestamp.  We will print it later.
						timestamp = timesvalue;
						System.out.print("This is the time stamp: ");
						System.out.print(timestamp + " ms");
						System.out.println();
						state = State.READ_MAGIC;
					}
					break;
				//Read the string info sent from the Arduino
				
				case READ_INFO_LENGTH:
					int f = port.readByte();
					stringorder = (f & 0xff);
					wordlength = stringorder;
//					System.out.println("THIS IS WORDLENGTH BEFORE GOING INTO OTHER STATE: " + wordlength);
					if(stringindex < 1) 
					{
					++stringindex;
					}
					else if(stringindex == 1) 
					{
						state = State.READ_INFO_VALUE;
					}		
					//need to know when we can get out of reading characters. Have READ_INFO_LENGTH state separate from READ_INFO then make a READ_INFO_VALUE
					break;
					
				case READ_INFO_VALUE:
					byte g = port.readByte();
					byte[] input = {g};
					count ++;
//					System.out.println("THIS IS WORD LENGTH: " + wordlength);
//					System.out.println("THIS IS COUNT: " + count);
					if(count >= wordlength) {
						info = new String(input, StandardCharsets.UTF_8);
						System.out.print(info);
						System.out.println();
						state = State.READ_MAGIC;
						break;
					}
					lastword_info = new String(input, StandardCharsets.UTF_8);
					System.out.print(lastword_info);
					break;

					
					
//				//Read the single-byte boolean, true for pressed, false for not pressed
				case READ_BUTTON:
					button_status = (byte) (port.readByte() & 0xff);
//					System.out.print(button_status);
					if (button_status == 1) {
						boolean val = true;
						System.out.println("THIS IS THE STATUS OF THE BUTTON: " + val);
						if(mode) {
							mode = false;
							System.out.println("THIS IS THE SLEEP MODE: " + mode);
							countsleep(x,y,z);
						}
						else {
							mode = true;
						}
					}
					else {
						boolean val = false;
						System.out.println("THIS IS THE STATUS OF THE BUTTON: " + val);
						countsleep(x,y,z);
					}
					state = State.READ_MAGIC;
					break;
//				
//				//Read the high alarm if the voltage is above 3V
				case READ_HIGH_ALARM_LENGTH:
					byte high_alarm_read = port.readByte();
					high_alarm_order = (high_alarm_read & 0xff);
					high_alarm_length = high_alarm_order;
//					System.out.println("THIS IS WORDLENGTH BEFORE GOING INTO OTHER STATE: " + wordlength);
					if(high_alarm_index < 1) 
					{
					++high_alarm_index;
					}
					else if(high_alarm_index == 1) 
					{
						state = State.READ_HIGH_ALARM;
					}		
					//need to know when we can get out of reading characters. Have READ_INFO_LENGTH state separate from READ_INFO then make a READ_INFO_VALUE
					break;
				
				case READ_HIGH_ALARM:
					byte high_alarm_string = port.readByte();
					byte[] high_alarm_input = {high_alarm_string};
					high_alarm_count++;
					if(high_alarm_count >= high_alarm_length) {
						sleep_reset = new String(high_alarm_input, StandardCharsets.UTF_8);
						System.out.print(sleep_reset);
						System.out.println();
						state = State.READ_MAGIC;
						break;
					}
					lastword_sleep_reset = new String(high_alarm_input, StandardCharsets.UTF_8);
					System.out.print(lastword_sleep_reset);
					break;
					
				// make default state is reading the magic number
				default:
					h = port.readByte();
					System.out.println("!!!Error!!! " + "This is an error message: " + "(" + String.format("%02x",h) + ")");
					state = State.READ_MAGIC;
					break;
				}
		}

			// We can do other processing here if we want since the above processing is non-blocking.
		}
	}

//if I use modulo don't use the for loop. Change step_index to a global variable. Store the mod 100 value into a variable and have a if statement to check. Stepcounnt + 1 or Stepcount -1  	
public void countSteps(float steps) {
		if(mode) {
		for(int i = 0; i < buffer.length; i++) {
			buffer[i] = steps;
		}
		float max_buffer = findpeak();
		if(max_buffer > 1.2) {
			stepCount++;
			System.out.println("THIS IS THE NUMER OF STEPS: " + stepCount);
			checkreset();
		}
}
}

public float findpeak() {
	int i; 
    
    // Initialize maximum element 
    float max = buffer[0]; 
   
    // Traverse array elements from second and 
    // compare every element with current max   
    for (i = 1; i < buffer.length; i++) 
        if (buffer[i] > max) 
            max = buffer[i]; 
   
    return max; 
}

public void countsleep (float a, float b, float c) {
	System.out.println("THIS IS THE MODE BEFORE IT COUNTS SLEEPS: " + mode);
	if(mode == false) {
		if((x < SLEEP_XY_THRESHOLD) && (y < SLEEP_XY_THRESHOLD) && (z < SLEEP_Z_THRESHOLD)) {
		sleepTime = timestamp - startTime;
		System.out.println("THIS IS THE SLEEP TIME: " + sleepTime);
		checkreset();
	}
	//when the user wake up
	else {
		sleepTime = 0;
		//need to reset the startTime in order to have a new sleeping interval
		startTime = timestamp;
	}
}
}


public void checkreset() {
	System.out.println("THIS IS THE MODE BEFORE RESET: " + mode);
	if(resetState != mode) {
		resetState = mode;
		System.out.println("THIS IS THE resetState AFTER RESET: " + resetState);
		if(resetState == false) {
			sleepTime = 0;
			startTime = timestamp;
//			String temp1 = info;
//			System.out.print(temp1);
//			String temp2 = lastword_info;
//			System.out.println(temp2);		
			}
		else {
		//reset number of step to 0
			stepCount = 0;
//			System.out.print(sleep_reset);
//			System.out.println(lastword_sleep_reset);
		}
		}
	}

	public static void main(String[] args) throws SerialPortException, IOException {
		MsgReceiver msgr = new MsgReceiver("COM3"); // Adjust this to be the right port for your machine
		msgr.run();
	}
}


