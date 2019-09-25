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

Write-Host "Build completed" -ForegroundColor green