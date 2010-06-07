/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.shared.metadata;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.cina.shared.labview.Cluster;
import ch.systemsx.cisd.cina.shared.labview.DBL;
import ch.systemsx.cisd.cina.shared.labview.EW;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataBoolean;
import ch.systemsx.cisd.cina.shared.labview.LVDataString;
import ch.systemsx.cisd.cina.shared.labview.U32;
import ch.systemsx.cisd.cina.shared.labview.U8;

/**
 * Helper class for converting the content of a LabView XML file into a HashMap.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class LabViewXMLToHashMap
{
    private final LVData lvdata;

    private final Map<String, String> metadataMap;

    private final Map<String, String> prefixMap;

    /**
     * Constructor for converting lvdata into the metadataMap
     * 
     * @param lvdata The XML Data
     * @param metadataMap The HashMap to append the converted content into
     */
    LabViewXMLToHashMap(LVData lvdata, Map<String, String> metadataMap)
    {
        this(lvdata, metadataMap, new HashMap<String, String>());
    }

    /**
     * Constructor for converting lvdata into the metadataMap, specifying a mapping from cluster
     * prefixes to key prefixes
     * 
     * @param lvdata The XML Data
     * @param metadataMap The map to append the converted content into
     * @param prefixMap A map from cluster names to prefix names for keys in the resulting hash map
     */
    public LabViewXMLToHashMap(LVData lvdata, Map<String, String> metadataMap,
            Map<String, String> prefixMap)
    {
        this.lvdata = lvdata;
        this.metadataMap = metadataMap;
        this.prefixMap = prefixMap;
    }

    public void appendIntoMap()
    {
        for (Cluster cluster : lvdata.getClusters())
        {
            parseCluster(cluster, getClusterPrefix(cluster));
        }
    }

    private void parseCluster(Cluster cluster, String prefix)
    {
        for (LVDataString lvdataString : cluster.getStrings())
        {
            metadataMap.put(prefix + lvdataString.getName(), lvdataString.getValue());
        }

        for (U8 u8 : cluster.getU8s())
        {
            metadataMap.put(prefix + u8.getName(), u8.getValue().toString());
        }

        for (U32 u32 : cluster.getU32s())
        {
            metadataMap.put(prefix + u32.getName(), u32.getValue().toString());
        }

        for (DBL dbl : cluster.getDbls())
        {
            metadataMap.put(prefix + dbl.getName(), dbl.getValue().toString());
        }

        for (LVDataBoolean bool : cluster.getBooleans())
        {
            metadataMap.put(prefix + bool.getName(), bool.getValue().toString());
        }

        for (EW ew : cluster.getEws())
        {
            metadataMap.put(prefix + ew.getName(), ew.getChosenValue());
        }

        for (Cluster subcluster : cluster.getClusters())
        {
            parseCluster(subcluster, getClusterPrefix(subcluster));
        }
    }

    private String getClusterPrefix(Cluster cluster)
    {
        String clusterPrefix = prefixMap.get(cluster.getName());
        if (null == clusterPrefix)
        {
            clusterPrefix = "";
        }
        return clusterPrefix;
    }
}
