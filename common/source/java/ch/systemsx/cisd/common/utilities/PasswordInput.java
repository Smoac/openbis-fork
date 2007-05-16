/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper method for reading unvisible characters from <code>System.in</code>.
 * Code adapted from <a href="http://java.sun.com/developer/technicalArticles/Security/pwordmask/">Password Masking in 
 * the Java Programming Language</a> by Qusay H. Mahmoud.
 * 
 * Note: This is a hack! Sometimes characters become visible.
 *
 */
public class PasswordInput
{
    private static class Eraser implements Runnable
    {
        private volatile boolean stop;

        public void run()
        {
            int priority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try
            {
                stop = true;
                while (stop)
                {
                    System.out.print("\010*");
                    try
                    {
                        Thread.sleep(1);
                    } catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } finally
            {
                Thread.currentThread().setPriority(priority);
            }
        }

        void stopMasking()
        {
            this.stop = false;
        }
    }
    
    /**
     * Read non-echoed characters from the console until 'enter' has been pressed.
     */
    public static String readPassword()
    {
        Eraser eraser = new Eraser();
        Thread mask = new Thread(eraser);
        mask.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String password = "";

        try
        {
            password = in.readLine();
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        eraser.stopMasking();
        return password;
    }

    public static void main(String[] args)
    {
        System.out.print("enter: ");
        System.out.println(PasswordInput.readPassword());
    }
}
