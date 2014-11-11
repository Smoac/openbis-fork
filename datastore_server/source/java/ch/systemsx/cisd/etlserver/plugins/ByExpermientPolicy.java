package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.plugins.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.etlserver.plugins.grouping.GroupByExperimentAndTypeCriteria;
import ch.systemsx.cisd.etlserver.plugins.grouping.GroupByProjectCriteria;
import ch.systemsx.cisd.etlserver.plugins.grouping.GroupCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public class ByExpermientPolicy extends BaseGroupingPolicy implements IAutoArchiverPolicy
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ByExpermientPolicy.class);

    public ByExpermientPolicy(ExtendedProperties properties)
    {
        super(properties);
    }

    @Override
    public List<AbstractExternalData> filter(List<AbstractExternalData> dataSets)
    {
        List<GroupCriteria> criterias = new ArrayList<GroupCriteria>();
        criterias.add(new GroupByExperimentAndTypeCriteria());
        criterias.add(new GroupByProjectCriteria());

        for (GroupCriteria criteria : criterias)
        {
            Collection<DatasetListWithTotal> result = applyCriteria(dataSets, criteria);
            if (result.size() > 0)
            {
                TreeSet<DatasetListWithTotal> sorted = new TreeSet<DatasetListWithTotal>(result);
                DatasetListWithTotal best = sorted.last();
                long size = best.getCumulatedSize();
                if (size > minArchiveSize)
                {
                    if (size < maxArchiveSize)
                    {
                        return best;
                    }

                    sortBySamples(best);
                    return splitDatasets(best);
                }
            }
        }
        
        return Collections.emptyList();
    }

    private Collection<DatasetListWithTotal> applyCriteria(List<AbstractExternalData> dataSets, GroupCriteria criteria)
    {
        Map<String, DatasetListWithTotal> result = new HashMap<String, DatasetListWithTotal>();

        for (AbstractExternalData ds : dataSets)
        {
            String key = criteria.group(ds);

            DatasetListWithTotal list = result.get(key);

            if (list == null)
            {
                result.put(key, list = new DatasetListWithTotal());
            }

            Long size = ds.getSize();
            if (size == null)
            {
                size = patchDatasetSize(ds);
            }

            if (size != null)
            {
                list.add(ds);
            } else
            {
                operationLog.warn("Failed determining data set size of " + ds.getCode() + ", cannot include it in archval candidates set.");
            }
        }
        return result.values();
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
}
