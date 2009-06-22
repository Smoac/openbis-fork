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

package ch.systemsx.cisd.cifex.plugins.dss;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.trigger.AsynchronousTrigger;
import ch.systemsx.cisd.cifex.server.trigger.ITrigger;
import ch.systemsx.cisd.cifex.server.trigger.ITriggerConsole;
import ch.systemsx.cisd.cifex.server.trigger.ITriggerRequest;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HardLinkMaker;
import ch.systemsx.cisd.common.filesystem.IFileImmutableCopier;

/**
 * {@link ITrigger} implementation moving the uploaded file to the data store incoming directory.
 * 
 * @author Izabela Adamczyk
 */
@AsynchronousTrigger
public class DataStoreTrigger implements ITrigger
{

    private static final String USER_EMAIL_KEY = "user-email";

    private static final String COMMENT_KEY = "comment";

    private static final String REQUEST_PROPERTIES_FILE = "request.properties";

    private static final String INCOMING_DIRECTORY_PATH_KEY = "dss-incoming-directory";

    private File incomingDirectory;

    public DataStoreTrigger()
    {
    }

    public DataStoreTrigger(final Properties properties)
    {
        String incomingDirectoryPath = properties.getProperty(INCOMING_DIRECTORY_PATH_KEY);
        if (StringUtils.isEmpty(incomingDirectoryPath))
        {
            throw new ConfigurationFailureException(INCOMING_DIRECTORY_PATH_KEY + " not configured");
        }
        incomingDirectory = new File(incomingDirectoryPath);
        String errorMessage =
                FileUtilities.checkDirectoryFullyAccessible(incomingDirectory,
                        "data store incoming directory");
        if (errorMessage != null)
        {
            throw new EnvironmentFailureException(errorMessage);
        }

    }

    public void handle(ITriggerRequest request, ITriggerConsole console)
    {
        File sourceFile = request.getFile();
        try
        {
            File destDir = createDestinationDirectory(sourceFile);
            saveRequestPropertiesFile(destDir, request);
            copyUploadedFile(sourceFile, destDir);
        } catch (Exception ex)
        {
            console.sendMessage("Data set upload failed", String.format(
                    "Upload of data set (file: '%s', comment: '%s') failed. ('%s')", sourceFile,
                    request.getComment(), ex.getMessage()), null, request.getUploadingUserEmail());
        } finally
        {
            request.dismiss();
        }
    }

    private File createDestinationDirectory(File sourceFile)
    {
        File destDir =
                FileUtilities.createNextNumberedFile(new File(incomingDirectory, sourceFile
                        .getName()
                        + ".dir"), null);
        boolean success = destDir.mkdir();
        if (success == false)
        {
            throw new EnvironmentFailureException("Could not create destination directory");
        }
        return destDir;
    }

    private void copyUploadedFile(File sourceFile, File destinationDirectory)
    {
        IFileImmutableCopier copier = HardLinkMaker.tryCreate();
        if (copier != null)
        {
            boolean status = copier.copyFileImmutably(sourceFile, destinationDirectory, null);
            if (status == false)
            {
                throw new EnvironmentFailureException("Could not create the hardlink");
            }
        } else
        {
            FileUtilities.copyFileTo(sourceFile, destinationDirectory, false);
        }
    }

    private void saveRequestPropertiesFile(File destDir, ITriggerRequest request)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(createProperty(COMMENT_KEY, request.getComment()));
        sb.append(createProperty(USER_EMAIL_KEY, request.getUploadingUserEmail()));
        File requestPropertiesFile = new File(destDir, REQUEST_PROPERTIES_FILE);
        FileUtilities.writeToFile(requestPropertiesFile, sb.toString());
    }

    private static String createProperty(String key, String value)
    {
        return key + "=" + value + "\n";
    }
}
