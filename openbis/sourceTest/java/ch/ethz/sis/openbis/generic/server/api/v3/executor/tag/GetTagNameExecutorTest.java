/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.tag;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagNameId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class GetTagNameExecutorTest extends AbstractExecutorTest
{

    @Test
    public void testWithPermId()
    {
        final Session session = createSession();

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).getSession();
                    will(returnValue(session));

                }
            });

        String name = execute(new TagPermId("/" + session.tryGetPerson().getUserId() + "/TEST_PERM_ID"));
        Assert.assertEquals("TEST_PERM_ID", name);
    }

    @Test(expectedExceptions = { UnauthorizedObjectAccessException.class }, expectedExceptionsMessageRegExp = ".*/OTHER_USER/TEST_PERM_ID.*")
    public void testWithPermIdForDifferentUser()
    {
        final Session session = createSession();

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).getSession();
                    will(returnValue(session));

                }
            });

        execute(new TagPermId("/OTHER_USER/TEST_PERM_ID"));
    }

    @Test
    public void testWithNameId()
    {
        String name = execute(new TagNameId("TEST_NAME_ID"));
        Assert.assertEquals("TEST_NAME_ID", name);
    }

    private String execute(ITagId tagId)
    {
        GetTagNameExecutor executor = new GetTagNameExecutor();
        return executor.getTagName(operationContext, tagId);
    }

}
