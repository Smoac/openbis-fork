/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset;

import java.util.Arrays;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractProviderTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.ProteinSequenceProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSequenceProviderTest extends AbstractProviderTest
{
    @Test
    public void test()
    {
        final ProteinSequence ps = new ProteinSequence();
        ps.setShortName("short-name");
        ps.setDatabaseNameAndVersion("db-version");
        ps.setSequence("ABC");
        final IPhosphoNetXServer phosphonetxServer = context.mock(IPhosphoNetXServer.class);
        context.checking(new Expectations()
            {
                {
                    one(phosphonetxServer).listProteinSequencesByProteinReference(SESSION_TOKEN, new TechId(42), new TechId(43));
                    will(returnValue(Arrays.asList(ps)));
                }
            });
        ProteinSequenceProvider provider =
                new ProteinSequenceProvider(phosphonetxServer, SESSION_TOKEN, new TechId(42), new TechId(43));

        TypedTableModel<ProteinSequence> model = provider.createTableModel();

        assertEquals("[SEQUENCE_SHORT_NAME, DATABASE_NAME_AND_VERSION, SEQUENCE]", getHeaderIDs(model).toString());
        assertEquals("[VARCHAR, VARCHAR, VARCHAR]", getHeaderDataTypes(model).toString());
        assertEquals("[null, null, null]", getHeaderEntityKinds(model).toString());
        assertSame(ps, model.getRows().get(0).getObjectOrNull());
        assertEquals("[short-name, db-version, ABC]", model.getRows().get(0).getValues().toString());
        assertEquals(1, model.getRows().size());
        context.assertIsSatisfied();
    }

}
