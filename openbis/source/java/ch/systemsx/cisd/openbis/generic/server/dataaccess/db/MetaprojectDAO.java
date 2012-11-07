/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.MetaprojectName;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectDAO extends AbstractGenericEntityDAO<MetaprojectPE> implements
        IMetaprojectDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MetaprojectDAO.class);

    private static final Class<MetaprojectPE> ENTITY_CLASS = MetaprojectPE.class;

    public MetaprojectDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    @Override
    public MetaprojectPE tryFindByOwnerAndName(String ownerId, String metaprojectName)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(MetaprojectPE.class);
        criteria.createAlias("owner", "o");
        criteria.add(Restrictions.eq("name", metaprojectName).ignoreCase());
        criteria.add(Restrictions.eq("o.userId", ownerId));
        final List<MetaprojectPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final MetaprojectPE entity = tryFindEntity(list, "metaproject");

        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s, %s): '%s'.", methodName, ownerId,
                    metaprojectName, entity));
        }
        return entity;
    }

    @Override
    public List<MetaprojectPE> listMetaprojects(PersonPE owner)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(MetaprojectPE.class);
        criteria.add(Restrictions.eq("owner", owner));
        criteria.addOrder(Order.asc("name"));
        final List<MetaprojectPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d metaproject(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), owner, list.size()));
        }
        return list;
    }

    @Override
    public void createOrUpdateMetaproject(MetaprojectPE metaproject, PersonPE owner)
    {
        assert metaproject != null : "Missing metaproject.";

        validatePE(metaproject);
        MetaprojectName.validate(metaproject.getName());

        if (metaproject.getOwner() == null)
        {
            metaproject.setOwner(owner);
        }
        metaproject.setPrivate(true);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(metaproject);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE: metaproject '%s'.", metaproject));
        }
    }

    @Override
    public Collection<MetaprojectPE> listMetaprojectsForEntity(PersonPE owner,
            IEntityInformationHolderDTO entity)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(MetaprojectAssignmentPE.class);
        criteria.createAlias("metaproject", "m");
        criteria.add(Restrictions.eq("m.owner", owner));
        criteria.add(Restrictions.eq(entity.getEntityKind().getLabel(), entity));
        final List<MetaprojectAssignmentPE> assignments =
                cast(getHibernateTemplate().findByCriteria(criteria));

        Set<MetaprojectPE> metaprojects = new HashSet<MetaprojectPE>();
        for (MetaprojectAssignmentPE assignment : assignments)
        {
            metaprojects.add(assignment.getMetaproject());
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s, %s): %d metaproject(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), owner, entity, metaprojects.size()));
        }

        return metaprojects;
    }

    @Override
    public Collection<MetaprojectAssignmentPE> listMetaprojectAssignmentsForEntities(
            PersonPE owner, Collection<? extends IEntityInformationWithPropertiesHolder> entities,
            EntityKind entityKind)
    {
        if (entities.isEmpty())
        {
            return Collections.emptySet();
        }
        final DetachedCriteria criteria = DetachedCriteria.forClass(MetaprojectAssignmentPE.class);
        criteria.createAlias("metaproject", "m");
        criteria.add(Restrictions.eq("m.owner", owner));
        criteria.add(Restrictions.in(entityKind.getLabel(), entities));
        final List<MetaprojectAssignmentPE> assignments =
                cast(getHibernateTemplate().findByCriteria(criteria));

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s, %s): %d metaproject(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), owner, entities, assignments.size()));
        }

        return assignments;
    }
}
