#! /bin/sh -x


GIT_REPO=""
GIT_BRANCH=""
GIT_COMMIT=""

git_check_current() {
    # get commit details from the remote and local branches
    # if remote is not latest and FORCE is not specified, skip
    # building aca
}


git_reset() {
    git reset --hard || {
        echo "ERROR: git reset failed"
        exit 1
    }
    echo "Success: git reset"
}

git_fetch() {
    git fetch || {
        echo "ERROR: git fetch failed"
        exit 1
    }
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
        -e '/^[\t ]*nohup[\t ][\t ]*$BUILD\/bin\/AlcorControlAgent /d' ./aca-machine-init.sh > ./aca-machine-init-jenkins.sh
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

if [ $# -lt 2 ]; then
    echo "$0 repo branch [commit]"
    echo "Failure: ACA machine init"
    exit 1
fi

GIT_REPO=$1
GIT_BRANCH=$2
if [ $# -eq 3 ]; then
    GIT_COMMIT=$3
else
    GIT_COMMIT=""
fi

git_check_current
git_reset
git_fetch
git_checkout
do_build
