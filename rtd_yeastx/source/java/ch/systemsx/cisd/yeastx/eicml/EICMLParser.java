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

package ch.systemsx.cisd.yeastx.eicml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;


/**
 * A file for parsing <code>eicML</code> files.
 * 
 * @author Bernd Rinn
 */
public class EICMLParser extends DefaultHandler
{

    /** A role that observes {@Link MSRun}s. */
    public interface IMSRunObserver
    {
        void observe(MSRunDTO run);
    }

    /** A role that observes {@Link Chromatogram}s. */
    public interface IChromatogramObserver
    {
        void observe(ChromatogramDTO chromatogram);
    }

    private StringBuilder buffer = new StringBuilder();

    private String permIdOrNull;
    
    private MSRunDTO msRun;

    private ChromatogramDTO chromatogram;

    private boolean parsingMsRun;

    private boolean parsingChromatogram;

    private final IMSRunObserver msRunObserverOrNull;

    private final IChromatogramObserver chromatogramObserverOrNull;

    public EICMLParser(String fileName, String permIdOrNull, IMSRunObserver msRunObserverOrNull,
            IChromatogramObserver chromatogramObserverOrNull) throws ParserConfigurationException,
            SAXException, IOException
    {
        assert fileName != null;

        this.permIdOrNull = permIdOrNull;
        this.msRunObserverOrNull = msRunObserverOrNull;
        this.chromatogramObserverOrNull = chromatogramObserverOrNull;
        parseDocument(fileName);
    }

    private void parseDocument(String fileName) throws ParserConfigurationException, SAXException,
            IOException
    {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        final SAXParser sp = spf.newSAXParser();
        sp.parse(fileName, this);
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException
    {
        buffer.setLength(0);
        if ("msRun".equals(name))
        {
            msRun = new MSRunDTO();
            msRun.permId = permIdOrNull;
            parsingMsRun = true;
        } else if ("chromatogram".equals(name))
        {
            chromatogram = new ChromatogramDTO();
            parsingChromatogram = true;
        }
    }

    private float[] convert(String b64)
    {
        final byte[] decoded = Base64.decodeBase64(b64.getBytes());
        return NativeData.byteToFloat(decoded, ByteOrder.BIG_ENDIAN);
    }

    void set(String name, String value) throws SAXException
    {
        if ("Q1Mz".equals(name) && value.length() > 0)
        {
            chromatogram.q1Mz = Float.parseFloat(value);
        } else if ("Q3LowMz".equals(name) && value.length() > 0)
        {
            chromatogram.q3LowMz = Float.parseFloat(value);
        } else if ("Q3HighMz".equals(name) && value.length() > 0)
        {
            chromatogram.q3HighMz = Float.parseFloat(value);
        } else if ("label".equals(name) && value.length() > 0)
        {
            chromatogram.label = value;
        } else if ("polarity".equals(name) && value.length() > 0)
        {
            if (value.length() != 1)
            {
                throw new SAXException("Illegal polarity: must be of length 1");
            }
            chromatogram.polarity = value.charAt(0);
        } else if ("RT".equals(name) && value.length() > 0)
        {
            chromatogram.runTimes = convert(value);
        } else if ("INT".equals(name) && value.length() > 0)
        {
            chromatogram.intensities = convert(value);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException
    {
        if ("msRun".equals(name))
        {
            parsingMsRun = false;
            if (msRunObserverOrNull != null)
            {
                msRunObserverOrNull.observe(msRun);
            }
            msRun = null;
        } else if ("chromatogram".equals(name))
        {
            parsingChromatogram = false;
            if (chromatogramObserverOrNull != null)
            {
                chromatogramObserverOrNull.observe(chromatogram);
            }
            chromatogram = null;
        }
        if (parsingMsRun && msRun != null)
        {
            msRun.set(name, buffer.toString());
        } else if (parsingChromatogram && chromatogram != null)
        {
            set(name, buffer.toString());
        }
        buffer.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        buffer.append(new String(ch, start, length));
    }

}
