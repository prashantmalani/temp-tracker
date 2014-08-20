<?
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

$databasehost = "localhost";
$databasename = "tempdb";
$databaseusername ="prashant";
$databasepassword = "hello123";

// Create connection
$con = mysql_connect($databasehost,$databaseusername,$databasepassword) or die(mysql_error());
mysql_select_db($databasename) or die(mysql_error());
mysql_query("SET CHARACTER SET utf8");
$query = "SELECT * FROM temptracker ORDER BY ts DESC";
$sth = mysql_query($query);

if (mysql_errno()) {
    header("HTTP/1.1 500 Internal Server Error");
    echo $query.'\n';
    echo mysql_error();
}
else
{
    $rows = array();
    while($r = mysql_fetch_assoc($sth)) {
        $rows[] = $r;
    }
    print json_encode($rows);
}

mysql_close($con);
?>
