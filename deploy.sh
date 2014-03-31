#!/bin/bash
cd /opt/akvaario/target/universal/stage/
cat RUNNING_PID
if [ $? -eq 0 ]; then
        echo "Service is running."
        cat RUNNING_PID | xargs kill
        if [ $? -eq 0 ]; then
            echo "Killing service."
            rm RUNNING_PID && echo "Service stopped."
        else
            echo "Unable to kill service."
            exit 1
        fi
fi

BUILD_ID=dontKillMe nohup bin/akvaario -Dconfig.file=/opt/akvaario/target/universal/stage/conf/env_ci.conf -Dhttp.port=9999 &
if [ $? -eq 0 ]; then
    echo "Service started."
fi
exit
