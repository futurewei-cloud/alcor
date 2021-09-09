#! /bin/sh

# Pull latest ACA code from specified repository, branch and commit
# and call aca_machine_init.sh to do the actual build.
# Emit success or failure so that the remote script learn about the status
# of the build.


GIT_REPO=""
GIT_BRANCH=""
GIT_COMMIT=
GIT_URL=
REMOTE_COMMIT=
LOCAL_COMMIT=
DO_FORCE=0
DO_BUILD=0

# Get local repo/branch/commit info
git_check_current() {
    LOCAL_INFO=`git log origin/$GIT_BRANCH | head -3`
    LOCAL_COMMIT=`echo $LOCAL_INFO | awk '/^commit/ {print $2}'`
    LOCAL_DATE=`echo $LOCAL_INFO | sed 's/^.*Date://' | awk '{print $1, $2, $3, $4, $5}'`
}


# Get remote repo/branch/commit info
# May need git fetch -all to be robust
git_check_remote() {
    REMOTE_INFO=`git log origin/$GIT_BRANCH | head -3`
    REMOTE_COMMIT=`echo $REMOTE_INFO | awk '/^commit/ {print $2}'`
    REMOTE_DATE=`echo $REMOTE_INFO |  sed 's/^.*Date://' | awk '{print $1, $2, $3, $4, $5}'`

    RSEC=`date --date="${REMOTE_DATE}" +%s`
    LSEC=`date --date="${LOCAL_DATE}" +%s`

    DIFF=`echo $RSEC - $LSEC | bc`
    if [ $DO_FORCE -eq 1 ]; then
        DO_BUILD=1
    elif [ $DIFF -gt 0 ]; then
        DO_BUILD=1
    fi
}


# Need to avoid getting stuck on stale modifications
git_reset() {
    git reset --hard || {
        echo "ERROR: git reset failed"
        exit 1
    }
    echo "Success: git reset"
}


# Fetch the specified repo
git_fetch() {
    git fetch --force --tags $GIT_URL || {
        echo "ERROR: git fetch failed"
        exit 1
    }

    git config remote.origin.url $GIT_URL
    git pull
}


# checkout the specified branch (add commit later).
git_checkout() {
    git checkout $GIT_BRANCH || {
        echo "git checkout $GIT_BRANCH failed"
        exit 1
    }

    echo "Success: git checkout $GIT_BRANCH"
}


# Start the build on the node.
# Waits for remote builds to finish.
do_build() {
    cd build
    sed -e '/^[\t ]*nohup[\t ][\t ]*$BUILD\/bin\/AlcorControlAgent /d' \
        ./aca-machine-init.sh > ./aca-machine-init-jenkins.sh
    chmod +x ./aca-machine-init-jenkins.sh
    D1=`date +%s`
    echo "Started build in `pwd`..."
    rm -f ../CMakeCache.txt ../cmake_install.cmake > /dev/null 2>&1
    sudo ./aca-machine-init-jenkins.sh > /tmp/amij.log 2>&1
    D2=`date +%s`
    echo "Build finished in `expr $D2 - $D1` seconds, waiting a little..."
    OSZ=0
    while :; do
        NSZ=`ls -l /tmp/amij.log | awk '{print $5}'`
        if [ $NSZ -eq $OSZ ]; then
            break
        else
            OSZ=$NSZ
            sleep 5
        fi
    done

    if fgrep "Built target AlcorControlAgent" /tmp/amij.log > /dev/null 2>&1; then
        echo "Success: ACA machine init"
        exit 0
    else
        echo "Failure: ACA machine init"
        exit 1
    fi
}

if [ $# -lt 4 ]; then
    echo "$0 repo branch commit force {0|1}"
    echo "Failure: ACA machine init"
    exit 1
fi

echo "build_aca started on `uname`"

GIT_REPO=$1
GIT_BRANCH=$2
GIT_COMMIT=$3

if [ $GIT_COMMIT = "HEAD" ]; then
    GIT_COMMIT=
fi

DO_FORCE=$4

if [ "$GIT_REPO" != "futurewei-cloud" -o "$BIT_BRANCH" != "master" -o -n "$GIT_COMMIT" ]; then
    echo "Can't check status of remote repository other than"
    echo "futurewei-cloud, forcing a build"
    DO_FORCE=1
fi

GIT_URL="https://github.com/$GIT_REPO/alcor-control-agent"

echo "build_aca using:
GIT_URL         = $GIT_URL
GIT_BRANCH      = $GIT_BRANCH
GIT_COMMIT      = $GIT_COMMIT
FORCED_BUILD    = $DO_FORCE
"

git_check_current

if [ $DO_FORCE -eq 1 ]; then
    git_checkout
    git_reset
    git_fetch
fi

git_check_remote

if [ $DO_BUILD -eq 0 ]; then
    echo "Skipping the build"
    echo "Success: ACA machine init"
    exit 0
else
    do_build
fi
