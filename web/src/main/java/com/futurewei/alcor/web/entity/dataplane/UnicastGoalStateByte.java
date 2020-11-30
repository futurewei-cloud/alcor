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
package com.futurewei.alcor.web.entity.dataplane;

public class UnicastGoalStateByte {
    private String nextTopic;
    private byte[] goalStateByte;

    public UnicastGoalStateByte() {

    }

    public UnicastGoalStateByte(String nextTopic, byte[] goalStateByte) {
        this.nextTopic = nextTopic;
        this.goalStateByte = goalStateByte;
    }

    public String getNextTopic() {
        return nextTopic;
    }

    public void setNextTopic(String nextTopic) {
        this.nextTopic = nextTopic;
    }

    public byte[] getGoalStateByte() {
        return goalStateByte;
    }

    public void setGoalStateByte(byte[] goalStateByte) {
        this.goalStateByte = goalStateByte;
    }
}
