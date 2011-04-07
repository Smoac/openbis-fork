/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDatasetLocation;

/**
 * Implementation of {@link IDataSetDirectoryProvider}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetDirectoryProvider implements IDataSetDirectoryProvider
{
    private final File storeRoot;

    private final IShareIdManager shareIdManager;

    public DataSetDirectoryProvider(File storeRoot, IShareIdManager shareIdManager)
    {
        this.storeRoot = storeRoot;
        this.shareIdManager = shareIdManager;

    }

    public File getStoreRoot()
    {
        return storeRoot;
    }

    public File getDataSetDirectory(IDatasetLocation dataSet)
    {
        String location = dataSet.getDataSetLocation();
        location = location.replace("\\", File.separator);
        File share = new File(storeRoot, shareIdManager.getShareId(dataSet.getDatasetCode()));
        return new File(share, location);
    }

    public IShareIdManager getShareIdManager()
    {
        return shareIdManager;
    }

}
