package com.marginallyclever.communications.transport.tcp;

import com.jcraft.jsch.UserInfo;

import javax.swing.*;

/**
 * Authentication info for SSH.
 */
public class SSHUserInfo implements UserInfo {
	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		Object[] options = { "yes", "no" };
		int foo = JOptionPane.showOptionDialog(null, 
				message, 
				"Warning", 
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, 
				null, options, options[0]);
		return foo == 0;
	}

	@Override
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
}