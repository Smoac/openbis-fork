/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * @author anttil
 */
public class WriteDirectoryNameToDropboxAction implements DirectoryAction
{

    private final File dropboxDirectory;

    public WriteDirectoryNameToDropboxAction(File dropboxDirectory)
    {
        this.dropboxDirectory = dropboxDirectory;

    }

    @Override
    public void performOn(File subdir)
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(dropboxDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
            out.print(subdir.getAbsolutePath());
        } catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }
}
