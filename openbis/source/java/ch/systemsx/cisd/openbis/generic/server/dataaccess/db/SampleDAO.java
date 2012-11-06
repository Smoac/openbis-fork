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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Implementation of {@link ISampleDAO} for databases.
 * 
 * @author Tomasz Pylak
 */
public class SampleDAO extends AbstractGenericEntityWithPropertiesDAO<SamplePE> implements
        ISampleDAO
{
    private final static Class<SamplePE> ENTITY_CLASS = SamplePE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SampleDAO.class);

    SampleDAO(final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources, databaseInstance, SamplePE.class);
    }

    // LockSampleModificationsInterceptor automatically obtains lock
    private final void internalCreateOrUpdateSample(final SamplePE sample, final PersonPE modifier,
            final HibernateTemplate hibernateTemplate, final boolean doLog)
    {
        validatePE(sample);
        sample.setCode(CodeConverter.tryToDatabase(sample.getCode()));
        hibernateTemplate.saveOrUpdate(sample);
        // Hibernate behaves as follows: If a PE bean property annotated with
        // @OptimisticLock(excluded = true) (as it is the case for modifier and modification date)
        // has changed the version will only be increased if a direct
        // bean property (like space, but not properties or meta-projects) has also changed. This
        // sounds like a bug. Thus, modifier and modification date is changed after the following
        // flush in order to increase the version in case of changed properties or meta-projects.
        hibernateTemplate.flush();
        sample.setModifier(modifier);
        sample.setModificationDate(new Date());
        hibernateTemplate.saveOrUpdate(sample);
        if (doLog && operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: sample '%s'.", sample));
        }
    }

    //
    // ISampleDAO
    //

    @Override
    public final void createOrUpdateSample(final SamplePE sample, final PersonPE modifier)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        try
        {
            final HibernateTemplate hibernateTemplate = getHibernateTemplate();

            internalCreateOrUpdateSample(sample, modifier, hibernateTemplate, true);

            // need to deal with exception thrown by trigger checking code uniqueness
            flushWithSqlExceptionHandling(hibernateTemplate);
            scheduleDynamicPropertiesEvaluation(Collections.singletonList(sample));
            scheduleDynamicPropertiesEvaluation(getDynamicPropertyEvaluatorScheduler(),
                    DataPE.class, new ArrayList<DataPE>(sample.getDatasets()));
        } catch (DataAccessException e)
        {
            SampleDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public final List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample)
    {
        return sample.getGenerated();
    }

    @Override
    public final List<SamplePE> listSamplesBySpaceAndProperty(final String propertyCode,
            final String propertyValue, final SpacePE space) throws DataAccessException
    {
        assert space != null : "Unspecified space.";
        assert propertyCode != null : "Unspecified property code";
        assert propertyValue != null : "Unspecified property value";

        String queryFormat =
                "from " + SamplePropertyPE.class.getSimpleName()
                        + " where %s = ? and entity.space = ? "
                        + " and entityTypePropertyType.propertyTypeInternal.simpleCode = ?"
                        + " and entityTypePropertyType.propertyTypeInternal.internalNamespace = ?";
        List<SamplePE> entities =
                listByPropertyValue(queryFormat, propertyCode, propertyValue, space);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for space '%s' and property '%s' equal to '%s'.",
                    entities.size(), space, propertyCode, propertyValue));
        }
        return entities;
    }

    private List<SamplePE> listByPropertyValue(String queryFormat, String propertyCode,
            String propertyValue, SpacePE parent)
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

    @Override
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

    @Override
    public final SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert databaseInstance != null : "Unspecified database instance.";

        Criteria criteria = createDatabaseInstanceCriteria(databaseInstance);
        addSampleCodeCriterion(criteria, sampleCode);
        SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (sample == null && isFullCode(sampleCode) == false)
        {
            criteria = createDatabaseInstanceCriteria(databaseInstance);
            sample = tryFindContainedSampleWithUniqueSubcode(criteria, sampleCode);
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String
                    .format("Following sample '%s' has been found for "
                            + "code '%s' and database instance '%s'.", sample, sampleCode,
                            databaseInstance));
        }
        return sample;
    }

    @Override
    public final List<SamplePE> listByCodesAndDatabaseInstance(final List<String> sampleCodes,
            final String containerCodeOrNull, final DatabaseInstancePE databaseInstance)
    {
        assert sampleCodes != null : "Unspecified sample codes.";
        assert databaseInstance != null : "Unspecified database instance.";

        Criteria criteria = createDatabaseInstanceCriteria(databaseInstance);
        addSampleCodesCriterion(criteria, sampleCodes, containerCodeOrNull);
        List<SamplePE> result = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s samples has been found", result.size()));
        }
        return result;
    }

    @Override
    public final SamplePE tryFindByCodeAndSpace(final String sampleCode, final SpacePE space)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert space != null : "Unspecified space.";

        Criteria criteria = createSpaceCriteria(space);
        addSampleCodeCriterion(criteria, sampleCode);
        SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (sample == null && isFullCode(sampleCode) == false)
        {
            criteria = createSpaceCriteria(space);
            sample = tryFindContainedSampleWithUniqueSubcode(criteria, sampleCode);
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following sample '%s' has been found for code '%s' and space '%s'.", sample,
                    sampleCode, space));
        }
        return sample;
    }

    @Override
    public final List<SamplePE> listByCodesAndSpace(final List<String> sampleCodes,
            final String containerCodeOrNull, final SpacePE space)
    {
        assert sampleCodes != null : "Unspecified sample codes.";
        assert space != null : "Unspecified space.";

        Criteria criteria = createSpaceCriteria(space);
        addSampleCodesCriterion(criteria, sampleCodes, containerCodeOrNull);
        List<SamplePE> result = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s samples has been found", result.size()));
        }
        return result;
    }

    private boolean isFullCode(String sampleCode)
    {
        return sampleCode.contains(SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
    }

    private SamplePE tryFindContainedSampleWithUniqueSubcode(Criteria criteria, String sampleCode)
    {
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
        criteria.add(Restrictions.isNotNull("container"));
        List<SamplePE> list = cast(criteria.list());
        return list.size() == 1 ? list.get(0) : null;
    }

    private Criteria createFindCriteria(Criterion criterion)
    {
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.setFetchMode("sampleType.sampleTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.add(criterion);
        return criteria;
    }

    private Criteria createDatabaseInstanceCriteria(final DatabaseInstancePE databaseInstance)
    {
        return createFindCriteria(Restrictions.eq("databaseInstance", databaseInstance));
    }

    private Criteria createSpaceCriteria(final SpacePE space)
    {
        return createFindCriteria(Restrictions.eq("space", space));
    }

    private void addSampleCodesCriterion(Criteria criteria, List<String> sampleCodes,
            String containerCodeOrNull)
    {
        List<String> convertedCodes = new ArrayList<String>();
        for (String sampleCode : sampleCodes)
        {
            convertedCodes.add(CodeConverter.tryToDatabase(sampleCode));
        }
        criteria.add(Restrictions.in("code", convertedCodes));
        addSampleContainerCriterion(criteria, containerCodeOrNull);
    }

    private void addSampleCodeCriterion(Criteria criteria, String sampleCode)
    {
        String[] sampleCodeTokens =
                sampleCode.split(SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
        String subCode = sampleCodeTokens.length > 1 ? sampleCodeTokens[1] : sampleCode;
        String containerCodeOrNull = sampleCodeTokens.length > 1 ? sampleCodeTokens[0] : null;
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(subCode)));
        addSampleContainerCriterion(criteria, containerCodeOrNull);
    }

    private void addSampleContainerCriterion(Criteria criteria, String containerCodeOrNull)
    {
        if (containerCodeOrNull != null)
        {
            criteria.createAlias("container", "c");
            criteria.add(Restrictions.eq("c.code", CodeConverter.tryToDatabase(containerCodeOrNull)));
        } else
        {
            criteria.add(Restrictions.isNull("container"));
        }
    }

    @Override
    public final void createOrUpdateSamples(final List<SamplePE> samples, final PersonPE modifier)
            throws DataAccessException
    {
        assert samples != null && samples.size() > 0 : "Unspecified or empty samples.";

        try
        {
            final HibernateTemplate hibernateTemplate = getHibernateTemplate();

            for (final SamplePE samplePE : samples)
            {
                internalCreateOrUpdateSample(samplePE, modifier, hibernateTemplate, false);
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("ADD: %d samples.", samples.size()));
            }

            // need to deal with exception thrown by trigger checking code uniqueness
            flushWithSqlExceptionHandling(getHibernateTemplate());
            scheduleDynamicPropertiesEvaluation(samples);

            // if session is not cleared registration of many samples slows down after each batch
            hibernateTemplate.clear();
        } catch (DataAccessException e)
        {
            SampleDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public final void updateSample(final SamplePE sample, final PersonPE modifier)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        try
        {
            sample.setModifier(modifier);
            validatePE(sample);

            // need to deal with exception thrown by trigger checking code uniqueness
            flushWithSqlExceptionHandling(getHibernateTemplate());
            scheduleDynamicPropertiesEvaluation(Collections.singletonList(sample));

            if (operationLog.isInfoEnabled())
            {
                operationLog.info("UPDATE: sample '" + sample + "'.");
            }
        } catch (DataAccessException e)
        {
            SampleDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public List<SamplePE> listByPermID(Set<String> values)
    {
        return listByIDsOfName("permId", values);
    }

    @Override
    public List<SamplePE> listByIDs(Collection<Long> ids)
    {
        return listByIDsOfName("id", ids);
    }

    private List<SamplePE> listByIDsOfName(String idName, Collection<?> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<SamplePE>();
        }
        final List<SamplePE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), SamplePE.class, idName, values);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d sample(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public void delete(final List<TechId> sampleIds, final PersonPE registrator, final String reason)
            throws DataAccessException
    {
        // NOTE: we use SAMPLES_ALL_TABLE, not DELETED_SAMPLES_VIEW because we still want to be
        // able to directly delete samples without going to trash (trash may be disabled)
        final String samplesTable = TableNames.SAMPLES_ALL_TABLE;

        final String sqlSelectPermIds = SQLBuilder.createSelectPermIdsSQL(samplesTable);
        final String sqlDeleteProperties =
                SQLBuilder.createDeletePropertiesSQL(TableNames.SAMPLE_PROPERTIES_TABLE,
                        ColumnNames.SAMPLE_COLUMN);
        final String sqlSelectAttachmentContentIds =
                SQLBuilder.createSelectAttachmentContentIdsSQL(ColumnNames.SAMPLE_COLUMN);
        final String sqlDeleteAttachmentContents = SQLBuilder.createDeleteAttachmentContentsSQL();
        final String sqlDeleteAttachments =
                SQLBuilder.createDeleteAttachmentsSQL(ColumnNames.SAMPLE_COLUMN);
        final String sqlDeleteSamples = SQLBuilder.createDeleteEnitiesSQL(samplesTable);
        final String sqlInsertEvent = SQLBuilder.createInsertEventSQL();

        executePermanentDeleteAction(EntityType.SAMPLE, sampleIds, registrator, reason,
                sqlSelectPermIds, sqlDeleteProperties, sqlSelectAttachmentContentIds,
                sqlDeleteAttachmentContents, sqlDeleteAttachments, sqlDeleteSamples, sqlInsertEvent);
    }

    @Override
    public Set<TechId> listSampleIdsByChildrenIds(final Collection<TechId> children,
            final TechId relationship)
    {
        final String query =
                "select sample_id_parent from " + TableNames.SAMPLE_RELATIONSHIPS_VIEW
                        + " where sample_id_child in (:ids) and relationship_id = :r ";
        @SuppressWarnings("unchecked")
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        @Override
                        public final Object doInHibernate(final Session session)
                        {
                            final List<Long> longIds = TechId.asLongs(children);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .setParameter("r", relationship.getId()).list();
                        }
                    });
        Set<TechId> result = transformNumbers2TechIdSet(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("found %d sample parents for given children",
                    results.size()));
        }
        return result;
    }

    @Override
    public Set<TechId> listSampleIdsByParentIds(Collection<TechId> parentIds)
    {
        return listChildrenIds(parentIds, TableNames.SAMPLE_RELATIONSHIPS_VIEW);
    }

    @Override
    public Set<TechId> listChildrenForTrashedSamples(Collection<TechId> parentIds)
    {
        return listChildrenIds(parentIds, TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE);
    }

    private Set<TechId> listChildrenIds(final Collection<TechId> parents, String tableName)
    {
        final String query =
                "SELECT sample_id_child FROM " + tableName + " WHERE sample_id_parent IN (:ids)";

        @SuppressWarnings("unchecked")
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        @Override
                        public final Object doInHibernate(final Session session)
                        {
                            final List<Long> longIds = TechId.asLongs(parents);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .list();
                        }
                    });
        Set<TechId> result = transformNumbers2TechIdSet(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %d sample children for given parents",
                    results.size()));
        }
        return result;
    }

    @Override
    public List<TechId> listSampleIdsByContainerIds(final Collection<TechId> containers)
    {
        final List<Long> longIds = TechId.asLongs(containers);
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(SamplePE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, "container.id", longIds);
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %s sample components for given containers",
                    results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    public List<TechId> listSampleIdsByExperimentIds(final Collection<TechId> experiments)
    {
        final List<Long> longIds = TechId.asLongs(experiments);
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(SamplePE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, "experimentInternal.id", longIds);
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %s samples for given experiments",
                    results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

}
