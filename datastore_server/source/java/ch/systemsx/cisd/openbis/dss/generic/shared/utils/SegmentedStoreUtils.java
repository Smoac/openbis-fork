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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Utility methods for segmented stores.
 *
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreUtils
{
    private static final Pattern INCOMING_SHARE_ID_PATTERN = Pattern.compile("[0-9]+");

    private static final FileFilter FILTER_ON_INCOMING_SHARES = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if (pathname.isDirectory() == false)
                {
                    return false;
                }
                String name = pathname.getName();
                return INCOMING_SHARE_ID_PATTERN.matcher(name).matches();
            }
        };

    /**
     * Lists all folders in specified store root directory which match incoming share pattern.
     */
    public static File[] getImcomingShares(File storeRootDir)
    {
        File[] files = storeRootDir.listFiles(SegmentedStoreUtils.FILTER_ON_INCOMING_SHARES);
        Arrays.sort(files);
        return files;
    }

    /**
     * Returns first the id of the first incoming share folder of specified store root which allows
     * to move a file from specified incoming folder to the incoming share.
     */
    public static String findIncomingShare(File incomingFolder, File storeRoot)
    {
        if (incomingFolder.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Incoming folder does not exist or is not a folder: " + incomingFolder);
        }
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exist or is not a folder: " + storeRoot);
        }
        return findIncomingShare(incomingFolder, storeRoot.listFiles(FILTER_ON_INCOMING_SHARES));
    }

    /**
     * Returns the name of the first share folder which allows to move a file from specified
     * incoming folder to that share folder.
     */
    public static String findIncomingShare(File incomingDataDirectory, File[] shares)
    {
        File testFile = new File(incomingDataDirectory, ".DDS_TEST");
        try
        {
            testFile.createNewFile();
        } catch (IOException ex)
        {
            throw new ConfigurationFailureException(
                    "Couldn't create a test file in the following incoming folder: "
                    + incomingDataDirectory, ex);
        }
        File matchingShare = findShare(testFile, shares);
        return matchingShare.getName();
    }
    
    private static File findShare(File testFile, File[] shares)
    {
        for (File share : shares)
        {
            File destination = new File(share, testFile.getName());
            if (testFile.renameTo(destination))
            {
                destination.delete();
                return share;
            }
        }
        throw new ConfigurationFailureException(
                "Now share could be found for the following incoming folder: "
                        + testFile.getParentFile().getAbsolutePath());
    }
    

}
