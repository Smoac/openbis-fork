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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.main.TrackingBO.TrackingStateUpdateHelper;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Piotr Buczek
 */
public class FileBasedTrackingDAOTest extends AbstractFileSystemTestCase
{
    private static final String DATABASE_FILE = "tracking-local-database";

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        super.setUp();
        FileUtilities.deleteRecursively(workingDirectory);
    }

    @Test
    public void testGetTrackingState()
    {
        long lastSeenDatasetId = 0;
        String toBeProcessed = "1 2 3 4 5";
        String processed = "1 2 3";
        prepareDatabaseFile(lastSeenDatasetId, toBeProcessed, processed);

        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(DATABASE_FILE);
        TrackingStateDTO state = trackingDAO.getTrackingState();
        assertEquals(lastSeenDatasetId, state.getLastSeenDatasetId());
        assertEquals(toBeProcessed, StringUtils.join(state
                .getAlreadyTrackedSampleIdsToBeProcessed(), " "));
        assertEquals(processed, StringUtils.join(state.getAlreadyTrackedSampleIdsProcessed(), " "));
    }

    @Test
    public void testCalcNewTrackingState()
    {
        prepareDatabaseFile(300, "1 2 3 4 5", "1 2 3");
        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(DATABASE_FILE);
        TrackingStateDTO state = trackingDAO.getTrackingState();

        TrackedEntities changedEntities =
                new TrackedEntities(createSamplesWithIds(6, 7), createSamplesWithIds(4),
                        createDataSetsWithIds(400, 500));
        TrackingStateDTO newState =
                TrackingStateUpdateHelper.calcNewTrackingState(state, changedEntities);
        assertEquals(500, newState.getLastSeenDatasetId());
        assertEquals("1 2 3 4 5 6 7", StringUtils.join(newState
                .getAlreadyTrackedSampleIdsToBeProcessed(), " "));
        assertEquals("1 2 3 4", StringUtils.join(newState.getAlreadyTrackedSampleIdsProcessed(),
                " "));
    }

    @Test
    public void testSaveTrackingState()
    {
        prepareDatabaseFile(300, "1 2", "1");
        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(DATABASE_FILE);
        TrackingStateDTO newState = new TrackingStateDTO();
        newState.setLastSeenDatasetId(400);
        newState.setAlreadyTrackedSampleIdsToBeProcessed(new TreeSet<Long>(Arrays.asList(new Long[]
            { 1L, 2L, 3L, 4L })));
        newState.setAlreadyTrackedSampleIdsProcessed(new TreeSet<Long>(Arrays.asList(new Long[]
            { 1L, 2L, 3L })));
        trackingDAO.saveTrackingState(newState);

        TrackingStateDTO loadesState = trackingDAO.getTrackingState();
        assertEquals(400, loadesState.getLastSeenDatasetId());
        assertEquals("1 2 3 4", StringUtils.join(loadesState
                .getAlreadyTrackedSampleIdsToBeProcessed(), " "));
        assertEquals("1 2 3", StringUtils.join(loadesState.getAlreadyTrackedSampleIdsProcessed(),
                " "));
    }

    private static List<Sample> createSamplesWithIds(long... ids)
    {
        List<Sample> result = new ArrayList<Sample>();
        for (long id : ids)
        {
            result.add(createSampleWithId(id));
        }
        return result;
    }

    private static Sample createSampleWithId(long id)
    {
        Sample result = new Sample();
        result.setId(id);
        return result;
    }

    private static List<ExternalData> createDataSetsWithIds(long... ids)
    {
        List<ExternalData> result = new ArrayList<ExternalData>();
        for (long id : ids)
        {
            result.add(createDataSetWithId(id));
        }
        return result;
    }

    private static ExternalData createDataSetWithId(long id)
    {
        ExternalData result = new ExternalData();
        result.setId(id);
        return result;
    }

    private void prepareDatabaseFile(long lastSeenDatasetId, String samplesToBeProcessed,
            String processedSamples)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FileBasedTrackingDAO.LAST_SEEN_DATASET_ID);
        sb.append(FileBasedTrackingDAO.SEPARATOR);
        sb.append(lastSeenDatasetId);
        sb.append("\n");
        sb.append(FileBasedTrackingDAO.TO_BE_PROCESSED);
        sb.append(FileBasedTrackingDAO.SEPARATOR);
        sb.append(samplesToBeProcessed);
        sb.append("\n");
        sb.append(FileBasedTrackingDAO.PROCESSED);
        sb.append(FileBasedTrackingDAO.SEPARATOR);
        sb.append(processedSamples);
        sb.append("\n");
        FileUtilities.writeToFile(new File(DATABASE_FILE), sb.toString());
    }
}
