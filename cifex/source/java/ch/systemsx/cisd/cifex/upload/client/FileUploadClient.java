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

package ch.systemsx.cisd.cifex.upload.client;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ch.systemsx.cisd.cifex.upload.IUploadService;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FileUploadClient implements IUploadListener
{
    private static final String TITLE = "CIFEX Uploader";

    private static final int SERVER_TIMEOUT_MIN = 5;
    
    private final Uploader uploader;

    private JFrame frame;

    private JPanel panel;

    private JProgressBar progressBar;
    
    FileUploadClient(String serviceURL, String uploadSessionID)
    {
        uploader = new Uploader(createServiceStub(serviceURL), uploadSessionID);
        uploader.addUploadListener(this);
    }
    
    private IUploadService createServiceStub(String serviceURL)
    {
        final HttpInvokerProxyFactoryBean httpInvokerProxy = new HttpInvokerProxyFactoryBean();
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(IUploadService.class);
        final CommonsHttpInvokerRequestExecutor httpInvokerRequestExecutor =
                new CommonsHttpInvokerRequestExecutor();
        httpInvokerRequestExecutor.setReadTimeout((int) DateUtils.MILLIS_PER_MINUTE
                * SERVER_TIMEOUT_MIN);
        httpInvokerProxy.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        httpInvokerProxy.afterPropertiesSet();
        return (IUploadService) httpInvokerProxy.getObject();
    }

    public void uploadingStarted(File file)
    {
        String path = file.getAbsolutePath();
        frame.setTitle(TITLE + ": " + path);
        panel.removeAll();
        panel.add(new JLabel("Uploading " + path));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel.add(progressBar);
        panel.invalidate();
        panel.getParent().validate();
    }
    
    public void uploadingFinished()
    {
        // TODO Auto-generated method stub
        
    }

    public void uploadingProgress(int percentage, long numberOfBytes)
    {
        progressBar.setValue(percentage);
        progressBar.setString(Long.toString(numberOfBytes));
    }

    void upload()
    {
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(createGUI(), BorderLayout.CENTER);
        frame.setBounds(200, 200, 600, 300);
        frame.setVisible(true);
        uploader.upload();
    }
    
    private JPanel createGUI()
    {
        panel = new JPanel();
        return panel;
    }
    
    public static void main(String[] args)
    {
        System.out.println("arguments: " + Arrays.asList(args));
        new FileUploadClient(args[0], args[1]).upload();
    }

}
