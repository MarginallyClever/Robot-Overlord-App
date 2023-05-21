package com.marginallyclever.misc;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.marginallyclever.convenience.log.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class JSCHTests {
    private static final Logger logger = LoggerFactory.getLogger(JSCHTests.class);
    private static final String DEFAULT_BAUD = "57600";
    private static final String DEFAULT_USB_DEVICE = "/dev/ttyACM0";

    private static final String SHELL_TO_SERIAL_STRING = "picocom -b" + DEFAULT_BAUD + " " + DEFAULT_USB_DEVICE;

	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    @Test
    @Timeout(15)
    public void testConnectAndReadData() throws Exception {
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
        //while(channel.isConnected()) logger.info((char)in.read());

        // TEST 2
        StringBuilder input = new StringBuilder();
        while (channel.isConnected()) {
            input.append((char) in.read());
            if (input.toString().endsWith("\n")) {
                logger.info(input.toString());
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
