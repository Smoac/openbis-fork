/*
 * Copyright 2008 ETH Zuerich, CISD
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
package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

public class AuthorizationBean extends TimerTask
{
    //
    // Singleton Pattern
    //
    
    private static final AuthorizationBean instance;
    
    private AuthorizationBean() { }
    
    public static AuthorizationBean getInstance() {
        return instance;
    }
    
    //
    // Fields
    //
    
    private boolean isASDisabled = false;
    private String disabledText = null;

    public boolean isASDisabled()
    {
        return this.isASDisabled;
    }

    public String getDisabledText()
    {
        return this.disabledText;
    }

    private void setState(boolean isASDisabled, String disabledText)
    {
        this.isASDisabled = isASDisabled;
        this.disabledText = disabledText;
    }

    //
    // Update Fields checking a file
    //
    
    private static final long DELAY_BETWEEN_CHECK = 30000L;
    private final File filePath = new File("./etc/nologin");
    
    static {
        instance = new AuthorizationBean();
        Timer timer = new Timer();
        timer.schedule(instance, 0L, DELAY_BETWEEN_CHECK);
    }
    
    @Override
    public void run()
    {
        if(filePath.exists()) {
            FileInputStream fis = null;
            String disabledText = null;
            try
            {
                fis = new FileInputStream(filePath);
                byte[] data = new byte[(int) filePath.length()];
                fis.read(data);
                fis.close();
                disabledText = new String(data, "UTF-8");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            setState(true, disabledText);
        } else {
            setState(false, null);
        }
    }
}
