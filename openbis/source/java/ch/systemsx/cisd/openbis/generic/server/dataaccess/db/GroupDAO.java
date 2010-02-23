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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * <i>Data Access Object</i> implementation for {@link GroupPE}.
 * 
 * @author Christian Ribeaud
 */
final class GroupDAO extends AbstractGenericEntityDAO<GroupPE> implements IGroupDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GroupDAO.class);

    GroupDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, GroupPE.class);
    }

    //
    // IGroupDAO
    //

    public final GroupPE tryFindGroupByCodeAndDatabaseInstance(final String groupCode,
            final DatabaseInstancePE databaseInstance) throws DataAccessException
    {
        assert groupCode != null : "Unspecified space code.";
        assert databaseInstance != null : "Unspecified database instance.";

        final List<GroupPE> list =
                cast(getHibernateTemplate().find(
                        String.format("select g from %s g where g.code = ? "
                                + "and g.databaseInstance = ?", getEntityClass().getSimpleName()),
                        toArray(CodeConverter.tryToDatabase(groupCode), databaseInstance)));
        final GroupPE entity = tryFindEntity(list, "group");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s, %s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), groupCode, databaseInstance, entity));
        }
        return entity;
    }

    public final List<GroupPE> listGroups() throws DataAccessException
    {
        final List<GroupPE> list = cast(getHibernateTemplate().loadAll(getEntityClass()));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d space(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    public final List<GroupPE> listGroups(final DatabaseInstancePE databaseInstance)
            throws DataAccessException
    {
        assert databaseInstance != null : "Unspecified database instance.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));
        final List<GroupPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d space(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), databaseInstance, list.size()));
        }
        return list;
    }

    public final void createGroup(final GroupPE group) throws DataAccessException
    {
        assert group != null : "Unspecified space";
        // TODO 2008-11-28, Christian Ribeaud: This is a business rule. Find a better location for
        // this.
        assert group.getDatabaseInstance().isOriginalSource() : "Registration on a non-home database is not allowed";
        validatePE(group);

        final HibernateTemplate template = getHibernateTemplate();
        group.setCode(CodeConverter.tryToDatabase(group.getCode()));
        template.save(group);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: space '%s'.", group));
        }
    }

}
