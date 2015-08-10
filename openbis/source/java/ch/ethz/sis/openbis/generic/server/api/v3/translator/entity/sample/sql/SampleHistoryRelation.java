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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.history.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationType;

/**
 * @author pkupczyk
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleHistoryRelation extends HistoryRelation
{

    public SampleHistoryRelation(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        super(context, entityIds, fetchOptions);
    }

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(Collection<Long> entityIds)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        return query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected RelationHistoryEntry createRelationshipEntry(HistoryRelationshipRecord record, Map<Long, Person> authorMap)
    {
        RelationHistoryEntry entry = super.createRelationshipEntry(record, authorMap);

        SampleRelationshipRecord sampleRecord = (SampleRelationshipRecord) record;

        if (sampleRecord.spaceId != null)
        {
            entry.setRelationType(SampleRelationType.SPACE);
            entry.setRelatedObjectId(new SpacePermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.experimentId != null)
        {
            entry.setRelationType(SampleRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.sampleId != null)
        {
            RelationType relationType = RelationType.valueOf(sampleRecord.relationType);

            switch (relationType)
            {
                case PARENT:
                    entry.setRelationType(SampleRelationType.CHILD);
                    break;
                case CHILD:
                    entry.setRelationType(SampleRelationType.PARENT);
                    break;
                case CONTAINER:
                    entry.setRelationType(SampleRelationType.CONTAINED);
                    break;
                case CONTAINED:
                case COMPONENT:
                    entry.setRelationType(SampleRelationType.CONTAINER);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported relation type: " + relationType);
            }
            entry.setRelatedObjectId(new SamplePermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.dataSetId != null)
        {
            entry.setRelationType(SampleRelationType.DATA_SET);
            entry.setRelatedObjectId(new DataSetPermId(sampleRecord.relatedObjectId));
        }

        return entry;
    }

}
