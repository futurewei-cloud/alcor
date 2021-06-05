/*
 *
 * MIT License
 * Copyright(c) 2020 Futurewei Cloud
 *
 *     Permission is hereby granted,
 *     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
 *     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
 *     to whom the Software is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package com.futurewei.alcor.web.entity.dataplane;

public class VpcUnicastGoalStateByte {
    private String nextTopic;
    private String nextSubTopic;
    private byte[] goalStateByte;

    public VpcUnicastGoalStateByte() {
    }

    public VpcUnicastGoalStateByte(String nextTopic, String nextSubTopic, byte[] goalStateByte) {
        this.nextTopic = nextTopic;
        this.nextSubTopic = nextSubTopic;
        this.goalStateByte = goalStateByte;
    }

    public String getNextTopic() {
        return nextTopic;
    }

    public void setNextTopic(String nextTopic) {
        this.nextTopic = nextTopic;
    }

    public String getNextSubTopic() {
        return nextSubTopic;
    }

    public void setNextSubTopic(String nextSubTopic) {
        this.nextSubTopic = nextSubTopic;
    }

    public byte[] getGoalStateByte() {
        return goalStateByte;
    }

    public void setGoalStateByte(byte[] goalStateByte) {
        this.goalStateByte = goalStateByte;
    }
}
