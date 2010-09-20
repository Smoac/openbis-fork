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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExtractableData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Container class for data extracted from the data set directory.
 * 
 * @author Bernd Rinn
 */
public class DataSetInformation implements Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    private String sampleCode;

    // top sample properties
    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    private DataSetType dataSetType;

    /**
     * The database instance <i>UUID</i>.
     */
    private String instanceUUID;

    /**
     * The database instance code.
     */
    private String instanceCode;

    private String spaceCode;

    /** An object that uniquely identifies the experiment. Can be <code>null</code>. */
    private ExperimentIdentifier experimentIdentifier;

    /** sample with properties, enriched with connected experiment with properties. */
    private transient Sample sample;

    private transient Experiment experiment;

    private BooleanOrUnknown isCompleteFlag = BooleanOrUnknown.U;

    /**
     * A subset of {@link NewExternalData} which gets set by the code extractor.
     * <p>
     * Initialized with <code>new ExtractableData()</code>.
     * </p>
     */
    private ExtractableData extractableData = new ExtractableData();

    /**
     * Email of uploading user.
     */
    private String uploadingUserEmailOrNull;
    
    private String uploadingUserIdOrNull;

    /** This constructor is for serialization. */
    public DataSetInformation()
    {
    }

    public String tryGetUploadingUserEmail()
    {
        return uploadingUserEmailOrNull;
    }

    public void setUploadingUserEmail(String uploadingUserEmail)
    {
        this.uploadingUserEmailOrNull = uploadingUserEmail;
    }

    public void setUploadingUserId(String uploadingUserIdOrNull)
    {
        this.uploadingUserIdOrNull = uploadingUserIdOrNull;
    }

    public String getUploadingUserIdOrNull()
    {
        return uploadingUserIdOrNull;
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
    public final IEntityProperty[] getProperties()
    {
        return properties == null ? new IEntityProperty[0] : properties;
    }

    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

    public DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
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
    // It can be null when we use e.g. CifexDataSetInfoExtractor - only identifier is set.
    public Experiment tryToGetExperiment()
    {
        return experiment == null ? (sample == null ? null : sample.getExperiment()) : experiment;
    }

    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    public Sample tryToGetSample()
    {
        return sample;
    }

    public void setSample(final Sample sample)
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
        if (spaceCode == null)
        {
            return new SampleIdentifier(databaseInstanceIdentifier, sampleCode);
        }
        return new SampleIdentifier(new SpaceIdentifier(databaseInstanceIdentifier, spaceCode),
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

    public final List<String> getParentDataSetCodes()
    {
        return extractableData.getParentDataSetCodes();
    }

    public final void setParentDataSetCodes(List<String> parentDataSetCodes)
    {
        extractableData.setParentDataSetCodes(parentDataSetCodes);
    }

    public final void setSpaceCode(final String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public final String getSpaceCode()
    {
        return spaceCode;
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