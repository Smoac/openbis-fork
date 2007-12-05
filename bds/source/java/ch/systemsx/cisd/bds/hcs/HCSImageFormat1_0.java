/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.hcs;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.IFormatParameterFactory;
import ch.systemsx.cisd.bds.Version;

/**
 * <code>Format</code> extension for <i>HCS (High-Content Screening) with Images</i>.
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageFormat1_0 extends Format
{

    private static final String FORMAT_CODE = "HCS_IMAGE";

    /** Key for setting the measurement device that was used to take the data of this data set. */
    public final static String DEVICE_ID = "device_id";

    /**
     * Flag ({@link Boolean#TRUE} or {@link Boolean#FALSE}) specifying whether the data directory contains the
     * original data or not.
     */
    public final static String CONTAINS_ORIGINAL_DATA = "contains_original_data";

    /**
     * The format parameters that must be defined so that this implementation is able to work properly.
     * <p>
     * These parameters are located in <code>metadata/parameters</code>.
     * </p>
     */
    private final static String[] FORMAT_PARAMETERS =
            new String[]
                { PlateGeometry.PLATE_GEOMETRY, WellGeometry.WELL_GEOMETRY, ChannelList.NUMBER_OF_CHANNELS,
                        CONTAINS_ORIGINAL_DATA };

    /**
     * The one and only one instance.
     */
    public static final Format HCS_IMAGE_1_0 = new HCSImageFormat1_0();

    private HCSImageFormat1_0()
    {
        super(FORMAT_CODE, new Version(1, 0), null);
    }

    //
    // Format
    //

    @Override
    public final List<String> getParameterNames()
    {
        return Arrays.asList(FORMAT_PARAMETERS);
    }

    @Override
    public final IFormatParameterFactory getFormatParameterFactory()
    {
        return FormatParameterFactory.getInstance();
    }

}
