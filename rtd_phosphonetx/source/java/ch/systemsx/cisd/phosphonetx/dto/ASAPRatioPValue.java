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

package ch.systemsx.cisd.phosphonetx.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ch.systemsx.cisd.phosphonetx.Constants;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlRootElement(name = "ASAPRatio_pvalue", namespace = Constants.NAMESPACE)
@XmlType
public class ASAPRatioPValue
{
    private double adjustedRatioMean;
    private double adjustedRatioStandardDeviation;
    private double adjustedHeavyToLisghtRatioMean;
    private double adjustedHeavyToLisghtRatioStandardDeviation;
    private double pValue;
    private double decimalPValue;
    
    public ASAPRatioPValue()
    {
        System.out.println("ASAPRatioPValue.ASAPRatioPValue()");
    }
    
    @XmlAttribute(name = "adj_ratio_mean", required = true)
    public final double getAdjustedRatioMean()
    {
        return adjustedRatioMean;
    }
    public final void setAdjustedRatioMean(double adjustedRatioMean)
    {
        System.out.println("ASAPRatioPValue.setAdjustedRatioMean(" + adjustedRatioMean + ")");
        this.adjustedRatioMean = adjustedRatioMean;
    }
    
    @XmlAttribute(name = "adj_ratio_standard_dev", required = true)
    public final double getAdjustedRatioStandardDeviation()
    {
        return adjustedRatioStandardDeviation;
    }
    public final void setAdjustedRatioStandardDeviation(double adjustedRatioStandardDeviation)
    {
        this.adjustedRatioStandardDeviation = adjustedRatioStandardDeviation;
    }
    
    @XmlAttribute(name = "heavy2light_adj_ratio_mean")
    public final double getAdjustedHeavyToLisghtRatioMean()
    {
        return adjustedHeavyToLisghtRatioMean;
    }
    public final void setAdjustedHeavyToLisghtRatioMean(double adjustedHeavyToLisghtRatioMean)
    {
        this.adjustedHeavyToLisghtRatioMean = adjustedHeavyToLisghtRatioMean;
    }
    
    @XmlAttribute(name = "heavy2light_adj_ratio_standard_dev")
    public final double getAdjustedHeavyToLisghtRatioStandardDeviation()
    {
        return adjustedHeavyToLisghtRatioStandardDeviation;
    }
    public final void setAdjustedHeavyToLisghtRatioStandardDeviation(
            double adjustedHeavyToLisghtRatioStandardDeviation)
    {
        this.adjustedHeavyToLisghtRatioStandardDeviation = adjustedHeavyToLisghtRatioStandardDeviation;
    }
    
    @XmlAttribute(name = "pvalue")
    public final double getPValue()
    {
        return pValue;
    }
    public final void setPValue(double value)
    {
        pValue = value;
    }
    
    @XmlAttribute(name = "decimal_pvalue")
    public final double getDecimalPValue()
    {
        return decimalPValue;
    }
    public final void setDecimalPValue(double decimalPValue)
    {
        this.decimalPValue = decimalPValue;
    }
    
    
}
