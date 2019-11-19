/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ArchivingAggregationService extends AggregationService
{
    static final String METHOD_KEY = "method";
    static final String ARGS_KEY = "args";
    static final String GET_ARCHIVING_INFO_METHOD = "getArchivingInfo";
    private static final String DATA_SET_COLUMN = "DataSet";
    private static final String SIZE_COLUMN = "Size";
    private static final String CONTAINER_SIZE_COLUMN = "ContainerSize";
    private static final String CONTAINER_COLUMN = "Container";


    private static final long serialVersionUID = 1L;

    private IApplicationServerApi v3api;

    private IArchiverPlugin archiver;

    public ArchivingAggregationService(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null, null);
    }

    ArchivingAggregationService(Properties properties, File storeRoot, IApplicationServerApi v3api, IArchiverPlugin archiver)
    {
        super(properties, storeRoot);
        this.v3api = v3api;
        this.archiver = archiver;
    }
    
    @Override
    public TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context)
    {
        Object method = parameters.get(METHOD_KEY);
        List<String> arguments = getArguments(parameters);
        if (GET_ARCHIVING_INFO_METHOD.equals(method))
        {
            return getArchivingInfo(context.trySessionToken(), arguments);
        }
        throw new UserFailureException("Unknown method '" + method + "'.");
    }

    private TableModel getArchivingInfo(String sessionToken, List<String> dataSetCodes)
    {
        Map<String, Set<String>> containersByDataSetCode = getContainers(dataSetCodes);
        Set<String> allDataSets = mergeAllContainers(containersByDataSetCode);
        Map<String, Long> dataSetSizes = getDataSetSizes(sessionToken, allDataSets);
        Map<String, Long> containerSizes = getContainerSizes(containersByDataSetCode, dataSetSizes);
        long totalSize = getTotalSize(allDataSets, dataSetSizes);

        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(DATA_SET_COLUMN);
        builder.addHeader(SIZE_COLUMN);
        builder.addHeader(CONTAINER_COLUMN);
        builder.addHeader(CONTAINER_SIZE_COLUMN);
        Set<Entry<String, Set<String>>> entrySet = containersByDataSetCode.entrySet();
        for (Entry<String, Set<String>> entry : entrySet)
        {
            IRowBuilder row = builder.addRow();
            String dataSetCode = entry.getKey();
            row.setCell(DATA_SET_COLUMN, dataSetCode);
            row.setCell(SIZE_COLUMN, dataSetSizes.get(dataSetCode));
            row.setCell(CONTAINER_COLUMN, String.join(",", entry.getValue()));
            row.setCell(CONTAINER_SIZE_COLUMN, containerSizes.get(dataSetCode));
        }
        IRowBuilder row = builder.addRow();
        row.setCell(SIZE_COLUMN, totalSize);

        return builder.getTableModel();
    }

    private Map<String, Set<String>> getContainers(List<String> dataSetCodes)
    {
        Map<String, Set<String>> results = new TreeMap<String, Set<String>>();
        for (String dataSetCode : new HashSet<>(dataSetCodes))
        {
            results.put(dataSetCode, new TreeSet<>(getArchiver().getDataSetCodesForUnarchiving(Arrays.asList(dataSetCode))));
        }
        return results;
    }

    private Set<String> mergeAllContainers(Map<String, Set<String>> containers)
    {
        Set<String> result = new TreeSet<>();
        for (Set<String> set : containers.values())
        {
            result.addAll(set);
        }
        return result;
    }

    private Map<String, Long> getDataSetSizes(String sessionToken, Collection<String> dataSetCodes)
    {
        List<DataSetPermId> ids = dataSetCodes.stream().map(DataSetPermId::new).collect(Collectors.toList());
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        Map<String, Long> result = new TreeMap<>();
        for (DataSet dataSet : getv3api().getDataSets(sessionToken, ids, fetchOptions).values())
        {
            result.put(dataSet.getCode(), dataSet.getPhysicalData().getSize());
        }
        return result;
    }

    private Map<String, Long> getContainerSizes(Map<String, Set<String>> containersByDataSetCode, Map<String, Long> dataSetSizes)
    {
        Map<String, Long> result = new TreeMap<>();
        for (Entry<String, Set<String>> entry : containersByDataSetCode.entrySet())
        {
            long sum = 0;
            for (String dataSetCode : entry.getValue())
            {
                sum += dataSetSizes.get(dataSetCode);
            }
            result.put(entry.getKey(), sum);
        }
        return result;
    }

    private long getTotalSize(Set<String> allDataSets, Map<String, Long> dataSetSizes)
    {
        long sum = 0;
        for (String dataSetCode : allDataSets)
        {
            sum += dataSetSizes.get(dataSetCode);
        }
        return sum;
    }

    private List<String> getArguments(Map<String, Object> parameters)
    {
        List<String> result = new ArrayList<String>();
        Object args = parameters.get(ARGS_KEY);
        if (args instanceof String)
        {
            String[] splitted = ((String) args).split(",");
            for (String string : splitted)
            {
                result.add(string.trim());
            }
        }
        return result;
    }

    private IArchiverPlugin getArchiver()
    {
        if (archiver == null)
        {
            IDataStoreServiceInternal dataStoreService = ServiceProvider.getDataStoreService();
            archiver = dataStoreService.getArchiverPlugin();
        }
        return archiver;
    }

    private IApplicationServerApi getv3api()
    {
        if (v3api == null)
        {
            v3api = ServiceProvider.getV3ApplicationService();
        }
        return v3api;
    }
}
