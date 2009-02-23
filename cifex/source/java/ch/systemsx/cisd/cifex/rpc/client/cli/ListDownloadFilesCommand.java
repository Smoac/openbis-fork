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
import ch.systemsx.cisd.cifex.shared.basic.dto.File;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command for listing files that are available for download.
 * 
 * @author Bernd Rinn
 */
public class ListDownloadFilesCommand extends AbstractCommandWithSessionToken
{

    private static final String NAME = "list-files";

    private static ListDownloadFilesCommand instance;

    ListDownloadFilesCommand()
    {
        super(NAME);
    }

    /** Returns the unique instance of this class. */
    public final static synchronized ListDownloadFilesCommand getInstance()
    {
        if (instance == null)
        {
            instance = new ListDownloadFilesCommand();
        }
        return instance;
    }

    @Override
    protected int execute(String sessionToken, ICIFEXRPCService service, String[] args)
            throws UserFailureException, EnvironmentFailureException
    {
        getParameters().assertArgsEmpty();
        final File[] files = service.listDownloadFiles(sessionToken);
        if (files.length == 0)
        {
            System.out.println("No files available for you to download.");
        } else
        {
            System.out.println("Id\tName\tSize\tContent Type\tUploader\tExpiration\tComment");
            for (File file : files)
            {
                System.out.println(file.getIDStr() + "\t" + file.getName() + "\t" + file.getSize()
                        + "\t" + file.getContentType() + "\t" + file.getRegisterer() + "\t"
                        + file.getExpirationDate() + "\t" + file.getComment());
            }
        }
        return 0;
    }

}
