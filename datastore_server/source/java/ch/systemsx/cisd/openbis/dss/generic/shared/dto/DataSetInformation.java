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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExtractableData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Container class for data extracted from the data set directory.
 * 
 * @author Bernd Rinn
 */
public class DataSetInformation implements Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    /** The sample code (aka <i>barcode</i>). <b>CAN NOT</b> be <code>null</code>. */
    private String sampleCode;

    private SamplePropertyPE[] properties = SamplePropertyPE.EMPTY_ARRAY;

    /**
     * The database instance <i>UUID</i>.
     */
    private String instanceUUID;

    /**
     * The database instance code.
     */
    private String instanceCode;

    /**
     * The group code.
     */
    private String groupCode;

    /** An object that uniquely identifies the experiment. Can be <code>null</code>. */
    private ExperimentIdentifier experimentIdentifier;

    /** sample with properties, enriched with connected experiment with properties. */
    private transient SamplePE sample;

    private BooleanOrUnknown isCompleteFlag = BooleanOrUnknown.U;

    /**
     * A subset of {@link ExternalData} which gets set by the code extractor.
     * <p>
     * Initialized with <code>new ExtractableData()</code>.
     * </p>
     */
    private ExtractableData extractableData = new ExtractableData();

    /** This constructor is for serialization. */
    public DataSetInformation()
    {
    }

    public final BooleanOrUnknown getIsCompleteFlag()
    {
        return isCompleteFlag;
    }

    public final void setComplete(final boolean complete)
    {
        isCompleteFlag = BooleanOrUnknown.resolve(complete);
    }

    /**
     * Returns the sample properties.
     * 
     * @return never <code>null</code> but could return an empty array.
     */
    public final SamplePropertyPE[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final SamplePropertyPE[] properties)
    {
        this.properties = properties;
    }

    public final String getInstanceCode()
    {
        return instanceCode;
    }

    public final void setInstanceCode(final String instanceCode)
    {
        this.instanceCode = instanceCode;
    }

    public final String getInstanceUUID()
    {
        return instanceUUID;
    }

    public final void setInstanceUUID(final String instanceUUID)
    {
        this.instanceUUID = instanceUUID;
    }

    /** Sets <code>experimentIdentifier</code>. */
    public final void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    /**
     * Returns the identifier of experiment which makes it unique.
     * 
     * @return <code>null</code> if no <code>ExperimentIdentifier</code> has been set.
     */
    public final ExperimentIdentifier getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    /**
     * Returns the basic information about the experiment.
     */
    public ExperimentPE getExperiment()
    {
        return sample == null ? null : sample.getExperiment();
    }

    public SamplePE getSample()
    {
        return sample;
    }

    public void setSample(final SamplePE sample)
    {
        this.sample = sample;
    }

    /**
     * Returns the sample identifier.
     * 
     * @return <code>null</code> if <code>sampleCode</code> has not been set.
     */
    public final SampleIdentifier getSampleIdentifier()
    {
        if (sampleCode == null)
        {
            return null;
        }
        final DatabaseInstanceIdentifier databaseInstanceIdentifier =
                new DatabaseInstanceIdentifier(instanceCode);
        if (groupCode == null)
        {
            return new SampleIdentifier(databaseInstanceIdentifier, sampleCode);
        }
        return new SampleIdentifier(new GroupIdentifier(databaseInstanceIdentifier, groupCode),
                sampleCode);
    }

    public final String getSampleCode()
    {
        return sampleCode;
    }

    public final void setSampleCode(final String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public final String getDataSetCode()
    {
        return extractableData.getCode();
    }

    public final void setDataSetCode(final String dataSetCode)
    {
        extractableData.setCode(dataSetCode);
    }

    public final String getProducerCode()
    {
        return extractableData.getDataProducerCode();
    }

    public final void setProducerCode(final String producerCode)
    {
        extractableData.setDataProducerCode(producerCode);
    }

    public final Date getProductionDate()
    {
        return extractableData.getProductionDate();
    }

    public final void setProductionDate(final Date productionDate)
    {
        extractableData.setProductionDate(productionDate);
    }

    public final ExtractableData getExtractableData()
    {
        return extractableData;
    }

    public final void setExtractableData(final ExtractableData extractableData)
    {
        this.extractableData = extractableData;
    }

    public final String getParentDataSetCode()
    {
        return extractableData.getParentDataSetCode();
    }

    public final void setParentDataSetCode(final String parentDataSetCode)
    {
        extractableData.setParentDataSetCode(parentDataSetCode);
    }

    public final void setGroupCode(final String groupCode)
    {
        this.groupCode = groupCode;
    }

    public final String getGroupCode()
    {
        return groupCode;
    }

    public void setDataSetProperties(List<NewProperty> dataSetProperties)
    {
        extractableData.setDataSetProperties(dataSetProperties);
    }

    public List<NewProperty> getDataSetProperties()
    {
        return extractableData.getDataSetProperties();
    }

    public final String describe()
    {
        if (experimentIdentifier == null)
        {
            return String.format("CODE('%s') SAMPLE_CODE('%s')", extractableData.getCode(),
                    sampleCode);
        } else
        {
            return String.format("CODE('%s') SAMPLE_CODE('%s') EXPERIMENT('%s')", extractableData
                    .getCode(), sampleCode, experimentIdentifier.describe());
        }
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}