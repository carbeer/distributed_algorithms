#!/bin/bash
#
# Tests the correctness of the Uniform Reliable Broadcast application.
#
# This is an example script that shows the general structure of the
# test. The details and parameters of the actual test might differ.
#

#time to wait for correct processes to broadcast all messages (in seconds)
#(should be adapted to the number of messages to send)
time_to_finish=4

init_time=2

# compile (should output: Da_proc.class)
make -C ..

echo "10
1 127.0.0.1 11001
2 127.0.0.1 11002
3 127.0.0.1 11003
4 127.0.0.1 11004
5 127.0.0.1 11005
6 127.0.0.1 11006
7 127.0.0.1 11007
8 127.0.0.1 11008
9 127.0.0.1 11009
10 127.0.0.1 11010" > ../config/membership

#start 5 processes, each broadcasting 1000 messages
for i in `seq 1 10`
do
    java -cp .. Da_proc $i ../config/membership 1000 &
    da_proc_id[$i]=$!
done

#leave some time for process initialization
sleep $init_time



#start broadcasting
for i in `seq 1 10`
do
    if [ -n "${da_proc_id[$i]}" ]; then
	kill -USR2 "${da_proc_id[$i]}"
    fi
done


#leave some time for the correct processes to broadcast all messages
sleep $time_to_finish

#stop all processes
for i in `seq 1 10`
do
    if [ -n "${da_proc_id[$i]}" ]; then
	kill -TERM "${da_proc_id[$i]}"
    fi
done

#wait until all processes stop
for i in `seq 1 10`
do
    if [ -n "${da_proc_id[$i]}" ]; then
	    wait "${da_proc_id[$i]}"
    fi
done

#check logs for correctness
./check_output.sh `seq 1 10`

echo "Correctness test done."
