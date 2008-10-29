/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * The <i>GWT</i> equivalent to {@link ExternalDataPE}.
 * 
 * @author Christian Ribeaud
 */
public class ExternalData extends CodeWithRegistration
{
    private String location;

    private FileFormatType fileFormatType;

    private LocatorType locatorType;

    public final String getLocation()
    {
        return location;
    }

    public final void setLocation(final String location)
    {
        this.location = location;
    }

    public final FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    public final void setFileFormatType(final FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    public final LocatorType getLocatorType()
    {
        return locatorType;
    }

    public final void setLocatorType(final LocatorType locatorType)
    {
        this.locatorType = locatorType;
    }

}
