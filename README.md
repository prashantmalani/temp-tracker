temp-tracker
============

The aim is to use a Raspberry Pi to keep a log of room temperature, and read that information using an Android App.


Updates:

09/11:
Summarizing a bunch of edits over the past few weeks:
 - Package data in JSON from server
 - Create an android app:
   + Can get and parse temperature data from web server
   + Can draw graph based on this data
 - Write basic sensor code.
   + Can access both temperature and humidity data
   + Checksum to validate read data.

08/17/2014:
 - switch to using MySQL
 - db name at present is "TESTDB"
 - table name is temptracker; columns are "ts(TIMESTAMP)" and "temp(int)"
 - MySQL username and password are set to "root", and "password"
