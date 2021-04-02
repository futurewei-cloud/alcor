#! /bin/sh

PAYTEMP="payload-tmp-$$.json"
GETTEMP="gettemp-tmp-$$.out"
CURLTEMP="curl-out-tmp.$$.out"
NODEIDS=
ALLNODES=

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
         "veth": "eth0"
      },
      {
         "local_ip": "20.213.43.11",
         "node_id": "f209465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C2:A0:C9:34:C8:29",
         "node_name": "blk909031",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "20.213.43.13",
         "node_id": "f309465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C3:A0:C9:34:C8:29",
         "node_name": "blk909032",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "20.213.43.14",
         "node_id": "f409465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C4:A0:C9:34:C8:29",
         "node_name": "blk909033",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "20.213.43.15",
         "node_id": "f509465c-5ef6-400e-806d-fbeff12f94a9",
         "mac_address": "C5:A0:C9:34:C8:29",
         "node_name": "blk909034",
         "server_port": 50001,
         "veth": "eth0"
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
         "veth": "eth0"
}'

N2='
{
         "local_ip": "10.213.43.11",
         "node_id": "d209465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A2:A0:C9:34:C8:29",
         "node_name": "ncm009031",
         "server_port": 50001,
         "veth": "eth0"
}'

N3='
{
         "local_ip": "10.213.43.13",
         "node_id": "d309465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A3:A0:C9:34:C8:29",
         "node_name": "ncm009032",
         "server_port": 50001,
         "veth": "eth0"
}'

N4='
{
         "local_ip": "10.213.43.14",
         "node_id": "d409465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A4:A0:C9:34:C8:29",
         "node_name": "ncm009033",
         "server_port": 50001,
         "veth": "eth0"
}'

N5='
{
         "local_ip": "10.213.43.15",
         "node_id": "d509465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "A5:A0:C9:34:C8:29",
         "node_name": "ncm009034",
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
    curl -X GET --no-progress-meter -o $CURLTEMP --header "Content-Type: application/json" --header "Accept: */*" http://localhost:9007/nodes/$1
    fgrep $1 $CURLTEMP > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        RET=0
    else
        RET=1
    fi

    return $RET
}

getncm() {
    RET=1
    curl -X GET --no-progress-meter -o $CURLTEMP --header "Content-Type: application/json" --header "Accept: */*" http://localhost:9014/nodes/$1
    fgrep $1 $CURLTEMP > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        RET=0
    else
        RET=1
    fi

    return $RET
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
        curl -X POST --no-progress-meter -o $CURLTEMP --header "Content-Type: application/json" --header "Accept: */*" -d@${PAYTEMP} http://localhost:9007/nodes
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

verifydel() {
    DOK=1
    getncm $1
    if fgrep $1 $CURLTEMP > /dev/null 2>&1; then
        true
    else
        echo "verifydel: NCM not found $1"
    fi

    curl -X DELETE  -o $CURLTEMP --no-progress-meter http://localhost:9007/nodes/$1
    if [ $? -ne 0 ]; then
        DOK=0
    fi

    getncm $1
    if fgrep $1 $CURLTEMP > /dev/null 2>&1; then
        echo "verifydel: NCM not deleted $1"
        DOK=0
    fi
}


verifybulk() {
    BOK=1
    for b in `awk -F: '/node_id/ {print $2}' $1 | sed 's/[", \t]//g'`; do
        getncm $b
        if fgrep $b $CURLTEMP > /dev/null 2>&1; then
            true
        else
            echo "verifybulk: NCM not found $b"
            BOK=0
        fi
    done
    return $RET
}

# main

setnodeids

echo "CLEAR caches"
for x in $ALL_NODES; do
    curl -X DELETE  --no-progress-meter  -o $CURLTEMP --header "Content-Type: application/json" --header "Accept: */*" http://localhost:9007/nodes/$x
    if fgrep "The node to update or delete is not existing" $CURLTEMP > /dev/null 2>&1; then
        true
    else
        echo "CLEAR: $x not cleared in NMM"
    fi

    curl -X DELETE  --no-progress-meter -o $CURLTEMP --header "Content-Type: application/json" --header "Accept: */*" http://localhost:9014/nodes/$x
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

NULLS=`fgrep -c '{"host_info":null}' $GETTEMP 2> /dev/null`
if [ $NULLS -ne 10 ]; then
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
echo "DELETE all"
for node in $NODEIDS; do
    verifydel $node
    echo "DELETED $node"
done

echo
echo "Verify DELETE"
doget
verifyget
if [ $DOK -eq 1 -a $GOK -eq 0 ]; then
    echo "PASSED: DELETE"
else
    echo "FAILED: DELETE"
fi
echo

echo "Bulk POST"
echo "$BULK" > $PAYTEMP
curl -X POST --no-progress-meter -o $CURLTEMP --header "Content-Type: application/json" --header "Accept: */*" -d@${PAYTEMP} http://localhost:9007/nodes/bulk
echo "Verify bulk POST"
verifybulk $PAYTEMP

if [ $BOK -eq 1 ]; then
    echo "PASSED: BULK"
else
    echo "FAILED: BULK"
fi

echo
echo "Verify file upload"
curl -X POST --no-progress-meter -o $CURLTEMP -H 'Content-Type: multipart/form-data' -H 'application-type:REST' --form file=@../json/nodes-upload.json http://localhost:9007/nodes/upload
if [ $? -ne 0 ]; then
    FOK=0
fi

verifybulk ../json/nodes-upload.json
if [ $BOK -eq 1 ]; then
    echo "PASSED: UPLOAD"
else
    echo "FAILED: UPLOAD"
fi

exit 0
