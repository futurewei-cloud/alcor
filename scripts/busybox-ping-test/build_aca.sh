#! /bin/sh -x


GIT_REPO=""
GIT_BRANCH=""
GIT_COMMIT=
GIT_URL=
REMOTE_COMMIT=
LOCAL_COMMIT=
DO_FORCE=0
DO_BUILD=0

git_check_current() {
    # get commit details from the remote and local branches
    # if remote is not latest and FORCE is not specified, skip
    # building aca

    # local info
    LOCAL_INFO=`git log origin/$GIT_BRANCH | head -3`
    LOCAL_COMMIT=`echo $LOCAL_INFO | awk '/^commit/ {print $2}'`
    LOCAL_DATE=`echo $LOCAL_INFO | sed 's/^.*Date://' | awk '{print $1, $2, $3, $4, $5}'`
}


git_check_remote() {
    REMOTE_INFO=`git log origin/$GIT_BRANCH | head -3`
    REMOTE_COMMIT=`echo $REMOTE_INFO | awk '/^commit/ {print $2}'`
    REMOTE_DATE=`echo $REMOTE_INFO |  sed 's/^.*Date://' | awk '{print $1, $2, $3, $4, $5}'`

    RSEC=`date --date="{REMOTE_DATE}" +%s`
    LSEC=`date --date="{LOCAL_DATE}" +%s`

    DIFF=`echo $RSEC - $LSEC | bc`
    if [ $DIFF -gt 0 ]; then
        DO_BUILD=1
    fi
}


git_reset() {
    git reset --hard || {
        echo "ERROR: git reset failed"
        exit 1
    }
    echo "Success: git reset"
}

git_fetch() {
    git fetch --force --tags $GIT_URL || {
        echo "ERROR: git fetch failed"
        exit 1
    }

    git config remote.origin.url $GIT_URL
}


git_checkout() {
    git checkout $ACA_BRANCH || {
        echo "git checkout $ACA_BRANCH failed"
        exit 1
    }

    echo "Success: git checkout $ACA_BRANCH"
}

do_build() {
    cd build
    sed -e 's/"$1" == "delete-bridges"/ -n "$1" -a "$1" = "delete-bridges"/' \
        -e '/^[\t ]*nohup[\t ][\t ]*$BUILD\/bin\/AlcorControlAgent /d' \
        -e '1i \
#! /bin/bash
' ./aca-machine-init.sh > ./aca-machine-init-jenkins.sh
    chmod +x ./aca-machine-init-jenkins.sh
    D1=`date +%s`
    echo "Started build in `pwd`..."
    sed -i -- 's/[\t ]*-Werror*//g' CMakeLists.txt
    rm -f ../CMakeCache.txt ../cmake_install.cmake > /dev/null 2>&1
    sudo ./aca-machine-init-jenkins.sh > /tmp/amij.log 2>&1
    D2=`date +%s`
    echo "Build finished in `expr $D2 - $D1` seconds, waiting for 60 seconds..."
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
FORCED_BUIL     = $DO_FORCE
"

git_check_current

if [ $DO_FORCE -eq 1 ]; then
    git_checkout
    git_reset
    git_fetch
fi

git_check_remote

if [ $DO_BUILD -eq 1 ]; then
    echo "Skipping the build"
    echo "Success: ACA machine init"
    exit 0
    do_build
fi
