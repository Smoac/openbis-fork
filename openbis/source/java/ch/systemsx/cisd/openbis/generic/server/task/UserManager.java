/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.UpdateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.DeleteRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class UserManager
{
    private static final String GLOBAL_AUTHORIZATION_GROUP_CODE = "ALL_GROUPS";

    private static final AuthorizationGroupPermId GLOBAL_AUTHORIZATION_GROUP_ID = new AuthorizationGroupPermId(GLOBAL_AUTHORIZATION_GROUP_CODE);

    private final IAuthenticationService authenticationService;

    private final IApplicationServerInternalApi service;

    private final ISimpleLogger logger;

    private final UserManagerReport report;

    private final Map<String, UserInfo> userInfosByUserId = new TreeMap<>();

    private final Map<String, Map<String, Principal>> usersByGroupCode = new LinkedHashMap<>();

    private final List<String> groupCodes = new ArrayList<>();

    private List<String> globalSpaces = new ArrayList<>();

    private Map<Role, List<String>> commonSpacesByRole = new HashMap<>();

    private Map<String, String> commonSamples = new HashMap<>();

    private Map<String, String> commonExperiments;

    private Map<String, HomeSpaceRequest> requestedHomeSpaceByUserId = new TreeMap<>();

    private File shareIdsMappingFileOrNull;

    private List<MappingAttributes> mappingAttributesList = new ArrayList<>();

    private boolean deactivateUnknownUsers;

    public UserManager(IAuthenticationService authenticationService, IApplicationServerInternalApi service,
            File shareIdsMappingFileOrNull, ISimpleLogger logger, UserManagerReport report)
    {
        this.authenticationService = authenticationService;
        this.service = service;
        this.shareIdsMappingFileOrNull = shareIdsMappingFileOrNull;
        this.logger = logger;
        this.report = report;
    }

    public void setGlobalSpaces(List<String> globalSpaces)
    {
        this.globalSpaces = globalSpaces;
    }

    public void setCommon(Map<Role, List<String>> commonSpacesByRole, Map<String, String> commonSamples,
            Map<String, String> commonExperiments)
    {
        this.commonSpacesByRole = commonSpacesByRole;
        this.commonSamples = commonSamples;
        this.commonExperiments = commonExperiments;
        Set<String> commonSpaces = new HashSet<>();
        commonSpacesByRole.values().forEach(spaces -> commonSpaces.addAll(spaces));
        checkIdentifierTemplates(commonSamples, commonSpaces, "sample", "<common space code>/<common sample code>");
        checkIdentifierTemplates(commonExperiments, commonSpaces, "experiment",
                "<common space code>/<common project code>/<common experiment code>");
    }

    private void checkIdentifierTemplates(Map<String, String> commonEntities, Set<String> commonSpaces,
            String entityKind, String templateSchema)
    {
        for (String identifierTemplate : commonEntities.keySet())
        {
            String[] parts = identifierTemplate.split("/");
            if (commonSpaces.contains(parts[0]) == false)
            {
                throw createConfigException(identifierTemplate, templateSchema, "No common space for common " + entityKind);
            }
            if (parts.length != templateSchema.split("/").length)
            {
                throw createConfigException(identifierTemplate, templateSchema, "");
            }
        }
    }

    private ConfigurationFailureException createConfigException(String identifierTemplate, String templateSchema, String message)
    {
        return new ConfigurationFailureException("Identifier template '" + identifierTemplate + "' is invalid"
                + (StringUtils.isBlank(message) ? ". " : " (reason: " + message + "). ") + "Template schema: " + templateSchema);
    }

    public void setDeactivateUnknwonUsers(boolean deactivateUnknownUsers)
    {
        this.deactivateUnknownUsers = deactivateUnknownUsers;
    }

    public void addGroup(UserGroup group, Map<String, Principal> principalsByUserId)
    {
        String groupCode = group.getKey().toUpperCase();
        usersByGroupCode.put(groupCode, group.isEnabled() ? principalsByUserId : new HashMap<>());
        groupCodes.add(groupCode);
        mappingAttributesList.add(new MappingAttributes(groupCode, group.getShareIds()));
        Set<String> admins = asSet(group.getAdmins());
        if (group.isEnabled())
        {
            for (Principal principal : principalsByUserId.values())
            {
                String userId = principal.getUserId();
                UserInfo userInfo = userInfosByUserId.get(userId);
                if (userInfo == null)
                {
                    userInfo = new UserInfo(principal);
                    userInfosByUserId.put(userId, userInfo);
                }
                userInfo.addGroupInfo(new GroupInfo(groupCode, admins.contains(userId)));
            }
        }
        logger.log(LogLevel.INFO, principalsByUserId.size() + " users for " + (group.isEnabled() ? "" : "disabled ") + "group " + groupCode);
    }

    public void manage()
    {
        try
        {
            String sessionToken = service.loginAsSystem();

            updateMappingFile();
            manageGlobalSpaces(sessionToken, report);
            if (deactivateUnknownUsers)
            {
                revokeUsersUnkownByAuthenticationService(sessionToken, report);
            }
            CurrentState currentState = loadCurrentState(sessionToken, service);
            for (Entry<String, Map<String, Principal>> entry : usersByGroupCode.entrySet())
            {
                String groupCode = entry.getKey();
                Map<String, Principal> users = entry.getValue();
                manageGroup(sessionToken, groupCode, users, currentState, report);
            }
            updateHomeSpaces(sessionToken, currentState, report);

            service.logout(sessionToken);
        } catch (Throwable e)
        {
            report.addErrorMessage("Error: " + e.toString());
            logger.log(LogLevel.ERROR, "", e);
        }
    }

    private void updateMappingFile()
    {
        if (shareIdsMappingFileOrNull != null && mappingAttributesList.isEmpty() == false)
        {
            File parentFile = shareIdsMappingFileOrNull.getParentFile();
            parentFile.mkdirs();
            File newFile = new File(parentFile, shareIdsMappingFileOrNull.getName() + ".new");
            PrintWriter printWriter = null;
            try
            {
                printWriter = new PrintWriter(newFile);
                printWriter.println("Identifier\tShare IDs\tArchive Folder");
                for (MappingAttributes attributes : mappingAttributesList)
                {
                    CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
                    List<String> shareIds = attributes.getShareIds();
                    if (shareIds != null && shareIds.isEmpty() == false)
                    {
                        shareIds.forEach(id -> builder.append(id));
                        printWriter.println(String.format("/%s_.*\t%s\t", attributes.getGroupCode(), builder.toString()));
                    }
                }
            } catch (IOException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            } finally
            {
                IOUtils.closeQuietly(printWriter);
            }
            newFile.renameTo(shareIdsMappingFileOrNull);
        }
    }

    private void updateHomeSpaces(String sessionToken, CurrentState currentState, UserManagerReport report)
    {
        List<PersonUpdate> updates = new ArrayList<>();
        for (Entry<String, HomeSpaceRequest> entry : requestedHomeSpaceByUserId.entrySet())
        {
            String userId = entry.getKey();
            HomeSpaceRequest request = entry.getValue();
            Person knownUser = currentState.getUser(userId);
            SpacePermId requestedHomeSpace = request.getHomeSpace();
            if (knownUser == null || knownUser.getSpace() == null)
            {
                if (requestedHomeSpace != null)
                {
                    updates.add(createPersonUpdate(userId, requestedHomeSpace, report));
                }
            } else if (request.shouldCurrentBeRemoved())
            {
                updates.add(createPersonUpdate(userId, requestedHomeSpace, report));
            }
        }
        if (updates.isEmpty() == false)
        {
            service.updatePersons(sessionToken, updates);
        }
    }

    private PersonUpdate createPersonUpdate(String userId, SpacePermId spacePermId, UserManagerReport report)
    {
        IPersonId personId = new PersonPermId(userId);
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(spacePermId);
        report.assignHomeSpace(userId, spacePermId);
        return personUpdate;
    }

    private void manageGlobalSpaces(String sessionToken, UserManagerReport report)
    {
        if (globalSpaces != null && globalSpaces.isEmpty() == false)
        {
            createGlobalSpaces(sessionToken, report);
            Set<String> knownGlobalSpaces = createGlobalGroupAndGetKnownSpaces(sessionToken, GLOBAL_AUTHORIZATION_GROUP_ID, report);
            createGlobalRoleAssignments(sessionToken, GLOBAL_AUTHORIZATION_GROUP_ID, knownGlobalSpaces, report);
        }
    }

    private void createGlobalSpaces(String sessionToken, UserManagerReport report)
    {
        List<SpacePermId> spaceIds = globalSpaces.stream().map(SpacePermId::new).collect(Collectors.toList());
        Set<ISpaceId> knownSpaces = service.getSpaces(sessionToken, spaceIds, new SpaceFetchOptions()).keySet();
        List<SpaceCreation> spaceCreations = new ArrayList<>();
        for (SpacePermId spaceId : spaceIds)
        {
            if (knownSpaces.contains(spaceId) == false)
            {
                SpaceCreation spaceCreation = new SpaceCreation();
                spaceCreation.setCode(spaceId.getPermId());
                spaceCreations.add(spaceCreation);
            }
        }
        if (spaceCreations.isEmpty() == false)
        {
            service.createSpaces(sessionToken, spaceCreations);
            report.addSpaces(spaceCreations);
        }
    }

    private Set<String> createGlobalGroupAndGetKnownSpaces(String sessionToken, AuthorizationGroupPermId groupId, UserManagerReport report)
    {
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        AuthorizationGroup group = service.getAuthorizationGroups(sessionToken, Arrays.asList(groupId), fetchOptions).get(groupId);
        Set<String> knownGlobalSpaces = new TreeSet<>();
        if (group == null)
        {
            AuthorizationGroupCreation groupCreation = new AuthorizationGroupCreation();
            groupCreation.setCode(GLOBAL_AUTHORIZATION_GROUP_CODE);
            groupCreation.setDescription("Authorization group for all users of all groups");
            service.createAuthorizationGroups(sessionToken, Arrays.asList(groupCreation));
            report.addGroup(GLOBAL_AUTHORIZATION_GROUP_CODE);
        } else
        {
            for (RoleAssignment roleAssignment : group.getRoleAssignments())
            {
                if (RoleLevel.SPACE.equals(roleAssignment.getRoleLevel()) && Role.OBSERVER.equals(roleAssignment.getRole()))
                {
                    knownGlobalSpaces.add(roleAssignment.getSpace().getCode());
                }
            }
        }
        return knownGlobalSpaces;
    }

    private void createGlobalRoleAssignments(String sessionToken, AuthorizationGroupPermId groupId, Set<String> knownGlobalSpaces,
            UserManagerReport report)
    {
        List<RoleAssignmentCreation> assignmentCreations = new ArrayList<>();
        for (String spaceCode : globalSpaces)
        {
            if (knownGlobalSpaces.contains(spaceCode) == false)
            {
                RoleAssignmentCreation assignmentCreation = new RoleAssignmentCreation();
                assignmentCreation.setAuthorizationGroupId(groupId);
                assignmentCreation.setRole(Role.OBSERVER);
                SpacePermId spaceId = new SpacePermId(spaceCode);
                assignmentCreation.setSpaceId(spaceId);
                assignmentCreations.add(assignmentCreation);
                report.assignRoleTo(groupId, assignmentCreation.getRole(), spaceId);
            }
        }
        if (assignmentCreations.isEmpty() == false)
        {
            service.createRoleAssignments(sessionToken, assignmentCreations);
        }
    }

    private void revokeUsersUnkownByAuthenticationService(String sessionToken, UserManagerReport report)
    {
        List<PersonUpdate> updates = new ArrayList<>();
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRegistrator();
        List<Person> persons = service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
        for (Person person : persons)
        {
            if (person.isActive() & person.getRegistrator() != null) // user 'system' has no registrator
            {
                try
                {
                    authenticationService.getPrincipal(person.getUserId());
                } catch (IllegalArgumentException e)
                {
                    PersonUpdate update = new PersonUpdate();
                    update.setUserId(person.getPermId());
                    update.deactivate();
                    updates.add(update);
                    report.deactivateUser(person.getUserId());
                }
            }
        }
        if (updates.isEmpty() == false)
        {
            service.updatePersons(sessionToken, updates);
        }
    }

    private CurrentState loadCurrentState(String sessionToken, IApplicationServerInternalApi service)
    {
        List<AuthorizationGroup> authorizationGroups = getAllAuthorizationGroups(sessionToken, service);
        List<Person> users = getAllUsers(sessionToken, service);
        List<Space> spaces = getAllSpaces(sessionToken, service);
        List<AuthorizationGroupPermId> ids = Arrays.asList(GLOBAL_AUTHORIZATION_GROUP_ID);
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withUsers();
        AuthorizationGroup group = service.getAuthorizationGroups(sessionToken, ids, fetchOptions).get(GLOBAL_AUTHORIZATION_GROUP_ID);
        return new CurrentState(authorizationGroups, group, spaces, users);
    }

    private List<AuthorizationGroup> getAllAuthorizationGroups(String sessionToken, IApplicationServerInternalApi service)
    {
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers().withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        return service.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private List<Person> getAllUsers(String sessionToken, IApplicationServerInternalApi service)
    {
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withSpace();
        return service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private List<Space> getAllSpaces(String sessionToken, IApplicationServerInternalApi service)
    {
        SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        return service.searchSpaces(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private void manageGroup(String sessionToken, String groupCode, Map<String, Principal> groupUsers,
            CurrentState currentState, UserManagerReport report)
    {
        try
        {
            Context context = new Context(sessionToken, service, currentState, report);
            if (currentState.groupExists(groupCode))
            {
                manageKnownGroup(context, groupCode, groupUsers);
            } else
            {
                manageNewGroup(context, groupCode, groupUsers);
            }
            createSamples(context, groupCode);
            createExperiments(context, groupCode);
            context.executeOperations();
        } catch (Exception e)
        {
            String message = String.format("Couldn't manage group '%s' because of the following error: %s",
                    groupCode, e);
            report.addErrorMessage(message);
            logger.log(LogLevel.ERROR, message, e);
        }
    }

    private void createSamples(Context context, String groupCode)
    {
        if (commonSamples.isEmpty() == false)
        {
            Set<SampleIdentifier> sampleIdentifiers = new LinkedHashSet<>();
            String sessionToken = context.getSessionToken();
            for (Entry<String, String> entry : commonSamples.entrySet())
            {
                String sampleType = entry.getValue();
                String[] identifierTemplateParts = entry.getKey().split("/");
                String spaceCode = createCommonSpaceCode(groupCode, identifierTemplateParts[0]);
                String sampleCode = createCommonSpaceCode(groupCode, identifierTemplateParts[1]);
                SampleIdentifier sampleId = new SampleIdentifier(spaceCode, null, sampleCode);
                sampleIdentifiers.add(sampleId);
                if (service.getSamples(sessionToken, Arrays.asList(sampleId), new SampleFetchOptions()).isEmpty())
                {
                    SampleCreation sampleCreation = new SampleCreation();
                    sampleCreation.setCode(sampleCode);
                    sampleCreation.setTypeId(new EntityTypePermId(sampleType));
                    sampleCreation.setSpaceId(new SpacePermId(spaceCode));
                    context.add(sampleCreation);
                    context.getReport().addSample(sampleId);
                }
            }
        }
    }

    private void createExperiments(Context context, String groupCode)
    {
        if (commonExperiments.isEmpty() == false)
        {
            Set<ProjectIdentifier> projectIdentifiers = new LinkedHashSet<>();
            Set<String> keySet = commonExperiments.keySet();
            for (String identifierTemplate : keySet)
            {
                String[] identifierTemplateParts = identifierTemplate.split("/");
                String spaceCode = createCommonSpaceCode(groupCode, identifierTemplateParts[0]);
                String projectCode = createCommonSpaceCode(groupCode, identifierTemplateParts[1]);
                projectIdentifiers.add(new ProjectIdentifier(spaceCode, projectCode));
            }
            String sessionToken = context.getSessionToken();
            Set<IProjectId> existingProjects =
                    service.getProjects(sessionToken, new ArrayList<>(projectIdentifiers), new ProjectFetchOptions()).keySet();
            projectIdentifiers.removeAll(existingProjects);
            for (ProjectIdentifier identifier : projectIdentifiers)
            {
                ProjectCreation projectCreation = new ProjectCreation();
                String[] spaceCodeAndProjectCode = identifier.getIdentifier().split("/");
                projectCreation.setSpaceId(new SpacePermId(spaceCodeAndProjectCode[1]));
                projectCreation.setCode(spaceCodeAndProjectCode[2]);
                context.add(projectCreation);
                context.getReport().addProject(identifier);
            }
            for (Entry<String, String> entry : commonExperiments.entrySet())
            {
                String experimentType = entry.getValue();
                String[] identifierTemplateParts = entry.getKey().split("/");
                String spaceCode = createCommonSpaceCode(groupCode, identifierTemplateParts[0]);
                String projectCode = createCommonSpaceCode(groupCode, identifierTemplateParts[1]);
                String experimentCode = createCommonSpaceCode(groupCode, identifierTemplateParts[2]);
                ExperimentIdentifier identifier = new ExperimentIdentifier(spaceCode, projectCode, experimentCode);
                if (service.getExperiments(sessionToken, Arrays.asList(identifier), new ExperimentFetchOptions()).isEmpty())
                {
                    ExperimentCreation experimentCreation = new ExperimentCreation();
                    experimentCreation.setProjectId(new ProjectIdentifier(spaceCode, projectCode));
                    experimentCreation.setCode(experimentCode);
                    experimentCreation.setTypeId(new EntityTypePermId(experimentType));
                    context.add(experimentCreation);
                    context.getReport().addExperiment(identifier);
                }
            }
        }
    }

    private void manageKnownGroup(Context context, String groupCode, Map<String, Principal> groupUsers)
    {
        createCommonSpaces(context, groupCode);
        manageUsers(context, groupCode, groupUsers);
    }

    private void manageNewGroup(Context context, String groupCode, Map<String, Principal> groupUsers)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        assertNoCommonSpaceExists(context, groupCode);

        createAuthorizationGroup(context, groupCode);
        createAuthorizationGroup(context, adminGroupCode);

        createCommonSpaces(context, groupCode);

        manageUsers(context, groupCode, groupUsers);
    }

    private void createCommonSpaces(Context context, String groupCode)
    {
        for (Entry<Role, List<String>> entry : commonSpacesByRole.entrySet())
        {
            Role role = entry.getKey();
            for (String commonSpaceCode : entry.getValue())
            {
                String spaceCode = createCommonSpaceCode(groupCode, commonSpaceCode);
                Space space = context.getCurrentState().getSpace(spaceCode);
                if (space == null)
                {
                    ISpaceId spaceId = createSpace(context, spaceCode);
                    createRoleAssignment(context, new AuthorizationGroupPermId(groupCode), role, spaceId);
                    createRoleAssignment(context, new AuthorizationGroupPermId(createAdminGroupCode(groupCode)), Role.ADMIN, spaceId);
                }
            }
        }
    }

    private void manageUsers(Context context, String groupCode, Map<String, Principal> groupUsers)
    {
        Map<String, Person> currentUsersOfGroup = context.currentState.getCurrentUsersOfGroup(groupCode);
        Set<String> usersToBeRemoved = new TreeSet<>(currentUsersOfGroup.keySet());
        AuthorizationGroup globalGroup = context.currentState.getGlobalGroup();
        String adminGroupCode = createAdminGroupCode(groupCode);
        AuthorizationGroupPermId adminGroupId = new AuthorizationGroupPermId(adminGroupCode);
        for (Principal user : groupUsers.values())
        {
            String userId = user.getUserId();
            usersToBeRemoved.remove(userId);
            PersonPermId personId = new PersonPermId(userId);
            if (currentUsersOfGroup.containsKey(userId) == false)
            {
                SpacePermId userSpaceId = createUserSpace(context, groupCode, userId);
                Person knownUser = context.getCurrentState().getUser(userId);
                if (context.getCurrentState().userExists(userId) == false)
                {
                    PersonCreation personCreation = new PersonCreation();
                    personCreation.setUserId(userId);
                    context.add(personCreation);
                    context.getCurrentState().addNewUser(userId);
                    context.getReport().addUser(userId);
                } else if (knownUser != null && knownUser.isActive() == false)
                {
                    context.getReport().reuseUser(userId);
                }
                getHomeSpaceRequest(userId).setHomeSpace(userSpaceId);
                RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
                roleCreation.setUserId(personId);
                roleCreation.setRole(Role.ADMIN);
                roleCreation.setSpaceId(userSpaceId);
                context.add(roleCreation);
                createRoleAssignment(context, adminGroupId, Role.ADMIN, userSpaceId);
            }
            addPersonToAuthorizationGroup(context, groupCode, userId);
            if (globalGroup != null)
            {
                addPersonToAuthorizationGroup(context, globalGroup.getCode(), userId);
            }
            if (isAdmin(userId, groupCode))
            {
                addPersonToAuthorizationGroup(context, adminGroupCode, userId);
            } else
            {
                removePersonFromAuthorizationGroup(context, adminGroupCode, userId);
            }
        }
        removeUsersFromGroup(context, groupCode, usersToBeRemoved);
    }

    private void removeUsersFromGroup(Context context, String groupCode, Set<String> usersToBeRemoved)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        for (String userId : usersToBeRemoved)
        {
            removePersonFromAuthorizationGroup(context, groupCode, userId);
            removePersonFromAuthorizationGroup(context, adminGroupCode, userId);
            AuthorizationGroup globalGroup = context.currentState.getGlobalGroup();
            if (globalGroup != null)
            {
                removePersonFromAuthorizationGroup(context, globalGroup.getCode(), userId);
            }
            Person user = context.currentState.getUser(userId);
            Space homeSpace = user.getSpace();
            for (RoleAssignment roleAssignment : user.getRoleAssignments())
            {
                Space space = roleAssignment.getSpace();
                if (space != null && space.getCode().startsWith(createCommonSpaceCode(groupCode, userId.toUpperCase())))
                {
                    context.delete(roleAssignment.getId());
                    context.report.unassignRoleFrom(userId, roleAssignment.getRole(), space.getPermId());
                    if (homeSpace != null && homeSpace.getCode().equals(space.getCode()))
                    {
                        getHomeSpaceRequest(userId).removeCurrentHomeSpace();
                    }
                }
            }
        }
    }

    private SpacePermId createUserSpace(Context context, String groupCode, String userId)
    {
        String userSpaceCode = createCommonSpaceCode(groupCode, userId.toUpperCase());
        int n = context.getCurrentState().getNumberOfSpacesStartingWith(userSpaceCode);
        if (n > 0)
        {
            userSpaceCode += "_" + (n + 1);
        }
        return createSpace(context, userSpaceCode);
    }

    private HomeSpaceRequest getHomeSpaceRequest(String userId)
    {
        HomeSpaceRequest homeSpaceRequest = requestedHomeSpaceByUserId.get(userId);
        if (homeSpaceRequest == null)
        {
            homeSpaceRequest = new HomeSpaceRequest();
            requestedHomeSpaceByUserId.put(userId, homeSpaceRequest);
        }
        return homeSpaceRequest;
    }

    private boolean isAdmin(String userId, String groupCode)
    {
        UserInfo userInfo = userInfosByUserId.get(userId);
        if (userInfo == null)
        {
            return false;
        }
        GroupInfo groupInfo = userInfo.getGroupInfosByGroupKey().get(groupCode);
        return groupInfo != null && groupInfo.isAdmin();
    }

    private void addPersonToAuthorizationGroup(Context context, String groupCode, String userId)
    {
        if (context.currentState.getCurrentUsersOfGroup(groupCode).keySet().contains(userId) == false)
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().add(new PersonPermId(userId));
            context.add(groupUpdate);
            context.getReport().addUserToGroup(groupCode, userId);
        }
    }

    private void removePersonFromAuthorizationGroup(Context context, String groupCode, String userId)
    {
        if (context.currentState.getCurrentUsersOfGroup(groupCode).keySet().contains(userId))
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().remove(new PersonPermId(userId));
            context.add(groupUpdate);
            context.getReport().removeUserFromGroup(groupCode, userId);
        }
    }

    private AuthorizationGroupPermId createAuthorizationGroup(Context context, String groupCode)
    {
        AuthorizationGroupCreation creation = new AuthorizationGroupCreation();
        creation.setCode(groupCode);
        context.add(creation);
        context.getReport().addGroup(groupCode);
        return new AuthorizationGroupPermId(groupCode);
    }

    private void createRoleAssignment(Context context, AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId)
    {
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setAuthorizationGroupId(groupId);
        roleCreation.setRole(role);
        roleCreation.setSpaceId(spaceId);
        context.add(roleCreation);
        context.getReport().assignRoleTo(groupId, role, spaceId);
    }

    private void assertNoCommonSpaceExists(Context context, String groupCode)
    {
        Set<String> commonSpaces = new TreeSet<>();
        for (List<String> set : commonSpacesByRole.values())
        {
            commonSpaces.addAll(set.stream().map(s -> createCommonSpaceCode(groupCode, s)).collect(Collectors.toList()));
        }
        Map<ISpaceId, Space> spaces = getSpaces(context.getSessionToken(), commonSpaces);
        if (spaces.isEmpty())
        {
            return;
        }
        List<String> existingSpaces = new ArrayList<>(spaces.values()).stream().map(Space::getCode).collect(Collectors.toList());
        Collections.sort(existingSpaces);
        throw new IllegalStateException("The group '" + groupCode + "' has already the following spaces: " + existingSpaces);
    }

    private static final class CurrentState
    {
        private Map<String, AuthorizationGroup> groupsByCode = new TreeMap<>();

        private Map<String, Space> spacesByCode = new TreeMap<>();

        private Map<String, Person> usersById = new TreeMap<>();

        private Set<String> newUsers = new TreeSet<>();

        private AuthorizationGroup globalGroup;

        CurrentState(List<AuthorizationGroup> authorizationGroups, AuthorizationGroup globalGroup, List<Space> spaces, List<Person> users)
        {
            this.globalGroup = globalGroup;
            authorizationGroups.forEach(group -> groupsByCode.put(group.getCode(), group));
            groupsByCode.put(GLOBAL_AUTHORIZATION_GROUP_CODE, globalGroup);
            spaces.forEach(space -> spacesByCode.put(space.getCode(), space));
            users.forEach(user -> usersById.put(user.getUserId(), user));
        }

        public Map<String, Person> getCurrentUsersOfGroup(String groupCode)
        {
            Map<String, Person> result = new TreeMap<>();
            AuthorizationGroup group = groupsByCode.get(groupCode);
            if (group != null)
            {
                group.getUsers().forEach(user -> result.put(user.getUserId(), user));
            }
            return result;
        }

        public AuthorizationGroup getGlobalGroup()
        {
            return globalGroup;
        }

        public boolean userExists(String userId)
        {
            return newUsers.contains(userId) || usersById.containsKey(userId);
        }

        public Space getSpace(String spaceCode)
        {
            return spacesByCode.get(spaceCode);
        }

        public int getNumberOfSpacesStartingWith(String userSpaceCode)
        {
            Predicate<String> predicate = code -> code.startsWith(userSpaceCode);
            return spacesByCode.keySet().stream().filter(predicate).collect(Collectors.counting()).intValue();
        }

        public Person getUser(String userId)
        {
            return usersById.get(userId);
        }

        public boolean groupExists(String groupCode)
        {
            boolean groupExists = groupsByCode.containsKey(groupCode);
            String adminGroupCode = createAdminGroupCode(groupCode);
            boolean adminGroupExists = groupsByCode.containsKey(adminGroupCode);
            if (groupExists)
            {
                if (adminGroupExists == false)
                {
                    throw new IllegalArgumentException("Group " + groupCode + " exists but not " + adminGroupCode);
                }
                return true;
            }
            if (adminGroupExists)
            {
                throw new IllegalArgumentException("Group " + groupCode + " does not exist but " + adminGroupCode);
            }
            return false;
        }

        public void addNewUser(String userId)
        {
            newUsers.add(userId);
        }
    }

    private SpacePermId createSpace(Context context, String spaceCode)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(spaceCode);
        context.add(spaceCreation);
        SpacePermId spaceId = new SpacePermId(spaceCode);
        context.getReport().addSpace(spaceId);
        return spaceId;
    }

    private String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
    }

    public static String createAdminGroupCode(String groupCode)
    {
        return groupCode + "_ADMIN";
    }

    private Map<ISpaceId, Space> getSpaces(String sessionToken, Collection<String> spaceCodes)
    {
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        return service.getSpaces(sessionToken, spaceCodes.stream().map(SpacePermId::new).collect(Collectors.toList()), fetchOptions);
    }

    private Set<String> asSet(List<String> users)
    {
        return users == null ? Collections.emptySet() : new TreeSet<>(users);
    }

    private static class UserInfo
    {
        private Principal principal;

        private Map<String, GroupInfo> groupInfosByGroupKey = new TreeMap<>();

        public UserInfo(Principal principal)
        {
            this.principal = principal;
        }

        public void addGroupInfo(GroupInfo groupInfo)
        {
            groupInfosByGroupKey.put(groupInfo.getKey(), groupInfo);
        }

        public Map<String, GroupInfo> getGroupInfosByGroupKey()
        {
            return groupInfosByGroupKey;
        }

        @Override
        public String toString()
        {
            return principal.getUserId() + " " + groupInfosByGroupKey.values();
        }
    }

    private static class GroupInfo
    {
        private String key;

        private boolean admin;

        GroupInfo(String key, boolean admin)
        {
            this.key = key;
            this.admin = admin;
        }

        public String getKey()
        {
            return key;
        }

        public boolean isAdmin()
        {
            return admin;
        }

        @Override
        public String toString()
        {
            return key + (admin ? "*" : "");
        }
    }

    private static final class MappingAttributes
    {
        private String groupCode;

        private List<String> shareIds;

        public MappingAttributes(String groupCode, List<String> shareIds)
        {
            this.groupCode = groupCode;
            this.shareIds = shareIds;
        }

        public String getGroupCode()
        {
            return groupCode;
        }

        public List<String> getShareIds()
        {
            return shareIds;
        }
    }

    private static final class HomeSpaceRequest
    {
        private boolean shouldCurrentBeRemoved;

        public boolean shouldCurrentBeRemoved()
        {
            return shouldCurrentBeRemoved;
        }

        public void removeCurrentHomeSpace()
        {
            this.shouldCurrentBeRemoved = true;
        }

        private SpacePermId homeSpace;

        public SpacePermId getHomeSpace()
        {
            return homeSpace;
        }

        public void setHomeSpace(SpacePermId homeSpace)
        {
            if (this.homeSpace == null)
            {
                this.homeSpace = homeSpace;
            }
        }
    }

    private static final class Context
    {
        private String sessionToken;

        private Map<String, PersonCreation> personCreations = new LinkedMap<>();

        private List<SpaceCreation> spaceCreations = new ArrayList<>();

        private List<ProjectCreation> projectCreations = new ArrayList<>();

        private List<SampleCreation> sampleCreations = new ArrayList<>();

        private List<ExperimentCreation> experimentCreations = new ArrayList<>();

        private List<AuthorizationGroupCreation> groupCreations = new ArrayList<>();

        private List<AuthorizationGroupUpdate> groupUpdates = new ArrayList<>();

        private List<RoleAssignmentCreation> roleCreations = new ArrayList<>();

        private List<IRoleAssignmentId> roleDeletions = new ArrayList<>();

        private IApplicationServerInternalApi service;

        private CurrentState currentState;

        private UserManagerReport report;

        Context(String sessionToken, IApplicationServerInternalApi service, CurrentState currentState, UserManagerReport report)
        {
            this.sessionToken = sessionToken;
            this.service = service;
            this.currentState = currentState;
            this.report = report;
        }

        public String getSessionToken()
        {
            return sessionToken;
        }

        public CurrentState getCurrentState()
        {
            return currentState;
        }

        public UserManagerReport getReport()
        {
            return report;
        }

        public void add(PersonCreation personCreation)
        {
            personCreations.put(personCreation.getUserId(), personCreation);
        }

        public void add(SpaceCreation spaceCreation)
        {
            spaceCreations.add(spaceCreation);
        }

        public void add(ProjectCreation projectCreation)
        {
            projectCreations.add(projectCreation);
        }

        public void add(SampleCreation sampleCreation)
        {
            sampleCreations.add(sampleCreation);
        }

        public void add(ExperimentCreation experimentCreation)
        {
            experimentCreations.add(experimentCreation);
        }

        public void add(AuthorizationGroupCreation creation)
        {
            groupCreations.add(creation);
        }

        public void add(RoleAssignmentCreation roleCreation)
        {
            roleCreations.add(roleCreation);
        }

        public void add(AuthorizationGroupUpdate groupUpdate)
        {
            groupUpdates.add(groupUpdate);
        }

        public void delete(IRoleAssignmentId roleAssignmentId)
        {
            roleDeletions.add(roleAssignmentId);
        }

        public void executeOperations()
        {
            List<IOperation> operations = new ArrayList<>();
            if (personCreations.isEmpty() == false)
            {
                operations.add(new CreatePersonsOperation(new ArrayList<>(personCreations.values())));
            }
            if (spaceCreations.isEmpty() == false)
            {
                operations.add(new CreateSpacesOperation(spaceCreations));
            }
            if (projectCreations.isEmpty() == false)
            {
                operations.add(new CreateProjectsOperation(projectCreations));
            }
            if (sampleCreations.isEmpty() == false)
            {
                operations.add(new CreateSamplesOperation(sampleCreations));
            }
            if (experimentCreations.isEmpty() == false)
            {
                operations.add(new CreateExperimentsOperation(experimentCreations));
            }
            if (groupCreations.isEmpty() == false)
            {
                operations.add(new CreateAuthorizationGroupsOperation(groupCreations));
            }
            if (groupUpdates.isEmpty() == false)
            {
                operations.add(new UpdateAuthorizationGroupsOperation(groupUpdates));
            }
            if (roleCreations.isEmpty() == false)
            {
                operations.add(new CreateRoleAssignmentsOperation(roleCreations));
            }
            if (roleDeletions.isEmpty() == false)
            {
                RoleAssignmentDeletionOptions options = new RoleAssignmentDeletionOptions();
                options.setReason("Users removed from a group");
                operations.add(new DeleteRoleAssignmentsOperation(roleDeletions, options));
            }
            if (operations.isEmpty())
            {
                return;
            }
            SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
            service.executeOperations(sessionToken, operations, options);
        }
    }
}
