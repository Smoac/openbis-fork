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

package ch.systemsx.cisd.cifex.client.application.grid;

import static org.testng.AssertJUnit.*;

import java.util.Collection;
import java.util.Map;

import org.testng.annotations.Test;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Test cases for {@link ColumnFilter}.
 *
 * @author Bernd Rinn
 */
public class ColumnFilterTest
{

    static class ConstantModelData implements ModelData
    {
        
        private final String s;
        
        ConstantModelData(String s)
        {
            this.s = s;
        }

        @SuppressWarnings("unchecked")
        public <X> X get(String property)
        {
            return (X) s;
        }
        
        public Map<String, Object> getProperties()
        {
            throw new UnsupportedOperationException();
        }

        public Collection<String> getPropertyNames()
        {
            throw new UnsupportedOperationException();
        }

        public <X> X remove(String property)
        {
            throw new UnsupportedOperationException();
        }

        public <X> X set(String property, X value)
        {
            throw new UnsupportedOperationException();
        }

    }

    private final String DUMMY = "DUMMY";
    
    private final ColumnFilter prepare(String value)
    {
        final ColumnFilter filter = new ColumnFilter(null, DUMMY);
        filter.setFilterValue(value);
        return filter;
    }
    
    private final ModelData record(String value)
    {
        return new ConstantModelData(value);
    }
    
    @Test
    public void testEmptyMatch()
    {
        final ColumnFilter filter = prepare("");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleMatch()
    {
        final ColumnFilter filter = prepare("middle");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleMismatch()
    {
        final ColumnFilter filter = prepare("boo");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeMatch()
    {
        final ColumnFilter filter = prepare("nothing middle");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeMismatch()
    {
        final ColumnFilter filter = prepare("nothing either");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleNegationMatch()
    {
        final ColumnFilter filter = prepare("!boo");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleNegationMismatch()
    {
        final ColumnFilter filter = prepare("!end");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleStartAnchorMatch()
    {
        final ColumnFilter filter = prepare("^start");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleStartAnchorMismatch()
    {
        final ColumnFilter filter = prepare("^middle");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleEndAnchorMatch()
    {
        final ColumnFilter filter = prepare("end$");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleEndAnchorMismatch()
    {
        final ColumnFilter filter = prepare("middle$");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleFullAnchorMatch()
    {
        final ColumnFilter filter = prepare("^start middle end$");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleFullAnchorMismatch()
    {
        final ColumnFilter filter = prepare("^middle$");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeNegationMatch()
    {
        final ColumnFilter filter = prepare("nothing !boo");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeNegationMismatch()
    {
        final ColumnFilter filter = prepare("nothing !middle");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteMatch()
    {
        final ColumnFilter filter = prepare("'rt mid'");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteMismatch()
    {
        final ColumnFilter filter = prepare("'rt boo'");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteStartAnchorMatch()
    {
        final ColumnFilter filter = prepare("'^start mid'");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteStartAnchorMismatch()
    {
        final ColumnFilter filter = prepare("'^start boo'");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteEndAnchorMatch()
    {
        final ColumnFilter filter = prepare("'middle end$'");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteEndAnchorMismatch()
    {
        final ColumnFilter filter = prepare("'boo end$'");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteFullAnchorMatch()
    {
        final ColumnFilter filter = prepare("'^start middle end$'");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testSimpleQuoteFullAnchorMismatch()
    {
        final ColumnFilter filter = prepare("'^ middle$'");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeQuoteMatch()
    {
        final ColumnFilter filter = prepare("nothing ' middle e'");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeQuoteMismatch()
    {
        final ColumnFilter filter = prepare("nothing 'boo end'");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeQuoteStartAnchorMatch()
    {
        final ColumnFilter filter = prepare("nothing '^start middle e'");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeQuoteStartAnchorMismatch()
    {
        final ColumnFilter filter = prepare("nothing '^middle end'");
        assertFalse(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeQuoteStartAnchorMismatchNormalMatch()
    {
        final ColumnFilter filter = prepare("'^middle end' art");
        assertTrue(filter.passes(record("start middle end")));
    }

    @Test
    public void testAlternativeEscapedQuoteStartAnchorMatch()
    {
        final ColumnFilter filter = prepare("nothing ^\\'start");
        assertTrue(filter.passes(record("'start middle end'")));
    }

    @Test
    public void testAlternativeEscapedQuoteMismatch()
    {
        final ColumnFilter filter = prepare("nothing \\'^start");
        assertFalse(filter.passes(record("'start middle end'")));
    }

    @Test
    public void testSimpleEscapedStartAnchorMatch()
    {
        final ColumnFilter filter = prepare("\\^middle");
        assertTrue(filter.passes(record("start ^middle end'")));
    }

    @Test
    public void testSimpleEscapedStartAnchorMismatch()
    {
        final ColumnFilter filter = prepare("\\^middle");
        assertFalse(filter.passes(record("start middle end'")));
    }

    @Test
    public void testSimpleEscapedEndAnchorMatch()
    {
        final ColumnFilter filter = prepare("middle\\$");
        assertTrue(filter.passes(record("start ^middle$ end'")));
    }

    @Test
    public void testSimpleEscapedEndAnchorMismatch()
    {
        final ColumnFilter filter = prepare("middle\\$");
        assertFalse(filter.passes(record("start middle end'")));
    }

    @Test
    public void testSimpleEscapedNegationMatch()
    {
        final ColumnFilter filter = prepare("\\!middle");
        assertTrue(filter.passes(record("start !middle end'")));
    }

    @Test
    public void testSimpleEscapedNegationMismatch()
    {
        final ColumnFilter filter = prepare("\\!middle");
        assertFalse(filter.passes(record("start middle end'")));
    }

    @Test
    public void testSimpleEscapedEverythingMatch()
    {
        final ColumnFilter filter = prepare("\\!\\^middle\\$");
        assertTrue(filter.passes(record("start !^middle$ end'")));
    }

    @Test
    public void testSimpleEscapedEverythingFullMatch()
    {
        final ColumnFilter filter = prepare("'^start !^middle$ end\\'$'");
        assertTrue(filter.passes(record("start !^middle$ end'")));
    }

    @Test
    public void testSimpleEscapedEverythingFullMismatch()
    {
        final ColumnFilter filter = prepare("'!^start !^middle$ end$'");
        assertFalse(filter.passes(record("start !^middle$ end")));
    }

}
