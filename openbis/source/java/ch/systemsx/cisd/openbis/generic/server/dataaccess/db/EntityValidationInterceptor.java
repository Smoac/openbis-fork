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
import java.util.LinkedList;
import java.util.Queue;
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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityValidationCalculator.IValidationRequestDelegate;
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
public class EntityValidationInterceptor extends EmptyInterceptor implements
        IValidationRequestDelegate
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IDAOFactory daoFactory;

    public EntityValidationInterceptor(IHibernateTransactionManagerCallback callback,
            IDAOFactory daoFactory)
    {
        this.callback = callback;
        this.daoFactory = daoFactory;
        initializeLists();
    }

    IHibernateTransactionManagerCallback callback;

    /*
     * During the transaction the new objects are inserted into the newEntities and
     * mofidiedEntities. During the validation phase, first all new entities are validated, then the
     * remaining, which consists of a modified entities, and the entities which were explicitly
     * marked for validation by validation scripts. We keep the track of validated entities in the
     * set, and keep the entities we still have to validate in entitiesToValidate.
     */
    // TODO: refactor - there are too many collections here
    Set<IEntityInformationWithPropertiesHolder> modifiedEntities;

    Set<IEntityInformationWithPropertiesHolder> newEntities;

    Set<IEntityInformationWithPropertiesHolder> validatedEntities;

    Queue<IEntityInformationWithPropertiesHolder> entitiesToValidate;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
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
        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            modifiedEntity((IEntityInformationWithPropertiesHolder) entity);
        }
        return false;
    }

    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        validateNewEntities(tx);

        for (IEntityInformationWithPropertiesHolder entity : modifiedEntities)
        {
            entitiesToValidate.add(entity);
        }

        while (entitiesToValidate.size() > 0)
        {
            IEntityInformationWithPropertiesHolder entity = entitiesToValidate.remove();
            validateEntity(tx, entity, false);
        }

    }

    private void validateNewEntities(Transaction tx)
    {
        for (IEntityInformationWithPropertiesHolder entity : newEntities)
        {
            validateEntity(tx, entity, true);
        }
    }

    private void validateEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            boolean isNewEntity)
    {
        validatedEntity(entity);
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
                EntityValidationCalculator.create(script.getScript(), this);
        IDynamicPropertyEvaluator evaluator = new DynamicPropertyEvaluator(daoFactory, null);
        IEntityAdaptor adaptor = EntityAdaptorFactory.create(entity, evaluator);
        calculator.setEntity(adaptor);
        calculator.setIsNewEntity(isNewEntity);
        return calculator.evalAsString();
    }

    private void newEntity(IEntityInformationWithPropertiesHolder entity)
    {
        newEntities.add(entity);
    }

    private void validatedEntity(IEntityInformationWithPropertiesHolder entity)
    {
        validatedEntities.add(entity);
    }

    private void modifiedEntity(IEntityInformationWithPropertiesHolder entity)
    {
        if (false == newEntities.contains(entity))
        {
            modifiedEntities.add(entity);
        }
    }

    private void initializeLists()
    {
        modifiedEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
        newEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
        validatedEntities = new HashSet<IEntityInformationWithPropertiesHolder>();
        entitiesToValidate = new LinkedList<IEntityInformationWithPropertiesHolder>();
    }

    @Override
    public void requestValidation(Object entity)
    {
        if (validatedEntities.contains(entity) || newEntities.contains(entity)
                || modifiedEntities.contains(entity))
        {
            // forcing validation of entity already listed for validation
        } else
        {
            IEntityInformationWithPropertiesHolder typedEntity =
                    (IEntityInformationWithPropertiesHolder) entity;

            // we update modified entities to know that we will validate this entity
            modifiedEntities.add(typedEntity);
            // we add to the actual validation queue
            entitiesToValidate.add(typedEntity);
        }
    }
}
