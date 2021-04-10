# MIT License
# Copyright(c) 2020 Futurewei Cloud
#     Permission is hereby granted,
#     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#     The above copyright notice and this permission notice shall be included in all copies
#     or
#     substantial portions of the Software.
#     THE SOFTWARE IS PROVIDED "AS IS",
#     WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
#     DAMAGES OR OTHER
#     LIABILITY,
#     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#     SOFTWARE.

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