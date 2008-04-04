/*
 * Copyright 2008 ETH Zuerich, CISD
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
 * The current <code>ObservableType</code> codes.
 * 
 * @author Christian Ribeaud
 */
public enum ObservableType
{
    HCS_IMAGE("HCS_IMAGE"), HCS_IMAGE_ANALYSIS_DATA("HCS_IMAGE_ANALYSIS_DATA"), UNKNOWN("UNKNOWN");

    private final String code;

    private ObservableType(final String code)
    {
        this.code = code;
    }

    /**
     * Returns the code of this observable type.
     */
    public final String getCode()
    {
        return code;
    }

    /** For given <var>typeCode</var> returns the corresponding {@link ObservableType}. */
    public final static ObservableType getObservableTypeCode(final String typeCode)
    {
        assert typeCode != null : "Unspecified observable type code.";
        for (final ObservableType observableTypeCode : values())
        {
            if (observableTypeCode.code.equals(typeCode))
            {
                return observableTypeCode;
            }
        }
        throw new IllegalArgumentException(String.format(
                "No observable type for given code '%s' found.", typeCode));
    }

}
