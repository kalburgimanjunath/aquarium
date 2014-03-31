# Robotframework

This project uses robotframework for end-to-end tests. (http://robotframework.org/)

# Installation

Requirements:
* pip
* virtualenv (optional)

Create a new virtual environment for python to keep it separated from the system's own python. Do this in the root folder of the project.

    virtualenv venv

Switch to the new environment. You need to do this each time you open a new terminal.

    source venv/bin/activate

Install the robot packages (doesn't support Python 3 yet, use e.g. 2.7)

    pip install robotframework
    pip install robotframework-selenium2library

To run a single test, execute (in the robot folder):

    pybot <testfile>.txt

To run all the tests (defined in the script), execute (in the robot folder):

	./robot_tests.sh

Writing tests in Robot Framework with Selenium2Library:
http://robotframework.googlecode.com/hg/doc/userguide/RobotFrameworkUserGuide.html?r=2.6.2
http://rtomac.github.io/robotframework-selenium2library/doc/Selenium2Library.html