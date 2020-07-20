#cleanup before run
docker stop  $(docker ps |grep -v rally|awk '{print $1}'|grep -v CON)
docker rm -f $(docker ps |grep -v rally|awk '{print $1}'|grep -v CON)
#run container
sleep 2
docker run --rm --name=ignite -tid -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v /tmp:/tmp ignite-11 sh
docker run --rm --net=host --name vpm -p 9001:9001 -v /tmp:/tmp -itd vpm
docker run --rm --net=host --name snm -p 9002:9002 -v /tmp:/tmp -itd snm
docker run --rm --net=host --name rm -p 9003:9003 -v /tmp:/tmp -itd rm
docker run --rm --net=host --name pim -p 9004:9004 -v /tmp:/tmp -itd pim
docker run --rm --net=host --name mm -p 9005:9005 -v /tmp:/tmp -itd mm
docker run --rm --net=host --name pm -p 9006:9006 -v /tmp:/tmp -itd pm
docker run --rm --net=host --name nm -p 9007:9007 -v /tmp:/tmp -itd nm
docker run --rm --net=host --name sgm -p 9008:9008 -v /tmp:/tmp -itd sgm
docker run --rm --net=host --name ag -p 9009:9009  -v /tmp:/tmp -itd ag
docker run --rm --net=host --name dpm -p 9010:9010 -v /tmp:/tmp -itd dpm
echo 'good now all service is up'
