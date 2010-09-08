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
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
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

    private final String spaceCode;

    private final String projectCode;

    public BZDataSetInfoExtractor(final Properties properties)
    {
        spaceCode = PropertyUtils.getMandatoryProperty(properties, SPACE_CODE);
        projectCode = PropertyUtils.getMandatoryProperty(properties, PROJECT_CODE);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {

        BZDatasetDirectoryNameTokenizer tokens =
                new BZDatasetDirectoryNameTokenizer(FilenameUtils.getBaseName(incomingDataSetPath
                        .getPath()));
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
            List<String> plateGeometries = loadPlateGeometries(openbisService);
            List<Location> plateLocations = Utils.extractPlateLocations(incomingDataSetPath);
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
            openbisService.registerExperiment(Utils.createExperimentSIRNAHCS(experimentIdentifier));
            experimentOrNull = openbisService.tryToGetExperiment(experimentIdentifier);
            if (experimentOrNull == null)
            {
                throw new UserFailureException(String.format("Experiment '%s' could not be found",
                        experimentIdentifier));
            }
        }
        openbisService.registerSample(Utils.createPlate(sampleIdentifier, experimentIdentifier,
                plateGeometry), null);
    }

    private static String getExperiment(BZDatasetDirectoryNameTokenizer tokens)
    {
        return tokens.getExperimentToken();
    }

    private static String getSampleCode(BZDatasetDirectoryNameTokenizer tokens)
    {
        return "P_" + tokens.getExperimentToken() + "_" + tokens.getTimestampToken();
    }

}
