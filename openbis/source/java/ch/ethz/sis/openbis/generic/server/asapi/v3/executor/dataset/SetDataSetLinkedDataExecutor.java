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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.SetRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocationType;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetLinkedDataExecutor implements ISetDataSetLinkedDataExecutor
{

    @Autowired
    private ISetDataSetExternalDmsExecutor setDataSetExternalDmsExecutor;

    @Override
    public void set(final IOperationContext context, final MapBatch<DataSetCreation, DataPE> batch)
    {
        new MapBatchProcessor<DataSetCreation, DataPE>(context, batch)
            {
                @Override
                public void process(DataSetCreation creation, DataPE entity)
                {
                    LinkedDataCreation linkedCreation = creation.getLinkedData();

                    if (entity instanceof LinkDataPE)
                    {
                        if (linkedCreation == null)
                        {
                            throw new UserFailureException("Linked data cannot be null for a link data set.");
                        }
                        set(context, linkedCreation, (LinkDataPE) entity);
                    } else
                    {
                        if (linkedCreation != null)
                        {
                            throw new UserFailureException("Linked data cannot be set for a non-link data set.");
                        }
                    }
                }

                @Override
                public IProgress createProgress(DataSetCreation creation, DataPE entity, int objectIndex, int totalObjectCount)
                {
                    return new SetRelationProgress(entity, creation, "dataset-linkeddata", objectIndex, totalObjectCount);
                }
            };

        setDataSetExternalDmsExecutor.set(context, batch);
    }

    private void set(IOperationContext context, LinkedDataCreation linkedCreation, LinkDataPE dataSet)
    {
        Set<ContentCopyPE> contentCopies = dataSet.getContentCopies();

        if (contentCopies == null)
        {
            ContentCopyPE copy = new ContentCopyPE();
            copy.setDataSet(dataSet);
            copy.setLocationType(LocationType.URL);
            contentCopies = new HashSet<>();
            contentCopies.add(copy);
            dataSet.setContentCopies(contentCopies);
        }

        if (contentCopies.size() == 1)
        {
            ContentCopyPE next = contentCopies.iterator().next();
            if (linkedCreation.getExternalCode() != null)
            {
                next.setExternalCode(linkedCreation.getExternalCode());
            } else
            {
                throw new UserFailureException("External code can not be null.");
            }
        } else
        {
            throw new UserFailureException("Cannot set external code to linked dataset with multiple or zero copies");
        }

    }

}
