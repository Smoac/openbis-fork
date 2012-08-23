/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.EntityValidationCalculator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * {@link Interceptor} which reacts to creation and update of entities, and calls the validation
 * script. It is coupled with {@link OpenBISHibernateTransactionManager} with the callback object,
 * as the only way to cancel transaction from this interceptor is to rollback hibernate transaction
 * object. In order to fail the transaction with a {@link UserFailureException} we have to provide
 * information
 * 
 * @author Jakub Straszewski
 */
public class EntityVerificationInterceptor extends EmptyInterceptor
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IDAOFactory daoFactory;

    public EntityVerificationInterceptor(IHibernateTransactionManagerCallback callback,
            IDAOFactory daoFactory)
    {
        this.callback = callback;
        this.daoFactory = daoFactory;
        initializeLists();
    }

    IHibernateTransactionManagerCallback callback;

    Set<SamplePE> modifiedSamples;

    Set<MaterialPE> modifiedMaterials;

    Set<ExperimentPE> modifiedExperiments;

    Set<DataPE> modifiedDatasets;

    Set<SamplePE> newSamples;

    Set<MaterialPE> newMaterials;

    Set<ExperimentPE> newExperiments;

    Set<DataPE> newDatasets;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        newEntity(entity);
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        modifiedEntity(entity);
        return false;
    }

    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        for (SamplePE sample : newSamples)
        {
            validateNewEntity(tx, sample, sample.getSampleType());
        }

        for (DataPE dataset : newDatasets)
        {
            validateNewEntity(tx, dataset, dataset.getDataSetType());
        }

        for (MaterialPE material : newMaterials)
        {
            validateNewEntity(tx, material, material.getMaterialType());
        }

        for (ExperimentPE experiment : newExperiments)
        {
            validateNewEntity(tx, experiment, experiment.getEntityType());
        }

        for (SamplePE sample : modifiedSamples)
        {
            validateModifiedEntity(tx, sample, sample.getSampleType());
        }

        for (DataPE dataset : modifiedDatasets)
        {
            validateModifiedEntity(tx, dataset, dataset.getDataSetType());
        }

        for (MaterialPE material : modifiedMaterials)
        {
            validateModifiedEntity(tx, material, material.getMaterialType());
        }

        for (ExperimentPE experiment : modifiedExperiments)
        {
            validateModifiedEntity(tx, experiment, experiment.getEntityType());
        }
    }

    private void validateModifiedEntity(Transaction tx,
            IEntityInformationWithPropertiesHolder entity, EntityTypePE entityType)
    {
        validateEntity(tx, entity, entityType, false);
    }

    private void validateNewEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            EntityTypePE entityType)
    {
        validateEntity(tx, entity, entityType, true);
    }

    private void validateEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            EntityTypePE entityType, boolean isNewEntity)
    {
        ScriptPE validationScript = entityType.getValidationScript();
        if (validationScript != null)
        {
            validateEntityWithScript(tx, validationScript, entity, isNewEntity);
        }
    }

    private void validateEntityWithScript(Transaction tx, ScriptPE script,
            IEntityInformationWithPropertiesHolder entity, boolean isNewEntity)
    {
        String result = null;

        try
        {
            result = calculate(script, entity, isNewEntity);
        } catch (Throwable e)
        {
            callback.rollbackTransaction(tx, "Validation of " + entityDescription(entity)
                    + " resulted in error. " + e.getMessage());
            e.printStackTrace();
        }
        if (result != null)
        {
            callback.rollbackTransaction(tx, "Validation of " + entityDescription(entity)
                    + " failed. " + result);
        }

    }

    private String entityDescription(IEntityInformationWithPropertiesHolder entity)
    {
        return entity.getEntityKind().getLabel() + " " + entity.getCode() + " ("
                + entity.getEntityType().getCode() + ")";
    }

    private String calculate(ScriptPE script, IEntityInformationWithPropertiesHolder entity,
            boolean isNewEntity)
    {
        EntityValidationCalculator calculator =
                EntityValidationCalculator.create(script.getScript());
        IDynamicPropertyEvaluator evaluator = new DynamicPropertyEvaluator(daoFactory, null);
        IEntityAdaptor adaptor = EntityAdaptorFactory.create(entity, evaluator);
        calculator.setEntity(adaptor);
        calculator.setIsNewEntity(isNewEntity);
        return calculator.evalAsString();
    }

    private void newEntity(Object entity)
    {
        if (entity instanceof SamplePE)
        {
            newSamples.add((SamplePE) entity);
        } else if (entity instanceof ExperimentPE)
        {
            newExperiments.add((ExperimentPE) entity);
        } else if (entity instanceof MaterialPE)
        {
            newMaterials.add((MaterialPE) entity);
        } else if (entity instanceof DataPE)
        {
            newDatasets.add((DataPE) entity);
        }
    }

    private void modifiedEntity(Object entity)
    {
        if (entity instanceof SamplePE)
        {
            addModifiedEntityToSet((SamplePE) entity, modifiedSamples, newSamples);
        } else if (entity instanceof ExperimentPE)
        {
            addModifiedEntityToSet((ExperimentPE) entity, modifiedExperiments, newExperiments);
        } else if (entity instanceof MaterialPE)
        {
            addModifiedEntityToSet((MaterialPE) entity, modifiedMaterials, newMaterials);
        } else if (entity instanceof DataPE)
        {
            addModifiedEntityToSet((DataPE) entity, modifiedDatasets, newDatasets);
        }
    }

    private <T> void addModifiedEntityToSet(T entity, Set<T> modifiedSet, Set<T> newSet)
    {
        if (false == newSet.contains(entity))
        {
            modifiedSet.add(entity);
        }
    }

    private void initializeLists()
    {
        modifiedSamples = new HashSet<SamplePE>();
        modifiedExperiments = new HashSet<ExperimentPE>();
        modifiedMaterials = new HashSet<MaterialPE>();
        modifiedDatasets = new HashSet<DataPE>();

        newSamples = new HashSet<SamplePE>();
        newExperiments = new HashSet<ExperimentPE>();
        newMaterials = new HashSet<MaterialPE>();
        newDatasets = new HashSet<DataPE>();
    }

}
