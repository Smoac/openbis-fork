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

package ch.systemsx.cisd.yeastx.utils;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Utility methods for parsing XML files.
 * 
 * @author Tomasz Pylak
 */
public class XmlUtils
{
    private static final Date EPOCH = new Date(0);

    private static DatatypeFactory DATATYPE_FACTORY = createDatatypeFactory();

    /**
     * Converts duration which is a text representation of {@link Duration}) object into a number of
     * seconds. Works only if the duration does not refer to months or years.
     * 
     * @param durationText should be a valid representation of {@link Duration}) object
     */
    public static double asSeconds(String durationText)
    {
        Duration duration = DATATYPE_FACTORY.newDuration(durationText);
        if (duration.getMonths() != 0 || duration.getYears() != 0)
        {
            throw new IllegalStateException("Cannot express a duration '" + durationText
                    + "' in a number of seconds, because it refers to months or years");
        }
        return duration.getTimeInMillis(EPOCH) / 1000.0;
    }

    private static DatatypeFactory createDatatypeFactory()
    {
        try
        {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex)
        {
            throw new EnvironmentFailureException("Cannot create DatatypeFactory instance: "
                    + ex.getMessage(), ex);
        }
    }

    /** Converts array of bytes to the array of floats using big-endian byte order. */
    public static float[] asFloats(byte[] bytes)
    {
        return NativeData.byteToFloat(bytes, ByteOrder.BIG_ENDIAN);
    }
}
