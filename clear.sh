#/bin/bash

# Due to the annoying job or double clicking and removeing junk kmls, this script will automate the task.
# Run the script as su. "sudo clear.sh". Or the rabbitmq-server will not restart.
# 29-01-2010 - bubby.

# NOTE: Make sure the directory exists before running the rm commands

#Remove the KMLS
if [ -d kml ]; then
    echo "Clearing KMLs..."
    cd ./kml
    rm -rf *.kml
    cd ..
fi

#Remove the logs
if [ -d logs ]; then
    echo "Clearing Logs..."
    cd ./logs
    rm -rf *
    cd ..
fi

#Remove the logs
if [ -d savedstates ]; then
    echo "Clearing States..."
    cd ./savedstates
    rm -rf *.data
    cd ..
fi

#Restart the server
/etc/init.d/rabbitmq-server restart
