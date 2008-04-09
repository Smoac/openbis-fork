/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Abstract test case for <i>DAO</i>.
 * <p>
 * Note that the {@link Transactional} does not work with TestNG right now but we keep it anyway to
 * express intention. The real transactions are handled in {@link #beforeMethod()} and
 * {@link #afterMethod()}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDAOTest extends AbstractDbUnitTest
{

    static final String EXCEED_20_CHARACTERS = StringUtils.repeat("A", 21);

    //
    // TestNG annotations. We put '(alwaysRun = true)' so that these methods get called as well when
    // we run the 'db'
    // group for instance (by default, they do not when running groups).
    //

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod()
    {
        startNewTransaction();
    }

    @AfterMethod(alwaysRun = true)
    public final void afterMethod()
    {
        endTransaction();
    }
}
