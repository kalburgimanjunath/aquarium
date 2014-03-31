# Setting up in Windows 7

##Prerequisites

Install [Scala][1], [Play 2][2] and [MongoDB][3]

[1]: http://www.scala-lang.org/download/
[2]: http://www.playframework.com/download
[3]: http://www.mongodb.org/downloads

## Adding to path

To make the system work/easier to use, add scala, play, mongodb and jdk to Path.  
Scala: \*path_to_scala\*\bin.  
Play: \*path_to_play\*.  
MongoDB: \*path_to_mongodb\*\bin  
JDK: \*path_to_jdk\*\bin. If you have multiple versions installed, just add them all.

To add a to Path, follow the following steps:

1. Right-click on "Computer" (either from start menu or explorer).
2. Click "Properties".
3. Click "Advanced system settings".
4. Click "Enviroment Variables".
5. In "System variables" find the "Path"-variable, select it and click "Edit".
6. Add the paths, as defined above, to the end of the variable. Separate paths with ";".
7. Apply changes with "OK".

## Clone the code

Clone the code from github.

## Getting Mongo running

1. Open a new command prompt
2. Start mongo by typing: mongod (may be running automatically)
3. Open a new command prompt, make sure to leave the first one running.
4. Type the following commands:
	1. cd \*path_to_project\*\mongo
	2. mongo
	3. use aquarium
	4. load("createIndexes.js")
	5. exit

## Starting the server

1. Type: play dependencies --sync
2. Type: cd \*path_to_project\*
3. Type: play
4. Type: test 			
	(needs to be done to use fixtures, if not already loaded)
5. Type: run
	

10. Open your browser and go to the adress localhost:9000. If you haven't compiled yet this might take a while.

Every time you have to start up again, redo step 1 and 2 of "Getting Mongo Running".
