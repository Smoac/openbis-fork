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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.ClassValidator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Implementation of {@link ISampleDAO} for databases.
 * 
 * @author Tomasz Pylak
 */
public class SampleDAO extends AbstractGenericEntityDAO<SamplePE> implements ISampleDAO
{
    private final static Class<SamplePE> ENTITY_CLASS = SamplePE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleDAO.class);

    SampleDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, SamplePE.class);
    }

    private final Criteria createListAllSamplesCriteria()
    {
        return getSession().createCriteria(ENTITY_CLASS);
    }

    private final Criteria createListSampleForTypeCriteria(final SampleTypePE sampleType)
    {
        final Criteria criteria = createListAllSamplesCriteria();
        criteria.add(Restrictions.eq("sampleType", sampleType));
        fetchRelations(criteria, "container", sampleType.getContainerHierarchyDepth());
        fetchRelations(criteria, "generatedFrom", sampleType.getGeneratedFromHierarchyDepth());

        criteria.setFetchMode("experimentInternal", FetchMode.JOIN);

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

    private List<SamplePE> listSamplesByCriteria(final Criteria basicCriteria,
            boolean withExperimentAndProperties, Criterion... additionalCriterions)
            throws DataAccessException
    {
        for (Criterion criterion : additionalCriterions)
        {
            basicCriteria.add(criterion);
        }
        final int count = DAOUtils.getCount(basicCriteria);
        if (withExperimentAndProperties)
        {
            basicCriteria.setFetchMode("experimentInternal", FetchMode.JOIN);
            if (count <= DAOUtils.MAX_COUNT_FOR_PROPERTIES)
            {
                basicCriteria.setFetchMode("sampleProperties", FetchMode.JOIN);
            } else
            {
                operationLog.info(String.format("Found %d samples, disable properties loading.",
                        count));
            }
        }
        basicCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return cast(basicCriteria.list());
    }

    private List<SamplePE> listSamplesByCriteria(boolean withExperimentAndProperties,
            Criterion... criterions) throws DataAccessException
    {
        return listSamplesByCriteria(createListAllSamplesCriteria(), withExperimentAndProperties,
                criterions);
    }

    private List<SamplePE> listSamplesWithPropertiesByCriteria(Criterion... criterions)
            throws DataAccessException
    {
        return listSamplesByCriteria(true, criterions);
    }

    private List<SamplePE> listSamplesWithPropertiesByCriterion(String propertyName, Object value)
            throws DataAccessException
    {
        assert propertyName != null : "Unspecified property name.";
        assert value != null : "Unspecified value.";

        final Criterion criterion = Restrictions.eq(propertyName, value);
        final List<SamplePE> list = listSamplesWithPropertiesByCriteria(criterion);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d samples have been found for \"%s\" '%s'.", list
                    .size(), propertyName, value));
        }
        return list;
    }

    // LockSampleModificationsInterceptor automatically obtains lock
    private final void internalCreateSample(final SamplePE sample,
            final HibernateTemplate hibernateTemplate,
            final ClassValidator<SamplePE> classValidator, final boolean doLog)
    {
        validatePE(sample, classValidator);
        sample.setCode(CodeConverter.tryToDatabase(sample.getCode()));

        hibernateTemplate.saveOrUpdate(sample);
        if (doLog && operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: sample '%s'.", sample));
        }
    }

    //
    // ISampleDAO
    //

    public final void createSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        internalCreateSample(sample, hibernateTemplate,
                new ClassValidator<SamplePE>(SamplePE.class), true);
        try
        {
            hibernateTemplate.flush();
        } catch (UncategorizedSQLException e)
        {
            // need to deal with exception thrown by trigger checking code uniqueness
            final SQLException sqlExceptionOrNull =
                    ExceptionUtils.tryGetThrowableOfClass(e, SQLException.class);
            if (sqlExceptionOrNull != null && sqlExceptionOrNull.getNextException() != null)
            {
                throw new DataIntegrityViolationException(sqlExceptionOrNull.getNextException()
                        .getMessage());
            } else
            {
                throw e;
            }
        }
    }

    public List<SamplePE> listSamplesWithPropertiesByExperiment(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";

        final Criteria criteria = createListAllSamplesCriteria();
        fetchRelations(criteria, "container", 1);
        final Criterion criterion = Restrictions.eq("experimentInternal", experiment);
        final List<SamplePE> list = listSamplesByCriteria(criteria, true, criterion);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d samples have been found for \"%s\" '%s'.", list
                    .size(), "experimentItnernal", experiment));
        }
        return list;
    }

    public List<SamplePE> listSamplesWithPropertiesByContainer(final SamplePE container)
            throws DataAccessException
    {
        return listSamplesWithPropertiesByCriterion("container", container);
    }

    public final List<SamplePE> listSamplesWithPropertiesByGeneratedFrom(
            final SamplePE generatedFrom) throws DataAccessException
    {
        return listSamplesWithPropertiesByCriterion("generatedFrom", generatedFrom);
    }

    public final List<SamplePE> listSamplesWithPropertiesByGroup(final GroupPE group)
            throws DataAccessException
    {
        return listSamplesWithPropertiesByCriterion("group", group);
    }

    public final List<SamplePE> listSamplesWithPropertiesByDatabaseInstance(
            final DatabaseInstancePE databaseInstance) throws DataAccessException
    {
        return listSamplesWithPropertiesByCriterion("databaseInstance", databaseInstance);
    }

    public final List<SamplePE> listSamplesWithPropertiesByTypeAndGroup(
            final SampleTypePE sampleType, final GroupPE group) throws DataAccessException
    {
        assert sampleType != null : "Unspecified sample type.";
        assert group != null : "Unspecified space.";

        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        final Criterion criterion = Restrictions.eq("group", group);
        final List<SamplePE> list = listSamplesByCriteria(criteria, true, criterion);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type '%s' and space '%s'.", list.size(),
                    sampleType, group));
        }
        return list;
    }

    public final List<SamplePE> listSamplesWithPropertiesByTypeAndDatabaseInstance(
            final SampleTypePE sampleType, final DatabaseInstancePE databaseInstance)
    {
        assert sampleType != null : "Unspecified sample type.";
        assert databaseInstance != null : "Unspecified database instance.";

        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        final Criterion criterion = Restrictions.eq("databaseInstance", databaseInstance);
        final List<SamplePE> list = listSamplesByCriteria(criteria, true, criterion);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type '%s' and database instance '%s'.",
                    list.size(), sampleType, databaseInstance));
        }
        return list;
    }

    public final List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample)
    {
        assert sample != null : "Unspecified sample.";

        final Criterion criterion = Restrictions.eq("generatedFrom", sample);
        final List<SamplePE> list = listSamplesByCriteria(false, criterion);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d sample(s) have been found for \"generatedFrom\" sample '%s'.", list.size(),
                    sample));
        }
        return list;
    }

    public final List<SamplePE> listSamplesByGroupAndProperty(final String propertyCode,
            final String propertyValue, final GroupPE group) throws DataAccessException
    {
        assert group != null : "Unspecified space.";
        assert propertyCode != null : "Unspecified property code";
        assert propertyValue != null : "Unspecified property value";

        String queryFormat =
                "from " + SamplePropertyPE.class.getSimpleName()
                        + " where %s = ? and entity.group = ? "
                        + " and entityTypePropertyType.propertyTypeInternal.simpleCode = ?"
                        + " and entityTypePropertyType.propertyTypeInternal.internalNamespace = ?";
        List<SamplePE> entities =
                listByPropertyValue(queryFormat, propertyCode, propertyValue, group);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for space '%s' and property '%s' equal to '%s'.",
                    entities.size(), group, propertyCode, propertyValue));
        }
        return entities;
    }

    private List<SamplePE> listByPropertyValue(String queryFormat, String propertyCode,
            String propertyValue, GroupPE parent)
    {
        String simplePropertyCode = CodeConverter.tryToDatabase(propertyCode);
        boolean isInternalNamespace = CodeConverter.isInternalNamespace(propertyCode);
        Object[] arguments =
                toArray(propertyValue, parent, simplePropertyCode, isInternalNamespace);

        String queryPropertySimpleValue = String.format(queryFormat, "value");
        List<SamplePropertyPE> properties1 =
                cast(getHibernateTemplate().find(queryPropertySimpleValue, arguments));

        String queryPropertyVocabularyTerm = String.format(queryFormat, "vocabularyTerm.code");
        List<SamplePropertyPE> properties2 =
                cast(getHibernateTemplate().find(queryPropertyVocabularyTerm, arguments));

        properties1.addAll(properties2);
        List<SamplePE> entities = extractEntities(properties1);
        return entities;
    }

    private static List<SamplePE> extractEntities(List<SamplePropertyPE> properties)
    {
        List<SamplePE> samples = new ArrayList<SamplePE>();
        for (SamplePropertyPE prop : properties)
        {
            samples.add(prop.getEntity());
        }
        return samples;
    }

    public SamplePE tryToFindByPermID(String permID) throws DataAccessException
    {
        assert permID != null : "Unspecified permanent ID.";
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("permId", permID));
        criteria.setFetchMode("sampleType.sampleTypePropertyTypesInternal", FetchMode.JOIN);
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Following sample '%s' has been found for "
                    + "permanent ID '%s'.", sample, permID));
        }
        return sample;
    }

    public final SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert databaseInstance != null : "Unspecified database instance.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        addSampleCodeCriterion(criteria, sampleCode);
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));
        criteria.setFetchMode("sampleType.sampleTypePropertyTypesInternal", FetchMode.JOIN);
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
        assert group != null : "Unspecified space.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        addSampleCodeCriterion(criteria, sampleCode);
        criteria.add(Restrictions.eq("group", group));
        criteria.setFetchMode("sampleType.sampleTypePropertyTypesInternal", FetchMode.JOIN);
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following sample '%s' has been found for code '%s' and space '%s'.", sample,
                    sampleCode, group));
        }
        return sample;
    }

    private void addSampleCodeCriterion(Criteria criteria, String sampleCode)
    {
        String[] sampleCodeTokens =
                sampleCode.split(SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
        if (sampleCodeTokens.length > 1)
        {
            final String containerCode = sampleCodeTokens[0];
            final String code = sampleCodeTokens[1];
            criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(code)));
            criteria.createAlias("container", "c");
            criteria.add(Restrictions.eq("c.code", CodeConverter.tryToDatabase(containerCode)));
        } else
        {
            criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
            criteria.add(Restrictions.isNull("container"));
        }

    }

    public final void createSamples(final List<SamplePE> samples) throws DataAccessException
    {
        assert samples != null && samples.size() > 0 : "Unspecified or empty samples.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        final ClassValidator<SamplePE> classValidator =
                new ClassValidator<SamplePE>(SamplePE.class);
        for (final SamplePE samplePE : samples)
        {
            internalCreateSample(samplePE, hibernateTemplate, classValidator, false);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: %d samples.", samples.size()));
        }
        hibernateTemplate.flush();
    }

    public final void updateSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";
        validatePE(sample);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("UPDATE: sample '" + sample + "'.");
        }
    }

}
