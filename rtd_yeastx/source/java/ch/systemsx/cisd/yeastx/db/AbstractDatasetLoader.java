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

package ch.systemsx.cisd.yeastx.db;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Abstract class for dataset loaders. Provides commit and rollback functionality and creation of
 * the dataset records (without the detail data).
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDatasetLoader<T extends IGenericDAO> implements IDatasetLoader
{
    // if false transaction has to be commited or rollbacked before the next dataset will be created
    protected boolean isTransactionCompleted = true;

    private final DataSource dataSource;

    private final Class<T> queryClass;

    private T daoOrNull; // created when used for the first time

    protected AbstractDatasetLoader(DataSource dataSource, Class<T> queryClass)
    {
        this.dataSource = dataSource;
        this.queryClass = queryClass;
    }

    // for tests only
    protected AbstractDatasetLoader(DataSource dataSource, Class<T> queryClass, T dao)
    {
        this.dataSource = dataSource;
        this.queryClass = queryClass;
        this.daoOrNull = dao;
    }

    protected final T getDao()
    {
        if (daoOrNull == null)
        {
            this.daoOrNull = DBUtils.getQuery(dataSource, queryClass);
        }
        return daoOrNull;
    }

    /**
     * Cannot be called twice in a row if {@link #commit()} or {@link #rollback()} has not been
     * called in between.
     */
    protected void createDataSet(DMDataSetDTO dataSet)
    {
        if (isTransactionCompleted == false)
        {
            throw new IllegalStateException(
                    "The previous transaction of uploading a dataset has been neither commited nor rollbacked.");
        }
        DBUtils.createDataSet(getDao(), dataSet);
        isTransactionCompleted = false;
    }

    protected void rollbackAndRethrow(Throwable exception) throws Error
    {
        try
        {
            rollback();
        } catch (DataAccessException ex)
        {
            // Avoid this exception shadowing the original exception.
        }
        throw CheckedExceptionTunnel.wrapIfNecessary(exception);
    }

    public void commit()
    {
        try
        {
            getDao().close(true);
        } catch (Throwable th)
        {
            rollbackAndRethrow(th);
        } finally
        {
            isTransactionCompleted = true;
        }
    }

    public void rollback()
    {
        isTransactionCompleted = true;
        DBUtils.rollbackAndClose(getDao());
    }
}
