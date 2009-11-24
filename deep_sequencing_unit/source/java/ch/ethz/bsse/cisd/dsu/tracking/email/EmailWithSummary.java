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

package ch.ethz.bsse.cisd.dsu.tracking.email;

/**
 * Encapsulation of {@link Email} with a short summary of its content.
 * 
 * @author Piotr Buczek
 */
public class EmailWithSummary
{
    private final Email email;

    private final String summary;

    public EmailWithSummary(Email email, String summary)
    {
        this.email = email;
        this.summary = summary;
    }

    public Email getEmail()
    {
        return email;
    }

    public String getSummary()
    {
        return summary;
    }

}
