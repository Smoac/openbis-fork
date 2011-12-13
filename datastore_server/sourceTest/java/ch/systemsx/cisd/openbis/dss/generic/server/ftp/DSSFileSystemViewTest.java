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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.jmock.Expectations;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.TrackingMockery;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFile;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DSSFileSystemViewTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "session";
    
    private TrackingMockery context;
    private IETLLIMSService service;
    private IGeneralInformationService infoService;
    private IFtpPathResolverRegistry registry;
    private DSSFileSystemView view;

    @BeforeMethod
    public void setUp() throws FtpException
    {
        context = new TrackingMockery();
        service = context.mock(IETLLIMSService.class);
        infoService = context.mock(IGeneralInformationService.class);
        registry = context.mock(IFtpPathResolverRegistry.class);
        prepareTryResolve("/");
        view = new DSSFileSystemView(SESSION_TOKEN, service, infoService, registry);
    }
    
    @AfterMethod(alwaysRun = true)
    public void tearDown(Method m)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            throw new Error(m.getName() + "() : ", t);
        }
    }
    
    @Test
    public void testGetRoot() throws FtpException
    {
        FtpFile file = view.getFile("./");
        
        assertEquals("/", file.getAbsolutePath());
    }
    
    @Test
    public void testGetFile() throws FtpException
    {
        prepareTryResolve("/abc/ghi/jkl");
        
        FtpFile file = view.getFile("abc/def/../ghi//jkl//");
        
        assertEquals("/abc/ghi/jkl", file.getAbsolutePath());
    }
    
    @Test
    public void testChangeDirAndGetFile() throws FtpException
    {
        prepareTryResolve("/abc");
        prepareTryResolve("/abc/def/ghi");
        
        view.changeWorkingDirectory("abc");
        
        FtpFile file = view.getFile("def/ghi/");
        
        assertEquals("/abc/def/ghi", file.getAbsolutePath());
    }
    
    private void prepareTryResolve(final String normalizedPath)
    {
        context.checking(new Expectations()
            {
                {
                    RecordingMatcher<FtpPathResolverContext> recorder = new RecordingMatcher<FtpPathResolverContext>();
                    one(registry).tryResolve(with(normalizedPath), with(recorder));
                    will(returnValue(new AbstractFtpFile(normalizedPath)
                        {
                            
                            public boolean isFile()
                            {
                                return false;
                            }
                            
                            public boolean isDirectory()
                            {
                                return true;
                            }
                            
                            public long getSize()
                            {
                                return 0;
                            }
                            
                            public long getLastModified()
                            {
                                return 0;
                            }
                            
                            public InputStream createInputStream(long offset) throws IOException
                            {
                                return null;
                            }
                            
                            @Override
                            public List<FtpFile> unsafeListFiles() throws RuntimeException
                            {
                                return null;
                            }
                        }));
                }
            });
    }

}
