/*
 * Copyright 2008 ETH Zuerich, CISD
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
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Implementation of {@link ISampleDAO} for databases.
 * 
 * @author Tomasz Pylak
 */
public class SampleDAO extends AbstractDAO implements ISampleDAO
{
    private final static Class<SamplePE> ENTITY_CLASS = SamplePE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleDAO.class);

    SampleDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    private final Criteria createListSampleForTypeCriteria(final SampleTypePE sampleType)
    {
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("sampleType", sampleType));
        fetchRelations(criteria, "container", sampleType.getContainerHierarchyDepth());
        fetchRelations(criteria, "generatedFrom", sampleType.getGeneratedFromHierarchyDepth());
        return criteria;
    }

    private final void fetchRelations(final Criteria criteria, final String relationName,
            final int relationDepth)
    {
        String relationPath = relationName;
        for (int i = 0; i < relationDepth; i++)
        {
            criteria.setFetchMode(relationPath, FetchMode.JOIN);
            relationPath += "." + relationName;
        }
    }

    public final void createSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";
        validatePE(sample);
        sample.setCode(CodeConverter.tryToDatabase(sample.getCode()));

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.save(sample);
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("ADD: sample " + sample);
        }
    }

    //
    // ISampleDAO
    //

    public final List<SamplePE> listSamplesByTypeAndGroup(final SampleTypePE sampleType,
            final GroupPE group) throws DataAccessException
    {
        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        criteria.add(Restrictions.eq("group", group));

        final List<SamplePE> list = cast(criteria.list());

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type %s and group %s.", list.size(),
                    sampleType, group));
        }
        return list;
    }

    public final List<SamplePE> listSamplesByTypeAndDatabaseInstance(final SampleTypePE sampleType,
            final DatabaseInstancePE databaseInstance)
    {
        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));

        final List<SamplePE> list = cast(criteria.list());

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type %s and database instance %s.", list
                            .size(), sampleType, databaseInstance));
        }
        return list;
    }

    public final SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert databaseInstance != null : "Unspecified database instance.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String
                    .format("Following sample '%s' has been found for "
                            + "code '%s' and database instance '%s'.", sample, sampleCode,
                            databaseInstance));
        }
        return sample;
    }

    public final SamplePE tryFindByCodeAndGroup(final String sampleCode, final GroupPE group)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert group != null : "Unspecified group.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
        criteria.add(Restrictions.eq("group", group));
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following sample '%s' has been found for code '%s' and group '%s'.", sample,
                    sampleCode, group));
        }
        return sample;
    }

    public final List<SamplePE> listSampleByGeneratedFrom(final SamplePE sample)
    {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        final String hql = String.format("from %s s where s.generatedFrom = ?", TABLE_NAME);
        final List<SamplePE> list = cast(hibernateTemplate.find(hql, toArray(sample)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d sample(s) have been found for \"generatedFrom\" sample '%s'.", list.size(),
                    sample));
        }
        return list;
    }
}
