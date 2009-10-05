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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomFilterDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;

/**
 * An <i>abstract</i> test for <i>Business Object</i>.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractBOTest extends AssertJUnit
{
    Mockery context;

    IDAOFactory daoFactory;

    IGroupDAO groupDAO;

    IExperimentDAO experimentDAO;

    IProjectDAO projectDAO;

    IEntityTypeDAO entityTypeDAO;

    IMaterialDAO materialDAO;

    IExternalDataDAO externalDataDAO;

    IDatabaseInstanceDAO databaseInstanceDAO;

    ISampleDAO sampleDAO;

    IEntityPropertyTypeDAO entityPropertyTypeDAO;

    IPropertyTypeDAO propertyTypeDAO;

    IPersonDAO personDAO;

    ISampleTypeDAO sampleTypeDAO;

    IVocabularyDAO vocabularyDAO;

    IVocabularyTermDAO vocabularyTermDAO;

    IEntityPropertiesConverter propertiesConverter;

    IDataSetTypeDAO dataSetTypeDAO;

    IFileFormatTypeDAO fileFormatTypeDAO;

    ILocatorTypeDAO locatorTypeDAO;

    IDataStoreDAO dataStoreDAO;

    IPermIdDAO permIdDAO;

    IEventDAO eventDAO;

    IAuthorizationGroupDAO authorizationGroupDAO;

    IGridCustomFilterDAO filterDAO;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        groupDAO = context.mock(IGroupDAO.class);
        experimentDAO = context.mock(IExperimentDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
        sampleDAO = context.mock(ISampleDAO.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        externalDataDAO = context.mock(IExternalDataDAO.class);
        personDAO = context.mock(IPersonDAO.class);
        propertiesConverter = context.mock(IEntityPropertiesConverter.class);
        sampleTypeDAO = context.mock(ISampleTypeDAO.class);
        propertyTypeDAO = context.mock(IPropertyTypeDAO.class);
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);
        vocabularyDAO = context.mock(IVocabularyDAO.class);
        vocabularyTermDAO = context.mock(IVocabularyTermDAO.class);
        materialDAO = context.mock(IMaterialDAO.class);
        dataSetTypeDAO = context.mock(IDataSetTypeDAO.class);
        fileFormatTypeDAO = context.mock(IFileFormatTypeDAO.class);
        locatorTypeDAO = context.mock(ILocatorTypeDAO.class);
        dataStoreDAO = context.mock(IDataStoreDAO.class);
        permIdDAO = context.mock(IPermIdDAO.class);
        eventDAO = context.mock(IEventDAO.class);
        authorizationGroupDAO = context.mock(IAuthorizationGroupDAO.class);
        filterDAO = context.mock(IGridCustomFilterDAO.class);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getDatabaseInstanceDAO();
                    will(returnValue(databaseInstanceDAO));
                    allowing(daoFactory).getGroupDAO();
                    will(returnValue(groupDAO));
                    allowing(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));
                    allowing(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));
                    allowing(daoFactory).getDataSetTypeDAO();
                    will(returnValue(dataSetTypeDAO));
                    allowing(daoFactory).getFileFormatTypeDAO();
                    will(returnValue(fileFormatTypeDAO));
                    allowing(daoFactory).getLocatorTypeDAO();
                    will(returnValue(locatorTypeDAO));
                    allowing(daoFactory).getExternalDataDAO();
                    will(returnValue(externalDataDAO));
                    allowing(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));
                    allowing(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));
                    allowing(daoFactory).getVocabularyTermDAO();
                    will(returnValue(vocabularyTermDAO));
                    allowing(daoFactory).getEventDAO();
                    will(returnValue(eventDAO));
                    allowing(daoFactory).getAuthorizationGroupDAO();
                    will(returnValue(authorizationGroupDAO));
                    allowing(daoFactory).getGridCustomFilterDAO();
                    will(returnValue(filterDAO));
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
}
