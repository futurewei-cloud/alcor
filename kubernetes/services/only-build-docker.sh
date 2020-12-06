cd ../../services
  for d in *;
  do
      echo "Build service -  $d"
      cd $d
      docker build -t $d:v1.0 .
      cd ..
      echo "Build service -  $d completed"
  done