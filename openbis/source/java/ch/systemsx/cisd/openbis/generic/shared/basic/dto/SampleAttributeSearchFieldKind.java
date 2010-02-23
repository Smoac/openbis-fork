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
 * Kinds of fields connected with Sample attributes that can be used in detailed text queries.
 * 
 * @author Piotr Buczek
 */
public enum SampleAttributeSearchFieldKind implements IsSerializable, IAttributeSearchFieldKind
{
    CODE("Code"),

    SAMPLE_TYPE("Sample Type"),

    GROUP("Space");// FIXME:

    private final String description;

    private SampleAttributeSearchFieldKind(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public String getCode()
    {
        return name();
    }

}
