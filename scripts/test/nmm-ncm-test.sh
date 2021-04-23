# MIT License
# Copyright(c) 2020 Futurewei Cloud
#
#     Permission is hereby granted,
#     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
#     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
#     to whom the Software is furnished to do so, subject to the following conditions:
#
#     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#    
#     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
#     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#! /bin/sh

PAYTEMP="payload-tmp-$$.json"
GETTEMP="gettemp-tmp-$$.out"
CURLTEMP="curl-out-tmp.$$.out"
NODEIDS=
ALL_NODES=

DEF_NMMURI="http://localhost:9007"
DEF_NCMURI="http://localhost:9014"
DEF_NCMID="test_ncm_001"
DEF_NCMCAP=10

UPLOAD_FILE=../json/nodes-upload.json

trap "rm -f $PAYTEMP $GETTEMP $CURLTEMP > /dev/null 2>&1" 0 1 2 3 4 5 6 7 8 10 11 12 13 14 15

BOK=1
DOK=1
GOK=1
POK=1
FOK=1

BULK='{
   "host_infos": [
      {
         "local_ip": "20.213.43.10",
         "node_id": "f109465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C1:A0:C9:34:C8:29",
         "node_name": "blk909030",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
      },
      {
         "local_ip": "20.213.43.11",
         "node_id": "f209465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C2:A0:C9:34:C8:29",
         "node_name": "blk909031",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
      },
      {
         "local_ip": "20.213.43.13",
         "node_id": "f309465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C3:A0:C9:34:C8:29",
         "node_name": "blk909032",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
      },
      {
         "local_ip": "20.213.43.14",
         "node_id": "f409465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C4:A0:C9:34:C8:29",
         "node_name": "blk909033",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
      },
      {
         "local_ip": "20.213.43.15",
         "node_id": "f509465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C5:A0:C9:34:C8:29",
         "node_name": "blk909034",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
      }
   ]
}'

N1='
{
         "local_ip": "10.213.43.10",
         "node_id": "d109465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A1:A0:C9:34:C8:29",
         "node_name": "ncm009030",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
}'

N2='
{
         "local_ip": "10.213.43.11",
         "node_id": "d209465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A2:A0:C9:34:C8:29",
         "node_name": "ncm009031",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
}'

N3='
{
         "local_ip": "10.213.43.13",
         "node_id": "d309465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A3:A0:C9:34:C8:29",
         "node_name": "ncm009032",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
}'

N4='
{
         "local_ip": "10.213.43.14",
         "node_id": "d409465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A4:A0:C9:34:C8:29",
         "node_name": "ncm009033",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
}'

N5='
{
         "local_ip": "10.213.43.15",
         "node_id": "d509465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A5:A0:C9:34:C8:29",
         "node_name": "ncm009034",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
}'

N5ID="d509465c-5ed6-400e-806d-fbefd12f94a9"
N5OIP="10.213.43.15"
N5NIP="101.213.43.151"

N5N='
{
         "local_ip": "101.213.43.151",
         "node_id": "d509465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A5:A0:C9:34:C8:29",
         "node_name": "ncm009034",
         "server_port": 50001,
         "veth": "eth0",
         "ncm_id" : "test_ncm_001"
}'

N6='
{
         "local_ip": "11.213.43.15",
         "node_id": "d609465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "F5:A0:C9:34:C8:29",
         "node_name": "ncm009035",
         "server_port": 50001,
         "veth": "eth0"
}'

setnodeids() {
    for node in "$N1" "$N2" "$N3" "$N4" "$N5"; do
        NODEID=`getnodeid "$node"`
        NODEIDS="$NODEIDS $NODEID"
    done

    BULK_NODES=`echo "$BULK" | awk -F: '/node_id/ {print $2}' | sed 's/[", \t]//g' | tr '[\r\n]' ' '`
    UPLOAD_NODES=`awk -F: '/node_id/ {print $2}' $UPLOAD_FILE | sed 's/[", \t]//g' | tr '[\r\n]' ' '`
    ALL_NODES="$NODEIDS $BULK_NODES $UPLOAD_NODES"
}

getnodeid() {
    NODEID=`echo "$1" | grep node_id | sed -e 's/[,"]//g' -e 's/node_id://' -e 's/[\t ][\t ]*//g'`
    echo $NODEID
}

getnmm() {
    RET=0
    curl  --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*' -o $CURLTEMP -X GET "http://localhost:9007/nodes/$1"
    fgrep $1 $CURLTEMP > /dev/null 2>&1
    return $?
}

getncm() {
    RET=1
    curl --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*' -o $CURLTEMP -X GET "http://localhost:9014/nodes/$1"
    fgrep $1 $CURLTEMP > /dev/null 2>&1
    return $?
}

