/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.api.dto.File;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ReadTransactionOperationTest extends AbstractTransactionOperationTest {

    @Override
    public void operation() throws Exception {
        read(FILE_A_PATH, 0, DATA.length);
    }

    @Test
    public void operation_read_succeed() throws Exception {
        begin();
        byte[] data = read(FILE_A_PATH, 0, DATA.length);
        assertArrayEquals(data, DATA);
        assertEquals(0, getTransaction().getOperations().size());
    }

    @Test(expected = RuntimeException.class)
    public void operation_readDirectory_exception() throws Exception {
        begin();
        byte[] data = read(DIR_A_PATH, 0, DATA.length);
    }

    @Test
    public void operation_read0_succeed() throws Exception {
        begin();
        byte[] empty = new byte[0];
        byte[] data = read(FILE_A_PATH, 0, empty.length);
        assertArrayEquals(data, empty);
        assertEquals(0, getTransaction().getOperations().size());
    }

    @Test
    public void operation_readEmpty_succeed() throws Exception {
        begin();
        byte[] empty = new byte[0];
        byte[] data = read(FILE_B_PATH, 0, empty.length);
        assertArrayEquals(data, empty);
        assertEquals(0, getTransaction().getOperations().size());
    }

    @Test(expected = IOException.class)
    public void operation_readOver_exception() throws Exception {
        begin();
        read(FILE_B_PATH, 0, 1);
    }

    @Test(expected = RuntimeException.class)
    public void operation_read_after_delete_exception() throws Exception {
        begin();
        delete(DIR_B_PATH);
        List<File> list = list(DIR_BC_PATH, true);
        assertEquals(1, list.size());
        byte[] read = read(FILE_C_PATH, 0, Math.toIntExact(list.get(0).getSize()));
    }
}
