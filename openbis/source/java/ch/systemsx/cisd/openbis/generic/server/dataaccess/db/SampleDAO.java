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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
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
    private final void internalCreateOrUpdateSample(final SamplePE sample,
            final HibernateTemplate hibernateTemplate, final boolean doLog)
    {
        validatePE(sample);
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

    public final void createOrUpdateSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        internalCreateOrUpdateSample(sample, hibernateTemplate, true);

        // need to deal with exception thrown by trigger checking code uniqueness
        flushWithSqlExceptionHandling(hibernateTemplate);
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(sample));
    }

    public final List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample)
    {
        return sample.getGenerated();
    }

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

    public final void createOrUpdateSamples(final List<SamplePE> samples)
            throws DataAccessException
    {
        assert samples != null && samples.size() > 0 : "Unspecified or empty samples.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        for (final SamplePE samplePE : samples)
        {
            internalCreateOrUpdateSample(samplePE, hibernateTemplate, false);
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
    }

    public final void updateSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";
        validatePE(sample);

        // need to deal with exception thrown by trigger checking code uniqueness
        flushWithSqlExceptionHandling(getHibernateTemplate());
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(sample));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("UPDATE: sample '" + sample + "'.");
        }
    }

    public List<SamplePE> listByPermID(Set<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<SamplePE>();
        }
        final DetachedCriteria criteria = DetachedCriteria.forClass(SamplePE.class);
        criteria.add(Restrictions.in("permId", values));
        final List<SamplePE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d sample(s) have been found.", list.size()));
        }
        return list;
    }

    public void delete(final List<TechId> sampleIds, final PersonPE registrator, final String reason)
            throws DataAccessException
    {
        final String sqlPermId =
                "SELECT perm_id FROM " + TableNames.SAMPLES_TABLE + " WHERE id = :sId";
        final String sqlDeleteProperties =
                "DELETE FROM " + TableNames.SAMPLE_PROPERTIES_TABLE + " WHERE samp_id = :sId";
        final String sqlDeleteSample =
                "DELETE FROM " + TableNames.SAMPLES_TABLE + " WHERE id = :sId";
        final String sqlInsertEvent =
                String.format(
                        "INSERT INTO %s (id, event_type, description, reason, pers_id_registerer, entity_type, identifier) "
                                + "VALUES (nextval('%s'), :eventType, :description, :reason, :registratorId, :entityType, :identifier)",
                        TableNames.EVENTS_TABLE, SequenceNames.EVENT_SEQUENCE);

        executeStatelessAction(new StatelessHibernateCallback()
            {
                public Object doInStatelessSession(StatelessSession session)
                {
                    final SQLQuery sqlQueryPermId = session.createSQLQuery(sqlPermId);
                    final SQLQuery sqlQueryDeleteProperties =
                            session.createSQLQuery(sqlDeleteProperties);
                    final SQLQuery sqlQueryDeleteSample = session.createSQLQuery(sqlDeleteSample);
                    final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                    sqlQueryInsertEvent.setParameter("eventType", EventType.DELETION.name());
                    sqlQueryInsertEvent.setParameter("reason", reason);
                    sqlQueryInsertEvent.setParameter("registratorId", registrator.getId());
                    sqlQueryInsertEvent.setParameter("entityType", EntityType.SAMPLE.name());
                    int counter = 0;
                    for (TechId techId : sampleIds)
                    {
                        sqlQueryPermId.setParameter("sId", techId.getId());
                        final String permIdOrNull = tryGetEntity(sqlQueryPermId.uniqueResult());
                        if (permIdOrNull != null)
                        {
                            // delete properties
                            sqlQueryDeleteProperties.setParameter("sId", techId.getId());
                            sqlQueryDeleteProperties.executeUpdate();
                            // delete sample
                            sqlQueryDeleteSample.setParameter("sId", techId.getId());
                            sqlQueryDeleteSample.executeUpdate();
                            // create event
                            sqlQueryInsertEvent.setParameter("description", permIdOrNull);
                            sqlQueryInsertEvent.setParameter("identifier", permIdOrNull);
                            sqlQueryInsertEvent.executeUpdate();
                            if (++counter % 1000 == 0)
                            {
                                operationLog.info(String.format("%d samples have been deleted...",
                                        counter));
                            }
                        }
                    }
                    return null;
                }
            });

        List<Long> ids = TechId.asLongs(sampleIds);
        scheduleRemoveFromFullTextIndex(ids);
    }

    public void invalidate(final List<TechId> sampleIds, final InvalidationPE invalidation)
            throws DataAccessException
    {
        // TODO 2011-06-16, Piotr Buczek: could be done faster with bulk update
        for (TechId sampleId : sampleIds)
        {
            SamplePE sample = loadByTechId(sampleId);
            sample.setInvalidation(invalidation);
            getHibernateTemplate().update(sample);
        }

        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    public Set<TechId> listParents(final Collection<TechId> children, final TechId relationship)
    {
        final String query =

                "select sample_id_parent from sample_relationships where sample_id_child in (:ids) and relationship_id = :r ";
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        public final Object doInHibernate(final Session session)
                        {
                            final List<Long> longIds = TechId.asLongs(children);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .setParameter("r", relationship.getId()).list();
                        }
                    });
        Set<TechId> result = transformNumbers2TechIds(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d sample parents(s) have been found.",
                    results.size()));
        }
        return result;
    }

}
