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
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * A customized <code>IFormatParameterFactory</code> implementation suitable for <i>HCS
 * (High-Content Screening) with Images</i>.
 * 
 * @author Christian Ribeaud
 */
public final class FormatParameterFactory implements IFormatParameterFactory
{

    private static IFormatParameterFactory instance;

    private FormatParameterFactory()
    {
        // Can not be instantiated.
    }

    /** Returns the unique instance of this class. */
    public final synchronized static IFormatParameterFactory getInstance()
    {
        if (instance == null)
        {
            instance = new FormatParameterFactory();
        }
        return instance;
    }

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
            }
        }
        final FormatParameter formatParameter =
                IFormatParameterFactory.DEFAULT_FORMAT_PARAMETER_FACTORY
                        .createFormatParameter(node);
        final String value = (String) formatParameter.getValue();
        if (nodeName.equals(HCSImageFormatV1_0.NUMBER_OF_CHANNELS))
        {
            return new FormatParameter(HCSImageFormatV1_0.NUMBER_OF_CHANNELS, Integer
                    .parseInt(value));
        } else if (nodeName.equals(HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA))
        {
            return createBooleanParameter(HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA, value);
        } else if (nodeName.equals(HCSImageFormatV1_0.IS_INCOMING_SYMBOLIC_LINK))
        {
            return createBooleanParameter(HCSImageFormatV1_0.IS_INCOMING_SYMBOLIC_LINK, value);
        }
        return formatParameter;
    }

    private static FormatParameter createBooleanParameter(String name, String value)
    {
        return new FormatParameter(name, Utilities.Boolean.fromString(value));
    }

    public final FormatParameter createFormatParameter(final String name, final String value)
    {
        if (name.equals(PlateGeometry.PLATE_GEOMETRY))
        {
            final Geometry geometry = Geometry.createFromString(value);
            if (geometry != null)
            {
                return new FormatParameter(name, new PlateGeometry(geometry));
            }
        } else if (name.equals(WellGeometry.WELL_GEOMETRY))
        {
            final Geometry geometry = Geometry.createFromString(value);
            if (geometry != null)
            {
                return new FormatParameter(name, new WellGeometry(geometry));
            }
        } else if (name.equals(HCSImageFormatV1_0.NUMBER_OF_CHANNELS))
        {
            return new FormatParameter(name, Integer.parseInt(value));
        } else if (name.equals(HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA))
        {
            return new FormatParameter(name, Utilities.Boolean.fromString(value));
        } else if (name.equals(HCSImageFormatV1_0.IS_INCOMING_SYMBOLIC_LINK))
        {
            return new FormatParameter(name, Utilities.Boolean.fromString(value));
        }
        return IFormatParameterFactory.DEFAULT_FORMAT_PARAMETER_FACTORY.createFormatParameter(name,
                value);
    }
}