doget() {
    cat /dev/null > $GETTEMP
    GOK=1
    for node in "$N1" "$N2" "$N3" "$N4" "$N5"; do
        PAY='{
        "host_info" : '
        PAY="$PAY $node
    }"
        NODEID=`getnodeid "$PAY"`
        echo "GET $NODEID"
        getnmm $NODEID
        if [ $? -ne 0 ]; then
            GOK=0
        fi
        cat $CURLTEMP >> $GETTEMP
        echo "GET $NODEID"
        getncm $NODEID
        if [ $? -ne 0 ]; then
            GOK=0
        fi
        cat $CURLTEMP >> $GETTEMP
    done
}

dopost() {
    POK=1
    for node in "$N1" "$N2" "$N3" "$N4" "$N5"; do
        PAY='{
        "host_info" : '
        PAY="$PAY $node
    }"
        NODEID=`getnodeid "$PAY"`
        echo "NMM POST $NODEID"
        echo "$PAY" > $PAYTEMP
        curl  --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*' -X POST -o $CURLTEMP -d@${PAYTEMP} http://localhost:9007/nodes
        if [ $? -ne 0 ]; then
            POK=0
        fi
        echo "NMM GET $NODEID"
        getnmm $NODEID
        fgrep $NODEID $CURLTEMP > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            POK=0
        fi

        echo "NCM GET $NODEID"
        getncm $NODEID
        fgrep $NODEID $CURLTEMP > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            POK=0
        fi
        echo
    done
}

verifyget() {
    echo "Verifying NCM"
    for node in "$N1" "$N2" "$N3" "$N4" "$N5"; do
        NODEID=`getnodeid "$node"`
        if fgrep $NODEID $GETTEMP > /dev/null 2>&1; then
            true
            GOK=1
        else
            echo "NCM GET $NODEID not found"
            GOK=0
        fi
    done
}

isupload() {
    echo "$UPLOAD_NODES" | fgrep $1 > /dev/null 2>&1
    return $?
}


verifydel() {
    DOK=1
    for n in $ALL_NODES; do
        getnmm $n
        if [ $? -eq 1 ]; then
            echo "verifydel: NMM MISSING TO DELETE $n"
        fi
        # skip file upload nodes in NCM
        isupload $n
        if [ $? -eq 0 ]; then
            true
        else
            getncm $n
            if [ $? -eq 1 ]; then
                echo "verifydel: NCM MISSING TO DELETE $n"
            fi
        fi

        echo "DELETE NMM $n"
        curl  --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*'  -X DELETE  -o $CURLTEMP "http://localhost:9007/nodes/$n"
        if [ $? -ne 0 ]; then
            DOK=0
        fi

        getnmm $n
        if [ $? -eq 0 ]; then
            echo "verifydel: NMM not deleted $n"
            DOK=0
        fi

        isupload $n
        if [ $? -eq 0 ]; then
            true
        else
            getncm $n
            if [ $? -eq 0 ]; then
                echo "verifydel: NCM not deleted $n"
                DOK=0
            fi
        fi
    done
}


verifybulk() {
    BOK=1
    for b in `awk -F: '/node_id/ {print $2}' $1 | sed 's/[", \t]//g'`; do
        getnmm $b
        if fgrep $b $CURLTEMP > /dev/null 2>&1; then
            true
        else
            echo "verifybulk: NMM not found $b"
            BOK=0
        fi
        # skip file upload nodes in NCM
        isupload $b
        if [ $? -eq 0 ]; then
            true
        else
            getncm $b
            if fgrep $b $CURLTEMP > /dev/null 2>&1; then
                true
            else
                echo "verifybulk: NCM not found $b"
                BOK=0
            fi
        fi
    done
    return $RET
}

check_create_ncm() {
    # check if an NCM with id test_ncm_001 exists,
    # if not register one with http://localhost:9014 as URI
    curl --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*' -X GET -o $CURLTEMP ${DEF_NMMURI}/ncms/${DEF_NCMID}
    fgrep ' does not exist' $CURLTEMP > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        cat << NCM_EOF > $PAYTEMP
            {
                "ncm_info": {
                    "cap": $DEF_NCMCAP,
                    "id": "${DEF_NCMID}",
                    "uri": "${DEF_NCMURI}"
                }
        }
NCM_EOF
        curl  --no-progress-meter --header 'Content-Type: application/json' -X POST -o $CURLTEMP -d@${PAYTEMP} "${DEF_NMMURI}/ncms"
        if [ -s $CURLTEMP ]; then
            echo "Could not register NCM"
            cat $CURLTEMP
        fi
        echo "NCM ${DEF_NCMID} created"
    fi
}

