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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CheckDataProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateDataSetExecutor extends AbstractCreateEntityExecutor<DataSetCreation, DataPE, DataSetPermId> implements ICreateDataSetExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private ISetDataSetPhysicalDataExecutor setDataSetPhysicalDataExecutor;

    @Autowired
    private ISetDataSetLinkedDataExecutor setDataSetLinkedDataExecutor;

    @Autowired
    private ISetDataSetDataStoreExecutor setDataSetDataStoreExecutor;

    @Autowired
    private ISetDataSetExperimentExecutor setDataSetExperimentExecutor;

    @Autowired
    private ISetDataSetSampleExecutor setDataSetSampleExecutor;

    @Autowired
    private ISetDataSetRelatedDataSetsExecutor setDataSetRelatedDataSetsExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifyDataSetExecutor verifyDataSetExecutor;

    @Override
    protected List<DataPE> createEntities(final IOperationContext context, CollectionBatch<DataSetCreation> batch)
    {
        // Get Types
        Collection<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

        for (DataSetCreation creation : batch.getObjects())
        {
            typeIds.add(creation.getTypeId());
        }

        final Map<IEntityTypeId, EntityTypePE> types = mapEntityTypeByIdExecutor.map(context, EntityKind.DATA_SET, typeIds);

        // Validate DataSet creations
        new CollectionBatchProcessor<DataSetCreation>(context, batch)
            {
                @Override
                public void process(DataSetCreation object)
                {
                    checkData(context, object, types.get(object.getTypeId()));
                }

                @Override
                public IProgress createProgress(DataSetCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CheckDataProgress(object, objectIndex, totalObjectCount);
                }
            };

        IPermIdDAO codeGenerator = daoFactory.getPermIdDAO();

        List<DataPE> dataSets = new LinkedList<DataPE>();
        for (DataSetCreation creation : batch.getObjects())
        {
            DataSetTypePE type = (DataSetTypePE) types.get(creation.getTypeId());

            // Create code if is not present
            if (StringUtils.isEmpty(creation.getCode()))
            {
                creation.setCode(codeGenerator.createPermId());
            }

            DataSetKind kind = DataSetKind.valueOf(type.getDataSetKind());
            DataPE dataSet = null;

            if (DataSetKind.PHYSICAL.equals(kind))
            {
                dataSet = new ExternalDataPE();
            } else if (DataSetKind.CONTAINER.equals(kind))
            {
                dataSet = new DataPE();
            } else if (DataSetKind.LINK.equals(kind))
            {
                dataSet = new LinkDataPE();
            } else
            {
                throw new IllegalArgumentException("Unsupported data set kind: " + kind);
            }

            dataSet.setCode(creation.getCode());
            dataSet.setDataSetType(type);
            dataSet.setDerived(false == creation.isMeasured());
            dataSet.setDataProducerCode(creation.getDataProducer());
            dataSet.setProductionDate(creation.getDataProductionDate());

            PersonPE person = context.getSession().tryGetPerson();
            dataSet.setRegistrator(person);
            Date timeStamp = daoFactory.getTransactionTimestamp();
            RelationshipUtils.updateModificationDateAndModifier(dataSet, person, timeStamp);

            dataSets.add(dataSet);
        }

        return dataSets;
    }

    private void checkData(IOperationContext context, DataSetCreation creation, EntityTypePE type)
    {
        if (type == null)
        {
            throw new ObjectNotFoundException(creation.getTypeId());
        } else if (StringUtils.isEmpty(creation.getCode()) && false == creation.isAutoGeneratedCode())
        {
            throw new UserFailureException("Code cannot be empty for a non auto generated code.");
        } else if (false == StringUtils.isEmpty(creation.getCode()) && creation.isAutoGeneratedCode())
        {
            throw new UserFailureException("Code should be empty when auto generated code is selected.");
        }
    }

    @Override
    protected void checkData(IOperationContext context, DataSetCreation creation)
    {
        if (creation.getTypeId() == null)
        {
            throw new UserFailureException("Type id cannot be null.");
        }
        if (creation.getExperimentId() == null && creation.getSampleId() == null)
        {
            throw new UserFailureException("Experiment id and sample id cannot be both null.");
        }
    }

    @Override
    protected DataSetPermId createPermId(IOperationContext context, DataPE entity)
    {
        return new DataSetPermId(entity.getPermId());
    }

    @Override
    protected void checkAccess(IOperationContext context, DataPE entity)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(entity.getRegistrator(), entity))
        {
            throw new UnauthorizedObjectAccessException(new DataSetPermId(entity.getPermId()));
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, CollectionBatch<DataPE> batch)
    {
        verifyDataSetExecutor.verify(context, batch);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<DataSetCreation, DataPE> batch)
    {
        setDataSetPhysicalDataExecutor.set(context, batch);
        setDataSetLinkedDataExecutor.set(context, batch);
        setDataSetDataStoreExecutor.set(context, batch);
        setDataSetSampleExecutor.set(context, batch);
        setDataSetExperimentExecutor.set(context, batch);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<DataSetCreation, DataPE> entry : batch.getObjects().entrySet())
        {
            propertyMap.put(entry.getValue(), entry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<DataSetCreation, DataPE> batch)
    {
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<DataSetCreation, DataPE> entry : batch.getObjects().entrySet())
        {
            DataSetCreation creation = entry.getKey();
            DataPE entity = entry.getValue();
            tagMap.put(entity, creation.getTagIds());
        }

        addTagToEntityExecutor.add(context, tagMap);
        setDataSetRelatedDataSetsExecutor.set(context, batch);
    }

    @Override
    protected List<DataPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getDataDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<DataPE> entities, boolean clearCache)
    {
        daoFactory.getDataDAO().createDataSets(entities, context.getSession().tryGetPerson());
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.DATA_SET.getLabel(), EntityKind.DATA_SET);
    }

}
