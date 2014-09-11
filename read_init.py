#
#
#
import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)

# Stuff goes here
GPIO.setup(4, GPIO.OUT, pull_up_down = GPIO.PUD_UP)


GPIO.cleanup()
