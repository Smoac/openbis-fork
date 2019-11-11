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

package ch.ethz.bsse.cisd.dsu.tracking.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.io.PropertyIOUtils;

/**
 * @author Tomasz Pylak
 * @author Manuel Kohler
 */
public class FileBasedTrackingDAO implements ITrackingDAO
{
    static String LAST_SEEN_DATASET_ID = "lastSeenDatasetId";

    static String TO_BE_PROCESSED = "trackedSamplesToBeProcessed";

    static String PROCESSED = "trackedSamplesProcessedSuccessfully";

    static String SEPARATOR = " ";

    static String EQUAL = "=";

    private final String filePathSampleDb;

    private final String filePathDatasetDb;

    public FileBasedTrackingDAO(String filePathSampleDb, String filePathDatasetDb)
    {
        this.filePathSampleDb = filePathSampleDb;
        this.filePathDatasetDb = filePathDatasetDb;
    }

    @Override
    public void saveTrackingState(TrackingStateDTO state)
    {
        List<String> lines = new ArrayList<String>();
        lines.add(TO_BE_PROCESSED + SEPARATOR
                + sampleIdsAsString(state.getAlreadyTrackedSampleIdsToBeProcessed()));
        lines.add(PROCESSED + SEPARATOR
                + sampleIdsAsString(state.getAlreadyTrackedSampleIdsProcessed()));

        writeLines(new File(filePathSampleDb), lines);

        lines = new ArrayList<String>();
        for (Map.Entry<String, Long> entry : state.getLastSeenDataSetIdMap().entrySet())
        {
            lines.add(entry.getKey() + EQUAL + entry.getValue());
        }
        writeLines(new File(filePathDatasetDb), lines);
    }

    private String sampleIdsAsString(Collection<Long> sampleIds)
    {
        return StringUtils.join(sampleIds, SEPARATOR);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TrackingStateDTO getTrackingState()
    {
        try
        {
            TrackingStateDTO state = new TrackingStateDTO();

            Properties props = PropertyIOUtils.loadProperties(filePathDatasetDb);
            TreeMap<String, Long> propsMap = new TreeMap<String, Long>();
            for (Object o : props.keySet())
            {
                propsMap.put(o.toString(), Long.parseLong(props.get(o).toString()));
            }
            state.setLastSeenProperties(props);
            state.setLastSeenDataSetIdMap(propsMap);

            List<String> lines = IOUtils.readLines(new FileReader(filePathSampleDb));
            String[] toBeProcessed = lines.get(0).split(SEPARATOR);
            String[] processed = lines.get(1).split(SEPARATOR);
            state.setAlreadyTrackedSampleIdsToBeProcessed(parseIds(toBeProcessed));
            state.setAlreadyTrackedSampleIdsProcessed(parseIds(processed));

            return state;
        } catch (Exception e)
        {
            throw LogUtils.environmentError("Incorrect file format", e);
        }
    }

    private static Set<Long> parseIds(String[] array)
    {
        Set<Long> ids = new TreeSet<Long>();
        for (int i = 1; i < array.length; i++)
        {
            ids.add(Long.parseLong(array[i]));
        }
        return ids;
    }

    private static void writeLines(File file, List<String> lines)
    {
        try
        {
            IOUtils.writeLines(lines, "\n", new FileOutputStream(file));
        } catch (IOException ex)
        {
            throw LogUtils.environmentError(String.format(
                    "Cannot save the file %s with content: %s", file.getPath(), lines), ex);
        }
    }

}
