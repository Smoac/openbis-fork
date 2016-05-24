/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.authentication;

import java.security.Security;

import org.alfresco.jlan.server.auth.AuthContext;
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.ClientInfoFactory;
import org.alfresco.jlan.server.auth.DefaultClientInfoFactory;
import org.alfresco.jlan.server.auth.NTLanManAuthContext;
import org.alfresco.jlan.server.auth.UserAccount;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * Utility methods for CIFS authentication. Uses Alfresco JLAN library.
 *
 * @author Franz-Josef Elmer
 */
public class CifsAuthenticationUtils
{
    static 
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    static final String CIFS_PREFIX = "$$CIFS$$";
    static enum Algorithm
    {
        LANMAN, NTLMV1, NTLMV2, MD4;
    }
    
    private static final AuthenticationHelper AUTHENTICATION_HELPER = new AuthenticationHelper();
    
    static boolean isPasswordInfo(String passwordInfo)
    {
        return tryExtractPasswordInfo(passwordInfo) != null;
    }
    
    public static String createPasswordInfo(byte[] password, int algorithm, byte[] challenge)
    {
        return createPasswordInfo(StringUtilities.asHexString(password), Algorithm.values()[algorithm], challenge);
    }
    
    public static String extractAlgorithm(String passwordInfo)
    {
        PasswordInfo info = tryExtractPasswordInfo(passwordInfo);
        return info == null ? "unknown" : info.algorithm.toString(); 
    }
    
    static String createPasswordInfo(String password, Algorithm algorithm, byte[] challenge)
    {
        return CIFS_PREFIX + password + ":" + StringUtilities.asHexString(challenge) + ":" + algorithm;
    }
    
    static String generatePassword(String user, Algorithm algorithm, byte[] challenge, String plainPassword)
    {
        byte[] password = AUTHENTICATION_HELPER.generatePassword(user, plainPassword, algorithm, challenge);
        return StringUtilities.asHexString(password);
    }

    static Principal tryGetAndAuthenticateUser(String user, String plainPassword, String passwordInfoString)
    {
        PasswordInfo passwordInfo = tryExtractPasswordInfo(passwordInfoString);
        if (passwordInfo == null)
        {
            return null;
        }
        if (AUTHENTICATION_HELPER.validatePassword(user, plainPassword, passwordInfo))
        {
            return new Principal(user, "", "", "", true);
        }
        return null;
    }
    
    private static byte[] tryConvertHexString(String hexString)
    {
        try
        {
            return StringUtilities.parseHexString(hexString);
        } catch (Exception ex)
        {
            return null;
        }
    }
    
    private static PasswordInfo tryExtractPasswordInfo(String passwordInfo)
    {
        if (passwordInfo.startsWith(CIFS_PREFIX) == false)
        {
            return null;
        }
        String[] splittedInfo = passwordInfo.substring(CifsAuthenticationUtils.CIFS_PREFIX.length()).split(":");
        if (splittedInfo.length != 3)
        {
            return null;
        }
        byte[] password = tryConvertHexString(splittedInfo[0]);
        byte[] challenge = tryConvertHexString(splittedInfo[1]);
        Algorithm algorithm = tryConvert(splittedInfo[2]);
        if (password == null || algorithm == null || challenge == null)
        {
            return null;
        }
        return new PasswordInfo(password, algorithm, challenge);
    }
    
    private static Algorithm tryConvert(String algorithm)
    {
        return Algorithm.valueOf(algorithm);
    }
    
    private static final class PasswordInfo
    {
        private byte[] password;
        private Algorithm algorithm;
        private byte[] challenge;

        PasswordInfo(byte[] password, Algorithm algorithm, byte[] challenge)
        {
            this.password = password;
            this.algorithm = algorithm;
            this.challenge = challenge;
            
        }
    }
    
    private static final class AuthenticationHelper extends CifsAuthenticator
    {
        private static final ClientInfoFactory CLIENT_INFO_FACTORY = new DefaultClientInfoFactory();
        
        boolean validatePassword(String user, String storedPassword, PasswordInfo passwordInfo)
        {
            Algorithm algorithm = passwordInfo.algorithm;
            byte[] challenge = passwordInfo.challenge;
            ClientInfo clientInfo = CLIENT_INFO_FACTORY.createInfo(user, passwordInfo.password);
            clientInfo.setANSIPassword(clientInfo.getPassword());
            clientInfo.setDomain("");
            AuthContext authCtx = challenge.length == 0 ? null : new NTLanManAuthContext(challenge);
            return validatePassword(new UserAccount(user, storedPassword), clientInfo, authCtx, algorithm.ordinal());
        }
        
        byte[] generatePassword(String user, String plainPassword, Algorithm algorithm, byte[] challenge)
        {
            try
            {
                return getEncryptor().generateEncryptedPassword(plainPassword, challenge, 
                        algorithm.ordinal(), plainPassword, "");
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }
}
