/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.web.entity.dataplane;

import java.util.List;

public class MulticastGoalStateByte {
    private List<String> nextTopics;
    private List<String> nextSubTopics;
    private byte[] goalStateByte;

    public MulticastGoalStateByte() {

    }

    public MulticastGoalStateByte(List<String> nextTopics, byte[] goalStateByte) {
        this.nextTopics = nextTopics;
        this.goalStateByte = goalStateByte;
    }

    public MulticastGoalStateByte(List<String> nextTopics, List<String> nextSubTopics, byte[] goalStateByte) {
        this.nextTopics = nextTopics;
        this.nextSubTopics = nextSubTopics;
        this.goalStateByte = goalStateByte;
    }

    public List<String> getNextTopics() {
        return nextTopics;
    }

    public void setNextTopics(List<String> nextTopics) {
        this.nextTopics = nextTopics;
    }

    public List<String> getNextSubTopics() { return nextSubTopics; }

    public void setNextSubTopics(List<String> nextSubTopics) { this.nextSubTopics = nextSubTopics; }

    public byte[] getGoalStateByte() {
        return goalStateByte;
    }

    public void setGoalStateByte(byte[] goalStateByte) {
        this.goalStateByte = goalStateByte;
    }
}
