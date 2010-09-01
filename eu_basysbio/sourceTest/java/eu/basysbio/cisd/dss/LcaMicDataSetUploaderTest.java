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

package eu.basysbio.cisd.dss;

import static eu.basysbio.cisd.dss.LcaMicDataSetUploader.LCA_MIC_TIME_SERIES;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class LcaMicDataSetUploaderTest extends UploaderTestCase
{
    private final class ReaderMatcher extends BaseMatcher<Reader>
    {
        private final String content;
        
        private ReaderMatcher(String content)
        {
            this.content = content;
        }

        public boolean matches(Object item)
        {
            try
            {
                String actualContent = IOUtils.toString((Reader) item);
                assertEquals(content, actualContent);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText(content);
        }
    }

    private Mockery context;
    private ITimeSeriesDAO dao;
    private IEncapsulatedOpenBISService service;
    private IDatabaseFeeder databaseFeeder;
    private IDataSetValidator validator;
    private LcaMicDataSetUploader uploader;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(ITimeSeriesDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        databaseFeeder = context.mock(IDatabaseFeeder.class);
        validator = context.mock(IDataSetValidator.class);
        Properties properties = new Properties();
        properties.setProperty(TimeSeriesDataSetUploaderParameters.DATA_SET_TYPE_PATTERN_FOR_DEFAULT_HANDLING, ".*");
        uploader =
                new LcaMicDataSetUploader(dao, databaseFeeder, service, validator,
                        new TimeSeriesDataSetUploaderParameters(properties));
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testNonUniqueBbaIDs()
    {
        File tsvFile = new File(workingDirectory, "data.tsv");
        FileUtilities
                .writeToFile(
                        tsvFile,
                        "# Ma::MS::B1::NT::EX::T1::NC::GrowthRate::Value[h^(-1)]::LIN::BBA9001#A_S20090325-2::NC\t0.68\n"
                                + "Time (s)\t"
                                + "Ma::MS::B1::NT::EX::T1::NC::LcaMicCfd::Value[um]::LIN::BBA9001#A_S20090325-2::NC\t"
                                + "Ma::MS::B1::NT::EX::T1::NC::LcaMicAbsFl::Mean[Au]::LIN::BBA9002#A_S20090325-2::NC\t"
                                + "Ma::MS::B1::NT::EX::T1::NC::LcaMicAbsFl::Std[Au]::LIN::BBA9001#A_S20090325-2::NC\n"
                                + "12\t2.5\t5.5\tN/A\n42\t42.5\t45.5\t3.25\n");

        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier("p1", "e1"));
        dataSetInformation.setDataSetCode("abc-1");
        dataSetInformation.setUploadingUserEmail("ab@c.de");
        prepareResetDatabaseFeeder();
        prepareValidatorAndFeeder(dataSetInformation,
                "BBA ID\tMa::MS::B1::12::EX::T1::NC::LcaMicCfd::Value[um]::LIN::NB::NC\t"
                        + "Ma::MS::B1::42::EX::T1::NC::LcaMicCfd::Value[um]::LIN::NB::NC\n"
                        + "BBA9001#A_S20090325-2\t2.5\t42.5\n", 1);
        
        try
        {
            uploader.handleTSVFile(tsvFile, dataSetInformation);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Invalid headers: All BBA IDs should be the same. "
                    + "The folowing two different BBA IDs found: "
                    + "BBA9001#A_S20090325-2 BBA9002#A_S20090325-2", ex.getMessage());
        }

        context.assertIsSatisfied();
    }
    
    @Test
    public void testHappyCase()
    {
        File tsvFile = new File(workingDirectory, "data.tsv");
        FileUtilities.writeToFile(tsvFile, LcaMicDataSetPropertiesExtractorTest.EXAMPLE 
                + "12\t2.5\t5.5\tN/A\n42\t42.5\t45.5\t3.25\n");
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier("p1", "MA_MS_B1"));
        dataSetInformation.setDataSetCode("abc-1");
        dataSetInformation.setUploadingUserEmail("ab@c.de");
        prepareResetDatabaseFeeder();
        prepareValidatorAndFeeder(dataSetInformation,
                "BBA ID\tMa::MS::B1::12::EX::T1::NC::LcaMicCfd::Value[um]::LIN::NB::NC\t"
                        + "Ma::MS::B1::42::EX::T1::NC::LcaMicCfd::Value[um]::LIN::NB::NC\n"
                        + "BBA9001#A_S20090325-2\t2.5\t42.5\n", 1);
        prepareValidatorAndFeeder(dataSetInformation,
                "BBA ID\tMa::MS::B1::12::EX::T1::NC::LcaMicAbsFl::Mean[Au]::LIN::NB::NC\t"
                + "Ma::MS::B1::42::EX::T1::NC::LcaMicAbsFl::Mean[Au]::LIN::NB::NC\n"
                + "BBA9001#A_S20090325-2\t5.5\t45.5\n", 2);
        prepareValidatorAndFeeder(dataSetInformation,
                "BBA ID\tMa::MS::B1::12::EX::T1::NC::LcaMicAbsFl::Std[Au]::LIN::NB::NC\t"
                + "Ma::MS::B1::42::EX::T1::NC::LcaMicAbsFl::Std[Au]::LIN::NB::NC\n"
                + "BBA9001#A_S20090325-2\tN/A\t3.25\n", 3);
        
        uploader.handleTSVFile(tsvFile, dataSetInformation);
        
        context.assertIsSatisfied();
    }
    
    private void prepareResetDatabaseFeeder()
    {
        context.checking(new Expectations()
            {
                {
                    one(databaseFeeder).resetValueGroupIDGenerator();
                }
            });
    }
    
    private void prepareValidatorAndFeeder(final DataSetInformation dataSetInformation, final String content, final int numberOfDataSet)
    {
        context.checking(new Expectations()
            {
                {
                    ReaderMatcher matcher = new ReaderMatcher(content);
                    String sourceName = LCA_MIC_TIME_SERIES + numberOfDataSet;
                    one(validator).assertValidDataSet(with(new DataSetType(LCA_MIC_TIME_SERIES)),
                            with(matcher), with(sourceName));
                    one(databaseFeeder).feedDatabase(with(dataSetInformation), with(matcher),
                            with(sourceName));
                }
            });
    }

}
