package ch.systemsx.cisd.openbis.generic.server.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.IAuthenticationService;

public class EmbeddedKeycloakUserStorageProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider, CredentialInputValidator
{
    private final KeycloakSession session;

    private final ComponentModel model;

    private final IApplicationServerInternalApi applicationServerApi;

    private final IAuthenticationService authenticationService;

    public EmbeddedKeycloakUserStorageProvider(final KeycloakSession session, final ComponentModel model,
            final IApplicationServerInternalApi applicationServerApi, final IAuthenticationService authenticationService)
    {
        this.session = session;
        this.model = model;
        this.applicationServerApi = applicationServerApi;
        this.authenticationService = authenticationService;
    }

    @Override public boolean supportsCredentialType(final String credentialType)
    {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override public boolean isConfiguredFor(final RealmModel realm, final UserModel user, final String credentialType)
    {
        return true;
    }

    @Override public boolean isValid(final RealmModel realm, final UserModel user, final CredentialInput credentialInput)
    {
        return authenticationService.authenticateUser(user.getUsername(), credentialInput.getChallengeResponse());
    }

    @Override public int getUsersCount(final RealmModel realm)
    {
        return getUsers(realm).size();
    }

    @Override public UserModel getUserById(final RealmModel realm, final String id)
    {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(username, realm);
    }

    @Override public UserModel getUserById(final String id, final RealmModel realm)
    {
        return getUserById(realm, id);
    }

    @Override public UserModel getUserByUsername(final String username, final RealmModel realm)
    {
        PersonSearchCriteria criteria = new PersonSearchCriteria();
        criteria.withUserId().thatEquals(username);
        return searchOne(criteria, new PersonFetchOptions(), realm);
    }

    @Override public UserModel getUserByEmail(final String email, final RealmModel realm)
    {
        PersonSearchCriteria criteria = new PersonSearchCriteria();
        criteria.withEmail().thatEquals(email);
        return searchOne(criteria, new PersonFetchOptions(), realm);
    }

    @Override public List<UserModel> getUsers(final RealmModel realm)
    {
        return getUsers(realm, 0, Integer.MAX_VALUE);
    }

    @Override public List<UserModel> getUsers(final RealmModel realm, final int firstResult, final int maxResults)
    {
        PersonSearchCriteria criteria = new PersonSearchCriteria();
        PersonFetchOptions fo = new PersonFetchOptions();
        fo.from(firstResult);
        fo.count(maxResults);
        return searchMany(criteria, fo, realm);
    }

    @Override public List<UserModel> searchForUser(final String search, final RealmModel realm)
    {
        return searchForUser(search, realm, 0, Integer.MAX_VALUE);
    }

    @Override public List<UserModel> searchForUser(final String search, final RealmModel realm, final int firstResult, final int maxResults)
    {
        PersonSearchCriteria criteria = new PersonSearchCriteria();
        criteria.withOrOperator();
        criteria.withUserId().thatEquals(search);
        criteria.withFirstName().thatEquals(search);
        criteria.withLastName().thatEquals(search);
        criteria.withEmail().thatEquals(search);
        PersonFetchOptions fo = new PersonFetchOptions();
        fo.from(firstResult);
        fo.count(maxResults);
        return searchMany(criteria, fo, realm);
    }

    @Override public List<UserModel> searchForUser(final Map<String, String> params, final RealmModel realm)
    {
        return searchForUser(params, realm, 0, Integer.MAX_VALUE);
    }

    @Override public List<UserModel> searchForUser(final Map<String, String> params, final RealmModel realm, final int firstResult,
            final int maxResults)
    {
        PersonSearchCriteria criteria = new PersonSearchCriteria();
        criteria.withAndOperator();

        if (params.get(UserModel.USERNAME) != null)
        {
            criteria.withUserId().thatEquals(params.get(UserModel.USERNAME));
        }
        if (params.get(UserModel.FIRST_NAME) != null)
        {
            criteria.withFirstName().thatEquals(params.get(UserModel.FIRST_NAME));
        }
        if (params.get(UserModel.LAST_NAME) != null)
        {
            criteria.withLastName().thatEquals(params.get(UserModel.LAST_NAME));
        }
        if (params.get(UserModel.EMAIL) != null)
        {
            criteria.withEmail().thatEquals(params.get(UserModel.EMAIL));
        }

        PersonFetchOptions fo = new PersonFetchOptions();
        fo.from(firstResult);
        fo.count(maxResults);
        return searchMany(criteria, fo, realm);
    }

    @Override public List<UserModel> searchForUserByUserAttribute(final String attrName, final String attrValue, final RealmModel realm)
    {
        return Collections.emptyList();
    }

    @Override public List<UserModel> getGroupMembers(final RealmModel realm, final GroupModel group)
    {
        return Collections.emptyList();
    }

    @Override public List<UserModel> getGroupMembers(final RealmModel realm, final GroupModel group, final int firstResult, final int maxResults)
    {
        return Collections.emptyList();
    }

    private UserModel searchOne(final PersonSearchCriteria criteria, final PersonFetchOptions fo, final RealmModel realm)
    {
        List<UserModel> list = searchMany(criteria, fo, realm);
        return list.size() > 0 ? list.get(0) : null;
    }

    private List<UserModel> searchMany(final PersonSearchCriteria criteria, final PersonFetchOptions fo, final RealmModel realm)
    {
        String sessionToken = null;

        try
        {
            sessionToken = applicationServerApi.loginAsSystem();
            SearchResult<Person> result = applicationServerApi.searchPersons(sessionToken, criteria, fo);
            return result.getObjects().stream().map(person -> convert(realm, person)).collect(Collectors.toList());
        } finally
        {
            if (sessionToken != null)
            {
                applicationServerApi.logout(sessionToken);
            }
        }
    }

    private UserModel convert(final RealmModel realm, final Person person)
    {
        if (person != null)
        {
            return new AbstractUserAdapter(this.session, realm, this.model)
            {
                @Override public String getUsername()
                {
                    return person.getUserId();
                }
            };
        } else
        {
            return null;
        }
    }

    @Override public void close()
    {

    }
}
