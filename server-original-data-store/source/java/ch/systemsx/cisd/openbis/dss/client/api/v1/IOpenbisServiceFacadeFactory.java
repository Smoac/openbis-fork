/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.client.api.v1;

import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;

/**
 * @author Jakub Straszewski
 */
public interface IOpenbisServiceFacadeFactory
{
    /**
     * Creates an {@link OpenbisServiceFacade} instance that can be used to interact with an openBIS backend.
     * 
     * @param username an openBIS user id.
     * @param password a password corresponding to the <code>username</code> parameter.
     * @param openbisUrl the HTTP url of the remote openBIS server e.g. https://openbis.ethz.ch/openbis/
     * @param timeoutInMillis a remote-call timeout.
     */
    public IOpenbisServiceFacade tryToCreate(String username, String password, String openbisUrl,
            long timeoutInMillis);

    /**
     * Creates an {@link OpenbisServiceFacade} instance that can be used to interact with an openBIS backend.
     * 
     * @param sessionToken token for existing sesstion in openBIS.
     * @param openbisUrl the HTTP url of the remote openBIS server e.g. https://openbis.ethz.ch/openbis/
     * @param timeoutInMillis a remote-call timeout.
     */
    public IOpenbisServiceFacade tryToCreate(String sessionToken, String openbisUrl,
            long timeoutInMillis);
}
