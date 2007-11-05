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

import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IFormatParameterFactory;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * A customized <code>IFormatParameterFactory</code> implementation suitable for <i>HCS (High-Content Screening) with
 * Images</i>.
 * 
 * @author Christian Ribeaud
 */
public final class FormatParameterFactory implements IFormatParameterFactory
{

    //
    // IFormatParameterFactory
    //

    public final FormatParameter createFormatParameter(final INode node)
    {
        assert node != null : "Given node can not be null.";
        final String nodeName = node.getName();
        if (node instanceof IDirectory)
        {
            final IDirectory directory = (IDirectory) node;
            if (nodeName.equals(PlateGeometry.PLATE_GEOMETRY))
            {
                return new FormatParameter(nodeName, PlateGeometry.loadFrom(directory));
            } else if (nodeName.equals(WellGeometry.WELL_GEOMETRY))
            {
                return new FormatParameter(nodeName, WellGeometry.loadFrom(directory));
            } else if (nodeName.equals(ChannelList.NUMBER_OF_CHANNELS))
            {
                return new FormatParameter(nodeName, ChannelList.loadFrom(directory));
            } else
            {
                // Probably 'channelN'. As already loaded, returns null here.
                return null;
            }
        }
        return IFormatParameterFactory.DEFAULT_FORMAT_PARAMETER_FACTORY.createFormatParameter(node);
    }
}
