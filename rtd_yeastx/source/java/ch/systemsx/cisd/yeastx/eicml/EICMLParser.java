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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.common.xml.XmlDateAdapter;

/**
 * A file for parsing <code>eicML</code> files.
 * 
 * @author Bernd Rinn
 */
public class EICMLParser extends DefaultHandler
{
    private static final String MS_RUN = "msRun";

    /** A role that observes {@link EICMSRunDTO}s. */
    public interface IMSRunObserver
    {
        void observe(EICMSRunDTO run);
    }

    /** A role that observes {@link ChromatogramDTO}s. */
    public interface IChromatogramObserver
    {
        void observe(ChromatogramDTO chromatogram);
    }

    private static final ThreadLocal<DateFormat> dateFormatHolder = new ThreadLocal<DateFormat>();

    public static DateFormat getDateFormat()
    {
        DateFormat dateFormat = dateFormatHolder.get();
        if (dateFormat == null)
        {
            dateFormat = new SimpleDateFormat(XmlDateAdapter.DATE_PATTERN);
            dateFormatHolder.set(dateFormat);
        }
        return dateFormat;
    }

    private StringBuilder buffer = new StringBuilder();

    private EICMSRunDTO msRun;

    private ChromatogramDTO chromatogram;

    private boolean parsingMsRun;

    private boolean parsingChromatogram;

    private final IMSRunObserver msRunObserverOrNull;

    private final IChromatogramObserver chromatogramObserverOrNull;

    public EICMLParser(String fileName, IMSRunObserver msRunObserverOrNull,
            IChromatogramObserver chromatogramObserverOrNull) throws ParserConfigurationException,
            SAXException, IOException
    {
        assert fileName != null;

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
        if (MS_RUN.equals(name))
        {
            msRun = new EICMSRunDTO();
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

    void setMsRun(String name, String value) throws SAXException
    {
        if ("filePath".equals(name))
        {
            msRun.setRawDataFilePath(value);
        } else if ("fileName".equals(name))
        {
            msRun.setRawDataFileName(value);
        } else if ("instrumentType".equals(name))
        {
            msRun.setInstrumentType(value);
        } else if ("instrumentManufacturer".equals(name))
        {
            msRun.setInstrumentManufacturer(value);
        } else if ("instrumentModel".equals(name))
        {
            msRun.setInstrumentModel(value);
        } else if ("methodIonisation".equals(name))
        {
            msRun.setMethodIonisation(value);
        } else if ("methodSeparation".equals(name))
        {
            msRun.setMethodSeparation(value);
        } else if ("acquisitionDate".equals(name) && StringUtils.isNotBlank(value))
        {
            try
            {
                msRun.setAcquisitionDate(getDateFormat().parse(value));
            } catch (ParseException ex)
            {
                throw new SAXException("Error parsing date: " + value);
            }
        } else if ("chromCount".equals(name) && value.length() > 0)
        {
            msRun.setChromCount(Integer.parseInt(value));
        } else if ("msRunId".equals(name) && value.length() > 0)
        {
            msRun.setMsRunId(Long.parseLong(value));
        } else if ("startTime".equals(name) && value.length() > 0)
        {
            msRun.setStartTime(Float.parseFloat(value));
        } else if ("endTime".equals(name) && value.length() > 0)
        {
            msRun.setEndTime(Float.parseFloat(value));
        } else if ("setId".equals(name))
        {
            msRun.setSetId(Long.parseLong(value));
        } else if ("operator".equals(name))
        {
            msRun.setOperator(value);
        }
    }

    void setChromatogram(String name, String value) throws SAXException
    {
        if ("Q1Mz".equals(name) && value.length() > 0)
        {
            chromatogram.setQ1Mz(Float.parseFloat(value));
        } else if ("Q3LowMz".equals(name) && value.length() > 0)
        {
            chromatogram.setQ3LowMz(Float.parseFloat(value));
        } else if ("Q3HighMz".equals(name) && value.length() > 0)
        {
            chromatogram.setQ3HighMz(Float.parseFloat(value));
        } else if ("label".equals(name) && value.length() > 0)
        {
            chromatogram.setLabel(value);
        } else if ("polarity".equals(name) && value.length() > 0)
        {
            if (value.length() != 1)
            {
                throw new SAXException("Illegal polarity: must be of length 1");
            }
            chromatogram.setPolarity(value.charAt(0));
        } else if ("RT".equals(name) && value.length() > 0)
        {
            chromatogram.setRunTimes(convert(value));
        } else if ("INT".equals(name) && value.length() > 0)
        {
            chromatogram.setIntensities(convert(value));
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException
    {
        if (MS_RUN.equals(name))
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
            setMsRun(name, buffer.toString());
        } else if (parsingChromatogram && chromatogram != null)
        {
            setChromatogram(name, buffer.toString());
        }
        buffer.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        buffer.append(new String(ch, start, length));
    }

}
