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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateMacAddressRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMacAddressRequest.class);

    private MacManagerRestClient macManagerRestClient;
    private MacState newMacState;
    private MacState oldMacState;

    public UpdateMacAddressRequest(PortContext context, MacState newMacState, MacState oldMacState) {
        super(context);
        this.newMacState = newMacState;
        this.oldMacState = oldMacState;
        this.macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        macManagerRestClient.updateMacAddress(oldMacState.getMacAddress(), newMacState);
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("UpdateMacRequest rollback, oldMacState: {}", oldMacState);
        macManagerRestClient.updateMacAddress(newMacState.getMacAddress(), oldMacState);
    }
}
