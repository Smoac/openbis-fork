package ch.ethz.sis.rdf.main.xlsx;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import junit.framework.TestCase;

public class ExcelImportMessageTest extends TestCase
{

    public void testFrom()
    {
        String exceptionMsg = "Exception importing data: sheet: 1 line: 209 message: Invalid terms:Invalid terms:\n" +
                "Given code 'ORTHOPAEDICSURGERYANDTRAUMATOLOGYOFTHELOCOMOTORAPPARATUS' is either too short (minimal length: 1 character) or too long (maximal length: 50 characters). (Context: []) (Context: [])";
        UserFailureException userFailureException =new UserFailureException(exceptionMsg);
        ExcelImportMessage result = ExcelImportMessage.from(userFailureException);
        assertEquals(209, result.getLine());
        assertEquals(1, result.getSheet());
        var oracleString = "Invalid terms:Invalid terms:\n" + "Given code 'ORTHOPAEDICSURGERYANDTRAUMATOLOGYOFTHELOCOMOTORAPPARATUS' is either too short (minimal length: 1 character) or too long (maximal length: 50 characters). (Context: []) (Context: [])";


        assertTrue(result.getMessage().contains("Given code 'ORTHOPAEDICSURGERYANDTRAUMATOLOGYOFTHELOCOMOTORAPPARATUS' is either too short (minimal length:  character) or too long (maximal length: 50 characters)."));


    }


    public void testFromWithError()
    {
        String exceptionMsg = "Something else!";
        UserFailureException userFailureException =new UserFailureException(exceptionMsg);
        ExcelImportMessage result = ExcelImportMessage.from(userFailureException);
        assertNull(result);


    }
}