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

import java.util.ArrayList;
import java.util.HashMap;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * @author Tomasz Pylak
 */
public class SampleDAO extends AbstractDAO implements ISampleDAO
{
    private final static Class<SamplePE> ENTITY_CLASS = SamplePE.class;

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

    private Criteria createListSampleForTypeCriteria(final SampleTypePE sampleType)
    {
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("sampleType", sampleType));
        fetchRelations(criteria, "container", sampleType.getContainerHierarchyDepth());
        fetchRelations(criteria, "generatedFrom", sampleType.getGeneratedFromHierarchyDepth());
        return criteria;
    }

    private void fetchRelations(final Criteria criteria, final String relationName,
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

    public final List<SamplePE> listSamplesByTypeAndGroup(final SampleTypePE sampleType,
            final GroupPE group, List<String> propertyCodes) throws DataAccessException
    {
        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        criteria.add(Restrictions.eq("group", group));

        final List<SamplePE> list = cast(criteria.list());
        new GroupPropertiesFetcher(sampleType, list, propertyCodes, group).fetch();

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type %s and group %s.", list.size(),
                    sampleType, group));
        }
        return list;
    }

    public final List<SamplePE> listSamplesByTypeAndDatabaseInstance(final SampleTypePE sampleType,
            final DatabaseInstancePE databaseInstance, List<String> propertyCodes)
    {
        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));

        final List<SamplePE> list = cast(criteria.list());
        new InstancePropertiesFetcher(sampleType, list, propertyCodes, databaseInstance).fetch();

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type %s and database instance %s.", list
                            .size(), sampleType, databaseInstance));
        }
        return list;
    }

    abstract class PropertiesFetcher
    {
        protected static final String PROPERTIES_QUERY =
                "from %s sp where sp.entityTypePropertyType.entityType.code = ? and sp.entityTypePropertyType.propertyType.simpleCode = ? ";

        protected final SampleTypePE sampleType;

        protected final List<SamplePE> samples;

        protected final List<String> propertyCodes;

        protected PropertiesFetcher(SampleTypePE sampleType, List<SamplePE> samples,
                List<String> propertyCodes)
        {
            this.sampleType = sampleType;
            this.samples = samples;
            this.propertyCodes = propertyCodes;
        }

        protected HashMap<String, SamplePE> convertToMap(final List<SamplePE> samps)
        {
            HashMap<String, SamplePE> sampleMap = new HashMap<String, SamplePE>(samps.size());
            for (SamplePE s : samps)
            {
                s.setProperties(new ArrayList<SamplePropertyPE>());
                sampleMap.put(s.getCode(), s);
            }
            return sampleMap;
        }

        public void fetch()
        {
            if (samples.size() == 0)
            {
                return;
            }
            HashMap<String, SamplePE> sampleMap = convertToMap(samples);
            for (String propertyTypeCode : propertyCodes)
            {
                for (SamplePropertyPE sp : listProperties(propertyTypeCode))
                {
                    final String sampleCode = sp.getSample().getCode();
                    final SamplePE sample = sampleMap.get(sampleCode);
                    if (sample != null)
                    {
                        sample.getProperties().add(sp);
                    }
                }
            }
        }

        abstract public List<SamplePropertyPE> listProperties(String propertyTypeCode);

    }

    class GroupPropertiesFetcher extends PropertiesFetcher
    {

        private final GroupPE group;

        GroupPropertiesFetcher(SampleTypePE sampleType, List<SamplePE> samples,
                List<String> propertyCodes, GroupPE group)
        {
            super(sampleType, samples, propertyCodes);
            this.group = group;
        }

        @Override
        public List<SamplePropertyPE> listProperties(String propertyTypeCode)
        {
            final List<SamplePropertyPE> list =
                    cast(getHibernateTemplate().find(
                            String.format(PROPERTIES_QUERY + " and sp.entity.group.code = ?",
                                    SamplePropertyPE.class.getSimpleName()), new Object[]
                                { sampleType.getCode(), propertyTypeCode, group.getCode() }));
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("listGroupProperties(" + sampleType.getCode() + ","
                        + propertyTypeCode + ")");
            }
            return list;
        }
    }

    class InstancePropertiesFetcher extends PropertiesFetcher
    {

        private final DatabaseInstancePE instance;

        InstancePropertiesFetcher(SampleTypePE sampleType, List<SamplePE> samples,
                List<String> propertyCodes, DatabaseInstancePE instance)
        {
            super(sampleType, samples, propertyCodes);
            this.instance = instance;
        }

        @Override
        public List<SamplePropertyPE> listProperties(String propertyTypeCode)
        {
            final List<SamplePropertyPE> list =
                    cast(getHibernateTemplate().find(
                            String.format(PROPERTIES_QUERY + "and sp.entity.databaseInstance = ?",
                                    SamplePropertyPE.class.getSimpleName()), new Object[]
                                { sampleType.getCode(), propertyTypeCode, instance }));
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("listInstanceProperties(" + sampleType.getCode() + ","
                        + propertyTypeCode + ")");
            }
            return list;
        }
    }

}
