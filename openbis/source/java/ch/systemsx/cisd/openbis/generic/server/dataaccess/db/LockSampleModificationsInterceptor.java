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
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * {@link Interceptor} implementation that obtains a (reentrant) Java lock before Sample save/update
 * and releases it on transaction completion. It is more safe to implement such a lock this way
 * rather than invoking a method obtaining the lock inside DAO methods because update of Sample
 * could happen automatically upon flush to DB and it would be easy to introduce such an update
 * without noticing it. On the other hand we need a lock that will be obtained for every Sample
 * modification because we have a complex unique code check in a before save/update trigger and we
 * don't want any race condition or deadlock (if lock is gathered in the trigger). See [LMS-814] for
 * details.<br>
 * <br>
 * NOTE: Explicit exclusive lock on 'samples' table cannot be used because H2 database does not
 * support it.
 * 
 * @author Piotr Buczek
 */
public class LockSampleModificationsInterceptor extends EmptyInterceptor
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    //
    // Interceptor
    //

    @Override
    public boolean onSave(Object entity, java.io.Serializable id, Object[] state,
            String[] propertyNames, org.hibernate.type.Type[] types)
    {
        obtainLockForSampleModifications(entity);
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        obtainLockForSampleModifications(entity);
        return false;
    }

    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        releaseLockForSampleModifications();
    }

    //
    // implementation using ReentrantLock
    //

    private final ReentrantLock sampleTableLock = new ReentrantLock();

    private void obtainLockForSampleModifications(Object entity)
    {
        if (entity instanceof SamplePE)
        {
            sampleTableLock.lock();
        }
    }

    private void releaseLockForSampleModifications()
    {
        while (sampleTableLock.isHeldByCurrentThread())
        {
            sampleTableLock.unlock();
        }
    }
}
