/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl;

import java.util.List;

import ch.systemsx.cisd.openbis.uitest.rmi.SearchForSamplesOnBehalfOfUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.SearchForSamplesRmi;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class SampleSearchCommandBuilder implements SearchCommandBuilder<Sample>
{

    private String code;

    private User user;

    @SuppressWarnings("hiding")
    public SampleSearchCommandBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @SuppressWarnings("hiding")
    public SampleSearchCommandBuilder onBehalfOf(User user)
    {
        this.user = user;
        return this;
    }

    @Override
    public Command<List<Sample>> build()
    {
        if (this.user == null)
        {
            return new SearchForSamplesRmi(code);
        } else
        {
            return new SearchForSamplesOnBehalfOfUserRmi(code, user);
        }
    }

}
