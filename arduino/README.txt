# raspberry pi + arduino #
##########################
# ssh into pi
login: pi
pass: the magic word

##########################
# once
##########################

# install prequisites for inotools + ino tools
~/$ sudo pip install configobj
~/$ sudo pip install glob2
~/$ sudo pip install jinja2
~/$ sudo pip install pyserial
~/$ sudo pip install ordereddict
~/$ sudo apt update
~/$ sudo apt install arduino
~/$ sudo apt install git
~/$ git clone git://github.com/amperka/ino
~/$ cd ino
~/ino$ sudo make install
~/ino$ cd ../

# install camera + python
~/$ sudo apt install python-picamera
~/$ sudo apt install python-pip

#install arduino cli: (http://www.raspberryvi.org/stories/arduino-cli.html)


##########################
# general
##########################


# get arduino sketch from github (once)
~/$ git clone git://github.com/MarginallyClever/Robot-Overlord-App

# checkout a branch. example here is '2020-03-dev' (once)
~/$ cd Robot-Overlord-App
~/Robot-Overlord-App$ git checkout 2020-03-dev

# update code in that branch
~/Robot-Overlord-App$ git pull

# surf to arduino sketch folder
$ cd arduino/sixi-pid

# build sketch
#build and upload
~/arduino/sixi-pid $ make upload

