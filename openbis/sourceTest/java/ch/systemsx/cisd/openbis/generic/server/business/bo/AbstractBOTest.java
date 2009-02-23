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

import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;

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

    IEntityPropertiesConverter propertiesConverter;

    IProcedureDAO procedureDAO;

    IProcedureTypeDAO procedureTypeDAO;

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
        procedureDAO = context.mock(IProcedureDAO.class);
        procedureTypeDAO = context.mock(IProcedureTypeDAO.class);
        materialDAO = context.mock(IMaterialDAO.class);
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
}
