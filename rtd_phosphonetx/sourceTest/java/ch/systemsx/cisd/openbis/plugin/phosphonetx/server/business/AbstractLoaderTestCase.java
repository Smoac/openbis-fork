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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOWithoutContextTest;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:phosphonetx-applicationContext.xml")
//In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
//Spring looks for 'transactionManager').
@TransactionConfiguration(transactionManager = "transaction-manager")
public abstract class AbstractLoaderTestCase extends AbstractDAOWithoutContextTest
{
    private static final Principal PRINCIPAL = new Principal(CommonTestUtils.USER_ID, "john",
            "doe", "j@d");

    private static final String SESSION_TOKEN = "session-token";

    protected static final Session SESSION = new Session(CommonTestUtils.USER_ID, SESSION_TOKEN,
            PRINCIPAL, "remote-host", 1);
    
    protected ICommonBusinessObjectFactory boFactory;

    @Autowired
    public final void setBoFactory(final ICommonBusinessObjectFactory boFactory)
    {
        this.boFactory = boFactory;
    }
}
