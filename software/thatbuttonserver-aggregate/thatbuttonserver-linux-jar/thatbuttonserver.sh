#!/bin/bash

NAME=thatbuttonserver
RUNNING_FILENAME=${NAME}.running

function showUsage {
    echo "Usage: ${NAME}.sh start"
    echo "   or  ${NAME}.sh stop"
}

function start {
    if [[ -e ${RUNNING_FILENAME} ]]
    then
        echo "${NAME} already running"
    else
        echo "starting ${NAME}"
        touch ${RUNNING_FILENAME}
    fi
}

function stop {
    if [[ -e ${RUNNING_FILENAME} ]]
    then
        echo "stopping ${NAME}"
        rm ${RUNNING_FILENAME}
    else
        echo "${NAME} not running"
    fi
}

if [[ $# -ne 1 ]]
then
    showUsage
elif [[ $1 == "start" ]]
then
    start
elif [[ $1 == "stop" ]]
then
    stop
else
    showUsage
fi
