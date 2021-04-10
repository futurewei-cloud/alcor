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

# Dockerfile for Ignite

FROM apacheignite/ignite:2.8.1

MAINTAINER Liguang Xie <lxie@futurewei.com>

EXPOSE 10800

# Generate container image and run container
COPY ./target/common-0.1.0-SNAPSHOT.jar /opt/ignite/apache-ignite/libs/common-0.1.0-SNAPSHOT.jar

#CMD /opt/ignite/apache-ignite/bin/ignite.sh

CMD $IGNITE_HOME/run.sh

# Container port exposure
EXPOSE 11211 47100 47500 49112 10800 8080

