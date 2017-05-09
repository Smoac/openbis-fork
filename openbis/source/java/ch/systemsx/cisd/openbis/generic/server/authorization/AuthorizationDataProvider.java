package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

// TODO 2010-07-14, Piotr Buczek: write tests for nontrivial methods
final public class AuthorizationDataProvider implements IAuthorizationDataProvider
{
    private final IAuthorizationDAOFactory daoFactory;

    public AuthorizationDataProvider(IAuthorizationDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public IAuthorizationDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    @Override
    public IAuthorizationConfig getAuthorizationConfig()
    {
        return daoFactory.getAuthorizationConfig();
    }

    @Override
    public List<SpacePE> listSpaces()
    {
        return daoFactory.getSpaceDAO().listSpaces();
    }

    @Override
    public SpacePE tryGetSpace(String spaceCode)
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
    }

    @Override
    public ExperimentPE tryGetExperimentByPermId(String permId)
    {
        return daoFactory.getExperimentDAO().tryGetByPermID(permId);
    }

    @Override
    public SamplePE tryGetSampleByPermId(String permId)
    {
        return daoFactory.getSampleDAO().tryToFindByPermID(permId);
    }

    @Override
    public ProjectPE tryGetProjectByPermId(PermId permId)
    {
        return daoFactory.getProjectDAO().tryGetByPermID(permId.getId());
    }

    @Override
    public ProjectPE tryGetProjectByTechId(TechId techId)
    {
        return daoFactory.getProjectDAO().tryGetByTechId(techId);
    }

    @Override
    public Map<TechId, ProjectPE> tryGetProjectsByTechIds(Collection<TechId> techIds)
    {
        List<ProjectPE> projects = daoFactory.getProjectDAO().listByIDs(TechId.asLongs(techIds));

        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();

        for (ProjectPE project : projects)
        {
            map.put(new TechId(project.getId()), project);
        }

        return map;
    }

    @Override
    public ProjectPE tryGetProjectByIdentifier(ProjectIdentifier identifier)
    {
        return daoFactory.getProjectDAO().tryFindProject(
                identifier.getSpaceCode(), identifier.getProjectCode());
    }

    @Override
    public ProjectPE tryGetProjectForDataSet(String dataSetCode)
    {
        DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(dataSetCode);
        if (dataSet != null && dataSet.getExperiment() != null)
        {
            return dataSet.getExperiment().getProject();
        } else
        {
            return null;
        }
    }

    @Override
    public DataSetAccessPE tryGetDatasetAccessData(String dataSetCode)
    {
        Set<DataSetAccessPE> results = getDatasetCollectionAccessData(Arrays.asList(dataSetCode));
        if (results.size() < 1)
            return null;
        return results.iterator().next();
    }

    @Override
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
                @Override
                public void execute(List<String> entities)
                {
                    query.setParameterList(DataSetAccessPE.DATA_SET_CODES_PARAMETER_NAME, entities);
                    List<DataSetAccessPE> results = cast(query.list());
                    fullResults.addAll(results);
                }

                @Override
                public List<String> getAllEntities()
                {
                    return dataSetCodes;
                }

                @Override
                public String getEntityName()
                {
                    return "dataset";
                }

