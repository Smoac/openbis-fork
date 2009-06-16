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

package ch.systemsx.cisd.yeastx.etl;

import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.LocalExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Tomasz Pylak
 */
class DatasetMappingResolver
{
    /**
     * The property type code for property which is supposed to have a unique value for all samples
     * in one experiment. It let's to identify the sample which should be attached to a dataset.
     * <p>
     * If property type code is not specified, only the sample code can be used to identify the
     * sample.
     * </p>
     */
    private final static String PROPERTY_TYPE_CODE_PROPERTY_NAME = "unique-property-type-code";

    private static final String PROPERTIES_PREFIX = "USER.";

    public static String tryGetUniquePropertyTypeCode(Properties properties)
    {
        return tryGetPropertyTypeCode(properties);
    }

    private final IEncapsulatedOpenBISService openbisService;

    private final String propertyCodeOrNull;

    public DatasetMappingResolver(Properties properties, IEncapsulatedOpenBISService openbisService)
    {
        this.openbisService = openbisService;
        this.propertyCodeOrNull = tryGetPropertyTypeCode(properties);
    }

    private static String tryGetPropertyTypeCode(Properties properties)
    {
        String code = properties.getProperty(PROPERTY_TYPE_CODE_PROPERTY_NAME);
        if (code != null)
        {
            return adaptPropertyCode(code);
        } else
        {
            return null;
        }
    }

    public String tryFigureSampleCode(DataSetMappingInformation mapping, LogUtils log)
    {
        String sampleCodeOrLabel = mapping.getSampleCodeOrLabel();
        if (propertyCodeOrNull == null)
        {
            return sampleCodeOrLabel;
        }
        if (mapping.getExperimentCode() == null)
        {
            // The main purpose of this checks is to ensure that sample with the given code exists.
            // If it is not a case, we will try to check if the specified sample label is unique (in
            // all experiments).
            if (tryFigureExperiment(sampleCodeOrLabel, mapping, log) != null)
            {
                return sampleCodeOrLabel;
            }
        }
        ListSamplesByPropertyCriteria criteria =
                new ListSamplesByPropertyCriteria(propertyCodeOrNull, sampleCodeOrLabel, mapping
                        .getGroupCode(), tryGetExperimentIdentifier(mapping));
        List<String> samples;
        try
        {
            samples = openbisService.listSamplesByCriteria(criteria);
        } catch (UserFailureException e)
        {
            logMappingError(mapping, log, e.getMessage());
            return null;
        }
        if (samples.size() == 1)
        {
            return samples.get(0);
        } else if (samples.size() == 0)
        {
            logMappingError(mapping, log, "there is no sample which matches the criteria <"
                    + criteria + ">");
            return null;
        } else
        {
            String errMsg =
                    String.format(
                            "there should be exacty one sample which matches the criteria '%s', but %d of them were found."
                                    + " Consider using the unique sample code.", criteria, samples
                                    .size());
            logMappingError(mapping, log, errMsg);
            return null;
        }
    }

    private static LocalExperimentIdentifier tryGetExperimentIdentifier(
            DataSetMappingInformation mapping)
    {
        String experimentCode = mapping.getExperimentCode();
        String projectCode = mapping.getProjectCode();
        if (experimentCode != null && projectCode != null)
        {
            return new LocalExperimentIdentifier(projectCode, experimentCode);
        } else
        {
            return null;
        }
    }

    public boolean isMappingCorrect(DataSetMappingInformation mapping, LogUtils log)
    {
        if (isExperimentColumnCorrect(mapping, log) == false)
        {
            return false;
        }
        String sampleCode = tryFigureSampleCode(mapping, log);
        if (sampleCode == null)
        {
            return false;
        }
        return isConversionColumnValid(mapping, log)
                && existsAndBelongsToExperiment(mapping, log, sampleCode);
    }

