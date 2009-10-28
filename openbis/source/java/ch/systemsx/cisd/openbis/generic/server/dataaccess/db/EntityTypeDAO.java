/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Database based implementation of {@link IEntityTypeDAO}.
 * 
 * @author Franz-Josef Elmer
 */
final class EntityTypeDAO extends AbstractTypeDAO<EntityTypePE> implements IEntityTypeDAO
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EntityTypeDAO.class);

    private final EntityKind entityKind;

    EntityTypeDAO(final EntityKind entityKind, final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, entityKind.getTypeClass());
        this.entityKind = entityKind;
    }

    //
    // IEntityTypeDAO
    //

    public final EntityTypePE tryToFindEntityTypeByCode(final String code)
            throws DataAccessException
    {
        return super.tryFindTypeByCode(code);
    }

    public final <T extends EntityTypePE> List<T> listEntityTypes() throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        final String entityKindName = entityKind.getLabel();
        criteria.setFetchMode(entityKindName + "TypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        final List<T> list = cast(getHibernateTemplate().findByCriteria(criteria));
        return list;
    }

    public final <T extends EntityTypePE> void createOrUpdateEntityType(T entityType)
            throws DataAccessException
    {
        assert entityType != null : "entityType is null";
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        validatePE(entityType);
        entityType.setCode(CodeConverter.tryToDatabase(entityType.getCode()));
        hibernateTemplate.saveOrUpdate(entityType);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: entity type '%s'.", entityType));
        }
    }

    public final <T extends EntityTypePE> void deleteEntityType(final T entityType)
    {
        assert entityType != null : "Entity Type unspecified";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.delete(entityType);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("DELETE: entity type '%s'.", entityType));
        }
    }

}
