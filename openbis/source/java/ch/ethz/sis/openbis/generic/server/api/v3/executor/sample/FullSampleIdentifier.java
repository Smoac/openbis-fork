/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import org.apache.commons.lang.StringUtils;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;

/**
 * Helper class which parses a sample identifier string.
 * It offers human readable errors for incorrect inputs.
 * 
 * @author Franz-Josef Elmer
 */
class FullSampleIdentifier
{
    private static final String CODE_CHAR_PATTERN = "a-zA-Z0-9_\\-\\.";
    private static final String CODE_CHARS = "[" + CODE_CHAR_PATTERN + "]+";
    private static final String CODE_PATTERN = "^" + CODE_CHARS + "$";
    
    private SampleIdentifierParts sampleIdentifierParts;
    private String sampleCode;
    
    FullSampleIdentifier(String sampleIdentifier)
    {
        //Parts Validation
        if (StringUtils.isBlank(sampleIdentifier))
        {
            throw new IllegalArgumentException("Unspecified sample identifier.");
        }
        if (sampleIdentifier.startsWith("/") == false)
        {
            throw new IllegalArgumentException("Sample identifier has to start with a '/': " + sampleIdentifier);
        }
        
        String[] parts = sampleIdentifier.split("/");
        
        if (parts.length == 0)
        {
            throw new IllegalArgumentException("Sample identifier don't contain any codes: " + sampleIdentifier);
        } else if (parts.length > 4)
        {
            throw new IllegalArgumentException("Sample identifier can not contain more than three '/': " + sampleIdentifier);
        }
        
        //Parts Parsing
        String spaceCode = null;
        String projectCode = null;
        String containerCode = null;
        String plainSampleCode = null;

        String code = null;
        if (parts.length == 2)
        {
            code = parts[1];
        } if (parts.length == 3)
        {
            spaceCode = parts[1];
            code = parts[2];
        } else if (parts.length == 4)
        {
            spaceCode = parts[1];
            projectCode = parts[2];
            code = parts[3];
        }
        
        //Container:Contained validation
        String[] splittedCode = code.split(":");
        if (splittedCode.length > 2)
        {
            throw new IllegalArgumentException("Sample code can not contain more than one ':': " + sampleIdentifier);
        }
        
        //Container:Contained parsing
        if (splittedCode.length == 2)
        {
            containerCode = splittedCode[0];
            plainSampleCode = splittedCode[1];
        } else {
            plainSampleCode = splittedCode[0];
        }
        
        //Code format validation
        verifyCodePattern(spaceCode);
        verifyCodePattern(projectCode);
        verifyCodePattern(containerCode);
        verifyCodePattern(plainSampleCode);
            
        sampleCode = plainSampleCode;
        sampleIdentifierParts = new SampleIdentifierParts(spaceCode, projectCode, containerCode);
        
    }

    private void verifyCodePattern(String code) {
        if(code != null && code.matches(CODE_PATTERN) == false) {
            throw new IllegalArgumentException("Code field containing other characters than letters, numbers, '_', '-' and '.': " + code);
        }
    }
    
    
    SampleIdentifierParts getParts()
    {
        return sampleIdentifierParts;
    }

    String getSampleCode()
    {
        return sampleCode;
    }

    @Override
    public String toString()
    {
        return new SampleIdentifier(sampleIdentifierParts.getSpaceCodeOrNull(), 
                sampleIdentifierParts.getProjectCodeOrNull(), sampleIdentifierParts.getContainerCodeOrNull(), 
                sampleCode).toString();
    }
    
}
