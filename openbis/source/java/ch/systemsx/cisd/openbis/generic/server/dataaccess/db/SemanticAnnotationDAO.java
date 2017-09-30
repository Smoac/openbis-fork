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

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISemanticAnnotationDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * <i>Data Access Object</i> implementation for {@link SemanticAnnotationPE}.
 * 
 * @author pkupczyk
 */
final class SemanticAnnotationDAO extends AbstractGenericEntityDAO<SemanticAnnotationPE> implements ISemanticAnnotationDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SemanticAnnotationDAO.class);

    SemanticAnnotationDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, SemanticAnnotationPE.class, historyCreator);
    }

    @Override
    public void createOrUpdate(SemanticAnnotationPE annotation)
    {
        validatePE(annotation);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(annotation);
        template.flush();

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Created or updated semantic annotation '%s'.", annotation));
        }
    }

    @Override
    public List<SemanticAnnotationPE> findByIds(Collection<Long> ids)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SemanticAnnotationPE.class);
        criteria.add(Restrictions.in("id", ids));

        final List<SemanticAnnotationPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d semantic annotation(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<SemanticAnnotationPE> findByPermIds(Collection<String> permIds)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SemanticAnnotationPE.class);
        criteria.add(Restrictions.in("permId", permIds));

        final List<SemanticAnnotationPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d semantic annotation(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

}
