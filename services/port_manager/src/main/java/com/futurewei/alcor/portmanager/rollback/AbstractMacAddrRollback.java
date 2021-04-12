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
package com.futurewei.alcor.portmanager.rollback;

import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractMacAddrRollback implements Rollback {
    protected MacManagerRestClient macManagerRestClient;

    protected List<MacState> allocatedMacs = new ArrayList<>();
    protected List<MacState> releasedMacs = new ArrayList<>();
    protected List<MacState> updatedMacs = new ArrayList<>();

    public AbstractMacAddrRollback(MacManagerRestClient macManagerRestClient) {
        this.macManagerRestClient = macManagerRestClient;
    }

    public abstract void doRollback() throws Exception;

    public void putAllocatedMacAddress(MacState macState) {
        allocatedMacs.add(macState);
    }

    public void putReleasedMacAddress(MacState macState) {
        releasedMacs.add(macState);
    }

    public void putUpdatedMacAddress(MacState macState) {
        updatedMacs.add(macState);
    }

    public void putAllocatedMacAddresses(List<MacState> macStates) {
        allocatedMacs.addAll(macStates);
    }

    public void putReleasedMacAddresses(List<MacState> macStates) {
        releasedMacs.addAll(macStates);
    }

    public void putUpdatedMacAddresses(List<MacState> macStates) {
        updatedMacs.addAll(macStates);
    }
}
