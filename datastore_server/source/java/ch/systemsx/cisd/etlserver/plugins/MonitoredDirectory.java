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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author anttil
 */
public class MonitoredDirectory
{

    private final File directory;

    private final String readyToImportMarkerFile;

    private final String alreadyImportedMarkerFile;

    public MonitoredDirectory(File directory, String readyToImportMarkerFile, String alreadyImportedMarkerFile)
    {
        if (directory.isFile())
        {
            throw new IllegalArgumentException("Monitored directory " + directory.getAbsolutePath() + " is a file.");
        }
        if (directory.exists() == false)
        {
            throw new IllegalArgumentException("Monitored directory " + directory.getAbsolutePath() + " does not exist.");
        }
        if (directory.canWrite() == false)
        {
            throw new IllegalArgumentException("Cannot write to monitored directory " + directory.getAbsolutePath());
        }
        if (directory.canRead() == false)
        {
            throw new IllegalArgumentException("Cannot read from monitored directory " + directory.getAbsolutePath());
        }
        if (readyToImportMarkerFile.equals(alreadyImportedMarkerFile))
        {
            throw new IllegalArgumentException("Cannot have same value for both marker files (" + readyToImportMarkerFile + ")");
        }

        this.directory = directory;
        this.readyToImportMarkerFile = readyToImportMarkerFile;
        this.alreadyImportedMarkerFile = alreadyImportedMarkerFile;
    }

    public void perform(DirectoryAction action)
    {
        for (File subdir : listSubDirectoriesOf(directory))
        {
            File ready = new File(subdir, readyToImportMarkerFile);
            File imported = new File(subdir, alreadyImportedMarkerFile);
            if ((ready.exists() == true) && (imported.exists() == false))
            {
                try
                {
                    imported.createNewFile();
                    action.performOn(subdir);
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                    continue;
                }
            }
        }
    }

    private Collection<File> listSubDirectoriesOf(File dir)
    {
        File[] subdirectories = dir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isDirectory();
                }
            });
        return Arrays.asList(subdirectories);
    }
}
