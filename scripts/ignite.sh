kill -9 `lsof -i:10800|awk '{print $2}'|grep -v PID`
cd /root/git/apache-ignite-2.8.1-bin/bin/
export IGNITE_HOME=/root/git/apache-ignite-2.8.1-bin
nohup ./ignite.sh ../examples/config/example-ignite.xml 2>&1 &
~                                                               
