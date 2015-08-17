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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistorySqlTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.history.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentHistoryTranslator extends HistorySqlTranslator
{

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        ExperimentQuery query = QueryTool.getManagedQuery(ExperimentQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(Collection<Long> entityIds)
    {
        ExperimentQuery query = QueryTool.getManagedQuery(ExperimentQuery.class);
        return query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected RelationHistoryEntry createRelationshipEntry(HistoryRelationshipRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = super.createRelationshipEntry(record, authorMap, fetchOptions);

        ExperimentRelationshipRecord experimentRecord = (ExperimentRelationshipRecord) record;

        if (experimentRecord.projectId != null)
        {
            entry.setRelationType(ExperimentRelationType.PROJECT);
            entry.setRelatedObjectId(new ProjectPermId(experimentRecord.relatedObjectId));
        } else if (experimentRecord.sampleId != null)
        {
            entry.setRelationType(ExperimentRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(experimentRecord.relatedObjectId));
        } else if (experimentRecord.dataSetId != null)
        {
            entry.setRelationType(ExperimentRelationType.DATA_SET);
            entry.setRelatedObjectId(new DataSetPermId(experimentRecord.relatedObjectId));
        }

        return entry;
    }

}
