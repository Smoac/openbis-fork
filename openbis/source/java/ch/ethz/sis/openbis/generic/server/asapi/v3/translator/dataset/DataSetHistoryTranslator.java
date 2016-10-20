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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationType;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class DataSetHistoryTranslator extends HistoryTranslator implements IDataSetHistoryTranslator
{

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected RelationHistoryEntry createRelationshipEntry(HistoryRelationshipRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = super.createRelationshipEntry(record, authorMap, fetchOptions);

        DataSetRelationshipRecord dataSetRecord = (DataSetRelationshipRecord) record;

        if (dataSetRecord.experimentId != null)
        {
            entry.setRelationType(DataSetRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(dataSetRecord.relatedObjectId));
        } else if (dataSetRecord.sampleId != null)
        {
            entry.setRelationType(DataSetRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(dataSetRecord.relatedObjectId));
        } else if (dataSetRecord.dataSetId != null)
        {
            RelationType relationType = RelationType.valueOf(dataSetRecord.relationType);

            switch (relationType)
            {
                case PARENT:
                    entry.setRelationType(DataSetRelationType.CHILD);
                    break;
                case CHILD:
                    entry.setRelationType(DataSetRelationType.PARENT);
                    break;
                case CONTAINER:
                    entry.setRelationType(DataSetRelationType.COMPONENT);
                    break;
                case CONTAINED:
                case COMPONENT:
                    entry.setRelationType(DataSetRelationType.CONTAINER);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported relation type: " + relationType);
            }
            entry.setRelatedObjectId(new DataSetPermId(dataSetRecord.relatedObjectId));
        }

        return entry;
    }

}
