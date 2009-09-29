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

package ch.systemsx.cisd.yeastx.quant.dto;

import javax.xml.bind.annotation.XmlElement;

/**
 * Stores the content of one concentration.
 * 
 * @author Tomasz Pylak
 */
public class MSConcentrationDTO
{
    private String parentDatasetCode; // not null

    private double amount; // not null

    private String unit;// not null, can be empty

    private boolean valid;// not null

    private String comment; // can be null

    private double retentionTime;// not null

    private double q1; // not null

    private double q3;// not null

    private String internalStandard;// not null, can be empty

    private ConcentrationCompounds compounds;

    @XmlElement(required = true, name = "datasetParent")
    public String getParentDatasetCode()
    {
        return parentDatasetCode;
    }

    public void setParentDatasetCode(String parentDatasetCode)
    {
        this.parentDatasetCode = parentDatasetCode;
    }

    @XmlElement(required = true, name = "amount")
    public double getAmount()
    {
        return amount;
    }

    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    @XmlElement(required = true, name = "unit")
    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    @XmlElement(required = true, name = "valid")
    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    @XmlElement(name = "comment")
    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @XmlElement(required = true, name = "retentionTime")
    public double getRetentionTime()
    {
        return retentionTime;
    }

    public void setRetentionTime(double retentionTime)
    {
        this.retentionTime = retentionTime;
    }

    @XmlElement(required = true, name = "Q1")
    public double getQ1()
    {
        return q1;
    }

    public void setQ1(double q1)
    {
        this.q1 = q1;
    }

    @XmlElement(required = true, name = "Q3")
    public double getQ3()
    {
        return q3;
    }

    public void setQ3(double q3)
    {
        this.q3 = q3;
    }

    @XmlElement(required = true, name = "internalStandard")
    public String getInternalStandard()
    {
        return internalStandard;
    }

    public void setInternalStandard(String internalStandard)
    {
        this.internalStandard = internalStandard;
    }

    @XmlElement(required = true, name = "identity")
    public ConcentrationCompounds getCompounds()
    {
        return compounds;
    }

    public void setCompounds(ConcentrationCompounds compounds)
    {
        this.compounds = compounds;
    }
}
