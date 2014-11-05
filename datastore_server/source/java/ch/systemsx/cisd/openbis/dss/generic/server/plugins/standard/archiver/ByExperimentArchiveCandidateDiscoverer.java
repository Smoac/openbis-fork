package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Find archive candidates grouped by experiment so that old data belonging to the same experiment lands together in the same archive
 * 
 * @author Sascha Fedorenko
 */
public class ByExperimentArchiveCandidateDiscoverer implements IArchiveCandidateDiscoverer
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ByExperimentArchiveCandidateDiscoverer.class);

    private static final String MINIMAL_ARCHIVE_SIZE = "minimal-archive-size";

    private static final String MAXIMAL_ARCHIVE_SIZE = "maximal-archive-size";

    private static final long DEFAULT_MINIMAL_ARCHIVE_SIZE = 0;

    private static final long DEFAULT_MAXIMAL_ARCHIVE_SIZE = Long.MAX_VALUE;

    private long minArchiveSize;

    private long maxArchiveSize;

    private IDataSetPathInfoProvider pathInfoProvider;

    private static class DatasetArchInfo implements Comparable<DatasetArchInfo>
    {
        private List<AbstractExternalData> datasets = new ArrayList<AbstractExternalData>();

        private long totalSize = 0;

        public Date minDate = new Date();

        @Override
        public int compareTo(DatasetArchInfo other)
        {
            return minDate.compareTo(other.minDate);
        }
    }

    public ByExperimentArchiveCandidateDiscoverer(ExtendedProperties properties)
    {
        minArchiveSize =
                PropertyUtils.getLong(properties, MINIMAL_ARCHIVE_SIZE, DEFAULT_MINIMAL_ARCHIVE_SIZE);

        maxArchiveSize =
                PropertyUtils.getLong(properties, MAXIMAL_ARCHIVE_SIZE, DEFAULT_MAXIMAL_ARCHIVE_SIZE);
    }

    @Override
    public List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis, ArchiverDataSetCriteria criteria)
    {
        List<AbstractExternalData> dataSets = openbis.listAvailableDataSets(criteria);

        DatasetArchInfo[] sortedCandidates = organizeCandidates(dataSets);

        if (sortedCandidates.length == 0)
        {
            return new ArrayList<AbstractExternalData>(0);
        }

        SortedMap<Project, DatasetArchInfo> byProject = groupByProject(sortedCandidates);

        boolean hadGoodCandidates = false;
        for (Project p : byProject.keySet())
        {
            DatasetArchInfo projectSets = byProject.get(p);
            if (projectSets.totalSize > minArchiveSize)
            {
                hadGoodCandidates = true;
                if (projectSets.totalSize < maxArchiveSize)
                {
                    return reportFind(projectSets.datasets);
                }

                List<AbstractExternalData> projectSubset = selectSuitableSubsetBySample(projectSets.datasets);
                if (projectSubset.size() > 0)
                {
                    return reportFind(projectSubset);
                }
            }
        }

        if (hadGoodCandidates)
        {
            operationLog.info("Found datasets matching By Experiment archivation policy, but no subset fit within "
                    + "MINIMAL_ARCHIVE_SIZE and MAXIMAL_ARCHIVE_SIZE criteria.");
        }

        return new ArrayList<AbstractExternalData>();
    }

    private List<AbstractExternalData> reportFind(List<AbstractExternalData> datasets)
    {
        for (AbstractExternalData ds : datasets)
        {
            operationLog.info("Will archive " + ds.getCode() + " with experiment " + ds.getExperiment().getCode());
        }
        return datasets;
    }

    private SortedMap<Project, DatasetArchInfo> groupByProject(DatasetArchInfo[] sortedCandidates)
    {
        SortedMap<Project, DatasetArchInfo> result = new TreeMap<Project, DatasetArchInfo>();
        for (DatasetArchInfo info : sortedCandidates)
        {
            Project project = info.datasets.get(0).getExperiment().getProject();
            DatasetArchInfo current = result.get(project);
            if (current == null)
            {
                current = new DatasetArchInfo();
            }

            current.datasets.addAll(info.datasets);
            current.totalSize += info.totalSize;
            result.put(project, current);
        }
        return result;
    }

    private DatasetArchInfo[] organizeCandidates(List<AbstractExternalData> dataSets)
    {
        Map<Experiment, DatasetArchInfo> candidates = new HashMap<Experiment, DatasetArchInfo>();
        
        for (AbstractExternalData ds : dataSets)
        {
            Experiment experiment = ds.getExperiment();
            DatasetArchInfo candidate = candidates.get(experiment);
            if (candidate == null)
            {
                candidate = new DatasetArchInfo();
            }

            candidate.datasets.add(ds);

            Long size = ds.getSize();
            if (size == null)
            {
                ISingleDataSetPathInfoProvider dsInfoProvider = getDatasetPathInfoProvider().tryGetSingleDataSetPathInfoProvider(ds.getCode());
                if (dsInfoProvider != null)
                {
                    size = dsInfoProvider.getRootPathInfo().getSizeInBytes();
                    ds.setSize(size);
                }
            }

            if (size != null)
            {
                candidate.totalSize += size;
                if (candidate.minDate.compareTo(ds.getModificationDate()) > 0)
                {
                    candidate.minDate = ds.getModificationDate();
                }

                candidates.put(experiment, candidate);
            } else
            {
                operationLog.warn("Failed determining data set size of " + ds.getCode() + ", cannot include it in archval candidates set.");
            }
        }

        if (candidates.size() == 0)
        {
            return new DatasetArchInfo[0];
        }


        DatasetArchInfo[] sortedCandidates = candidates.values().toArray(new DatasetArchInfo[candidates.size()]);
        Arrays.sort(sortedCandidates);
        return sortedCandidates;
    }

    private IDataSetPathInfoProvider getDatasetPathInfoProvider()
    {
        if (pathInfoProvider == null)
        {
            pathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
        }
        return pathInfoProvider;
    }

    private List<AbstractExternalData> selectSuitableSubsetBySample(List<AbstractExternalData> datasets)
    {
        ArrayList<AbstractExternalData> result = new ArrayList<AbstractExternalData>();

        Collections.sort(datasets, new Comparator<AbstractExternalData>()
            {

                @Override
                public int compare(AbstractExternalData arg0, AbstractExternalData arg1)
                {
                    String sid1 = arg0.getSampleIdentifier();
                    sid1 = sid1 == null ? "" : sid1;
                    String sid2 = arg1.getSampleIdentifier();
                    sid2 = sid2 == null ? "" : sid2;
                    return sid1.compareTo(sid2);
                }
            });

        long curSize = 0;
        for (AbstractExternalData ds : datasets)
        {
            if (curSize + ds.getSize() > maxArchiveSize && curSize > minArchiveSize)
            {
                return result;
            }
            result.add(ds);
            curSize += ds.getSize();
        }

        if (curSize < minArchiveSize)
        {
            return new ArrayList<AbstractExternalData>();
        }

        return result;
    }
}
