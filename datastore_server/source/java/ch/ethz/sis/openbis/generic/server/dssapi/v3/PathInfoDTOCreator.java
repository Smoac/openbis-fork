/*
 * Copyright 2010 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;

/**
 * Converter between DataSetFileCreation (V3 API) and PathEntryDTO (PathinfoDB EODSQL DTO)
 * 
 * @author anttil
 */
public class PathInfoDTOCreator
{

    public static Collection<PathEntryDTO> createPathEntries(long dataSetId, String dataSetCode, Collection<DataSetFileCreation> files)
    {
        checkInput(files);
        Map<String, Long> fileIds = createIdsToPaths(files);

        Date lastModified = new Date();
        List<PathEntryDTO> result = new ArrayList<>();
        Map<String, Long> directorySizes = new HashMap<>();
        for (DataSetFileCreation file : files)
        {
            String path = file.getPath();

            File f = new File(path);
            PathEntryDTO dto = new PathEntryDTO();
            dto.setDataSetId(dataSetId);
            dto.setDataSetCode(dataSetCode);
            dto.setDirectory(file.isDirectory());
            dto.setFileName(f.getName());
            dto.setRelativePath(f.getPath());
            dto.setId(fileIds.get(f.getPath()));
            if (f.getParent() != null)
            {
                dto.setParentId(fileIds.get(f.getParent()));
            }
            if (file.isDirectory() == false)
            {
                dto.setSizeInBytes(file.getFileLength());
                dto.setChecksumCRC32(file.getChecksumCRC32());
            } else
            {
                dto.setSizeInBytes(0);
            }
            dto.setLastModifiedDate(lastModified);
            result.add(dto);

            long leafSize = dto.getSizeInBytes();

            f = new File(path).getParentFile();
            while (f != null)
            {
                dto = new PathEntryDTO();
                dto.setDataSetId(dataSetId);
                dto.setDataSetCode(dataSetCode);
                dto.setDirectory(true);
                dto.setFileName(f.getName());
                dto.setRelativePath(f.getPath());
                dto.setId(fileIds.get(f.getPath()));
                if (f.getParent() != null)
                {
                    dto.setParentId(fileIds.get(f.getParent()));
                }
                dto.setLastModifiedDate(lastModified);
                result.add(dto);

                Long current = directorySizes.get(f.getPath());
                if (current == null)
                {
                    current = 0l;
                }
                current += leafSize;
                directorySizes.put(f.getPath(), current);

                f = f.getParentFile();
            }
        }

        for (PathEntryDTO dto : result)
        {
            if (dto.isDirectory() && dto.getSizeInBytes() == null)
            {
                dto.setSizeInBytes(directorySizes.get(dto.getRelativePath()));
            }
        }

        return new HashSet<PathEntryDTO>(result);
    }

    private static Map<String, Long> createIdsToPaths(Collection<DataSetFileCreation> files)
    {
        Map<String, Long> result = new HashMap<>();
        long id = 1l;
        for (DataSetFileCreation file : files)
        {
            String path = file.getPath();
            File f = new File(path);
            while (f != null)
            {
                if (result.containsKey(f.getPath()) == false)
                {
                    result.put(f.getPath(), id++);
                }
                f = f.getParentFile();
            }
        }
        return result;
    }

    private static void checkInput(Collection<DataSetFileCreation> files)
    {
        Set<String> paths = new HashSet<>();
        for (DataSetFileCreation file : files)
        {
            checkInput(file);
            check(paths.contains(file.getPath()), "Path " + file.getPath() + " appears twice");
            paths.add(file.getPath());
        }
    }

    private static void checkInput(DataSetFileCreation file)
    {
        String path = file.getPath();
        check(path == null || path.length() == 0, "Path of " + file + " was null");
        check(path.startsWith("/"), "Path of " + file + " is absolute");
        check(file.isDirectory() == false && file.getFileLength() == null, "Size of " + file + " is null");
        check(file.isDirectory() && file.getFileLength() != null, "Directory " + file + " has a size");
        check(file.isDirectory() && file.getChecksumCRC32() != null, "Directory " + file + " has a checksum");
    }

    private static void check(boolean condition, String message)
    {
        if (condition)
        {
            throw new IllegalArgumentException(message);
        }
    }
}
