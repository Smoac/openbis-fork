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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the images in the dataset: tiles geometry, channels, dataset code and plate geometry if
 * it is HCS image.
 * 
 * @author Tomasz Pylak
 */
// TODO 2010-12-09, Tomasz Pylak: rename: PlateImageParameters -> ImageDatasetParameters
public class PlateImageParameters implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // dataset code for which plate parameters are valid
    private String datasetCode;

    private Integer rowsNumOrNull;

    private Integer colsNumOrNull;

    private int tileRowsNum;

    private int tileColsNum;

    private List<String> channelsCodes;

    private List<String> channelsLabels;

    private Map<String/* channel code */, String/* signature */> channelsTransformerFactorySignatures =
            new HashMap<String, String>();

    // true if any well in the dataset has a time series (or depth stack) of images
    private boolean isMultidimensional;

    public Integer tryGetRowsNum()
    {
        return rowsNumOrNull;
    }

    public void setRowsNum(int rowsNum)
    {
        this.rowsNumOrNull = rowsNum;
    }

    public Integer tryGetColsNum()
    {
        return colsNumOrNull;
    }

    public void setColsNum(int colsNum)
    {
        this.colsNumOrNull = colsNum;
    }

    public int getTileRowsNum()
    {
        return tileRowsNum;
    }

    public void setTileRowsNum(int tileRowsNum)
    {
        this.tileRowsNum = tileRowsNum;
    }

    public int getTileColsNum()
    {
        return tileColsNum;
    }

    public void setTileColsNum(int tileColsNum)
    {
        this.tileColsNum = tileColsNum;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    public void setChannelsCodes(List<String> channelsCodes)
    {
        this.channelsCodes = channelsCodes;
    }

    public List<String> getChannelsCodes()
    {
        return channelsCodes;
    }

    public int getChannelsNumber()
    {
        return channelsCodes.size();
    }

    public boolean isMultidimensional()
    {
        return isMultidimensional;
    }

    public void setIsMultidimensional(boolean isMultidimensional)
    {
        this.isMultidimensional = isMultidimensional;
    }

    public void setChannelsLabels(List<String> channelsLabels)
    {
        this.channelsLabels = channelsLabels;
    }

    public List<String> getChannelsLabels()
    {
        return channelsLabels;
    }

    public void addTransformerFactorySignatureFor(String channelCode, String signatureOrNull)
    {
        channelsTransformerFactorySignatures.put(channelCode, signatureOrNull);
    }

    public String getTransformerFactorySignatureOrNull(String channelCode)
    {
        return channelsTransformerFactorySignatures.get(channelCode);
    }

}
