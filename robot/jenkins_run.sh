#!/bin/bash

pidof Xvfb
if [ $? -eq 0 ]; then
    echo "Xvfb Running"
else
    echo "Starting Xvfb"
    Xvfb :89 -ac -noreset &
    echo "Xvfb Started"
fi

export DISPLAY=:89
pybot --outputdir $1/output/ basic_test.txt layout_test.txt email_test.txt