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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public final class ExperimentIdentifier implements IsSerializable
{
    private String identifier;
    private TechId techID;

    public static ExperimentIdentifier createIdentifier(Experiment entity)
    {
        ExperimentIdentifier identifier = new ExperimentIdentifier();
        identifier.setIdentifier(entity.getIdentifier());
        TechId tid = new TechId(entity.getId());
        identifier.setTechID(tid);
        return identifier;
    }

    public ExperimentIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    private ExperimentIdentifier()
    {
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public final TechId getTechID()
    {
        return techID;
    }

    public final void setTechID(TechId techID)
    {
        this.techID = techID;
    }

    @Override
    public String toString()
    {
        return "Experiment[" + identifier + ", " + techID + "]";
    }

    
}