package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
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

    private static final int DEFAULT_MINIMAL_ARCHIVE_SIZE = 0;

    private static final int DEFAULT_MAXIMAL_ARCHIVE_SIZE = Integer.MAX_VALUE;

    private int minArchiveSize;

    private int maxArchiveSize;

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

    @Override
    public List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis, ArchiverDataSetCriteria criteria)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, criteria.tryGetDataSetTypeCode()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateBefore = dateFormat.format(DateUtils.addDays(new Date(), -criteria.getOlderThan()));
        sc.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.LESS_THAN_OR_EQUAL,
                dateBefore, "0"));

        // TODO: not yet archived
        // sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute., desiredValue));

        List<AbstractExternalData> dataSets = openbis.searchForDataSets(sc);

        Map<Project, DatasetArchInfo> candidates = new HashMap<Project, DatasetArchInfo>();
        
        for (AbstractExternalData ds : dataSets)
        {
            Project project = ds.getExperiment().getProject();
            DatasetArchInfo candidate = candidates.get(project);
            if (candidate == null)
            {
                candidate = new DatasetArchInfo();
            }

            candidate.datasets.add(ds);
            candidate.totalSize += ds.getSize();
            if (candidate.minDate.compareTo(ds.getModificationDate()) > 0)
            {
                candidate.minDate = ds.getModificationDate();
            }

            candidates.put(project, candidate);
        }

        DatasetArchInfo[] sortedCandidates = candidates.values().toArray(new DatasetArchInfo[candidates.size()]);
        Arrays.sort(sortedCandidates);

        for (DatasetArchInfo ai : sortedCandidates)
        {
            if (ai.totalSize > minArchiveSize)
            {
                if (ai.totalSize < maxArchiveSize)
                {
                    return ai.datasets;
                }

                return selectSuitableSubset(ai.datasets);
            }
        }

        operationLog.info("No dataset collection matches By Experiment archivation policy.");

        return new ArrayList<AbstractExternalData>();
    }

    private List<AbstractExternalData> selectSuitableSubset(List<AbstractExternalData> datasets)
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
            if (curSize + ds.getSize() > maxArchiveSize)
            {
                return result;
            }
            result.add(ds);
            curSize += ds.getSize();
        }

        operationLog.warn("Found datasets matching By Experiment archivation policy "
                + "but wasn't able to pick a subset for MAXIMAL_ARCHIVE_SIZE criteria.");

        return result;
    }

    @Override
    public void initialize(Properties properties)
    {
        minArchiveSize =
                PropertyUtils.getInt(properties, MINIMAL_ARCHIVE_SIZE, DEFAULT_MINIMAL_ARCHIVE_SIZE);

        maxArchiveSize =
                PropertyUtils.getInt(properties, MAXIMAL_ARCHIVE_SIZE, DEFAULT_MAXIMAL_ARCHIVE_SIZE);

    }

}
