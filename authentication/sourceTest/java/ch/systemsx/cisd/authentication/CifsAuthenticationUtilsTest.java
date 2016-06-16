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

import static junit.framework.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.CifsAuthenticationUtils.Algorithm;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CifsAuthenticationUtilsTest
{
    private static final String DOMAIN = "MY-DOMAIN";
    private static final byte[] CHALLENGE = new byte[] {12, 122, 42, -78, 88, -22, 77, 21};
    private static final String USER = "my-id";
    private static final String PASSWORD = "my-password123456789";
    
    @Test
    public void testTryGetAndAuthenticateUserForMissingPrefix()
    {
        assertNull(CifsAuthenticationUtils.tryGetAndAuthenticateUser("abc", "abc", "abcdef"));
    }
    
    @Test
    public void testTryGetAndAuthenticateUserForNotLoggedIn()
    {
        String passwordInfo = CifsAuthenticationUtils.createPasswordInfo(DOMAIN, "abcd", Algorithm.LANMAN, new byte[0]);
        assertNull(CifsAuthenticationUtils.tryGetAndAuthenticateUser("abc", null, passwordInfo));
    }
    
    @Test
    public void testTryGetAndAuthenticateUserForMissingChallenge()
    {
        String passwordInfo = CifsAuthenticationUtils.createPasswordInfo(DOMAIN, PASSWORD, Algorithm.LANMAN, new byte[0]);
        assertNull(CifsAuthenticationUtils.tryGetAndAuthenticateUser(USER, PASSWORD, passwordInfo));
    }
    
    @Test
    public void testTryGetAndAuthenticateUserSuccessfullyForAlgorithmLANMAN()
    {
        checkForAlgorithm(Algorithm.LANMAN);
    }

    @Test
    public void testTryGetAndAuthenticateUserSuccessfullyForAlgorithmNTLMV1()
    {
        checkForAlgorithm(Algorithm.NTLMV1);
    }
    
//    @Test
    public void testTryGetAndAuthenticateUserSuccessfullyForAlgorithmNTLMV2()
    {
        checkForAlgorithm(Algorithm.NTLMV2);
    }
    
//    @Test
    public void testTryGetAndAuthenticateUserSuccessfullyForAlgorithmMD4()
    {
        checkForAlgorithm(Algorithm.MD4);
    }
    
    private void checkForAlgorithm(Algorithm algorithm)
    {
        String pwd = CifsAuthenticationUtils.generatePassword(DOMAIN, USER, algorithm, CHALLENGE, PASSWORD);
        String passwordInfo = CifsAuthenticationUtils.createPasswordInfo(DOMAIN, pwd, algorithm, CHALLENGE);
        Principal principal = CifsAuthenticationUtils.tryGetAndAuthenticateUser(USER, PASSWORD, passwordInfo);
        assertEquals(USER, principal.getUserId());
        assertEquals(true, principal.isAuthenticated());
    }

}
