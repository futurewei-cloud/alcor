#! /bin/sh

if [ -z "$1" ]; then
    echo "Need ncmstats.sh aggregate output file(s)"
    exit 1
fi

i=1
PLOT=plot
NFILES=$#
while [ -n "$1" ]; do
	INF=$1
	shift
	BASE="`echo $INF | sed 's/\.[a-zA-Z0-9][a-zA-Z0-9].*$//'`"
	PORTS="`awk -F: '/^Number of Ports/ {print $2}' $INF`"

	for h in PGSS_FIN_PUT_GS GRM_PIP GRM_VNI UGS_FIN_GRM UGS_ADD_NEW UGS_UPD_OLD UGS_FIN_GS_PROC UGS_FIN_VPC_POP UGS_TOT_ET PVRC_ARMC PVRC_LOOP_RS PVRC_END GET_VNI; do
		LC=`awk "/${h}/ {print NF}" $INF | head -1`
		echo "BASE $BASE PORTS $PORTS LC $LC"
		if  [ -z "$LC" ]; then
			echo "No times for ${h}..."
			continue
		elif [ $LC -eq 11 -o $LC -eq 13 ]; then
			echo "Found TAG: $h"
			true
		fi

		echo "Creating plot script for ${H} in $INF"
		GPS="gps-${BASE}-${h}.gps"
		TITLE="`echo ${BASE}-${h} | sed 's/_/-/'g`"

		cat <<-EOF > $GPS
set terminal svg size 900,600 dynamic mouse standalone enhanced linewidth 1.2
set title "${TITLE}"
set grid
show grid
set log y
set xlabel "Number of Ports in the Goal State"
set ylabel "Time in milliseconds"
EOF

if [ $LC -eq 11 ]; then
cat <<-EOF >> %GPS
plot '< sed -n -e "3d" -e "s/^${h}[\t ][\t ]*//p" $INF' \
       using 13:2 with lp title "MIN", \
    '' using 13:3 with lp title "MAX", \
    '' using 13:4 with lp title "AVG", \
    '' using 13:5 with lp title "MED", \
    '' using 13:6 with lp title "STDDEV", \
    '' using 13:7 with lp title "P25", \
    '' using 13:8 with lp title "P75", \
    '' using 13:9 with lp title "P95", \
    '' using 13:10 with lp title "P99"
EOF
elif [ $LC -eq 13 ]; then
cat <<-EOF >> %GPS
plot '< sed -n -e "3d" -e "s/^${h}[\t ][\t ]*//p" $INF' \
       using 13:2 with lp title "MIN", \
    '' using 13:3 with lp title "MAX", \
    '' using 13:4 with lp title "AVG", \
    '' using 13:5 with lp title "MED", \
    '' using 13:6 with lp title "STDDEV", \
    '' using 13:7 with lp title "P25", \
    '' using 13:8 with lp title "P75", \
    '' using 13:9 with lp title "P95", \
    '' using 13:10 with lp title "P99", \
    '' using 13:11 with lp title "MOD", \
    '' using 13:12 with lp title "VAR"
EOF
fi
	echo "Running plot for ${h} in $INF"
	gnuplot $GPS > ${BASE}-${h}.svg
	echo "Generated plot ${BASE}-${h}.svg for ${h} in $INF"
	done
done
