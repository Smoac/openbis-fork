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

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSummary implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private double fdr;

    private int proteinCount;

    private int peptideCount;

    public double getFDR()
    {
        return fdr;
    }

    public void setFDR(double fdr)
    {
        this.fdr = fdr;
    }

    public int getProteinCount()
    {
        return proteinCount;
    }

    public void setProteinCount(int proteinCount)
    {
        this.proteinCount = proteinCount;
    }

    public int getPeptideCount()
    {
        return peptideCount;
    }

    public void setPeptideCount(int peptideCount)
    {
        this.peptideCount = peptideCount;
    }

}
