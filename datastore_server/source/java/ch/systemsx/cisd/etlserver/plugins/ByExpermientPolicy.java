package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.plugins.grouping.DataSetTypeGroup;
import ch.systemsx.cisd.etlserver.plugins.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.etlserver.plugins.grouping.ExperimentGroup;
import ch.systemsx.cisd.etlserver.plugins.grouping.Grouper;
import ch.systemsx.cisd.etlserver.plugins.grouping.ProjectGroup;
import ch.systemsx.cisd.etlserver.plugins.grouping.TreeNode;
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

    public static final String MINIMAL_ARCHIVE_SIZE = "minimal-archive-size";

    public static final String MAXIMAL_ARCHIVE_SIZE = "maximal-archive-size";

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
                if (node instanceof DatasetListWithTotal)
                {
                    DatasetListWithTotal goodDatasetList = (DatasetListWithTotal) node;
                    if (size <= maxArchiveSize)
                    {
                        return goodDatasetList;
                    } else
                    {
                        sortBySamples(goodDatasetList);
                        return splitDatasets(goodDatasetList);
                    }
                } else
                {
                    @SuppressWarnings("unchecked")
                    Grouper<TreeNode, TreeNode> subtree = (Grouper<TreeNode, TreeNode>) node;
                    List<AbstractExternalData> found = walkAndFind(subtree);
                    if (found.size() > 0)
                    {
                        return found;
                    }

                    // no individual subtree is bigger than min-size but this branch is so let's have a subset
                    return splitDatasets(node.collectSubTree());
                }
            }
        }
        return new ArrayList<AbstractExternalData>();
    }

    private void sortBySamples(DatasetListWithTotal datasets)
    {
        Collections.sort(datasets, new SimpleComparator<AbstractExternalData, String>()
            {
                @Override
                public String evaluate(AbstractExternalData data)
                {
                    String sid1 = data.getSampleIdentifier();
                    return sid1 == null ? "" : sid1;
                }
            });
    }

    private List<AbstractExternalData> splitDatasets(List<AbstractExternalData> datasets)
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

        return result;
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
