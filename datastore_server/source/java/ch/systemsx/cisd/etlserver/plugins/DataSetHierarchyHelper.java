/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Utility class useful in creating data set path.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetHierarchyHelper
{

    private static String NOT_DIRECTLY_CONNECTED = "NOT_DIRECTLY_CONNECTED";

    /**
     * For given {@link SimpleDataSetInformationDTO} creates relevant path part.
     * 
     * @return Instance_AAA/Group_BBB/Project_CCC...</code>
     */
    public static String createHierarchicalPath(SimpleDataSetInformationDTO data)
    {
        return buildPath(instance(data), group(data), project(data), experiment(data),
                dataSetType(data), sample(data), dataSet(data));
    }

    /**
     * Creates a {@link Set} of data set paths located inside <code>root</code>.
     */
    public static Set<String> extractPaths(File root)
    {
        HashSet<String> set = new HashSet<String>();
        addPaths(set, root, PathElementKey.Instance);
        return set;
    }

    @Private
    static void addPaths(HashSet<String> set, File dir, PathElementKey level)
    {
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File d : files)
            {
                if (d.getName().startsWith(createFilter(level)))
                {
                    if (level.tryGetNext() == null)
                    {
                        set.add(d.getAbsolutePath());
                    } else
                    {
                        addPaths(set, d, level.tryGetNext());
                    }
                }
            }
        }
    }

    private static String createFilter(PathElementKey pathElementKey)
    {
        return pathElementKey.name() + ENTITY_SEPARATOR;
    }

    enum PathElementKey
    {
        Dataset(null),

        Sample(Dataset),

        DataSetType(Sample),

        Experiment(DataSetType),

        Project(Experiment),

        Space(Project),

        Instance(Space);

        private PathElementKey next;

        private PathElementKey(PathElementKey next)
        {
            this.next = next;
        }

        PathElementKey tryGetNext()
        {
            return next;
        }

    }

    static class Pair
    {
        private final PathElementKey key;

        private final String value;

        public Pair(PathElementKey key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public PathElementKey getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }
    }

    @Private
    static final String ENTITY_SEPARATOR = "_";

    @Private
    static Pair tryExtractPair(String merged)
    {
        String[] splitted = merged.split(ENTITY_SEPARATOR, 2);
        PathElementKey value;
        try
        {
            value = PathElementKey.valueOf(splitted[0]);
        } catch (IllegalArgumentException ex)
        {
            value = null;
        }
        if (splitted.length != 2 || value == null)
        {
            return null;
        }
        return new Pair(value, splitted[1]);
    }

    static private String merge(PathElementKey key, String value)
    {
        return key.name() + ENTITY_SEPARATOR + value;
    }

    private static String buildPath(String... parts)
    {
        StringBuilder sb = new StringBuilder();
        for (String part : parts)
        {
            if (sb.length() != 0)
            {
                sb.append(File.separator);
            }
            sb.append(part);
        }
        return sb.toString();
    }

    private static String dataSetType(SimpleDataSetInformationDTO data)
    {
        return merge(PathElementKey.DataSetType, data.getDataSetType());
    }

    private static String instance(SimpleDataSetInformationDTO data)
    {
        return merge(PathElementKey.Instance, data.getDatabaseInstanceCode());
    }

    private static String group(SimpleDataSetInformationDTO data)
    {
        return merge(PathElementKey.Space, data.getGroupCode());
    }

    private static String sample(SimpleDataSetInformationDTO data)
    {
        String pathElementValue = data.getSampleCode();
        if (pathElementValue == null)
        {
            pathElementValue = NOT_DIRECTLY_CONNECTED;
        }
        return merge(PathElementKey.Sample, pathElementValue);
    }

    private static String project(SimpleDataSetInformationDTO data)
    {
        return merge(PathElementKey.Project, data.getProjectCode());
    }

    private static String experiment(SimpleDataSetInformationDTO data)
    {
        return merge(PathElementKey.Experiment, data.getExperimentCode());
    }

    private static String dataSet(SimpleDataSetInformationDTO data)
    {
        return merge(PathElementKey.Dataset, data.getDataSetCode());
    }

}