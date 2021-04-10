/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.AllocateMacAddrException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateBulkJson;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AllocateMacAddressRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(AllocateMacAddressRequest.class);

    private MacManagerRestClient macManagerRestClient;
    private List<MacState> macStates;
    private List<MacState> result;

    public AllocateMacAddressRequest(PortContext context, List<MacState> macStates) {
        super(context);
        this.macStates = macStates;
        this.result = new ArrayList<>();
        this.macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
    }

    public List<MacState> getResult() {
        return result;
    }

    @Override
    public void send() throws Exception {
        if (macStates.size() == 1) {
            MacStateJson macStateJson = macManagerRestClient.allocateMacAddress(macStates.get(0));
            if (macStateJson == null || macStateJson.getMacState() == null) {
                throw new AllocateMacAddrException();
            }

            result.add(macStateJson.getMacState());
        } else {
            MacStateBulkJson macStateBulkJson = macManagerRestClient.allocateMacAddressBulk(macStates);
            if (macStateBulkJson == null ||
                    macStateBulkJson.getMacStates() == null ||
                    macStateBulkJson.getMacStates().size() != macStates.size()) {
                throw new AllocateMacAddrException();
            }

            result.addAll(macStateBulkJson.getMacStates());
        }
    }



    @Override
    public void rollback() throws Exception {
        LOG.info("AllocateRandomMacRequest rollback, macStates: {}", result);
        //TODO: Instead by releaseMacAddresses interface
        for (MacState macState: result) {
            macManagerRestClient.releaseMacAddress(macState.getMacAddress());
        }
    }
}
