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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link SampleIdentifier} class.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleIdentifierTest
{
    final static String SAMPLE_TYPE = "TYPE1";

    @Test
    public final void testToStringDbLevel()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "SAMP");
        assertEquals(sampleIdentifier.toString(), "DB:/SAMP");
    }

    @Test
    public final void testToStringGroupLevelHomeBd()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new GroupIdentifier(DatabaseInstanceIdentifier.HOME, "GR"),
                        "SAMP");
        assertEquals(sampleIdentifier.toString(), "/GR/SAMP");
    }

    @Test
    public final void testToStringGroupLevel()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new GroupIdentifier("DB", "GR"), "SAMP");
        assertEquals(sampleIdentifier.toString(), "DB:/GR/SAMP");
    }

    @Test
    public final void testToStringHomeGroupLevel()
    {
        final SampleIdentifier sampleIdentifier = SampleIdentifier.createHomeGroup("SAMP");
        assertEquals(sampleIdentifier.toString(), "SAMP");
    }

    @Test
    public final void testCompareIdentifiers()
    {
        DatabaseInstanceIdentifier homeDb = DatabaseInstanceIdentifier.createHome();
        DatabaseInstanceIdentifier otherDb = new DatabaseInstanceIdentifier("db");

        String g1 = "AG";
        String g2 = "BG";
        String c1 = "A";
        String c2 = "B";
        SampleIdentifier[] expectedOrder =
                new SampleIdentifier[]
                    { SampleIdentifier.createHomeGroup(c1), SampleIdentifier.createHomeGroup(c2),
                            new SampleIdentifier(new GroupIdentifier(homeDb, g1), c1),
                            new SampleIdentifier(new GroupIdentifier(homeDb, g2), c1),
                            new SampleIdentifier(new GroupIdentifier(homeDb, g2), c2),
                            new SampleIdentifier(new GroupIdentifier(otherDb, g1), c1),
                            new SampleIdentifier(new GroupIdentifier(otherDb, g1), c2),
                            new SampleIdentifier(new GroupIdentifier(otherDb, g2), c1),
                            new SampleIdentifier(new GroupIdentifier(otherDb, g2), c2) };

        SampleIdentifier[] idents = revert(expectedOrder);
        Arrays.sort(idents);
        assertEquals(Arrays.asList(expectedOrder), Arrays.asList(idents));
    }

    @Test
    public void testEquals()
    {
        SampleIdentifier id1 = new SampleIdentifierFactory("DB:/SPACE/SAMP").createIdentifier();
        SampleIdentifier id2 = new SampleIdentifierFactory("DB:/SPACE/SAMP").createIdentifier();
        assertEquals(id1, id2);
    }

    private static SampleIdentifier[] revert(SampleIdentifier[] expectedOrder)
    {
        SampleIdentifier[] idents = new SampleIdentifier[expectedOrder.length];
        for (int i = 0; i < idents.length; i++)
        {
            idents[idents.length - i - 1] = expectedOrder[i];
        }
        return idents;
    }
}
