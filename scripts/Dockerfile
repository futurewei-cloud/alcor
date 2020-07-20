FROM ubuntu

MAINTAINER xzhang2 <xzhang2@futurewei.com>

EXPOSE 10800
EXPOSE 10081
EXPOSE 47100
EXPOSE 47500

RUN apt-get update && apt-get install -y \
    wget openjdk-11-jdk unzip \ 
    && mkdir /code \
&& cd /code/ \
&& wget https://downloads.apache.org//ignite/2.8.1/apache-ignite-2.8.1-bin.zip \
  &&    unzip -d . apache-ignite-2.8.1-bin.zip \
&& cd apache-ignite-2.8.1-bin/bin \
&& echo '<?xml version="1.0" encoding="UTF-8"?><beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd"><bean class="org.apache.ignite.configuration.IgniteConfiguration"> <property name="peerClassLoadingEnabled" value="true"/> </bean></beans>' > config.xml 
  ENTRYPOINT  /code/apache-ignite-2.8.1-bin/bin/ignite.sh /code/apache-ignite-2.8.1-bin/bin/config.xml 
