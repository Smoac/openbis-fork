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

import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.server.DatabaseBasedDataSetPathInfoProvider;

/**
 * Provides a CRC32 checksum from pathinfo db for given file.
 * 
 * @author anttil
 */
public class CrcProvider implements ICrcProvider
{
    private Map<String, Integer> checksums;

    public CrcProvider(String dataSetCode)
    {
        checksums = new DatabaseBasedDataSetPathInfoProvider().getDataSetChecksums(dataSetCode);
    }

    @Override
    public Long getCrc(String name)
    {
        Integer crc = checksums.get(name);
        if (crc == null)
        {
            return null;
        } else
        {
            return getUnsignedInt(crc);
        }
    }

    public static long getUnsignedInt(int x)
    {
        return x & 0x00000000ffffffffL;
    }

}
