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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ImageViewer
{
    
    private static final class DataSetAndWells
    {
        private final String dataSetCode;
        private final List<String> wells = new ArrayList<String>();
        
        DataSetAndWells(String description)
        {
            int indexOfColon = description.indexOf(':');
            if (indexOfColon < 0)
            {
                dataSetCode = description;
            } else
            {
                dataSetCode = description.substring(0, indexOfColon);
                wells.addAll(Arrays.asList(StringUtils.split(description.substring(indexOfColon + 1))));
            }
        }

        public final String getDataSetCode()
        {
            return dataSetCode;
        }

        public final List<String> getWells()
        {
            return wells;
        }
    }

    public static void main(String[] args)
    {
        String serviceURL = args[0];
        String sessionToken = args[1];
        long experimentID = Long.parseLong(args[2]);
        String channel = args[3];
        Map<String, DataSetAndWells> dataSets = new HashMap<String, DataSetAndWells>();
        for (int i = 4; i < args.length; i++)
        {
            DataSetAndWells dataSetAndWells = new DataSetAndWells(args[i]);
            dataSets.put(dataSetAndWells.getDataSetCode(), dataSetAndWells);
        }
        JFrame frame = new JFrame("Image Viewer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        contentPane.add(content);
        ImageSize thumbnailSize = new ImageSize(200, 160);
        try
        {
            IScreeningOpenbisServiceFacade facade = ScreeningOpenbisServiceFacadeFactory.tryCreate(sessionToken, serviceURL);
            List<IDatasetIdentifier> dsIdentifier = facade.getDatasetIdentifiers(new ArrayList<String>(dataSets.keySet()));
            for (IDatasetIdentifier identifier : dsIdentifier)
            {
                List<byte[]> imageBytes =
                        facade.loadImages(identifier, dataSets.get(identifier.getDatasetCode())
                                .getWells(), channel, thumbnailSize);
                for (byte[] bytes : imageBytes)
                {
                    content.add(new JLabel(new ImageIcon(bytes)));
                }
            }
        } catch (Exception ex)
        {
            content.add(new JLabel(ex.toString()));
        }
        
        frame.pack();
        frame.setVisible(true);
    }

}
