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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes geometry of the plate and well.
 * 
 * @author Tomasz Pylak
 */
public class PlateImageParameters implements IsSerializable
{
    // dataset code for which plate parameters are valid
    private String datasetCode;

    private int rowsNum;

    private int colsNum;

    private int tileRowsNum;

    private int tileColsNum;

    private List<String> channelsNames;

    public int getRowsNum()
    {
        return rowsNum;
    }

    public void setRowsNum(int rowsNum)
    {
        this.rowsNum = rowsNum;
    }

    public int getColsNum()
    {
        return colsNum;
    }

    public void setColsNum(int colsNum)
    {
        this.colsNum = colsNum;
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

    public void setChannelsNames(List<String> channelsNames)
    {
        this.channelsNames = channelsNames;
    }

    public List<String> getChannelsNames()
    {
        return channelsNames;
    }

}
