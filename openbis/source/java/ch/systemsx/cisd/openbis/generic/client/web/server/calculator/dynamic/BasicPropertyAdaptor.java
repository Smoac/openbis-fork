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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator.dynamic;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;

/**
 * Simple {@link IEntityPropertyAdaptor} implementation
 * 
 * @author Piotr Buczek
 */
public class BasicPropertyAdaptor implements IEntityPropertyAdaptor
{

    private final String code;

    private final String value;

    private final EntityPropertyPE propertyPE;

    public BasicPropertyAdaptor(String code, String value, EntityPropertyPE propertyPE)
    {
        this.code = code;
        this.value = value;
        this.propertyPE = propertyPE;
    }

    public String getPropertyTypeCode()
    {
        return code;
    }

    public String getValueAsString()
    {
        return value;
    }

    public EntityPropertyPE getPropertyPE()
    {
        return propertyPE;
    }

}
