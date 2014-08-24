#!/usr/bin/python
#
# Script to read temperature and humidity data and store
# it in a file  (along with time information)
#
# @author : Prashant Malani <p.malani@gmail.com>
# @date   : 08/14/2014
#
# It is expected that this script is run as a cronjob, at an
# interval of 1 min.
#
# Format used for storage should be : "MM/DD/YYYY-HH:MM,TTT"
# Where TTT stands for temperature.
# This file assumes that the MySQL server has been set up correctly.
# Please see the README.txt file for complete information.

# Imports
from datetime import datetime
import random
import os
import dbentry

# Global variable definitions


def get_temp():
    """ Get temperature data.
    """
    # TODO: Call sensor routine to get temperature
    cur_temp = random.randint(0, 100)
    return cur_temp;

def write_to_file(temp, date_obj):
    """ Write contents to a file.
    The file is name .sensor_info, and is located in the user's
    home directory.
    """
    temp_str = str(temp).zfill(3)
    date_str  = date_obj.strftime('%m/%d/%Y-%H:%M')
    home_path = os.path.expanduser("~")
    file_ptr = open(home_path + "/.sensor_info", "a");
    file_ptr.write(date_str + "," + temp_str + "\n")

def main():
    """ Call sub routines to return temperature data,
    other sensor data, and then call function to write
    to write the data to a file.
    """
    temp = get_temp();
    hum = get_temp();
    # If you are using mySQL, this isn't required.
    # cur_date = datetime.now()
    #write_to_file(temp, cur_date)
    dbentry.write_values_to_db(temp, hum);

if __name__ == '__main__':
    main()
