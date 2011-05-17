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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server.resultset;

import java.util.Arrays;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractProviderTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSummaryProviderTest extends AbstractProviderTest
{
    @Test
    public void test()
    {
        final ProteinSummary ps = new ProteinSummary();
        ps.setFDR(0.125);
        ps.setProteinCount(42);
        ps.setPeptideCount(4711);
        ps.setDecoyProteinCount(2);
        ps.setDecoyPeptideCount(5);
        final IPhosphoNetXServer phosphonetxServer = context.mock(IPhosphoNetXServer.class);
        context.checking(new Expectations()
            {
                {
                    one(phosphonetxServer).listProteinSummariesByExperiment(SESSION_TOKEN,
                            new TechId(12));
                    will(returnValue(Arrays.asList(ps)));
                }
            });
        ProteinSummaryProvider provider =
                new ProteinSummaryProvider(phosphonetxServer, SESSION_TOKEN, new TechId(12));

        TypedTableModel<ProteinSummary> model = provider.createTableModel();

        assertEquals(
                "[FDR, PROTEIN_COUNT, PEPTIDE_COUNT, DECOY_PROTEIN_COUNT, DECOY_PEPTIDE_COUNT]",
                getHeaderIDs(model).toString());
        assertEquals("[REAL, INTEGER, INTEGER, INTEGER, INTEGER]", getHeaderDataTypes(model)
                .toString());
        assertEquals("[null, null, null, null, null]", getHeaderEntityKinds(model).toString());
        assertSame(ps, model.getRows().get(0).getObjectOrNull());
        assertEquals("[0.125, 42, 4711, 2, 5]", model.getRows().get(0).getValues().toString());
        assertEquals(1, model.getRows().size());
        context.assertIsSatisfied();
    }
}
