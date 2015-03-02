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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.main.TrackingBO.TrackingStateUpdateHelper;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Piotr Buczek
 * @author Manuel Kohler
 */
public class FileBasedTrackingDAOTest extends AbstractFileSystemTestCase
{
    private static final String DATABASE_FILE = "tracking-local-database";

    private static final String DATABASE_FILE_DATA_SETS = "tracking-sample-database";

    private TreeMap<String, Long> changedTrackingMap = new TreeMap<String, Long>();

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        super.setUp();
        FileUtilities.deleteRecursively(workingDirectory);
    }

    @Test
    public void testGetTrackingState()
    {
        changedTrackingMap.put("20150213144812415-60450886", 29877L);
        changedTrackingMap.put("20150226134806587-60452073", 30293L);
        String toBeProcessed = "1 2 3 4 5";
        String processed = "1 2 3";
        prepareDatabaseFile(changedTrackingMap, toBeProcessed, processed);

        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(DATABASE_FILE, DATABASE_FILE_DATA_SETS);
        TrackingStateDTO state = trackingDAO.getTrackingState();
        assertEquals(changedTrackingMap, state.getLastSeenDataSetIdMap());
        assertEquals(toBeProcessed, StringUtils.join(state
                .getAlreadyTrackedSampleIdsToBeProcessed(), " "));
        assertEquals(processed, StringUtils.join(state.getAlreadyTrackedSampleIdsProcessed(), " "));
    }

    @Test
    public void testCalcNewTrackingState()
    {
        TreeMap<String, Long> changedTrackingMap = new TreeMap<String, Long>();
        changedTrackingMap.put("20150213144812415-60450886", 29877L);
        changedTrackingMap.put("20150226134806587-60452073", 30293L);
        String toBeProcessed = "1 2 3 4 5";
        String processed = "1 2 3";
        prepareDatabaseFile(changedTrackingMap, toBeProcessed, processed);
        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(DATABASE_FILE, DATABASE_FILE_DATA_SETS);
        TrackingStateDTO state = trackingDAO.getTrackingState();

        HashMap<String, ArrayList<Long>> tmpTrackingMap = new HashMap<String, ArrayList<Long>>();
        ArrayList<Long> techIds = new ArrayList<Long>();
        techIds.add(32000L);
        techIds.add(33000L);
        techIds.add(29877L);
        tmpTrackingMap.put("20150213144812415-60450886", techIds);

        techIds = new ArrayList<Long>();
        techIds.add(32001L);
        techIds.add(33001L);
        tmpTrackingMap.put("20150226134806587-60452073", techIds);

        techIds = new ArrayList<Long>();
        techIds.add(31000L);
        techIds.add(34000L);
        tmpTrackingMap.put("20150226134806587-90000000", techIds);

        TrackedEntities changedEntities =
                new TrackedEntities(createSamplesWithIds(6, 7), createSamplesWithIds(4),
                        createDataSetsWithIds(33000L));
        TrackingStateDTO newState =
                TrackingStateUpdateHelper.calcNewTrackingState(state, changedEntities, tmpTrackingMap);

        TreeMap<String, Long> expectedTrackingMap = new TreeMap<String, Long>();

        expectedTrackingMap.put("20150213144812415-60450886", 33000L);
        expectedTrackingMap.put("20150226134806587-60452073", 33001L);
        expectedTrackingMap.put("20150226134806587-90000000", 34000L);

        assertEquals(expectedTrackingMap, newState.getLastSeenDataSetIdMap());

        assertEquals("1 2 3 4 5 6 7", StringUtils.join(newState
                .getAlreadyTrackedSampleIdsToBeProcessed(), " "));
        assertEquals("1 2 3 4", StringUtils.join(newState.getAlreadyTrackedSampleIdsProcessed(),
                " "));
    }

    @Test
    public void testSaveTrackingState()
    {
        changedTrackingMap.put("20150213144812415-60450886", 29877L);
        changedTrackingMap.put("20150226134806587-60452073", 30293L);
        String toBeProcessed = "1 2";
        String processed = "1";
        prepareDatabaseFile(changedTrackingMap, toBeProcessed, processed);

        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(DATABASE_FILE, DATABASE_FILE_DATA_SETS);
        TrackingStateDTO newState = new TrackingStateDTO();

        changedTrackingMap.put("20150226134806587-60452099", 31296L);
        newState.setLastSeenDataSetIdMap(changedTrackingMap);
        newState.setAlreadyTrackedSampleIdsToBeProcessed(new TreeSet<Long>(Arrays.asList(new Long[]
        { 1L, 2L, 3L, 4L })));
        newState.setAlreadyTrackedSampleIdsProcessed(new TreeSet<Long>(Arrays.asList(new Long[]
        { 1L, 2L, 3L })));
        trackingDAO.saveTrackingState(newState);

        TrackingStateDTO loadesState = trackingDAO.getTrackingState();
        assertEquals(changedTrackingMap, loadesState.getLastSeenDataSetIdMap());
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

    private static List<AbstractExternalData> createDataSetsWithIds(long... ids)
    {
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (long id : ids)
        {
            result.add(createDataSetWithId(id));
        }
        return result;
    }

    private static PhysicalDataSet createDataSetWithId(long id)
    {
        PhysicalDataSet result = new PhysicalDataSet();
        result.setId(id);
        return result;
    }

    private void prepareDatabaseFile(TreeMap<String, Long> changedTrackingMap, String samplesToBeProcessed,
            String processedSamples)
    {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Long> entry : changedTrackingMap.entrySet())
        {
            sb.append(entry.getKey() + FileBasedTrackingDAO.EQUAL + entry.getValue() + "\n");
        }
        System.out.println(sb.toString());
        FileUtilities.writeToFile(new File(DATABASE_FILE_DATA_SETS), sb.toString());

        sb = new StringBuilder();
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
