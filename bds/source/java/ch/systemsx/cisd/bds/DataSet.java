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

import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.bds.Utilities.Boolean;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.common.collections.CollectionIO;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;

/**
 * Identifier of the data set. This is an immutable but extendable value object class. An instance
 * of this class allows unique identification in the database.
 * 
 * @author Christian Ribeaud
 */
public final class DataSet implements IStorable
{
    static final String NO_PARENT_FOR_MEASURED_DATA =
            "No parent could be specified for measured data.";

    static final String FOLDER = "data_set";

    static final String CODE = "code";

    static final String PRODUCTION_TIMESTAMP = "production_timestamp";

    static final String PRODUCER_CODE = "producer_code";

    static final String OBSERVABLE_TYPE = "observable_type";

    static final String IS_MEASURED = "is_measured";

    static final String IS_COMPLETE = "is_complete";

    static final String PARENT_CODES = "parent_codes";

    /** Provides the information when the data set has been created. */
    private final Date productionTimestamp;

    /** Identifies the "device" that produced this data set. */
    private final String producerCode;

    /** Is a code that describes the type of data that is stored in this standard. */
    private final String observableTypeCode;

    /**
     * Specifies whether the data set has been measured from a sample or whether it has been derived
     * by means of some calculation from another data set.
     */
    private final Boolean isMeasured;

    /** The list of parent codes. Never <code>null</code> but could be empty. */
    private final List<String> parentCodes;

    /** This data set unique identifier. */
    private String code;

    private BooleanOrUnknown isComplete = BooleanOrUnknown.U;

    /**
     * Creates an instance of data set.
     * 
     * @param code A non-empty string of the data set code. Can not be empty.
     * @param observableType type of this data set. Can not be <code>null</code>.
     */
    public DataSet(final String code, final String observableType)
    {
        this(code, observableType, Boolean.TRUE, null, null, null);
    }

    /**
     * Creates an instance of data set.
     * 
     * @param code A non-empty string of the data set code. Can not be empty.
     * @param observableType type of this data set. Can not be <code>null</code>.
     * @param isMeasured measured or derived.
     * @param productionTimestampOrNull production timestamp or <code>null</code> if unknown.
     * @param producerCodeOrNull producer code (aka "device id") or <code>null</code> if unknown.
     * @param parentCodesOrNull list of parent data sets. Must be <code>null</code> or empty for
     *            measured data (or not empty for derived data).
     */
    public DataSet(final String code, final String observableType, final Boolean isMeasured,
            final Date productionTimestampOrNull, final String producerCodeOrNull,
            final List<String> parentCodesOrNull)
    {
        this.code = code == null ? "" : code;
        this.isMeasured = isMeasured;
        assert StringUtils.isEmpty(observableType) == false : "Unspecified observable type.";
        this.observableTypeCode = observableType;
        if (isMeasured.toBoolean() == true && parentCodesOrNull != null
                && parentCodesOrNull.size() > 0)
        {
            throw new IllegalArgumentException(String.format(NO_PARENT_FOR_MEASURED_DATA));
        }
        this.parentCodes =
                parentCodesOrNull == null ? Collections.<String> emptyList() : parentCodesOrNull;
        this.producerCode = producerCodeOrNull;
        this.productionTimestamp = productionTimestampOrNull;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    public final String getCode()
    {
        return code;
    }

    public final Date getProductionTimestamp()
    {
        return productionTimestamp;
    }

    public final String getProducerCode()
    {
        return producerCode;
    }

    public final String getObservableTypeCode()
    {
        return observableTypeCode;
    }

    public final Boolean isMeasured()
    {
        return isMeasured;
    }

    public final List<String> getParentCodes()
    {
        return parentCodes;
    }

    public final void setComplete(final boolean complete)
    {
        isComplete = BooleanOrUnknown.resolve(complete);
    }

    /**
     * Loads the experiment identifier from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    final static DataSet loadFrom(final IDirectory directory)
    {
        assert directory != null : "Given directory can not be null.";
        final IDirectory idFolder = Utilities.getSubDirectory(directory, FOLDER);
        final String code = Utilities.getTrimmedString(idFolder, CODE);
        final String observableTypeCode = Utilities.getTrimmedString(idFolder, OBSERVABLE_TYPE);
        final Boolean isMeasured = Utilities.getBoolean(idFolder, IS_MEASURED);
        final Date productionTimestampOrNull = Utilities.tryGetDate(idFolder, PRODUCTION_TIMESTAMP);
        final String producerCode = Utilities.getTrimmedString(idFolder, PRODUCER_CODE);
        final List<String> parentCodes = Utilities.getStringList(idFolder, PARENT_CODES);
        final String strIsComplete = Utilities.getTrimmedString(idFolder, IS_COMPLETE);
        BooleanOrUnknown completeFlag;
        try
        {
            completeFlag = BooleanOrUnknown.resolve(strIsComplete);
        } catch (final IllegalArgumentException ex)
        {
            throw new DataStructureException(ex.getMessage());
        }
        assert completeFlag != null : "Complete flag not specified.";
        final DataSet dataSet =
                new DataSet(code, observableTypeCode, isMeasured, productionTimestampOrNull,
                        (producerCode.length() == 0 ? null : producerCode), parentCodes);
        dataSet.isComplete = completeFlag;
        return dataSet;
    }

    //
    // IStorable
    //

    public final void saveTo(final IDirectory directory)
    {
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(CODE, code);
        folder.addKeyValuePair(PRODUCTION_TIMESTAMP,
                productionTimestamp == null ? StringUtils.EMPTY_STRING : Constants.DATE_FORMAT
                        .get().format(productionTimestamp));
        folder.addKeyValuePair(PRODUCER_CODE, StringUtils.emptyIfNull(producerCode));
        folder.addKeyValuePair(IS_MEASURED, isMeasured.toString());
        folder.addKeyValuePair(OBSERVABLE_TYPE, observableTypeCode);
        folder.addKeyValuePair(IS_COMPLETE, isComplete.getNiceRepresentation());
        final String value;
        if (parentCodes.size() > 0)
        {
            final StringWriter stringWriter = new StringWriter();
            CollectionIO.writeIterable(stringWriter, parentCodes, null);
            value = stringWriter.toString();
        } else
        {
            value = StringUtils.EMPTY_STRING;
        }
        folder.addKeyValuePair(PARENT_CODES, value);
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
        if (obj instanceof DataSet == false)
        {
            return false;
        }
        final DataSet that = (DataSet) obj;
        return that.code.equals(code);
    }

    @Override
    public final int hashCode()
    {
        int result = 17;
        result = 37 * result + code.hashCode();
        return result;
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder = new ToStringBuilder();
        builder.append(CODE, code);
        builder.append(IS_COMPLETE, isComplete);
        builder.append(IS_MEASURED, isMeasured);
        builder.append(OBSERVABLE_TYPE, observableTypeCode);
        builder.append(PARENT_CODES, parentCodes);
        builder.append(PRODUCER_CODE, producerCode);
        builder.append(PRODUCTION_TIMESTAMP, productionTimestamp);
        return builder.toString();
    }
}