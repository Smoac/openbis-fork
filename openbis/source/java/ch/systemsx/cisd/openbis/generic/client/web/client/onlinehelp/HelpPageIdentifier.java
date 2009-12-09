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

package ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp;

/**
 * An identifier that uniquely designates a help page. The identifier is made up of a domain and an
 * action.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class HelpPageIdentifier
{
    public static enum HelpPageDomain
    {
        EXPERIMENT, SAMPLE, DATA_SET, MATERIAL, GROUP, PROJECT, VOCABULARY, PROPERTY_TYPE,
        FILE_TYPE, AUTHORIZATION
    }

    public static enum HelpPageAction
    {
        BROWSE, REGISTER, IMPORT, UPLOAD
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

}
