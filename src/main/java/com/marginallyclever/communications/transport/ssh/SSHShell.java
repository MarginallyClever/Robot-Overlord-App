package com.marginallyclever.communications.transport.ssh;

import com.jcraft.jsch.*;
import com.marginallyclever.convenience.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */

/**
 * This program enables you to connect to sshd server and get the shell prompt.
 *   $ CLASSPATH=.:../build javac Shell.java
 *   $ CLASSPATH=.:../build java Shell
 * You will be asked username, hostname and passwd.
 * If everything works fine, you will get the shell prompt. Output may
 * be ugly because of lacks of terminal-emulation, but you can issue commands.
 *
 */
public class SSHShell {
	private static final Logger logger = LoggerFactory.getLogger(SSHShell.class);
	public static void main(String[] arg) {
		Log.start();
		try {
			JSch jsch = new JSch();

			jsch.setKnownHosts("./.ssh/known_hosts");

			String host = null;
			if (arg.length > 0) {
				host = arg[0];
			} else {
				host = JOptionPane.showInputDialog("Enter username@hostname",
						System.getProperty("user.name") + "@localhost");
			}
			String user = host.substring(0, host.indexOf('@'));
			host = host.substring(host.indexOf('@') + 1);

			Session session = jsch.getSession(user, host, 22);

			String passwd = JOptionPane.showInputDialog("Enter password");
			session.setPassword(passwd);

			UserInfo ui = new MyUserInfo();

			session.setUserInfo(ui);

			// It must not be recommended, but if you want to skip host-key check,
			// invoke following,
			// session.setConfig("StrictHostKeyChecking", "no");

			// session.connect();
			session.connect(30000); // making a connection with timeout.

			Channel channel = session.openChannel("shell");

			// Enable agent-forwarding.
			// ((ChannelShell)channel).setAgentForwarding(true);

			channel.setInputStream(System.in);
			/*
			 * // a hack for MS-DOS prompt on Windows. channel.setInputStream(new
			 * FilterInputStream(System.in){ public int read(byte[] b, int off, int
			 * len)throws IOException{ return in.read(b, off, (len>1024?1024:len)); } });
			 */

			channel.setOutputStream(System.out);

			/*
			 * // Choose the pty-type "vt102". ((ChannelShell)channel).setPtyType("vt102");
			 */

			/*
			 * // Set environment variable "LANG" as "ja_JP.eucJP".
			 * ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
			 */

			// channel.connect();
			channel.connect(3 * 1000);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * user info for authentication.
	 */
	public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		@Override
		public String getPassword() {
			return passwd;
		}

		@Override
		public boolean promptYesNo(String str) {
			Object[] options = { "yes", "no" };
			int foo = JOptionPane.showOptionDialog(null, str, "Warning", JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			return foo == 0;
		}

		String passwd;
		JTextField passwordField = (JTextField) new JPasswordField(20);

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public boolean promptPassphrase(String message) {
			return true;
		}

		@Override
		public boolean promptPassword(String message) {
			Object[] ob = { passwordField };
			int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				passwd = passwordField.getText();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}

		final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
		private Container panel;

		@Override
		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
				boolean[] echo) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts = new JTextField[prompt.length];
			for (int i = 0; i < prompt.length; i++) {
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add(new JLabel(prompt[i]), gbc);

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if (echo[i]) {
					texts[i] = new JTextField(20);
				} else {
					texts[i] = new JPasswordField(20);
				}
				panel.add(texts[i], gbc);
				gbc.gridy++;
			}

			if (JOptionPane.showConfirmDialog(null, panel, destination + ": " + name, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
				String[] response = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					response[i] = texts[i].getText();
				}
				return response;
			} else {
				return null; // cancel
			}
		}
	}
}
