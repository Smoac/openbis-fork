/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractorUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Data set info extractor dealing with BZ data. Creates experiments and plates if needed.
 * 
 * @author Izabela Adamczyk
 */
public class BZDataSetInfoExtractor implements IDataSetInfoExtractor
{

    static final String SPACE_CODE = "space-code";

    static final String PROJECT_CODE = "project-code";

    private final Properties properties;

    public BZDataSetInfoExtractor(final Properties properties)
    {
        this.properties = properties;
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        DirectoryDatasetInfoExtractor tokens =
                new DirectoryDatasetInfoExtractor(FilenameUtils.getBaseName(incomingDataSetPath
                        .getPath()));
        String spaceCode = PropertyUtils.getMandatoryProperty(properties, SPACE_CODE);
        String projectCode = PropertyUtils.getMandatoryProperty(properties, PROJECT_CODE);
        String sampleCode = getSampleCode(tokens);
        String experimentCode = getExperiment(tokens);
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(null, spaceCode, projectCode, experimentCode);
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new GroupIdentifier(DatabaseInstanceIdentifier.createHome(),
                        spaceCode), sampleCode);
        Sample sampleOrNull = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
        if (sampleOrNull == null)
        {
            Collection<VocabularyTerm> terms =
                    openbisService.listVocabularyTerms(ScreeningConstants.PLATE_GEOMETRY);
            List<String> plateGeometries = new ArrayList<String>();
            for (VocabularyTerm v : terms)
            {
                plateGeometries.add(v.getCode());
            }
            List<File> imageFiles = ImageFileExtractorUtils.listImageFiles(incomingDataSetPath);
            List<Location> plateLocations = new ArrayList<Location>();
            for (File imageFile : imageFiles)
            {
                String baseName = FilenameUtils.getBaseName(imageFile.getPath());
                String plateLocationToken =
                        HCSImageFileExtractor.extractFileInfo(baseName).getPlateLocationToken();
                plateLocations.add(Location
                        .tryCreateLocationFromTransposedMatrixCoordinate(plateLocationToken));
            }
            String plateGeometry =
                    PlateGeometryOracle.figureGeometry(plateLocations, plateGeometries);
            registerSampleWithExperiment(openbisService, sampleIdentifier, experimentIdentifier,
                    plateGeometry);
            sampleOrNull = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            if (sampleOrNull == null)
            {
                throw new UserFailureException(String.format("Sample '%s' could not be found",
                        sampleIdentifier));
            }
        }
        if (sampleOrNull.getExperiment() == null
                || new ExperimentIdentifier(sampleOrNull.getExperiment())
                        .equals(experimentIdentifier) == false)
        {
            throw new UserFailureException(String.format(
                    "Sample '%s' is not part of experiment '%s'", sampleIdentifier,
                    experimentIdentifier));
        }
        dataSetInformation.setSpaceCode(spaceCode);
        dataSetInformation.setSampleCode(sampleCode);
        return dataSetInformation;
    }

    private static void registerSampleWithExperiment(IEncapsulatedOpenBISService openbisService,
            SampleIdentifier sampleIdentifier, ExperimentIdentifier experimentIdentifier,
            String plateGeometry)
    {
        Experiment experimentOrNull = openbisService.tryToGetExperiment(experimentIdentifier);
        if (experimentOrNull == null)
        {
            registerExperiment(openbisService, experimentIdentifier);
            experimentOrNull = openbisService.tryToGetExperiment(experimentIdentifier);
            if (experimentOrNull == null)
            {
                throw new UserFailureException(String.format("Experiment '%s' could not be found",
                        experimentIdentifier));
            }
        }
        NewSample sample = new NewSample();
        sample.setExperimentIdentifier(experimentIdentifier.toString());
        sample.setIdentifier(sampleIdentifier.toString());
        SampleType sampleType = new SampleType();
        sampleType.setCode(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
        sample.setSampleType(sampleType);
        sample.setProperties(createPlateGeometryProperty(plateGeometry));
        openbisService.registerSample(sample, null);
    }

    private static IEntityProperty[] createPlateGeometryProperty(String plateGeometry)
    {
        List<IEntityProperty> sampleProperties = new ArrayList<IEntityProperty>();
        VocabularyTermValueEntityProperty property = new VocabularyTermValueEntityProperty();
        VocabularyTerm vocabularyTerm = new VocabularyTerm();
        vocabularyTerm.setCode(plateGeometry);
        property.setVocabularyTerm(vocabularyTerm);
        PropertyType propertyType = new PropertyType();
        DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.CONTROLLEDVOCABULARY);
        propertyType.setDataType(dataType);
        propertyType.setCode(ScreeningConstants.PLATE_GEOMETRY);
        property.setPropertyType(propertyType);
        sampleProperties.add(property);
        return sampleProperties.toArray(new IEntityProperty[sampleProperties.size()]);
    }

    private static void registerExperiment(IEncapsulatedOpenBISService openbisService,
            ExperimentIdentifier experimentIdentifier)
    {
        NewExperiment experiment = new NewExperiment();
        experiment.setExperimentTypeCode(ScreeningConstants.SIRNA_HCS);
        experiment.setIdentifier(experimentIdentifier.toString());
        openbisService.registerExperiment(experiment);
    }

    private String getExperiment(DirectoryDatasetInfoExtractor tokens)
    {
        return tokens.getExperimentToken();
    }

    private String getSampleCode(DirectoryDatasetInfoExtractor tokens)
    {
        return "P_" + tokens.getExperimentToken() + "_" + tokens.getTimestampToken();
    }

    private class DirectoryDatasetInfoExtractor
    {
        private final String experimentToken;

        private final String plateToken;

        private final String barcodeToken;

        private final String timestampToken;

        DirectoryDatasetInfoExtractor(String identifier)
        {
            String[] namedParts = StringUtils.split(identifier, "_");
            experimentToken = StringUtils.split(namedParts[0], "-")[1];
            plateToken = StringUtils.split(namedParts[1], "-")[1];
            barcodeToken = StringUtils.split(namedParts[2], "-")[1];
            timestampToken = StringUtils.split(namedParts[3], "-")[1];
        }

        public String getExperimentToken()
        {
            return experimentToken;
        }

        public String getPlateToken()
        {
            return plateToken;
        }

        public String getBarcodeToken()
        {
            return barcodeToken;
        }

        public String getTimestampToken()
        {
            return timestampToken;
        }

    }

}
