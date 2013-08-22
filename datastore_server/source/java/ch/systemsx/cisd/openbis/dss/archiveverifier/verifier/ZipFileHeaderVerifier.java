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

package ch.systemsx.cisd.openbis.dss.archiveverifier.verifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

/**
 * Compares zip file headers to information from an external source, such as pathinfo db.
 * 
 * @author anttil
 */
public class ZipFileHeaderVerifier extends AbstractZipFileVerifier
{

    private final IArchiveFileMetaDataRepository fileMetaDataRepository;

    private final CrcEnabled crcEnabled;

    public ZipFileHeaderVerifier(IArchiveFileMetaDataRepository fileMetaDataRepository, CrcEnabled crcEnabled)
    {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.crcEnabled = crcEnabled;
    }

    @Override
    public List<String> verify(ZipFile zip)
    {
        List<String> errors = new ArrayList<String>();

        String filename = zip.getName();
        String dataSetCode = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
        String repositoryName = fileMetaDataRepository.getDescription();

        IArchiveFileContent metaData = fileMetaDataRepository.getMetaData(dataSetCode);
        if (metaData == null)
        {
            errors.add("Could not find entry for dataset in " + repositoryName);
            return errors;
        }

        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryName = entry.getName();

            if (entry.isDirectory() || entryName.equals(AbstractDataSetPackager.META_DATA_FILE_NAME))
            {
                continue;
            }

            Long externalSize = metaData.getFileSize(entryName);
            if (externalSize == null)
            {
                errors.add("Could not find entry for file " + entryName + " in " + repositoryName);
                continue;
            }

            if (entry.getSize() != externalSize)
            {
                errors.add(entryName + ": size in archive file: " + entry.getSize() + ", size in " + repositoryName + ": "
                        + externalSize);
            }

            Long externalCrc = metaData.getFileCrc(entryName);
            if (CrcEnabled.TRUE.equals(crcEnabled))
            {
                if (externalCrc == null)
                {
                    errors.add(entryName + ": no CRC32 found in " + repositoryName);

                } else if (externalCrc != entry.getCrc())
                {
                    errors.add(entryName + ": CRC32 in archive file: " + entry.getCrc() + ", CRC32 in " + repositoryName + ": " + externalCrc);
                }
            } else if (externalCrc != null)
            {
                errors.add(entryName + ": CRC32 found in " + repositoryName + " even it should be disabled. Value was " + externalCrc);
            }
        }
        return errors;
    }
}
