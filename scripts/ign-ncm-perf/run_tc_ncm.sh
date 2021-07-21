#! /bin/sh

# script to run TC with fake ACA to send GS to NCM and collect
# NCM timings.



IGN1_IP="10.213.43.163"
IGN2_IP="10.213.43.164"
NCM_IP="10.213.43.161"
TC_IP="10.213.43.90"

NCM_PID=-1
IGN1_PID=-1
IGN2_PID=-1
TC_PID=-1
IGN_LOG=""
NCM_LOG=""
TC_LOG=""
NCM_SUM_LOG=""
ITER=""

IGN1_PID=-1
IGN2_PID=-1

SSH_ID="$HOME/.ssh/user90_id_rsa"

IGNITE_HOME="/home/user/apache-ignite-2.9.1"
ALCOR_HOME="/home/user/alcor-rio"
NCM_CMD="/home/user/bin/runncm.sh"

SYNTAX="[-h] [-i] [-a] -c igncfg -n niter [ports ...]"
OPTS="hiac:n:"

Usage() {
    echo "Usage: `basename $0` [-h] ignite_config_file num_iter [port sequence]"
    echo "default port sequence is 100, 200, 1000, 2000, 3000, 4000, 5000"
    echo "NEEDS TO BE RUN ON 10.213.43.90 (same as TC)"
    echo "IF RUNNING FROM A DIFFERENT MACHINE, GENERATE RSA KEYS"
    echo "AND COPY id_rsa.pub into ~/.ssh/authorized_keys ON"
    echo "TO MACHINES RUNNING NCM AND IGNITE."
    echo "Ignite should be in $IGNITE_HOME"
    echo "Alcor should be in $ALCOR_HOME"
    echo "Ignite and Alcor are in different place, adjust the values"
    echo "above"
    echo
}

