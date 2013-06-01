/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.knime.core.node.NodeSettingsRO;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.knime.common.AggregatedDataImportDescription;
import ch.systemsx.cisd.openbis.knime.common.ParameterBindings;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.knime.server.Constants;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTableModelTranslator;

/**
 * @author Franz-Josef Elmer
 */
public class AggregatedDataFileImportNodeModelTest extends AbstractFileSystemTestCase
{
    private static final String SESSION_TOKEN = "session";

    private static final String DATA_STORE_CODE = "DS";

    private static final class MockAggregatedDataFileImportNodeModel extends AggregatedDataFileImportNodeModel
    {
        private final IQueryApiFacade facade;

        private final File sysTempDir;

        MockAggregatedDataFileImportNodeModel(IQueryApiFacade facade, File sysTempDir)
        {
            this.facade = facade;
            this.sysTempDir = sysTempDir;
        }

        @Override
        protected File getSystemTempDir()
        {
            return sysTempDir;
        }

        @Override
        protected IQueryApiFacade createQueryFacade()
        {
            return facade;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IQueryApiFacade facade;

    private IGeneralInformationService service;

    private File tempDir;

    private MockAggregatedDataFileImportNodeModel model;

    private NodeSettingsRO nodeSettingsRO;

    private AggregationServiceDescription description;

    @BeforeMethod
    public void setUpModel() throws Exception
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        facade = context.mock(IQueryApiFacade.class);
        nodeSettingsRO = context.mock(NodeSettingsRO.class);
        description = new AggregationServiceDescription();
        description.setDataStoreCode(DATA_STORE_CODE);
        description.setServiceKey(AggregatedDataImportDescription.PREFIX + "as");
        ArrayList<AggregatedDataImportDescription> descriptions = new ArrayList<AggregatedDataImportDescription>();
        AggregatedDataImportDescription.addDescriptionIfDataTable(descriptions, description);
        final byte[] bytes = Util.serializeDescription(descriptions.get(0));
        context.checking(new Expectations()
            {
                {
                    allowing(nodeSettingsRO).getByteArray(AggregatedDataImportDescription.AGGREGATION_DESCRIPTION_KEY);
                    will(returnValue(bytes));

                    allowing(nodeSettingsRO).getStringArray(ParameterBindings.PARAMETER_KEYS_KEY);
                    allowing(nodeSettingsRO).getStringArray(ParameterBindings.PARAMETER_VALUES_KEY);

                    allowing(facade).getSessionToken();
                    will(returnValue(SESSION_TOKEN));

                    allowing(facade).getGeneralInformationService();
                    will(returnValue(service));
                }
            });
        tempDir = new File(workingDirectory, "temp");
        model = new MockAggregatedDataFileImportNodeModel(facade, tempDir);
        File reportFile = new File(workingDirectory, "session_workspace_file_download");
        FileUtilities.writeToFile(reportFile, "hello world");
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testExecute() throws Exception
    {
        final RecordingMatcher<AggregationServiceDescription> descriptionRecorder = new RecordingMatcher<AggregationServiceDescription>();
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(Constants.FILE_NAME_COLUMN);
        builder.addRow().setCell(Constants.FILE_NAME_COLUMN, "report.txt");
        final QueryTableModel tableModel = new QueryTableModelTranslator(builder.getTableModel()).translate();
        context.checking(new Expectations()
            {
                {
                    one(facade).createReportFromAggregationService(with(descriptionRecorder), with(new HashMap<String, Object>()));
                    will(returnValue(tableModel));

                    one(facade).logout();

                    one(service).listDataStores(SESSION_TOKEN);
                    DataStore dataStore = new DataStore(DATA_STORE_CODE, "file://" + workingDirectory.getAbsolutePath(), "");
                    will(returnValue(Arrays.asList(dataStore)));
                }
            });
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        logRecorder.resetLogContent();

        model.execute(null, null);

        assertEquals("AggregationServiceDescription [dataStoreCode=DS, serviceKey=knime-as, label=null, type=null]",
                descriptionRecorder.recordedObject().toString());
        assertEquals("hello world", FileUtilities.loadToString(new File(tempDir, "knime-openbis-session/report.txt")).trim());
        assertEquals("INFO  " + MockAggregatedDataFileImportNodeModel.class.getName()
                + " - Content MIME type: text/plain", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

}
