#cleanup before run
#docker stop  $(docker ps |grep -v rally|awk '{print $1}'|grep -v CON)
#docker rm -f $(docker ps |grep -v rally|awk '{print $1}'|grep -v CON)
#run container
docker run --name=ignite -p 10800:10800 -p 10801:10801 -p 47100:47100 -p 47500:47500 -v /tmp:/tmp -tid ignite-11 sh
docker run --net=host --name vpm -p 9001:9001 -v /tmp:/tmp -itd vpm
docker run --net=host --name snm -p 9002:9002 -v /tmp:/tmp -itd snm
docker run --net=host --name rm  -p 9003:9003 -v /tmp:/tmp -itd rm
docker run --net=host --name pim -p 9004:9004 -v /tmp:/tmp -itd pim
docker run --net=host --name mm  -p 9005:9005 -v /tmp:/tmp -itd mm
docker run --net=host --name pm  -p 9006:9006 -v /tmp:/tmp -itd pm
docker run --net=host --name nm  -p 9007:9007 -v /tmp:/tmp -itd nm
docker run --net=host --name sgm -p 9008:9008 -v /tmp:/tmp -itd sgm
docker run --net=host --name ag  -p 9009:9009 -v /tmp:/tmp -itd ag
docker run --net=host --name dpm -p 9010:9010 -v /tmp:/tmp -itd dpm
docker run --net=host --name eim -p 9011:9011 -v /tmp:/tmp -itd eim
docker run --net=host --name qm  -p 9012:9012 -v /tmp:/tmp -itd qm
docker run --net=host --name nam -p 9013:9013 -v /tmp:/tmp -itd nam
docker run --net=host --name gm  -p 9015:9015 -v /tmp:/tmp -itd gm
echo 'good now all service is up'