                @Override
                public String getOperationName()
                {
                    return "authorization";
                }
            });

        return fullResults;
    }

    @Override
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
                @Override
                public void execute(List<TechId> entities)
                {
                    querySpaceSamples.setParameterList(SampleAccessPE.SAMPLE_IDS_PARAMETER_NAME,
                            TechId.asLongs(entities));
                    querySharedSamples.setParameterList(SampleAccessPE.SAMPLE_IDS_PARAMETER_NAME,
                            TechId.asLongs(entities));
                    List<SampleAccessPE> spaceSamples = cast(querySpaceSamples.list());
                    List<SampleAccessPE> sharedSamples = cast(querySharedSamples.list());
                    fullResults.addAll(spaceSamples);
                    fullResults.addAll(sharedSamples);
                }

                @Override
                public List<TechId> getAllEntities()
                {
                    return sampleTechIds;
                }

                @Override
                public String getEntityName()
                {
                    return "sample";
                }

                @Override
                public String getOperationName()
                {
                    return "authorization";
                }
            });

        return fullResults;
    }

    @Override
    public Set<DataSetAccessPE> getDeletedDatasetCollectionAccessData(final List<TechId> deletionIds)
    {
        Session sess = daoFactory.getSessionFactory().getCurrentSession();
        final Query query = sess.getNamedQuery(DataSetAccessPE.DELETED_DATASET_ACCESS_QUERY_NAME);
        query.setReadOnly(true);

        // WORKAROUND Problem in Hibernate when the number of data set codes > 1000
        // Though this query runs quickly within the pgadmin tool, even for large numbers of
        // data set codes, Hibernate becomes *very* slow when the size of the data set codes
        // exceeds 1000. For that reason, break down the query into smaller sections and
        // reassemble the results.
        final Set<DataSetAccessPE> fullResults = new HashSet<DataSetAccessPE>();

        BatchOperationExecutor.executeInBatches(new IBatchOperation<TechId>()
            {
                @Override
                public void execute(List<TechId> entities)
                {
                    query.setParameterList(DataSetAccessPE.DELETION_IDS_PARAMETER_NAME,
                            TechId.asLongs(entities));
                    List<DataSetAccessPE> results = cast(query.list());
                    fullResults.addAll(results);
                }

                @Override
                public List<TechId> getAllEntities()
                {
                    return deletionIds;
                }

                @Override
                public String getEntityName()
                {
                    return "deletion";
                }

                @Override
                public String getOperationName()
                {
                    return "authorization";
                }
            });

        return fullResults;
    }

    @Override
    public Set<SampleAccessPE> getDeletedSampleCollectionAccessData(final List<TechId> deletionIds)
    {
        Session sess = daoFactory.getSessionFactory().getCurrentSession();
        final Query querySpaceSamples =
                sess.getNamedQuery(SampleAccessPE.DELETED_SPACE_SAMPLE_ACCESS_QUERY_NAME);
        querySpaceSamples.setReadOnly(true);
        final Query querySharedSamples =
                sess.getNamedQuery(SampleAccessPE.DELETED_SHARED_SAMPLE_ACCESS_QUERY_NAME);
        querySharedSamples.setReadOnly(true);

        final Set<SampleAccessPE> fullResults = new HashSet<SampleAccessPE>();

        BatchOperationExecutor.executeInBatches(new IBatchOperation<TechId>()
            {
                @Override
                public void execute(List<TechId> entities)
                {
                    querySpaceSamples.setParameterList(SampleAccessPE.DELETION_IDS_PARAMETER_NAME,
                            TechId.asLongs(entities));
                    querySharedSamples.setParameterList(SampleAccessPE.DELETION_IDS_PARAMETER_NAME,
                            TechId.asLongs(entities));
                    List<SampleAccessPE> spaceSamples = cast(querySpaceSamples.list());
                    List<SampleAccessPE> sharedSamples = cast(querySharedSamples.list());
                    fullResults.addAll(spaceSamples);
                    fullResults.addAll(sharedSamples);
                }

                @Override
                public List<TechId> getAllEntities()
                {
                    return deletionIds;
                }

                @Override
                public String getEntityName()
                {
                    return "deletion";
                }

                @Override
                public String getOperationName()
                {
                    return "authorization";
                }
            });

        return fullResults;
    }

    @Override
    public Set<ExperimentAccessPE> getDeletedExperimentCollectionAccessData(
            final List<TechId> deletionIds)
    {
        Session sess = daoFactory.getSessionFactory().getCurrentSession();
        final Query query =
                sess.getNamedQuery(ExperimentAccessPE.DELETED_EXPERIMENT_ACCESS_QUERY_NAME);
        query.setReadOnly(true);

        // WORKAROUND Problem in Hibernate when the number of data set codes > 1000
        // Though this query runs quickly within the pgadmin tool, even for large numbers of
        // data set codes, Hibernate becomes *very* slow when the size of the data set codes
        // exceeds 1000. For that reason, break down the query into smaller sections and
        // reassemble the results.
        final Set<ExperimentAccessPE> fullResults = new HashSet<ExperimentAccessPE>();

        BatchOperationExecutor.executeInBatches(new IBatchOperation<TechId>()
            {
                @Override
                public void execute(List<TechId> entities)
                {
                    query.setParameterList(ExperimentAccessPE.DELETION_IDS_PARAMETER_NAME,
                            TechId.asLongs(entities));
                    List<ExperimentAccessPE> results = cast(query.list());
                    fullResults.addAll(results);
                }

                @Override
                public List<TechId> getAllEntities()
                {
                    return deletionIds;
                }

                @Override
                public String getEntityName()
                {
                    return "deletion";
                }

                @Override
                public String getOperationName()
                {
                    return "authorization";
                }
            });

        return fullResults;
    }

    @Override
    public SpacePE tryGetSpace(SpaceOwnerKind kind, TechId techId)
    {
        switch (kind)
        {
            case DATASET:
                DataPE dataset = daoFactory.getDataDAO().getByTechId(techId);
                return dataset.getSpace();
            case EXPERIMENT:
                ExperimentPE experiment = daoFactory.getExperimentDAO().getByTechId(techId);
                return experiment.getProject().getSpace();
            case SPACE:
                SpacePE space = daoFactory.getSpaceDAO().getByTechId(techId);
                return space;
            case PROJECT:
                ProjectPE project = daoFactory.getProjectDAO().getByTechId(techId);
                return project.getSpace();
        }
        return null;
    }

    @Override
    public Set<SpacePE> getDistinctSpacesByEntityIds(SpaceOwnerKind kind, List<TechId> techIds)
    {
        Set<SpacePE> spaces = new HashSet<SpacePE>();
        List<Long> ids = TechId.asLongs(techIds);
        switch (kind)
        {
            case DATASET:
                spaces.addAll(daoFactory.getDataDAO().listSpacesByDataSetIds(ids));
                break;
            case EXPERIMENT:
                spaces.addAll(daoFactory.getExperimentDAO().listSpacesByExperimentIds(ids));
                break;
            case SPACE:
                List<SpacePE> allSpaces = daoFactory.getSpaceDAO().listSpaces();
                Set<Long> idSet = new HashSet<Long>(ids);
                for (SpacePE space : allSpaces)
                {
                    if (idSet.contains(space.getId()))
                    {
                        spaces.add(space);
                    }
                }
                break;
            case PROJECT:
                List<ProjectPE> projects = daoFactory.getProjectDAO().listProjects();
                Set<Long> idSet2 = new HashSet<Long>(ids);
                for (ProjectPE project : projects)
                {
                    if (idSet2.contains(project.getId()))
                    {
                        spaces.add(project.getSpace());
                    }
                }
                break;
        }
        return spaces;
    }

    @Override
    public SamplePE getSample(TechId techId)
    {
        return daoFactory.getSampleDAO().getByTechId(techId);
    }

    @Override
    public GridCustomFilterPE getGridCustomFilter(TechId techId)
    {
        return daoFactory.getGridCustomFilterDAO().getByTechId(techId);
    }

    @Override
    public GridCustomColumnPE getGridCustomColumn(TechId techId)
    {
        return daoFactory.getGridCustomColumnDAO().getByTechId(techId);
    }

    @Override
    public QueryPE getQuery(TechId techId)
    {
        return daoFactory.getQueryDAO().getByTechId(techId);
    }

    @Override
    public List<DeletionPE> getDeletions(List<TechId> deletionIds)
    {
        return daoFactory.getDeletionDAO().findAllById(TechId.asLongs(deletionIds));
    }

    @Override
    public MetaprojectPE getMetaproject(TechId id)
    {
        return daoFactory.getMetaprojectDAO().getByTechId(id);
    }

    /**
     * Casts given <var>list</var> to specified type.
     * <p>
     * The purpose of this method is to avoid <code>SuppressWarnings("unchecked")</code> in calling methods.
     * </p>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final <T> List<T> cast(final List list)
    {
        return list;
    }
}
