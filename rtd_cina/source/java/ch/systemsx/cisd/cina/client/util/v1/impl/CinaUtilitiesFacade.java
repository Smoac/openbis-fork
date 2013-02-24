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

package ch.systemsx.cisd.cina.client.util.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.DssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.DssServiceRpcFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaUtilitiesFacade implements ICinaUtilities
{
    /**
     * Public factory method for creating a CinaUtilitiesFacade with a username and password.
     * 
     * @param user The user name
     * @param password The user's password
     * @param openBISUrl The URL to openBIS
     */
    public static CinaUtilitiesFacade tryCreate(String user, String password, String openBISUrl,
            long timeoutInMillis)
    {
        CinaUtilitiesFacade component = tryCreate(null, openBISUrl, timeoutInMillis);
        try
        {
            component.login(user, password, timeoutInMillis);
        } catch (AuthorizationFailureException e)
        {
            // User name / Password is incorrect.
            return null;
        }
        return component;
    }

    /**
     * Public factory method for creating a DssComponent for a user that has already been
     * authenticated.
     * 
     * @param sessionToken The session token provided by authentication
     * @param openBISUrl The URL to openBIS
     */
    public static CinaUtilitiesFacade tryCreate(String sessionToken, String openBISUrl,
            long timeoutInMillis)
    {
        IGeneralInformationService generalInfoService =
                createGeneralInformationService(openBISUrl, timeoutInMillis);
        IServiceForDataStoreServer openbisService = createOpenBisService(openBISUrl, timeoutInMillis);
        IDssComponent dssComponent =
                createDssComponent(openbisService, generalInfoService, sessionToken,
                        timeoutInMillis);
        return new CinaUtilitiesFacade(generalInfoService, openbisService, dssComponent,
                sessionToken);
    }

    private static IGeneralInformationService createGeneralInformationService(String openBISURL,
            long timeoutInMillis)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        openBISURL, timeoutInMillis);
        return service;
    }

    private static IServiceForDataStoreServer createOpenBisService(String openBISURL, long timeoutInMillis)
    {
        return new OpenBisServiceFactory(openBISURL, ResourceNames.ETL_SERVICE_URL)
                .createService(timeoutInMillis);
    }

    /**
     * Create a DSS component that connects to the openBIS instance specified by the URL.
     * <p>
     * The DSS component needs to connect to openBIS to find out which DSS manages a given data set.
     * Once it has a connection to openBIS, it can figure out how to connect to DSS servers itself.
     */
    private static IDssComponent createDssComponent(IServiceForDataStoreServer openbisService,
            IGeneralInformationService generalInformationService, String sessionTokenOrNull,
            long timeoutInMillis)
    {
        return new DssComponent(generalInformationService,
                new DssServiceRpcFactory(timeoutInMillis), sessionTokenOrNull);
    }

    /** The interface for accessing the remote services. */
    private final IGeneralInformationService generalInformationService;

    /** The LIMS service. */
    private final IServiceForDataStoreServer openbisService;

    /** The current state of the facade */
    private AbstractCinaFacadeState state;

    /**
     * Internal constructor, also used for testing.
     * 
     * @param generalInformationService A proxy to the openBIS application server's general
     *            information service
     * @param openbisService A proxy to the openBIS application server's ETLLIMS Service
     * @param dssComponent A dss component facade for interacting with dss services
     * @param sessionTokenOrNull A session token, if the user has already logged in, or null
     *            otherwise.
     */
    protected CinaUtilitiesFacade(IGeneralInformationService generalInformationService,
            IServiceForDataStoreServer openbisService, IDssComponent dssComponent, String sessionTokenOrNull)

    {
        this.generalInformationService = generalInformationService;
        this.openbisService = openbisService;
        if (sessionTokenOrNull == null)
        {
            this.state = new UnauthenticatedState(generalInformationService);
        } else
        {
            this.state =
                    new AuthenticatedState(generalInformationService, openbisService, dssComponent,
                            sessionTokenOrNull);
        }
    }

    /**
     * FOR TESTING ONLY <br>
     * This method makes it possible to hand in a mocked dssComponent -- it should only be used for
     * testing and it therefore marked deprecated.
     * 
     * @deprecated
     */
    @Deprecated
    void loginForTesting(String user, String password, IDssComponent dssComponent)
            throws AuthorizationFailureException, EnvironmentFailureException
    {
        // login and transition to the authenticated state
        state.login(user, password);
        state =
                new AuthenticatedState(generalInformationService, openbisService, dssComponent,
                        state.getSessionToken());
    }

    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     * 
     * @throws AuthorizationFailureException Thrown if the username / password do not authenticate.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    void login(String user, String password, long timeoutInMillis)
            throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        // login and transition to the authenticated state
        state.login(user, password);
        state =
                new AuthenticatedState(generalInformationService, openbisService,
                        createDssComponent(openbisService, generalInformationService,
                                state.getSessionToken(), timeoutInMillis), state.getSessionToken());
    }

    @Override
    public void logout()
    {
        // logout and transition to the unauthenticated state
        state.logout();
        state = new UnauthenticatedState(generalInformationService);
    }

    @Override
    public String getSessionToken() throws IllegalStateException
    {
        return state.getSessionToken();
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException
    {
        return state.searchForSamples(searchCriteria);
    }

    @Override
    public String generateSampleCode(String sampleTypeCode) throws IllegalStateException,
            EnvironmentFailureException
    {
        return state.generateSampleCode(sampleTypeCode);
    }

    @Override
    public List<Experiment> listVisibleExperiments(String experimentType)
            throws IllegalStateException, EnvironmentFailureException
    {
        return state.listVisibleExperiments(experimentType);
    }

    @Override
    public List<DataSet> listDataSets(List<Sample> samples) throws IllegalStateException,
            EnvironmentFailureException
    {
        return state.listDataSets(samples);
    }

    @Override
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException
    {
        return state.getDataSet(dataSetCode);
    }

    @Override
    public List<DataSet> listDataSetsForSample(Sample sample,
            boolean areOnlyDirectlyConnectedIncluded) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException
    {
        return state.listDataSetsForSample(sample, areOnlyDirectlyConnectedIncluded);
    }
}

