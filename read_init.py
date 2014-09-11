#
#
#
import RPi.GPIO as GPIO
import time

# Store the input data here
data = []

GPIO.setmode(GPIO.BCM)

# Stuff goes here
GPIO.setup(4, GPIO.OUT)
GPIO.output(4, GPIO.LOW)
time.sleep(.018)
GPIO.output(4, GPIO.HIGH)
#time.sleep(.000005)

# Now we move into input mode and wait for the device to respond
# with following falling edge
GPIO.setup(4, GPIO.IN)
for i in range(0,300):
	data.append(GPIO.input(4))

print data
GPIO.cleanup()
