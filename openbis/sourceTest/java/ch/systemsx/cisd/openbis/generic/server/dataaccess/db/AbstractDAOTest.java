/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * {@link AbstractDAOTest} extension setting configuration context.
 * 
 * @author Izabela Adamczyk
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
// In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
// Spring looks for 'transactionManager').
@TransactionConfiguration(transactionManager = "transaction-manager")
public abstract class AbstractDAOTest extends AbstractDAOWithoutContextTest
{
}
