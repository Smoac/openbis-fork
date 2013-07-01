/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.clc;

import java.util.ArrayList;
import java.util.List;

import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.accesscontrol.AuthenticationFailureException;
import com.clcbio.api.base.persistence.accesscontrol.Authenticator;
import com.clcbio.api.base.persistence.accesscontrol.Session;
import com.clcbio.api.base.persistence.accesscontrol.UsersGroupsChangedListener;

/**
 * @author anttil
 */
public class OpenBISAuthenticator extends Authenticator
{

    @Override
    public void addGroup(String arg0, Session arg1) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.addGroup: " + arg0 + ", " + arg1);
    }

    @Override
    public void addListener(UsersGroupsChangedListener arg0)
    {
        System.err.println("OpenBISAuthenticator.addListener: " + arg0);
    }

    @Override
    public void addUser(String arg0, String arg1, Session arg2) throws AuthenticationFailureException
    {
        System.err.println("OpenBISAuthenticator.addUser: " + arg0 + ", " + arg1 + ", " + arg2);
    }

    @Override
    public void addUserToGroup(String arg0, String arg1, Session arg2) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.addUserToGroup: " + arg0 + ", " + arg1 + ", " + arg2);
    }

    @Override
    public void authorize(String arg0, String arg1) throws AuthenticationFailureException
    {
        System.err.println("OpenBISAuthenticator.authorize: " + arg0 + ", " + arg1);
    }

    @Override
    public void checkAdminPermission() throws PersistenceException
    {
        System.err.println("OpenBISAuthenticator.checkAdminPermission");
    }

    @Override
    public void deauthorize(String arg0)
    {
        System.err.println("OpenBISAuthenticator.deauthorize: " + arg0);
    }

    @Override
    public String getAdminGroupID()
    {
        System.err.println("OpenBISAuthenticator.getAdminGroupID");
        return "ADMINS";
    }

    @Override
    public List<String> getAllGroups(Session arg0) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.getAllGroups: " + arg0);
        return new ArrayList<String>();
    }

    @Override
    public List<String> getGroups(String arg0, Session arg1) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.getGroups: " + arg0 + ", " + arg1);
        return null;
    }

    @Override
    public List<String> getUsers(Session arg0)
    {
        System.err.println("OpenBISAuthenticator.getUsers: " + arg0);
        return new ArrayList<String>();
    }

    @Override
    public List<String> getUsersInGroup(String arg0, Session arg1) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.getUsersInGroup: " + arg0 + ", " + arg1);
        return new ArrayList<String>();
    }

    @Override
    public boolean isEditable()
    {
        System.err.println("OpenBISAuthenticator.isEditable");
        return true;
    }

    @Override
    public void removeGroup(String arg0, Session arg1) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.removeGroup: " + arg0 + ", " + arg1);
    }

    @Override
    public void removeListener(UsersGroupsChangedListener arg0)
    {
        System.err.println("OpenBISAuthenticator.removeListener: " + arg0);
    }

    @Override
    public void removeUser(String arg0, Session arg1) throws AuthenticationFailureException
    {
        System.err.println("OpenBISAuthenticator.removeUser: " + arg0 + ", " + arg1);
    }

    @Override
    public void removeUserFromGroup(String arg0, String arg1, Session arg2) throws UserGroupException
    {
        System.err.println("OpenBISAuthenticator.removeUserFromGroup: " + arg0 + ", " + arg1 + ", " + arg2);
    }

    @Override
    public void setPassword(String arg0, String arg1, Session arg2) throws AuthenticationFailureException
    {
        System.err.println("OpenBISAuthenticator.setPassword: " + arg0 + ", " + arg1 + ", " + arg2);
    }

}
