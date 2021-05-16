# Robot Overlord #

Robot Overlord is 3D control software for robots.  It is intended to be easier than ROS.  It was started by http://www.marginallyclever.com/

We would love to see your robot run in the app.  Please joint our Discord channel and talk live with a human!  https://discord.gg/Q5TZFmB

Some of the robots it controls are:

 - Sixi 2+3, 6DOF arms
 - Arm3, a 3DOF arm
 - Thor, a 5DOF arm
 - Rotary Stewart Platforms, like flight simulators
 - Delta Robot 3, aka a Kossel
 - Spidee, a 6 legged crab robot

# Get Started! ##

Steps to get started:

1. Install The latest OpenJDK (java software)
2. Install Eclipse (prorgramming interface)
3. Install OpenCV
4. Install Robot Overlord App

Then you should be able to run the application.

## Install The latest OpenJDK

Get the Open Java Development Kit (OpenJDK) https://jdk.java.net/ .  The latest version is the "ready to use" edition.

*OSX*: Unarchive the OpenJDK tar, and place the resulting folder (i.e. jdk-12.jdk) into your /Library/Java/JavaVirtualMachines/ folder since this is the standard and expected location of JDK installs. You can also install anywhere you want in reality.

*Windows*: https://stackoverflow.com/a/52531093 _untested_
*Linux*: https://linuxize.com/post/install-java-on-ubuntu-18-04/ _untested_

## Install Eclipse

* Download Eclipse IDE: https://www.eclipse.org/downloads/
* install the latest.  
* Choose "Eclipse IDE for Java developers"

## Install OpenCV

At this time there is no Maven repository that works for us.  You'll have to install OpenCV from source files.
Please see https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html for complete instructions.

## Install Robot Overlord App

* Goto https://github.com/MarginallyClever/Robot-Overlord-App/
* Fownload this zip file: https://github.com/MarginallyClever/Robot-Overlord-App/archive/refs/heads/master.zip and extract it.  Note the folder for later.
* In Eclipse. Fo to File > Import > Maven > Existing Maven Projects > Next > (folder from previous step) > Finish
* If you see the "Weldome!" tab, close it.
* select Run > Debug As > Java Application
* select "Robot Overlord - com.marginallyclever.robotOverlord.RobotOverlord" and click OK.
* wait while Eclipse builds the project and updates some stuff.
* "Errors exist...Proceed with launch?" Select Proceed.

Application should now launch.

# Usage

tbd
