#!/bin/bash
avrdude -v -p atmega2560 -c wiring -P /dev/ttyACM0 -D -U flash:w:./Makelangelo-firmware.ino.hex:i
