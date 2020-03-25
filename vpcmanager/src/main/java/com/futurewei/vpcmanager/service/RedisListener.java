/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.vpcmanager.service;

import com.futurewei.vpcmanager.comm.logging.Logger;
import com.futurewei.vpcmanager.comm.logging.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Service
public class RedisListener implements MessageListener {

    public static List<String> messageList = new ArrayList<String>();

    public RedisListener() {

    }

    public void onMessage(final Message message, final byte[] pattern) {
        messageList.add(message.toString());
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, ("Message received: " + new String(message.getBody())));
    }
}