    private static boolean isConversionColumnValid(final DataSetMappingInformation mapping,
            LogUtils log)
    {
        String conversionText = mapping.getConversion();
        MLConversionType conversion = MLConversionType.tryCreate(conversionText);
        if (conversion == null)
        {
            log.userError(String.format(
                    "Error for file '%s'. Unexpected value '%s' in 'conversion' column. "
                            + "Leave the column empty or use one of the allowed values: %s.",
                    mapping.getFileName(), conversionText, CollectionUtils.abbreviate(
                            MLConversionType.values(), MLConversionType.values().length)));
            return false;
        }

        boolean conversionRequired = isConversionRequired(mapping);
        if (conversion == MLConversionType.NONE && conversionRequired)
        {
            log.userError("Error for file '%s'. Conversion column cannot be empty "
                    + "for this type of file.", mapping.getFileName());
            return false;
        }
        if (conversion != MLConversionType.NONE && conversionRequired == false)
        {
            log.userError("Error for file '%s'. Conversion column must be empty "
                    + "for this type of file.", mapping.getFileName());
            return false;
        }
        return true;
    }

    private static boolean isConversionRequired(final DataSetMappingInformation dataset)
    {
        String extension = FilenameUtils.getExtension(dataset.getFileName());
        boolean conversionRequired = extension.equalsIgnoreCase(ConstantsYeastX.MZXML_EXT);
        return conversionRequired;
    }

    private boolean existsAndBelongsToExperiment(DataSetMappingInformation mapping, LogUtils log,
            String sampleCode)
    {
        ExperimentPE experiment = tryFigureExperiment(sampleCode, mapping, log);
        if (experiment == null)
        {
            logMappingError(mapping, log, String.format("sample with the code '%s' does not exist"
                    + " or is not connected to any experiment", sampleCode));
            return false;
        }
        return true;
    }

    private ExperimentPE tryFigureExperiment(String sampleCode, DataSetMappingInformation mapping,
            LogUtils log)
    {
        SampleIdentifier sampleIdentifier = createSampleIdentifier(sampleCode, mapping);
        try
        {
            return openbisService.getBaseExperiment(sampleIdentifier);
        } catch (UserFailureException e)
        {
            log.userError("Error when checking if sample '%s' belongs to an experiment: %s",
                    sampleIdentifier, e.getMessage());
            return null;
        }
    }

    private SampleIdentifier createSampleIdentifier(String sampleCode,
            DataSetMappingInformation mapping)
    {
        return new SampleIdentifier(new GroupIdentifier((String) null, mapping.getGroupCode()),
                sampleCode);
    }

    private boolean isExperimentColumnCorrect(DataSetMappingInformation mapping, LogUtils log)
    {
        if ((mapping.getExperimentCode() == null) != (mapping.getProjectCode() == null))
        {
            logMappingError(mapping, log,
                    "experiment and project columns should be both empty or should be both filled.");
            return false;
        }
        if (propertyCodeOrNull == null && mapping.getExperimentCode() != null)
        {
            logMappingError(
                    mapping,
                    log,
                    "openBis is not configured to use the sample label to identify the sample."
                            + " You can still identify the sample by the code (clear the experiment column in this case)."
                            + " You can also contact your administrator to change the server configuration and set the property type code which should be used.");
            return false;
        }
        return true;
    }

    private void logMappingError(DataSetMappingInformation mapping, LogUtils log,
            String errorMessage)
    {
        log.userError("Mapping for file " + mapping.getFileName() + " is incorrect: " + errorMessage);
    }

    public static void adaptPropertyCodes(List<DataSetMappingInformation> list)
    {
        for (DataSetMappingInformation mapping : list)
        {
            adaptPropertyCodes(mapping.getProperties());
        }
    }

    private static List<NewProperty> adaptPropertyCodes(List<NewProperty> properties)
    {
        for (NewProperty prop : properties)
        {
            String propertyCode = adaptPropertyCode(prop.getPropertyCode());
            prop.setPropertyCode(propertyCode);
        }
        return properties;
    }

    private static String adaptPropertyCode(String propertyCode)
    {
        if (propertyCode.startsWith(PROPERTIES_PREFIX) == false)
        {
            return PROPERTIES_PREFIX + propertyCode;
        } else
        {
            return propertyCode;
        }
    }
}
