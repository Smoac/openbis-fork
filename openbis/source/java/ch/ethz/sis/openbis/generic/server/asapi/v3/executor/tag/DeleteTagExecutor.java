/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.deletion.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IReindexEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagAuthorization;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMetaprojectBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteTagExecutor extends AbstractDeleteEntityExecutor<Void, ITagId, MetaprojectPE, TagDeletionOptions> implements
        IDeleteTagExecutor
{

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Autowired
    private IReindexEntityExecutor reindexObjectExecutor;

    @Override
    protected Map<ITagId, MetaprojectPE> map(IOperationContext context, List<? extends ITagId> entityIds)
    {
        return mapTagByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, ITagId entityId, MetaprojectPE entity)
    {
        new TagAuthorization(context).checkAccess(entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, MetaprojectPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<MetaprojectPE> tags, TagDeletionOptions deletionOptions)
    {
        Collection<ExperimentPE> experiments = new HashSet<ExperimentPE>();
        Collection<SamplePE> samples = new HashSet<SamplePE>();
        Collection<DataPE> dataSets = new HashSet<DataPE>();
        Collection<MaterialPE> materials = new HashSet<MaterialPE>();

        for (MetaprojectPE tag : tags)
        {
            Set<MetaprojectAssignmentPE> assignments = tag.getAssignments();

            if (assignments != null)
            {
                for (MetaprojectAssignmentPE assignment : assignments)
                {
                    if (assignment.getExperiment() != null)
                    {
                        experiments.add(assignment.getExperiment());
                    } else if (assignment.getSample() != null)
                    {
                        samples.add(assignment.getSample());
                    } else if (assignment.getDataSet() != null)
                    {
                        dataSets.add(assignment.getDataSet());
                    } else if (assignment.getMaterial() != null)
                    {
                        materials.add(assignment.getMaterial());
                    }
                }
            }

            IMetaprojectBO metaprojectBO = businessObjectFactory.createMetaprojectBO(context.getSession());
            metaprojectBO.deleteByMetaprojectId(new MetaprojectTechIdId(tag.getId()), deletionOptions.getReason());
        }

        reindexObjectExecutor.reindex(context, ExperimentPE.class, experiments);
        reindexObjectExecutor.reindex(context, SamplePE.class, samples);
        reindexObjectExecutor.reindex(context, DataPE.class, dataSets);
        reindexObjectExecutor.reindex(context, MaterialPE.class, materials);

        return null;
    }

}
