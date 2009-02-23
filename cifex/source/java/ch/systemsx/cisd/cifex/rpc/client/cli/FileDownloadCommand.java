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

package ch.systemsx.cisd.cifex.rpc.client.cli;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command to perform file downloads from CIFEX.
 *
 * @author Bernd Rinn
 */
public class FileDownloadCommand extends AbstractCommandWithSessionToken
{

    private static final String NAME = "download";

    private static FileDownloadCommand instance;

    FileDownloadCommand()
    {
        super(NAME);
    }

    /** Returns the unique instance of this class. */
    public final static synchronized FileDownloadCommand getInstance()
    {
        if (instance == null)
        {
            instance = new FileDownloadCommand();
        }
        return instance;
    }

    @Override
    protected int execute(String sessionToken, ICIFEXRPCService service, String[] args)
            throws UserFailureException, EnvironmentFailureException
    {
        
        // TODO Auto-generated method stub
        return 0;
    }

}
