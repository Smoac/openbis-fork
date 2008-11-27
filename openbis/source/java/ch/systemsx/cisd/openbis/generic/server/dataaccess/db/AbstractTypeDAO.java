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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * An abstract extension of <code>HibernateAbstractDAO</code> suitable for tables that contains
 * <i>type</i> information. <br>
 * Note: this class has been copied from old lims project.
 * 
 * @author Izabela Adamczyk
 */
abstract class AbstractTypeDAO<T extends AbstractTypePE> extends AbstractDAO
{

    /**
     * The <code>Logger</code> of this class.
     * <p>
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    public AbstractTypeDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    final T tryFindTypeByCode(final String code) throws DataAccessException
    {
        return tryFindTypeByCode(code, true);
    }

    final T tryFindTypeByCode(final String code, final boolean appendDatabaseInstance)
            throws DataAccessException
    {
        assert code != null : "Unspecified code";
        final List<T> list;
        if (appendDatabaseInstance)
        {
            list =
                    cast(getHibernateTemplate()
                            .find(
                                    String.format("select st from %s st where st.code = ? "
                                            + "and st.databaseInstance.id = ?", getEntityClass()
                                            .getSimpleName()),
                                    new Object[]
                                        { CodeConverter.tryToDatabase(code),
                                                getDatabaseInstance().getId() }));

        } else
        {
            list =
                    cast(getHibernateTemplate().find(
                            String.format("select st from %s st where st.code = ? ",
                                    getEntityClass().getSimpleName()), new Object[]
                                { CodeConverter.tryToDatabase(code) }));
        }
        final T entity = tryFindEntity(list, "type");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("tryFind" + getTypeDescription() + "ByCode(" + code + "): '"
                    + entity + "'");
        }
        return entity;
    }

    abstract Class<?> getEntityClass();

    final String getTypeDescription()
    {
        final String className = getEntityClass().getSimpleName();
        return StringUtils.substring(className, 0, className.length() - 2);
    }

    final List<T> listTypes() throws DataAccessException
    {
        return listTypes(true);
    }

    final List<T> listTypes(final boolean appendDatabaseInstance) throws DataAccessException
    {
        final List<T> list;
        if (appendDatabaseInstance)
        {
            list =
                    cast(getHibernateTemplate().find(
                            String.format("from %s st where st.databaseInstance = ?",
                                    getEntityClass().getSimpleName()), new Object[]
                                { getDatabaseInstance() }));

        } else
        {
            list =
                    cast(getHibernateTemplate().find(
                            String.format("from %s", getEntityClass().getSimpleName())));

        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("list" + getTypeDescription() + "s: " + list.size()
                    + " type(s) have been found.");
        }
        return list;
    }

}