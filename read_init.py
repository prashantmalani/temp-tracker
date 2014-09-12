#
# Read and parse sensor data from DHT11
#
#
# Author: Prashant Malani <p.malani@gmail.com>
# Date  : 09/10/2014
#
# Followed the spec in http://www.uugear.com/portfolio/dht11-humidity-temperature-sensor-module/
#
import RPi.GPIO as GPIO
import time

# Local defines
ZERO_THRESH = 3

def bin_array_to_dec(bin_array):
    """ Takes in a binary array, returns equivalent
    decimal number. Assume that the binary array is
    always 8 bits long.
    """
    temp_array = reversed(bin_array)
    result = 0
    exp = 1
    for i in temp_array:
        result += i * exp
        exp *= 2
    return result

def get_sensor_val():
    """ Calleable function which should return the temp and
    humidity values. If either cannot be obtained, (-1, -1) should
    be returned.
    """ 
    # Store the input data here
    data = []
    
    GPIO.setmode(GPIO.BCM)

    # Stuff goes here
    GPIO.setup(4, GPIO.OUT)
    GPIO.output(4, GPIO.LOW)
    time.sleep(.018)
    GPIO.output(4, GPIO.HIGH)

    # Now we move into input mode and wait for the device to respond
    # with following falling edge
    GPIO.setup(4, GPIO.IN)
    for i in range(0,500):
        data.append(GPIO.input(4))

    parsed_bits = []
    num_ones = 0
    cur_bit = 0
    # Analyze the data

    # The first bit is generally the HIGH bit DHT11 sends before it is
    # ready for data transfer so we ignore that.
    for i in range(1, len(data)):
        if data[i] == 0:
            if num_ones == 0:
                pass
            elif num_ones <= ZERO_THRESH:
                parsed_bits.append(0)
                num_ones = 0
            else:
                parsed_bits.append(1)
                num_ones = 0
            if len(parsed_bits) == 41:
               break
        elif data[i] == 1:
            num_ones = num_ones + 1
 
    #We omit the first bit, which is probably setup information anyway
    final_array = parsed_bits[1:] 

    hum_int = bin_array_to_dec(final_array[:8])
    hum_dec = bin_array_to_dec(final_array[8:16])
    temp_int = bin_array_to_dec(final_array[16:24])
    temp_dec = bin_array_to_dec(final_array[24:32])
    obs_checksum = bin_array_to_dec(final_array[32:])

    calc_checksum = hum_int + hum_dec + temp_int + temp_dec

    if calc_checksum != obs_checksum:
        print "CHECKSUM ERROR"
        temp_int = -1
        hum_int = -1
    else:
        print "Humidity is " + str(hum_int)
        print "Temperature is " + str(temp_int)

    GPIO.cleanup()
    return temp_int, hum_int
