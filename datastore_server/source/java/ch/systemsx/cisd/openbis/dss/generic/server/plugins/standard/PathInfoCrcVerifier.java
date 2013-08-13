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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

/**
 * @author anttil
 */
public class PathInfoCrcVerifier implements IArchiveFileVerifier
{

    private final ICrcProvider crcProvider;

    public PathInfoCrcVerifier(ICrcProvider crcProvider)
    {
        this.crcProvider = crcProvider;
    }

    @Override
    public List<String> verify(File file)
    {
        List<String> errors = new ArrayList<String>();
        ZipFile zip;
        try
        {
            zip = new ZipFile(file);
        } catch (ZipException ex)
        {
            errors.add("Reading zip file failed: " + ex.getMessage());
            return errors;
        } catch (IOException ex)
        {
            errors.add("Reading zip file failed: " + ex.getMessage());
            return errors;
        }

        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            Long externalCrc = crcProvider.getCrc(entry.getName());
            if (externalCrc != null && externalCrc != entry.getCrc())
            {
                errors.add(entry.getName() + ": external CRC: " + externalCrc + ", header CRC: " + entry.getCrc());
            }
        }
        return errors;
    }

}
