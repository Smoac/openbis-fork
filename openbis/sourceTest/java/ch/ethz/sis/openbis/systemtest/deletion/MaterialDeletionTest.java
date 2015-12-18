package ch.ethz.sis.openbis.systemtest.deletion;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.id.MaterialPermId;

public class MaterialDeletionTest extends DeletionTest
{

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

        assertHistory(material.getCode(), "DESCRIPTION", "desc", "", "desc2", "", "desc3");
        assertHistory(material.getCode(), "ORGANISM", "FLY [ORGANISM]", "", "GORILLA [ORGANISM]", "", "DOG [ORGANISM]");
        assertHistory(material.getCode(), "BACTERIUM", "BACTERIUM-X [BACTERIUM]", "", "BACTERIUM-Y [BACTERIUM]", "", "BACTERIUM2 [BACTERIUM]");
    }
}
