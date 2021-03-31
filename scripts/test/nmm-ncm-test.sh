#! /bin/sh

PAYTEMP="payload-tmp-$$.json"
trap "rm -f $PAYTEMP > /dev/null 2>&1" 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 

N1='
{
         "local_ip": "10.213.43.10",
         "node_id": "d109465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "11:A0:C9:34:C8:29",
         "node_name": "ncm009030",
         "server_port": 50001,
         "veth": "eth0"
}'

N2='
{
         "local_ip": "10.213.43.11",
         "node_id": "d209465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "12:A0:C9:34:C8:29",
         "node_name": "ncm009031",
         "server_port": 50001,
         "veth": "eth0"
}'

N3='
{
         "local_ip": "10.213.43.13",
         "node_id": "d309465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "13:A0:C9:34:C8:29",
         "node_name": "ncm009032",
         "server_port": 50001,
         "veth": "eth0"
}'

N4='
{
         "local_ip": "10.213.43.14",
         "node_id": "d409465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "14:A0:C9:34:C8:29",
         "node_name": "ncm009033",
         "server_port": 50001,
         "veth": "eth0"
}'

N5='
{
         "local_ip": "10.213.43.15",
         "node_id": "d509465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "15:A0:C9:34:C8:29",
         "node_name": "ncm009034",
         "server_port": 50001,
         "veth": "eth0"
}'

BASIC_OPTS="--no-progress-meter"

getnodeid() {
    NODEID=`echo "$1" | grep node_id | sed -e 's/[,"]//g' -e 's/node_id://' -e 's/[\t ][\t ]*//g'`
    echo $NODEID
}

getnmm() {
    curl -X GET --no-progress-meter --header "Content-Type: application/json" --header "Accept: */*" http://localhost:9007/nodes/$1
}

getncm() {
    curl -X GET --no-progress-meter --header "Content-Type: application/json" --header "Accept: */*" http://localhost:9014/nodes/$1
}

doget() {
    for node in "$N1" "$N2" "$N3" "$N4" "$N5"; do
        PAY='{
        "host_info" : '
        PAY="$PAY $node
    }"
        NODEID=`getnodeid "$PAY"`
        echo "GET $NODEID from empty NMM"
        getnmm $NODEID
        echo
        echo "GET $NODEID from empty NCM"
        getncm $NODEID
        echo
        echo
    done
}

dopost() {
    for node in "$N1" "$N2" "$N3" "$N4" "$N5"; do
        PAY='{
        "host_info" : '
        PAY="$PAY $node
    }"
        NODEID=`getnodeid "$PAY"`
        echo "POST $NODEID to NMM"
        echo "$PAY" > $PAYTEMP
        curl -X POST --no-progress-meter --header "Content-Type: application/json" --header "Accept: */*" -d@${PAYTEMP} http://localhost:9007/nodes
        echo
        echo "GET $NODEID from NMM"
        getnmm $NODEID
        echo
        echo

        echo "GET NODEID from NCM"
        getncm $NODEID
        echo
        echo
    done
}

doget
dopost
exit 0


B='{
   "host_infos": [
      {
         "local_ip": "10.213.43.10",
         "node_id": "d109465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "11:A0:C9:34:C8:29",
         "node_name": "ncm009030",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "10.213.43.11",
         "node_id": "d209465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "12:A0:C9:34:C8:29",
         "node_name": "ncm009031",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "10.213.43.13",
         "node_id": "d309465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "13:A0:C9:34:C8:29",
         "node_name": "ncm009032",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "10.213.43.14",
         "node_id": "d409465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "14:A0:C9:34:C8:29",
         "node_name": "ncm009033",
         "server_port": 50001,
         "veth": "eth0"
      },
      {
         "local_ip": "10.213.43.15",
         "node_id": "d509465c-5ed6-400e-806d-fbefd12f94a9",
         "mac_address": "15:A0:C9:34:C8:29",
         "node_name": "ncm009034",
         "server_port": 50001,
         "veth": "eth0"
      }
   ]
}'
