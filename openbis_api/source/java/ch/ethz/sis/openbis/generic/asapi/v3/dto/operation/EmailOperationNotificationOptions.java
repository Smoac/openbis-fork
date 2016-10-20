/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation;

import java.util.Arrays;
import java.util.List;

/**
 * @author pkupczyk
 */
public class EmailOperationNotificationOptions implements IOperationNotificationOptions
{

    private List<String> emails;

    @SuppressWarnings("unused")
    private EmailOperationNotificationOptions()
    {
    }

    public EmailOperationNotificationOptions(List<String> emails)
    {
        this.emails = emails;
    }

    public EmailOperationNotificationOptions(String... emails)
    {
        this.emails = Arrays.asList(emails);
    }

    public List<String> getEmails()
    {
        return emails;
    }

}
