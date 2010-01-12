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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.help;

import java.util.ArrayList;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * An identifier that uniquely designates a help page. The identifier is made up of a domain and an
 * action.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public class HelpPageIdentifier
{
    /**
     * {@link HelpPageIdentifier} domains with names being used in dictionary keys
     * 
     * @see HelpPageIdentifier#getHelpPageTitle(IMessageProvider)
     */
    public static enum HelpPageDomain
    {
        // base domains (as in menu)
        EXPERIMENT, SAMPLE, DATA_SET, MATERIAL, ADMINISTRATION,

        // administration subdomains
        GROUP(ADMINISTRATION),

        PROJECT(ADMINISTRATION),

        VOCABULARY(ADMINISTRATION),

        PROPERTY_TYPE(ADMINISTRATION),
        // property type subdomains
        ASSIGNMENT(PROPERTY_TYPE),

        FILE_FORMAT(ADMINISTRATION),

        AUTHORIZATION(ADMINISTRATION),
        // authorization subdomains
        USERS(AUTHORIZATION), ROLES(AUTHORIZATION), AUTHORIZATION_GROUPS(AUTHORIZATION),

        // other base domains
        CHANGE_USER_SETTINGS;

        // could be used to create a hierarchy of help pages
        private HelpPageDomain superDomainOrNull;

        HelpPageDomain()
        {
            // no super domain
        }

        HelpPageDomain(HelpPageDomain superDomain)
        {
            this.superDomainOrNull = superDomain;
        }

        public HelpPageDomain getSuperDomainOrNull()
        {
            return superDomainOrNull;
        }

        /**
         * List of {@link HelpPageDomain}s starting from the base domain (a domain without super
         * domain) down to this domain.
         */
        public List<HelpPageDomain> getDomainPath()
        {
            final List<HelpPageDomain> result = new ArrayList<HelpPageDomain>();
            fillDomainPath(result);
            return result;
        }

        private void fillDomainPath(List<HelpPageDomain> domainPath)
        {
            if (getSuperDomainOrNull() != null)
            {
                getSuperDomainOrNull().fillDomainPath(domainPath);
            }
            domainPath.add(this);
        }
    }

    /**
     * {@link HelpPageIdentifier} actions with names being used in dictionary keys.
     * 
     * @see HelpPageIdentifier#getHelpPageTitle(IMessageProvider)
     */
    public static enum HelpPageAction
    {
        BROWSE, REGISTER, IMPORT, UPLOAD, EDIT, SEARCH, ACTION
    }

    private HelpPageDomain domain;

    private HelpPageAction action;

    /**
     * Create a new help page identifier for the given domain and action.
     * 
     * @param domain
     * @param action
     */
    public HelpPageIdentifier(HelpPageDomain domain, HelpPageAction action)
    {
        assert domain != null;
        assert action != null;
        this.domain = domain;
        this.action = action;
    }

    public HelpPageDomain getHelpPageDomain()
    {
        return domain;
    }

    public HelpPageAction getHelpPageAction()
    {
        return action;
    }

    private static char PAGE_NAME_KEY_SEPARATOR = '.';

    public String getHelpPageTitle(IMessageProvider messageProvider)
    {
        final String messageKey = getHelpPageTitleKey();
        // If there is no message for the key return the key as the title,
        // otherwise return the message.
        return messageProvider.containsKey(messageKey) ? messageProvider.getMessage(messageKey)
                : messageKey; // TODO 2010-01-12, Piotr Buczek: is it better to return null?
    }

    @Private
    public String getHelpPageTitleKey()
    {
        final StringBuilder messageKeyBuilder = new StringBuilder();
        final List<HelpPageDomain> domainPath = getHelpPageDomain().getDomainPath();
        for (HelpPageDomain d : domainPath)
        {
            messageKeyBuilder.append(d.name() + PAGE_NAME_KEY_SEPARATOR);
        }
        messageKeyBuilder.append(getHelpPageAction().name());

        return messageKeyBuilder.toString();
    }
}
