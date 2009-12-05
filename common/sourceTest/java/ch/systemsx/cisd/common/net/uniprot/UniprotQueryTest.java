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

package ch.systemsx.cisd.common.net.uniprot;

import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.ENTRY_NAME;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.EXISTENCE;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.GENES;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.LAST_MODIFIED;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.LENGTH;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.SEQUENCE;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.STATUS;
import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.VERSION;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * A simple test case for the {@link UniprotQuery}.
 * <p>
 * <i>Note: This test depends on the Uniprot database query engine at http://www.uniprot.org/uniprot
 * !</i>
 * 
 * @author Bernd Rinn
 */
public class UniprotQueryTest
{
    private final static Date REFERENCE_DATE;

    static
    {
        LogInitializer.init();
        try
        {
            REFERENCE_DATE = UniprotEntry.DATE_FORMAT.get().parse("2009-11-24");
        } catch (ParseException ex)
        {
            throw new Error(ex);
        }
    }

    @Test(groups = "network")
    public void testUniprotQuery()
    {
        final UniprotQuery uniprot =
                new UniprotQuery(ENTRY_NAME, GENES, EXISTENCE, STATUS, LENGTH, SEQUENCE, VERSION,
                        LAST_MODIFIED);
        int count = 0;
        for (UniprotEntry entry : uniprot.queryForIds("p12345"))
        {
            ++count;
            assertEquals("P12345", entry.getId());
            assertEquals("AATM_RABIT", entry.getEntryName());
            assertEquals("GOT2", entry.getGenes());
            assertEquals("Evidence at protein level", entry.getExistence());
            assertEquals("reviewed", entry.getStatus());
            assertEquals(30, entry.getLength().intValue());
            assertEquals("SSWWAHVEMG PPDPILGVTE AYKRDTNSKK", entry.getSequence());
            assertTrue(Integer.toString(entry.getVersion()), entry.getVersion() >= 63);
            assertTrue(entry.getLastModifiedStr(),
                    entry.getLastModified().getTime() >= REFERENCE_DATE.getTime());
        }
        assertEquals(1, count);
    }
}
