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
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractorUtils;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor.ImageFileInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Utility class containing methods useful in iBrain integration.
 * 
 * @author Izabela Adamczyk
 */
public class Utils
{
    static ImageFileInfo extractImageFileInfo(File imageFile)
    {
        return extractBZImageFileInfo(FilenameUtils.getBaseName(imageFile.getPath()));
    }

    /**
     * Extracts useful information from dataset image file name specific to iBrain2.
     */
    static ImageFileInfo extractBZImageFileInfo(String text)
    {
        String[] namedParts = StringUtils.split(text, "_");
        final String plateLocationToken = StringUtils.split(namedParts[3], "-")[1];
        final String wellLocationToken = StringUtils.split(namedParts[4], "-")[1];
        final String timepointToken = StringUtils.split(namedParts[5], "-")[1];
        final String channelToken = StringUtils.split(namedParts[6], "-")[1];
        ImageFileInfo info = new ImageFileInfo();
        info.setPlateLocationToken(plateLocationToken);
        info.setWellLocationToken(wellLocationToken);
        info.setChannelToken(channelToken);
        info.setTimepointToken(timepointToken);
        return info;
    }

    static IEntityProperty[] createVocabularyProperty(String propertyTypeCode, String termCode)
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

    static IEntityProperty[] createVarcharProperty(String propertyTypeCode, String description)
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

    static NewSample createPlate(SampleIdentifier sampleIdentifier,
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

    static List<Location> extractPlateLocations(File incomingDataSetPath)
    {
        List<File> imageFiles = ImageFileExtractorUtils.listImageFiles(incomingDataSetPath);
        List<Location> plateLocations = new ArrayList<Location>();
        for (File imageFile : imageFiles)
        {
            String baseName = FilenameUtils.getBaseName(imageFile.getPath());
            String plateLocationToken = extractBZImageFileInfo(baseName).getPlateLocationToken();
            plateLocations.add(Location
                    .tryCreateLocationFromTransposedMatrixCoordinate(plateLocationToken));
        }
        return plateLocations;
    }

    static NewExperiment createExperimentSIRNAHCS(ExperimentIdentifier experimentIdentifier)
    {
        NewExperiment experiment = new NewExperiment();
        experiment.setExperimentTypeCode(ScreeningConstants.SIRNA_HCS);
        experiment.setIdentifier(experimentIdentifier.toString());
        experiment.setProperties(createVarcharProperty(ScreeningConstants.DESCRIPTION, "-"));
        return experiment;

    }

}
