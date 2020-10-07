Write-Host "Check and Install prerequisites"
if (Get-Command "mvn" -ErrorAction SilentlyContinue)
{
    $mavenVer = mvn --version
    Write-Host "Installed Maven version is $mavenVer" -ForegroundColor green
}
else
{
    Write-Host "Install Apache Maven and retry this build script. Link: https://maven.apache.org/install.html" -ForegroundColor red
    exit
}

Write-Host "Clean build controller project"
mvn clean
mvn compile
mvn install

Write-Host "Clean build common library project"
cd lib
mvn clean
mvn compile
mvn install
cd ..

Write-Host "Clean build web project"
cd web
mvn clean
mvn compile
mvn install
cd ..

Write-Host "Build service one by one under services directory"
cd services
$service_subdirectories = Get-ChildItem -Directory -Force -ErrorAction SilentlyContinue
foreach($subdirectory in $service_subdirectories)
{
    Write-Host "Build service -  $($subdirectory.name)"
    cd $subdirectory.name
    mvn clean
    mvn compile
    mvn install
    cd ..
    Write-Host "Build service -  $($subdirectory.name) completed" -ForegroundColor green
}

cd ..
Write-Host "Build completed" -ForegroundColor green