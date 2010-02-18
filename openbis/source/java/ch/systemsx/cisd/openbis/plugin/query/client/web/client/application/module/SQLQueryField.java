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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;

/**
 * A text area to specify SQL query.
 * 
 * @author Piotr Buczek
 */
public class SQLQueryField extends MultilineVarcharField
{

    private final static String EMPTY_TEXT = "SELECT ... \nFROM ... \nWHERE ...";

    // SQL query needs to start with 'select' (ignore-case) and can contain newlines
    private final static String REGEX = "^(select|SELECT)(.|[\n\r])*";

    private final static String REGEX_TEXT_MSG = "SQL query should begin with a 'SELECT' keyword.";

    private final static String BLANK_TEXT_MSG = "SQL query text required";

    public SQLQueryField(IMessageProvider messageProvider, boolean mandatory, int lines)
    {
        super(messageProvider.getMessage(Dict.SQL_QUERY), mandatory, lines);
        setEmptyText(EMPTY_TEXT);
        setRegex(REGEX);
        getMessages().setRegexText(REGEX_TEXT_MSG);
        getMessages().setBlankText(BLANK_TEXT_MSG);
    }

    public SQLQueryField(IMessageProvider messageProvider, boolean mandatory)
    {
        this(messageProvider, mandatory, DEFAULT_LINES);
    }

}
