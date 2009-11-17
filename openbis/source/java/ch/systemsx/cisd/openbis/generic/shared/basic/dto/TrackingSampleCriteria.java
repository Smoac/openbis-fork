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

/**
 * Criteria for tracking <i>samples</i> of a particular type with technical id bigger than the
 * specified one.
 * <p>
 * Parent and Container samples should be loaded according to {@link SampleType} hierarchy depths.
 * All referenced samples should have all properties loaded.
 * 
 * @author Piotr Buczek
 */
// NOTE: It doesn't implement IsSerializable as it is not supposed to be used on the GWT client side
public class TrackingSampleCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final String sampleTypeCode;

    private int lastSeenSampleId;

    public TrackingSampleCriteria(String sampleTypeCode, int lastSeenSampleId)
    {
        assert sampleTypeCode != null;
        this.sampleTypeCode = sampleTypeCode;
        this.lastSeenSampleId = lastSeenSampleId;
    }

    public String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    public int getLastSeenSampleId()
    {
        return lastSeenSampleId;
    }

    public void setLastSeenSampleId(int lastSeenSampleId)
    {
        this.lastSeenSampleId = lastSeenSampleId;
    }

}
