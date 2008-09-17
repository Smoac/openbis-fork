/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.io.Serializable;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Identifier for searching material through <i>Web Service</i> lookup methods in the database.
 * 
 * @author Christian Ribeaud
 */
public final class MaterialIdentifier extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    /**
     * The material code of this material: together with typeCode uniquely identifies the material.
     * <p>
     * Could not be <code>null</code>.
     * </p>
     */
    private String code;

    /**
     * The code of material type of this material: together with code uniquely identifies the
     * material.
     * <p>
     * Could not be <code>null</code>.
     * </p>
     */
    private String typeCode;

    public final String getCode()
    {
        return code;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(final String typeCode)
    {
        this.typeCode = typeCode;
    }
}