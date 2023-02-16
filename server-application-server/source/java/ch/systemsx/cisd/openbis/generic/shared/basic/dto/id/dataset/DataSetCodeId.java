/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset;

/**
 * Identifies a data set by code.
 * 
 * @author pkupczyk
 */
public class DataSetCodeId implements IDataSetId
{

    private static final long serialVersionUID = 1L;

    private String code;

    /**
     * @param code Data set code, e.g "201108050937246-1031".
     */
    public DataSetCodeId(String code)
    {
        setCode(code);
    }

    public String getCode()
    {
        return code;
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
        DataSetCodeId other = (DataSetCodeId) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (!code.equals(other.code))
        {
            return false;
        }
        return true;
    }

}
