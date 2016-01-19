package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Date;
import java.util.HashMap;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;

public class MaterialDeletionTest extends DeletionTest
{

    @Test
    @Rollback(false)
    public void testAttributes() throws Exception
    {
        Date after = new Date();
        MaterialPermId material =
                createMaterial("MATERIAL_ATT", props("DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X"));

        newTx();
        Date before = new Date();

        delete(material);

        HashMap<String, String> expectations = new HashMap<String, String>();
        expectations.put("CODE", "MATERIAL_ATT");
        expectations.put("ENTITY_TYPE", "DELETION_TEST");
        expectations.put("REGISTRATOR", "test");
        assertAttributes(material.getCode(), expectations);
        assertRegistrationTimestampAttribute(material.getCode(), after, before);
    }

    @Test
    @Rollback(false)
    public void changeProperties() throws Exception
    {
        MaterialPermId material =
                createMaterial("MATERIAL_1", props("DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X"));

        newTx();
        setProperties(material, "DESCRIPTION", "desc2", "ORGANISM", "GORILLA", "BACTERIUM", "BACTERIUM-Y");

        newTx();
        setProperties(material);

        newTx();
        setProperties(material, "DESCRIPTION", "desc3", "ORGANISM", "DOG", "BACTERIUM", "BACTERIUM2");

        delete(material);

        assertPropertiesHistory(material.getCode(), "DESCRIPTION", "desc", "desc2", "desc3");
        assertPropertiesHistory(material.getCode(), "ORGANISM", "FLY [ORGANISM]", "GORILLA [ORGANISM]", "DOG [ORGANISM]");
        assertPropertiesHistory(material.getCode(), "BACTERIUM", "BACTERIUM-X [BACTERIUM]", "BACTERIUM-Y [BACTERIUM]", "BACTERIUM2 [BACTERIUM]");
    }
}
