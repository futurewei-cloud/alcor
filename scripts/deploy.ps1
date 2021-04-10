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

Param (
   [Parameter(Mandatory = $false)]
   [string] $CtrlContainer = "alcor-controller",

   [Parameter(Mandatory = $false)]
   [string] $RedisContainer = "alcor-redis"
)

Write-Host "Check and Install prerequisites"
if (Get-Command "docker" -ErrorAction SilentlyContinue)
{
    $dockerVer = docker --version
    Write-Host "Installed Docker version is $dockerVer" -ForegroundColor green
}
else
{
    Write-Host "Install Docker Desktop and retry this build script. Link: https://docs.docker.com/docker-for-windows/install/" -ForegroundColor red
    exit
}

Write-Host "Verify no conflict with running containers"
$runningContainerID = $(docker ps -qf "name=$RedisContainer")
if($runningContainerID)
{
    Write-Host "Redis container $RedisContainer is already running with id $runningContainerID. Build exits..." -ForegroundColor red
    exit
}
else{
    $containerID = $(docker ps -aq -f status=exited -f name=$RedisContainer)
    if($containerID)
    {
        Write-Host "Clean up non-running redis container with id $containerID"
        docker rm $containerID
    }
}

$runningContainerID = $(docker ps -qf "name=$CtrlContainer")
if($runningContainerID)
{
    Write-Host "Controller container $CtrlContainer is already running with id $runningContainerID. Build exits..." -ForegroundColor red
    exit
}
else{
    $containerID = $(docker ps -aq -f status=exited -f name=$CtrlContainer)
    if($containerID)
    {
        Write-Host "Clean up non-running controller container with id $containerID"
        docker rm $containerID
    }
}

Write-Host "Deploy a redis container"
docker run -p 6379:6379 --name $RedisContainer -d redis

Write-Host "Build a controller image and deploy as docker container"
docker build -t alcor/controller .
docker run -p 8080:8080 --name $CtrlContainer -d alcor/controller 

Write-Host "Deployment completed" -ForegroundColor green