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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Criteria for tracking <i>data sets</i> connected to samples of a particular type with technical
 * id bigger than the specified one.
 * <p>
 * Connected samples should be loaded as well as their parent and container samples according to
 * {@link SampleType} hierarchy depths. All referenced samples should have all properties loaded.
 * 
 * @author Piotr Buczek
 */
// NOTE: It doesn't implement IsSerializable as it is not supposed to be used on the GWT client side
public class TrackingDataSetCriteria implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final String connectedSampleTypeCode;

    private int lastSeenDataSetId;

    public TrackingDataSetCriteria(String connectedSampleTypeCode, int lastSeenDataSetId)
    {
        this.lastSeenDataSetId = lastSeenDataSetId;
        this.connectedSampleTypeCode = connectedSampleTypeCode;
    }

    public String getConnectedSampleTypeCode()
    {
        return connectedSampleTypeCode;
    }

    public int getLastSeenDataSetId()
    {
        return lastSeenDataSetId;
    }

    public void setLastSeenDataSetId(int lastSeenDataSetId)
    {
        this.lastSeenDataSetId = lastSeenDataSetId;
    }

}
