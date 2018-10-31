#!/usr/bin/env bash

intexit() {
    # Kill all subprocesses (all processes in the current process group)
    kill -INT -$$
}

hupexit() {
    # HUP'd (probably by intexit)
    echo
    echo "Interrupted"
    exit
}

trap hupexit HUP
trap intexit INT


for i in $(seq 1 8); do java da_proc ${i} membership.txt 10 & done

wait