#! /bin/sh -x

# Build ACA on ACA nodes from alcor_services.ini

ALCOR_INI=alcor_services.ini
SCRIPT_DIR=`dirname $0`
SCRIPT_DIR=`realpath $SCRIPT_DIR`

ACA_DIR="repos/aca"
ACA_NODES=""

ACA_REPO="futurewei-cloud"
ACA_BRANCH="master"
ACA_COMMIT=""
USER_REPO=0
USER_BRANCH=0

get_aca_node_info() {
     ACA_NODES=`sed -n '/^\[AlcorControlAgents\]/,/^\[/p' ${ALCOR_INI} | grep -v '^\[' | grep -v '^[\t ]*$' |  awk -F= '{print $2}' | tr -d '[\t ]'`

    # TEMP: Until merge
    ACA_NODES="172.31.25.39 172.31.17.252"
}

if [ -d "${SCRIPT_DIR}" -a -s ${SCRIPT_DIR}/${ALCOR_INI} ]; then
    true
else
    echo "Missing ${ALCOR_INI} file, can only run in alcor/scripts/busybox-ping-test"
    exit 1
fi

while getopts "rb" opt; do
    case "$opt" in
        r) ACA_REPO=$OPTARG
            USER_REPO=1
            ;;

        b) ACA_BRANCH=$OPTARG
            USER_BRANCH=1
            ;;

        c) ACA_COMMIT=$OPTARG
            ;;

        f)  DO_FORCE=1
            ;;
        ?)
            echo "Usage: $0 [-r repo] [-b branch] [-c commit] [-f]
-r repo     : repository, if using a personal fork.
-b branch   : branch, if using a personal branch.
-c commit   : commit, if not using the HEAD.
-f          : to force a build even if there are no changes in remote.
"
            exit 1
            ;;
    esac
done

get_aca_node_info
NC=0
for node in `echo ${ACA_NODES}`; do
    NC=`expr $NC + 1`
    scp ./build_aca.sh ubuntu@${node}:${ACA_DIR}/
    ssh ubuntu@$node "cd $ACA_DIR && sudo ./build_aca.sh $ACA_REPO $ACA_BRANCH" > /tmp/aca_${node}_build.log 2>&1 &
done

# check the status
SC=0
for node in `echo ${ACA_NODES}`; do
    while :; do
        if fgrep 'Success: ACA machine init' /tmp/aca_${node}_build.log > /dev/null 2>&1; then
            SC=`expr $SC + 1`
            break
        elif fgrep 'Failure: ACA machine init' /tmp/aca_${node}_build.log > /dev/null 2>&1; then
            echo "ACA Build failed on $node"
            exit 1
        else
            sleep 60
        fi
    done
    if [ $SC -eq $NC ]; then
        break
    fi
done
exit 0
