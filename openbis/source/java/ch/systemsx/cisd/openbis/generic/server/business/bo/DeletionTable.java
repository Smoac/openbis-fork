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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedSamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DeletionTranslator;

/**
 * Business object implementing {@link IDeletionTable}.
 * 
 * @author Franz-Josef Elmer
 */
public class DeletionTable extends AbstractBusinessObject implements IDeletionTable
{
    private List<Deletion> deletions;

    public DeletionTable(IDAOFactory daoFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory);
    }

    @Override
    public List<Deletion> getDeletions()
    {
        return deletions;
    }

    @Override
    public void load(boolean withEntities)
    {
        final List<DeletionPE> deletionPEs = getDeletionDAO().listAllEntities();
        Collections.sort(deletionPEs);
        deletions = DeletionTranslator.translate(deletionPEs);
        if (false == withEntities)
        {
            return;
        }
        Map<Long, RootEntitiesFinder> findersMap = new HashMap<Long, RootEntitiesFinder>();
        for (DeletionPE deletionPE : deletionPEs)
        {
            findersMap.put(deletionPE.getId(), new RootEntitiesFinder());
        }
        List<TechId> deletionIDs = TechId.createList(deletionPEs);
        if (false == deletionIDs.isEmpty())
        {
            findExperiments(findersMap, deletionIDs);
            findSamples(findersMap, deletionIDs);
            findDataSets(findersMap, deletionIDs);
        }
        for (Deletion deletion : deletions)
        {
            findersMap.get(deletion.getId()).addRootEntitiesTo(deletion);
        }
    }

    private void findDataSets(Map<Long, RootEntitiesFinder> findersMap, List<TechId> deletionIDs)
    {
        IDeletionDAO deletionDAO = getDeletionDAO();
        List<TechId> deletedDataSetIds = deletionDAO.findTrashedDataSetIds(deletionIDs);
        List<DeletedDataPE> dataSets =
                cast(getDeletionDAO().listDeletedEntities(EntityKind.DATA_SET, deletedDataSetIds));
        addEntities(dataSets, findersMap);
    }

    private void findSamples(Map<Long, RootEntitiesFinder> findersMap, List<TechId> deletionIDs)
    {
        IDeletionDAO deletionDAO = getDeletionDAO();
        List<TechId> deletedSampleIds = deletionDAO.findTrashedSampleIds(deletionIDs);
        List<DeletedSamplePE> samples =
                cast(getDeletionDAO().listDeletedEntities(EntityKind.SAMPLE, deletedSampleIds));
        addEntities(samples, findersMap);
    }

    private void findExperiments(Map<Long, RootEntitiesFinder> findersMap, List<TechId> deletionIDs)
    {
        IDeletionDAO deletionDAO = getDeletionDAO();
        List<TechId> deletedExperimentIds = deletionDAO.findTrashedExperimentIds(deletionIDs);
        List<DeletedExperimentPE> experiments =
                cast(getDeletionDAO().listDeletedEntities(EntityKind.EXPERIMENT,
                        deletedExperimentIds));
        addEntities(experiments, findersMap);
    }

    @SuppressWarnings("unchecked")
    private final static <T> T cast(final Object object)
    {
        return (T) object;
    }

    private void addEntities(List<? extends IDeletablePE> entities,
            Map<Long, RootEntitiesFinder> findersMap)
    {
        for (IDeletablePE entity : entities)
        {
            DeletionPE deletion = entity.getDeletion();
            if (deletion != null)
            {
                RootEntitiesFinder finder = findersMap.get(deletion.getId());
                if (finder != null)
                {
                    finder.addEntity(entity);
                }
            }
        }
    }

}
