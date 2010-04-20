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
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;

/**
 * @author Tomasz Pylak
 */
public class FileBasedTrackingDAO implements ITrackingDAO
{
    private final String filePath;

    public FileBasedTrackingDAO(String filePath)
    {
        this.filePath = filePath;
    }

    public void saveTrackingState(TrackingStateDTO state)
    {
        List<String> lines = new ArrayList<String>();
        lines.add("lastSeenDatasetId\t" + state.getLastSeenDatasetId());
        lines.add("trackedSamplesToBeProcessed\t"
                + sampleIdsAsString(state.getAlreadyTrackedSampleIdsToBeProcessed()));
        lines.add("trackedSamplesProcessedSuccessfully\t"
                + sampleIdsAsString(state.getAlreadyTrackedSampleIdsProcessed()));
        writeLines(new File(filePath), lines);
    }

    private String sampleIdsAsString(Collection<Long> sampleIds)
    {
        return StringUtils.join(sampleIds, "\t");
    }

    @SuppressWarnings("unchecked")
    public TrackingStateDTO getTrackingState()
    {
        try
        {
            List<String> lines = IOUtils.readLines(new FileReader(filePath));
            if (lines.size() != 3)
            {
                throw LogUtils.environmentError("File %s should have exactly 3 rows.", filePath);
            }
            TrackingStateDTO state = new TrackingStateDTO();
            String[] datasetId = lines.get(0).split("\t");
            String[] toBeProcessed = lines.get(1).split("\t");
            String[] processed = lines.get(2).split("\t");
            state.setLastSeenDatasetId(Integer.parseInt(datasetId[1]));
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
