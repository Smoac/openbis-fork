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

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractorUtils;
import ch.systemsx.cisd.openbis.dss.etl.UnparsedImageFileInfoLexer;
import ch.systemsx.cisd.openbis.dss.etl.dto.UnparsedImageFileInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
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
 * Data set info extractor dealing with iBrain data. Creates experiments and plates if needed.
 * 
 * @author Izabela Adamczyk
 */
public class BZDataSetInfoExtractor implements IDataSetInfoExtractor
{

    static final String SPACE_CODE = "space-code";

    static final String PROJECT_CODE = "project-code";

    static final String PLATE_GEOMETRY = "plate-geometry";

    static final String SEPARATOR = "separator";

    private final String spaceCode;

    private final String projectCode;

    private final String defaultPlateGeometryOrNull;

    private final String separatorOrNull;

    public BZDataSetInfoExtractor(final Properties properties)
    {
        spaceCode = PropertyUtils.getMandatoryProperty(properties, SPACE_CODE);
        projectCode = PropertyUtils.getMandatoryProperty(properties, PROJECT_CODE);
        defaultPlateGeometryOrNull = properties.getProperty(PLATE_GEOMETRY);
        separatorOrNull = properties.getProperty(SEPARATOR);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {

        String fileBaseName = FilenameUtils.getBaseName(incomingDataSetPath.getPath());
        if (separatorOrNull != null)
        {
            int separatorIndex = fileBaseName.indexOf(separatorOrNull);
            if (separatorIndex != -1)
            {
                fileBaseName = fileBaseName.substring(0, separatorIndex);
            }
        }
        BZDatasetDirectoryNameTokenizer tokens = new BZDatasetDirectoryNameTokenizer(fileBaseName);
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
            String plateGeometry = defaultPlateGeometryOrNull;
            if (plateGeometry == null)
            {
                List<String> plateGeometries = loadPlateGeometries(openbisService);
                List<Location> plateLocations = extractPlateLocations(incomingDataSetPath);
                plateGeometry = PlateGeometryOracle.figureGeometry(plateLocations, plateGeometries);
            }
            registerSampleWithExperiment(openbisService, sampleIdentifier, experimentIdentifier,
                    plateGeometry);
            sampleOrNull = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            if (sampleOrNull == null)
            {
                throw new UserFailureException(String.format("Sample '%s' could not be found",
                        sampleIdentifier));
            }
        }
        checkSampleExperiment(experimentIdentifier, sampleIdentifier, sampleOrNull);

        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setSpaceCode(spaceCode);
        dataSetInformation.setSampleCode(sampleCode);
        return dataSetInformation;
    }

    private void checkSampleExperiment(ExperimentIdentifier experimentIdentifier,
            SampleIdentifier sampleIdentifier, Sample sampleOrNull)
    {
        if (sampleOrNull.getExperiment() == null
                || new ExperimentIdentifier(sampleOrNull.getExperiment())
                        .equals(experimentIdentifier) == false)
        {
            throw new UserFailureException(String.format(
                    "Sample '%s' is not part of experiment '%s'", sampleIdentifier,
                    experimentIdentifier));
        }
    }

    private List<String> loadPlateGeometries(IEncapsulatedOpenBISService openbisService)
    {
        Collection<VocabularyTerm> terms =
                openbisService.listVocabularyTerms(ScreeningConstants.PLATE_GEOMETRY);
        List<String> plateGeometries = new ArrayList<String>();
        for (VocabularyTerm v : terms)
        {
            plateGeometries.add(v.getCode());
        }
        return plateGeometries;
    }

    private static void registerSampleWithExperiment(IEncapsulatedOpenBISService openbisService,
            SampleIdentifier sampleIdentifier, ExperimentIdentifier experimentIdentifier,
            String plateGeometry)
    {
        Experiment experimentOrNull = openbisService.tryToGetExperiment(experimentIdentifier);
        if (experimentOrNull == null)
        {
            openbisService.registerExperiment(createExperimentSIRNAHCS(experimentIdentifier));
        }
        openbisService.registerSample(
                createPlate(sampleIdentifier, experimentIdentifier, plateGeometry), null);
    }

    private static String getExperiment(BZDatasetDirectoryNameTokenizer tokens)
    {
        return tokens.getExperimentToken();
    }

    private static String getSampleCode(BZDatasetDirectoryNameTokenizer tokens)
    {
        return "PLATE_" + tokens.getPlateBarcodeToken();
    }

    private static IEntityProperty[] createVocabularyProperty(String propertyTypeCode,
            String termCode)
    {
        List<IEntityProperty> sampleProperties = new ArrayList<IEntityProperty>();
        VocabularyTermValueEntityProperty property = new VocabularyTermValueEntityProperty();
        VocabularyTerm vocabularyTerm = new VocabularyTerm();
        vocabularyTerm.setCode(termCode);
        property.setVocabularyTerm(vocabularyTerm);
        PropertyType propertyType = new PropertyType();
        DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.CONTROLLEDVOCABULARY);
        propertyType.setDataType(dataType);
        propertyType.setCode(propertyTypeCode);
        property.setPropertyType(propertyType);
        sampleProperties.add(property);
        return sampleProperties.toArray(new IEntityProperty[sampleProperties.size()]);
    }

    private static IEntityProperty[] createVarcharProperty(String propertyTypeCode,
            String description)
    {
        List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        GenericValueEntityProperty property = new GenericValueEntityProperty();
        property.setValue(description);
        PropertyType propertyType = new PropertyType();
        DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        propertyType.setCode(propertyTypeCode);
        property.setPropertyType(propertyType);
        properties.add(property);
        return properties.toArray(new IEntityProperty[properties.size()]);
    }

    private static NewSample createPlate(SampleIdentifier sampleIdentifier,
            ExperimentIdentifier experimentIdentifier, String plateGeometry)
    {
        NewSample sample = new NewSample();
        sample.setExperimentIdentifier(experimentIdentifier.toString());
        sample.setIdentifier(sampleIdentifier.toString());
        SampleType sampleType = new SampleType();
        sampleType.setCode(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
        sample.setSampleType(sampleType);
        sample.setProperties(createVocabularyProperty(ScreeningConstants.PLATE_GEOMETRY,
                plateGeometry));
        return sample;
    }

    private static List<Location> extractPlateLocations(File incomingDataSetPath)
    {
        List<File> imageFiles = ImageFileExtractorUtils.listImageFiles(incomingDataSetPath);
        List<Location> plateLocations = new ArrayList<Location>();
        for (File imageFile : imageFiles)
        {
            UnparsedImageFileInfo imageInfo =
                    UnparsedImageFileInfoLexer.tryExtractHCSImageFileInfo(imageFile,
                            incomingDataSetPath);
            if (imageInfo != null)
            {
                String wellLocationToken = imageInfo.getWellLocationToken();
                plateLocations.add(Location
                        .tryCreateLocationFromTransposedMatrixCoordinate(wellLocationToken));
            }
        }
        return plateLocations;
    }

    private static NewExperiment createExperimentSIRNAHCS(ExperimentIdentifier experimentIdentifier)
    {
        NewExperiment experiment = new NewExperiment();
        experiment.setExperimentTypeCode(ScreeningConstants.HCS_SIRNA_EXPERIMENT_TYPE);
        experiment.setIdentifier(experimentIdentifier.toString());
        experiment.setProperties(createVarcharProperty(ScreeningConstants.DESCRIPTION, "-"));
        return experiment;

    }

}
