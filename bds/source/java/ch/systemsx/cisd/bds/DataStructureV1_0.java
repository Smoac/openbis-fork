/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataStructureV1_0 extends AbstractDataStructure
{
    private static final Version VERSION = new Version(1, 0);
    
    public DataStructureV1_0(IStorage storage)
    {
        super(storage);
    }

    public Version getVersion()
    {
        return VERSION;
    }

    public IDirectory getOriginalData()
    {
        return Utilities.getSubDirectory(getDataDirectory(), "original");
    }

    public IFormatedData getFormatedData()
    {
        return null;
    }
    
    public ExperimentIdentifier getExperimentIdentifier()
    {
        return ExperimentIdentifier.loadFrom(getMetaDataDirectory());
    }
    
    public void setExperimentIdentifier(ExperimentIdentifier id)
    {
        id.saveTo(getMetaDataDirectory());
    }
    
    public ProcessingType getProcessingType()
    {
        return ProcessingType.loadFrom(getMetaDataDirectory());
    }
    
    public void setProcessingType(ProcessingType type)
    {
        type.saveTo(getMetaDataDirectory());
    }
    
    private IDirectory getDataDirectory()
    {
        IDirectory subDirectory = Utilities.getSubDirectory(root, "data");
        return subDirectory;
    }
    
    private IDirectory getMetaDataDirectory()
    {
        IDirectory subDirectory = Utilities.getSubDirectory(root, "metadata");
        return subDirectory;
    }
}

