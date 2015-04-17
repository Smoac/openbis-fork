/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.history.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityHistoryDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleHistoryPE;

/**
 * @author pkupczyk
 */
@Component
public class HistoryTranslator extends AbstractCachingTranslator<IEntityInformationHolderDTO, List<HistoryEntry>, HistoryEntryFetchOptions> implements
        IHistoryTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IPersonTranslator personTranslator;

    @Override
    protected List<HistoryEntry> createObject(TranslationContext context, IEntityInformationHolderDTO entity, HistoryEntryFetchOptions fetchOptions)
    {
        IEntityHistoryDAO historyDAO = daoFactory.getEntityPropertyHistoryDAO();
        List<AbstractEntityPropertyHistoryPE> peEntries = historyDAO.getPropertyHistory(entity.getEntityKind(), new TechId(entity.getId()));
        List<HistoryEntry> entries = new ArrayList<HistoryEntry>();

        for (AbstractEntityPropertyHistoryPE peEntry : peEntries)
        {
            HistoryEntry entry = null;

            if (peEntry.getEntityTypePropertyType() == null)
            {
                if (peEntry instanceof ExperimentHistoryPE)
                {
                    entry = createRelationEntry(context, (ExperimentHistoryPE) peEntry, fetchOptions);
                } else if (peEntry instanceof SampleHistoryPE)
                {
                    entry = createRelationEntry(context, (SampleHistoryPE) peEntry, fetchOptions);
                } else if (peEntry instanceof DataSetHistoryPE)
                {
                    entry = createRelationEntry(context, (DataSetHistoryPE) peEntry, fetchOptions);
                } else
                {
                    throw new IllegalArgumentException("Unsupported entity history: " + peEntry.getClass().getName());
                }
            } else
            {
                entry = createPropertyEntry(context, peEntry, fetchOptions);
            }

            if (entry != null)
            {
                entry.setFetchOptions(new HistoryEntryFetchOptions());
                entry.setValidFrom(peEntry.getValidFromDate());
                entry.setValidTo(peEntry.getValidUntilDate());

                if (fetchOptions.hasAuthor())
                {
                    entry.setAuthor(personTranslator.translate(context, peEntry.getAuthor(), fetchOptions.withAuthor()));
                    entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
                }

                entries.add(entry);
            }
        }

        return entries;
    }

    private HistoryEntry createRelationEntry(TranslationContext context, ExperimentHistoryPE history, HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = new RelationHistoryEntry();

        if (history.getProject() != null)
        {
            entry.setRelationType(ExperimentRelationType.PROJECT);
            entry.setRelatedObjectId(new ProjectPermId(history.getProject().getPermId()));
        } else if (history.getSample() != null)
        {
            entry.setRelationType(ExperimentRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(history.getSample().getPermId()));
        } else if (history.getDataSet() != null)
        {
            entry.setRelationType(ExperimentRelationType.DATA_SET);
            entry.setRelatedObjectId(new DataSetPermId(history.getDataSet().getCode()));
        } else
        {
            entry = null;
        }

        return entry;
    }

    private HistoryEntry createRelationEntry(TranslationContext context, SampleHistoryPE history, HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = new RelationHistoryEntry();

        if (history.getSpace() != null)
        {
            entry.setRelationType(SampleRelationType.SPACE);
            entry.setRelatedObjectId(new SpacePermId(history.getSpace().getCode()));
        } else if (history.getExperiment() != null)
        {
            entry.setRelationType(SampleRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(history.getExperiment().getPermId()));
        } else if (history.getSample() != null)
        {
            switch (history.getRelationType())
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
                    throw new IllegalArgumentException("Unsupported relation type: " + history.getRelationType());
            }
            entry.setRelatedObjectId(new SamplePermId(history.getSample().getPermId()));
        } else if (history.getDataSet() != null)
        {
            entry.setRelationType(SampleRelationType.DATA_SET);
            entry.setRelatedObjectId(new DataSetPermId(history.getDataSet().getCode()));
        } else
        {
            entry = null;
        }

        return entry;
    }

    private HistoryEntry createRelationEntry(TranslationContext context, DataSetHistoryPE history, HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = new RelationHistoryEntry();

        if (history.getExperiment() != null)
        {
            entry.setRelationType(DataSetRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(history.getExperiment().getPermId()));
        } else if (history.getSample() != null)
        {
            entry.setRelationType(DataSetRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(history.getSample().getPermId()));
        } else if (history.getDataSet() != null)
        {
            switch (history.getRelationType())
            {
                case PARENT:
                    entry.setRelationType(DataSetRelationType.CHILD);
                    break;
                case CHILD:
                    entry.setRelationType(DataSetRelationType.PARENT);
                    break;
                case CONTAINER:
                    entry.setRelationType(DataSetRelationType.CONTAINED);
                    break;
                case CONTAINED:
                case COMPONENT:
                    entry.setRelationType(DataSetRelationType.CONTAINER);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported relation type: " + history.getRelationType());
            }
            entry.setRelatedObjectId(new DataSetPermId(history.getDataSet().getPermId()));
        } else
        {
            entry = null;
        }

        return entry;
    }

    private HistoryEntry createPropertyEntry(TranslationContext context, AbstractEntityPropertyHistoryPE peEntry,
            HistoryEntryFetchOptions fetchOptions)
    {
        PropertyHistoryEntry entry = new PropertyHistoryEntry();
        entry.setPropertyName(peEntry.getEntityTypePropertyType().getPropertyType().getCode());
        entry.setPropertyValue(peEntry.getValue());
        return entry;
    }

    @Override
    protected void updateObject(TranslationContext context, IEntityInformationHolderDTO entity, List<HistoryEntry> entries, Relations relations,
            HistoryEntryFetchOptions fetchOptions)
    {
    }
}
