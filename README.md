Sample showing Spring, Akka and Camel working together
=========

To run this from the command prompt with maven use:

mvn install exec:java -DskipTests

Then in your browser go to:
http://localhost:8875/

Notice how the word Akka (not the image) has been replaced with SUPER AKKA along with the count of the times the web page has been hit.

At the command line the following options are available:
m 'new message'   #set a new message that is used to replace the word Akka.
c                 #get a count of how many times the web page has been hit
q                 #quit
