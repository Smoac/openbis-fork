/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client;

import ch.systemsx.cisd.common.exception.EnvironmentFailureException;

/**
 * An exception for communicating a mismatch in client-server API.
 * 
 * @author Bernd Rinn
 */
public class IncompatibleAPIVersionsException extends EnvironmentFailureException
{
    private static final long serialVersionUID = 1L;

    /**
     * Either <code>clientVersion < minimalClientVersion</code>, or
     * <code>clientVersion > serverVersion</code> is expected here.
     * 
     * @param clientVersion The version of the client.
     * @param serverVersion The version of the server.
     * @param minimalClientVersion The minimal client version that the server requires to be able to
     *            work with the client.
     */
    public IncompatibleAPIVersionsException(int clientVersion, int serverVersion,
            int minimalClientVersion)
    {
        super((clientVersion < serverVersion) ? String.format(
                "This client is too old for the server "
                        + "(client API version: %d, minimally required API version: %d",
                clientVersion, minimalClientVersion) : String.format(
                "This client is too new for the server "
                        + "(client API version: %d, server API version: %d", clientVersion,
                serverVersion));
    }
}
