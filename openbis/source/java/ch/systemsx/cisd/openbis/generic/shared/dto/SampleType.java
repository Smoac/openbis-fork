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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which transports through Web Service any
 * information we would like to know about a sample type.
 * 
 * @author Christian Ribeaud
 */
public final class SampleType extends EntityType
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final SampleType[] EMPTY_ARRAY = new SampleType[0];
    
    private boolean plate;

    private boolean controlLayout;

    public final boolean isPlate()
    {
        return plate;
    }

    public final void setPlate(boolean plate)
    {
        this.plate = plate;
    }

    public final boolean isControlLayout()
    {
        return controlLayout;
    }

    public final void setControlLayout(final boolean controlLayout)
    {
        this.controlLayout = controlLayout;
    }
}
