# How to install Arduino command line for Sixi Raspberry Pi

##########################
# always first
##########################

# ssh into pi
login: pi
pass: the magic word

##########################
# once
##########################

# install prequisites for inotools + ino tools
pi@raspberrypi:~/ $ sudo pip install configobj
pi@raspberrypi:~/ $ sudo pip install glob2
pi@raspberrypi:~/ $ sudo pip install jinja2
pi@raspberrypi:~/ $ sudo pip install pyserial
pi@raspberrypi:~/ $ sudo pip install ordereddict
pi@raspberrypi:~/ $ sudo apt update
pi@raspberrypi:~/ $ sudo apt install arduino
pi@raspberrypi:~/ $ sudo apt install git
pi@raspberrypi:~/ $ git clone git://github.com/amperka/ino
pi@raspberrypi:~/ $ cd ino
pi@raspberrypi:~/ino $ sudo make install
pi@raspberrypi:~/ino $ cd ../

# install camera + python
pi@raspberrypi:~/ $ sudo apt install python-picamera
pi@raspberrypi:~/ $ sudo apt install python-pip

#install arduino cli: (http://www.raspberryvi.org/stories/arduino-cli.html)

#install serial tool
pi@raspberrypi:~/Robot-Overlord-App/arduino/sixi-pid $ sudo apt install picocom

##########################
# general
##########################


# get arduino sketch from github (once)
pi@raspberrypi:~/ $ git clone git://github.com/MarginallyClever/Robot-Overlord-App

# checkout a branch. example here is '2020-03-dev' (once)
pi@raspberrypi:~/ $ cd Robot-Overlord-App
pi@raspberrypi:~/Robot-Overlord-App $ git checkout 2020-03-dev

# update code in that branch
pi@raspberrypi:~/Robot-Overlord-App $ git pull

# surf to arduino sketch folder
pi@raspberrypi:~/ $ cd arduino/sixi-pid

# build sketch
#build and upload
pi@raspberrypi:~/arduino/sixi-pid $ make upload

# open serial connection
pi@raspberrypi:~/arduino/sixi-pid $ picocom -b57600 /dev/ttyACM0

# to exit picocom serial [CTRL + A] followed by [CTRL + X]

##########################
# always last
##########################

~/ $ exit