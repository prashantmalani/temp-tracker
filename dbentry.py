#!/usr/bin/python
#
# Support functions to connect to the desired database and
# store the most recent temperature information.
# This can later be expanded to add more functionality,
#
# like clearing data, updating it. etc
# author : Prashant Malani <p.malani@gmail.com>
# date   : 08/17/2014
#
# The assumption is that there is a table called temptracker, where all
# the information required is stored.

# Imports
import MySQLdb

# Global constants and variables
DEFAULT_TEMP = 63
MYSQL_USER = "prashant"
MYSQL_PASSWORD = "hello123"
MYSQL_IP = "localhost"
MYSQL_DB = "tempdb"
MYSQL_TABLE = "temptracker"


def write_values_to_db(temp, hum):
    """ Write contents to the mysql DB.
    """
    db = MySQLdb.connect(MYSQL_IP, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DB)
    cursor = db.cursor()
    sql = "INSERT INTO %s(timestamp, temp, hum) VALUES(UNIX_TIMESTAMP(), %d, %d)" % (MYSQL_TABLE, temp, hum)
    try:
        cursor.execute(sql)
        db.commit()
    except:
        print "MySQL command did not work!!"
        db.rollback()
    db.close()
