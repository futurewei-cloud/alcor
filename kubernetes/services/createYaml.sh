#!/bin/bash

# Install prerequisites
if [[ "$OSTYPE" == "linux-gnu" ]]; then

  echo "Create yaml files"

  for d in *;
  do
      kubectl create -f $d
      echo "Create yaml -  $d completed"
  done
  
  echo "Create yaml files done"
  
  sleep 20s
  
  kubectl exec -it ignite-alcor-0 -n ignite-alcor -c ignite-alcor-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  
  sleep 10s
  
  kubectl exec -it ignite-alcor-ip-0 -n ignite-alcor-ip -c ignite-alcor-ip-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  
  sleep 10s
  
  kubectl exec -it ignite-alcor-mac-0 -n ignite-alcor-mac -c ignite-alcor-mac-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  
  sleep 10s
  
  kubectl exec -it ignite-alcor-port-0 -n ignite-alcor-port -c ignite-alcor-port-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  #cd apache-ignite/bin/
  #./control.sh --activate
  
  echo "ignite cluster has been activated"

elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Install prerequisites in Mac OSX"
  /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
  brew install maven

#  echo "Clean build legacy controller project"
#  mvn clean
#  mvn compile
#  mvn install -DskipTests

  echo "Clean build common library project"
  cd lib
  mvn clean
  mvn compile
  mvn install -DskipTests
  cd ..

  echo "Clean build web project"
  cd web
  mvn clean
  mvn compile
  mvn install -DskipTests
  cd ..

  echo "Build service one by one under services directory"
  cd services
  for d in ./*/;
  do
      echo "Build service -  $d"
      cd $d
      mvn clean
      mvn compile
      mvn install -DskipTests
      cd ..
      echo "Build service -  $d completed"
  done

elif [[ "$OSTYPE" == "cygwin" ]]; then
  # POSIX compatibility layer and Linux environment emulation for Windows
  echo "Install prerequisites in Linux environment of Windows"
elif [[ "$OSTYPE" == "msys" ]]; then
  # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
  echo "Install prerequisites in MinGW of Windows"
else
  echo "Unknown supported OS"
fi

echo "Build completed"
