/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Simple data type with information about recovery state.
 *  - the file
 *  - time of last recovery try
 *  - total count of tries to recover
 * 
 * @author jakubs
 */
public class DataSetStorageRecoveryInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final File recoveryStateFile;
    
    private final Date lastTry;
    
    private int tryCount;

    public DataSetStorageRecoveryInfo(File recoveryStateFile, Date lastTry, int tryCount)
    {
        super();
        this.recoveryStateFile = recoveryStateFile;
        this.lastTry = lastTry;
        this.tryCount = tryCount;
    }
    
    public void increaseTryCount()
    {
        tryCount++;
    }
    
    public File getRecoveryStateFile()
    {
        return recoveryStateFile;
    }

    public Date getLastTry()
    {
        return lastTry;
    }

    public int getTryCount()
    {
        return tryCount;
    }
    
    public void writeToFile(File informationFile)
    {
        FileUtilities.writeToFile(informationFile, this);
    }
    
    public static DataSetStorageRecoveryInfo loadFromFile(File informationFile)
    {
        return FileUtilities.loadToObject(informationFile, DataSetStorageRecoveryInfo.class);
    }
}
