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
MYSQL_USER = "root"
MYSQL_PASSWORD = "password"
MYSQL_IP = "localhost"
MYSQL_DB = "TESTDB"
MYSQL_TABLE = "temptracker"


def write_temp_to_db(temp):
    """ Write contents to the mysql DB.
    """
    db = MySQLdb.connect(MYSQL_IP, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DB)
    cursor = db.cursor()
    sql = "INSERT INTO %s(temp) VALUES(%d)" % (MYSQL_TABLE, temp)
    try:
        cursor.execute(sql)
        db.commit()
    except:
        print "MySQL command did not work!!"
        db.rollback()
    db.close()
