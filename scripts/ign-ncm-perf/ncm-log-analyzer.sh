#! /bin/sh

if [ -z "$1" ]; then
    echo "Need NCM logfile with timings"
    exit 1
fi

INF=$1
gawk -F: '
BEGIN {
    i1=0
    i2=0
    i3=0
    i4=0
    i5=0
    i6=0
    i7=0
    i8=0
    i9=0
    i10=0
    i11=0
    i12=0
    i13=0
    i14=0
    i15=0
    
    H["finished putting GS into cache, elapsed time in milliseconds"] = "PGSS_FIN_PUT_GS"
    H["[getResourceMeta(privateIP)] GRM"]="GRM_PIP"
    H["[getResourceMeta(vni)] elapsed time"]="GRM_VNI"
    H["finished getting resource meta from cache"]="UGS_FIN_GRM"
    H["existing is null, finished adding resource meta from cache"]="UGS_ADD_NEW"
    H["existing is NOT null, finished adding resource meta from cache"]="UGS_UPD_OLD"
    H["finished processing goalState, elapsed time"]="UGS_FIN_GS_PROC"
    H["finished populating vpc resource cache"]="UGS_FIN_VPC_POP"
    H["total time, elapsed time in milliseconds"]="UGS_TOT_ET"
    H["looped throught the port states for vpc with vni"]="PVRCC_PORT_LOOP"
    H["added resource metadata for vpc with vni"]="PVRCC_ARM"
}

/pushGoalStatesStream : finished putting GS into cache, elapsed time in milliseconds/ {
    if ($NF > 0) {
	    # printf("%s %d\n", "PGSSC", $NF)
        T["PGSS_FIN_PUT_GS#", i1++]=$NF
    }
}
/[getResourceMeta(privateIP)] GRM: elapsed time:/ {
    if ($NF > 0) {
        # printf("%s %d\n", "GRM_PIP", $NF)
        T["GRM_PIP#", i2++]=$NF
    }
}
/[getResourceMeta(vni)] elapsed time:/ {
    if ($NF > 0) {
        # printf("%s %d\n", "GRM_VNI", $NF)
        T["GRM_VNI#", i3++]=$NF
    }
}
/finished getting resource meta from cache/ {
    if ($NF > 0) {
        # printf("%s %d\n", "UGS_GRMC", $NF)
        T["UGS_FIN_GRM#", i4++]=$NF
    }
}
/existing is null, finished adding resource meta from cache/ {
    if ($NNF > 0) {
        # printf("%s %d\n", "UGS_ADD_NEW", $NF)
        T["UGS_ADD_NEW#", i5++]=$NF
    }
}
/existing is NOT null, finished adding resource meta from cache/ {
    if ($NF > 0) {
        # printf("%s %d\n", "UGS_UPD_OLD", $NF)
        T["UGS_UPD_OLD#", i6++]=$NF
    }
}
/finished processing goalState, elapsed time/ {
    if ($NF > 0) {
        # printf("%s %d\n", "UGS_FIN_GS_PROC", $NF)
        T["UGS_FIN_GS_PROC#", i7++]=$NF
    }
}
/finished populating vpc resource cache/ {
    if ($NF > 0) {
        # printf("%s %d\n", "UGS_FIN_VPC_POP", $NF)
        T["UGS_FIN_VPC_POP#", i8++]=$NF
    }
}
/total time, elapsed time in milliseconds/ {
    if ($NF > 0) {
        # printf("%s %d\n", "UGS_TOT_ET", $NF)
        T["UGS_TOT_ET#", i9++]=$NF
    }
}
/looped throught the port states for vpc with vni/ {
    if ($NF > 0) {
        # printf("%s %d\n", "PVRC_LOOP_PORTS", $NF)
        T["PVRC_LOOP_PORTS#", i10++]=$NF
    }
}
/added resource metadata for vpc with vni/ {
    if ($NF > 0) {
        # printf("%s %d\n", "PVRC_ARMC", $NF)
        T["PVRC_ARMC#", i11++]=$NF
    }
}
/after loop, rs_count/ {
# 2021-07-01 18:23:34.229  INFO 29847 --- [ault-executor-0] global                                   : populateVpcResourceCache : after loop, rs_count = 0 elapsed time in milliseconds: 1625189014229
    if ($NF > 0 && $5 !~ /rs_count = 0/) {
        # printf("%s %d\n", "PVRC_LOOP_RS", $NF)
        T["PVRC_LOOP_RS#", i12++]=$NF
    }
}
/populateVpcResourceCache : end,/ {
    if ($NF > 0) {
        # printf("%s %d\n", "PVRC_END", $NF)
        T["PVRC_END#", i13++]=$NF
    }
}

/getVniInLoop/ {
    if ($5 !~ /done/ && $NF > 0) {
        T["GET_VNI#", i14++]=$NF
    }
}

END {
        print "# NCM times from ", FILENAME
        for (x in T) {
            split(T[x], t, "\034")
            di=index(x, "#")
            hdr=substr(x, 1, di - 1)
            sec=substr(x, di + 1)
            si=index(hdr, "\034")
            if (si != 0) {
                hdr = substr(hdr, 1, si - 1)
                sec = substr(sec, si + 1)
            }
            print hdr, sec, t[1]

    }
}' $INF | tr -d '\034' | sort -k1
