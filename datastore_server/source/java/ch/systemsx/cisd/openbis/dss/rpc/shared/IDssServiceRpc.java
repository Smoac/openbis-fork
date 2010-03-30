/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.rpc.shared;

/**
 * The most generic interface for RPC invocations into DSS.
 * <p>
 * This interface defines a minimal interface presented by DSS. It lets clients determine which
 * version of the interface the server supports. To actualy interact with the server, clients should
 * use a versioned interface, e.g., IDssServiceRpcV1.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpc
{
    //
    // Protocol versioning
    //
    /** Returns the version of the server side interface. */
    public int getVersion();

    /**
     * Returns the minimal version that the client needs to have in order to be able to talk to this
     * server.
     */
    public int getMinClientVersion();

}
