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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.ldap.LDAPDirectoryConfiguration;
import ch.systemsx.cisd.authentication.ldap.LDAPPrincipalQuery;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * {@link IMaintenanceTask} to revoke access to delete LDAP users.
 * 
 * @author Juan Fuentes
 */
public class RevokeUserAccessMaintenanceTask implements IMaintenanceTask {
	private static final Logger operationLog = LogFactory.getLogger(
			LogCategory.OPERATION, RevokeUserAccessMaintenanceTask.class);

	private LDAPDirectoryConfiguration config;
	private LDAPPrincipalQuery query;

	private static final String SERVER_URL_KEY = "server-url";
	private static final String SECURITY_PRINCIPAL_DISTINGUISHED_NAME_KEY = "security-principal-distinguished-name";
	private static final String SECURITY_PRINCIPAL_PASSWORD_KEY = "security-principal-password";

	@Override
	public void setUp(String pluginName, Properties properties) {
		operationLog.info("Task " + pluginName + " initialized.");

		config = new LDAPDirectoryConfiguration();
		config.setServerUrl(properties.getProperty(SERVER_URL_KEY));
		config.setSecurityPrincipalDistinguishedName(properties
				.getProperty(SECURITY_PRINCIPAL_DISTINGUISHED_NAME_KEY));
		config.setSecurityPrincipalPassword(properties
				.getProperty(SECURITY_PRINCIPAL_PASSWORD_KEY));
		config.setQueryEmailForAliases("true");
		config.setTimeoutStr("1000");
		config.setTimeToWaitAfterFailureStr("1000");

		query = new LDAPPrincipalQuery(config);
	}

	@Override
	public void execute() {
		operationLog.info("execution started");

		// 1. Grab all users, user roles and user authorization groups
		IPersonDAO personDAO = CommonServiceProvider.getDAOFactory()
				.getPersonDAO();
		IRoleAssignmentDAO rolesDAO = CommonServiceProvider.getDAOFactory()
				.getRoleAssignmentDAO();
		// Used to manage the authorization groups since the IPersonDAO throw a session exception when accessing this information.
		ICommonServerForInternalUse server = CommonServiceProvider
				.getCommonServer();
		SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();

		List<PersonPE> people = personDAO.listActivePersons();

		// 2. Users to Revoke
		List<PersonPE> peopleToRevoke = new ArrayList<PersonPE>();

		// 3. Check if the users exists on LDAP currently
		personCheck: for (PersonPE person : people) {
			if (false == person.isSystemUser() && person.isActive()
					&& false == isUserAtLDAP(person.getUserId())) {

				List<RoleAssignmentPE> roles = rolesDAO
						.listRoleAssignmentsByPerson(person);
				for (RoleAssignmentPE role : roles) {
					if (role.getRole().name().equals("ETL_SERVER")) {
						continue personCheck;
					}
				}
				peopleToRevoke.add(person);
			}
		}

		// 4. If is not found on the LDAP, revoke access
		for (PersonPE person : peopleToRevoke) {
			String userIdToRevoke = person.getUserId();
			operationLog.info("person " + userIdToRevoke
					+ " is going to be revoked.");

			// Delete person roles
			for (RoleAssignmentPE role : rolesDAO
					.listRoleAssignmentsByPerson(person)) {
				rolesDAO.delete(role);
			}

			// Delete person from groups
			List<AuthorizationGroup> groups = server
					.listAuthorizationGroups(contextOrNull.getSessionToken());

			for (AuthorizationGroup group : groups) {
				List<Person> peopleInGroup = server
						.listPersonInAuthorizationGroup(contextOrNull
								.getSessionToken(), new TechId(group.getId()));
				for (Person personInGroup : peopleInGroup) {
					if (personInGroup.getUserId().equals(userIdToRevoke)) {
						List<String> toRemoveFromGroup = new ArrayList<String>();
						toRemoveFromGroup.add(person.getUserId());
						server.removePersonsFromAuthorizationGroup(
								contextOrNull.getSessionToken(), new TechId(
										group.getId()), toRemoveFromGroup);
					}
				}
			}

			// Change userId and disable
			person.setUserId(person.getUserId() + "-" + getTimeStamp());
			person.setActive(false);
			personDAO.updatePerson(person);

			operationLog
					.info("person " + userIdToRevoke + " has been revoked.");
		}

		operationLog.info("task executed");
	}

	private boolean isUserAtLDAP(String userId) {
		List<Principal> principals = query.listPrincipalsByUserId(userId);
		return false == principals.isEmpty();
	}

	private String getTimeStamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		return dateFormat.format(new Date());
	}

}
