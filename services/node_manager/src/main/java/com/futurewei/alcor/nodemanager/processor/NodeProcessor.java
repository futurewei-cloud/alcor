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
package com.futurewei.alcor.nodemanager.processor;

import com.futurewei.alcor.nodemanager.request.*;

public class NodeProcessor extends AbstractProcessor{
    @Override
    void createProcess(NodeContext context) throws Exception {
        IRestRequest createNodeRequest = new CreateNodeInfoRequest(context, context.getNodeInfo());
        context.getRequestManager().sendRequestAsync(createNodeRequest);
    }

    @Override
    void updateProcess(NodeContext context) throws Exception {
        IRestRequest updateNodeRequest = new UpdateNodeInfoRequest(context, context.getNodeInfo());
        context.getRequestManager().sendRequestAsync(updateNodeRequest);
    }

    @Override
    void deleteProcess(NodeContext context) throws Exception {
        IRestRequest deleteNodeRequest = new DeleteNodeInfoRequest(context, context.getNodeInfo().getId());
        context.getRequestManager().sendRequestAsync(deleteNodeRequest);
    }

    @Override
    void bulkCreateProcess(NodeContext context) throws Exception {
        IRestRequest bulkCreateNodeInfoRequest = new BulkCreateNodeInfoRequest(context, context.getNodeInfos());
        context.getRequestManager().sendRequestAsync(bulkCreateNodeInfoRequest);
    }
}
