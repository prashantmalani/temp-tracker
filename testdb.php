<?php
/*
 * File to extract temperature data from mysql db.
 *
 * @author : Prashant Malani <p.malani@gmail.com>
 * date    : 08/20/2014
 *
 * There are a few assumptions which need to be made.
 * This file needs to be placed in the system's /var/www/ directory.
 *
 * We assume that the database used is 'tempdb', the user is 'prashant'
 * , the password is 'hello123', and the table name is 'temptracker'.
 *
 */

// Create connection
$con=mysqli_connect("localhost","prashant","hello123","tempdb");

// Check connection
if (mysqli_connect_errno()) {
	echo "Failed to connect to MySQL: " . mysqli_connect_error();
    exit;
} else {
	echo " Connection was successful";
}

// Do stuff
echo "<br>";
$result = mysqli_query($con, "select * from temptracker order by ts desc");
while ($row = mysqli_fetch_array($result)) {
    echo $row['ts'] . " " . $row['temp'];
    echo "<br>";
}

// Close connection
mysqli_close($con);
?>
