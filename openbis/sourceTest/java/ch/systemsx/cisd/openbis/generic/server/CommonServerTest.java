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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.DataStoreServerSessionManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link CommonServer} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonServerTest extends AbstractServerTestCase
{
    private static final String MATERIAL_TYPE_1 = "MATERIAL-TYPE-1";

    private static final String MATERIAL_1 = "MATERIAL-1";

    private static final String SAMPLE_1 = "SAMPLE-1";

    private static final String EXP_1 = "EXP-1";

    private static final String PROJECT_1 = "PROJECT-1";

    private static final String GROUP_1 = "GROUP-1";

    private static final String DATABASE_1 = "DATABASE-1";

    private ICommonBusinessObjectFactory commonBusinessObjectFactory;

    private DataStoreServerSessionManager dssSessionManager;

    private final ICommonServer createServer()
    {
        return new CommonServer(authenticationService, sessionManager, dssSessionManager,
                daoFactory, commonBusinessObjectFactory, new LastModificationState());
    }

    private final static PersonPE createSystemUser()
    {
        final PersonPE systemPerson = new PersonPE();
        systemPerson.setUserId(PersonPE.SYSTEM_USER_ID);
        return systemPerson;
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        dssSessionManager = new DataStoreServerSessionManager();
        commonBusinessObjectFactory = context.mock(ICommonBusinessObjectFactory.class);
    }

    @Test
    public void testLogout()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).closeSession(SESSION_TOKEN);
                }
            });
        createServer().logout(SESSION_TOKEN);

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticateWhichFailed()
    {
        final String user = "user";
        final String password = "password";
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(null));
                }
            });

        assertEquals(null, createServer().tryToAuthenticate(user, password));

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setDatabaseInstance(homeDatabaseInstance);
        roleAssignment.setRegistrator(systemPerson);
        roleAssignment.setRole(RoleCode.ADMIN);
        person.addRoleAssignment(roleAssignment);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson))); // only 'system' in database

                    one(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).createPerson(person);
                    one(personDAO).updatePerson(person);
                }
            });

        final Session s = createServer().tryToAuthenticate(user, password);

        assertEquals(person, s.tryGetPerson());
        assertEquals(roleAssignment, s.tryGetPerson().getRoleAssignments().iterator().next());

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticateButNotFirstUser()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    one(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).createPerson(person);
                }
            });

        final Session s = createServer().tryToAuthenticate(user, password);

        assertEquals(person, s.tryGetPerson());

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    one(personDAO).tryFindPersonByUserId(user);
                    will(returnValue(person));
                }
            });
        assertEquals(null, session.tryGetPerson());

        final Session s = createServer().tryToAuthenticate(user, password);

        assertSame(session, s);
        assertEquals(person, s.tryGetPerson());

        context.assertIsSatisfied();
    }

    @Test
    public void testListGroups()
    {
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        final DatabaseInstanceIdentifier identifier = DatabaseInstanceIdentifier.createHome();
        final GroupPE g1 = CommonTestUtils.createGroup("g1", homeDatabaseInstance);
        final GroupPE g2 = CommonTestUtils.createGroup("g2", homeDatabaseInstance);
        final Session session = createSession();
        session.setPerson(person);
        person.setHomeGroup(g1);
        g1.setId(42L);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));
                    one(groupDAO).listGroups(homeDatabaseInstance);
                    will(returnValue(Arrays.asList(g1, g2)));
                }
            });

        final List<GroupPE> groups = createServer().listGroups(SESSION_TOKEN, identifier);

        assertSame(g1, groups.get(0));
        assertSame(g2, groups.get(1));
        assertEquals(2, groups.size());
        assertEquals(true, g1.isHome().booleanValue());
        assertEquals(false, g2.isHome().booleanValue());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterGroup()
    {
        prepareGetSession();
        final String groupCode = "group";
        final String description = "description";
        final String leader = "leader";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createGroupBO(SESSION);
                    will(returnValue(groupBO));

                    one(groupBO).define(groupCode, description, leader);
                    one(groupBO).save();
                }
            });

        createServer().registerGroup(SESSION_TOKEN, groupCode, description, leader);

        context.assertIsSatisfied();
    }

    @Test
    public void testListPersons()
    {
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(person)));
                }
            });

        final List<PersonPE> persons = createServer().listPersons(SESSION_TOKEN);

        assertSame(person, persons.get(0));
        assertEquals(1, persons.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(null));

                    final String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));

                    one(authenticationService).getPrincipal(applicationToken,
                            CommonTestUtils.USER_ID);
                    will(returnValue(PRINCIPAL));

                    final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
                    one(personDAO).createPerson(person);
                }
            });

        createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExistingPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(CommonTestUtils.createPersonFromPrincipal(PRINCIPAL)));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Person '" + CommonTestUtils.USER_ID + "' already exists.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterUnknownPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(null));

                    final String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));

                    one(authenticationService).getPrincipal(applicationToken,
                            CommonTestUtils.USER_ID);
                    will(throwException(new IllegalArgumentException()));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Person '" + CommonTestUtils.USER_ID
                    + "' unknown by the authentication service.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListRoles()
    {
        prepareGetSession();
        final RoleAssignmentPE role = new RoleAssignmentPE();
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    will(returnValue(Arrays.asList(role)));
                }
            });

        final List<RoleAssignmentPE> roles = createServer().listRoles(SESSION_TOKEN);

        assertSame(role, roles.get(0));
        assertEquals(1, roles.size());

        context.assertIsSatisfied();
    }

    @Test
    public final void testListExternalData()
    {
        final SampleIdentifier sampleIdentifier = CommonTestUtils.createSampleIdentifier();
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadBySampleIdentifier(sampleIdentifier);

                    one(externalDataTable).getExternalData();
                    will(returnValue(Arrays.asList(externalDataPE)));
                }
            });

        List<ExternalDataPE> list =
                createServer().listExternalData(SESSION_TOKEN, sampleIdentifier);

        assertEquals(1, list.size());
        assertSame(externalDataPE, list.get(0));

        context.assertIsSatisfied();
    }

    @Test
    public void testListExternalDataOfAnExperiment()
    {
        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadByExperimentIdentifier(identifier);
                    one(externalDataTable).getExternalData();
                    will(returnValue(Arrays.asList(externalDataPE)));
                }
            });

        List<ExternalDataPE> list = createServer().listExternalData(SESSION_TOKEN, identifier);

        assertEquals(1, list.size());
        assertSame(externalDataPE, list.get(0));

        context.assertIsSatisfied();
    }

    @Test
    public void testListExperiments()
    {
        final ProjectIdentifier projectIdentifier = CommonTestUtils.createProjectIdentifier();
        final ExperimentTypePE experimentType = CommonTestUtils.createExperimentType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExperimentTable(SESSION);
                    will(returnValue(experimentTable));

                    one(experimentTable).load(experimentType.getCode(), projectIdentifier);

                    one(experimentTable).enrichWithProperties();

                    one(experimentTable).getExperiments();
                    will(returnValue(new ArrayList<ExperimentPE>()));
                }
            });
        createServer().listExperiments(SESSION_TOKEN, experimentType, projectIdentifier);
        context.assertIsSatisfied();
    }

    @Test
    public void testListExperimentTypes()
    {
        prepareGetSession();
        final List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        types.add(CommonTestUtils.createExperimentType());
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(types));
                }
            });
        assertEquals(types, createServer().listExperimentTypes(SESSION_TOKEN));
        context.assertIsSatisfied();
    }

    @Test
    public void testListProjects()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    one(projectDAO).listProjects();
                    will(returnValue(new ArrayList<ProjectPE>()));
                }
            });
        createServer().listProjects(SESSION_TOKEN);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPropertyTypes()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createPropertyTypeTable(SESSION);
                    will(returnValue(propertyTypeTable));

                    one(propertyTypeTable).load();
                    one(propertyTypeTable).enrichWithRelations();

                    one(propertyTypeTable).getPropertyTypes();
                    will(returnValue(new ArrayList<PropertyTypePE>()));
                }
            });
        createServer().listPropertyTypes(SESSION_TOKEN);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListDataTypes()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).listDataTypes();
                    will(returnValue(Collections.emptyList()));
                }
            });
        final List<DataTypePE> dataTypes = createServer().listDataTypes(SESSION_TOKEN);
        assertEquals(0, dataTypes.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testListVocabularies()
    {
        prepareGetSession();
        final boolean excludeInternal = true;
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));

                    one(vocabularyDAO).listVocabularies(excludeInternal);
                    will(returnValue(Collections.emptyList()));
                }
            });
        final List<VocabularyPE> vocabularies =
                createServer().listVocabularies(SESSION_TOKEN, true, excludeInternal);
        assertEquals(0, vocabularies.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterPropertyType()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createPropertyTypeBO(SESSION);
                    will(returnValue(propertyTypeBO));

                    one(propertyTypeBO).define(with(aNonNull(PropertyType.class)));
                    one(propertyTypeBO).save();
                }
            });
        createServer().registerPropertyType(SESSION_TOKEN, new PropertyType());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterVocabulary()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(SESSION);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).define(with(aNonNull(Vocabulary.class)));
                    one(vocabularyBO).save();
                }
            });
        createServer().registerVocabulary(SESSION_TOKEN, new Vocabulary());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAssignPropertyType()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        final boolean mandatory = true;
        final String value = "50m";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(SESSION,
                            entityKind);
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).createAssignment(propertyTypeCode,
                            entityTypeCode, mandatory, value);
                }
            });
        assertEquals(
                "Mandatory property type 'USER.DISTANCE' successfully assigned to experiment type 'ARCHERY'",
                createServer().assignPropertyType(SESSION_TOKEN, entityKind, propertyTypeCode,
                        entityTypeCode, mandatory, value));
        context.assertIsSatisfied();
    }

    @Test
    public void testListMaterials()
    {
        prepareGetSession();
        final MaterialTypePE materialType = CommonTestUtils.createMaterialType();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createMaterialTable(SESSION);
                    will(returnValue(materialTable));

                    one(materialTable).load(materialType.getCode());

                    one(materialTable).getMaterials();
                    will(returnValue(new ArrayList<MaterialTypePE>()));
                }
            });
        createServer().listMaterials(SESSION_TOKEN, materialType);
        context.assertIsSatisfied();
    }

    @Test
    public void testListMaterialTypes()
    {
        prepareGetSession();
        final List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        types.add(CommonTestUtils.createMaterialType());
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(types));
                }
            });
        assertEquals(types, createServer().listMaterialTypes(SESSION_TOKEN));
        context.assertIsSatisfied();
    }

    @Test
    public void testEditMaterialNothingChanged() throws Exception
    {
        final MaterialIdentifier identifier = new MaterialIdentifier(MATERIAL_1, MATERIAL_TYPE_1);
        final List<MaterialProperty> properties = new ArrayList<MaterialProperty>();
        prepareGetSession();
        final Date version = new Date();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createMaterialBO(SESSION);
                    will(returnValue(materialBO));

                    one(materialBO).edit(identifier, properties, version);
                    one(materialBO).save();

                }
            });
        createServer().editMaterial(SESSION_TOKEN, identifier, properties, version);
        context.assertIsSatisfied();
    }

    @Test
    public void testEditSampleNothingChanged() throws Exception
    {
        final SampleIdentifier identifier =
                SampleIdentifier.createOwnedBy(new SampleOwnerIdentifier(new GroupIdentifier(
                        DATABASE_1, GROUP_1)), SAMPLE_1);
        final List<SampleProperty> properties = new ArrayList<SampleProperty>();
        prepareGetSession();
        final Date version = new Date();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).edit(identifier, properties, null, version);
                    one(sampleBO).save();

                }
            });
        createServer().editSample(SESSION_TOKEN, identifier, properties, null, version);
        context.assertIsSatisfied();
    }

    @Test
    public void testEditExperimentNothingChanged() throws Exception
    {
        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(DATABASE_1, GROUP_1, PROJECT_1, EXP_1);
        final List<ExperimentProperty> properties = new ArrayList<ExperimentProperty>();
        final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();
        final ProjectIdentifier newProjectIdentifier =
                new ProjectIdentifier(DATABASE_1, GROUP_1, PROJECT_1);
        prepareGetSession();
        final Date version = new Date();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).edit(identifier, properties, attachments,
                            newProjectIdentifier, version);
                    one(experimentBO).save();

                }
            });
        createServer().editExperiment(SESSION_TOKEN, identifier, properties, attachments,
                newProjectIdentifier, version);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteDataSets()
    {
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("a", "b");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadByDataSetCodes(dataSetCodes);
                    one(externalDataTable).deleteLoadedDataSets(dssSessionManager, "reason");
                }
            });

        createServer().deleteDataSets(SESSION_TOKEN, dataSetCodes, "reason");

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSets()
    {
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("a", "b");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadByDataSetCodes(dataSetCodes);
                    one(externalDataTable).uploadLoadedDataSetsToCIFEX(dssSessionManager,
                            "cifexURL", "pwd");
                }
            });

        createServer().uploadDataSets(SESSION_TOKEN, dataSetCodes, "cifexURL", "pwd");

        context.assertIsSatisfied();
    }

}
