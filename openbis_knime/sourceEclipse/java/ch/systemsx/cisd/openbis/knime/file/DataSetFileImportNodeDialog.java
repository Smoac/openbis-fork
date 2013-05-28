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

package ch.systemsx.cisd.openbis.knime.file;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.knime.common.AbstractDescriptionBasedNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.EntityChooser;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * Node for downloading a file of an openBIS data set.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetFileImportNodeDialog extends AbstractDescriptionBasedNodeDialog<ReportDescription>
{
    private static boolean isBlank(String text)
    {
        return text == null || text.trim().length() == 0;
    }

    private JTextComponent dataSetCodeField;
    private JTextComponent filePathField;
    private JTextComponent downloadsPathField;
    private JCheckBox reuseCheckBox;

    DataSetFileImportNodeDialog()
    {
        super("Data Set File Importer Settings");
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel, JComboBox reportComboBox)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        dataSetCodeField = createTextFieldWithButton("Data Set Code", new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    chooseDataSet(createFacade());
                }
            }, panel);
        filePathField = createTextFieldWithButton("File", new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    chooseDataSetFile();
                }
            }, panel);
        downloadsPathField =
                createTextFieldWithButton("Location of Downloads", new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            chooseTempFolder();
                        }
                    }, panel);
        reuseCheckBox = new JCheckBox();
        addField(panel, "Reuse already downloaded file", reuseCheckBox);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(panel, BorderLayout.NORTH);
        queryPanel.add(northPanel, BorderLayout.CENTER);
    }
    
    private JTextComponent createTextFieldWithButton(String label, ActionListener actionListener,
            JPanel panel)
    {
        JPanel textFieldWithButton = new JPanel(new BorderLayout());
        JTextField field = new JTextField(20);
        textFieldWithButton.add(field, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(actionListener);
        textFieldWithButton.add(button, BorderLayout.EAST);
        addField(panel, label, textFieldWithButton);
        return field;
    }

    @Override
    protected List<ReportDescription> getSortedDescriptions(IQueryApiFacade facade)
    {
        List<ReportDescription> descriptions = facade.listTableReportDescriptions();
        Collections.sort(descriptions, new Comparator<ReportDescription>()
            {
                @Override
                public int compare(ReportDescription o1, ReportDescription o2)
                {
                    return o1.getLabel().compareTo(o2.getLabel());
                }
            });
        return descriptions;
    }
    
    @Override
    protected String getDescriptionKey()
    {
        return "";
    }

    @Override
    protected void loadMoreSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        dataSetCodeField.setText(settings.getString(DataSetFileImportNodeModel.DATA_SET_CODE_KEY,
                ""));
        filePathField.setText(settings.getString(DataSetFileImportNodeModel.FILE_PATH_KEY, ""));
        downloadsPathField.setText(settings.getString(
                DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, ""));
        reuseCheckBox.setSelected(settings.getBoolean(DataSetFileImportNodeModel.REUSE_FILE, true));
    }

    @Override
    protected void saveMoreSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        String dataSetCode = dataSetCodeField.getText();
        if (isBlank(dataSetCode))
        {
            throw new InvalidSettingsException("Data set code hasn't been specified.");
        }
        settings.addString(DataSetFileImportNodeModel.DATA_SET_CODE_KEY, dataSetCode);
        String filePath = filePathField.getText();
        if (isBlank(filePath))
        {
            throw new InvalidSettingsException("Data set file path hasn't been specified.");
        }
        settings.addString(DataSetFileImportNodeModel.FILE_PATH_KEY, filePath);
        String downloadsPath = downloadsPathField.getText();
        if (isBlank(downloadsPath))
        {
            throw new InvalidSettingsException("Location of downloads hasn't been specified.");
        }
        settings.addString(DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, downloadsPath);
        settings.addBoolean(DataSetFileImportNodeModel.REUSE_FILE, reuseCheckBox.isSelected());
    }

    private void chooseDataSet(IQueryApiFacade facade)
    {
        try
        {
            String ownerOrNull =
                    new EntityChooser(getPanel(), DataSetOwnerType.DATA_SET, true,
                            facade.getSessionToken(), facade.getGeneralInformationService())
                            .getOwnerOrNull();
            if (ownerOrNull != null)
            {
                dataSetCodeField.setText(ownerOrNull);
                filePathField.setText("");
            }
        } catch (Exception ex)
        {
            showException(ex);
        }
    }
    
    private void chooseDataSetFile()
    {
        String dataSetCode = dataSetCodeField.getText();
        if (isBlank(dataSetCode))
        {
            return;
        }

        IOpenbisServiceFacade openbisFacade = createOpenbisFacade(createFacade().getSessionToken());
        try
        {
            ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet =
                    openbisFacade.getDataSet(dataSetCode);
            if (dataSet == null)
            {
                JOptionPane.showMessageDialog(getPanel(), "Unknown data set: " + dataSetCode);
                return;
            }
            FileInfoDssDTO[] files = dataSet.listFiles("", true);
            FileChooser fileChooser = new FileChooser(dataSetCode, files);
            int result = JOptionPane.showOptionDialog(getPanel(), fileChooser, "Data Set File Chooser", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            FileInfoDssDTO fileInfo = fileChooser.getSelectedFileInfoOrNull();
            if (fileInfo != null && fileInfo.getPathInListing() != null
                    && fileInfo.isDirectory() == false && result == JOptionPane.OK_OPTION)
            {
                filePathField.setText(fileInfo.getPathInDataSet());
            }
        } catch (RuntimeException e)
        {
            showException(e);
            throw e;
        } catch (Throwable t)
        {
            showException(t);
        }
    }

    private void chooseTempFolder()
    {
        JFileChooser chooser = new JFileChooser();
        String path = downloadsPathField.getText();
        if (isBlank(path) == false)
        {
            chooser.setSelectedFile(new File(path));
        }
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.showDialog(getPanel(), "Select");
        File selectedFile = chooser.getSelectedFile();
        if (selectedFile != null)
        {
            downloadsPathField.setText(selectedFile.getPath());
        }
    }
    
}
