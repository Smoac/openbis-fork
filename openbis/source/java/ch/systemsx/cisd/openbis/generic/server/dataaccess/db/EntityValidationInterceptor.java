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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.EntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * {@link Interceptor} which reacts to creation and update of entities, and calls the validation
 * script. It is coupled with {@link OpenBISHibernateTransactionManager} with the callback object,
 * as the only way to cancel transaction from this interceptor is to rollback hibernate transaction
 * object. In order to fail the transaction with a {@link UserFailureException} we have to provide
 * information
 * 
 * @author Jakub Straszewski
 */
public class EntityValidationInterceptor extends EmptyInterceptor implements
        IValidationRequestDelegate<INonAbstractEntityAdapter>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IDAOFactory daoFactory;

    public EntityValidationInterceptor(IHibernateTransactionManagerCallback callback,
            IDAOFactory daoFactory)
    {
        this.callback = callback;
        this.daoFactory = daoFactory;
        initializeLists();

        totalEntitiesToValidateCount = 0;

        entitiesValidatedCount = 0;

        isRolledBack = false;
    }

    IHibernateTransactionManagerCallback callback;

    /**
     * Used only to store information about whether some objects are new or not
     */
    Set<IEntityInformationWithPropertiesHolder> newEntities;

    /**
     * Keeps the list of all items that should be validated
     */
    LinkedHashSet<IEntityInformationWithPropertiesHolder> entitiesToValidate;

    /**
     * Keeps the list of all items that should be validated
     */
    Set<IEntityInformationWithPropertiesHolder> validatedEntities;

    /**
     * WE get information about progress listener, form the caller of updates. Luckily the
     * onFlushDirty and onSave hooks are executed in the same thread as the caller.
     * <p>
     * The beforeTransactionCompletionHook is called in the separate thread, therefore we persist
     * progress listener in a designated variable
     */
    IServiceConversationProgressListener progressListener;

    int totalEntitiesToValidateCount;

    int entitiesValidatedCount;

    /**
     * if true - than it means that at least one validation has failed, and we don't need to do any
     * additional validations
     */
    private boolean isRolledBack;

    private void updateListener()
    {
        progressListener = ServiceConversationsThreadContext.getProgressListener();
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        updateListener();

        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            newEntity((IEntityInformationWithPropertiesHolder) entity);
        }
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        updateListener();

        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            modifiedEntity((IEntityInformationWithPropertiesHolder) entity);
        }
        return false;
    }

    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        while (hasMoreItemsToValidate())
        {
            IEntityInformationWithPropertiesHolder entity = nextItemToValidate();
            validateEntity(tx, entity);
        }
    }

    private boolean hasMoreItemsToValidate()
    {
        return entitiesToValidate.size() > 0 && false == isRolledBack;
    }

    private IEntityInformationWithPropertiesHolder nextItemToValidate()
    {
        Iterator<IEntityInformationWithPropertiesHolder> iterator = entitiesToValidate.iterator();
        IEntityInformationWithPropertiesHolder entity = iterator.next();
        iterator.remove();
        return entity;
    }

    private void validateEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity)
    {
        if (newEntities.contains(entity))
        {
            validateEntity(tx, entity, true);
        } else
        {
            validateEntity(tx, entity, false);
        }
    }

    private void validateEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            boolean isNewEntity)
    {
        validatedEntity(entity);
        IEntityValidator entityValidator =
                EntityValidatorFactory.createEntityValidator(entity.getEntityType(), this);
        if (entityValidator != null)
        {
            IEntityInformationWithPropertiesHolder regained =
                    (IEntityInformationWithPropertiesHolder) daoFactory.getSessionFactory()
                            .getCurrentSession().get(entity.getClass(), entity.getId());
            validateEntityWithScript(tx, entityValidator, regained, isNewEntity);
        }
    }

    private void validateEntityWithScript(Transaction tx, IEntityValidator entityValidator,
            IEntityInformationWithPropertiesHolder entity, boolean isNewEntity)
    {
        String result = null;
        try
        {
            if (progressListener != null)
            {
                progressListener.update("Validation of entities", totalEntitiesToValidateCount,
                        entitiesValidatedCount);
            }
            result = calculate(entityValidator, entity, isNewEntity);
        } catch (Throwable e)
        {
            setRollback(tx, entity, " resulted in error. " + e.getMessage());
        }
        if (result != null)
        {
            setRollback(tx, entity, " failed. " + result);
        }
    }

    private void setRollback(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            String msg)
    {
        callback.rollbackTransaction(tx, "Validation of " + entityDescription(entity) + msg);
        isRolledBack = true;
    }

    private String entityDescription(IEntityInformationWithPropertiesHolder entity)
    {
        return entity.getEntityKind().getLabel() + " " + entity.getIdentifier() + " ("
                + entity.getEntityType().getCode() + ")";
    }

    private String calculate(IEntityValidator entityValidator,
            IEntityInformationWithPropertiesHolder entity, boolean isNewEntity)
    {
        IDynamicPropertyEvaluator evaluator = new DynamicPropertyEvaluator(daoFactory, null);
        IEntityAdaptor adaptor =
                EntityAdaptorFactory.create(entity, evaluator, daoFactory.getSessionFactory()
                        .getCurrentSession());

        return entityValidator.validate(adaptor, isNewEntity);
    }

    private void newEntity(IEntityInformationWithPropertiesHolder entity)
    {
        if (entitiesToValidate.add(entity))
        {
            newEntities.add(entity);
            totalEntitiesToValidateCount++;
        }
    }

    private void validatedEntity(IEntityInformationWithPropertiesHolder entity)
    {
        entitiesValidatedCount++;
        if (false == validatedEntities.add(entity))
        {
            throw new IllegalStateException(
                    "Programming error - trying to validate the same entity twice!");
        }
    }

    private void modifiedEntity(IEntityInformationWithPropertiesHolder entity)
    {
        if (entitiesToValidate.add(entity))
        {
            totalEntitiesToValidateCount++;
        }
    }

    private void initializeLists()
    {
        validatedEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
        newEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
        entitiesToValidate = new LinkedHashSet<IEntityInformationWithPropertiesHolder>();
    }

    @Override
    public void requestValidation(INonAbstractEntityAdapter entityAdapter)
    {
        IEntityInformationWithPropertiesHolder entity = entityAdapter.entityPE();
        if (validatedEntities.contains(entity) || entitiesToValidate.contains(entity))
        {
            // forcing validation of entity already listed for validation
        } else
        {
            entitiesToValidate.add(entity);
            totalEntitiesToValidateCount++;
        }
    }
}
