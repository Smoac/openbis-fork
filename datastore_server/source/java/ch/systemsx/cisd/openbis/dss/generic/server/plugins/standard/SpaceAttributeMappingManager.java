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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Helper class to read and provided space-related attributes. 
 *
 * @author Franz-Josef Elmer
 */
public class SpaceAttributeMappingManager
{
    private final Map<String, File> folders = new HashMap<String, File>();
    
    public SpaceAttributeMappingManager(String mappingFile, boolean createArchives)
    {
        CsvReader reader = null;
        try
        {
            reader = new CsvReader(mappingFile, '\t');
            reader.setSkipEmptyRecords(true);
            reader.setUseComments(false);
            reader.setComment('#');
            reader.setTrimWhitespace(true);
            boolean success = reader.readHeaders();
            if (success == false)
            {
                throw new IllegalArgumentException("Empty mapping file: " + mappingFile);
            }
            while (reader.readRecord())
            {
                String[] row = reader.getValues();
                if (row.length != 3)
                {
                    throw new IllegalArgumentException("Invalid number of row elements in mapping file '"
                            + mappingFile + "': " + Arrays.asList(row));
                }
                String space = row[0].toUpperCase();
                folders.put(space, getArchiveFolder(space, row, createArchives));
            }
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    private File getArchiveFolder(String space, String[] row, boolean createArchives)
    {
        File folder = new File(row[2]);
        if (createArchives)
        {
            if (folder.isFile())
            {
                throw new IllegalArgumentException("Archive folder '" + folder + "' is a file.");
            }
            if (folder.exists() == false)
            {
                boolean success = folder.mkdirs();
                if (success == false)
                {
                    throw new IllegalArgumentException("Couldn't create archive folder '" + folder + "'.");
                }
            }
        } else
        {
            if (folder.isDirectory() == false)
            {
                throw new IllegalArgumentException("Archive folder '" + folder + "' for space "
                        + space + " doesn't exists or is a file.");
            }
        }
        return folder;
    }
    
    public Map<String, File> getFoldersMap()
    {
        return folders;
    }
}
