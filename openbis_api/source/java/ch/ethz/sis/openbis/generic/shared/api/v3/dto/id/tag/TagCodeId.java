/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Tag code.
 * 
 * @author Franz-Josef Elmer
 * @author Jakub Straszewski
 */
@JsonObject("TagCodeId")
public class TagCodeId implements ITagId, Serializable
{

    private static final long serialVersionUID = 1L;

    private String code;

    /**
     * @param code Tag code, e.g. "MY_TAG".
     */
    public TagCodeId(String code)
    {
        setCode(code);
    }

    //
    // JSON-RPC
    //

    public String getCode()
    {
        return code;
    }

    @SuppressWarnings("unused")
    private TagCodeId()
    {
        super();
    }

    private void setCode(String code)
    {
        if (code == null)
        {
            throw new IllegalArgumentException("Code cannot be null");
        }
        this.code = code;
    }

    @Override
    public String toString()
    {
        return getCode();
    }

    @Override
    public int hashCode()
    {
        return ((code == null) ? 0 : code.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        TagCodeId other = (TagCodeId) obj;
        return code == null ? code == other.code : code.equals(other.code);
    }

}
