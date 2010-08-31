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

package eu.basysbio.cisd.dss;

import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public class ChipChipData extends AbstractDataValue
{
    private String bsuIdentifier;

    private String geneName;

    private String geneFunction;

    private String arrayDesign;

    private Integer microArrayID;

    private Integer chipPeakPosition;

    private String chipPeakPositionScale;

    private Double chipLocalHeight;

    private String chipLocalHeightScale;

    private Double chipScore;

    private String chipScoreScale;

    private boolean intergenic;

    private String nearbyGeneNames;

    private String nearbyGeneIDs;

    private String distancesFromStart;

    public final String getBsuIdentifier()
    {
        return bsuIdentifier;
    }

    public final void setBsuIdentifier(String bsuIdentifier)
    {
        this.bsuIdentifier = bsuIdentifier;
    }

    public final String getGeneName()
    {
        return geneName;
    }

    public final void setGeneName(String geneName)
    {
        this.geneName = geneName;
    }

    public final String getGeneFunction()
    {
        return geneFunction;
    }

    public final void setGeneFunction(String geneFunction)
    {
        this.geneFunction = geneFunction;
    }

    public final String getArrayDesign()
    {
        return arrayDesign;
    }

    public final void setArrayDesign(String arrayDesign)
    {
        this.arrayDesign = arrayDesign;
    }

    public final Integer getMicroArrayID()
    {
        return microArrayID;
    }

    public final void setMicroArrayID(Integer microArrayID)
    {
        this.microArrayID = microArrayID;
    }

    public final Integer getChipPeakPosition()
    {
        return chipPeakPosition;
    }

    public final void setChipPeakPosition(Integer chipPeakPosition)
    {
        this.chipPeakPosition = chipPeakPosition;
    }

    public final String getChipPeakPositionScale()
    {
        return chipPeakPositionScale;
    }

    public final void setChipPeakPositionScale(String chipPeakPositionScale)
    {
        this.chipPeakPositionScale = chipPeakPositionScale;
    }

    public final Double getChipLocalHeight()
    {
        return chipLocalHeight;
    }

    public final void setChipLocalHeight(Double chipLocalHeight)
    {
        this.chipLocalHeight = chipLocalHeight;
    }

    public final String getChipLocalHeightScale()
    {
        return chipLocalHeightScale;
    }

    public final void setChipLocalHeightScale(String chipLocalHeightScale)
    {
        this.chipLocalHeightScale = chipLocalHeightScale;
    }

    public final Double getChipScore()
    {
        return chipScore;
    }

    public final void setChipScore(Double chipScore)
    {
        this.chipScore = chipScore;
    }

    public final String getChipScoreScale()
    {
        return chipScoreScale;
    }

    public final void setChipScoreScale(String chipScoreScale)
    {
        this.chipScoreScale = chipScoreScale;
    }

    public final boolean isIntergenic()
    {
        return intergenic;
    }

    public final void setIntergenic(boolean intergenic)
    {
        this.intergenic = intergenic;
    }

    public final String getNearbyGeneNames()
    {
        return nearbyGeneNames;
    }

    public final void setNearbyGeneNames(String nearbyGeneNames)
    {
        this.nearbyGeneNames = nearbyGeneNames;
    }

    public final String getNearbyGeneIDs()
    {
        return nearbyGeneIDs;
    }

    public final void setNearbyGeneIDs(String nearbyGeneIDs)
    {
        this.nearbyGeneIDs = nearbyGeneIDs;
    }

    public final String getDistancesFromStart()
    {
        return distancesFromStart;
    }

    public final void setDistancesFromStart(String distancesFromStart)
    {
        this.distancesFromStart = distancesFromStart;
    }
    
    ChipChipData createFor(int rowIndex, Integer position, Double height, Double score, List<IColumnInjection<ChipChipData>> injections)
    {
        ChipChipData data = new ChipChipData();
        for (IColumnInjection<ChipChipData> injection : injections)
        {
            injection.inject(data, rowIndex);
        }
        data.setDescriptor(getDescriptor());
        data.setRowIndex(rowIndex);
        data.setChipPeakPosition(position);
        data.setChipPeakPositionScale(getChipPeakPositionScale());
        data.setChipLocalHeight(height);
        data.setChipLocalHeightScale(getChipLocalHeightScale());
        data.setChipScore(score);
        data.setChipScoreScale(getChipScoreScale());
        return data;
    }
}
