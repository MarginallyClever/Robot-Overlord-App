package com.marginallyclever.robotOverlord;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JSCHTests {
	private static final String DEFAULT_BAUD = "57600";
	private static final String DEFAULT_USB_DEVICE = "/dev/ttyACM0";
	
    private static final String SHELL_TO_SERIAL_STRING = "picocom -b"+DEFAULT_BAUD+" "+DEFAULT_USB_DEVICE;
    
	@Test(timeout=15000)
	public void testExec() throws Exception {
		JSch jsch = new JSch();
		jsch.setKnownHosts("./.ssh/known_hosts");
		
		Session session = jsch.getSession("pi", "raspberrypi", 22);
		session.setPassword("******");
		session.connect();
		
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(SHELL_TO_SERIAL_STRING);
		channel.connect();
		
		InputStream in = channel.getInputStream();
		// TEST 1
		//while(channel.isConnected()) System.out.print((char)in.read());
		
		// TEST 2
		StringBuilder input = new StringBuilder();
		while(channel.isConnected()) {
			input.append((char)in.read());
			if(input.toString().endsWith("\n")) {
				System.out.println(input);
			}
		}
		/*
		OutputStream out = channel.getOutputStream();
		out.write("D19\n".getBytes());
		out.flush();
		while (channel.isConnected()) {
			try {
				Thread.sleep(1);
			} catch(Exception e) {}
		}
		*/
		assertEquals(2, channel.getExitStatus());
		session.disconnect();
	}
}
