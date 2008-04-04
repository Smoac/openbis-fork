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

package ch.systemsx.cisd.bds;

/**
 * The current <code>SampleType</code> codes.
 * 
 * @author Christian Ribeaud
 */
public enum SampleType
{
    CELL_PLATE("CELL_PLATE"), REINFECTION_PLATE("REINFECT_PLATE");

    private final String code;

    private SampleType(final String code)
    {
        this.code = code;
    }

    public final String getCode()
    {
        return code;
    }

    /** For given <var>typeCode</var> returns the corresponding <code>SampleTypeCode</code>. */
    public final static SampleType getSampleTypeCode(final String typeCode)
    {
        assert typeCode != null;
        for (final SampleType sampleTypeCode : values())
        {
            if (sampleTypeCode.code.equals(typeCode))
            {
                return sampleTypeCode;
            }
        }
        throw new IllegalArgumentException(String.format(
                "No sample type for given code '%s' found.", typeCode));
    }
}