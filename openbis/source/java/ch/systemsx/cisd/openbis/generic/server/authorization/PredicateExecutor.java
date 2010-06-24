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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ArrayPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.CollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.plugin.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.plugin.generic.server.batch.IBatchOperation;

/**
 * A static class to execute {@link IPredicate}.
 * 
 * @author Christian Ribeaud
 */
public final class PredicateExecutor
{
    private static IPredicateFactory predicateFactory;

    private static IAuthorizationDataProvider authorizationDataProvider;

    private PredicateExecutor()
    {
        // Can not be instantiated.
    }

    /**
     * Statically sets the <code>IPredicateProvider</code> implementation.
     */
    static final void setPredicateFactory(final IPredicateFactory predicateProvider)
    {
        PredicateExecutor.predicateFactory = predicateProvider;
    }

    static final IPredicateFactory getPredicateFactory()
    {
        return predicateFactory;
    }

    /**
     * Statically sets the {@link IAuthorizationDAOFactory} implementation.
     */
    static final void setDAOFactory(final IAuthorizationDAOFactory daoFactory)
    {
        PredicateExecutor.authorizationDataProvider = new AuthorizationDataProvider(daoFactory);
    }

    static final IAuthorizationDataProvider getAuthorizationDataProvider()
    {
        return authorizationDataProvider;
    }

    static final void setAuthorizationDataProvider(
            IAuthorizationDataProvider authorizationDataProvider)
    {
        PredicateExecutor.authorizationDataProvider = authorizationDataProvider;
    }

