package com.marginallyclever.evilOverlord;

/** \mainpage Evil Overlord
 * \section intro_sec Introduction
 * Evil Overloard is a GUI for controlling Marginally Clever "Evil Minion" robot arms.
 * See https://marginallyclever.com/ for more details.
 * \section install_sec Install instructions for developers
 * \step 
 * download the code from http://github.com/imakerobots/Evil-Overlord
 * \step
 * download and install Eclipse
 * \step
 * in Eclipse, add the project.
 * \step
 * in Eclipse, right click on the project and choose Maven > Update Project
 * \step
 * you should now be able to build and run the program
 */

/**
 * main() entry point into the application.
 * @author danroyer
 *
 */
public class EvilOverlord {
	public static MainGUI gui;
	
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	gui = MainGUI.getSingleton();
	        }
	    });
	}
}
