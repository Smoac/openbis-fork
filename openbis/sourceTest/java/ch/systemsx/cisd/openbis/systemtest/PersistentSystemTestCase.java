/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Abstract super class of head-less system tests which makes database changes persistent. Test
 * classes extending this test case class are responsible for cleaning up the database.
 * 
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public abstract class PersistentSystemTestCase extends AbstractTestNGSpringContextTests
{
    protected static final String SESSION_KEY = "session-key";

    protected ICommonServerForInternalUse commonServer;

    protected IGenericServer genericServer;

    protected ICommonClientService commonClientService;

    protected IGenericClientService genericClientService;

    protected IETLLIMSService etlService;

    protected MockHttpServletRequest request;

    protected String systemSessionToken;

    @BeforeSuite
    public void beforeSuite()
    {
        TestInitializer.init();
    }

    @BeforeClass
    public void loginAsSystem()
    {
        systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
    }

    /**
     * Sets a {@link MockHttpServletRequest} for the specified context provider
     */
    @Autowired
    public final void setRequestContextProvider(final SpringRequestContextProvider contextProvider)
    {
        request = new MockHttpServletRequest();
        contextProvider.setRequest(request);
    }

    /**
     * Sets <code>commonServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonServer(final ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    /**
     * Sets <code>genericServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericServer(final IGenericServer genericServer)
    {
        this.genericServer = genericServer;
    }

    /**
     * Sets <code>commonClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonClientService(final ICommonClientService commonClientService)
    {
        this.commonClientService = commonClientService;
    }

    /**
     * Sets <code>genericClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericClientService(final IGenericClientService genericClientService)
    {
        this.genericClientService = genericClientService;
    }

    @Autowired
    public void setETLService(IETLLIMSService etlService)
    {
        this.etlService = etlService;

    }

    protected SessionContext logIntoCommonClientService()
    {
        SessionContext context = commonClientService.tryToLogin("test", "a");
        AssertJUnit.assertNotNull(context);
        return context;
    }

    protected void logOutFromCommonClientService()
    {
        commonClientService.logout(new DisplaySettings(), false);
    }

    protected void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    public final class NewSampleBuilder
    {
        private NewSample sample = new NewSample();

        private List<IEntityProperty> propertis = new ArrayList<IEntityProperty>();

        public NewSampleBuilder(String identifier)
        {
            sample.setIdentifier(identifier);
        }

        public NewSampleBuilder type(String type)
        {
            SampleType sampleType = new SampleType();
            sampleType.setCode(type);
            sample.setSampleType(sampleType);
            return this;
        }

        public NewSampleBuilder experiment(String identifier)
        {
            sample.setExperimentIdentifier(identifier);
            return this;
        }

        public NewSampleBuilder parents(String... parentIdentifiers)
        {
            sample.setParentsOrNull(parentIdentifiers);
            return this;
        }

        public NewSampleBuilder property(String key, String value)
        {
            propertis.add(new PropertyBuilder(key).value(value).getProperty());
            return this;
        }

        public void register()
        {
            sample.setProperties(propertis.toArray(new IEntityProperty[propertis.size()]));
            genericClientService.registerSample(SESSION_KEY, sample);
        }
    }

    /**
     * Register a person with specified user ID.
     * 
     * @return userID
     */
    protected String registerPerson(String userID)
    {
        commonServer.registerPerson(systemSessionToken, userID);
        return userID;
    }

    protected void assignInstanceRole(String userID, RoleCode roleCode)
    {
        commonServer.registerInstanceRole(systemSessionToken, roleCode,
                Grantee.createPerson(userID));
    }

    protected void assignSpaceRole(String userID, RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        commonServer.registerSpaceRole(systemSessionToken, roleCode, spaceIdentifier,
                Grantee.createPerson(userID));
    }

    /**
     * Authenticates as specified user.
     * 
     * @return session token
     */
    protected String authenticateAs(String user)
    {
        return commonServer.tryToAuthenticate(user, "password").getSessionToken();
    }

    protected void uploadFile(String fileName, String fileContent)
    {
        UploadedFilesBean bean = new UploadedFilesBean();
        bean.addMultipartFile(new MockMultipartFile(fileName, fileName, null, fileContent
                .getBytes()));
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_KEY, bean);
    }

}
