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

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Enity of measurement or calculation covered by the data. This is an immutable value object class.
 * 
 * @author Franz-Josef Elmer
 */
public final class Sample implements IStorable
{
    static final String FOLDER = "sample";

    static final String SAMPLE_TYPE_DESCRIPTION = "sample_type_description";

    static final String SAMPLE_TYPE_CODE = "sample_type_code";

    static final String SAMPLE_CODE = "sample_code";

    /**
     * Loads the enity from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    final static Sample loadFrom(final IDirectory directory)
    {
        final IDirectory folder = Utilities.getSubDirectory(directory, FOLDER);
        final String sampleTypeDescription =
                Utilities.getTrimmedString(folder, SAMPLE_TYPE_DESCRIPTION);
        final String sampleCode = Utilities.getTrimmedString(folder, SAMPLE_CODE);
        SampleType sampleType = null;
        try
        {
            sampleType =
                    SampleType.getSampleTypeCode(Utilities.getTrimmedString(folder,
                            SAMPLE_TYPE_CODE));
        } catch (final IllegalArgumentException ex)
        {
            throw new DataStructureException(ex.getMessage());
        }
        return new Sample(sampleCode, sampleType, sampleTypeDescription);
    }

    private final String typeDescription;

    private final SampleType type;

    private final String sampleCode;

    /**
     * Creates an instance for the specified code and type description of the sample.
     * 
     * @param sampleCode A non-empty string of the sample code.
     * @param sampleType the sample type code.
     * @param sampleTypeDescription A non-empty description of the sample type.
     */
    public Sample(final String sampleCode, final SampleType sampleType,
            final String sampleTypeDescription)
    {
        assert StringUtils.isEmpty(sampleTypeDescription) == false : "Undefined sample type description.";
        this.typeDescription = sampleTypeDescription;
        assert StringUtils.isEmpty(sampleCode) == false : "Undefined sample code.";
        this.sampleCode = sampleCode;
        assert sampleType != null : "Undefined sample type code.";
        this.type = sampleType;
    }

    /**
     * Returns the description of the sample type.
     */
    public final String getTypeDescription()
    {
        return typeDescription;
    }

    /**
     * Returns the sample type.
     */
    public final SampleType getType()
    {
        return type;
    }

    /**
     * Returns the sample code.
     */
    public final String getSampleCode()
    {
        return sampleCode;
    }

    //
    // IStorable
    //

    /**
     * Saves this instance to the specified directory.
     */
    public final void saveTo(final IDirectory directory)
    {
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(SAMPLE_TYPE_DESCRIPTION, typeDescription);
        folder.addKeyValuePair(SAMPLE_CODE, sampleCode);
        folder.addKeyValuePair(SAMPLE_TYPE_CODE, type.getCode());
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
        return that.sampleCode.equals(sampleCode) && type == that.type;
    }

    @Override
    public final int hashCode()
    {
        int result = 17;
        result = 37 * result + sampleCode.hashCode();
        result = 37 * result + type.hashCode();
        return result;
    }

    @Override
    public final String toString()
    {
        return "[code:" + sampleCode + ",type:" + type + ",typeDescription:"
                + typeDescription + "]";
    }

}
