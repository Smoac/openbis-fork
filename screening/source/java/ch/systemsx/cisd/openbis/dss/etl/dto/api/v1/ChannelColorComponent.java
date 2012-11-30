/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * This class is obsolete, and should not be used. Use
 * {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent} instead
 * 
 * @author Jakub Straszewski
 */
public enum ChannelColorComponent
{
    RED, GREEN, BLUE;

    public ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent getIndependentChannelColorComponent()
    {
        switch (this)
        {
            case BLUE:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.BLUE;
            case GREEN:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.GREEN;
            case RED:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.RED;
        }
        return null;
    }

    /**
     * Gets the list of enums which are either of this type or of the version independent
     * CahnnelColorComponent. Returns the list of the independent type
     */
    public static List<ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent> convertToIndependentChannelColorList(
            List<?> inputList)
    {
        List<ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent> results =
                new LinkedList<ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent>();
        for (Object o : inputList)
        {
            if (o instanceof ChannelColorComponent)
            {
                results.add(((ChannelColorComponent) o).getIndependentChannelColorComponent());
            } else if (o instanceof ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent)
            {
                results.add((ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent) o);
            } else
            {
                throw new IllegalArgumentException(
                        "List items must be of one of ChannelColorComponent types.");
            }
        }
        return results;
    }

    public static ColorComponent getColorComponent(ChannelColorComponent channelColorComponent)
    {
        switch (channelColorComponent)
        {
            case BLUE:
                return ColorComponent.BLUE;
            case GREEN:
                return ColorComponent.GREEN;
            case RED:
                return ColorComponent.RED;
        }
        return null;
    }
}
