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

package ch.systemsx.cisd.yeastx.mzxml;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzInstrumentDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPeaksDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPrecursorDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzRunDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzScanDTO;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = MzXml2Database.class)
public class MzXml2DatabaseTest
{
    /**
     * Tests the logic which adds mzXML run to the database, does not test xml parsing or adding a
     * dataset/experiment/sample.
     */
    @Test
    public void testUpload()
    {
        Mockery context = new Mockery();
        final DMDataSetDTO dataSet = new DMDataSetDTO();
        final IMzXmlDAO dao = context.mock(IMzXmlDAO.class);

        MzRunDTO run = new MzRunDTO();
        final MzInstrumentDTO instrument = new MzInstrumentDTO();
        run.setInstrument(instrument);
        final MzScanDTO scan = new MzScanDTO();
        final MzPrecursorDTO precursor = new MzPrecursorDTO();
        scan.setPrecursors(Arrays.asList(precursor, precursor));
        MzPeaksDTO peaks = new MzPeaksDTO();
        final float mz = 1.11F;
        final float intensity = 2.22F;
        peaks.setPeaks(NativeData.floatToByte(new float[]
            { mz, intensity }, ByteOrder.BIG_ENDIAN));
        scan.setPeaksBytes(peaks);
        List<MzScanDTO> scans = Arrays.asList(scan, scan);
        run.setScans(scans);

        context.checking(new Expectations()
            {
                {
                    one(dao).addRun(dataSet, instrument);
                    long runId = 222;
                    will(returnValue(runId));

                    exactly(2).of(dao).addScan(runId, scan, precursor, precursor);
                    long scanId = 333;
                    will(returnValue(scanId));

                    exactly(2).of(dao).addPeaks(with(scanId), with(createFloatIteratorMatcher(mz)),
                            with(createFloatIteratorMatcher(intensity)));
                }

                // checks that iterator has exactly one value
                private Matcher<Iterable<Float>> createFloatIteratorMatcher(final float value)
                {
                    return new BaseMatcher<Iterable<Float>>()
                        {
                            public boolean matches(Object item)
                            {
                                Iterator<Float> iterator = asIterator(item);
                                Float number = iterator.next();
                                return number.equals(value) && iterator.hasNext() == false;
                            }

                            @SuppressWarnings("unchecked")
                            private Iterator<Float> asIterator(Object item)
                            {
                                return ((Iterable<Float>) item).iterator();
                            }

                            public void describeTo(Description description)
                            {
                                description.appendValue("iterator with one value '" + value + "'");
                            }
                        };
                }
            });

        new MzXml2Database(dao).uploadRun(run, dataSet);
        context.assertIsSatisfied();
    }
}
