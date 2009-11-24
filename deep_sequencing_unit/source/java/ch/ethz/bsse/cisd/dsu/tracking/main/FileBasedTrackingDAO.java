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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;

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
        lines.add("lastSeenSequencingSampleId\tlastSeenFlowLaneSampleId\tlastSeenDatasetId\n");
        lines.add("" + state.getLastSeenSequencingSampleId() + "\t"
                + state.getLastSeenFlowLaneSampleId() + "\t" + state.getLastSeenDatasetId());
        writeLines(new File(filePath), lines);
    }

    public TrackingStateDTO getTrackingState()
    {
        TabFileLoader<TrackingStateDTO> tabFileLoader =
                new TabFileLoader<TrackingStateDTO>(
                        new IParserObjectFactoryFactory<TrackingStateDTO>()
                            {
                                public IParserObjectFactory<TrackingStateDTO> createFactory(
                                        IPropertyMapper propertyMapper) throws ParserException
                                {
                                    return new AbstractParserObjectFactory<TrackingStateDTO>(
                                            TrackingStateDTO.class, propertyMapper)
                                        {
                                        };
                                }
                            });
        try
        {
            List<TrackingStateDTO> trackingState = tabFileLoader.load(new File(filePath));
            if (trackingState.size() != 1)
            {
                throw LogUtils.environmentError("File %s has to many rows, it should have exactly 1.",
                        filePath);
            }
            return trackingState.get(0);
        } catch (Exception e)
        {
            throw LogUtils.envErr("Incorrect file format", e);
        }
    }

    private static void writeLines(File file, List<String> lines)
    {
        try
        {
            IOUtils.writeLines(lines, "\n", new FileOutputStream(file));
        } catch (IOException ex)
        {
            throw LogUtils.envErr(String.format("Cannot save the file %s with content: %s",
                    file.getPath(), lines), ex);
        }
    }
}
