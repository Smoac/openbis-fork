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

package ch.systemsx.cisd.bds.hcs;

import java.io.File;

import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.v1_0.IDataStructureV1_0;

/**
 * Helper class for easy handling of HCS image dataset standard structure.
 * 
 * @author Tomasz Pylak
 */
public class HCSDatasetLoader
{
    private final DataStructureLoader loader;

    private final IDataStructureV1_0 dataStructure;

    private final IHCSImageFormattedData imageAccessor;

    public HCSDatasetLoader(File baseDir)
    {
        this.loader = new DataStructureLoader(baseDir);
        this.dataStructure = (IDataStructureV1_0) loader.load(".", false);
        this.imageAccessor = (IHCSImageFormattedData) dataStructure.getFormattedData();

    }

	/** has to be called at the end */
    public void close()
    {
        dataStructure.close();
    }

    public Geometry getPlateGeometry()
    {
        return imageAccessor.getPlateGeometry();
    }

    public Geometry getWellGeometry()
    {
        return imageAccessor.getWellGeometry();
    }

    public int getChannelCount()
    {
        return imageAccessor.getChannelCount();
    }

    public INode tryGetStandardNodeAt(int chosenChannel, Location wellLocation,
            Location tileLocation)
    {
        return imageAccessor.tryGetStandardNodeAt(chosenChannel, wellLocation, tileLocation);
    }

}