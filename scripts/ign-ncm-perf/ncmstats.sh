#! /bin/sh

if [ -z "$1" ]; then
    echo "Need NCM one or more logfiles with timings"
    exit 1
elif [ ! -r $1 -o ! -s $1 ]; then
    echo "$1 is not readable or empty"
    exit 1
fi

# trap "rm -f ncm-stats-temp-*.out ncm-stats.m > /dev/null 2>&1" 0 1 2 3 4 5 6 7 8 10 11 12 13 14 15

H1="PGSS_FIN_PUT_GS"
H2="GRM_PIP"
H3="GRM_VNI"
H4="UGS_FIN_GRM"
H5="UGS_ADD_NEW"
H6="UGS_UPD_OLD"
H7="UGS_FIN_GS_PROC"
H8="UGS_FIN_VPC_POP"
H9="UGS_TOT_ET"
H10="PVRC_ARMC"
H11="PVRC_LOOP_RS"
H12="PVRC_END"
H13="GET_VNI"


call_octave() {
    HDR=$1
    FILE=$2
    PORTS=$3
    OCTOUT=ncm-stats-temp-${HDR}.out
    OCTSCRIPT=ncm-stats-temp-${HDR}.m
    if [ "${HDR}" = "GET_VNI" ]; then
        fgrep "$HDR" $FILE | awk '{if ($2 != 999) print}' | sort -k3 -n | awk '{print $NF}' > ${OCTOUT} 2> /dev/null
    else
        fgrep "$HDR" $FILE | sort -k3 -n | awk '{print $NF}' > ${OCTOUT} 2> /dev/null
    fi

    cat <<END_TAG_01 > ${OCTSCRIPT}
v=importdata("${OCTOUT}")
avg=mean(v)
med=median(v)
dev=std(v)
low=min(v)
high=max(v)
p25=prctile(v, 0.25)
p75=prctile(v, 0.75)
p95=prctile(v, 0.95)
p99=prctile(v, .99)
omode=mode(v)
ovar=var(v)
printf("STATS %-20s %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f % 12d %12.4f % 12d\n", "$HDR", low, high, avg, med, dev, p25, p75, p95, p99, omode, ovar, $PORTS)
END_TAG_01

  octave -q ${OCTSCRIPT} 2> /dev/null | sed -n 's/^STATS //p'
}


while [ -n "$1" ]; do
    INF=$1
    shift
    OUT=ncm-stats-temp-summary-${INF}.out
    ncm-log-analyzer.sh $INF > $OUT

    STATS="`basename $INF .log`".stats
    # NPORTS="`echo $INF | sed 's/.*-\([0-9][0-9]*p\)-.*/\1/' | sed 's/p//'`"
    NPORTS="`echo $INF | sed -e 's/^ncm-2021-[0-9][0-9]-[0-9][0-9]-//' -e 's/\([a-zA-Z-]*\)-*//' -e 's/\([0-9][0-9][0-9]*\).*/\1/'`"
    IGNOPT="`echo $INF | sed -e 's/^ncm-2021-[0-9][0-9]-[0-9][0-9]-//' -e 's/-[0-9][0-9][0-9][0-9]*p.*//' -e 's/-/ /' | tr '[a-z]' '[A-Z]'`"
    IGNOPT="$IGNOPT PVRCO"
    cat <<END_TAG_02 > $STATS
Summary statistics of NCM times from $INF
Number of Ports      : $NPORTS
Optimizations        : $IGNOPT

Time Tag                   MIN          MAX          AVG          MED          STDDEV       P25          P75          P95          P99             MOD       VAR

END_TAG_02

    for h in $H1 $H2 $H3 $H4 $H5 $H6 $H7 $H8 $H9 $H10 $H11 $H12 $H13 $H14; do
        call_octave $h ${OUT} ${NPORTS}
    done >> ${STATS}
    cat <<END_TAG_03 >> $STATS


Tag description
PGSS_FIN_PUT_GS : Time to update GS by host in a loop (time spent in updateGoalstate method).
                  [logline] pushGoalStatesStream : finished putting GS into cache.

GRM_VNI         : Time to retrieve ResourceMeta from VpcResourceMeta cache for the VNI.
                  [logline] getResourceMeta(vni) elapsed time.

UGS_FIN_GRM     : Time to retrieve ResourceMeta from hostResourceMetadataCache for a given hostid (inside updateGoalState method).
                  [logline] finished getting resource meta from cache.

UGS_ADD_NEW     : Time to put a new ResourceMeta into hostResourceMetadataCache (inside updateGoalState method).
                  [logline] existing is null, finished adding resource meta from cache.

UGS_UPD_OLD     : Time to put an updated ResourceMeta into hostResourceMetadataCache.
                  [logilne] existing is NOT null, finished adding resource meta from cache.

UGS_FIN_GS_PROC : Time to populate ResourceState cache for vpc, subnets, ports etc., (inside updateGoalState).
                  [logline] finished processing goalState.

UGS_TOT_ET      : Total time spent in UGS_FIN_GRM, UGS_ADD_NEW or UGS_UPD_OLD and UGS_FIN_GS_PROC.
                  [logline] total time.

UGS_FIN_VPC_POP : finished populating vpc resource cache.

PGSS_FIB_TO_DPM : Time to send GS to ACA and DPM (non ondemand)
                  [logline] pushGoalStatesStream : Replied to DPM, from received to replied

REQUEST_GS      : Time to to retrieve goalstate to sending GS to host. (on demand, includes time from onDemandService.retrieveGoalState)
                  [logline] From retrieving goalstate to sent goalstate


Description of Optimizations:
NEAR		: Ignite Near cache maintained in the Microservice address space.
HEAP            : On JVM heap, subject to GC but no serialization and deserialization.
PVRCO		: NCM - Get the VNI only once.
=========================================================================================================================================



END_TAG_03
    cat ${STATS}
    # rm -f ${STATS} > /dev/null 2>&1
done
