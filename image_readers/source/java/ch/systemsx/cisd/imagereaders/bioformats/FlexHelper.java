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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import loci.formats.tiff.IFDList;
import loci.formats.tiff.TiffParser;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.geometry.SpatialPoint;

/**
 * A helper class with utility methods that parse metadata from FLEX files.
 * <p>
 * Clients can implement their own utility methods on the top of FlexHelper, by parsing the 
 * metadata XML as returned by {@link #getMetadata()} or by directly executing XPath query 
 * via {@link #selectByXpathQuery(String)}.
 * 
 * @author Kaloyan Enimanev
 */
public class FlexHelper
{

    /** Custom IFD entry for Flex XML. */
    private static final int FLEX = 65200;

    private static final String SELECT_CHANNELS = "//Arrays/Array/@Name";

    private static final String SELECT_TILE_FOR_IMAGE =
            "//Images/Image[@BufferNo='%s']/Sublayout/text()";

    private static final String SELECT_TILE_XCOORDS = "//Sublayouts/Sublayout/Field/OffsetX/text()";

    private static final String SELECT_TILE_YCOORDS = "//Sublayouts/Sublayout/Field/OffsetY/text()";

    private final String fileName;

    private final String metadataXML;

    private final Document metadata;

    public FlexHelper(String fileName)
    {
        this.fileName = fileName;
        try
        {
            this.metadataXML = readMetadata();
            this.metadata = parseXmlDocument(metadataXML);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int getTileNumber(int imageIdx)
    {
        String query = String.format(SELECT_TILE_FOR_IMAGE, imageIdx);
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        String tile = nodeList.item(0).getNodeValue();
        return Integer.parseInt(tile.trim());
    }
    
    public String getChannelCode(int imageIdx)
    {
        NodeList nodeList = selectByXpathQuery(SELECT_CHANNELS);
        if (imageIdx < 0 || nodeList.getLength() <= imageIdx)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        return nodeList.item(imageIdx).getNodeValue();
    }

    public Map<Integer, SpatialPoint> getTileCoordinates()
    {
        Map<Integer, SpatialPoint> points = new HashMap<Integer, SpatialPoint>();
        List<Double> xCoords = selectDoubleList(SELECT_TILE_XCOORDS);
        List<Double> yCoords = selectDoubleList(SELECT_TILE_YCOORDS);
        for (int i = 0; i < xCoords.size(); i++)
        {
            int tile = i + 1;
            SpatialPoint point = new SpatialPoint(xCoords.get(i), yCoords.get(i));
            points.put(tile, point);
        }
        return points;
    }

    /**
     * Return the raw metadata as String, so that users can extend the API of the helper if needed.
     */
    public String getMetadata()
    {
        return metadataXML;
    }

    /**
     * Execute an XPath query directly on the metadata DOM.
     */
    public NodeList selectByXpathQuery(String xpathQuery)
    {
        try
        {
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(xpathQuery);
            return (NodeList) expr.evaluate(metadata, XPathConstants.NODESET);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private List<Double> selectDoubleList(String query)
    {
        NodeList nodeList = selectByXpathQuery(query);
        List<Double> result = new ArrayList<Double>();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            String stringValue = nodeList.item(i).getNodeValue();
            Double doubleValue = Double.parseDouble(stringValue.trim());
            result.add(doubleValue);
        }
        return result;
    }

    private String readMetadata() throws Exception
    {
        TiffParser tiffParser = new TiffParser(fileName);
        final IFDList ifds = tiffParser.getIFDs();
        if (ifds != null && ifds.get(0) != null)
        {
            return ifds.get(0).getIFDStringValue(FLEX);
        } else
        {
            throw new IllegalArgumentException("Cannot parse Flex XML metadata from file "
                    + fileName);
        }
    }

    /**
     * Parse given string as XML {@link Document}.
     * 
     * @throws UserFailureException if provided value is not a well-formed XML document
     */
    private Document parseXmlDocument(String value)
    {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setNamespaceAware(true);
        InputSource is = new InputSource(new StringReader(value));
        try
        {
            return dBF.newDocumentBuilder().parse(is);
        } catch (Exception e)
        {
            final String errorMessage = String.format(
                    "Failed to parse FLEX metadata :\n\n%s\n\nisn't a well formed XML document. %s",
                    value, e.getMessage());
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
