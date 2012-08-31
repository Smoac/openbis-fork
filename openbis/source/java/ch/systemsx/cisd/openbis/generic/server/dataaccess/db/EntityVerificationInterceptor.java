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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityValidationCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
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

    Set<IEntityInformationWithPropertiesHolder> modifiedEntities;

    Set<IEntityInformationWithPropertiesHolder> newEntities;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            newEntity(entity);
        }
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            modifiedEntity(entity);
        }
        return false;
    }

    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        for (IEntityInformationWithPropertiesHolder entity : newEntities)
        {
            validateNewEntity(tx, entity);
        }

        for (IEntityInformationWithPropertiesHolder entity : modifiedEntities)
        {
            validateModifiedEntity(tx, entity);
        }
    }

    private void validateModifiedEntity(Transaction tx,
            IEntityInformationWithPropertiesHolder entity)
    {
        validateEntity(tx, entity, false);
    }

    private void validateNewEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity)
    {
        validateEntity(tx, entity, true);
    }

    private void validateEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            boolean isNewEntity)
    {
        ScriptPE validationScript = entity.getEntityType().getValidationScript();
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
        return entity.getEntityKind().getLabel() + " " + entity.getIdentifier() + " ("
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
        newEntities.add((IEntityInformationWithPropertiesHolder) entity);
    }

    private void modifiedEntity(Object entity)
    {
        addModifiedEntityToSet((IEntityInformationWithPropertiesHolder) entity, modifiedEntities,
                newEntities);
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
        modifiedEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
        newEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
    }

}
