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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.etlserver.proteomics.ProteinDescription;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinAnnotation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinDescriptionTest extends AssertJUnit
{
    @Test
    public void testWithOutAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("Q92902");
        try
        {
            new ProteinDescription(annotation, 0, true);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Can not find a amino-acid sequence in following protein description: Q92902", ex.getMessage());
        }
    }
    
    @Test
    public void testWithAccessionNumberAndSequence()
    {
        ProteinAnnotation annotation =
                createAnnotation("Q92902 \\ID=HPS1_HUMAN \\MODRES= "
                        + "\\VARIANT=(55|.|)(100|E|D)(186|A|V)(283|G|W)(480|A|T) "
                        + "\\NCBITAXID=9606 \\DE=Hermansky-Pudlak syndrome 1 protein "
                        + "\\SEQ=MKCVLVATEGAEVLFYWTDQEFEESLRLKFGQSENEEEELPA");
        ProteinDescription description = new ProteinDescription(annotation, 0, true);
        assertEquals("Q92902", description.getAccessionNumber());
        assertEquals("Hermansky-Pudlak syndrome 1 protein", description.getDescription());
        assertEquals("MKCVLVATEGAEVLFYWTDQEFEESLRLKFGQSENEEEELPA", description.getSequence());
    }
    
    @Test
    public void testWithSwissProtNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("sp|swissprot-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithTremblNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        annotation.setSwissprotName(null);
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("tr|trembl-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithIpiNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        annotation.setSwissprotName(null);
        annotation.setTremblName(null);
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("ipi|ipi-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithEnsemblNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        annotation.setSwissprotName(null);
        annotation.setTremblName(null);
        annotation.setIpiName(null);
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("ens|ensembl-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithRefSeqNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        annotation.setSwissprotName(null);
        annotation.setTremblName(null);
        annotation.setIpiName(null);
        annotation.setEnsemblName(null);
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("rs|refseq-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithLocusLinkNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        annotation.setSwissprotName(null);
        annotation.setTremblName(null);
        annotation.setIpiName(null);
        annotation.setEnsemblName(null);
        annotation.setRefseqName(null);
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("ll|locus-link-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithFlybaseNameAsAccessionNumber()
    {
        ProteinAnnotation annotation = createAnnotation("my protein");
        annotation.setSwissprotName(null);
        annotation.setTremblName(null);
        annotation.setIpiName(null);
        annotation.setEnsemblName(null);
        annotation.setRefseqName(null);
        annotation.setLocusLinkName(null);
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("my protein", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("fb|flybase-42", description.getAccessionNumber());
    }
    
    @Test
    public void testWithNoAccessionNumber()
    {
        ProteinAnnotation annotation = new ProteinAnnotation();
        annotation.setDescription("");
        ProteinDescription description = new ProteinDescription(annotation, 4711, false);
        
        assertEquals("", description.getDescription());
        assertEquals("", description.getSequence());
        assertEquals("unknown|4711", description.getAccessionNumber());
    }
    
    private ProteinAnnotation createAnnotation(String description)
    {
        ProteinAnnotation proteinAnnotation = new ProteinAnnotation();
        proteinAnnotation.setDescription(description);
        proteinAnnotation.setEnsemblName("ensembl-42");
        proteinAnnotation.setFlybase("flybase-42");
        proteinAnnotation.setIpiName("ipi-42");
        proteinAnnotation.setLocusLinkName("locus-link-42");
        proteinAnnotation.setRefseqName("refseq-42");
        proteinAnnotation.setSwissprotName("swissprot-42");
        proteinAnnotation.setTremblName("trembl-42");
        return proteinAnnotation;
    }
}
