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
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
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
    private final static String UNIQUE_SAMPLE_NAME_PROPERTY = "unique-sample-name-property-code";

    /**
     * The property type code for property which is supposed to have a unique value for all
     * experiments in one project.
     */
    private final static String UNIQUE_EXPERIMENT_NAME_PROPERTY =
            "unique-experiment-name-property-code";

    private static final String PROPERTIES_PREFIX = "USER.";

    public static String getUniqueSampleNamePropertyCode(Properties properties)
    {
        return getUniqueNamePropertyCode(properties, UNIQUE_SAMPLE_NAME_PROPERTY);
    }

    public static String getUniqueExperimentNamePropertyCode(Properties properties)
    {
        return getUniqueNamePropertyCode(properties, UNIQUE_EXPERIMENT_NAME_PROPERTY);
    }

    private static String tryGetUniqueSampleNamePropertyCode(Properties properties)
    {
        return tryGetUniqueNamePropertyCode(properties, UNIQUE_SAMPLE_NAME_PROPERTY);
    }

    private static String tryGetUniqueExperimentNamePropertyCode(Properties properties)
    {
        return tryGetUniqueNamePropertyCode(properties, UNIQUE_EXPERIMENT_NAME_PROPERTY);
    }

    private static String getUniqueNamePropertyCode(Properties properties, String propertyName)
    {
        String name = tryGetUniqueNamePropertyCode(properties, propertyName);
        if (name == null)
        {
            throw EnvironmentFailureException.fromTemplate("Property '%s' is not set.",
                    propertyName);
        }
        return name;
    }

    private static String tryGetUniqueNamePropertyCode(Properties properties, String propertyName)
    {
        String code = properties.getProperty(propertyName);
        if (code != null)
        {
            return adaptPropertyCode(code);
        } else
        {
            return null;
        }
    }

    // ---------------
    private final IEncapsulatedOpenBISService openbisService;

    private final String samplePropertyCodeOrNull;

    private final String experimentPropertyCodeOrNull;

    public DatasetMappingResolver(Properties properties, IEncapsulatedOpenBISService openbisService)
    {
        this.openbisService = openbisService;
        this.samplePropertyCodeOrNull = tryGetUniqueSampleNamePropertyCode(properties);
        this.experimentPropertyCodeOrNull = tryGetUniqueExperimentNamePropertyCode(properties);
    }

    public String tryFigureSampleCode(DataSetMappingInformation mapping, LogUtils log)
    {
        String sampleCodeOrLabel = mapping.getSampleCodeOrLabel();
        if (samplePropertyCodeOrNull == null)
        {
            return sampleCodeOrLabel;
        }
        if (mapping.getExperimentName() == null)
        {
            // The main purpose of this checks is to ensure that sample with the given code exists.
            // If it is not a case, we will try to check if the specified sample label is unique (in
            // all experiments).
            if (isConnectedToExperiment(sampleCodeOrLabel, mapping, log))
            {
                return sampleCodeOrLabel;
            }
        }
        LocalExperimentIdentifier experimentIdentifier =
                tryGetExperimentIdentifier(mapping, experimentPropertyCodeOrNull);
        ListSamplesByPropertyCriteria criteria =
                new ListSamplesByPropertyCriteria(samplePropertyCodeOrNull, sampleCodeOrLabel,
                        mapping.getGroupCode(), experimentIdentifier);
        List<String> samples;
        try
        {
            samples = openbisService.listSamplesByCriteria(criteria);
        } catch (UserFailureException e)
        {
            log.datasetMappingError(mapping, e.getMessage());
            return null;
        }
        if (samples.size() == 1)
        {
            return samples.get(0);
        } else if (samples.size() == 0)
        {
            log.datasetMappingError(mapping, "there is no sample which matches the criteria <"
                    + criteria + ">");
            return null;
        } else
        {
            String errMsg =
                    String.format(
                            "there should be exacty one sample which matches the criteria '%s', but %d of them were found."
                                    + " Consider using the unique sample code.", criteria, samples
                                    .size());
            log.datasetMappingError(mapping, errMsg);
            return null;
        }
    }

    private static LocalExperimentIdentifier tryGetExperimentIdentifier(
            DataSetMappingInformation mapping, String experimentPropertyCodeOrNull)
    {
        String experimentName = mapping.getExperimentName();
        String projectCode = mapping.getProjectCode();
        if (experimentName != null && projectCode != null)
        {
            if (experimentPropertyCodeOrNull != null)
            {
                return new LocalExperimentIdentifier(projectCode, experimentPropertyCodeOrNull,
                        experimentName);
            } else
            {
                return new LocalExperimentIdentifier(projectCode, experimentName);
            }
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
            String availableConvTypes =
                    CollectionUtils.abbreviate(MLConversionType.values(),
                            MLConversionType.values().length);
            log.datasetMappingError(mapping, "unexpected value '%s' in 'conversion' column. "
                    + "Leave the column empty or use one of the allowed values: %s.",
                    conversionText, availableConvTypes);
            return false;
        }

        boolean conversionRequired = isConversionRequired(mapping);
        if (conversion == MLConversionType.NONE && conversionRequired)
        {
            log.datasetMappingError(mapping, "conversion column cannot be empty "
                    + "for this type of file.");
            return false;
        }
        if (conversion != MLConversionType.NONE && conversionRequired == false)
        {
            log.datasetMappingError(mapping, "conversion column must be empty "
                    + "for this type of file.");
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
        if (isConnectedToExperiment(sampleCode, mapping, log) == false)
        {
            log.datasetMappingError(mapping, "sample with the code '%s' does not exist"
                    + " or is not connected to any experiment", sampleCode);
            return false;
        }
        return true;
    }

    private boolean isConnectedToExperiment(String sampleCode, DataSetMappingInformation mapping,
            LogUtils log)
    {
        SampleIdentifier sampleIdentifier = createSampleIdentifier(sampleCode, mapping);
        try
        {
            SamplePE sample = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            return sample != null && sample.getExperiment() != null;
        } catch (UserFailureException e)
        {
            log.datasetMappingError(mapping,
                    "error when checking if sample '%s' belongs to an experiment: %s",
                    sampleIdentifier, e.getMessage());
            return false;
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
        if ((mapping.getExperimentName() == null) != (mapping.getProjectCode() == null))
        {
            log
                    .datasetMappingError(mapping,
                            "experiment and project columns should be both empty or should be both filled.");
            return false;
        }
        if (samplePropertyCodeOrNull == null && mapping.getExperimentName() != null)
        {
            log
                    .datasetMappingError(
                            mapping,
                            "openBis is not configured to use the sample label to identify the sample."
                                    + " You can still identify the sample by the code (clear the experiment column in this case)."
                                    + " You can also contact your administrator to change the server configuration and set the property type code which should be used.");
            return false;
        }
        return true;
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
        if (propertyCode.toLowerCase().startsWith(PROPERTIES_PREFIX.toLowerCase()) == false)
        {
            return PROPERTIES_PREFIX + propertyCode;
        } else
        {
            return propertyCode;
        }
    }
}
