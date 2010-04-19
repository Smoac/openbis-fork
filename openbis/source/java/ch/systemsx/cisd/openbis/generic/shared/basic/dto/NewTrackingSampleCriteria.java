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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Collection;

/**
 * Criteria for tracking <i>samples</i> with a particular property having certain property value.
 * <p>
 * Parent and Container samples should be loaded according to {@link SampleType} hierarchy depths.
 * All referenced samples should have all properties loaded.
 * 
 * @author Piotr Buczek
 */
// NOTE: It doesn't implement IsSerializable as it is not supposed to be used on the GWT client side
public class NewTrackingSampleCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // TODO
    // private final String sampleTypeCode;

    private final String propertyTypeCode;

    private final String propertyValue;

    private final Collection<Long> alreadyTrackedSampleIds;

    public NewTrackingSampleCriteria(String propertyTypeCode, String propertyValue,
            Collection<Long> alreadyTrackedSampleIds)
    {
        assert propertyTypeCode != null;
        assert propertyValue != null;
        this.propertyTypeCode = propertyTypeCode;
        this.propertyValue = propertyValue;
        this.alreadyTrackedSampleIds = alreadyTrackedSampleIds;
    }

    public String getPropertyTypeCode()
    {
        return propertyTypeCode;
    }

    public String getPropertyValue()
    {
        return propertyValue;
    }

    public Collection<Long> getAlreadyTrackedSampleIds()
    {
        return alreadyTrackedSampleIds;
    }

}
