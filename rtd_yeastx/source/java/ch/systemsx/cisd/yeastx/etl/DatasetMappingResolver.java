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

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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

    private static final String GROUP_CODE_PROPERTY_NAME = "group-code";

    private static final String PROPERTIES_PREFIX = "USER.";

    public static String getGroupCode(Properties properties)
    {
        String groupCode = properties.getProperty(GROUP_CODE_PROPERTY_NAME);
        if (groupCode == null)
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "No group code defined in server configuration. Use '%s' property to specify it.",
                            GROUP_CODE_PROPERTY_NAME);
        }
        return groupCode;
    }

    public static String tryGetUniquePropertyTypeCode(Properties properties)
    {
        return tryGetPropertyTypeCode(properties);
    }

    private final IEncapsulatedOpenBISService openbisService;

    private final String groupCode;

    private final String propertyCodeOrNull;

    public DatasetMappingResolver(Properties properties, IEncapsulatedOpenBISService openbisService)
    {
        this.openbisService = openbisService;
        this.propertyCodeOrNull = tryGetPropertyTypeCode(properties);
        this.groupCode = properties.getProperty(GROUP_CODE_PROPERTY_NAME);
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

    public String tryFigureSampleCode(DataSetMappingInformation mapping, File logDir)
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
            if (tryFigureExperiment(sampleCodeOrLabel) != null)
            {
                return sampleCodeOrLabel;
            }
        }
        ListSamplesByPropertyCriteria criteria =
                new ListSamplesByPropertyCriteria(propertyCodeOrNull, sampleCodeOrLabel, groupCode,
                        tryGetExperimentIdentifier(mapping));
        List<String> samples;
        try
        {
            samples = openbisService.listSamplesByCriteria(criteria);
        } catch (UserFailureException e)
        {
            logMappingError(mapping, logDir, e.getMessage());
            return null;
        }
        if (samples.size() == 1)
        {
            return samples.get(0);
        } else if (samples.size() == 0)
        {
            logMappingError(mapping, logDir, "there is no sample which matches the criteria <"
                    + criteria + ">");
            return null;
        } else
        {
            String errMsg =
                    String.format(
                            "there should be exacty one sample which matches the criteria '%s', but %d of them were found."
                                    + " Consider using the unique sample code.", criteria, samples
                                    .size());
            logMappingError(mapping, logDir, errMsg);
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

    public boolean isMappingCorrect(DataSetMappingInformation mapping, File logDir)
    {
        if (isExperimentColumnCorrect(mapping, logDir) == false)
        {
            return false;
        }
        String sampleCode = tryFigureSampleCode(mapping, logDir);
        if (sampleCode == null)
        {
            return false;
        }
        return existsAndBelongsToExperiment(mapping, logDir, sampleCode);
    }

    private boolean existsAndBelongsToExperiment(DataSetMappingInformation mapping, File logDir,
            String sampleCode)
    {
        ExperimentPE experiment = tryFigureExperiment(sampleCode);
        if (experiment == null)
        {
            logMappingError(mapping, logDir, String.format(
                    "sample with the code '%s' does not exist"
                            + " or is not connected to any experiment", sampleCode));
            return false;
        }
        return true;
    }

    private ExperimentPE tryFigureExperiment(String sampleCode)
    {
        SampleIdentifier sampleIdentifier = createSampleIdentifier(sampleCode);
        return openbisService.getBaseExperiment(sampleIdentifier);
    }

    private SampleIdentifier createSampleIdentifier(String sampleCode)
    {
        return new SampleIdentifier(new GroupIdentifier((String) null, groupCode), sampleCode);
    }

    private boolean isExperimentColumnCorrect(DataSetMappingInformation mapping, File logDir)
    {
        if ((mapping.getExperimentCode() == null) != (mapping.getProjectCode() == null))
        {
            logMappingError(mapping, logDir,
                    "experiment and project columns should be both empty or should be both filled.");
            return false;
        }
        if (propertyCodeOrNull == null && mapping.getExperimentCode() != null)
        {
            logMappingError(
                    mapping,
                    logDir,
                    "openBis is not configured to use the sample label to identify the sample."
                            + " You can still identify the sample by the code (clear the experiment column in this case)."
                            + " You can also contact your administrator to change the server configuration and set the property type code which should be used.");
            return false;
        }
        return true;
    }

    private void logMappingError(DataSetMappingInformation mapping, File logDir, String errorMessage)
    {
        LogUtils.error(logDir, "Mapping for file " + mapping.getFileName() + " is incorrect: "
                + errorMessage);
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
