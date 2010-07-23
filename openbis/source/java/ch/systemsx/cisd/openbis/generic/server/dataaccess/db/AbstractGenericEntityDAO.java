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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGenericDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Abstract super class of DAOs using generic interface.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGenericEntityDAO<T extends IIdHolder> extends AbstractDAO implements
        IGenericDAO<T>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractGenericEntityDAO.class);

    private final Class<T> entityClass;

    protected AbstractGenericEntityDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance, final Class<T> entityClass)
    {
        super(sessionFactory, databaseInstance);
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass()
    {
        return entityClass;
    }

    public final T getByTechId(final TechId techId) throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        final Object entity = getHibernateTemplate().get(getEntityClass(), techId.getId());
        T result = null;
        if (entity == null)
        {
            throw new DataRetrievalFailureException(getEntityDescription() + " with ID "
                    + techId.getId() + " does not exist. Maybe someone has just deleted it.");
        } else
        {
            result = getEntity(getHibernateTemplate().get(getEntityClass(), techId.getId()));
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, result));
        }
        return result;
    }

    private String getEntityDescription()
    {
        String nameWithoutPE = getEntityClass().getSimpleName().replace("PE", "");
        String words[] = StringUtils.splitByCharacterTypeCamelCase(nameWithoutPE);
        return StringUtils.join(words, " ");
    }

    // TODO 2009-05-22, Tomasz Pylak: remove connections, it forces BOs to use strings with field
    // paths
    public final T tryGetByTechId(final TechId techId, String... connections)
            throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("id", techId.getId()));
        for (String connection : connections)
        {
            criteria.setFetchMode(connection, FetchMode.JOIN);
        }
        final T result = tryGetEntity(criteria.uniqueResult());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, result));
        }
        return result;
    }

    // TODO 2009-05-28, Piotr Buczek: use this instead of save() after BO update that does not flush
    public final void validateAndSaveUpdatedEntity(T entity)
    {
        assert entity != null : "entity is null";

        // as long as CODE cannot be edited we don't have to translate it with a converter here
        // because the code set in updated entity should be the one already translated during save
        // but if we allow it this will have to be changed for entities with codes e.g.
        // like for experiment:
        // experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));

        validatePE(entity);
        getHibernateTemplate().flush();
    }

    public final void validate(T entity)
    {
        assert entity != null : "entity is null";

        // as long as CODE cannot be edited we don't have to translate it with a converter here
        // because the code set in updated entity should be the one already translated during save
        // but if we allow it this will have to be changed for entities with codes e.g.
        // like for experiment:
        // experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));

        validatePE(entity);
    }

    public void clearSession()
    {
        getHibernateTemplate().clear();
    }

    public final void flush()
    {
        getHibernateTemplate().flush();
    }

    public void persist(T entity)
    {
        assert entity != null : "entity unspecified";

        validatePE(entity);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.save(entity);
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format("%s(%s)", MethodUtils.getCurrentMethod().getName(),
                    entity));
        }
    }

    public void delete(T entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.delete(entity);
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format("%s(%s)", MethodUtils.getCurrentMethod().getName(),
                    entity));
        }
    }

    public List<T> listAllEntities() throws DataAccessException
    {
        return cast(getHibernateTemplate().loadAll(getEntityClass()));
    }

    protected static List<Long> transformTechIds2Longs(Collection<TechId> techIds)
    {
        final List<Long> result = new ArrayList<Long>(techIds.size());
        for (TechId techId : techIds)
        {
            result.add(techId.getId());
        }
        return result;
    }

    protected static Set<TechId> transformNumbers2TechIds(Collection<? extends Number> numbers)
    {
        final Set<TechId> result = new HashSet<TechId>();
        for (Number number : numbers)
        {
            result.add(new TechId(number));
        }
        return result;
    }

}
