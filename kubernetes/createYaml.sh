# MIT License
# Copyright(c) 2020 Futurewei Cloud
#
#     Permission is hereby granted,
#     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
#     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
#     to whom the Software is furnished to do so, subject to the following conditions:
#
#     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#    
#     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
#     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


#!/bin/bash

# Install prerequisites
if [[ "$OSTYPE" == "linux-gnu" ]]; then

  echo "Create ignite yaml files"

  for d in db/ignite/*.yaml;
  do
      kubectl create -f $d
      echo "Create ignite yaml -  $d completed"
  done
  
  echo "Create yaml files done"
  
  kubectl exec -it ignite-alcor-dpm-0 -n ignite-alcor-dpm -c ignite-alcor-dpm-node -- /opt/ignite/apache-ignite/bin/control.sh --activate

  kubectl exec -it ignite-alcor-ncm-0 -n ignite-alcor-ncm -c ignite-alcor-ncm-node -- /opt/ignite/apache-ignite/bin/control.sh --activate

  numberOfRnningPods="foo"
  numberOfAllPods="bar"

  while [ "$numberOfRnningPods" != "$numberOfAllPods" ];
  do
    sleep 10s
    numberOfRnningPods=$(kubectl get pods -A --field-selector=status.phase=Running | wc -l)
    numberOfAllPods=$(kubectl get pods -A | wc -l)
    echo "numberOfRnningPods: " "$numberOfRnningPods"
    echo "numberOfAllPods: " "$numberOfAllPods"
  done

  kubectl exec -it ignite-alcor-0 -n ignite-alcor -c ignite-alcor-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  kubectl exec -it ignite-alcor-ip-0 -n ignite-alcor-ip -c ignite-alcor-ip-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  kubectl exec -it ignite-alcor-mac-0 -n ignite-alcor-mac -c ignite-alcor-mac-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  kubectl exec -it ignite-alcor-port-0 -n ignite-alcor-port -c ignite-alcor-port-node -- /opt/ignite/apache-ignite/bin/control.sh --activate
  
  
  #cd apache-ignite/bin/
  #./control.sh --activate

  echo "ignite cluster has been activated"
  
  for d in services/*.yaml;
  do
      kubectl create -f $d
      echo "Create services yaml -  $d completed"
  done
  
  echo "Cluster has been created"

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
