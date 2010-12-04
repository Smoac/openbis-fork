/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes one channel stack which has images for many channels and a specific tile (and
 * optionally timepoint and/or depth) of one well.
 * 
 * @author Tomasz Pylak
 */
public class WellImageChannelStack implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // technical id in the imaging db
    private long channelStackTechId;

    private int tileRow, tileCol;

    private Float tOrNull, zOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private WellImageChannelStack()
    {
    }

    public WellImageChannelStack(long channelStackTechId, int tileRow, int tileCol, Float tOrNull,
            Float zOrNull)
    {
        this.channelStackTechId = channelStackTechId;
        this.tileRow = tileRow;
        this.tileCol = tileCol;
        this.tOrNull = tOrNull;
        this.zOrNull = zOrNull;
    }

    public long getChannelStackTechId()
    {
        return channelStackTechId;
    }

    public int getTileRow()
    {
        return tileRow;
    }

    public int getTileCol()
    {
        return tileCol;
    }

    public Float tryGetTimepoint()
    {
        return tOrNull;
    }

    public Float tryGetDepth()
    {
        return zOrNull;
    }

    @Override
    public String toString()
    {
        String desc = "";
        if (tOrNull != null)
        {
            desc += ", t=" + tOrNull;
        }
        if (zOrNull != null)
        {
            desc += ", z=" + zOrNull;
        }

        return "channelStack=" + channelStackTechId + ", tile[" + tileRow + "," + tileCol + "]"
                + desc;
    }
}
