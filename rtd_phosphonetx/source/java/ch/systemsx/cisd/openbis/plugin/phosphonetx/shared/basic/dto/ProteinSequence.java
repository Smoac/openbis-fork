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
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSequence implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String shortName;

    private TechId id;

    private TechId databaseID;

    private String sequence;

    private String databaseNameAndVersion;

    public final String getShortName()
    {
        return shortName;
    }

    public final void setShortName(String version)
    {
        this.shortName = version;
    }

    public final TechId getId()
    {
        return id;
    }

    public final void setId(TechId id)
    {
        this.id = id;
    }

    public final TechId getDatabaseID()
    {
        return databaseID;
    }

    public final void setDatabaseID(TechId databaseID)
    {
        this.databaseID = databaseID;
    }

    public final String getSequence()
    {
        return sequence;
    }

    public final void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public final String getDatabaseNameAndVersion()
    {
        return databaseNameAndVersion;
    }

    public final void setDatabaseNameAndVersion(String databaseNameAndVersion)
    {
        this.databaseNameAndVersion = databaseNameAndVersion;
    }

}
