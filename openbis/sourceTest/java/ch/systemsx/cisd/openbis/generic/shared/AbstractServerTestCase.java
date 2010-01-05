/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An <i>abstract</i> test infrastructure for {@link IServer} implementations.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractServerTestCase extends AssertJUnit
{
    protected static final Principal PRINCIPAL =
            new Principal(CommonTestUtils.USER_ID, "john", "doe", "j@d");

    protected static final String SESSION_TOKEN = "session-token";

    protected static final Session SESSION =
            new Session(CommonTestUtils.USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 1);

    protected BufferedAppender logRecorder;

    protected Mockery context;

    protected IDAOFactory daoFactory;

    protected IAuthenticationService authenticationService;

    protected ISessionManager<Session> sessionManager;

    protected IDatabaseInstanceDAO databaseInstanceDAO;

    protected IPersonDAO personDAO;

    protected IRoleAssignmentDAO roleAssignmentDAO;

    protected DatabaseInstancePE homeDatabaseInstance;

    protected IGroupDAO groupDAO;

    protected IExternalDataDAO externalDataDAO;

    protected IPermIdDAO permIdDAO;

    protected ISampleDAO sampleDAO;

    protected IGroupBO groupBO;

    protected ISampleBO sampleBO;

    protected IMaterialBO materialBO;

    protected IExternalDataTable externalDataTable;

    protected IExperimentTable experimentTable;

    protected IMaterialTable materialTable;

    protected IMaterialLister materialLister;

    protected IEntityTypeDAO entityTypeDAO;

    protected IProjectDAO projectDAO;

    protected IExperimentBO experimentBO;

    protected ISampleTypeDAO sampleTypeDAO;

    protected IPropertyTypeTable propertyTypeTable;

    protected IPropertyTypeDAO propertyTypeDAO;

    protected IPropertyTypeBO propertyTypeBO;

    protected IVocabularyDAO vocabularyDAO;

    protected IVocabularyBO vocabularyBO;

    protected IEntityTypePropertyTypeBO entityTypePropertyTypeBO;

    protected IAttachmentDAO experimentAttachmentDAO;

    protected IExternalDataBO externalDataBO;

    protected IEntityTypeBO entityTypeBO;

    protected IFileFormatTypeDAO fileFormatDAO;

    protected IDataSetTypeDAO dataSetTypeDAO;

    protected IExperimentDAO experimentDAO;

    protected IDatasetLister datasetLister;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%m%n", Level.DEBUG);
        context = new Mockery();
        authenticationService = context.mock(IAuthenticationService.class);
        sessionManager = context.mock(ISessionManager.class);
        // DAO
        daoFactory = context.mock(IDAOFactory.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        personDAO = context.mock(IPersonDAO.class);
        groupDAO = context.mock(IGroupDAO.class);
        sampleDAO = context.mock(ISampleDAO.class);
        roleAssignmentDAO = context.mock(IRoleAssignmentDAO.class);
        externalDataDAO = context.mock(IExternalDataDAO.class);
        permIdDAO = context.mock(IPermIdDAO.class);
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
        experimentDAO = context.mock(IExperimentDAO.class);
        experimentAttachmentDAO = context.mock(IAttachmentDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
        sampleTypeDAO = context.mock(ISampleTypeDAO.class);
        propertyTypeDAO = context.mock(IPropertyTypeDAO.class);
        fileFormatDAO = context.mock(IFileFormatTypeDAO.class);
        dataSetTypeDAO = context.mock(IDataSetTypeDAO.class);
        vocabularyDAO = context.mock(IVocabularyDAO.class);
        // BO
        groupBO = context.mock(IGroupBO.class);
        entityTypeBO = context.mock(IEntityTypeBO.class);
        sampleBO = context.mock(ISampleBO.class);
        materialBO = context.mock(IMaterialBO.class);
        experimentBO = context.mock(IExperimentBO.class);
        propertyTypeBO = context.mock(IPropertyTypeBO.class);
        vocabularyBO = context.mock(IVocabularyBO.class);
        entityTypePropertyTypeBO = context.mock(IEntityTypePropertyTypeBO.class);
        externalDataBO = context.mock(IExternalDataBO.class);
        // Table
        externalDataTable = context.mock(IExternalDataTable.class);
        datasetLister = context.mock(IDatasetLister.class);
        experimentTable = context.mock(IExperimentTable.class);
        propertyTypeTable = context.mock(IPropertyTypeTable.class);
        materialTable = context.mock(IMaterialTable.class);
        materialLister = context.mock(IMaterialLister.class);

        homeDatabaseInstance = CommonTestUtils.createHomeDatabaseInstance();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(homeDatabaseInstance));
                    allowing(daoFactory).getDatabaseInstanceDAO();
                    will(returnValue(databaseInstanceDAO));
                    allowing(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));
                    allowing(daoFactory).getGroupDAO();
                    will(returnValue(groupDAO));
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));
                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));
                    allowing(daoFactory).getRoleAssignmentDAO();
                    will(returnValue(roleAssignmentDAO));
                    allowing(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));
                    allowing(daoFactory).getExternalDataDAO();
                    will(returnValue(externalDataDAO));
                    allowing(daoFactory).getAttachmentDAO();
                    will(returnValue(experimentAttachmentDAO));
                    allowing(daoFactory).getFileFormatTypeDAO();
                    will(returnValue(fileFormatDAO));
                    allowing(daoFactory).getDataSetTypeDAO();
                    will(returnValue(dataSetTypeDAO));
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    protected Session createSession()
    {
        return new Session(CommonTestUtils.USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 2);
    }

    protected void prepareGetSession()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(SESSION));
                }
            });
    }

    static final protected ExperimentPE createExperiment(final String experimentTypeCode,
            final String experimentCode, final String groupCode)
    {
        final ExperimentPE experimentPE = new ExperimentPE();
        experimentPE.setCode(experimentCode);
        final ProjectPE projectPE = createProject(groupCode);
        experimentPE.setProject(projectPE);
        final ExperimentTypePE experimentTypePE = new ExperimentTypePE();
        experimentTypePE.setCode(experimentTypeCode);
        experimentPE.setExperimentType(experimentTypePE);
        return experimentPE;
    }

    protected static ProjectPE createProject(final String groupCode)
    {
        final ProjectPE projectPE = new ProjectPE();
        final GroupPE groupPE = createGroup(groupCode);
        projectPE.setGroup(groupPE);
        projectPE.setCode("P");
        return projectPE;
    }

    protected static GroupPE createGroup(final String groupCode)
    {
        final GroupPE groupPE = new GroupPE();
        groupPE.setCode(groupCode);
        DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("DB");
        groupPE.setDatabaseInstance(databaseInstancePE);
        return groupPE;
    }

    static protected String createSampleIdentifier(final String groupCode, String sample1Code)
    {
        return "/" + groupCode + "/" + sample1Code;
    }

    static protected NewExperiment createNewExperiment(final String experimentTypeCode,
            final String experimentCode, final String groupCode, final String[] samples)
    {
        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setExperimentTypeCode(experimentTypeCode);
        newExperiment.setIdentifier("/" + groupCode + "/PROJ/" + experimentCode);
        newExperiment.setSamples(samples);
        return newExperiment;
    }
}