/**
 * Superclass for component states, which make the state machine of the Cina facade explicit.
 * <p>
 * By default, all methods just throw an exception. Subclasses should override the methods they
 * accept.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractCinaFacadeState implements ICinaUtilities
{
    protected final IGeneralInformationService service;

    AbstractCinaFacadeState(IGeneralInformationService service)
    {
        this.service = service;
    }

    public void checkSession() throws IllegalStateException
    {
        throw new IllegalStateException("Please log in");
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    @Override
    public String generateSampleCode(String sampleTypeCode) throws IllegalStateException,
            EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    @Override
    public List<Experiment> listVisibleExperiments(String experimentType)
            throws IllegalStateException, EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    @Override
    public List<DataSet> listDataSets(List<Sample> samples) throws IllegalStateException,
            EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    @Override
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    @Override
    public List<DataSet> listDataSetsForSample(Sample sample,
            boolean areOnlyDirectlyConnectedIncluded) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     * 
     * @throws AuthorizationFailureException Thrown if the username / password do not authenticate.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    abstract void login(final String user, final String password)
            throws AuthorizationFailureException, EnvironmentFailureException;

    /**
     * Package visible method used to transfer context information between states.
     */
    @Override
    public abstract String getSessionToken();
}

/**
 * An object representing an unauthenticated state. Being in this state implies that the user has
 * not yet logged in. Only login and logout are allowed in this state.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class UnauthenticatedState extends AbstractCinaFacadeState
{
    private String sessionTokenOrNull;

    UnauthenticatedState(IGeneralInformationService service)
    {
        super(service);
    }

    @Override
    public String getSessionToken()
    {
        if (sessionTokenOrNull == null)
            throw new IllegalStateException("Please log in");
        return sessionTokenOrNull;
    }

    @Override
    void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        try
        {
            sessionTokenOrNull = service.tryToAuthenticateForAllServices(user, password);
        } catch (RemoteConnectFailureException e)
        {
            throw new EnvironmentFailureException("Could not connect to server", e);
        }
        if (sessionTokenOrNull == null)
            throw new AuthorizationFailureException("Login or Password invalid");
    }

    @Override
    public void logout()
    {
        return;
    }
}

/**
 * An object representing an authenticated state. Being in this state means that the user has logged
 * in and all operations are available, except login.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class AuthenticatedState extends AbstractCinaFacadeState
{
    private final String sessionToken;

    private final IServiceForDataStoreServer openbisService;

    private final IDssComponent dssComponent;

    private final int generalInformationServiceMinorVersion;

    /**
     * @param service
     */
    AuthenticatedState(IGeneralInformationService service, IServiceForDataStoreServer openbisService,
            IDssComponent dssComponent, String sessionToken)
    {
        super(service);
        this.sessionToken = sessionToken;
        this.openbisService = openbisService;
        this.dssComponent = dssComponent;
        this.generalInformationServiceMinorVersion = service.getMinorVersion();
    }

    @Override
    void login(String user, String password) throws AuthorizationFailureException
    {
        throw new IllegalStateException("Already logged in.");
    }

    @Override
    public void logout()
    {
        service.logout(getSessionToken());
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException
    {
        // This functionality has only been supported since version 1.1
        int minorVersion = generalInformationServiceMinorVersion;
        if (minorVersion < 1)
        {
            throw new EnvironmentFailureException("Server does not support this feature.");
        }

        return service.searchForSamples(sessionToken, searchCriteria);
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    @Override
    public String generateSampleCode(String sampleTypeCode) throws IllegalStateException,
            EnvironmentFailureException
    {
        SampleType replicaSampleType = openbisService.getSampleType(sessionToken, sampleTypeCode);
        long sampleCodeSuffix = openbisService.drawANewUniqueID(sessionToken);
        String sampleCode =
                String.format("%s%d", replicaSampleType.getGeneratedCodePrefix(), sampleCodeSuffix);
        return sampleCode;
    }

    @Override
    public List<Experiment> listVisibleExperiments(String experimentType)
            throws IllegalStateException, EnvironmentFailureException
    {
        // This functionality has only been supported since version 1.2
        int minorVersion = generalInformationServiceMinorVersion;
        if (minorVersion < 2)
        {
            throw new EnvironmentFailureException("Server does not support this feature.");
        }

        // First get a list of spaces the user has access to
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                service.listSpacesWithProjectsAndRoleAssignments(sessionToken, null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            projects.addAll(space.getProjects());
        }

        // Then get the experiments for these spaces
        return service.listExperiments(sessionToken, projects, experimentType);
    }

    @Override
    public List<DataSet> listDataSets(List<Sample> samples) throws IllegalStateException,
            EnvironmentFailureException
    {
        // This functionality has only been supported since version 1.1
        int minorVersion = generalInformationServiceMinorVersion;
        if (minorVersion < 1)
        {
            throw new EnvironmentFailureException("Server does not support this feature.");
        }

        return service.listDataSets(sessionToken, samples);
    }

    @Override
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException
    {
        return dssComponent.getDataSet(dataSetCode);
    }

    @Override
    public List<DataSet> listDataSetsForSample(Sample sample,
            boolean areOnlyDirectlyConnectedIncluded) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException
    {
        // This functionality has only been supported since version 1.1
        int minorVersion = generalInformationServiceMinorVersion;
        if (minorVersion < 3)
        {
            throw new EnvironmentFailureException("Server does not support this feature.");
        }

        return service
                .listDataSetsForSample(sessionToken, sample, areOnlyDirectlyConnectedIncluded);
    }

}