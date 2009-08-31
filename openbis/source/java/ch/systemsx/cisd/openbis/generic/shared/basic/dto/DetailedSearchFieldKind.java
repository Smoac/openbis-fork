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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * General kinds of fields that can be used in detailed text query for an entity.
 * 
 * @author Piotr Buczek
 */
public enum DetailedSearchFieldKind implements IsSerializable
{
    ANY_FIELD("Any Field"),

    ANY_PROPERTY("Any Property"),

    PROPERTY("Property"),

    ATTRIBUTE("");

    private final String description;

    private DetailedSearchFieldKind(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
