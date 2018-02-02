Cups4J Server
=============
JEE 7 print service implementation for JBoss/Wildfly 11 (http://wildfly.org). 

Features
======== 
- Web Service accepting print jobs and queries for job status
- Print job creation in database
- Signal new print jobs via JMS
- Retry print jobs that could not be printed because of recoverable errors (network connection refused etc.)
- Automatic print job maintenance (update job state in database)
- Take actions when print jobs complete (f.ex. remove from database)
- Simple Java web service client to print from command line or to be included in client apps 

Get ready to run
================

First run Wildfly 11 with full profile. This is necessary in order to be able to use message driven beans.
set WILDFLY_HOME to {your installation directory}/wildfly-11.0.0.Final
set JAVA_HOME to your Java 8 or later installation (JDK)

Change to {your installation directory}/wildfly-11.0.0.Final/bin and run from there:
on UNIX/Linux
./standalone.sh -c standalone-full.xml
or Windows
standalone.bat -c standalone-full.xml


Deploy the application to Wildfly:
==================================
change to cups4jserver directory and run
mvn clean install wildfly:deploy 

Test your installation:
=======================
Try to print a PDF or Postscript file
run java -jar client/target/server-client-1.0.0-SNAPSHOT.one-jar.jar to get help on command line parameters possible.


For a quick print on localhost running Wildfly and CUPS with a Printer named "PDF" you can try to print the test pdf file this way:
java -jar client/target/server-client-1.0.0-SNAPSHOT.one-jar.jar -w localhost -h localhost -P PDF -f client/pdf.pdf

Cups4J Server will go through the following steps:
- persist the print job in database and return the primary key as jobID to the client
- send a JMS message to the print job queue to signal a new print job
- message receiver will fetch this print job from the database and print it via Cups4J on the printer "PDF". 
  Dependent on if printing to CUPS succeeds the print job in database will be updated with status spooled, error or recoverable_error. 
- every 30sec. the print job maintenance will start and check the print job database 
  - check all jobs that are "spooled" and update status in database as long as job state is NOT completed
  - completed jobs are removed from database
  - try to reprint jobs with recoverable errors (connection errors while trying to print to CUPS server and similar errors) 
- if the database table of print jobs is empty every printed job has completed

Conclusion:
===========
The current implementation is a quick shot that shows how such kind of print server could work. 
There is in general no need to use JMS in a print server like this, but it can be of good use in complex situations with more servers or a dedicated print server. At least it is impressive to see how easy it is today to make use of messaging without the need to change a single configuration file or deployment descriptor. 

Anyway, at this time this is demonstration code not intended to be used in production environments.

 





