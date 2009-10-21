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

package ch.systemsx.cisd.yeastx.mzxml.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import ch.systemsx.cisd.yeastx.utils.XmlUtils;

/**
 * @author Tomasz Pylak
 */
public class MzScanDTO
{
    private int number;

    private int level;

    private int peaksCount;

    private String polarity;

    private String scanType;

    private String retentionTime;

    private Double collisionEnergy;

    private Double lowMz;

    private Double highMz;

    private List<MzPrecursorDTO> precursors;

    private MzPeaksDTO peaks;

    @XmlAttribute(name = "num", required = true)
    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    @XmlAttribute(name = "msLevel", required = true)
    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    @XmlAttribute(name = "peaksCount", required = true)
    public int getPeaksCount()
    {
        return peaksCount;
    }

    public void setPeaksCount(int peaksCount)
    {
        this.peaksCount = peaksCount;
    }

    @XmlAttribute(name = "polarity")
    public String getPolarity()
    {
        return polarity;
    }

    public void setPolarity(String polarity)
    {
        this.polarity = polarity;
    }

    @XmlAttribute(name = "scanType")
    public String getScanType()
    {
        return scanType;
    }

    public void setScanType(String scanType)
    {
        this.scanType = scanType;
    }

    @XmlAttribute(name = "retentionTime")
    public String getRetentionTime()
    {
        return retentionTime;
    }

    public void setRetentionTime(String retentionTime)
    {
        this.retentionTime = retentionTime;
    }

    @XmlAttribute(name = "collisionEnergy")
    public Double getCollisionEnergy()
    {
        return collisionEnergy;
    }

    public void setCollisionEnergy(Double collisionEnergy)
    {
        this.collisionEnergy = collisionEnergy;
    }

    @XmlAttribute(name = "lowMz")
    public Double getLowMz()
    {
        return lowMz;
    }

    public void setLowMz(Double lowMz)
    {
        this.lowMz = lowMz;
    }

    @XmlAttribute(name = "highMz")
    public Double getHighMz()
    {
        return highMz;
    }

    public void setHighMz(Double highMz)
    {
        this.highMz = highMz;
    }

    @XmlElement(name = "precursorMz", namespace = MzXmlDTO.NAMESPACE)
    public List<MzPrecursorDTO> getPrecursors()
    {
        return precursors;
    }

    public void setPrecursors(List<MzPrecursorDTO> precursors)
    {
        this.precursors = precursors;
    }

    @XmlElement(name = "peaks", namespace = MzXmlDTO.NAMESPACE)
    public MzPeaksDTO getPeaksBytes()
    {
        return peaks;
    }

    public void setPeaksBytes(MzPeaksDTO peaks)
    {
        this.peaks = peaks;
    }

    // --- getters which do not map to xml directly ---------------

    public Double getRetentionTimeInSeconds()
    {
        return XmlUtils.tryAsSeconds(retentionTime);
    }

    /** the array has mz on even positions and intensities on odd positions */
    public float[] getPeaks()
    {
        return XmlUtils.asFloats(peaks.getPeaks());
    }
}
