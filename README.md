# Robot Overlord #

http://www.marginallyclever.com/

3D control software for most Marginally Clever robots.

 - Sixi 2, a 6DOF arm
 - Arm3, a 3DOF arm
 - Thor, a 5DOF arm
 - Rotary Stewart Platform 2, a 6 axis motion control platform
 - Delta Robot 3, aka a Kossel
 - Spidee, a 6 legged crab robot

## Installation ##
h1. Help

Please visit "the marginallyclever.com support Discord":https://discord.gg/Q5TZFmB or post an issue ticket to this repository.

h1.

Robot Overlord is is written in Java.  That means you'll need 
- the latest OpenJDK (java software), 
- Eclipse (prorgramming interface),
- the code of the app itself.
- Maybe

h1. Install OpenJDK

Robot Overlord Java Application requires the "Open Java Development Kit (OpenJDK)":https://jdk.java.net/ .  The latest version is the "ready to use" edition.

*OSX*: Unarchive the OpenJDK tar, and place the resulting folder (i.e. jdk-12.jdk) into your /Library/Java/JavaVirtualMachines/ folder since this is the standard and expected location of JDK installs. You can also install anywhere you want in reality.

*Windows*: https://stackoverflow.com/a/52531093 _untested_
*Linux*: https://linuxize.com/post/install-java-on-ubuntu-18-04/ _untested_

h1. Eclipse IDE

* "Download Eclipse IDE":https://www.eclipse.org/ide/, install and run.
* Select _Checkout projects from Git > Clone URI > Url=https://github.com/MarginallyClever/Robot-Overlord-App > Next > (dev & master branch) > Next_.  Note the location of your local github folder for later.
* Install Git if required
* Complete clone of repository
* Do NOT import project.  Close the wizard.
* Eclipse > File > Import > Maven > Existing Maven Projects > (locate robot overlord folder in your local github folder)

You should be mostly error message free now.
* select Robot-Overlord > Debug As > Java Application
* select com.marginallyclever.robotOverlord.RobotOverlord as main class.
* wait while Eclipse builds the workspace
* "Errors exist...Proceed with launch?" Select Proceed.

Application should now launch.

h1. Install OpenCV

At this time there is no Maven repository that works for us.  You'll have to install OpenCV from source files.
Please see https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html for complete instructions.

## Get help ##

Please visit the forums
https://marginallyclever.com/forum

## Misc ##

This file was downloaded from https://github.com/MarginallyClever/Robot-Overlord
This file was last updated 2019-04-06
