package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping.DataSetTypeGroup;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping.ExperimentGroup;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping.Grouper;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping.ProjectGroup;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping.TreeNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

public class ByExpermientPolicy implements IAutoArchiverPolicy
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ByExpermientPolicy.class);

    private static final String MINIMAL_ARCHIVE_SIZE = "minimal-archive-size";

    private static final String MAXIMAL_ARCHIVE_SIZE = "maximal-archive-size";

    private static final long DEFAULT_MINIMAL_ARCHIVE_SIZE = 0;

    private static final long DEFAULT_MAXIMAL_ARCHIVE_SIZE = Long.MAX_VALUE;

    private long minArchiveSize;

    private long maxArchiveSize;

    private IDataSetPathInfoProvider pathInfoProvider;

    public ByExpermientPolicy(ExtendedProperties properties)
    {
        minArchiveSize =
                PropertyUtils.getLong(properties, MINIMAL_ARCHIVE_SIZE, DEFAULT_MINIMAL_ARCHIVE_SIZE);

        maxArchiveSize =
                PropertyUtils.getLong(properties, MAXIMAL_ARCHIVE_SIZE, DEFAULT_MAXIMAL_ARCHIVE_SIZE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AbstractExternalData> filter(List<AbstractExternalData> dataSets)
    {
        ProjectGroup prjMap = groupDatasets(dataSets);

        return walkAndFind((Grouper<? extends TreeNode, ? extends TreeNode>) prjMap);
    }

    private List<AbstractExternalData> walkAndFind(Grouper<? extends TreeNode, ? extends TreeNode> tree)
    {
        for (TreeNode node : tree.values())
        {
            long size = node.getCumulatedSize();
            if (size >= minArchiveSize)
            {
                TreeNode kid = node;
                if (size <= maxArchiveSize)
                {
                    return reportFind(kid.collectSubTree());
                }

                if (kid instanceof Grouper)
                {
                    @SuppressWarnings("unchecked")
                    Grouper<TreeNode, TreeNode> subtree = (Grouper<TreeNode, TreeNode>) kid;
                    List<AbstractExternalData> found = walkAndFind(subtree);
                    if (found.size() > 0)
                    {
                        return found;
                    }

                    // no individual subtree is bigger than min-size but this branch is so let's have a subset
                    return splitDataset(kid.collectSubTree());

                } else
                {
                    DatasetListWithTotal bigDataset = (DatasetListWithTotal) kid;
                    sortBySamples(bigDataset);
                    return splitDataset(bigDataset);
                }
            }
        }
        return new ArrayList<AbstractExternalData>();
    }

    private List<AbstractExternalData> reportFind(List<AbstractExternalData> list)
    {
        long total = 0;
        for (AbstractExternalData ds : list)
        {
            total += ds.getSize();
            operationLog.info("added ds " + ds.getCode() + " for exp " + ds.getExperiment().getCode() + " with size " + ds.getSize() + " and total "
                    + total);
        }

        return list;
    }

    private void sortBySamples(DatasetListWithTotal datasets)
    {
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
    }

    private List<AbstractExternalData> splitDataset(List<AbstractExternalData> datasets)
    {
        DatasetListWithTotal result = new DatasetListWithTotal();

        for (AbstractExternalData ds : datasets)
        {
            if (result.getCumulatedSize() + ds.getSize() > maxArchiveSize)
            {
                continue; // optimistically try to fit as much as possible
            }

            result.add(ds);
        }

        return reportFind(result);
    }

    private ProjectGroup groupDatasets(List<AbstractExternalData> dataSets)
    {
        ProjectGroup prjMap = new ProjectGroup();

        for (AbstractExternalData ds : dataSets)
        {
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
                Experiment exp = ds.getExperiment();
                if (exp == null)
                {
                    exp = ds.getSample().getExperiment();
                }

                Project prj = exp.getProject();
                DataSetType type = ds.getDataSetType();

                ExperimentGroup grpMap = prjMap.sureGet(prj);
                DataSetTypeGroup expMap = grpMap.sureGet(exp);
                DatasetListWithTotal dslist = expMap.sureGet(type);

                dslist.add(ds);

                expMap.addSize(size);
                grpMap.addSize(size);
                prjMap.addSize(size);

            } else
            {
                operationLog.warn("Failed determining data set size of " + ds.getCode() + ", cannot include it in archval candidates set.");
            }
        }

        return prjMap;
    }

    private IDataSetPathInfoProvider getDatasetPathInfoProvider()
    {
        if (pathInfoProvider == null)
        {
            pathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
        }
        return pathInfoProvider;
    }

}
