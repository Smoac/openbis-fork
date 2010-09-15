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

package ch.systemsx.cisd.common.shared.basic;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Test cases for {@link AlternativesStringFilter}.
 * 
 * @author Bernd Rinn
 */
public class AlternativesStringFilterTest
{

    private static final AlternativesStringFilter prepare(String value)
    {
        final AlternativesStringFilter filter = new AlternativesStringFilter();
        filter.setFilterValue(value);
        return filter;
    }

    @Test
    public void testEmptyMatch()
    {
        final AlternativesStringFilter filter = prepare("");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleMatch()
    {
        final AlternativesStringFilter filter = prepare("middle");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleMismatch()
    {
        final AlternativesStringFilter filter = prepare("boo");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeMatch()
    {
        final AlternativesStringFilter filter = prepare("nothing middle");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeMismatch()
    {
        final AlternativesStringFilter filter = prepare("nothing either");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleNegationMatch()
    {
        final AlternativesStringFilter filter = prepare("!boo");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleNegationMismatch()
    {
        final AlternativesStringFilter filter = prepare("!end");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleStartAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("^start");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleStartAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("^middle");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleEndAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("end$");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleEndAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("middle$");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleFullAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("^start middle end$");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleFullAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("^middle$");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeNegationMatch()
    {
        final AlternativesStringFilter filter = prepare("nothing !boo");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeNegationMismatch()
    {
        final AlternativesStringFilter filter = prepare("nothing !middle");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteMatch()
    {
        final AlternativesStringFilter filter = prepare("'rt mid'");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteMismatch()
    {
        final AlternativesStringFilter filter = prepare("'rt boo'");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteStartAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("'^start mid'");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteStartAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("'^start boo'");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteEndAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("'middle end$'");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteEndAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("'boo end$'");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteFullAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("'^start middle end$'");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testSimpleQuoteFullAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("'^ middle$'");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeQuoteMatch()
    {
        final AlternativesStringFilter filter = prepare("nothing ' middle e'");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeQuoteMismatch()
    {
        final AlternativesStringFilter filter = prepare("nothing 'boo end'");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeQuoteStartAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("nothing '^start middle e'");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeQuoteStartAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("nothing '^middle end'");
        assertFalse(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeQuoteStartAnchorMismatchNormalMatch()
    {
        final AlternativesStringFilter filter = prepare("'^middle end' art");
        assertTrue(filter.passes("start middle end"));
    }

    @Test
    public void testAlternativeEscapedQuoteStartAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("nothing ^\\'start");
        assertTrue(filter.passes("'start middle end'"));
    }

    @Test
    public void testAlternativeEscapedQuoteMismatch()
    {
        final AlternativesStringFilter filter = prepare("nothing \\'^start");
        assertFalse(filter.passes("'start middle end'"));
    }

    @Test
    public void testSimpleEscapedStartAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("\\^middle");
        assertTrue(filter.passes("start ^middle end'"));
    }

    @Test
    public void testSimpleEscapedStartAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("\\^middle");
        assertFalse(filter.passes("start middle end'"));
    }

    @Test
    public void testSimpleEscapedEndAnchorMatch()
    {
        final AlternativesStringFilter filter = prepare("middle\\$");
        assertTrue(filter.passes("start ^middle$ end'"));
    }

    @Test
    public void testSimpleEscapedEndAnchorMismatch()
    {
        final AlternativesStringFilter filter = prepare("middle\\$");
        assertFalse(filter.passes("start middle end'"));
    }

    @Test
    public void testSimpleEscapedNegationMatch()
    {
        final AlternativesStringFilter filter = prepare("\\!middle");
        assertTrue(filter.passes("start !middle end'"));
    }

    @Test
    public void testSimpleEscapedNegationMismatch()
    {
        final AlternativesStringFilter filter = prepare("\\!middle");
        assertFalse(filter.passes("start middle end'"));
    }

    @Test
    public void testSimpleEscapedEverythingMatch()
    {
        final AlternativesStringFilter filter = prepare("\\!\\^middle\\$");
        assertTrue(filter.passes("start !^middle$ end'"));
    }

    @Test
    public void testSimpleEscapedEverythingFullMatch()
    {
        final AlternativesStringFilter filter = prepare("'^start !^middle$ end\\'$'");
        assertTrue(filter.passes("start !^middle$ end'"));
    }

    @Test
    public void testSimpleEscapedEverythingFullMismatch()
    {
        final AlternativesStringFilter filter = prepare("'!^start !^middle$ end$'");
        assertFalse(filter.passes("start !^middle$ end"));
    }

    // numeric

    @Test
    public void testNumericMatch()
    {
        final AlternativesStringFilter integerFilter = prepare("<10");
        assertTrue(integerFilter.passes("5"));
        assertTrue(integerFilter.passes("9.9"));
        assertFalse(integerFilter.passes("10"));
        assertFalse(integerFilter.passes("10.0"));
        assertFalse(integerFilter.passes("10.1"));
        assertFalse(integerFilter.passes("11"));
        final AlternativesStringFilter realFilter = prepare("<9.8");
        assertTrue(realFilter.passes("5"));
        assertTrue(realFilter.passes("9.7"));
        assertFalse(realFilter.passes("9.8"));
        assertFalse(realFilter.passes("9.9"));
        assertFalse(realFilter.passes("10"));
    }

    @Test
    public void testAlternativeNumericMatch()
    {
        final AlternativesStringFilter filter = prepare("<10 >15 abc");
        assertTrue(filter.passes("5"));
        assertTrue(filter.passes("20"));
        assertTrue(filter.passes("abcd"));
        assertFalse(filter.passes("ab"));
        assertFalse(filter.passes("11"));
    }

    @Test
    public void testNonNumericMatch()
    {
        AlternativesStringFilter filter = prepare("<1>0");
        assertTrue(filter.passes("<1>0"));
        assertFalse(filter.passes("0.5"));

        filter = prepare("==10");
        assertTrue(filter.passes("==10"));
        assertFalse(filter.passes("10"));

        filter = prepare("<1a0");
        assertTrue(filter.passes("<1a0"));
        assertFalse(filter.passes("1a0"));

        filter = prepare("\\<10");
        assertTrue(filter.passes("<10"));
        assertFalse(filter.passes("10"));
    }

    @Test
    public void testLowerThan()
    {
        final AlternativesStringFilter filter = prepare("<10");
        assertTrue(filter.passes("5"));
        assertTrue(filter.passes("9.9"));

        assertFalse(filter.passes("10"));
        assertFalse(filter.passes("10.1"));
        assertFalse(filter.passes("11"));

        assertFalse(filter.passes("1abc"));
        assertFalse(filter.passes("<10"));
    }

    @Test
    public void testLowerEqual()
    {
        final AlternativesStringFilter filter = prepare("<=10");
        assertTrue(filter.passes("5"));
        assertTrue(filter.passes("9.9"));
        assertTrue(filter.passes("10"));

        assertFalse(filter.passes("10.1"));
        assertFalse(filter.passes("11"));

        assertFalse(filter.passes("1abc"));
        assertFalse(filter.passes("<=10"));
    }

    @Test
    public void testGreaterThan()
    {
        AlternativesStringFilter filter = prepare(">10");
        assertFalse(filter.passes("5"));
        assertFalse(filter.passes("9.9"));
        assertFalse(filter.passes("10"));

        assertTrue(filter.passes("10.1"));
        assertTrue(filter.passes("11"));

        assertFalse(filter.passes("1abc"));
        assertFalse(filter.passes(">10"));
    }

    @Test
    public void testGreaterEqual()
    {
        AlternativesStringFilter filter = prepare(">=10");
        assertFalse(filter.passes("5"));
        assertFalse(filter.passes("9.9"));

        assertTrue(filter.passes("10"));
        assertTrue(filter.passes("10.1"));
        assertTrue(filter.passes("11"));

        assertFalse(filter.passes("1abc"));
        assertFalse(filter.passes(">=10"));
    }

    @Test
    public void testEqual()
    {
        AlternativesStringFilter filter = prepare("=10.3");
        assertFalse(filter.passes("5"));
        assertFalse(filter.passes("9.9"));

        assertTrue(filter.passes("10.3"));

        assertFalse(filter.passes("10.33"));
        assertFalse(filter.passes("11"));

        assertFalse(filter.passes("1abc"));
        assertFalse(filter.passes("=10.3"));
    }

    @Test
    public void testNotEqual()
    {
        AlternativesStringFilter filter = prepare("!=10.3");
        assertTrue(filter.passes("5"));
        assertTrue(filter.passes("9.9"));

        assertFalse(filter.passes("10.3"));

        assertTrue(filter.passes("10.33"));
        assertTrue(filter.passes("11"));

        assertTrue(filter.passes("1abc"));
        assertTrue(filter.passes("=10.3"));
        assertTrue(filter.passes("!=10.3"));
    }

}
