/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObject;
import org.knime.core.data.uri.URIPortObjectSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.Credentials;
import org.knime.core.node.workflow.ICredentials;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;
import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetFileImportNodeModelTest extends AbstractFileSystemTestCase
{
    private static final String FILE_PATH = "document.txt";

    private static final String DATA_SET_CODE = "DS-42";

    private static final String MY_PASSWORD = "my-password";

    private static final String MY_PASSWORD2 = "my-password2";

    private static final String CREDENTIALS_NAME = "my-credentials";

    private static final String USER = "albert";

    private static final String USER2 = "isaac";

    private static final String URL = "https://open.bis";

    private static final class Model extends DataSetFileImportNodeModel
    {
        private final Map<String, String> flowVariables = new TreeMap<String, String>();

        private final Map<String, ICredentials> credentialsMap = new HashMap<String, ICredentials>();

        public Model(IOpenbisServiceFacadeFactory factory)
        {
            super(factory);
        }

        @Override
        protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
                throws InvalidSettingsException
        {
            super.loadValidatedSettingsFrom(settings);
        }

        @Override
        protected void saveSettingsTo(NodeSettingsWO settings)
        {
            super.saveSettingsTo(settings);
        }

        @Override
        protected void addFlowVariable(String name, String value)
        {
            flowVariables.put(name, value);
        }

        void addCredentials(String name, ICredentials credentials)
        {
            credentialsMap.put(name, credentials);
        }

        @Override
        protected ICredentials getCredentials(String name)
        {
            return credentialsMap.get(name);
        }

        String getUrl()
        {
            return url;
        }

        String getUserId()
        {
            return userID;
        }

        String getPassword()
        {
            return password;
        }
    }

    private Mockery context;

    private IOpenbisServiceFacadeFactory facadeFactory;

    private Model model;

    private NodeSettingsRO nodeSettingsRO;

    private NodeSettingsWO nodeSettingsWO;

    private IOpenbisServiceFacade facade;

    private IDssComponent dssComponent;

    private IDataSetDss dataSetDss;

    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet metadata;

    private DataSet dataSet;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        context = new Mockery();
        facadeFactory = context.mock(IOpenbisServiceFacadeFactory.class);
        nodeSettingsRO = context.mock(NodeSettingsRO.class);
        nodeSettingsWO = context.mock(NodeSettingsWO.class);
        dataSetDss = context.mock(IDataSetDss.class);
        facade = context.mock(IOpenbisServiceFacade.class);
        DataSetInitializer dataSetInitializer = new DataSetInitializer();
        dataSetInitializer.setCode(DATA_SET_CODE);
        dataSetInitializer.setDataSetTypeCode("MY-TYPE");
        EntityRegistrationDetailsInitializer regDetailsInit =
                new EntityRegistrationDetailsInitializer();
        dataSetInitializer.setRegistrationDetails(new EntityRegistrationDetails(regDetailsInit));
        dataSetInitializer.setExperimentIdentifier("/A/B/C");
        dataSetInitializer.setSampleIdentifierOrNull("/A/B");
        metadata = new ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet(dataSetInitializer);
        model = new Model(facadeFactory);
        model.addCredentials(CREDENTIALS_NAME, new Credentials(CREDENTIALS_NAME, USER2, MY_PASSWORD2));
        dataSet = new DataSet(facade, dssComponent, metadata, dataSetDss);
        context.checking(new Expectations()
            {
                {
                    allowing(dataSetDss).getCode();
                    will(returnValue(DATA_SET_CODE));

                    allowing(facade).getDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));
                }
            });
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadingAndSavingSettingsWithCredentials() throws InvalidSettingsException
    {
        prepareLoadSaveStringSetting(AbstractOpenBisNodeModel.URL_KEY, URL);
        prepareLoadSaveStringSetting(AbstractOpenBisNodeModel.CREDENTIALS_KEY, CREDENTIALS_NAME, "");
        prepareLoadSaveStringSetting(DataSetFileImportNodeModel.DATA_SET_CODE_KEY, DATA_SET_CODE);
        prepareLoadSaveStringSetting(DataSetFileImportNodeModel.FILE_PATH_KEY, FILE_PATH);
        prepareLoadSaveStringSetting(DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, workingDirectory.getAbsolutePath());
        prepareSaveStringSetting(DataSetFileImportNodeModel.ABSOLUTE_FILE_PATH_KEY,
                new File(workingDirectory, DATA_SET_CODE + "/" + FILE_PATH).getAbsolutePath());
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getBoolean(DataSetFileImportNodeModel.REUSE_FILE, false);
                    will(returnValue(true));

                    one(nodeSettingsWO).addBoolean(DataSetFileImportNodeModel.REUSE_FILE, true);
                }
            });

        model.loadValidatedSettingsFrom(nodeSettingsRO);
        model.saveSettingsTo(nodeSettingsWO);

        assertEquals(URL, model.getUrl());
        assertEquals(USER2, model.getUserId());
        assertEquals(MY_PASSWORD2, model.getPassword());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadingAndSavingSettingsWithUserAndPassword() throws InvalidSettingsException
    {
        prepareLoadSaveStringSetting(AbstractOpenBisNodeModel.URL_KEY, URL);
        prepareLoadSaveStringSetting(AbstractOpenBisNodeModel.CREDENTIALS_KEY, "", "");
        prepareLoadSaveStringSetting(AbstractOpenBisNodeModel.USER_KEY, USER);
        prepareLoadSaveStringSetting(AbstractOpenBisNodeModel.PASSWORD_KEY, MY_PASSWORD, "");
        prepareLoadSaveStringSetting(DataSetFileImportNodeModel.DATA_SET_CODE_KEY, DATA_SET_CODE);
        prepareLoadSaveStringSetting(DataSetFileImportNodeModel.FILE_PATH_KEY, FILE_PATH);
        prepareLoadSaveStringSetting(DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, workingDirectory.getAbsolutePath());
        prepareSaveStringSetting(DataSetFileImportNodeModel.ABSOLUTE_FILE_PATH_KEY,
                new File(workingDirectory, DATA_SET_CODE + "/" + FILE_PATH).getAbsolutePath());
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getBoolean(DataSetFileImportNodeModel.REUSE_FILE, false);
                    will(returnValue(true));

                    one(nodeSettingsWO).addBoolean(DataSetFileImportNodeModel.REUSE_FILE, true);
                }
            });

        model.loadValidatedSettingsFrom(nodeSettingsRO);
        model.saveSettingsTo(nodeSettingsWO);

        assertEquals(URL, model.getUrl());
        assertEquals(USER, model.getUserId());
        assertEquals(MY_PASSWORD, model.getPassword());
        context.assertIsSatisfied();
    }

    @Test
    public void testConfigure() throws Exception
    {
        NodeSettings settings = createSettings();
        model.loadValidatedSettingsFrom(settings);

        PortObjectSpec[] portSpecs = model.configure(null);

        assertEquals("[text/plain]", ((URIPortObjectSpec) portSpecs[0])
                .getFileExtensions().toString());
        assertEquals(1, portSpecs.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteReuseFile() throws Exception
    {
        NodeSettings settings = createSettings();
        settings.addBoolean(DataSetFileImportNodeModel.REUSE_FILE, true);
        model.loadValidatedSettingsFrom(settings);
        File file = new File(workingDirectory, DATA_SET_CODE + "/" + FILE_PATH);
        file.getParentFile().mkdirs();
        FileUtilities.writeToFile(file, "hello world");

        PortObject[] portObjects = model.execute(null, null);

        URIPortObject uriPortObject = (URIPortObject) portObjects[0];
        assertEquals("[text/plain]", uriPortObject.getSpec().getFileExtensions().toString());
        List<URIContent> uriContents = uriPortObject.getURIContents();
        assertEquals(file.toURI().toString(), uriContents.get(0).getURI().toString());
        assertEquals(1, uriContents.size());
        assertEquals(1, portObjects.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteReuseFileButFileDoesNotExist() throws Exception
    {
        NodeSettings settings = createSettings();
        settings.addBoolean(DataSetFileImportNodeModel.REUSE_FILE, true);
        model.loadValidatedSettingsFrom(settings);
        File file = new File(workingDirectory, DATA_SET_CODE + "/" + FILE_PATH);
        prepareGetDataSetFile(FILE_PATH, new ByteArrayInputStream("hello world".getBytes()));

        PortObject[] portObjects = model.execute(null, null);

        assertEquals("hello world", FileUtilities.loadToString(file).trim());
        URIPortObject uriPortObject = (URIPortObject) portObjects[0];
        assertEquals("[text/plain]", uriPortObject.getSpec().getFileExtensions().toString());
        List<URIContent> uriContents = uriPortObject.getURIContents();
        assertEquals(file.toURI().toString(), uriContents.get(0).getURI().toString());
        assertEquals(1, uriContents.size());
        assertEquals(1, portObjects.length);
        assertEquals("{openbis.DATA_SET=DS-42, openbis.EXPERIMENT=/A/B/C, openbis.SAMPLE=/A/B}",
                model.flowVariables.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteDoNotReuseFile() throws Exception
    {
        NodeSettings settings = createSettings();
        settings.addBoolean(DataSetFileImportNodeModel.REUSE_FILE, false);
        model.loadValidatedSettingsFrom(settings);
        File file = new File(workingDirectory, DATA_SET_CODE + "/" + FILE_PATH);
        file.getParentFile().mkdirs();
        FileUtilities.writeToFile(file, "hello world");
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 1200; i++)
        {
            builder.append(random.nextInt(5));
        }
        String exampleContent = builder.toString();
        prepareGetDataSetFile(FILE_PATH, new ByteArrayInputStream(exampleContent.getBytes()));

        PortObject[] portObjects = model.execute(null, null);

        assertEquals(exampleContent, FileUtilities.loadToString(file).trim());
        URIPortObject uriPortObject = (URIPortObject) portObjects[0];
        assertEquals("[text/plain]", uriPortObject.getSpec().getFileExtensions().toString());
        List<URIContent> uriContents = uriPortObject.getURIContents();
        assertEquals(file.toURI().toString(), uriContents.get(0).getURI().toString());
        assertEquals(1, uriContents.size());
        assertEquals(1, portObjects.length);
        context.assertIsSatisfied();
    }

    private NodeSettings createSettings()
    {
        NodeSettings settings = new NodeSettings("test");
        settings.addString(AbstractOpenBisNodeModel.URL_KEY, URL);
        settings.addString(AbstractOpenBisNodeModel.USER_KEY, USER);
        settings.addString(AbstractOpenBisNodeModel.PASSWORD_KEY, MY_PASSWORD);
        settings.addString(DataSetFileImportNodeModel.DATA_SET_CODE_KEY, DATA_SET_CODE);
        settings.addString(DataSetFileImportNodeModel.FILE_PATH_KEY, FILE_PATH);
        settings.addString(DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, workingDirectory.getAbsolutePath());
        return settings;
    }

    private void prepareGetDataSetFile(final String filePath, final InputStream inputStream)
    {
        context.checking(new Expectations()
            {
                {
                    one(facadeFactory).createFacade(URL, USER, MY_PASSWORD);
                    will(returnValue(facade));

                    one(dataSetDss).getFile(filePath);
                    will(returnValue(inputStream));

                    one(facade).logout();
                }
            });
    }

    private void prepareLoadSaveStringSetting(final String key, final String value)
            throws InvalidSettingsException
    {
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getString(key);
                    will(returnValue(value));

                    one(nodeSettingsWO).addString(key, value);
                }
            });
    }

    private void prepareSaveStringSetting(final String key, final String value)
            throws InvalidSettingsException
    {
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsWO).addString(key, value);
                }
            });
    }

    private void prepareLoadSaveStringSetting(final String key, final String value,
            final String defaultValue) throws InvalidSettingsException
    {
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getString(key, defaultValue);
                    will(returnValue(value));

                    one(nodeSettingsWO).addString(key, value);
                }
            });
    }

}
