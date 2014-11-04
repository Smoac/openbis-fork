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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Context for perfoming archiving/unarchiving.
 * 
 * @author Franz-Josef Elmer
 */
public class ArchiverTaskContext
{
    private final IDataSetDirectoryProvider directoryProvider;

    private IUnarchivingPreparation unarchivingPreparation = new IUnarchivingPreparation()
        {
            @Override
            public void prepareForUnarchiving(List<DatasetDescription> dataSet)
            {
            }
        };

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    public ArchiverTaskContext(IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentProvider hierarchicalContentProvider)
    {
        this.directoryProvider = directoryProvider;
        this.hierarchicalContentProvider = hierarchicalContentProvider;
    }

    public IDataSetDirectoryProvider getDirectoryProvider()
    {
        return directoryProvider;
    }

    public IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return hierarchicalContentProvider;
    }

    public void setUnarchivingPreparation(IUnarchivingPreparation unarchivingPreparation)
    {
        if (unarchivingPreparation == null)
        {
            throw new IllegalArgumentException("Unspecified unarchiving preparation object.");
        }
        this.unarchivingPreparation = unarchivingPreparation;
    }

    public IUnarchivingPreparation getUnarchivingPreparation()
    {
        return unarchivingPreparation;
    }

}
