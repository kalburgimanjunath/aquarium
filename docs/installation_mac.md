# Setting up in Mac OSX

#Versions used in this guide (10.1.2014)
* Scala: 2.10.3
* MongoDB: 2.4.8
* Play: 2.2.1
* Java: 1.7.0_45

#What is needed

* [Scala](http://www.scala-lang.org/download/)
* [Play](http://www.playframework.com/download)
* [MongoDB](http://www.mongodb.org/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

# Installation

To install MongoDB and Play using [Homebrew](http://brew.sh) run the following command in the terminal.

	ruby -e "$(curl -fsSL https://raw.github.com/Homebrew/homebrew/go/install)"

Follow the instructions and run:

	brew update

	brew install mongodb

	brew install play

## Clone the code

To get the code, clone it from GitHub:

	$ git clone <Address from project in GitHub>

## Start the project
*Note that mongo needs to be running (usually it has started automatically).*

Go to the project folder

Start the play console:

	$ play

## Install java if prompted to do so

Test and load fixtures	

	$ test 			

Start the application in development mode  

	$ run
