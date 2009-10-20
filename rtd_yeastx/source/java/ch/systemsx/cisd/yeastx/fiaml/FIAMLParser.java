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

package ch.systemsx.cisd.yeastx.fiaml;

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
import ch.systemsx.cisd.yeastx.utils.XmlDateAdapter;

/**
 * A file for parsing <code>eicML</code> files.
 * 
 * @author Bernd Rinn
 */
public class FIAMLParser extends DefaultHandler
{
    private final static String FIA_RUN = "fiaRun";

    /** A role that observes {@link FIAMSRunDTO}s. */
    public interface IMSRunObserver
    {
        void observe(FIAMSRunDTO run, FIAMSRunDataDTO runData);
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

    private FIAMSRunDTO msRun;

    private FIAMSRunDataDTO fiaRunData;

    private boolean parsingMsRun;

    private final IMSRunObserver msRunObserverOrNull;

    public FIAMLParser(String fileName, IMSRunObserver msRunObserverOrNull)
            throws ParserConfigurationException, SAXException, IOException
    {
        assert fileName != null;

        this.msRunObserverOrNull = msRunObserverOrNull;
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
        if (FIA_RUN.equals(name))
        {
            msRun = new FIAMSRunDTO();
            fiaRunData = new FIAMSRunDataDTO();
            parsingMsRun = true;
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
        } else if ("polarity".equals(name) || "methodPolarity".equals(name))
        {
            if (value.length() != 1)
            {
                throw new SAXException("Illegal polarity: must be of length 1");
            }
            msRun.setPolarity(value.charAt(0));
        } else if ("lowMz".equals(name))
        {
            msRun.setLowMz(Float.parseFloat(value));
        } else if ("highMz".equals(name))
        {
            msRun.setHighMz(Float.parseFloat(value));
        } else if ("is".equals(name))
        {
            msRun.setInternalStandard(Float.parseFloat(value));
        } else if ("od".equals(name))
        {
            msRun.setOd(Float.parseFloat(value));
        } else if ("operator".equals(name))
        {
            msRun.setOperator(value);
        } else if ("acquisitionDate".equals(name) && StringUtils.isNotBlank(value))
        {
            try
            {
                msRun.setAcquisitionDate(getDateFormat().parse(value));
            } catch (ParseException ex)
            {
                throw new SAXException("Error parsing date: " + value);
            }
        } else if ("profileMz".equals(name))
        {
            fiaRunData.setProfileMz(convert(value));
        } else if ("profileInt".equals(name))
        {
            fiaRunData.setProfileIntensities(convert(value));
        } else if ("centroidMz".equals(name))
        {
            fiaRunData.setCentroidMz(convert(value));
        } else if ("centroidInt".equals(name))
        {
            fiaRunData.setCentroidIntensities(convert(value));
        } else if ("centroidCorr".equals(name))
        {
            fiaRunData.setCentroidCorrelations(convert(value));
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException
    {
        if (FIA_RUN.equals(name))
        {
            parsingMsRun = false;
            if (msRunObserverOrNull != null)
            {
                msRunObserverOrNull.observe(msRun, fiaRunData);
            }
            msRun = null;
            fiaRunData = null;
        }
        if (parsingMsRun && msRun != null && fiaRunData != null)
        {
            setMsRun(name, buffer.toString());
        }
        buffer.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        buffer.append(new String(ch, start, length));
    }

}
