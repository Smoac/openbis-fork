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

import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
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
        return properties.getProperty(propertyName);
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
        if (sampleCodeOrLabel == null)
        {
            return null;
        }
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
        ListSamplesByPropertyCriteria criteria =
                createFindSampleByNameCriteria(mapping, sampleCodeOrLabel);
        List<Sample> samples = tryListSamplesByCriteria(criteria, mapping, log);
        if (samples == null)
        {
            return null; // some error occurred
        } else if (samples.size() == 1)
        {
            return samples.get(0).getCode();
        } else if (samples.size() == 0)
        {
            // try to assume that the sample code, not name, has been provided
            if (isConnectedToExperiment(sampleCodeOrLabel, mapping, log))
            {
                return sampleCodeOrLabel;
            } else
            {
                log.datasetMappingError(mapping, "there is no sample which matches the criteria <"
                        + criteria + ">");
                return null;
            }
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

    private List<Sample> tryListSamplesByCriteria(ListSamplesByPropertyCriteria criteria,
            DataSetMappingInformation mapping, LogUtils log)
    {
        try
        {
            return openbisService.listSamplesByCriteria(criteria);
        } catch (UserFailureException e)
        {
            log.datasetMappingError(mapping, e.getMessage());
            return null;
        }
    }

    private ListSamplesByPropertyCriteria createFindSampleByNameCriteria(
            DataSetMappingInformation mapping, String sampleCodeOrLabel)
    {
        LocalExperimentIdentifier experimentIdentifier =
                tryGetExperimentIdentifier(mapping, experimentPropertyCodeOrNull);
        ListSamplesByPropertyCriteria criteria =
                new ListSamplesByPropertyCriteria(samplePropertyCodeOrNull, sampleCodeOrLabel,
                        mapping.getSpaceOrGroupCode(), experimentIdentifier);
        return criteria;
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
        if (mapping.getSpaceOrGroupCode() == null)
        {
            log
                    .datasetMappingError(
                            mapping,
                            "mandatory property 'space' (previously called 'group') not specified. "
                                    + "Note: the name 'group' may still be used but will be forbidden in the future.");
            return false;
        } else if (mapping.getSpaceCode() != null && mapping.getGroupCode() != null)
        {
            log
                    .datasetMappingError(
                            mapping,
                            "either 'space' or 'group' should be specified - but not both. "
                                    + "Note: the name 'group' may still be used but will be forbidden in the future.");
            return false;
        }
        if (isExperimentColumnCorrect(mapping, log) == false)
        {
            return false;
        }
        if (isConversionColumnValid(mapping, log) == false)
        {
            return false;
        }
        ExperimentIdentifier experimentIdentifier = tryFigureExperimentIdentifier(mapping);
        if (mapping.getSampleCodeOrLabel() == null && experimentIdentifier == null)
        {
            log.datasetMappingError(mapping, "neither sample nor experiment has been specified.");
            return false;
        }
        String sampleCode = tryFigureSampleCode(mapping, log);
        if (sampleCode == null)
        {
            if (mapping.getSampleCodeOrLabel() != null)
            {
                return false; // error has been already reported
            }
            assert experimentIdentifier != null : "experimentIdentifier should be not null here";
            return experimentExists(mapping, log, experimentIdentifier);
        } else
        {
            if (StringUtils.isBlank(mapping.getParentDataSetCodes()) == false)
            {
                log.datasetMappingError(mapping,
                        "when dataset is connected to a sample it cannot have parent datasets.");
                return false;
            }
            return sampleExistsAndBelongsToExperiment(mapping, log, sampleCode);
        }
    }

    private boolean experimentExists(DataSetMappingInformation mapping, LogUtils log,
            ExperimentIdentifier experimentIdentifier)
    {
        try
        {
            Experiment experiment = openbisService.tryToGetExperiment(experimentIdentifier);
            if (experiment == null)
            {
                log.datasetMappingError(mapping, "experiment '%s' does not exist",
                        experimentIdentifier);
                return false;
            } else
            {
                return true;
            }
        } catch (UserFailureException ex)
        {
            // if project or group is unknown then an exception is thrown
            log.datasetMappingError(mapping, "experiment '%s' does not exist: %s",
                    experimentIdentifier, ex.getMessage());
            return false;
        }
    }

    /**
     * NOTE: we do not support experiment names if the dataset has to be connected to the experiment
     * directly.
     */
    public static ExperimentIdentifier tryFigureExperimentIdentifier(
            DataSetMappingInformation mapping)
    {
        String project = mapping.getProjectCode();
        String experimentCode = mapping.getExperimentName();
        if (project != null && experimentCode != null)
        {
            return new ExperimentIdentifier(null, mapping.getSpaceOrGroupCode(), project,
                    experimentCode);
        } else
        {
            return null;
        }
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

        boolean conversionAllowed = isConversionAllowed(mapping);
        if (conversion != MLConversionType.NONE && conversionAllowed == false)
        {
            log.datasetMappingError(mapping, "conversion column must be empty "
                    + "for this type of file.");
            return false;

        }
        return true;
    }

    private static boolean isConversionAllowed(final DataSetMappingInformation dataset)
    {
        String extension = FilenameUtils.getExtension(dataset.getFileName());
        boolean conversionRequired = extension.equalsIgnoreCase(ConstantsYeastX.MZXML_EXT);
        return conversionRequired;
    }

    private boolean sampleExistsAndBelongsToExperiment(DataSetMappingInformation mapping,
            LogUtils log, String sampleCode)
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
            Sample sample = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
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
        return new SampleIdentifier(new GroupIdentifier((String) null, mapping
                .getSpaceOrGroupCode()), sampleCode);
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
}