    /**
     * Creates, casts and ensures that the returned {@link IPredicate} is not <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    private final static <T> IPredicate<T> createPredicate(
            final Class<? extends IPredicate<?>> predicateClass)
    {
        assert predicateFactory != null : "Unspecified predicate factory";
        final IPredicate<?> predicate = predicateFactory.createPredicateForClass(predicateClass);
        assert predicate != null : "Predicate factory should not return a null predicate";
        return (IPredicate<T>) predicate;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T[] castToArray(final T argumentValue)
    {
        return (T[]) argumentValue;
    }

    @SuppressWarnings("unchecked")
    private final static <T> List<T> castToList(final T argumentValue)
    {
        return (List<T>) argumentValue;
    }

    /**
     * Finds out and executes the appropriate {@link IPredicate} for given <var>argument</var>.
     */
    public final static <T> Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final Argument<T> argument)
    {
        assert person != null : "Person unspecified";
        assert allowedRoles != null : "Allowed roles not specified";
        assert argument != null : "Argument not specified";
        final T argumentValue = argument.tryGetArgument();
        final Class<? extends IPredicate<?>> predicateClass =
                argument.getPredicateCandidate().guardClass();
        final Class<T> argumentType = argument.getType();
        ShouldFlattenCollections flattenAnnotation =
                predicateClass.getAnnotation(ShouldFlattenCollections.class);
        boolean shouldFlattenCollections =
                (null == flattenAnnotation) ? true : flattenAnnotation.value();
        return evaluate(person, allowedRoles, argumentValue, predicateClass, argumentType,
                shouldFlattenCollections);
    }

    @Private
    final static <T> Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T argumentValue,
            final Class<? extends IPredicate<?>> predicateClass, final Class<T> argumentType,
            final boolean shouldFlattenCollections)
    {
        assert authorizationDataProvider != null : "Authorization data provider not set";
        final IPredicate<T> predicate = createPredicate(predicateClass);
        if (List.class.isAssignableFrom(argumentType) && shouldFlattenCollections)
        {
            final CollectionPredicate<T> collectionPredicate =
                    new CollectionPredicate<T>(predicate);
            collectionPredicate.init(authorizationDataProvider);
            final List<T> list = castToList(argumentValue);
            try
            {
                return collectionPredicate.evaluate(person, allowedRoles, list);
            } catch (final ClassCastException e)
            {
                throw new IllegalArgumentException(String.format("Given predicate class '%s' "
                        + "and list type '%s' are not compatible.", predicateClass.getName(), list
                        .get(0).getClass().getName()));
            }
        }
        if (argumentType.isArray() && shouldFlattenCollections)
        {
            final ArrayPredicate<T> arrayPredicate = new ArrayPredicate<T>(predicate);
            arrayPredicate.init(authorizationDataProvider);
            try
            {
                return arrayPredicate.evaluate(person, allowedRoles, castToArray(argumentValue));
            } catch (final ClassCastException e)
            {
                throw new IllegalArgumentException(String.format("Given predicate class '%s' "
                        + "and array type '%s' are not compatible.", predicateClass.getName(),
                        argumentType.getComponentType().getName()));
            }
        }
        predicate.init(authorizationDataProvider);
        try
        {
            return predicate.evaluate(person, allowedRoles, argumentValue);
        } catch (final ClassCastException e)
        {
            throw new IllegalArgumentException(String.format("Given predicate class '%s' "
                    + "and argument type '%s' are not compatible.", predicateClass.getName(),
                    argumentType.getName()));
        }
    }

    private static final class AuthorizationDataProvider implements IAuthorizationDataProvider
    {
        private final IAuthorizationDAOFactory daoFactory;

        private final Map<String, DatabaseInstancePE> codeToDbInstanceMap =
                new HashMap<String, DatabaseInstancePE>();

        private final Map<String, DatabaseInstancePE> uuidToDbInstanceMap =
                new HashMap<String, DatabaseInstancePE>();

        private final DatabaseInstancePE homeDatabaseInstance;

        AuthorizationDataProvider(IAuthorizationDAOFactory daoFactory)
        {
            this.daoFactory = daoFactory;
            if (daoFactory != null) // Make unit tests work
            {
                this.homeDatabaseInstance = daoFactory.getDatabaseInstanceDAO().getHomeInstance();
                for (DatabaseInstancePE instance : daoFactory.getDatabaseInstanceDAO()
                        .listDatabaseInstances())
                {
                    codeToDbInstanceMap.put(instance.getCode(), instance);
                    uuidToDbInstanceMap.put(instance.getUuid(), instance);
                }
            } else
            {
                this.homeDatabaseInstance = null;
            }
        }

        public List<GroupPE> listGroups()
        {
            return daoFactory.getGroupDAO().listGroups();
        }

        public DatabaseInstancePE getHomeDatabaseInstance()
        {
            return homeDatabaseInstance;
        }

        public DatabaseInstancePE tryFindDatabaseInstanceByCode(String databaseInstanceCode)
        {
            return codeToDbInstanceMap.get(databaseInstanceCode);
        }

        public DatabaseInstancePE tryFindDatabaseInstanceByUUID(String databaseInstanceUUID)
        {
            return uuidToDbInstanceMap.get(databaseInstanceUUID);
        }

        public ProjectPE tryToGetProject(String dataSetCode)
        {
            DataPE dataSet = daoFactory.getExternalDataDAO().tryToFindDataSetByCode(dataSetCode);
            if (dataSet != null && dataSet.getExperiment() != null)
            {
                return dataSet.getExperiment().getProject();
            } else
            {
                return null;
            }
        }

        public DataSetAccessPE tryGetDatasetAccessData(String dataSetCode)
        {
            Set<DataSetAccessPE> results =
                    getDatasetCollectionAccessData(Arrays.asList(dataSetCode));
            if (results.size() < 1)
                return null;
            return results.iterator().next();
        }

        public Set<DataSetAccessPE> getDatasetCollectionAccessData(final List<String> dataSetCodes)
        {

            Session sess = daoFactory.getSessionFactory().getCurrentSession();
            final Query query = sess.getNamedQuery(DataSetAccessPE.DATASET_ACCESS_QUERY_NAME);
            query.setReadOnly(true);

            // WORKAROUND Problem in Hibernate when the number of data set codes > 1000
            // Though this query runs quickly within the pgadmin tool, even for large numbers of
            // data set codes, Hibernate becomes *very* slow when the size of the data set codes
            // exceeds 1000. For that reason, break down the query into smaller sections and
            // reassemble the results.
            final Set<DataSetAccessPE> fullResults = new HashSet<DataSetAccessPE>();

            BatchOperationExecutor.executeInBatches(new IBatchOperation<String>()
                {
                    public void execute(List<String> entities)
                    {
                        query.setParameterList(DataSetAccessPE.DATA_SET_CODES_PARAMETER_NAME,
                                entities);
                        List<DataSetAccessPE> results = cast(query.list());
                        fullResults.addAll(results);
                    }

                    public List<String> getAllEntities()
                    {
                        return dataSetCodes;
                    }

                    public String getEntityName()
                    {
                        return "dataset";
                    }

                    public String getOperationName()
                    {
                        return "authorization";
                    }
                });

            return fullResults;
        }

        public Set<SampleAccessPE> getSampleCollectionAccessData(final List<TechId> sampleTechIds)
        {
            Session sess = daoFactory.getSessionFactory().getCurrentSession();
            final Query querySpaceSamples =
                    sess.getNamedQuery(SampleAccessPE.SPACE_SAMPLE_ACCESS_QUERY_NAME);
            querySpaceSamples.setReadOnly(true);
            final Query querySharedSamples =
                    sess.getNamedQuery(SampleAccessPE.SHARED_SAMPLE_ACCESS_QUERY_NAME);
            querySharedSamples.setReadOnly(true);

            final Set<SampleAccessPE> fullResults = new HashSet<SampleAccessPE>();

            BatchOperationExecutor.executeInBatches(new IBatchOperation<TechId>()
                {
                    public void execute(List<TechId> entities)
                    {
                        querySpaceSamples.setParameterList(
                                SampleAccessPE.SAMPLE_IDS_PARAMETER_NAME, TechId.asLongs(entities));
                        querySharedSamples.setParameterList(
                                SampleAccessPE.SAMPLE_IDS_PARAMETER_NAME, TechId.asLongs(entities));
                        List<SampleAccessPE> spaceSamples = cast(querySpaceSamples.list());
                        List<SampleAccessPE> sharedSamples = cast(querySharedSamples.list());
                        fullResults.addAll(spaceSamples);
                        fullResults.addAll(sharedSamples);
                    }

                    public List<TechId> getAllEntities()
                    {
                        return sampleTechIds;
                    }

                    public String getEntityName()
                    {
                        return "sample";
                    }

                    public String getOperationName()
                    {
                        return "authorization";
                    }
                });

            return fullResults;
        }

        public GroupPE tryToGetGroup(SpaceOwnerKind kind, TechId techId)
        {
            switch (kind)
            {
                case DATASET:
                    ExternalDataPE dataset = daoFactory.getExternalDataDAO().getByTechId(techId);
                    return dataset.getExperiment().getProject().getGroup();
                case EXPERIMENT:
                    ExperimentPE experiment = daoFactory.getExperimentDAO().getByTechId(techId);
                    return experiment.getProject().getGroup();
                case SPACE:
                    GroupPE group = daoFactory.getGroupDAO().getByTechId(techId);
                    return group;
                case PROJECT:
                    ProjectPE project = daoFactory.getProjectDAO().getByTechId(techId);
                    return project.getGroup();
            }
            return null;
        }

        public SamplePE getSample(TechId techId)
        {
            return daoFactory.getSampleDAO().getByTechId(techId);
        }

        public SamplePE getSample(PermId id)
        {
            return daoFactory.getSampleDAO().tryToFindByPermID(id.getId());
        }

        public GridCustomFilterPE getGridCustomFilter(TechId techId)
        {
            return daoFactory.getGridCustomFilterDAO().getByTechId(techId);
        }

        public GridCustomColumnPE getGridCustomColumn(TechId techId)
        {
            return daoFactory.getGridCustomColumnDAO().getByTechId(techId);
        }

        public QueryPE getQuery(TechId techId)
        {
            return daoFactory.getQueryDAO().getByTechId(techId);
        }

        /**
         * Casts given <var>list</var> to specified type.
         * <p>
         * The purpose of this method is to avoid <code>SuppressWarnings("unchecked")</code> in
         * calling methods.
         * </p>
         */
        @SuppressWarnings("unchecked")
        private static final <T> List<T> cast(final List list)
        {
            return list;
        }

    }
}
