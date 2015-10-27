/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.Complete;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.PhysicalDataCreation;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetPhysicalDataExecutor implements ISetDataSetPhysicalDataExecutor
{

    @Autowired
    private ISetDataSetStorageFormatExecutor setDataSetStorageFormatExecutor;

    @Autowired
    private ISetDataSetFileFormatTypeExecutor setDataSetFileFormatTypeExecutor;

    @Autowired
    private ISetDataSetLocatorTypeExecutor setDataSetLocatorTypeExecutor;

    @Override
    public void set(IOperationContext context, Map<DataSetCreation, DataPE> entitiesMap)
    {
        for (Map.Entry<DataSetCreation, DataPE> entry : entitiesMap.entrySet())
        {
            DataSetCreation creation = entry.getKey();
            DataPE entity = entry.getValue();

            if (entity instanceof ExternalDataPE)
            {
                set(context, creation, (ExternalDataPE) entity);
            }
        }

        setDataSetStorageFormatExecutor.set(context, entitiesMap);
        setDataSetFileFormatTypeExecutor.set(context, entitiesMap);
        setDataSetLocatorTypeExecutor.set(context, entitiesMap);
    }

    private void set(IOperationContext context, DataSetCreation creation, ExternalDataPE dataSet)
    {
        PhysicalDataCreation physicalCreation = creation.getPhysicalData();

        if (physicalCreation == null)
        {
            throw new IllegalArgumentException("Physical data cannot be null for a physical data set.");
        }

        dataSet.setShareId(physicalCreation.getShareId());
        dataSet.setLocation(physicalCreation.getLocation());
        dataSet.setSize(physicalCreation.getSize());
        if (physicalCreation.getSpeedHint() != null)
        {
            dataSet.setSpeedHint(physicalCreation.getSpeedHint());
        }

        BooleanOrUnknown complete = BooleanOrUnknown.U;
        if (Complete.YES.equals(physicalCreation.getComplete()))
        {
            complete = BooleanOrUnknown.T;
        } else if (Complete.NO.equals(physicalCreation.getComplete()))
        {
            complete = BooleanOrUnknown.F;
        }
        dataSet.setComplete(complete);
    }

}
