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
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Jakub Straszewski
 */
public class EntityVerificationInterceptor extends EmptyInterceptor
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public EntityVerificationInterceptor(IHibernateTransactionManagerCallback callback)
    {
        this.callback = callback;
        clearLists();
    }

    IHibernateTransactionManagerCallback callback;

    Set<SamplePE> modifiedSamples;

    Set<MaterialPE> modifiedMaterials;

    Set<ExperimentPE> modifiedExperiments;

    Set<DataPE> modifiedDatasets;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        modifiedEntity(entity);
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
        // TODO: implement the validation logic
        // callback.setRollbackOnly(tx, "The sample has been modified");

    }

    private void modifiedEntity(Object entity)
    {
        if (entity instanceof SamplePE)
        {
            modifiedEntity((SamplePE) entity, modifiedSamples);
        } else if (entity instanceof ExperimentPE)
        {
            modifiedEntity((ExperimentPE) entity, modifiedExperiments);
        } else if (entity instanceof MaterialPE)
        {
            modifiedEntity((MaterialPE) entity, modifiedMaterials);
        } else if (entity instanceof DataPE)
        {
            modifiedEntity((DataPE) entity, modifiedDatasets);
        }
    }

    private <T> void modifiedEntity(T entity, Set<T> modifiedEntities)
    {
        modifiedEntities.add(entity);
    }

    private void clearLists()
    {
        modifiedSamples = new HashSet<SamplePE>();
        modifiedExperiments = new HashSet<ExperimentPE>();
        modifiedMaterials = new HashSet<MaterialPE>();
        modifiedDatasets = new HashSet<DataPE>();
    }

}
