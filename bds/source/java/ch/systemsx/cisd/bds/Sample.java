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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Entity of measurement or calculation covered by the data. This is an immutable value object
 * class.
 * 
 * @author Franz-Josef Elmer
 */
public class Sample implements IStorable
{
    static final String FOLDER = "sample";

    static final String TYPE_DESCRIPTION = "type_description";

    static final String TYPE_CODE = "type_code";

    static final String CODE = "code";

    /**
     * Loads the entity from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    static Sample loadFrom(final IDirectory directory)
    {
        assert directory != null : "Unspecified directory";
        final IDirectory folder = Utilities.getSubDirectory(directory, FOLDER);
        final String typeDescription = Utilities.getTrimmedString(folder, TYPE_DESCRIPTION);
        final String code = Utilities.getTrimmedString(folder, CODE);
        final String typeCode = Utilities.getTrimmedString(folder, TYPE_CODE);
        return new Sample(code, typeCode, typeDescription);
    }

    private final String typeDescription;

    private final String typeCode;

    private final String code;

    /**
     * Creates an instance for the specified code and type description of the sample.
     * 
     * @param code A non-empty string of the sample code.
     * @param typeCode the sample type code.
     * @param typeDescription A non-empty description of the sample type.
     */
    public Sample(final String code, final String typeCode, final String typeDescription)
    {
        assert StringUtils.isEmpty(typeDescription) == false : "Undefined sample type description.";
        this.typeDescription = typeDescription;
        assert StringUtils.isEmpty(code) == false : "Undefined sample code.";
        this.code = code;
        assert StringUtils.isEmpty(typeCode) == false : "Undefined sample type code.";
        this.typeCode = typeCode;
    }

    ToStringBuilder createToStringBuilder()
    {
        final ToStringBuilder builder = new ToStringBuilder();
        builder.append(CODE, code);
        builder.append(TYPE_CODE, typeCode);
        builder.append(TYPE_DESCRIPTION, typeDescription);
        return builder;
    }

    /**
     * Returns the description of the sample type.
     */
    public final String getTypeDescription()
    {
        return typeDescription;
    }

    /**
     * Returns the sample type code.
     */
    public final String getTypeCode()
    {
        return typeCode;
    }

    /**
     * Returns the sample code.
     */
    public final String getCode()
    {
        return code;
    }

    //
    // IStorable
    //

    /**
     * Saves this instance to the specified directory.
     */
    public void saveTo(final IDirectory directory)
    {
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(TYPE_DESCRIPTION, typeDescription);
        folder.addKeyValuePair(CODE, code);
        folder.addKeyValuePair(TYPE_CODE, typeCode);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Sample == false)
        {
            return false;
        }
        final Sample that = (Sample) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.code, code);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(code);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        final ToStringBuilder builder = createToStringBuilder();
        return builder.toString();
    }
}
