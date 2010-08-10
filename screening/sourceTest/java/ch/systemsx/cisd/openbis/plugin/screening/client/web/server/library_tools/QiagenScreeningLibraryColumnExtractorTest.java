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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.library_tools;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class QiagenScreeningLibraryColumnExtractorTest extends AssertJUnit
{

    QiagenScreeningLibraryColumnExtractor extractor;

    @BeforeMethod
    public void setUp()
    {
        String[] headerTokens =
                    { "barcode", "row", "col", "sirna", "productId", "productName", "reseqMrnas",
                            "geneId", "symbol", "description" };
        extractor = new QiagenScreeningLibraryColumnExtractor(headerTokens);
    }

    @Test
    public void testWellCode()
    {
        assertEquals("A1", extractor.getWellCode(getRow()));

        // Test the hypothetical situation that the col is 01 instead of 1
        // The well code should still be A1
        String[] row = getRow();
        row[2] = "01";
        assertEquals("A1", extractor.getWellCode(row));
    }

    private String[] getRow()
    {
        String[] row =
                    { "H001-1A", "A", "1", "TCCCGTATAAGTATGTTCCAA", "SI00077350", "Hs_BMP15_3",
                            "NM_005448,9210", "BMP15", "bone morphogenetic protein 15" };
        return row;
    }
}
