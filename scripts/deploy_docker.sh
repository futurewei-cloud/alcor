sh ignite.sh
sleep 30
export IGNITE_HOME=/root/git/apache-ignite-2.8.1-bin/
docker stop -f $(docker ps |grep -v rally|awk '{print $1}'|grep -v CON)

docker run --rm --net=host --name mm -p 9005:9005 -v /tmp:/tmp -itd mm
sleep 2
docker run --rm --net=host --name pim -p 9004:9004 -v /tmp:/tmp -itd pim
sleep 2
docker run --rm --net=host --name rm -p 9003:9003 -v /tmp:/tmp -itd rm
sleep 2
docker run --rm --net=host --name nm -p 9007:9007 -v /tmp:/tmp -itd nm
sleep 2
docker run --rm --net=host --name sgm -p 9008:9008 -v /tmp:/tmp -itd sgm
sleep 2
docker run --rm --net=host --name sm -p 9002:9002 -v /tmp:/tmp -itd sm
sleep 2
docker run --rm --net=host --name vm -p 9001:9001 -v /tmp:/tmp -itd vm
sleep 2
docker run --rm --net=host --name dpm -p 9009:9009 -v /tmp:/tmp -itd dpm
sleep 2
sleep 10
docker run --rm --net=host --name pm -p 9006:9006 -v /tmp:/tmp -itd pm
sleep 2
docker run --rm --net=host --name ag -p 8080:8080  -v /tmp:/tmp -itd ag
sleep 2
echo 'good now all service is up'
