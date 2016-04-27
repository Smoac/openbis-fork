/*
 * Copyright 2011 ETH Zuerich, CISD
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

package eu.basynthec.cisd.client.examples;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;

/**
 * Abstract superclass for code that interacts with the BaSynthec database via the IOpenbisServiceFacade interface.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractBaSynthecClient
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            BaSynthecClient.class);

    protected final IOpenbisServiceFacade openBis;

    /**
     * Utility method to log information.
     */
    protected static void logInfo(String msg)
    {
        operationLog.info(msg);
    }

    /**
     * Constructor that initializes the openBis facade.
     * 
     * @param facade The facade for interacting with openBIS.
     */
    public AbstractBaSynthecClient(IOpenbisServiceFacade facade)
    {
        this.openBis = facade;
    }

    /**
     * Utility method for printing.
     */
    protected void println(String text)
    {
        System.out.println(text);
    }

}