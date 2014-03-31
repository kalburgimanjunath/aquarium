# Setting up in Linux

##Prerequisites

Install [Scala][1], [Play 2][2], [MongoDB][3], [Java JDK][4], [Git][5]

[1]: http://www.scala-lang.org/download/
[2]: http://www.playframework.com/download
[3]: http://www.mongodb.org/downloads
[4]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[5]: https://www.digitalocean.com/community/articles/how-to-install-git-on-ubuntu-12-04

## Add play to path

For example on Ubuntu add this line to the end of .bashrc in the Home-folder
	
	PATH=/path/to/play:${PATH}

## Clone the code

To get the code, clone it from GitHub:

	$ git clone <Address from project in GitHub>

# Play 2

## Start the project
*Note that mongo needs to be running (usually it has started automatically).*

Go to the project folder

Start the play console:

	$ play

Run the tests and load fixtures:

	$ test 			

Start the server in development mode:

	$ run
