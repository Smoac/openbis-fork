/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateExperimentExecutor implements ICreateExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetExperimentTypeExecutor setExperimentTypeExecutor;

    @Autowired
    private ISetExperimentProjectExecutor setExperimentProjectExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifyExperimentExecutor verifyExperimentExecutor;

    @SuppressWarnings("unused")
    private CreateExperimentExecutor()
    {
    }

    public CreateExperimentExecutor(IDAOFactory daoFactory, ISetExperimentTypeExecutor setExperimentTypeExecutor,
            ISetExperimentProjectExecutor setExperimentProjectExecutor, IUpdateEntityPropertyExecutor updateEntityPropertyExecutor,
            ICreateAttachmentExecutor createAttachmentExecutor, IAddTagToEntityExecutor addTagToEntityExecutor,
            IVerifyExperimentExecutor verifyExperimentExecutor)
    {
        this.daoFactory = daoFactory;
        this.setExperimentTypeExecutor = setExperimentTypeExecutor;
        this.setExperimentProjectExecutor = setExperimentProjectExecutor;
        this.updateEntityPropertyExecutor = updateEntityPropertyExecutor;
        this.createAttachmentExecutor = createAttachmentExecutor;
        this.addTagToEntityExecutor = addTagToEntityExecutor;
        this.verifyExperimentExecutor = verifyExperimentExecutor;
    }

    @Override
    public List<ExperimentPermId> create(IOperationContext context, List<ExperimentCreation> creations)
    {
        try
        {
            List<ExperimentPermId> permIdsAll = new LinkedList<ExperimentPermId>();
            Map<ExperimentCreation, ExperimentPE> experimentsAll = new LinkedHashMap<ExperimentCreation, ExperimentPE>();

            int batchSize = 1000;
            for (int batchStart = 0; batchStart < creations.size(); batchStart += batchSize)
            {
                List<ExperimentCreation> creationsBatch = creations.subList(batchStart, Math.min(batchStart + batchSize, creations.size()));
                createExperiments(context, creationsBatch, permIdsAll, experimentsAll);
            }

            reloadExperiments(experimentsAll);

            for (ExperimentCreation creation : creations)
            {
                ExperimentPE experiment = experimentsAll.get(creation);
                createAttachmentExecutor.create(context, experiment, creation.getAttachments());
                addTagToEntityExecutor.add(context, experiment, creation.getTagIds());
            }

            verifyExperimentExecutor.verify(context, experimentsAll.values());

            daoFactory.getSessionFactory().getCurrentSession().flush();
            daoFactory.getSessionFactory().getCurrentSession().clear();
            return permIdsAll;
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "Experiment", EntityKind.EXPERIMENT);
            return null;
        }
    }

    private void createExperiments(IOperationContext context, List<ExperimentCreation> creationsBatch,
            List<ExperimentPermId> permIdsAll, Map<ExperimentCreation, ExperimentPE> experimentsAll)
    {
        Map<ExperimentCreation, ExperimentPE> batchMap = new LinkedHashMap<ExperimentCreation, ExperimentPE>();

        daoFactory.setBatchUpdateMode(true);

        for (ExperimentCreation creation : creationsBatch)
        {
            context.pushContextDescription("register experiment " + creation.getCode());

            ExperimentPE experiment = createExperimentPE(context, creation);

            permIdsAll.add(new ExperimentPermId(experiment.getPermId()));
            experimentsAll.put(creation, experiment);
            batchMap.put(creation, experiment);

            context.popContextDescription();
        }

        setExperimentProjectExecutor.set(context, batchMap);
        setExperimentTypeExecutor.set(context, batchMap);

        Map<IEntityPropertiesHolder, Map<String, String>> entityToPropertiesMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();

        for (Map.Entry<ExperimentCreation, ExperimentPE> batchEntry : batchMap.entrySet())
        {
            ExperimentPE experiment = batchEntry.getValue();

            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
            {
                throw new UnauthorizedObjectAccessException(new ExperimentIdentifier(experiment.getIdentifier()));
            }

            entityToPropertiesMap.put(experiment, batchEntry.getKey().getProperties());
        }

        updateEntityPropertyExecutor.update(context, entityToPropertiesMap);

        PersonPE modifier = context.getSession().tryGetPerson();
        daoFactory.getExperimentDAO().createOrUpdateExperiments(new ArrayList<ExperimentPE>(batchMap.values()), modifier);

        daoFactory.setBatchUpdateMode(false);
        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();
    }

    private ExperimentPE createExperimentPE(IOperationContext context, ExperimentCreation experimentCreation)
    {
        if (StringUtils.isEmpty(experimentCreation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        ExperimentIdentifierFactory.assertValidCode(experimentCreation.getCode());

        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(experimentCreation.getCode());
        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        experiment.setPermId(createdPermId);
        experiment.setRegistrator(context.getSession().tryGetPerson());
        RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession().tryGetPerson());

        return experiment;
    }

    private void reloadExperiments(Map<ExperimentCreation, ExperimentPE> creationToExperimentMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (ExperimentPE experiment : creationToExperimentMap.values())
        {
            ids.add(experiment.getId());
        }

        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByIDs(ids);

        Map<Long, ExperimentPE> idToExperimentMap = new HashMap<Long, ExperimentPE>();

        for (ExperimentPE experiment : experiments)
        {
            idToExperimentMap.put(experiment.getId(), experiment);
        }

        for (Map.Entry<ExperimentCreation, ExperimentPE> entry : creationToExperimentMap.entrySet())
        {
            entry.setValue(idToExperimentMap.get(entry.getValue().getId()));
        }

    }

}
