d="$(docker ps -a |grep ignite|wc -l)"
if [ $d -eq 0 ]; then
  docker run -p 10800:10800 --name ignite -d apacheignite/ignite:2.8.0
fi
d="$(docker ps |grep ignite|wc -l)"
if [ $d -ne 0 ]; then
  mvn -B package --file pom.xml
fi
