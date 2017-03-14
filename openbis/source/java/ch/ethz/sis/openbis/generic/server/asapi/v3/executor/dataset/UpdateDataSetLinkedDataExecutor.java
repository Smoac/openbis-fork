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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ContentCopyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IContentCopyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.LinkedDataUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetLinkedDataExecutor implements IUpdateDataSetLinkedDataExecutor
{

    @Autowired
    private IUpdateDataSetExternalDmsExecutor updateDataSetExternalDmsExecutor;

    @Autowired
    private IAddContentCopiesToLinkedDataExecutor addContentCopiesToLinkedDataExecutor;

    @Autowired
    protected IDAOFactory daoFactory;

    @Override
    public void update(IOperationContext context, MapBatch<DataSetUpdate, DataPE> batch)
    {
        for (Map.Entry<DataSetUpdate, DataPE> entry : batch.getObjects().entrySet())
        {
            DataSetUpdate update = entry.getKey();
            DataPE entity = entry.getValue();

            if (entity instanceof LinkDataPE && update.getLinkedData() != null && update.getLinkedData().isModified())
            {
                update(context, update.getLinkedData().getValue(), (LinkDataPE) entity);
            }
        }
        updateDataSetExternalDmsExecutor.update(context, batch);
    }

    private void update(IOperationContext context, LinkedDataUpdate update, LinkDataPE entity)
    {
        if (update.getExternalCode() != null && update.getExternalCode().isModified())
        {
            Set<ContentCopyPE> contentCopies = entity.getContentCopies();
            if (contentCopies.size() == 1)
            {
                ContentCopyPE next = contentCopies.iterator().next();
                if (next.getExternalCode() != null)
                {
                    next.setExternalCode(update.getExternalCode().getValue());
                } else
                {
                    throw new UserFailureException("Cannot set external code to content copy of type" + next.getLocationType());
                }
            } else
            {
                throw new UserFailureException("Cannot set external code to linked dataset with multiple or zero copies");
            }
        }

        Collection<ContentCopyCreation> set = update.getContentCopies().getSet();
        if (set.isEmpty() == false)
        {
            for (ContentCopyPE cc : entity.getContentCopies())
            {
                daoFactory.getSessionFactory().getCurrentSession().delete(cc);
            }
            entity.getContentCopies().removeAll(entity.getContentCopies());

            addContentCopiesToLinkedDataExecutor.add(context, entity, set);

        } else
        {
            Collection<IContentCopyId> removed = update.getContentCopies().getRemoved();
            Set<ContentCopyPE> remove = new HashSet<ContentCopyPE>();

            for (ContentCopyPE cc : entity.getContentCopies())
            {
                if (removed.contains(new ContentCopyPermId(cc.getId().toString())))
                {
                    remove.add(cc);
                }
            }

            entity.getContentCopies().removeAll(remove);
            for (ContentCopyPE cc : remove)
            {
                daoFactory.getSessionFactory().getCurrentSession().delete(cc);
            }

            addContentCopiesToLinkedDataExecutor.add(context, entity, update.getContentCopies().getAdded());
        }
    }
}
