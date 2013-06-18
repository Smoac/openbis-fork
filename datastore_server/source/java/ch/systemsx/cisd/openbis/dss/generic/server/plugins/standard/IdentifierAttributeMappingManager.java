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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Helper class to load and provided from mapping of space/project/experiment identifier to attributes. 
 * If a mappings exist on experiment, project and space level the experiment mapping will be used if
 * the experiment of the data set fits. Otherwise the project mapping is tried. 
 *
 * @author Franz-Josef Elmer
 */
public class IdentifierAttributeMappingManager
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, IdentifierAttributeMappingManager.class);
    
    private final Map<String, Attributes> attributesMap = new HashMap<String, Attributes>();
    private final boolean createArchives;
    private final File mappingFileOrNull;
    
    private long mappingFileLastModified;
    
    public IdentifierAttributeMappingManager(String mappingFilePathOrNull, boolean createArchives)
    {
        this.createArchives = createArchives;
        if (StringUtils.isBlank(mappingFilePathOrNull))
        {
            mappingFileOrNull = null;
        }
        else
        {
            mappingFileOrNull = new File(mappingFilePathOrNull);
            if (mappingFileOrNull.exists() == false)
            {
                throw new IllegalArgumentException("Mapping file '" + mappingFileOrNull + "' does not exist.");
            }
            getAttributesMap(); // loads and validates mapping data
        }
    }

    private Map<String, Attributes> getAttributesMap()
    {
        if (mappingFileOrNull != null)
        {
            long lastModified = mappingFileOrNull.lastModified();
            if (lastModified != mappingFileLastModified)
            {
                loadMappingFile(mappingFileOrNull);
                mappingFileLastModified = lastModified;
            }
        }
        return attributesMap;
    }

    private void loadMappingFile(File mappingFile)
    {
        CsvReader reader = null;
        try
        {
            reader = new CsvReader(mappingFile.getPath(), '\t');
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
                String identifier = row[0].toUpperCase();
                String shareID = row[1];
                if (StringUtils.isBlank(shareID))
                {
                    shareID = null;
                }
                File archiveFolder = getArchiveFolder(identifier, row);
                List<String> shareIds = getShareIds(identifier, row);
                attributesMap.put(identifier, new Attributes(shareIds , archiveFolder));
            }
            operationLog.info("Mapping file '" + mappingFile + "' successfully loaded.");
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

    private List<String> getShareIds(String identifier, String[] row)
    {
        String idsAttribute = row[1];
        List<String> ids = new ArrayList<String>();
        if (StringUtils.isBlank(idsAttribute))
        {
            return ids;
        }
        String[] splittedIds = idsAttribute.split(",");
        for (String id : splittedIds)
        {
            ids.add(id.trim());
        }
        return ids;
    }

    private File getArchiveFolder(String identifier, String[] row)
    {
        String folderAttribute = row[2];
        if (StringUtils.isBlank(folderAttribute))
        {
            return null;
        }
        File folder = new File(folderAttribute);
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
                throw new IllegalArgumentException("Archive folder '" + folder + "' for identifier "
                        + identifier + " doesn't exists or is a file.");
            }
        }
        return folder;
    }
    
    public Collection<File> getAllFolders()
    {
        Set<File> folders = new HashSet<File>();
        for (Attributes attributes : getAttributesMap().values())
        {
            File archiveFolder = attributes.getArchiveFolder();
            if (archiveFolder != null && archiveFolder.exists())
            {
                folders.add(archiveFolder);
            }
        }
        return folders;
    }
    
    public File getArchiveFolder(DatasetDescription dataSetDescription, File defaultFolder)
    {
        String spaceCode = dataSetDescription.getSpaceCode();
        String projectCode = dataSetDescription.getProjectCode();
        String experimentCode = dataSetDescription.getExperimentCode();
        Attributes attributes = tryGetExperimentAttributes(spaceCode, projectCode, experimentCode);
        if (attributes != null && attributes.getArchiveFolder() != null)
        {
            return attributes.getArchiveFolder();
        }
        attributes = tryGetProjectAttributes(spaceCode, projectCode);
        if (attributes != null && attributes.getArchiveFolder() != null)
        {
            return attributes.getArchiveFolder();
        }
        attributes = tryGetSpaceAttributes(spaceCode);
        if (attributes != null && attributes.getArchiveFolder() != null)
        {
            return attributes.getArchiveFolder();
        }
        return defaultFolder;
    }
    
    public List<String> getShareIds(SimpleDataSetInformationDTO dataSet)
    {
        String spaceCode = dataSet.getSpaceCode();
        String projectCode = dataSet.getProjectCode();
        String experimentCode = dataSet.getExperimentCode();
        Attributes attributes = tryGetExperimentAttributes(spaceCode, projectCode, experimentCode);
        if (attributes != null && attributes.getShareIds() != null)
        {
            return attributes.getShareIds();
        }
        attributes = tryGetProjectAttributes(spaceCode, projectCode);
        if (attributes != null && attributes.getShareIds() != null)
        {
            return attributes.getShareIds();
        }
        attributes = tryGetSpaceAttributes(spaceCode);
        if (attributes != null && attributes.getShareIds() != null)
        {
            return attributes.getShareIds();
        }
        return Collections.emptyList();
    }
    
    private Attributes tryGetExperimentAttributes(String spaceCode, String projectCode, String experimentCode)
    {
        String identifier = new ExperimentIdentifier(null, spaceCode, projectCode, experimentCode).toString();
        return getAttributesMap().get(identifier);
    }

    private Attributes tryGetProjectAttributes(String spaceCode, String projectCode)
    {
        String identifier = new ProjectIdentifier(null, spaceCode, projectCode).toString();
        return getAttributesMap().get(identifier);
    }
    
    private Attributes tryGetSpaceAttributes(String spaceCode)
    {
        return getAttributesMap().get(new SpaceIdentifier(spaceCode).toString());
    }
    
    private static final class Attributes
    {
        private final File archiveFolder;
        private final List<String> shareIds;

        Attributes(List<String> shareIds, File archiveFolder)
        {
            this.shareIds = shareIds;
            this.archiveFolder = archiveFolder;
        }

        public List<String> getShareIds()
        {
            return shareIds;
        }
        
        public File getArchiveFolder()
        {
            return archiveFolder;
        }

    }
}
