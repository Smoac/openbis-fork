/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.beanlib.hibernate3.Hibernate3SequenceGenerator;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionStyle;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.IToStringConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Abstract super class of all <i>Hibernate</i> DAOs.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDAO extends HibernateDaoSupport
{

    /** The original source database instance. */
    private DatabaseInstancePE databaseInstance;

    protected AbstractDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        assert sessionFactory != null : "Unspecified session factory";
        assert databaseInstance != null || this instanceof DatabaseInstanceDAO : "Unspecified "
                + "database instance (only permitted for DatabaseInstancesDAO).";
        setDatabaseInstance(databaseInstance);
        setSessionFactory(sessionFactory);
    }

    /**
     * Validates given <i>Persistence Entity</i> using an appropriate {@link ClassValidator}.
     */
    @SuppressWarnings("unchecked")
    protected final static <E> void validatePE(final E pe) throws DataIntegrityViolationException
    {
        validatePE(pe, new ClassValidator(pe.getClass()));
    }

    /**
     * Validates given <i>Persistence Entity</i> using the given {@link ClassValidator}.
     */
    protected final static <E> void validatePE(final E pe, final ClassValidator<E> validator)
            throws DataIntegrityViolationException
    {
        final InvalidValue[] validationMessages = validator.getInvalidValues(pe);
        if (validationMessages.length > 0)
        {
            throw new DataIntegrityViolationException(CollectionUtils.abbreviate(
                    validationMessages, -1, new IToStringConverter<InvalidValue>()
                        {
                            //
                            // IToStringConverter
                            //

                            public final String toString(final InvalidValue value)
                            {
                                return String.format(value.getMessage(), value.getValue());
                            }
                        }, CollectionStyle.NO_BOUNDARY));
        }
    }

    @Private
    public final void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    /**
     * Returns home database instance.
     */
    public final DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    /**
     * Casts given <var>list</var> to specified type.
     * <p>
     * The purpose of this method is to avoid <code>SuppressWarnings("unchecked")</code> in calling
     * methods.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected static final <T> List<T> cast(final List list)
    {
        return list;
    }

    /**
     * Ensures that given {@link List} contains one and only one entity.
     * 
     * @throws EmptyResultDataAccessException if given <var>entities</var> are <code>null</code> or
     *             empty.
     * @throws IncorrectResultSizeDataAccessException if more than one entity is found in given
     *             {@link List}.
     */
    @SuppressWarnings("unchecked")
    protected final static <T> T getEntity(final List<T> entities) throws DataAccessException
    {
        return (T) DataAccessUtils.requiredSingleResult(entities);
    }

    /**
     * Casts given <var>entity</var> to specified type.
     * 
     * @throws EmptyResultDataAccessException if given <var>entity</var> is <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    protected final static <T> T getEntity(final Object entity)
            throws EmptyResultDataAccessException
    {
        if (entity == null)
        {
            throw new EmptyResultDataAccessException(1);
        }
        return (T) entity;
    }

    /**
     * Casts given <var>entityOrNull</var> to specified type or returns null for null input.
     */
    @SuppressWarnings("unchecked")
    protected final static <T> T tryGetEntity(final Object entityOrNull)
    {
        return entityOrNull == null ? null : (T) entityOrNull;
    }

    /**
     * Checks given <var>entities</var> and throws a {@link IncorrectResultSizeDataAccessException}
     * if it contains more than one item.
     * 
     * @return <code>null</code> or the entity found at index <code>0</code>.
     */
    protected final static <T> T tryFindEntity(final List<T> entities, final String entitiesName,
            final Object... parameters) throws IncorrectResultSizeDataAccessException
    {
        final int size = entities.size();
        switch (size)
        {
            case 0:
                return null;
            case 1:
                return entities.get(0);
            default:
                throw new IncorrectResultSizeDataAccessException(String.format(
                        "%d %s found for '%s'. Expected: 1 or 0.", size, entitiesName,
                        parameters.length == 1 ? parameters[0] : Arrays.asList(parameters)), 1,
                        size);
        }
    }

    /**
     * Executes given <var>sql</var>.
     * <p>
     * Should be an <code>INSERT</code> or <code>UPDATE</code> statement.
     * </p>
     */
    protected final void executeUpdate(final String sql, final Serializable... parameters)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    final SQLQuery sqlQuery = session.createSQLQuery(sql);
                    for (int i = 0; i < parameters.length; i++)
                    {
                        Serializable parameter = parameters[i];
                        if (parameter instanceof Long)
                        {
                            sqlQuery.setLong(i, (Long) parameter);
                        } else if (parameter instanceof Integer)
                        {
                            sqlQuery.setInteger(i, (Integer) parameter);
                        } else if (parameter instanceof Character)
                        {
                            sqlQuery.setCharacter(i, (Character) parameter);
                        } else if (parameter instanceof Date)
                        {
                            sqlQuery.setDate(i, (Date) parameter);
                        } else
                        {
                            sqlQuery.setSerializable(i, parameter);
                        }
                    }
                    sqlQuery.executeUpdate();
                    return null;
                }
            });
    }

    /**
     * Natively performs given <var>sql</var> and returns an unique result by calling
     * {@link Query#uniqueResult()}.
     */
    @SuppressWarnings("unchecked")
    protected final <T> T getUniqueResult(final String sql, final Object... parameters)
    {
        return (T) getHibernateTemplate().execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session)
                {
                    return session.createSQLQuery(String.format(sql, parameters)).uniqueResult();
                }
            });
    }

    protected final static Object[] toArray(final Object... objects)
    {
        return objects;
    }

    protected final long getNextSequenceId(String sequenceName)
    {
        return Hibernate3SequenceGenerator.nextval(sequenceName, getSession(true));
    }

    protected final Object executeStatelessAction(final StatelessHibernateCallback action)
            throws DataAccessException
    {
        assert action != null;
        return getHibernateTemplate().execute(new HibernateCallback()
            {

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    StatelessSession sls = null;
                    try
                    {
                        sls = this.getStatelessSession();
                        return action.doInStatelessSession(sls);
                    } catch (HibernateException ex)
                    {
                        throw convertHibernateAccessException(ex);
                    } catch (RuntimeException ex)
                    {
                        // Callback code threw application exception...
                        throw ex;
                    } finally
                    {
                        if (sls != null)
                        {
                            releaseConnection(sls.connection());
                            sls.close();
                        }
                    }

                }

                private StatelessSession getStatelessSession()
                        throws CannotGetJdbcConnectionException
                {
                    return getSessionFactory().openStatelessSession(getThreadBoundConnection());
                }

                private Connection getThreadBoundConnection()
                        throws CannotGetJdbcConnectionException
                {
                    return DataSourceUtils.getConnection(SessionFactoryUtils
                            .getDataSource(getSessionFactory()));
                }

                private void releaseConnection(Connection connection)
                {
                    DataSourceUtils.releaseConnection(connection, SessionFactoryUtils
                            .getDataSource(getSessionFactory()));
                }
            });
    }

    /**
     * Callback interface for Hibernate code requiring a {@link org.hibernate.StatelessSession}. To
     * be used with {@link HibernateTemplate}'s execution methods, often as anonymous classes within
     * a method implementation.
     */
    protected interface StatelessHibernateCallback
    {
        Object doInStatelessSession(StatelessSession sls);
    }

}