create_without_ncm()
{
    NODEID=`getnodeid "$N6"`
    echo "NMM POST NONCM $NODEID"
        PAY='{
        "host_info" : '
        PAY="$PAY $N6
    }"
    echo "$PAY" > $PAYTEMP
    curl  --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*' -o $CURLTEMP -d@${PAYTEMP} -X POST http://localhost:9007/nodes
    if [ $? -ne 0 ]; then
        echo "FAILED: NONCM POST"
        return 1
    fi
    echo "NMM GET $NODEID"
    getnmm $NODEID
    fgrep $NODEID $CURLTEMP > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "FAILED: NONCM GET"
        return 1
    else
        curl  --no-progress-meter --header 'Content-Type: application/json' -X DELETE   -o $CURLTEMP http://localhost:9007/nodes/$NODEID
    fi
    echo "PASSED: NONCM POST"
}

# Change N5 ip and check (N5N)
verifyupd() {
    PAY='{
        "host_info" : '
        PAY="$PAY $N5N
    }"
    echo "NMM PUT $N5ID"
    echo "$PAY" > $PAYTEMP
    curl  --no-progress-meter --header 'Content-Type: application/json' --header 'Accept: */*' -X PUT -o $CURLTEMP -d@${PAYTEMP} http://localhost:9007/nodes/$N5ID
    RET=$?
    echo
    if [ $RET -ne 0 ]; then
        return 1
    fi
    echo "NMM GET $N5ID"
    getnmm $N5ID
    if [ $? -ne 0 ]; then
        return 1
    fi

    fgrep "$N5NIP" $CURLTEMP > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "FAILED: UPDATE NMM $N5ID IP"
        cat $CURLTEMP
        return 1
    fi

    echo "NCM GET $N5ID"
    getncm $N5ID
    if [ $? -ne 0 ]; then
        return 1
    fi
    fgrep $N5ID $CURLTEMP > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        return 1
    fi
    fgrep "$N5NIP" $CURLTEMP > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "FAILED: UPDATE NCM $N5ID IP"
        return 1
    fi
    return 0
}

# main

setnodeids

check_create_ncm

echo "CLEAR caches"
for x in $ALL_NODES; do
    curl  --no-progress-meter --header 'Content-Type: application/json' -X DELETE   -o $CURLTEMP http://localhost:9007/nodes/$x
    if fgrep "The node to update or delete is not existing" $CURLTEMP > /dev/null 2>&1; then
        true
    else
        echo "CLEAR: $x not cleared in NMM"
    fi

    curl  --no-progress-meter --header 'Content-Type: application/json' -X DELETE  -o $CURLTEMP http://localhost:9014/nodes/$x
    if [ ! -s $CURLTEMP ]; then
        true
    else
        echo "CLEAR: $x not cleared in NCM"
    fi
done


echo "Querying empty caches"
for x in $ALL_NODES; do
    getnmm $x
    if [ $? -ne 1 ]; then
        echo "CLEAR: did not clear $x in NMM"
    fi

    getncm $x
    if [ $? -ne 1 ]; then
        echo "CLEAR: did not clear $x in NCM"
    fi
done

NULLS=`fgrep -c '{"host_info":null}' $CURLTEMP 2> /dev/null`
if [ -z "$NULLS" ]; then
    true
elif [ $NULLS -eq 10 ]; then
    true
else
    echo "NON EPMTY: NMM / NCM cache"
fi

echo
echo "POST to NMM"
dopost
if [ $POK -eq 0 ]; then
    echo "FAILED: POST"
fi
doget

echo
echo "Verifying POST"
verifyget
if [ $POK -eq 1 -a $GOK -eq 1 ]; then
    echo "PASSED: POST"
else
    echo "FAILED: POST"
fi

echo

echo "Bulk POST"
echo "$BULK" > $PAYTEMP
curl  --no-progress-meter --header 'Content-Type: application/json' -X POST -o $CURLTEMP -d@${PAYTEMP} http://localhost:9007/nodes/bulk
echo "Verify bulk POST"
verifybulk $PAYTEMP

if [ $BOK -eq 1 ]; then
    echo "PASSED: BULK"
else
    echo "FAILED: BULK"
fi

echo
echo "Verify file upload"
curl  --no-progress-meter -X POST -o $CURLTEMP -H 'Content-Type: multipart/form-data' -H 'application-type:REST' --form file=@../json/nodes-upload.json http://localhost:9007/nodes/upload
if [ $? -ne 0 ]; then
    FOK=0
fi

verifybulk ../json/nodes-upload.json
if [ $BOK -eq 1 ]; then
    echo "PASSED: UPLOAD"
else
    echo "FAILED: UPLOAD"
fi

# verify update
verifyupd
if [ $? -ne 0 ]; then
    echo "FAILED: UPDATE"
else
    echo "PASSED: UPDATE"
fi

#verify node creation without NCMID
create_without_ncm

echo "DELETE all"
verifydel
if [ $DOK -eq 1 ]; then
    echo "PASSED: DELETE"
else
    echo "FAILED: DELETE"
fi
exit 0