if [ $# -lt 2 -o "$1" = "-h" ]; then
    Usage
    exit 1

    exit 1
fi

IGNCFG=${IGNITE_HOME}/config/$1
NUM_ITER=$2

if [ $# -ge 3 ]; then
    shift
    shift
    PORTS="$@"
else
    PORTS="100 200 500 1000 2000 3000 4000 5000"
fi

SEC="`date +%s`"
TODAY="`date +%Y-%m-%d`"
IGNOPT="`echo $IGNCFG | sed -e 's/^.*with-//' -e 's/\.xml//'`"
IGNOPT="`echo $IGNOPT | sed 's/[\t -]*txn-*//'`"

echo "IGNOPT = $IGNOPT"

start_ignite() {
    #  Node [id=e8a540de, uptime=00:01:00.007]
    # Cluster is active
    IP=$1
    if echo "$IP" | fgrep 163; then
        NODE="01"
    elif echo "$IP" | fgrep 164; then
        NODE="02"
    else
        echo "Unknown Ignite machine, I only know about 10.213.43.{163|164}"
        exit 1
    fi

    echo "Starting Ignite on $IP ..."
    IGN_LOG="$IGNITE_HOME/ign-${NODE}-${TODAY}-${IGNOPT}-${SEC}.log"
    IGN_LOG="`echo ${IGN_LOG} | sed 's/--//g'`"
    ssh -i $SSH_ID ${IP} "cd $IGNITE_HOME && bin/ignite.sh -v $IGNCFG > ${IGN_LOG} 2>&1 &" > /dev/null 2>&1 &

    doagain=true
    while $doagain ; do
        echo "Sleeping for Ignite to start on $IP ..."
        sleep 5
        ssh -i $SSH_ID ${IP} "fgrep 'uptime=' $IGN_LOG" > ign-state.tmp
        if fgrep 'uptime=' ign-state.tmp > /dev/null 2>&1; then
            ssh -i $SSH_ID ${IP} "echo y | $IGNITE_HOME/bin/control.sh --set-state ACTIVE"
            ssh -i $SSH_ID $IP "$IGNITE_HOME/bin/control.sh --state" > ign-state.tmp
            if fgrep "Cluster is active" ign-state.tmp; then
                cat ign-state.tmp
                doagain=false
            fi
        fi
    done

    ssh -i $SSH_ID $IP "fgrep 'PID: ' $IGN_LOG | awk -F: '{print \$NF}'" > ign-pid.tmp
    if [ ! -s ign-pid.tmp ]; then
        echo "Failed to get Ignite PID"
        exit 1
    fi
    if [ "$NODE" = "01" ]; then
        IGN1_PID=`cat ign-pid.tmp`
    else
        IGN2_PID=`cat ign-pid.tmp`
    fi

    echo "*** Ignite on $IP Started"

    return 0
}

start_ncm() {
    PORT="$1"
    SEQ="$2"
    NCM_LOG="$ALCOR_HOME/ncm-${TODAY}-${IGNOPT}-${PORT}p-${SEQ}.log"
    NCM_SUM_LOG="$ALCOR_HOME/ncm-${TODAY}-${IGNOPT}-${PORT}p.log"
    echo "Starting NCM: ports $PORT iteration $SEQ"
    ssh -i $SSH_ID $NCM_IP "$NCM_CMD > $NCM_LOG 2>&1&" &

    doagain=true
    while $doagain ; do
        echo "Sleeping for NCM to start ..."
        sleep 5
        ssh -i $SSH_ID $NCM_IP "fgrep 'Server blockUntilShutdown, list' $NCM_LOG" > ncm-state.tmp
        if fgrep 'Server blockUntilShutdown, list' ncm-state.tmp; then
            doagain=false
        else
            sleep 5
        fi
    done

    ssh -i $SSH_ID $NCM_IP "fgrep 'with PID' $NCM_LOG | sed 's/.*PID \([0-9][0-9]*\) .*$/\1/' | tr -d '\n'" > ncm-state.tmp
    if [ ! -s ncm-state.tmp ]; then
        echo "Can't find NCM pid"
        exit 1
    else
        NCM_PID=`cat ncm-state.tmp`
    fi
    echo "*** NCM Started"
    return 0
}

start_tc() {
    PORT=$1
    SEQ="$2"
    TC_LOG="tc-${TODAY}-${IGNOPT}-${PORT}p-${SEQ}.log"
    cd /home/user/rpk/alcor/services/pseudo_controller
    mvn exec:java -D exec.mainClass=com.futurewei.alcor.pseudo_controller.pseudo_controller -Dexec.args="$PORT 10.213.43.90 10.213.43.94 10.213.43.161 9016 root Huawei@2018" -e > $TC_LOG 2>&1 &
    ps aux | grep 'com.futurewei.alcor.pseudo_controller.pseudo_controller' | grep -v grep | awk '{print $2}' > tc-state.tmp
    if [ ! -s tc-state.tmp ]; then
        echo "TC did not start"
        exit 1
    else
        TC_PID=`cat tc-state.tmp`
    fi
    echo "TC Started"
}

kill_tc() {
    kill $TC_PID
    SIG=""
    doagain=true
    while $doagain ; do
        sleep 5
        kill $SIG $TC_PID
        if [ $? -ne 0 ]; then
            doagain=false
        else
            ps -p $TC_PID > tc-state.tmp
            if fgrep $TC_PID tc-state.tmp; then
                sleep 5
                SIG="-9"
            else
                doagain=false
            fi
        fi
    done
}

kill_proc() {
    IP=$1
    PID=$2
    SIG=""
    ssh -i $SSH_ID $IP "kill $PID"
    doagain=true
    while $doagain ; do
        sleep 5
        ssh -i $SSH_ID $IP "kill ${SIG} $PID"
        ssh -i $SSH_ID $IP "ps -p $PID | grep $PID" > proc-state.tmp
        sleep 5
        if fgrep 'No such process' proc-state.tmp; then
            doagain=false
        else
            if fgrep $PID proc-state.tmp > /dev/null 2>&1; then
                SIG="-9"
            else
                doagain=false
            fi
        fi
    done
}

kill_ignite() {
    kill_proc $IGN1_IP $IGN1_PID
    kill_proc $IGN2_IP $IGN2_PID
}

check_ncm_done() {
    doagain=true
    while $doagain ; do
        ssh -i $SSH_ID $NCM_IP "fgrep ' getVniInLoop : done' $NCM_LOG" > ncm-state.tmp
        if fgrep ' getVniInLoop : done' ncm-state.tmp; then
            sleep 20
            domore=true
            while $domore ; do
                ssh -i $SSH_ID $NCM_IP "fgrep ' Start expire sessions StandardManager' $NCM_LOG" > ncm-state.tmp
                if fgrep ' Start expire sessions StandardManager' ncm-state.tmp; then
                    domore=false
                    return 0
                fi
            done
        fi
    done
}

clear_ncm_sum_log() {
    NCM_SUM_LOG="$ALCOR_HOME/ncm-${TODAY}-${IGNOPT}-${p}p.log"
    echo "Clearing consolidted log $ncm_sum_log"
    echo "ssh -i $SSH_ID $NCM_IP cat /dev/null > $NCM_SUM_LOG"
    ssh -i $SSH_ID $NCM_IP "cat /dev/null > $NCM_SUM_LOG"
}

consolidate_ncm_sum_log() {
    NCM_LOG="$ALCOR_HOME/ncm-${TODAY}-${IGNOPT}-${1}p-${2}.log"
    NCM_SUM_LOG="$ALCOR_HOME/ncm-${TODAY}-${IGNOPT}-${1}p.log"
    echo "Adding $NCM_LOG to consolidated log $NCM_SUM_LOG"
    echo "ssh -i $SSH_ID $NCM_IP cat $NCM_LOG >> $NCM_SUM_LOG"
    ssh -i $SSH_ID $NCM_IP "cat $NCM_LOG >> $NCM_SUM_LOG"
}

# run NCM, TC in loop for all the ports
run_tests() {
    for p in ${PORTS}; do
        clear_ncm_sum_log $p
        k=1
        while [ $k -le $NUM_ITER ]; do
            iter="`printf "%02d" $k`"	
            start_ncm $p $iter
            if [ $? -ne 0 ]; then
                echo "NCM did not start"
                exit 1
            fi

            start_tc $p $iter
            check_ncm_done
            kill_tc
            kill_proc $NCM_IP $NCM_PID
            k=`expr $k + 1`
	    JOBS="`jobs`"
	    echo "JOBS: $JOBS"
            consolidate_ncm_sum_log $p $iter
        done
    done
}

stop_ignite() {
    echo "About to test kill_ignite ..."
    sleep 5
    for ip in $IGN1_IP $IGN2_IP; do
        kill_ignite $ip
    done
}


# main
for ip in $IGN1_IP $IGN2_IP; do
    start_ignite $ip
    if [ $? -ne 0 ]; then
        echo "Ignite on $ip did not start"
        exit 1
    fi
    echo "Ignite started on $ip"
done

run_tests
stop_ignite
