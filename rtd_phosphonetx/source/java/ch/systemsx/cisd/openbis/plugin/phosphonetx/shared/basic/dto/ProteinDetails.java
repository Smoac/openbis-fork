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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Tomasz Pylak
 */
public class ProteinDetails implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String sequence;

    private String databaseNameAndVersion;

    private double falseDiscoveryRate;

    private String dataSetPermID;

    private Long dataSetTechID;

    private String dataSetTypeCode;

    private List<Peptide> peptides;

    public String getSequence()
    {
        return sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public String getDatabaseNameAndVersion()
    {
        return databaseNameAndVersion;
    }

    public void setDatabaseNameAndVersion(String databaseNameAndVersion)
    {
        this.databaseNameAndVersion = databaseNameAndVersion;
    }

    public double getFalseDiscoveryRate()
    {
        return falseDiscoveryRate;
    }

    public void setFalseDiscoveryRate(double falseDiscoveryRate)
    {
        this.falseDiscoveryRate = falseDiscoveryRate;
    }

    public String getDataSetPermID()
    {
        return dataSetPermID;
    }

    public void setDataSetPermID(String dataSetPermID)
    {
        this.dataSetPermID = dataSetPermID;
    }

    public List<Peptide> getPeptides()
    {
        return peptides;
    }

    public void setPeptides(List<Peptide> peptides)
    {
        this.peptides = peptides;
    }

    public Long getDataSetTechID()
    {
        return dataSetTechID;
    }

    public void setDataSetTechID(Long dataSetTechID)
    {
        this.dataSetTechID = dataSetTechID;
    }

    public String getDataSetTypeCode()
    {
        return dataSetTypeCode;
    }

    public void setDataSetTypeCode(String dataSetTypeCode)
    {
        this.dataSetTypeCode = dataSetTypeCode;
    }
}
