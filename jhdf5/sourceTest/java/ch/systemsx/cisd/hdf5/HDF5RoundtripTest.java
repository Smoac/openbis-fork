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

package ch.systemsx.cisd.hdf5;

import static ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping.mapping;
import static ch.systemsx.cisd.hdf5.HDF5FloatStorageFeatures.FLOAT_DEFLATE;
import static ch.systemsx.cisd.hdf5.HDF5FloatStorageFeatures.FLOAT_SCALING1_DEFLATE;
import static ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures.GENERIC_DEFLATE;
import static ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures.GENERIC_DEFLATE_MAX;
import static ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures.INT_AUTO_SCALING;
import static ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures.INT_AUTO_SCALING_DEFLATE;
import static ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures.INT_DEFLATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints.EnumReturnType;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation.DataTypeInfoOptions;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.SyncMode;
import ch.systemsx.cisd.hdf5.hdf5lib.H5General;
import ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants;

/**
 * Test cases for {@link IHDF5Writer} and {@link IHDF5Reader}, doing "round-trips" to the HDF5 disk
 * format and back.
 * 
 * @author Bernd Rinn
 */
public class HDF5RoundtripTest
{

    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory, "hdf5-roundtrip-wd");

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    @Override
    protected void finalize() throws Throwable
    {
        // Delete the working directory
        if (workingDirectory.exists() && workingDirectory.canWrite())
        {
            workingDirectory.delete();
        }
        // Delete root directory
        if (rootDirectory.exists() && rootDirectory.canWrite())
        {
            rootDirectory.delete();
        }

        super.finalize();
    }

    public static void main(String[] args) throws Throwable
    {
        // Print OS Version
        System.out.println("Platform: " + OSUtilities.getComputerPlatform());
        HDF5RoundtripTest test = new HDF5RoundtripTest();

        test.init();

        // Print Library Version
        final int[] libversion = new int[3];
        H5General.H5get_libversion(libversion);
        System.out.println("HDF5 Version: " + libversion[0] + "." + libversion[1] + "."
                + libversion[2]);

        // Tests
        test.testStrangeDataSetName();
        test.testCreateSomeDeepGroup();
        test.testGetGroupMembersIteratively();
        test.testScalarValues();
        test.testOverwriteScalar();
        test.testOverwriteScalarKeepDataSet();
        test.testDataSets();
        test.testDataTypeInfoOptions();
        test.testCompactDataset();
        test.testMaxPathLength();
        test.testExceedMaxPathLength();
        test.testAccessClosedReaderWriter();
        test.testDataSetsNonExtendable();
        test.testOverwriteContiguousDataSet();
        test.testScaleOffsetFilterInt();
        test.testScaleOffsetFilterFloat();
        test.testBooleanArray();
        test.testBooleanArrayBlock();
        test.testSmallString();
        test.testVeryLargeString();
        test.testOverwriteString();
        test.testOverwriteStringWithLarge();
        test.testOverwriteStringWithLargeKeepCompact();
        test.testStringCompact();
        test.testStringContiguous();
        test.testStringUnicode();
        test.testStringArray();
        test.testStringArrayBlock();
        test.testStringArrayBlockCompact();
        test.testStringArrayCompact();
        test.testStringCompression();
        test.testStringArrayCompression();
        test.testStringArrayBlockVL();
        test.testStringArrayMD();
        test.testStringArrayMDBlocks();
        test.testStringMDArrayVL();
        test.testStringMDArrayVLBlocks();
        test.testReadMDFloatArray();
        test.testReadToFloatMDArray();
        test.testFloatArrayTypeDataSet();
        test.testFloatArrayTypeDataSetOverwrite();
        test.testFloatArrayCreateCompactOverwriteBlock();
        test.testFloatMDArrayTypeDataSet();
        test.testIterateOverFloatArrayInNaturalBlocks(10, 99);
        test.testIterateOverFloatArrayInNaturalBlocks(10, 100);
        test.testIterateOverFloatArrayInNaturalBlocks(10, 101);
        test.testIterateOverStringArrayInNaturalBlocks(10, 99);
        test.testIterateOverStringArrayInNaturalBlocks(10, 100);
        test.testIterateOverStringArrayInNaturalBlocks(10, 101);
        test.testReadToFloatMDArrayBlockWithOffset();
        test.testIterateOverMDFloatArrayInNaturalBlocks(new int[]
            { 2, 2 }, new long[]
            { 4, 3 }, new float[]
            { 0f, 2f, 6f, 8f }, new int[][]
            {
                { 2, 2 },
                { 2, 1 },
                { 2, 2 },
                { 2, 1 } });
        test.testIterateOverMDFloatArrayInNaturalBlocks(new int[]
            { 2, 2 }, new long[]
            { 4, 4 }, new float[]
            { 0f, 2f, 8f, 10f }, new int[][]
            {
                { 2, 2 },
                { 2, 2 },
                { 2, 2 },
                { 2, 2 } });
        test.testIterateOverMDFloatArrayInNaturalBlocks(new int[]
            { 2, 2 }, new long[]
            { 4, 5 }, new float[]
            { 0f, 2f, 4f, 10f, 12f, 14f }, new int[][]
            {
                { 2, 2 },
                { 2, 2 },
                { 2, 1 },
                { 2, 2 },
                { 2, 2 },
                { 2, 1 } });
        test.testIterateOverMDFloatArrayInNaturalBlocks(new int[]
            { 3, 2 }, new long[]
            { 5, 4 }, new float[]
            { 0f, 2f, 12f, 14f }, new int[][]
            {
                { 3, 2 },
                { 3, 2 },
                { 2, 2 },
                { 2, 2 } });
        test.testIterateOverMDFloatArrayInNaturalBlocks(new int[]
            { 2, 2 }, new long[]
            { 5, 4 }, new float[]
            { 0f, 2f, 8f, 10f, 16f, 18f }, new int[][]
            {
                { 2, 2 },
                { 2, 2 },
                { 2, 2 },
                { 2, 2 },
                { 1, 2 },
                { 1, 2 } });
        test.testSetExtentBug();
        test.testMDFloatArrayBlockWise();
        test.testMDFloatArrayBlockWiseWithMemoryOffset();
        test.testDoubleArrayAsByteArray();
        test.testCompressedDataSet();
        test.testCreateEmptyFloatMatrix();
        test.testFloatVectorLength1();
        test.testFloatMatrixLength1();
        test.testOneRowFloatMatrix();
        test.testEmptyVectorDataSets();
        test.testEmptyVectorDataSetsContiguous();
        test.testEmptyVectorDataSetsCompact();
        test.testEmptyMatrixDataSets();
        test.testEmptyMatrixDataSetsContiguous();
        test.testOverwriteVectorIncreaseSize();
        test.testOverwriteMatrixIncreaseSize();
        test.testOverwriteStringVectorDecreaseSize();
        test.testAttributes();
        test.testTimeStampAttributes();
        test.testTimeDurationAttributes();
        test.testTimeStampArrayAttributes();
        test.testTimeDurationArrayAttributes();
        test.testAttributeDimensionArray();
        test.testAttributeDimensionArrayOverwrite();
        test.testCreateDataTypes();
        test.testGroups();
        test.testSoftLink();
        test.testBrokenSoftLink();
        test.testDeleteSoftLink();
        test.testRenameLink();
        try
        {
            test.testRenameLinkOverwriteFails();
        } catch (HDF5SymbolTableException ex)
        {
            // Expected.
        }
        try
        {
            test.testRenameLinkSrcNonExistentFails();
        } catch (HDF5SymbolTableException ex)
        {
            // Expected.
        }
        test.testNullOnGetSymbolicLinkTargetForNoLink();
        test.testUpdateSoftLink();
        test.testExternalLink();
        test.testEnum();
        test.testJavaEnum();
        test.testEnum16();
        try
        {
            test.testConfusedEnum();
            System.err.println("testConfusedEnum(): failure not detected.");
        } catch (HDF5JavaException ex)
        {
            assertEquals("Enum member index 0 of enum testEnum is 'ONE', but should be 'THREE'",
                    ex.getMessage());
        }
        test.testReplaceConfusedEnum();
        test.testEnumArray();
        test.testJavaEnumArray();
        test.testEnumArrayBlock();
        test.testEnumArrayBlockScalingCompression();
        test.testEnumArrayFromIntArray();
        test.testEnumArray16BitFromIntArray();
        test.testEnumArray16BitFromIntArrayScaled();
        test.testEnumArray16BitFromIntArrayLarge();
        test.testEnumArrayBlock16Bit();
        test.testEnumArrayScaleCompression();
        test.testOpaqueType();
        test.testCompound();
        test.testCompoundJavaEnum();
        test.testCompoundJavaEnumMap();
        test.testCompoundAttribute();
        test.testCompoundIncompleteJavaPojo();
        test.testCompoundManualMapping();
        test.testInferredCompoundType();
        test.testInferredIncompletelyMappedCompoundType();
        test.testNameChangeInCompoundMapping();
        test.testInferredCompoundTypedWithEnum();
        test.testInferredCompoundTypeWithEnumArray();
        test.testCompoundMap();
        test.testCompoundMapManualMapping();
        test.testCompoundMapManualMappingWithConversion();
        test.testDateCompound();
        test.testMatrixCompound();
        try
        {
            test.testMatrixCompoundSizeMismatch();
            System.err.println("testMatrixCompoundSizeMismatch(): failure not detected.");
        } catch (IllegalArgumentException ex)
        {
            // Expected
        }
        try
        {
            test.testMatrixCompoundDifferentNumberOfColumnsPerRow();
            System.err
                    .println("testMatrixCompoundDifferentNumberOfColumnsPerRow(): failure not detected.");
        } catch (IllegalArgumentException ex)
        {
            // Expected
        }
        test.testCompoundOverflow();
        test.testBitFieldCompound();
        test.testCompoundMapArray();
        test.testCompoundArray();
        test.testCompoundArrayBlockWise();
        test.testCompoundMapMDArray();
        test.testCompoundMDArray();
        test.testCompoundMDArrayManualMapping();
        test.testCompoundMDArrayBlockWise();
        test.testIterateOverMDCompoundArrayInNaturalBlocks();
        test.testConfusedCompound();
        test.testMDArrayCompound();
        test.testMDArrayCompoundArray();
        test.testGetGroupMemberInformation();
        try
        {
            test.testGetLinkInformationFailed();
            System.err.println("testGetObjectTypeFailed(): failure not detected.");
        } catch (HDF5JavaException ex)
        {
            // Expected
        }
        test.testGetObjectType();
        test.testHardLink();
        test.testNullOnGetSymbolicLinkTargetForNoLink();
        test.testReadByteArrayDataSetBlockWise();
        test.testWriteByteArrayDataSetBlockWise();
        test.testCreateByteArrayDataSetBlockSize0();
        test.testCreateFloatArrayWithDifferentStorageLayouts();
        test.testWriteByteArrayDataSetBlockWiseExtend();
        test.testWriteByteMatrixDataSetBlockWise();
        test.testWriteByteArrayDataSetBlockWiseMismatch();
        test.testWriteByteMatrixDataSetBlockWiseMismatch();
        test.testReadFloatMatrixDataSetBlockWise();
        test.testWriteFloatMatrixDataSetBlockWise();
        test.testExtendContiguousDataset();
        test.testAutomaticDeletionOfDataSetOnWrite();
        test.testAutomaticDeletionOfDataSetOnCreate();
        test.testTimestamps();
        test.testTimestampArray();
        test.testTimestampArrayChunked();
        test.testTimeDurations();
        test.testSmallTimeDurations();
        test.testTimeDurationArray();
        test.testTimeDurationArrayChunked();
        test.testNumericConversion();
        test.testSetDataSetSize();
        test.testObjectReference();
        test.testObjectReferenceArray();
        test.testObjectReferenceOverwriteWithKeep();
        test.testObjectReferenceOverwriteWithKeepOverridden();
        test.testObjectReferenceArrayBlockWise();
        test.testObjectReferenceAttribute();
        test.testObjectReferenceArrayAttribute();
        test.testObjectReferenceMDArrayAttribute();
        test.testObjectReferenceMDArray();
        test.testObjectReferenceMDArrayBlockWise();
        test.testHDFJavaLowLevel();

        test.finalize();
    }

    @Test
    public void testStrangeDataSetName()
    {
        final File file = new File(workingDirectory, "testStrangeDataSetName.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.configure(file).noAutoDereference().writer();
        writer.writeInt("\0\255", 15);
        writer.close();
        final IHDF5Reader reader =
                HDF5Factory.configureForReading(file).noAutoDereference().reader();
        assertEquals(15, reader.readInt("\0\255"));
        reader.close();
    }

    @Test
    public void testCreateSomeDeepGroup()
    {
        final File datasetFile = new File(workingDirectory, "deepGroup.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(datasetFile).useUTF8CharacterEncoding()
                        .writer();
        final String groupName = "/some/deep/and/non/existing/group";
        writer.createGroup(groupName);
        assertTrue(writer.isGroup(groupName));
        writer.close();
    }

    @Test
    public void testGetGroupMembersIteratively()
    {
        final File datasetFile = new File(workingDirectory, "writereadwriteread.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String groupName = "/test/group/";
        final String dset1Name = "dset1";
        final String dset1Path = groupName + dset1Name;
        final float[] dset1 = new float[]
            { 1.3f, 2.4f, 3.6f };
        writer.writeFloatArray(dset1Path, dset1);
        final List<String> members1 = writer.getGroupMembers(groupName);
        assertEquals(1, members1.size());
        assertEquals(dset1Name, members1.get(0));
        final String dset2Name = "dset2";
        final String dset2Path = groupName + dset2Name;
        final int[] dset2 = new int[]
            { 1, 2, 3 };
        writer.writeIntArray(dset2Path, dset2);
        final Set<String> members2 = new HashSet<String>(writer.getGroupMembers(groupName));
        assertEquals(2, members2.size());
        assertTrue(members2.contains(dset1Name));
        assertTrue(members2.contains(dset2Name));
        writer.close();
    }

    @Test
    public void testOverwriteScalar()
    {
        final File datasetFile = new File(workingDirectory, "overwriteScalar.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(datasetFile);
        writer.writeInt("a", 4);
        assertEquals(HDF5DataClass.INTEGER, writer.getDataSetInformation("a").getTypeInformation()
                .getDataClass());
        writer.writeFloat("a", 1e6f);
        assertEquals(HDF5DataClass.FLOAT, writer.getDataSetInformation("a").getTypeInformation()
                .getDataClass());
        assertEquals(1e6f, writer.readFloat("a"));
        writer.close();
    }

    @Test
    public void testOverwriteScalarKeepDataSet()
    {
        final File datasetFile = new File(workingDirectory, "overwriteScalarKeepDataSet.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer =
                HDF5Factory.configure(datasetFile).keepDataSetsIfTheyExist().writer();
        writer.writeInt("a", 4);
        assertEquals(HDF5DataClass.INTEGER, writer.getDataSetInformation("a").getTypeInformation()
                .getDataClass());
        writer.writeFloat("a", 5.1f);
        assertEquals(HDF5DataClass.INTEGER, writer.getDataSetInformation("a").getTypeInformation()
                .getDataClass());
        assertEquals(5, writer.readInt("a"));
        writer.close();
    }

    @Test
    public void testScalarValues()
    {
        final File datasetFile = new File(workingDirectory, "values.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String booleanDatasetName = "/boolean";
        writer.writeBoolean(booleanDatasetName, true);
        final String byteDatasetName = "/byte";
        writer.writeByte(byteDatasetName, (byte) 17);
        final String shortDatasetName = "/short";
        writer.writeShort(shortDatasetName, (short) 1000);
        final String intDatasetName = "/int";
        writer.writeInt(intDatasetName, 1000000);
        final String longDatasetName = "/long";
        writer.writeLong(longDatasetName, 10000000000L);
        final String floatDatasetName = "/float";
        writer.writeFloat(floatDatasetName, 0.001f);
        final String doubleDatasetName = "/double";
        writer.writeDouble(doubleDatasetName, 1.0E100);
        final String stringDatasetName = "/string";
        writer.writeString(stringDatasetName, "some string");
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertTrue(reader.readBoolean(booleanDatasetName));
        assertEquals(17, reader.readByte(byteDatasetName));
        assertEquals(1000, reader.readShort(shortDatasetName));
        assertEquals(1000000, reader.readInt(intDatasetName));
        assertEquals(10000000000L, reader.readLong(longDatasetName));
        assertEquals(0.001f, reader.readFloat(floatDatasetName));
        assertEquals(1.0E100, reader.readDouble(doubleDatasetName));
        assertEquals("some string", reader.readString(stringDatasetName));
        reader.close();
    }

    @Test
    public void testReadMDFloatArray()
    {
        final File datasetFile = new File(workingDirectory, "mdArray.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/floatMatrix";
        final MDFloatArray arrayWritten = new MDFloatArray(new int[]
            { 3, 2, 4 });
        int count = 0;
        for (int i = 0; i < arrayWritten.size(0); ++i)
        {
            for (int j = 0; j < arrayWritten.size(1); ++j)
            {
                for (int k = 0; k < arrayWritten.size(2); ++k)
                {
                    arrayWritten.set(++count, new int[]
                        { i, j, k });
                }
            }
        }
        writer.writeFloatMDArray(floatDatasetName, arrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final MDFloatArray arrayRead = reader.readFloatMDArray(floatDatasetName);
        reader.close();
        assertEquals(arrayWritten, arrayRead);

    }

    @Test
    public void testBooleanArray()
    {
        final File datasetFile = new File(workingDirectory, "booleanArray.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String booleanDatasetName = "/booleanArray";
        final String longArrayDataSetName = "/longArray";
        final BitSet arrayWritten = new BitSet();
        arrayWritten.set(32);
        writer.writeBitField(booleanDatasetName, arrayWritten);
        writer.writeLongArray(longArrayDataSetName,
                BitSetConversionUtils.toStorageForm(arrayWritten));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final BitSet arrayRead = reader.readBitField(booleanDatasetName);
        try
        {
            reader.readBitField(longArrayDataSetName);
            fail("Failed to detect type mismatch.");
        } catch (HDF5DatatypeInterfaceException ex)
        {
            // Expected, as the types do not match.
        }
        assertEquals(arrayWritten, arrayRead);
        final HDF5DataSetInformation info = reader.getDataSetInformation(booleanDatasetName);
        assertEquals(HDF5DataClass.BITFIELD, info.getTypeInformation().getDataClass());
        assertChunkSizes(info, HDF5Utils.MIN_CHUNK_SIZE);
        reader.close();
    }

    @Test
    public void testBooleanArrayBlock()
    {
        final File datasetFile = new File(workingDirectory, "booleanArrayBlock.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String booleanDatasetName = "/booleanArray";
        final BitSet arrayWritten = new BitSet();
        writer.createBitField(booleanDatasetName, 4L, 2);
        arrayWritten.set(32);
        arrayWritten.set(40);
        writer.writeBitFieldBlock(booleanDatasetName, arrayWritten, 2, 0);
        arrayWritten.clear();
        arrayWritten.set(0);
        writer.writeBitFieldBlock(booleanDatasetName, arrayWritten, 2, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final BitSet arrayBlockRead = reader.readBitFieldBlock(booleanDatasetName, 2, 1);
        assertEquals(1, arrayBlockRead.cardinality());
        assertTrue(arrayBlockRead.get(0));
        assertTrue(reader.isBitSetInBitField(booleanDatasetName, 32));
        assertTrue(reader.isBitSetInBitField(booleanDatasetName, 40));
        assertTrue(reader.isBitSetInBitField(booleanDatasetName, 128));
        assertFalse(reader.isBitSetInBitField(booleanDatasetName, 33));
        assertFalse(reader.isBitSetInBitField(booleanDatasetName, 64));
        reader.close();
    }

    private void assertChunkSizes(final HDF5DataSetInformation info,
            final long... expectedChunkSize)
    {
        assertEquals(HDF5StorageLayout.CHUNKED, info.getStorageLayout());
        final int[] chunkSize = info.tryGetChunkSizes();
        assertNotNull(chunkSize);
        assertEquals(expectedChunkSize.length, chunkSize.length);
        for (int i = 0; i < expectedChunkSize.length; ++i)
        {
            assertEquals(Integer.toString(i), expectedChunkSize[i], chunkSize[i]);
        }
    }

    @Test
    public void testMDFloatArrayBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "mdArrayBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/floatMatrix";
        final long[] shape = new long[]
            { 10, 10, 10 };
        final int[] blockShape = new int[]
            { 5, 5, 5 };
        writer.createFloatMDArray(floatDatasetName, shape, blockShape);
        final float[] flatArray = new float[MDArray.getLength(blockShape)];
        for (int i = 0; i < flatArray.length; ++i)
        {
            flatArray[i] = i;
        }
        final MDFloatArray arrayBlockWritten = new MDFloatArray(flatArray, blockShape);
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                for (int k = 0; k < 2; ++k)
                {
                    writer.writeFloatMDArrayBlock(floatDatasetName, arrayBlockWritten, new long[]
                        { i, j, k });
                }
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                for (int k = 0; k < 2; ++k)
                {
                    final MDFloatArray arrayRead =
                            reader.readFloatMDArrayBlock(floatDatasetName, blockShape, new long[]
                                { i, j, k });
                    assertEquals(arrayBlockWritten, arrayRead);
                }
            }
        }
        reader.close();

    }

    @Test
    public void testMDFloatArrayBlockWiseWithMemoryOffset()
    {
        final File datasetFile = new File(workingDirectory, "mdArrayBlockWiseWithMemoryOffset.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/floatMatrix";
        final long[] shape = new long[]
            { 10, 10 };
        writer.createFloatMDArray(floatDatasetName, shape, MDArray.toInt(shape));
        final float[] flatArray = new float[MDArray.getLength(shape)];
        for (int i = 0; i < flatArray.length; ++i)
        {
            flatArray[i] = i;
        }
        final MDFloatArray arrayBlockWritten = new MDFloatArray(flatArray, shape);
        writer.writeFloatMDArrayBlockWithOffset(floatDatasetName, arrayBlockWritten, new int[]
            { 2, 2 }, new long[]
            { 0, 0 }, new int[]
            { 1, 3 });
        writer.writeFloatMDArrayBlockWithOffset(floatDatasetName, arrayBlockWritten, new int[]
            { 2, 2 }, new long[]
            { 2, 2 }, new int[]
            { 5, 1 });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[][] matrixRead = reader.readFloatMatrix(floatDatasetName);
        reader.close();
        assertEquals(13f, matrixRead[0][0]);
        assertEquals(14f, matrixRead[0][1]);
        assertEquals(23f, matrixRead[1][0]);
        assertEquals(24f, matrixRead[1][1]);
        assertEquals(51f, matrixRead[2][2]);
        assertEquals(52f, matrixRead[2][3]);
        assertEquals(61f, matrixRead[3][2]);
        assertEquals(62f, matrixRead[3][3]);
        for (int i = 0; i < 10; ++i)
        {
            for (int j = 0; j < 10; ++j)
            {
                if ((i < 2 && j < 2) || (i > 1 && i < 4 && j > 1 && j < 4))
                {
                    continue;
                }
                assertEquals("(" + i + "," + j + "}", 0f, matrixRead[i][j]);
            }
        }
    }

    @Test
    public void testDataSets()
    {
        final File datasetFile = new File(workingDirectory, "datasets.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/Group1/floats";
        final float[] floatDataWritten = new float[]
            { 2.8f, 8.2f, -3.1f, 0.0f, 10000.0f };
        writer.writeFloatArray(floatDatasetName, floatDataWritten);
        final long[] longDataWritten = new long[]
            { 10, -1000000, 1, 0, 100000000000L };
        final String longDatasetName = "/Group2/longs";
        writer.writeLongArray(longDatasetName, longDataWritten);
        final byte[] byteDataWritten = new byte[]
            { 0, -1, 1, -128, 127 };
        final String byteDatasetName = "/Group2/bytes";
        writer.writeByteArray(byteDatasetName, byteDataWritten, INT_DEFLATE);
        final short[] shortDataWritten = new short[]
            { 0, -1, 1, -128, 127 };
        final String shortDatasetName = "/Group2/shorts";
        writer.writeShortArray(shortDatasetName, shortDataWritten, INT_DEFLATE);
        writer.flush();
        final String stringDataWritten1 = "Some Random String";
        final String stringDataWritten2 = "Another Random String";
        final String stringDatasetName = "/Group3/strings";
        final String stringDatasetName2 = "/Group4/strings";
        writer.writeString(stringDatasetName, stringDataWritten1);
        writer.writeStringVariableLength(stringDatasetName2, stringDataWritten1);
        writer.writeStringVariableLength(stringDatasetName2, stringDataWritten2);
        final String stringDatasetName3 = "/Group4/stringArray";
        writer.writeStringVariableLengthArray(stringDatasetName3, new String[]
            { stringDataWritten1, stringDataWritten2 });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[] floatDataRead = reader.readFloatArray(floatDatasetName);
        assertTrue(Arrays.equals(floatDataWritten, floatDataRead));
        final long[] longDataRead = reader.readLongArray(longDatasetName);
        assertTrue(Arrays.equals(longDataWritten, longDataRead));
        final byte[] byteDataRead = reader.readByteArray(byteDatasetName);
        assertTrue(Arrays.equals(byteDataWritten, byteDataRead));
        final short[] shortDataRead = reader.readShortArray(shortDatasetName);
        assertTrue(Arrays.equals(shortDataWritten, shortDataRead));
        final String stringDataRead1 = reader.readString(stringDatasetName);
        assertEquals(stringDataWritten1, stringDataRead1);
        final String stringDataRead2 = reader.readString(stringDatasetName2);
        assertEquals(stringDataWritten2, stringDataRead2);
        final String[] vlStringArrayRead = reader.readStringArray(stringDatasetName3);
        assertEquals(stringDataWritten1, vlStringArrayRead[0]);
        assertEquals(stringDataWritten2, vlStringArrayRead[1]);
        reader.close();
    }

    @Test
    public void testScaleOffsetFilterInt()
    {
        final File datasetFile = new File(workingDirectory, "scaleoffsetfilterint.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final int[] intWritten = new int[1000000];
        for (int i = 0; i < intWritten.length; ++i)
        {
            intWritten[i] = (i % 4);
        }
        writer.writeIntArray("ds", intWritten, INT_AUTO_SCALING_DEFLATE);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final int[] intRead = reader.readIntArray("ds");
        assertTrue(Arrays.equals(intRead, intWritten));
        reader.close();

        // Shouldn't work in strict HDF5 1.6 mode.
        final File file2 = new File(workingDirectory, "scaleoffsetfilterintfailed.h5");
        file2.delete();
        assertFalse(file2.exists());
        file2.deleteOnExit();
        final IHDF5Writer writer2 =
                HDF5FactoryProvider.get().configure(file2).fileFormat(FileFormat.STRICTLY_1_6)
                        .writer();
        try
        {
            writer2.writeIntArray("ds", intWritten, INT_AUTO_SCALING_DEFLATE);
            fail("Usage of scaling compression in strict HDF5 1.6 mode not detected");
        } catch (IllegalStateException ex)
        {
            assertTrue(ex.getMessage().indexOf("not allowed") >= 0);
        }
        writer2.close();
    }

    @Test
    public void testScaleOffsetFilterFloat()
    {
        final File datasetFile = new File(workingDirectory, "scaleoffsetfilterfloat.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final float[] floatWritten = new float[1000000];
        for (int i = 0; i < floatWritten.length; ++i)
        {
            floatWritten[i] = (i % 10) / 10f;
        }
        writer.writeFloatArray("ds", floatWritten, FLOAT_SCALING1_DEFLATE);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[] floatRead = reader.readFloatArray("ds");
        assertTrue(Arrays.equals(floatRead, floatWritten));
        reader.close();
    }

    @Test
    public void testMaxPathLength()
    {
        final File datasetFile = new File(workingDirectory, "maxpathlength.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String madnessOverwhelmesUs1 = StringUtils.repeat("a", 16384);
        final String madnessOverwhelmesUs2 = StringUtils.repeat("/b", 8192);
        writer.writeInt(madnessOverwhelmesUs1, 17);
        writer.writeFloat(madnessOverwhelmesUs2, 0.0f);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals(17, reader.readInt(madnessOverwhelmesUs1));
        assertEquals(0.0f, reader.readFloat(madnessOverwhelmesUs2));
        reader.close();
    }

    @Test
    public void testExceedMaxPathLength()
    {
        final File datasetFile = new File(workingDirectory, "exceedmaxpathlength.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String madnessOverwhelmesUs = StringUtils.repeat("a", 16385);
        try
        {
            writer.writeInt(madnessOverwhelmesUs, 17);
            fail("path overflow not detected");
        } catch (HDF5JavaException ex)
        {
            assertEquals(0, ex.getMessage().indexOf("Path too long"));
        } finally
        {
            writer.close();
        }
    }

    @Test
    public void testAccessClosedReaderWriter()
    {
        final File datasetFile = new File(workingDirectory, "datasetsNonExtendable.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.close();
        try
        {
            writer.writeBoolean("dataSet", true);
        } catch (HDF5JavaException ex)
        {
            assertEquals(String.format("HDF5 file '%s' is closed.", datasetFile.getAbsolutePath()),
                    ex.getMessage());
        }
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        reader.close();
        try
        {
            reader.readBoolean("dataSet");
        } catch (HDF5JavaException ex)
        {
            assertEquals(String.format("HDF5 file '%s' is closed.", datasetFile.getAbsolutePath()),
                    ex.getMessage());
        }
    }

    @Test
    public void testDataSetsNonExtendable()
    {
        final File datasetFile = new File(workingDirectory, "datasetsNonExtendable.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(datasetFile).dontUseExtendableDataTypes()
                        .syncMode(SyncMode.SYNC_BLOCK).writer();
        final String floatDatasetName = "/Group1/floats";
        final float[] floatDataWritten = new float[]
            { 2.8f, 8.2f, -3.1f, 0.0f, 10000.0f };
        writer.writeFloatArray(floatDatasetName, floatDataWritten);
        final String compressedFloatDatasetName = "/Group1/floatsCompressed";
        writer.writeFloatArray(compressedFloatDatasetName, floatDataWritten, FLOAT_DEFLATE);
        final long[] longDataWritten = new long[]
            { 10, -1000000, 1, 0, 100000000000L };
        final String longDatasetName = "/Group2/longs";
        writer.writeLongArray(longDatasetName, longDataWritten);
        final long[] longDataWrittenAboveCompactThreshold = new long[128];
        for (int i = 0; i < longDataWrittenAboveCompactThreshold.length; ++i)
        {
            longDataWrittenAboveCompactThreshold[i] = i;
        }
        final String longDatasetNameAboveCompactThreshold = "/Group2/longsContiguous";
        writer.writeLongArray(longDatasetNameAboveCompactThreshold,
                longDataWrittenAboveCompactThreshold);
        final String longDatasetNameAboveCompactThresholdCompress = "/Group2/longsChunked";
        writer.writeLongArray(longDatasetNameAboveCompactThresholdCompress,
                longDataWrittenAboveCompactThreshold, INT_DEFLATE);
        final byte[] byteDataWritten = new byte[]
            { 0, -1, 1, -128, 127 };
        final String byteDatasetName = "/Group2/bytes";
        writer.writeByteArray(byteDatasetName, byteDataWritten, INT_DEFLATE);
        final String stringDataWritten = "Some Random String";
        final String stringDatasetName = "/Group3/strings";
        final String stringDatasetName2 = "/Group4/strings";
        writer.writeString(stringDatasetName, stringDataWritten);
        writer.writeStringVariableLength(stringDatasetName2, stringDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[] floatDataRead = reader.readFloatArray(floatDatasetName);
        HDF5DataSetInformation info = reader.getDataSetInformation(floatDatasetName);
        assertEquals(HDF5StorageLayout.COMPACT, info.getStorageLayout());
        assertNull(info.tryGetChunkSizes());
        assertTrue(Arrays.equals(floatDataWritten, floatDataRead));
        final long[] compressedLongDataRead =
                reader.readLongArray(longDatasetNameAboveCompactThresholdCompress);
        info = reader.getDataSetInformation(longDatasetNameAboveCompactThresholdCompress);
        assertChunkSizes(info, longDataWrittenAboveCompactThreshold.length);
        assertTrue(Arrays.equals(longDataWrittenAboveCompactThreshold, compressedLongDataRead));
        final long[] longDataRead = reader.readLongArray(longDatasetName);
        info = reader.getDataSetInformation(longDatasetName);
        assertEquals(HDF5StorageLayout.COMPACT, info.getStorageLayout());
        assertNull(info.tryGetChunkSizes());
        assertTrue(Arrays.equals(longDataWritten, longDataRead));
        final long[] longDataReadAboveCompactThreshold =
                reader.readLongArray(longDatasetNameAboveCompactThreshold);
        info = reader.getDataSetInformation(longDatasetNameAboveCompactThreshold);
        assertEquals(HDF5StorageLayout.CONTIGUOUS, info.getStorageLayout());
        assertNull(info.tryGetChunkSizes());
        assertTrue(Arrays.equals(longDataWrittenAboveCompactThreshold,
                longDataReadAboveCompactThreshold));
        final byte[] byteDataRead = reader.readByteArray(byteDatasetName);
        assertTrue(Arrays.equals(byteDataWritten, byteDataRead));
        final String stringDataRead = reader.readString(stringDatasetName);
        assertEquals(stringDataWritten, stringDataRead);
        reader.close();
    }

    @Test
    public void testOverwriteContiguousDataSet()
    {
        // Test for a bug in 1.8.1 and 1.8.2 when overwriting contiguous data sets and thereby
        // changing its size.
        // We have some workaround code in IHDF5Writer.getDataSetId(), this is why this test runs
        // green. As new versions of HDF5 become available, one can try to comment out the
        // workaround code and see whether this test still runs red.
        final File datasetFile = new File(workingDirectory, "overwriteContiguousDataSet.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final String dsName = "longArray";
        IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(datasetFile).dontUseExtendableDataTypes()
                        .writer();
        // Creating the group is part of the "bug magic".
        writer.createGroup("group");
        final long[] arrayWritten1 = new long[1000];
        for (int i = 0; i < arrayWritten1.length; ++i)
        {
            arrayWritten1[i] = i;
        }
        writer.writeLongArray(dsName, arrayWritten1);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        final long[] arrayWritten2 = new long[5];
        for (int i = 0; i < arrayWritten1.length; ++i)
        {
            arrayWritten1[i] = i * i;
        }
        writer.writeLongArray(dsName, arrayWritten2);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final long[] arrayRead = reader.readLongArray(dsName);
        assertTrue(Arrays.equals(arrayWritten2, arrayRead));
        reader.close();
    }

    @Test
    public void testCompactDataset()
    {
        final File datasetFile = new File(workingDirectory, "compactDS.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final String dsName = "ds";
        long[] data = new long[]
            { 1, 2, 3 };
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeLongArray(dsName, data, HDF5IntStorageFeatures.INT_COMPACT);
        assertEquals(HDF5StorageLayout.COMPACT, writer.getDataSetInformation(dsName)
                .getStorageLayout());
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertTrue(Arrays.equals(data, reader.readLongArray(dsName)));
        reader.close();
    }

    @Test
    public void testExtendChunkedDataset()
    {
        final File datasetFile = new File(workingDirectory, "extendChunked.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final String dsName = "ds";
        long[] data = new long[]
            { 1, 2, 3, 4 };
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.createLongArray(dsName, 5, 3);
        writer.writeLongArray(dsName, data, HDF5IntStorageFeatures.INT_NO_COMPRESSION_KEEP);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        long[] dataRead = reader.readLongArray(dsName);
        assertTrue(Arrays.equals(data, dataRead));
        reader.close();
        // Now write a larger data set and see whether the data set is correctly extended.
        writer = HDF5FactoryProvider.get().open(datasetFile);
        data = new long[]
            { 17, 42, 1, 2, 3, 101, -5 };
        writer.writeLongArray(dsName, data);
        writer.close();
        reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        dataRead = reader.readLongArray(dsName);
        assertTrue(Arrays.equals(data, dataRead));
        reader.close();
    }

    @Test
    public void testExtendContiguousDataset()
    {
        final File datasetFile = new File(workingDirectory, "extendContiguous.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final String dsName = "ds";
        long[] longArrayWritten = new long[]
            { 1, 2, 3 };
        IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(datasetFile).dontUseExtendableDataTypes()
                        .writer();
        // Set maxdims such that COMPACT_LAYOUT_THRESHOLD (int bytes!) is exceeded so that we get a
        // contiguous data set.
        writer.createLongArray(dsName, 128, 1);
        writer.writeLongArray(dsName, longArrayWritten);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final long[] longArrayRead = reader.readLongArray(dsName);
        assertTrue(Arrays.equals(longArrayWritten, longArrayRead));
        reader.close();
        // Now write a larger data set and see whether the data set is correctly extended.
        writer = HDF5FactoryProvider.get().open(datasetFile);
        longArrayWritten = new long[]
            { 17, 42, 1, 2, 3 };
        writer.writeLongArray(dsName, longArrayWritten);
        writer.close();
        reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertTrue(Arrays.equals(longArrayWritten, reader.readLongArray(dsName)));
        reader.close();
    }

    @Test
    public void testAutomaticDeletionOfDataSetOnWrite()
    {
        final File datasetFile = new File(workingDirectory, "automaticDeletionOfDataSetOnWrite.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = new HDF5WriterConfigurator(datasetFile).writer();
        writer.createFloatArray("f", 12, HDF5FloatStorageFeatures.FLOAT_COMPACT);
        writer.writeFloatArray("f", new float[]
            { 1f, 2f, 3f, 4f, 5f });
        writer.close();
        final IHDF5Reader reader = new HDF5ReaderConfigurator(datasetFile).reader();
        HDF5DataSetInformation info = reader.getDataSetInformation("f");
        assertEquals(HDF5StorageLayout.CHUNKED, info.getStorageLayout());
        assertEquals(5, info.tryGetChunkSizes()[0]);
    }

    @Test
    public void testAutomaticDeletionOfDataSetOnCreate()
    {
        final File datasetFile =
                new File(workingDirectory, "automaticDeletionOfDataSetOnCreate.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = new HDF5WriterConfigurator(datasetFile).writer();
        writer.createFloatArray("f", 12, 6, HDF5FloatStorageFeatures.FLOAT_COMPACT);
        writer.createFloatArray("f", 10, HDF5FloatStorageFeatures.FLOAT_CONTIGUOUS);
        // This won't overwrite the data set as it is a block write command.
        writer.writeFloatArrayBlock("f", new float[]
            { 1f, 2f, 3f, 4f, 5f }, 0);
        writer.close();
        final IHDF5Reader reader = new HDF5ReaderConfigurator(datasetFile).reader();
        HDF5DataSetInformation info = reader.getDataSetInformation("f");
        assertEquals(HDF5StorageLayout.CONTIGUOUS, info.getStorageLayout());
        assertEquals(10, info.getDimensions()[0]);
        assertNull(info.tryGetChunkSizes());
    }

    @Test
    public void testSpacesInDataSetName()
    {
        final File datasetFile = new File(workingDirectory, "datasetsWithSpaces.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "Float Dataset";
        final float[] floatDataWritten = new float[]
            { 2.8f, 8.2f, -3.1f, 0.0f, 10000.0f };
        writer.writeFloatArray(floatDatasetName, floatDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[] floatDataRead = reader.readFloatArray(floatDatasetName);
        assertTrue(Arrays.equals(floatDataWritten, floatDataRead));
        reader.close();
    }

    @Test
    public void testFloatArrayTypeDataSet()
    {
        final File datasetFile = new File(workingDirectory, "floatArrayTypeDataSet.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final HDF5ArrayTypeFloatWriter efWriter = new HDF5ArrayTypeFloatWriter((HDF5Writer) writer);
        final float[] floatDataWritten = new float[]
            { 2.8f, 8.2f, -3.1f, 0.0f, 10000.0f };
        efWriter.writeFloatArrayArrayType("f", floatDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals("FLOAT(4, #5):{}", reader.getDataSetInformation("f").toString());
        final float[] floatDataRead = reader.readFloatArray("f");
        assertTrue(Arrays.equals(floatDataWritten, floatDataRead));
    }

    @Test
    public void testDoubleArrayAsByteArray()
    {
        final File datasetFile = new File(workingDirectory, "doubleArrayTypeDataSetAsByteArray.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final HDF5ArrayTypeFloatWriter efWriter = new HDF5ArrayTypeFloatWriter((HDF5Writer) writer);
        writer.createDoubleArray("f", 6, 3);
        final double[] floatDataWritten = new double[]
            { 2.8, 8.2, -3.1, 0.0, 10000.0 };
        efWriter.writeDoubleArrayBigEndian("f", floatDataWritten,
                HDF5FloatStorageFeatures.FLOAT_NO_COMPRESSION_KEEP);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals("FLOAT(8):{5}", reader.getDataSetInformation("f").toString());
        final byte[] byteDataRead = reader.readAsByteArray("f");
        final double[] floatDataRead = NativeData.byteToDouble(byteDataRead, ByteOrder.NATIVE);
        assertTrue(Arrays.equals(floatDataWritten, floatDataRead));
        byte[] byteDataBlockRead = reader.readAsByteArrayBlock("f", 2, 1);
        assertEquals(16, byteDataBlockRead.length);
        assertEquals(floatDataWritten[2],
                NativeData.byteToDouble(byteDataBlockRead, ByteOrder.NATIVE, 0, 1)[0]);
        assertEquals(floatDataWritten[3],
                NativeData.byteToDouble(byteDataBlockRead, ByteOrder.NATIVE, 8, 1)[0]);

        byteDataBlockRead = reader.readAsByteArrayBlockWithOffset("f", 2, 1);
        assertEquals(16, byteDataBlockRead.length);
        assertEquals(floatDataWritten[1],
                NativeData.byteToDouble(byteDataBlockRead, ByteOrder.NATIVE, 0, 1)[0]);
        assertEquals(floatDataWritten[2],
                NativeData.byteToDouble(byteDataBlockRead, ByteOrder.NATIVE, 8, 1)[0]);
        final double[][] values =
            {
                { 2.8, 8.2, -3.1 },
                { 0.0, 10000.0 } };
        int i = 0;
        for (HDF5DataBlock<byte[]> block : reader.getAsByteArrayNaturalBlocks("f"))
        {
            assertEquals(i, block.getIndex());
            assertEquals(i * 3, block.getOffset());
            assertTrue(Arrays.equals(values[i],
                    NativeData.byteToDouble(block.getData(), ByteOrder.NATIVE)));
            ++i;
        }
    }

    @Test
    public void testFloatArrayTypeDataSetOverwrite()
    {
        final File datasetFile = new File(workingDirectory, "floatArrayTypeDataSetOverwrite.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final HDF5ArrayTypeFloatWriter efWriter = new HDF5ArrayTypeFloatWriter((HDF5Writer) writer);
        final float[] floatDataWritten = new float[]
            { 2.8f, 8.2f, -3.1f, 0.0f, 10000.0f };
        efWriter.writeFloatArrayArrayType("f", floatDataWritten);
        final float[] floatDataWritten2 = new float[]
            { 0.1f, 8.2f, -3.1f, 0.0f, 20000.0f };
        writer.writeFloatArray("f", floatDataWritten2);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals("FLOAT(4):{5}", reader.getDataSetInformation("f").toString());
        final float[] floatDataRead = reader.readFloatArray("f");
        assertTrue(Arrays.equals(floatDataWritten2, floatDataRead));
    }

    @Test
    public void testFloatMDArrayTypeDataSet()
    {
        final File datasetFile = new File(workingDirectory, "floatMDArrayTypeDataSet.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final HDF5ArrayTypeFloatWriter efWriter = new HDF5ArrayTypeFloatWriter((HDF5Writer) writer);
        final MDFloatArray floatDataWritten = new MDFloatArray(new float[]
            { 2.8f, 8.2f, -3.1f, 0.0f, 10000.0f, 1.111f }, new int[]
            { 3, 2 });
        efWriter.writeFloatArrayArrayType("f", floatDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals("FLOAT(4, [3,2]):{}", reader.getDataSetInformation("f").toString());
        final MDFloatArray floatDataRead = reader.readFloatMDArray("f");
        assertEquals(floatDataWritten, floatDataRead);
    }

    @Test
    public void testFloatArrayCreateCompactOverwriteBlock()
    {
        final File datasetFile =
                new File(workingDirectory, "testFloatArrayCreateCompactOverwroteBlock.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeFloatArray("f", new float[]
            { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, HDF5FloatStorageFeatures.FLOAT_COMPACT);
        writer.writeFloatArrayBlockWithOffset("f", new float[]
            { 400, 500, 600 }, 3, 3);
        float[] arrayWritten = new float[]
            { 1, 2, 3, 400, 500, 600, 7, 8, 9, 10 };
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertTrue(Arrays.equals(arrayWritten, reader.readFloatArray("f")));
    }

    @Test
    public void testReadFloatMatrixDataSetBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "readFloatMatrixBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final float[][] floatMatrix = new float[10][10];
        for (int i = 0; i < floatMatrix.length; ++i)
        {
            for (int j = 0; j < floatMatrix[i].length; ++j)
            {
                floatMatrix[i][j] = i * j;
            }
        }
        writer.writeFloatMatrix(dsName, floatMatrix);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final int blockSize = 5;
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                final float[][] floatMatrixBlockRead =
                        reader.readFloatMatrixBlock(dsName, blockSize, blockSize, i, j);
                assertEquals(blockSize, floatMatrixBlockRead.length);
                assertEquals(blockSize, floatMatrixBlockRead[0].length);
                final float[][] floatMatrixBlockExpected = new float[blockSize][];
                for (int k = 0; k < blockSize; ++k)
                {
                    final float[] rowExpected = new float[blockSize];
                    System.arraycopy(floatMatrix[i * blockSize + k], blockSize * j, rowExpected, 0,
                            blockSize);
                    floatMatrixBlockExpected[k] = rowExpected;
                }
                assertMatrixEquals(floatMatrixBlockExpected, floatMatrixBlockRead);
            }
        }
        reader.close();
    }

    @Test
    public void testSetExtentBug()
    {
        final File datasetFile = new File(workingDirectory, "setExtentBug.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final float[][] floatMatrixBlockWritten = new float[][]
            {
                { 1, 2 },
                { 3, 4 } };
        final int blockSize = 2;
        writer.createFloatMatrix(dsName, 0, 0, blockSize, blockSize);
        writer.writeFloatMatrixBlock(dsName, floatMatrixBlockWritten, 0, 0);
        writer.writeFloatMatrixBlock(dsName, floatMatrixBlockWritten, 0, 1);
        // The next line will make the the block (0,1) disappear if the bug is present.
        writer.writeFloatMatrixBlock(dsName, floatMatrixBlockWritten, 1, 0);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[][] floatMatrixBlockRead =
                reader.readFloatMatrixBlock(dsName, blockSize, blockSize, 0, 1);
        assertMatrixEquals(floatMatrixBlockWritten, floatMatrixBlockRead);
        reader.close();
    }

    @Test
    public void testWriteFloatMatrixDataSetBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "writeFloatMatrixBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final float[][] floatMatrixBlockWritten = new float[5][5];
        for (int i = 0; i < floatMatrixBlockWritten.length; ++i)
        {
            for (int j = 0; j < floatMatrixBlockWritten[i].length; ++j)
            {
                floatMatrixBlockWritten[i][j] = i * j;
            }
        }
        final int blockSize = 5;
        writer.createFloatMatrix(dsName, 2 * blockSize, 2 * blockSize, blockSize, blockSize);
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                writer.writeFloatMatrixBlock(dsName, floatMatrixBlockWritten, i, j);
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                final float[][] floatMatrixBlockRead =
                        reader.readFloatMatrixBlock(dsName, blockSize, blockSize, i, j);
                assertMatrixEquals(floatMatrixBlockWritten, floatMatrixBlockRead);
            }
        }
        reader.close();
    }

    @Test
    public void testWriteFloatMatrixDataSetBlockWiseWithOffset()
    {
        final File datasetFile =
                new File(workingDirectory, "writeFloatMatrixBlockWiseWithOffset.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final float[][] floatMatrixBlockWritten = new float[5][5];
        int count = 0;
        for (int i = 0; i < floatMatrixBlockWritten.length; ++i)
        {
            for (int j = 0; j < floatMatrixBlockWritten[i].length; ++j)
            {
                floatMatrixBlockWritten[i][j] = ++count;
            }
        }
        final int blockSize = 5;
        final int offsetX = 2;
        final int offsetY = 3;
        writer.createFloatMatrix(dsName, 2 * blockSize, 2 * blockSize, blockSize, blockSize);
        writer.writeFloatMatrixBlockWithOffset(dsName, floatMatrixBlockWritten, 5, 5, offsetX,
                offsetY);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[][] floatMatrixBlockRead =
                reader.readFloatMatrixBlockWithOffset(dsName, blockSize, blockSize, offsetX,
                        offsetY);
        assertMatrixEquals(floatMatrixBlockWritten, floatMatrixBlockRead);
        final float[][] floatMatrixRead = reader.readFloatMatrix(dsName);
        // Subtract the non-zero block.
        for (int i = 0; i < floatMatrixBlockWritten.length; ++i)
        {
            for (int j = 0; j < floatMatrixBlockWritten[i].length; ++j)
            {
                floatMatrixRead[offsetX + i][offsetY + j] -= floatMatrixBlockWritten[i][j];
            }
        }
        for (int i = 0; i < floatMatrixRead.length; ++i)
        {
            for (int j = 0; j < floatMatrixRead[i].length; ++j)
            {
                assertEquals(i + ":" + j, 0.0f, floatMatrixRead[i][j]);
            }
        }
        reader.close();
    }

    @Test
    public void testReadByteArrayDataSetBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "readByteArrayBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final byte[] byteArray = new byte[100];
        for (int i = 0; i < byteArray.length; ++i)
        {
            byteArray[i] = (byte) (100 + i);
        }
        writer.writeByteArray(dsName, byteArray);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final int blockSize = 10;
        for (int i = 0; i < 10; ++i)
        {
            final byte[] byteArrayBlockRead = reader.readByteArrayBlock(dsName, blockSize, i);
            assertEquals(blockSize, byteArrayBlockRead.length);
            final byte[] byteArrayBlockExpected = new byte[blockSize];
            System.arraycopy(byteArray, blockSize * i, byteArrayBlockExpected, 0, blockSize);
            assertTrue("Block " + i, Arrays.equals(byteArrayBlockExpected, byteArrayBlockRead));
        }
        reader.close();
    }

    @Test
    public void testWriteByteArrayDataSetBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "writeByteArrayBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int size = 100;
        final int blockSize = 10;
        final int numberOfBlocks = 10;
        writer.createByteArray(dsName, size, blockSize, INT_DEFLATE);
        final byte[] block = new byte[blockSize];
        for (int i = 0; i < numberOfBlocks; ++i)
        {
            Arrays.fill(block, (byte) i);
            writer.writeByteArrayBlock(dsName, block, i);
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] byteArrayRead = reader.readAsByteArray(dsName);
        reader.close();
        assertEquals(size, byteArrayRead.length);
        for (int i = 0; i < byteArrayRead.length; ++i)
        {
            assertEquals("Byte " + i, (i / blockSize), byteArrayRead[i]);
        }
    }

    @Test
    public void testCreateByteArrayDataSetBlockSize0()
    {
        final File datasetFile = new File(workingDirectory, "testCreateByteArrayDataSetBlockSize0");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int size = 100;
        final int blockSize = 10;
        final int numberOfBlocks = 10;
        final int nominalBlockSize = 0;
        writer.createByteArray(dsName, size, nominalBlockSize, INT_DEFLATE);
        final byte[] block = new byte[blockSize];
        for (int i = 0; i < numberOfBlocks; ++i)
        {
            Arrays.fill(block, (byte) i);
            writer.writeByteArrayBlock(dsName, block, i);
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] byteArrayRead = reader.readAsByteArray(dsName);
        reader.close();
        assertEquals(size, byteArrayRead.length);
        for (int i = 0; i < byteArrayRead.length; ++i)
        {
            assertEquals("Byte " + i, (i / blockSize), byteArrayRead[i]);
        }
    }

    @Test
    public void testCreateFloatArrayWithDifferentStorageLayouts()
    {
        final File datasetFile =
                new File(workingDirectory, "testCreateFloatArrayWithDifferentStorageLayouts");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName1 = "ds1";
        final String dsName2 = "ds2";
        final int size = 100;
        writer.createFloatArray(dsName1, size, HDF5FloatStorageFeatures.FLOAT_CONTIGUOUS);
        writer.createFloatArray(dsName2, size, HDF5FloatStorageFeatures.FLOAT_CHUNKED);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info1 = reader.getDataSetInformation(dsName1);
        final HDF5DataSetInformation info2 = reader.getDataSetInformation(dsName2);
        reader.close();
        assertEquals(HDF5StorageLayout.CONTIGUOUS, info1.getStorageLayout());
        assertEquals(size, info1.getDimensions()[0]);
        assertNull(info1.tryGetChunkSizes());
        assertEquals(HDF5StorageLayout.CHUNKED, info2.getStorageLayout());
        assertEquals(0, info2.getDimensions()[0]);
        assertEquals(size, info2.tryGetChunkSizes()[0]);
    }

    @Test
    public void testWriteByteArrayDataSetBlockWiseExtend()
    {
        final File datasetFile = new File(workingDirectory, "writeByteArrayBlockWiseExtend.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int size = 100;
        final int blockSize = 10;
        final int numberOfBlocks = 10;
        writer.createByteArray(dsName, 0, blockSize, INT_DEFLATE);
        final byte[] block = new byte[blockSize];
        for (int i = 0; i < numberOfBlocks; ++i)
        {
            Arrays.fill(block, (byte) i);
            writer.writeByteArrayBlock(dsName, block, i);
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] byteArrayRead = reader.readAsByteArray(dsName);
        reader.close();
        assertEquals(size, byteArrayRead.length);
        for (int i = 0; i < byteArrayRead.length; ++i)
        {
            assertEquals("Byte " + i, (i / blockSize), byteArrayRead[i]);
        }
    }

    @Test
    public void testWriteByteArrayDataSetBlockWiseMismatch()
    {
        final File datasetFile = new File(workingDirectory, "writeByteArrayBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int size = 99;
        final int blockSize = 10;
        final int numberOfBlocks = 10;
        writer.createByteArray(dsName, size, blockSize, INT_DEFLATE);
        final byte[] block = new byte[blockSize];
        for (int i = 0; i < numberOfBlocks; ++i)
        {
            Arrays.fill(block, (byte) i);
            if (blockSize * (i + 1) > size)
            {
                final int ofs = blockSize * i;
                writer.writeByteArrayBlockWithOffset(dsName, block, size - ofs, ofs);
            } else
            {
                writer.writeByteArrayBlock(dsName, block, i);
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] byteArrayRead = reader.readByteArray(dsName);
        reader.close();
        assertEquals(size, byteArrayRead.length);
        for (int i = 0; i < byteArrayRead.length; ++i)
        {
            assertEquals("Byte " + i, (i / blockSize), byteArrayRead[i]);
        }
    }

    @Test
    public void testWriteOpaqueByteArrayDataSetBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "writeOpaqueByteArrayBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int size = 100;
        final int blockSize = 10;
        final int numberOfBlocks = 10;
        final HDF5OpaqueType opaqueDataType =
                writer.createOpaqueByteArray(dsName, "TAG", size / 2, blockSize,
                        GENERIC_DEFLATE_MAX);
        final byte[] block = new byte[blockSize];
        for (int i = 0; i < numberOfBlocks; ++i)
        {
            Arrays.fill(block, (byte) i);
            writer.writeOpaqueByteArrayBlock(dsName, opaqueDataType, block, i);
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] byteArrayRead = reader.readAsByteArray(dsName);
        reader.close();
        assertEquals(size, byteArrayRead.length);
        for (int i = 0; i < byteArrayRead.length; ++i)
        {
            assertEquals("Byte " + i, (i / blockSize), byteArrayRead[i]);
        }
    }

    @Test
    public void testWriteOpaqueByteArrayDataSetBlockWiseMismatch()
    {
        final File datasetFile = new File(workingDirectory, "writeOpaqueByteArrayBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int size = 99;
        final int blockSize = 10;
        final int numberOfBlocks = 10;
        final HDF5OpaqueType opaqueDataType =
                writer.createOpaqueByteArray(dsName, "TAG", size, blockSize, GENERIC_DEFLATE);
        final byte[] block = new byte[blockSize];
        for (int i = 0; i < numberOfBlocks; ++i)
        {
            Arrays.fill(block, (byte) i);
            if (blockSize * (i + 1) > size)
            {
                final int ofs = blockSize * i;
                writer.writeOpaqueByteArrayBlockWithOffset(dsName, opaqueDataType, block, size
                        - ofs, ofs);
            } else
            {
                writer.writeOpaqueByteArrayBlock(dsName, opaqueDataType, block, i);
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] byteArrayRead = reader.readAsByteArray(dsName);
        reader.close();
        assertEquals(size, byteArrayRead.length);
        for (int i = 0; i < byteArrayRead.length; ++i)
        {
            assertEquals("Byte " + i, (i / blockSize), byteArrayRead[i]);
        }
    }

    @Test
    public void testWriteByteMatrixDataSetBlockWise()
    {
        final File datasetFile = new File(workingDirectory, "writeByteMatrixBlockWise.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int sizeX = 100;
        final int sizeY = 10;
        final int blockSizeX = 10;
        final int blockSizeY = 5;
        final int numberOfBlocksX = 10;
        final int numberOfBlocksY = 2;
        writer.createByteMatrix(dsName, sizeX, sizeY, blockSizeX, blockSizeY, INT_DEFLATE);
        final byte[][] block = new byte[blockSizeX][blockSizeY];
        for (int i = 0; i < numberOfBlocksX; ++i)
        {
            for (int j = 0; j < numberOfBlocksY; ++j)
            {
                for (int k = 0; k < blockSizeX; ++k)
                {
                    Arrays.fill(block[k], (byte) (i + j));
                }
                writer.writeByteMatrixBlock(dsName, block, i, j);
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[][] byteMatrixRead = reader.readByteMatrix(dsName);
        reader.close();
        assertEquals(sizeX, byteMatrixRead.length);
        for (int i = 0; i < byteMatrixRead.length; ++i)
        {
            for (int j = 0; j < byteMatrixRead[i].length; ++j)
            {
                assertEquals("Byte (" + i + "," + j + ")", (i / blockSizeX + j / blockSizeY),
                        byteMatrixRead[i][j]);
            }
        }
    }

    @Test
    public void testWriteByteMatrixDataSetBlockWiseMismatch()
    {
        final File datasetFile = new File(workingDirectory, "writeByteMatrixBlockWiseMismatch.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final int sizeX = 99;
        final int sizeY = 12;
        final int blockSizeX = 10;
        final int blockSizeY = 5;
        final int numberOfBlocksX = 10;
        final int numberOfBlocksY = 3;
        writer.createByteMatrix(dsName, sizeX, sizeY, blockSizeX, blockSizeY, INT_DEFLATE);
        final byte[][] block = new byte[blockSizeX][blockSizeY];
        for (int i = 0; i < numberOfBlocksX; ++i)
        {
            for (int j = 0; j < numberOfBlocksY; ++j)
            {
                for (int k = 0; k < blockSizeX; ++k)
                {
                    Arrays.fill(block[k], (byte) (i + j));
                }
                writer.writeByteMatrixBlockWithOffset(dsName, block,
                        Math.min(blockSizeX, sizeX - i * blockSizeX),
                        Math.min(blockSizeY, sizeY - j * blockSizeY), i * blockSizeX, j
                                * blockSizeY);
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[][] byteMatrixRead = reader.readByteMatrix(dsName);
        reader.close();
        assertEquals(sizeX, byteMatrixRead.length);
        for (int i = 0; i < byteMatrixRead.length; ++i)
        {
            for (int j = 0; j < byteMatrixRead[i].length; ++j)
            {
                assertEquals("Byte (" + i + "," + j + ")", (i / blockSizeX + j / blockSizeY),
                        byteMatrixRead[i][j]);
            }
        }
    }

    @Test
    public void testReadToFloatMDArray()
    {
        final File datasetFile = new File(workingDirectory, "readToFloatMDArray.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final MDFloatArray arrayWritten = new MDFloatArray(new float[]
            { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[]
            { 3, 3 });
        writer.writeFloatMDArray(dsName, arrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final MDFloatArray arrayRead = new MDFloatArray(new int[]
            { 10, 10 });
        final int memOfsX = 2;
        final int memOfsY = 3;
        reader.readToFloatMDArrayWithOffset(dsName, arrayRead, new int[]
            { memOfsX, memOfsY });
        reader.close();
        final boolean[][] isSet = new boolean[10][10];
        for (int i = 0; i < arrayWritten.size(0); ++i)
        {
            for (int j = 0; j < arrayWritten.size(1); ++j)
            {
                isSet[memOfsX + i][memOfsY + j] = true;
                assertEquals("(" + i + "," + j + ")", arrayWritten.get(i, j),
                        arrayRead.get(memOfsX + i, memOfsY + j));
            }
        }
        for (int i = 0; i < arrayRead.size(0); ++i)
        {
            for (int j = 0; j < arrayRead.size(1); ++j)
            {
                if (isSet[i][j] == false)
                {
                    assertEquals("(" + i + "," + j + ")", 0f, arrayRead.get(i, j));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @DataProvider
    private Object[][] provideSizes()
    {
        return new Object[][]
            {
                { 10, 99 },
                { 10, 100 },
                { 10, 101 } };
    }

    @Test(dataProvider = "provideSizes")
    public void testIterateOverFloatArrayInNaturalBlocks(int blockSize, int dataSetSize)
    {
        final File datasetFile =
                new File(workingDirectory, "iterateOverFloatArrayInNaturalBlocks.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final float[] arrayWritten = new float[dataSetSize];
        for (int i = 0; i < dataSetSize; ++i)
        {
            arrayWritten[i] = i;
        }
        writer.createFloatArray(dsName, dataSetSize, blockSize);
        writer.writeFloatArrayBlock(dsName, arrayWritten, 0);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        int i = 0;
        for (HDF5DataBlock<float[]> block : reader.getFloatArrayNaturalBlocks(dsName))
        {
            assertEquals(i, block.getIndex());
            assertEquals(blockSize * i, block.getOffset());
            final float[] arrayReadBlock = block.getData();
            if (blockSize * (i + 1) > dataSetSize)
            {
                assertEquals(dataSetSize - i * blockSize, arrayReadBlock.length);
            } else
            {
                assertEquals(blockSize, arrayReadBlock.length);
            }
            final float[] arrayWrittenBlock = new float[arrayReadBlock.length];
            System.arraycopy(arrayWritten, (int) block.getOffset(), arrayWrittenBlock, 0,
                    arrayWrittenBlock.length);
            assertTrue(Arrays.equals(arrayWrittenBlock, arrayReadBlock));
            ++i;
        }
        assertEquals(dataSetSize / blockSize + (dataSetSize % blockSize != 0 ? 1 : 0), i);
        reader.close();
    }

    @Test
    public void testReadToFloatMDArrayBlockWithOffset()
    {
        final File datasetFile = new File(workingDirectory, "readToFloatMDArrayBlockWithOffset.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final MDFloatArray arrayWritten = new MDFloatArray(new float[]
            { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[]
            { 3, 3 });
        writer.writeFloatMDArray(dsName, arrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final MDFloatArray arrayRead = new MDFloatArray(new int[]
            { 10, 10 });
        final int memOfsX = 2;
        final int memOfsY = 3;
        final int diskOfsX = 1;
        final int diskOfsY = 0;
        final int blockSizeX = 3;
        final int blockSizeY = 2;
        final int[] effectiveDimensions =
                reader.readToFloatMDArrayBlockWithOffset(dsName, arrayRead, new int[]
                    { blockSizeX, blockSizeY }, new long[]
                    { diskOfsX, diskOfsY }, new int[]
                    { memOfsX, memOfsY });
        reader.close();
        assertEquals(blockSizeX - 1, effectiveDimensions[0]);
        assertEquals(blockSizeY, effectiveDimensions[1]);
        final boolean[][] isSet = new boolean[10][10];
        for (int i = 0; i < effectiveDimensions[0]; ++i)
        {
            for (int j = 0; j < effectiveDimensions[1]; ++j)
            {
                isSet[memOfsX + i][memOfsY + j] = true;
                assertEquals("(" + i + "," + j + ")", arrayWritten.get(diskOfsX + i, diskOfsY + j),
                        arrayRead.get(memOfsX + i, memOfsY + j));
            }
        }
        for (int i = 0; i < arrayRead.size(0); ++i)
        {
            for (int j = 0; j < arrayRead.size(1); ++j)
            {
                if (isSet[i][j] == false)
                {
                    assertEquals("(" + i + "," + j + ")", 0f, arrayRead.get(i, j));
                }
            }
        }
    }

    @Test(dataProvider = "provideSizes")
    public void testIterateOverStringArrayInNaturalBlocks(int blockSize, int dataSetSize)
    {
        final File datasetFile =
                new File(workingDirectory, "testIterateOverStringArrayInNaturalBlocks.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final String[] arrayWritten = new String[dataSetSize];
        for (int i = 0; i < dataSetSize; ++i)
        {
            arrayWritten[i] = "" + i;
        }
        writer.createStringArray(dsName, dataSetSize, blockSize);
        writer.writeStringArrayBlock(dsName, arrayWritten, 0);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        int i = 0;
        for (HDF5DataBlock<String[]> block : reader.getStringArrayNaturalBlocks(dsName))
        {
            assertEquals(i, block.getIndex());
            assertEquals(blockSize * i, block.getOffset());
            final String[] arrayReadBlock = block.getData();
            if (blockSize * (i + 1) > dataSetSize)
            {
                assertEquals(dataSetSize - i * blockSize, arrayReadBlock.length);
            } else
            {
                assertEquals(blockSize, arrayReadBlock.length);
            }
            final String[] arrayWrittenBlock = new String[arrayReadBlock.length];
            System.arraycopy(arrayWritten, (int) block.getOffset(), arrayWrittenBlock, 0,
                    arrayWrittenBlock.length);
            assertTrue(Arrays.equals(arrayWrittenBlock, arrayReadBlock));
            ++i;
        }
        assertEquals(dataSetSize / blockSize + (dataSetSize % blockSize != 0 ? 1 : 0), i);
        reader.close();
    }

    @SuppressWarnings("unused")
    @DataProvider
    private Object[][] provideMDSizes()
    {
        return new Object[][]
            {
                { new int[]
                    { 2, 2 }, new long[]
                    { 4, 3 }, new float[]
                    { 0f, 2f, 6f, 8f }, new int[][]
                    {
                        { 2, 2 },
                        { 2, 1 },
                        { 2, 2 },
                        { 2, 1 } } },
                { new int[]
                    { 2, 2 }, new long[]
                    { 4, 4 }, new float[]
                    { 0f, 2f, 8f, 10f }, new int[][]
                    {
                        { 2, 2 },
                        { 2, 2 },
                        { 2, 2 },
                        { 2, 2 } } },
                { new int[]
                    { 2, 2 }, new long[]
                    { 4, 5 }, new float[]
                    { 0f, 2f, 4f, 10f, 12f, 14f }, new int[][]
                    {
                        { 2, 2 },
                        { 2, 2 },
                        { 2, 1 },
                        { 2, 2 },
                        { 2, 2 },
                        { 2, 1 } } },
                { new int[]
                    { 3, 2 }, new long[]
                    { 5, 4 }, new float[]
                    { 0f, 2f, 12f, 14f }, new int[][]
                    {
                        { 3, 2 },
                        { 3, 2 },
                        { 2, 2 },
                        { 2, 2 } } },
                { new int[]
                    { 2, 2 }, new long[]
                    { 5, 4 }, new float[]
                    { 0f, 2f, 8f, 10f, 16f, 18f }, new int[][]
                    {
                        { 2, 2 },
                        { 2, 2 },
                        { 2, 2 },
                        { 2, 2 },
                        { 1, 2 },
                        { 1, 2 } } }, };
    }

    @Test(dataProvider = "provideMDSizes")
    public void testIterateOverMDFloatArrayInNaturalBlocks(int[] blockSize, long[] dataSetSize,
            float[] firstNumberPerIteration, int[][] blockSizePerIteration)
    {
        final File datasetFile =
                new File(workingDirectory, "iterateOverMDFloatArrayInNaturalBlocks.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final float[] flattenedArray = new float[getNumberOfElements(dataSetSize)];
        for (int i = 0; i < flattenedArray.length; ++i)
        {
            flattenedArray[i] = i;
        }
        final MDFloatArray arrayWritten = new MDFloatArray(flattenedArray, dataSetSize);
        writer.createFloatMDArray(dsName, dataSetSize, blockSize);
        writer.writeFloatMDArrayBlock(dsName, arrayWritten, new long[blockSize.length]);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        int i = 0;
        for (HDF5MDDataBlock<MDFloatArray> block : reader.getFloatMDArrayNaturalBlocks(dsName))
        {
            assertEquals(firstNumberPerIteration[i], block.getData().get(0, 0));
            assertTrue(Arrays.equals(block.getData().dimensions(), blockSizePerIteration[i]));
            ++i;
        }
        assertEquals(firstNumberPerIteration.length, i);
        reader.close();
    }

    private static int getNumberOfElements(long[] size)
    {
        int elements = 1;
        for (long dim : size)
        {
            elements *= dim;
        }
        return elements;
    }

    @Test
    public void testStringArray()
    {
        final File stringArrayFile = new File(workingDirectory, "stringArray.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final String[] data = new String[]
            { "abc", "ABCxxx", "xyz" };
        final String dataSetName = "/aStringArray";
        writer.writeStringArray(dataSetName, data);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        final String[] dataStored = reader.readStringArray(dataSetName);
        assertTrue(Arrays.equals(data, dataStored));
        reader.close();
    }

    public void testStringArrayBlock()
    {
        final File stringArrayFile = new File(workingDirectory, "stringArrayBlock.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final String[] data = new String[]
            { "abc", "ABCxxx", "xyz" };
        final String dataSetName = "/aStringArray";
        writer.createStringArray(dataSetName, 6, 5, 3);
        writer.writeStringArrayBlock(dataSetName, data, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        String[] dataStored = reader.readStringArray(dataSetName);
        assertTrue(Arrays.equals(new String[]
            { "", "", "", data[0], data[1], data[2] }, dataStored));
        dataStored = reader.readStringArrayBlock(dataSetName, 3, 0);
        assertTrue(Arrays.equals(new String[]
            { "", "", "" }, dataStored));
        dataStored = reader.readStringArrayBlock(dataSetName, 3, 1);
        assertTrue(Arrays.equals(data, dataStored));
        dataStored = reader.readStringArrayBlockWithOffset(dataSetName, 3, 2);
        assertTrue(Arrays.equals(new String[]
            { "", data[0], data[1] }, dataStored));
        reader.close();
    }

    @Test
    public void testStringArrayBlockCompact()
    {
        final File stringArrayFile = new File(workingDirectory, "stringArrayBlockCompact.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final String[] data = new String[]
            { "abc", "ABCxxx", "xyz" };
        final String dataSetName = "/aStringArray";
        writer.createStringArray(dataSetName, 6, 6, HDF5GenericStorageFeatures.GENERIC_COMPACT);
        writer.writeStringArrayBlock(dataSetName, data, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        String[] dataStored = reader.readStringArray(dataSetName);
        assertTrue(Arrays.equals(new String[]
            { "", "", "", data[0], data[1], data[2] }, dataStored));
        dataStored = reader.readStringArrayBlock(dataSetName, 3, 0);
        assertTrue(Arrays.equals(new String[]
            { "", "", "" }, dataStored));
        dataStored = reader.readStringArrayBlock(dataSetName, 3, 1);
        assertTrue(Arrays.equals(data, dataStored));
        dataStored = reader.readStringArrayBlockWithOffset(dataSetName, 3, 2);
        assertTrue(Arrays.equals(new String[]
            { "", data[0], data[1] }, dataStored));
        reader.close();
    }

    @Test
    public void testStringArrayBlockVL()
    {
        final File stringArrayFile = new File(workingDirectory, "stringArrayBlockVL.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final String[] data1 = new String[]
            { "abc", "ABCxxx", "xyz" };
        final String[] data2 = new String[]
            { "abd", "ABDxxx", "xyw" };
        final String[] data = new String[]
            { "", "", "", "abc", "ABCxxx", "xyz", "abd", "ABDxxx", "xyw" };
        final String dataSetName = "/aStringArray";
        writer.createStringVariableLengthArray(dataSetName, 0, 5);
        writer.writeStringArrayBlock(dataSetName, data1, 1);
        writer.writeStringArrayBlock(dataSetName, data2, 2);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        String[] dataRead = reader.readStringArray(dataSetName);
        assertTrue(Arrays.equals(data, dataRead));
        dataRead = reader.readStringArrayBlock(dataSetName, 3, 1);
        assertTrue(Arrays.equals(data1, dataRead));
        dataRead = reader.readStringArrayBlock(dataSetName, 3, 2);
        assertTrue(Arrays.equals(data2, dataRead));
        dataRead = reader.readStringArrayBlockWithOffset(dataSetName, 3, 5);
        assertTrue(Arrays.equals(new String[]
            { "xyz", "abd", "ABDxxx" }, dataRead));
        reader.close();
    }

    @Test
    public void testStringArrayMD()
    {
        final File stringArrayFile = new File(workingDirectory, "stringMDArray.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final MDArray<String> data = new MDArray<String>(new String[]
            { "abc", "ABCxxx", "xyz", "DEF" }, new long[]
            { 2, 2 });
        final String dataSetName = "/aStringArray";
        writer.writeStringMDArray(dataSetName, data);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        final MDArray<String> dataStored = reader.readStringMDArray(dataSetName);
        assertTrue(Arrays.equals(data.getAsFlatArray(), dataStored.getAsFlatArray()));
        assertTrue(Arrays.equals(data.dimensions(), dataStored.dimensions()));
        reader.close();
    }

    @Test
    public void testStringArrayMDBlocks()
    {
        final File stringArrayFile = new File(workingDirectory, "stringMDArrayBlocks.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final String dataSetName = "/aStringArray";
        writer.createStringMDArray(dataSetName, 6, new long[]
            { 4, 4 }, new int[]
            { 2, 2 });
        final MDArray<String> data = new MDArray<String>(new String[]
            { "abc", "ABCxxx", "xyz", "DEF" }, new long[]
            { 2, 2 });
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                writer.writeStringMDArrayBlock(dataSetName, data, new long[]
                    { i, j });
            }
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        int i = 0;
        int j = 0;
        for (HDF5MDDataBlock<MDArray<String>> block : reader
                .getStringMDArrayNaturalBlocks(dataSetName))
        {
            assertTrue(Arrays.equals(data.getAsFlatArray(), block.getData().getAsFlatArray()));
            assertTrue(Arrays.equals(data.dimensions(), block.getData().dimensions()));
            assertTrue(Arrays.equals(new long[]
                { i, j }, block.getIndex()));
            if (++j > 1)
            {
                j = 0;
                ++i;
            }
        }
        reader.close();
    }

    @Test
    public void testStringMDArrayVL()
    {
        final File stringArrayFile = new File(workingDirectory, "stringMDArrayVL.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final MDArray<String> data = new MDArray<String>(new String[]
            { "abc", "ABCxxx", "xyz", "DEF" }, new long[]
            { 2, 2 });
        final String dataSetName = "/aStringArray";
        writer.writeStringVariableLengthMDArray(dataSetName, data);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        final MDArray<String> dataStored = reader.readStringMDArray(dataSetName);
        assertTrue(Arrays.equals(data.getAsFlatArray(), dataStored.getAsFlatArray()));
        assertTrue(Arrays.equals(data.dimensions(), dataStored.dimensions()));
        final HDF5DataSetInformation info = reader.getDataSetInformation(dataSetName);
        reader.close();
        assertTrue(info.getTypeInformation().isVariableLengthType());
        assertEquals("STRING(-1)", info.getTypeInformation().toString());
        assertEquals(HDF5StorageLayout.CHUNKED, info.getStorageLayout());
        assertTrue(Arrays.equals(new long[]
            { 2, 2 }, info.getDimensions()));
        assertTrue(Arrays.equals(new long[]
            { -1, -1 }, info.getMaxDimensions()));
        assertTrue(Arrays.equals(new int[]
            { 4, 4 }, info.tryGetChunkSizes()));
    }

    @Test
    public void testStringMDArrayVLBlocks()
    {
        final File stringArrayFile = new File(workingDirectory, "stringMDArrayVLBlocks.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(stringArrayFile);
        final long[] dims = new long[]
            { 8, 8 };
        final int[] blockSize = new int[]
            { 2, 2 };
        final MDArray<String> data = new MDArray<String>(new String[]
            { "abc", "ABCxxx", "xyz", "DEF" }, blockSize);
        final String dataSetName = "/aStringArray";
        writer.createStringVariableLengthMDArray(dataSetName, dims, blockSize);
        writer.writeStringMDArrayBlock(dataSetName, data, new long[]
            { 1, 1 });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        final MDArray<String> dataStored =
                reader.readStringMDArrayBlock(dataSetName, blockSize, new long[]
                    { 1, 1 });
        assertTrue(Arrays.equals(data.getAsFlatArray(), dataStored.getAsFlatArray()));
        assertTrue(Arrays.equals(data.dimensions(), dataStored.dimensions()));
        assertTrue(Arrays.equals(new String[]
            { "", "", "", "" }, reader.readStringMDArrayBlock(dataSetName, blockSize, new long[]
            { 1, 0 }).getAsFlatArray()));
        assertTrue(Arrays.equals(new String[]
            { "", "", "", "" }, reader.readStringMDArrayBlock(dataSetName, blockSize, new long[]
            { 0, 1 }).getAsFlatArray()));
        assertTrue(Arrays.equals(new String[]
            { "", "", "", "" }, reader.readStringMDArrayBlock(dataSetName, blockSize, new long[]
            { 2, 2 }).getAsFlatArray()));
        final HDF5DataSetInformation info = reader.getDataSetInformation(dataSetName);
        reader.close();
        assertTrue(info.getTypeInformation().isVariableLengthType());
        assertEquals("STRING(-1)", info.getTypeInformation().toString());
        assertEquals(HDF5StorageLayout.CHUNKED, info.getStorageLayout());
        assertTrue(Arrays.equals(dims, info.getDimensions()));
        assertTrue(Arrays.equals(new long[]
            { -1, -1 }, info.getMaxDimensions()));
        assertTrue(Arrays.equals(blockSize, info.tryGetChunkSizes()));
    }

    @Test
    public void testOverwriteString()
    {
        final File stringOverwriteFile = new File(workingDirectory, "overwriteString.h5");
        stringOverwriteFile.delete();
        assertFalse(stringOverwriteFile.exists());
        stringOverwriteFile.deleteOnExit();
        IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(stringOverwriteFile)
                        .dontUseExtendableDataTypes().writer();
        final String largeData = StringUtils.repeat("a", 12);
        final String smallData = "abc1234";
        final String dataSetName = "/aString";
        writer.writeString(dataSetName, smallData);
        writer.writeString(dataSetName, largeData);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringOverwriteFile);
        final String dataRead = reader.readString(dataSetName);
        assertEquals(largeData, dataRead);
        reader.close();
    }

    @Test
    public void testOverwriteStringWithLarge()
    {
        final File stringOverwriteFile = new File(workingDirectory, "overwriteStringWithLarge.h5");
        stringOverwriteFile.delete();
        assertFalse(stringOverwriteFile.exists());
        stringOverwriteFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().configure(stringOverwriteFile).writer();
        final String largeData = StringUtils.repeat("a", 64 * 1024);
        final String smallData = "abc1234";
        final String dataSetName = "/aString";
        writer.writeString(dataSetName, smallData);
        writer.writeString(dataSetName, largeData);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringOverwriteFile);
        final String dataRead = reader.readString(dataSetName);
        assertEquals(largeData, dataRead);
        reader.close();
    }

    @Test
    public void testOverwriteStringWithLargeKeepCompact()
    {
        final File stringOverwriteFile =
                new File(workingDirectory, "overwriteStringWithLargeKeepCompact.h5");
        stringOverwriteFile.delete();
        assertFalse(stringOverwriteFile.exists());
        stringOverwriteFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().configure(stringOverwriteFile).writer();
        final String largeData = StringUtils.repeat("a", 64 * 1024);
        final String smallData = "abc1234";
        final String dataSetName = "/aString";
        writer.writeString(dataSetName, smallData);
        writer.writeString(dataSetName, largeData,
                HDF5GenericStorageFeatures.GENERIC_CONTIGUOUS_KEEP);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringOverwriteFile);
        final String dataRead = reader.readString(dataSetName);
        assertEquals(largeData.substring(0, smallData.length()), dataRead);
        assertEquals(HDF5StorageLayout.COMPACT, reader.getDataSetInformation(dataSetName)
                .getStorageLayout());
        reader.close();
    }

    @Test
    public void testSmallString()
    {
        final File smallStringFile = new File(workingDirectory, "smallString.h5");
        smallStringFile.delete();
        assertFalse(smallStringFile.exists());
        smallStringFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().configure(smallStringFile).writer();
        final String dataSetName = "/aString";
        writer.writeString(dataSetName, "abc");
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(smallStringFile);
        final String dataRead = reader.readString(dataSetName);
        assertEquals("abc", dataRead);
        assertEquals(HDF5StorageLayout.COMPACT, reader.getDataSetInformation(dataSetName)
                .getStorageLayout());
        reader.close();
    }

    @Test
    public void testVeryLargeString()
    {
        final File veryLargeStringFile = new File(workingDirectory, "veryLargeString.h5");
        veryLargeStringFile.delete();
        assertFalse(veryLargeStringFile.exists());
        veryLargeStringFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().configure(veryLargeStringFile).writer();
        final String largeData = StringUtils.repeat("a", 64 * 1024);
        final String dataSetName = "/aString";
        writer.writeString(dataSetName, largeData);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(veryLargeStringFile);
        final String dataRead = reader.readString(dataSetName);
        assertEquals(largeData, dataRead);
        assertEquals(HDF5StorageLayout.CONTIGUOUS, reader.getDataSetInformation(dataSetName)
                .getStorageLayout());
        reader.close();
    }

    @Test
    public void testStringCompact()
    {
        final File stringCompactFile = new File(workingDirectory, "stringCompact.h5");
        stringCompactFile.delete();
        assertFalse(stringCompactFile.exists());
        stringCompactFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().configure(stringCompactFile).writer();
        final String smallData = "abc1234";
        final String dataSetName1 = "/aString";
        writer.writeString(dataSetName1, smallData, HDF5GenericStorageFeatures.GENERIC_COMPACT);
        final String dataSetName2 = "/anotherString";
        final String largeData = StringUtils.repeat("a", 64 * 1024 - 13);
        writer.writeString(dataSetName2, largeData, HDF5GenericStorageFeatures.GENERIC_COMPACT);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringCompactFile);
        final String dataRead1 = reader.readString(dataSetName1);
        assertEquals(HDF5StorageLayout.COMPACT, reader.getDataSetInformation(dataSetName1)
                .getStorageLayout());
        assertEquals(smallData, dataRead1);
        final String dataRead2 = reader.readString(dataSetName2);
        assertEquals(HDF5StorageLayout.COMPACT, reader.getDataSetInformation(dataSetName2)
                .getStorageLayout());
        assertEquals(largeData, dataRead2);
        reader.close();
    }

    @Test
    public void testStringContiguous()
    {
        final File stringCompactFile = new File(workingDirectory, "stringContiguous.h5");
        stringCompactFile.delete();
        assertFalse(stringCompactFile.exists());
        stringCompactFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().configure(stringCompactFile).writer();
        final String smallData = "abc1234";
        final String dataSetName1 = "/aString";
        writer.writeString(dataSetName1, smallData, HDF5GenericStorageFeatures.GENERIC_CONTIGUOUS);
        final String dataSetName2 = "/anotherString";
        final String largeData = StringUtils.repeat("a", 64 * 1024 - 13);
        writer.writeString(dataSetName2, largeData, HDF5GenericStorageFeatures.GENERIC_CONTIGUOUS);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringCompactFile);
        final String dataRead1 = reader.readString(dataSetName1);
        assertEquals(HDF5StorageLayout.CONTIGUOUS, reader.getDataSetInformation(dataSetName1)
                .getStorageLayout());
        assertEquals(smallData, dataRead1);
        final String dataRead2 = reader.readString(dataSetName2);
        assertEquals(HDF5StorageLayout.CONTIGUOUS, reader.getDataSetInformation(dataSetName2)
                .getStorageLayout());
        assertEquals(largeData, dataRead2);
        reader.close();
    }

    @Test
    public void testStringUnicode() throws Exception
    {
        final File stringUnicodeFile = new File(workingDirectory, "stringUnicode.h5");
        stringUnicodeFile.delete();
        assertFalse(stringUnicodeFile.exists());
        stringUnicodeFile.deleteOnExit();
        IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(stringUnicodeFile).dontUseExtendableDataTypes()
                        .useUTF8CharacterEncoding().writer();
        final String uniCodeData = "\u00b6\u00bc\u09ab";
        final String dataSetName = "/aString";
        final String attributeName = "attr1";
        final String uniCodeAttributeData = "\u09bb";
        writer.writeString(dataSetName, uniCodeData);
        writer.setStringAttribute(dataSetName, attributeName, uniCodeAttributeData);
        writer.close();
        final IHDF5Reader reader =
                HDF5FactoryProvider.get().configureForReading(stringUnicodeFile)
                        .useUTF8CharacterEncoding().reader();
        final String dataRead = reader.readString(dataSetName);
        final String attributeDataRead = reader.getStringAttribute(dataSetName, attributeName);
        assertEquals(uniCodeData, dataRead);
        assertEquals(uniCodeAttributeData, attributeDataRead);
        reader.close();
    }

    @Test
    public void testStringArrayCompact()
    {
        final File stringArrayFile = new File(workingDirectory, "stringArrayCompact.h5");
        stringArrayFile.delete();
        assertFalse(stringArrayFile.exists());
        stringArrayFile.deleteOnExit();
        IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(stringArrayFile).dontUseExtendableDataTypes()
                        .writer();
        final String[] data = new String[]
            { "abc1234", "ABCxxxX", "xyzUVWX" };
        final String dataSetName = "/aStringArray";
        writer.writeStringArray(dataSetName, data);
        writer.close();
        writer = HDF5FactoryProvider.get().open(stringArrayFile);
        writer.writeStringArray(dataSetName, new String[]
            { data[0], data[1] });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(stringArrayFile);
        String[] dataStored = reader.readStringArray(dataSetName);
        assertTrue(Arrays.equals(new String[]
            { data[0], data[1] }, dataStored));
        reader.close();
    }

    @Test
    public void testStringCompression()
    {
        final File compressedStringFile = new File(workingDirectory, "compressedStrings.h5");
        compressedStringFile.delete();
        assertFalse(compressedStringFile.exists());
        compressedStringFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(compressedStringFile);
        final int size = 100000;
        final String dataSetName = "/hopefullyCompressedString";
        final String longMonotonousString = StringUtils.repeat("a", size);
        writer.writeString(dataSetName, longMonotonousString, GENERIC_DEFLATE);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(compressedStringFile);
        final String longMonotonousStringStored = reader.readString(dataSetName);
        assertEquals(longMonotonousString, longMonotonousStringStored);
        reader.close();
        assertTrue(Long.toString(compressedStringFile.length()),
                compressedStringFile.length() < size / 10);
    }

    @Test
    public void testStringArrayCompression()
    {
        final File compressedStringArrayFile =
                new File(workingDirectory, "compressedStringArray.h5");
        compressedStringArrayFile.delete();
        assertFalse(compressedStringArrayFile.exists());
        compressedStringArrayFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(compressedStringArrayFile);
        final int size = 100000;
        final String longMonotonousString = StringUtils.repeat("a", size);
        final String[] data = new String[]
            { longMonotonousString, longMonotonousString, longMonotonousString };
        final String dataSetName = "/aHopeFullyCompressedStringArray";
        writer.writeStringArray(dataSetName, data, size, GENERIC_DEFLATE_MAX);
        writer.close();
        final IHDF5Reader reader =
                HDF5FactoryProvider.get().openForReading(compressedStringArrayFile);
        final String[] dataStored = reader.readStringArray(dataSetName);
        assertTrue(Arrays.equals(data, dataStored));
        reader.close();
        assertTrue(Long.toString(compressedStringArrayFile.length()),
                compressedStringArrayFile.length() < 3 * size / 10);
    }

    private void assertMatrixEquals(final float[][] floatMatrixWritten,
            final float[][] floatMatrixRead)
    {
        assertEquals(floatMatrixWritten.length, floatMatrixRead.length);
        for (int i = 0; i < floatMatrixWritten.length; ++i)
        {
            assertEquals(floatMatrixWritten[i].length, floatMatrixRead[i].length);
            for (int j = 0; j < floatMatrixWritten[i].length; ++j)
            {
                assertEquals(i + ":" + j, floatMatrixWritten[i][j], floatMatrixRead[i][j]);
            }
        }
    }

    @Test
    public void testCompressedDataSet()
    {
        final File datasetFile = new File(workingDirectory, "compressed.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String stringDatasetName = "/compressed";
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < 10000; ++i)
        {
            b.append("easyToCompress");
        }
        writer.writeByteArray(stringDatasetName, b.toString().getBytes(), INT_DEFLATE);
        writer.close();
    }

    @Test
    public void testCreateEmptyFloatMatrix()
    {
        final File datasetFile = new File(workingDirectory, "initiallyEmptyFloatMatrix.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/emptyMatrix";
        writer.createFloatMatrix(floatDatasetName, 2, 2);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        float[][] floatMatrixRead = writer.readFloatMatrix(floatDatasetName);
        assertEquals(0, floatMatrixRead.length);

        // No write a non-empty matrix
        float[][] floatMatrixWritten = new float[][]
            {
                { 1f, 2f, 3f },
                { 4f, 5f, 6f },
                { 7f, 8f, 9f } };
        writer.writeFloatMatrix(floatDatasetName, floatMatrixWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        floatMatrixRead = reader.readFloatMatrix(floatDatasetName);
        assertTrue(equals(floatMatrixWritten, floatMatrixRead));
        reader.close();
    }

    @Test
    public void testFloatVectorLength1()
    {
        final File datasetFile = new File(workingDirectory, "singleFloatVector.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/singleFloat";
        final float[] floatDataWritten = new float[]
            { 1.0f };
        writer.writeFloatArray(floatDatasetName, floatDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        reader.hasAttribute(floatDatasetName, "flag");
        final float[] floatDataRead = reader.readFloatArray(floatDatasetName);
        assertTrue(Arrays.equals(floatDataWritten, floatDataRead));
        reader.close();
    }

    @Test
    public void testFloatMatrixLength1()
    {
        final File datasetFile = new File(workingDirectory, "singleFloatMatrix.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/singleFloat";
        final float[][] floatDataWritten = new float[][]
            {
                { 1.0f } };
        writer.writeFloatMatrix(floatDatasetName, floatDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[][] floatDataRead = reader.readFloatMatrix(floatDatasetName);
        assertTrue(equals(floatDataWritten, floatDataRead));
        reader.close();
    }

    @Test
    public void testOneRowFloatMatrix()
    {
        final File datasetFile = new File(workingDirectory, "oneRowFloatMatrix.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/singleFloat";
        final float[][] floatDataWritten = new float[][]
            {
                { 1.0f, 2.0f } };
        writer.writeFloatMatrix(floatDatasetName, floatDataWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[][] floatDataRead = reader.readFloatMatrix(floatDatasetName);
        assertTrue(equals(floatDataWritten, floatDataRead));
        reader.close();
    }

    private static boolean equals(float[][] a, float[][] a2)
    {
        if (a == a2)
        {
            return true;
        }
        if (a == null || a2 == null)
        {
            return false;
        }

        int rows = a.length;
        if (a2.length != rows)
        {
            return false;
        }

        for (int i = 0; i < rows; i++)
        {
            int columns = a[i].length;
            if (a2[i].length != columns)
            {
                return false;
            }
            for (int j = 0; j < columns; j++)
            {
                if (Float.floatToIntBits(a[i][j]) != Float.floatToIntBits(a2[i][j]))
                {
                    return false;
                }
            }
        }

        return true;
    }

    @Test
    public void testEmptyVectorDataSets()
    {
        final File datasetFile = new File(workingDirectory, "emptyVectorDatasets.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/float";
        writer.writeFloatArray(floatDatasetName, new float[0]);
        final String doubleDatasetName = "/double";
        writer.writeDoubleArray(doubleDatasetName, new double[0]);
        final String byteDatasetName = "byte";
        writer.writeByteArray(byteDatasetName, new byte[0]);
        final String shortDatasetName = "/short";
        writer.writeShortArray(shortDatasetName, new short[0]);
        final String intDatasetName = "/int";
        writer.writeIntArray(intDatasetName, new int[0]);
        final String longDatasetName = "/long";
        writer.writeLongArray(longDatasetName, new long[0]);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals(HDF5ObjectType.DATASET, reader.getObjectType(floatDatasetName));
        assertTrue(reader.readFloatArray(floatDatasetName).length == 0);
        assertTrue(reader.readDoubleArray(doubleDatasetName).length == 0);
        assertTrue(reader.readByteArray(byteDatasetName).length == 0);
        assertTrue(reader.readShortArray(shortDatasetName).length == 0);
        assertTrue(reader.readIntArray(intDatasetName).length == 0);
        assertTrue(reader.readLongArray(longDatasetName).length == 0);
        reader.close();
    }

    @Test
    public void testEmptyVectorDataSetsContiguous()
    {
        final File datasetFile = new File(workingDirectory, "emptyVectorDatasetsContiguous.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(datasetFile).dontUseExtendableDataTypes()
                        .writer();
        final String floatDatasetName = "/float";
        writer.writeFloatArray(floatDatasetName, new float[0]);
        final String doubleDatasetName = "/double";
        writer.writeDoubleArray(doubleDatasetName, new double[0]);
        final String byteDatasetName = "byte";
        writer.writeByteArray(byteDatasetName, new byte[0]);
        final String shortDatasetName = "/short";
        writer.writeShortArray(shortDatasetName, new short[0]);
        final String intDatasetName = "/int";
        writer.writeIntArray(intDatasetName, new int[0]);
        final String longDatasetName = "/long";
        writer.writeLongArray(longDatasetName, new long[0]);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals(HDF5ObjectType.DATASET, reader.getObjectType(floatDatasetName));
        assertTrue(reader.readFloatArray(floatDatasetName).length == 0);
        assertTrue(reader.readDoubleArray(doubleDatasetName).length == 0);
        assertTrue(reader.readByteArray(byteDatasetName).length == 0);
        assertTrue(reader.readShortArray(shortDatasetName).length == 0);
        assertTrue(reader.readIntArray(intDatasetName).length == 0);
        assertTrue(reader.readLongArray(longDatasetName).length == 0);
        reader.close();
    }

    @Test
    public void testEmptyVectorDataSetsCompact()
    {
        final File datasetFile = new File(workingDirectory, "emptyVectorDatasetsCompact.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/float";
        writer.writeFloatArray(floatDatasetName, new float[0],
                HDF5FloatStorageFeatures.FLOAT_COMPACT);
        final String doubleDatasetName = "/double";
        writer.writeDoubleArray(doubleDatasetName, new double[0],
                HDF5FloatStorageFeatures.FLOAT_COMPACT);
        final String byteDatasetName = "byte";
        writer.writeByteArray(byteDatasetName, new byte[0], HDF5IntStorageFeatures.INT_COMPACT);
        final String shortDatasetName = "/short";
        writer.writeShortArray(shortDatasetName, new short[0], HDF5IntStorageFeatures.INT_COMPACT);
        final String intDatasetName = "/int";
        writer.writeIntArray(intDatasetName, new int[0], HDF5IntStorageFeatures.INT_COMPACT);
        final String longDatasetName = "/long";
        writer.writeLongArray(longDatasetName, new long[0], HDF5IntStorageFeatures.INT_COMPACT);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals(HDF5ObjectType.DATASET, reader.getObjectType(floatDatasetName));
        assertTrue(reader.readFloatArray(floatDatasetName).length == 0);
        assertTrue(reader.readDoubleArray(doubleDatasetName).length == 0);
        assertTrue(reader.readByteArray(byteDatasetName).length == 0);
        assertTrue(reader.readShortArray(shortDatasetName).length == 0);
        assertTrue(reader.readIntArray(intDatasetName).length == 0);
        assertTrue(reader.readLongArray(longDatasetName).length == 0);
        reader.close();
    }

    @Test
    public void testEmptyMatrixDataSets()
    {
        final File datasetFile = new File(workingDirectory, "emptyMatrixDatasets.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String floatDatasetName = "/float";
        writer.writeFloatMatrix(floatDatasetName, new float[0][0]);
        final String doubleDatasetName = "/double";
        writer.writeDoubleMatrix(doubleDatasetName, new double[1][0]);
        final String byteDatasetName = "byte";
        writer.writeByteMatrix(byteDatasetName, new byte[2][0]);
        final String shortDatasetName = "/short";
        writer.writeShortMatrix(shortDatasetName, new short[3][0]);
        final String intDatasetName = "/int";
        writer.writeIntMatrix(intDatasetName, new int[4][0]);
        final String longDatasetName = "/long";
        writer.writeLongMatrix(longDatasetName, new long[5][0]);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertTrue(isEmpty(reader.readFloatMatrix(floatDatasetName)));
        assertTrue(isEmpty(reader.readDoubleMatrix(doubleDatasetName)));
        assertTrue(isEmpty(reader.readByteMatrix(byteDatasetName)));
        assertTrue(isEmpty(reader.readShortMatrix(shortDatasetName)));
        assertTrue(isEmpty(reader.readIntMatrix(intDatasetName)));
        assertTrue(isEmpty(reader.readLongMatrix(longDatasetName)));
        reader.close();
    }

    @Test
    public void testEmptyMatrixDataSetsContiguous()
    {
        final File datasetFile = new File(workingDirectory, "emptyMatrixDatasetsContiguous.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(datasetFile).dontUseExtendableDataTypes()
                        .writer();
        final String floatDatasetName = "/float";
        writer.writeFloatMatrix(floatDatasetName, new float[0][0]);
        final String doubleDatasetName = "/double";
        writer.writeDoubleMatrix(doubleDatasetName, new double[1][0]);
        final String byteDatasetName = "byte";
        writer.writeByteMatrix(byteDatasetName, new byte[2][0]);
        final String shortDatasetName = "/short";
        writer.writeShortMatrix(shortDatasetName, new short[3][0]);
        final String intDatasetName = "/int";
        writer.writeIntMatrix(intDatasetName, new int[4][0]);
        final String longDatasetName = "/long";
        writer.writeLongMatrix(longDatasetName, new long[5][0]);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertTrue(isEmpty(reader.readFloatMatrix(floatDatasetName)));
        assertTrue(isEmpty(reader.readDoubleMatrix(doubleDatasetName)));
        assertTrue(isEmpty(reader.readByteMatrix(byteDatasetName)));
        assertTrue(isEmpty(reader.readShortMatrix(shortDatasetName)));
        assertTrue(isEmpty(reader.readIntMatrix(intDatasetName)));
        assertTrue(isEmpty(reader.readLongMatrix(longDatasetName)));
        reader.close();
    }

    @Test
    public void testOverwriteVectorIncreaseSize()
    {
        final File datasetFile = new File(workingDirectory, "resizableVector.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "/vector";
        final float[] firstVector = new float[]
            { 1f, 2f, 3f };
        writer.writeFloatArray(dsName, firstVector);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        final float[] secondVector = new float[]
            { 1f, 2f, 3f, 4f };
        writer.writeFloatArray(dsName, secondVector);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[] vectorRead = reader.readFloatArray(dsName);
        reader.close();
        assertTrue(Arrays.equals(secondVector, vectorRead));
    }

    @Test
    public void testOverwriteWithEmptyVector()
    {
        final File datasetFile = new File(workingDirectory, "overwriteVector1.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "/vector";
        final byte[] firstVector = new byte[]
            { 1, 2, 3 };
        writer.writeByteArray(dsName, firstVector);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        final byte[] emptyVector = new byte[0];
        writer.writeByteArray(dsName, emptyVector);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] vectorRead = reader.readByteArray(dsName);
        reader.close();
        assertTrue(Arrays.equals(emptyVector, vectorRead));
    }

    @Test
    public void testOverwriteEmptyVectorWithNonEmptyVector()
    {
        final File datasetFile = new File(workingDirectory, "overwriteVector2.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "/vector";
        final byte[] emptyVector = new byte[0];
        writer.writeByteArray(dsName, emptyVector);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        final byte[] nonEmptyVector = new byte[]
            { 1 };
        writer.writeByteArray(dsName, nonEmptyVector);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final byte[] vectorRead = reader.readByteArray(dsName);
        reader.close();
        assertTrue(Arrays.equals(nonEmptyVector, vectorRead));
    }

    @Test
    public void testDeleteVector()
    {
        final File datasetFile = new File(workingDirectory, "deleteVector.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        try
        {
            final String dsName = "/vector";
            final byte[] firstVector = new byte[]
                { 1, 2, 3 };
            writer.writeByteArray(dsName, firstVector);
            writer.close();
            writer = HDF5FactoryProvider.get().open(datasetFile);
            writer.delete(dsName.substring(1));
        } finally
        {
            writer.close();
        }
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        try
        {
            final List<String> members = reader.getAllGroupMembers("/");
            assertEquals(1, members.size());
            assertEquals("__DATA_TYPES__", members.get(0));
        } finally
        {
            reader.close();
        }
    }

    @Test
    public void testDeleteGroup()
    {
        final File datasetFile = new File(workingDirectory, "deleteGroup.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        try
        {
            final String groupName = "/group";
            final String dsName = groupName + "/vector";
            final byte[] firstVector = new byte[]
                { 1, 2, 3 };
            writer.writeByteArray(dsName, firstVector);
            writer.close();
            writer = HDF5FactoryProvider.get().open(datasetFile);
            writer.delete(groupName);
        } finally
        {
            writer.close();
        }
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        try
        {
            final List<String> members = reader.getAllGroupMembers("/");
            assertEquals(1, members.size());
            assertEquals("__DATA_TYPES__", members.get(0));
            assertEquals(0, reader.getGroupMembers("/").size());
        } finally
        {
            reader.close();
        }
    }

    @Test
    public void testRenameLink()
    {
        final File file = new File(workingDirectory, "renameLink.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeBoolean("/some/boolean/value", true);
        writer.move("/some/boolean/value", "/a/new/home");
        assertFalse(writer.exists("/home/boolean/value"));
        assertTrue(writer.exists("/a/new/home"));
        writer.close();
    }

    @Test(expectedExceptions = HDF5SymbolTableException.class)
    public void testRenameLinkOverwriteFails()
    {
        final File file = new File(workingDirectory, "renameLinkOverwriteFails.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeBoolean("/some/boolean/value", true);
        writer.writeInt("/a/new/home", 4);
        writer.move("/some/boolean/value", "/a/new/home");
        writer.close();
    }

    @Test(expectedExceptions = HDF5SymbolTableException.class)
    public void testRenameLinkSrcNonExistentFails()
    {
        final File file = new File(workingDirectory, "renameLinkSrcNonExistentFails.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.move("/some/boolean/value", "/a/new/home");
        writer.close();
    }

    @Test
    public void testOverwriteWithEmptyString()
    {
        final File datasetFile = new File(workingDirectory, "overwriteString.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "/string";
        writer.writeString(dsName, "non-empty");
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeString(dsName, "");
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final String stringRead = reader.readString(dsName);
        reader.close();
        assertEquals("", stringRead);
    }

    @Test
    public void testOverwriteMatrixIncreaseSize()
    {
        final File datasetFile = new File(workingDirectory, "resizableMatrix.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "/matrix";
        final float[][] firstMatrix = new float[][]
            {
                { 1f, 2f, 3f },
                { 4f, 5f, 6f } };
        writer.writeFloatMatrix(dsName, firstMatrix);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        final float[][] secondMatrix = new float[][]
            {
                { 1f, 2f, 3f, 4f },
                { 5f, 6f, 7f, 8f },
                { 9f, 10f, 11f, 12f } };
        writer.writeFloatMatrix(dsName, secondMatrix);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final float[][] matrixRead = reader.readFloatMatrix(dsName);
        reader.close();
        assertMatrixEquals(secondMatrix, matrixRead);
    }

    @Test
    public void testOverwriteStringVectorDecreaseSize()
    {
        final File datasetFile = new File(workingDirectory, "resizableStringVector.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "/vector";
        final String[] firstVector = new String[]
            { "a", "b", "c" };
        writer.writeStringArray(dsName, firstVector);
        writer.close();
        writer = HDF5FactoryProvider.get().open(datasetFile);
        final String[] secondVector = new String[]
            { "a", "b" };
        writer.writeStringArray(dsName, secondVector);
        writer.close();
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final String[] vectorRead = reader.readStringArray(dsName);
        reader.close();
        assertTrue(Arrays.equals(secondVector, vectorRead));
    }

    private static boolean isEmpty(Object matrix)
    {
        Object maybeAnArray = matrix;
        do
        {
            if (Array.getLength(maybeAnArray) == 0)
            {
                return true;
            }
            maybeAnArray = Array.get(maybeAnArray, 0);
        } while (maybeAnArray.getClass().isArray());
        return false;
    }

    @Test
    public void testTimestamps()
    {
        final File datasetFile = new File(workingDirectory, "timestamps.h5");
        final String timeStampDS = "prehistoric";
        final long timestampValue = 10000L;
        final String noTimestampDS = "notatimestamp";
        final long someLong = 173756123L;
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeTimeStamp(timeStampDS, timestampValue);
        writer.writeLong(noTimestampDS, someLong);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                reader.tryGetTypeVariant(timeStampDS));
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeStampDS);
        assertTrue(info.isScalar());
        assertEquals(HDF5StorageLayout.COMPACT, info.getStorageLayout());
        assertNull(info.tryGetChunkSizes());
        assertEquals(HDF5DataClass.INTEGER, info.getTypeInformation().getDataClass());
        assertTrue(info.isTimeStamp());
        assertFalse(info.isTimeDuration());
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                info.tryGetTypeVariant());
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH, info
                .getTypeInformation().tryGetTypeVariant());
        assertEquals(timestampValue, reader.readTimeStamp(timeStampDS));
        assertEquals(timestampValue, reader.readDate(timeStampDS).getTime());
        try
        {
            reader.readTimeStamp(noTimestampDS);
            fail("Failed to detect non-timestamp value.");
        } catch (HDF5JavaException ex)
        {
            if (ex.getMessage().contains("not a time stamp") == false)
            {
                throw ex;
            }
            // That is what we expect.
        }
        reader.close();
    }

    @Test
    public void testTimestampArray()
    {
        final File datasetFile = new File(workingDirectory, "timestampArray.h5");
        final String timeSeriesDS = "/some/timeseries";
        final long[] timeSeries = new long[10];
        for (int i = 0; i < timeSeries.length; ++i)
        {
            timeSeries[i] = i * 10000L;
        }
        final long[] notATimeSeries = new long[100];
        final String noTimeseriesDS = "nota/timeseries";
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeTimeStampArray(timeSeriesDS, timeSeries);
        writer.writeLongArray(noTimeseriesDS, notATimeSeries);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeSeriesDS);
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                info.tryGetTypeVariant());
        assertChunkSizes(info, 10);
        assertTrue(Arrays.equals(timeSeries, reader.readTimeStampArray(timeSeriesDS)));
        final Date[] datesRead = reader.readDateArray(timeSeriesDS);
        final long[] timeStampsRead = new long[datesRead.length];
        for (int i = 0; i < timeStampsRead.length; ++i)
        {
            timeStampsRead[i] = datesRead[i].getTime();
        }
        assertTrue(Arrays.equals(timeSeries, timeStampsRead));
        try
        {
            reader.readTimeStampArray(noTimeseriesDS);
            fail("Failed to detect non-timestamp array.");
        } catch (HDF5JavaException ex)
        {
            if (ex.getMessage().contains("not a time stamp") == false)
            {
                throw ex;
            }
            // That is what we expect.
        }
        reader.close();
    }

    @Test
    public void testTimestampArrayChunked()
    {
        final File datasetFile = new File(workingDirectory, "timestampArrayChunked.h5");
        final String timeSeriesDS = "/some/timeseries";
        final long[] timeSeries = new long[10];
        for (int i = 0; i < timeSeries.length; ++i)
        {
            timeSeries[i] = i * 10000L;
        }
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.createTimeStampArray(timeSeriesDS, 0, 10, GENERIC_DEFLATE);
        for (int i = 0; i < 10; ++i)
        {
            writer.writeTimeStampArrayBlock(timeSeriesDS, timeSeries, i);
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeSeriesDS);
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                info.tryGetTypeVariant());
        assertChunkSizes(info, 10);
        for (int i = 0; i < 10; ++i)
        {
            assertTrue(Arrays.equals(timeSeries,
                    reader.readTimeStampArrayBlock(timeSeriesDS, 10, i)));
        }
        reader.close();
    }

    @Test
    public void testTimeDurations()
    {
        final File datasetFile = new File(workingDirectory, "timedurations.h5");
        final String timeDurationDS = "someDuration";
        final String timeDurationDS2 = "someOtherDuration";
        final long timeDurationInSeconds = 10000L;
        final long timeDurationInMilliSeconds = 10000L * 1000L;
        final long timeDurationInHoursRounded = 3L;
        final String noTimestampDS = "notatimeduration";
        final long someLong = 173756123L;
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeTimeDuration(timeDurationDS, timeDurationInSeconds, HDF5TimeUnit.SECONDS);
        final HDF5TimeDuration timeDurationWithUnit =
                new HDF5TimeDuration(timeDurationInHoursRounded, HDF5TimeUnit.HOURS);
        writer.writeTimeDuration(timeDurationDS2, timeDurationWithUnit);
        writer.writeLong(noTimestampDS, someLong);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeDurationDS);
        assertTrue(info.isScalar());
        assertEquals(HDF5StorageLayout.COMPACT, info.getStorageLayout());
        assertNull(info.tryGetChunkSizes());
        assertEquals(HDF5DataClass.INTEGER, info.getTypeInformation().getDataClass());
        assertTrue(info.isTimeDuration());
        assertFalse(info.isTimeStamp());
        assertEquals(HDF5TimeUnit.SECONDS, reader.tryGetTimeUnit(timeDurationDS));
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_SECONDS, info.tryGetTypeVariant());
        assertEquals(HDF5TimeUnit.SECONDS, info.tryGetTimeUnit());
        assertEquals(timeDurationInSeconds,
                HDF5TimeUnit.SECONDS.convert(reader.readTimeDuration(timeDurationDS)));
        assertEquals(timeDurationInMilliSeconds,
                HDF5TimeUnit.MILLISECONDS.convert(reader.readTimeDuration(timeDurationDS)));
        assertEquals(timeDurationInHoursRounded,
                HDF5TimeUnit.HOURS.convert(reader.readTimeDuration(timeDurationDS)));
        assertEquals(new HDF5TimeDuration(timeDurationInSeconds, HDF5TimeUnit.SECONDS),
                reader.readTimeDuration(timeDurationDS));
        assertEquals(timeDurationWithUnit, reader.readTimeDuration(timeDurationDS2));
        try
        {
            reader.readTimeDuration(noTimestampDS);
            fail("Failed to detect non-timeduration value.");
        } catch (HDF5JavaException ex)
        {
            if (ex.getMessage().contains("not a time duration") == false)
            {
                throw ex;
            }
            // That is what we expect.
        }
        reader.close();
    }

    @Test
    public void testSmallTimeDurations()
    {
        final File datasetFile = new File(workingDirectory, "smalltimedurations.h5");
        final String timeDurationDS = "someDuration";
        final short timeDurationInSeconds = 10000;
        final long timeDurationInMilliSeconds = 10000L * 1000L;
        final long timeDurationInHoursRounded = 3L;
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeShort(timeDurationDS, timeDurationInSeconds);
        writer.setTypeVariant(timeDurationDS, HDF5TimeUnit.SECONDS.getTypeVariant());
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeDurationDS);
        assertTrue(info.isScalar());
        assertEquals(HDF5StorageLayout.COMPACT, info.getStorageLayout());
        assertNull(info.tryGetChunkSizes());
        assertEquals(HDF5DataClass.INTEGER, info.getTypeInformation().getDataClass());
        assertEquals(NativeData.SHORT_SIZE, info.getTypeInformation().getElementSize());
        assertTrue(info.isTimeDuration());
        assertFalse(info.isTimeStamp());
        assertEquals(HDF5TimeUnit.SECONDS, reader.tryGetTimeUnit(timeDurationDS));
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_SECONDS, info.tryGetTypeVariant());
        assertEquals(HDF5TimeUnit.SECONDS, info.tryGetTimeUnit());
        assertEquals(timeDurationInSeconds,
                HDF5TimeUnit.SECONDS.convert(reader.readTimeDuration(timeDurationDS)));
        assertEquals(timeDurationInMilliSeconds,
                HDF5TimeUnit.MILLISECONDS.convert(reader.readTimeDuration(timeDurationDS)));
        assertEquals(timeDurationInHoursRounded,
                HDF5TimeUnit.HOURS.convert(reader.readTimeDuration(timeDurationDS)));
        reader.close();
    }

    @Test
    public void testTimeDurationArray()
    {
        final File datasetFile = new File(workingDirectory, "timedurationarray.h5");
        final String timeDurationDS = "someDuration";
        final HDF5TimeDuration[] durationsWritten =
                new HDF5TimeDuration[]
                    { new HDF5TimeDuration(2, HDF5TimeUnit.SECONDS),
                            new HDF5TimeDuration(5, HDF5TimeUnit.HOURS),
                            new HDF5TimeDuration(1, HDF5TimeUnit.DAYS) };
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.writeTimeDurationArray(timeDurationDS,
                HDF5TimeDurationArray.create(durationsWritten));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeDurationDS);
        assertTrue(info.isTimeDuration());
        assertFalse(info.isTimeStamp());
        assertEquals(HDF5TimeUnit.SECONDS, info.tryGetTimeUnit());
        final HDF5TimeDurationArray durationsRead = reader.readTimeDurationArray(timeDurationDS);
        assertEquals(durationsWritten.length, durationsRead.getLength());
        for (int i = 0; i < durationsWritten.length; ++i)
        {
            assertTrue(durationsRead.get(i).isEquivalent(durationsWritten[i]));
        }
    }

    @Test
    public void testTimeDurationArrayChunked()
    {
        final File datasetFile = new File(workingDirectory, "timeDurationArrayChunked.h5");
        final String timeDurationSeriesDS = "/some/timeseries";
        final String timeDurationSeriesDS2 = "/some/timeseries2";
        final long[] timeDurationSeriesMillis = new long[10];
        final long[] timeDurationSeriesMicros = new long[10];
        for (int i = 0; i < timeDurationSeriesMillis.length; ++i)
        {
            timeDurationSeriesMillis[i] = i * 10000L;
            timeDurationSeriesMicros[i] = timeDurationSeriesMillis[i] * 1000L;
        }
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        writer.createTimeDurationArray(timeDurationSeriesDS, 100, 10, HDF5TimeUnit.MILLISECONDS,
                GENERIC_DEFLATE);
        for (int i = 0; i < 10; ++i)
        {
            writer.writeTimeDurationArrayBlock(timeDurationSeriesDS, new HDF5TimeDurationArray(
                    timeDurationSeriesMicros, HDF5TimeUnit.MICROSECONDS), i);
        }
        writer.createTimeDurationArray(timeDurationSeriesDS2, 100, 10, HDF5TimeUnit.SECONDS,
                GENERIC_DEFLATE);
        final HDF5TimeDuration[] timeDurationSeries =
                new HDF5TimeDuration[]
                    {
                            new HDF5TimeDuration(timeDurationSeriesMicros[0],
                                    HDF5TimeUnit.MICROSECONDS),
                            new HDF5TimeDuration(timeDurationSeriesMicros[1],
                                    HDF5TimeUnit.MICROSECONDS),
                            new HDF5TimeDuration(timeDurationSeriesMillis[2],
                                    HDF5TimeUnit.MILLISECONDS),
                            new HDF5TimeDuration(timeDurationSeriesMillis[3],
                                    HDF5TimeUnit.MILLISECONDS),
                            new HDF5TimeDuration(timeDurationSeriesMillis[4] / 1000L,
                                    HDF5TimeUnit.SECONDS),
                            new HDF5TimeDuration(timeDurationSeriesMillis[5] / 1000L,
                                    HDF5TimeUnit.SECONDS),
                            new HDF5TimeDuration(6, HDF5TimeUnit.HOURS),
                            new HDF5TimeDuration(7, HDF5TimeUnit.HOURS),
                            new HDF5TimeDuration(8, HDF5TimeUnit.DAYS),
                            new HDF5TimeDuration(9, HDF5TimeUnit.DAYS) };
        for (int i = 0; i < 10; ++i)
        {
            writer.writeTimeDurationArrayBlock(timeDurationSeriesDS2,
                    HDF5TimeDurationArray.create(timeDurationSeries), i);
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        final HDF5DataSetInformation info = reader.getDataSetInformation(timeDurationSeriesDS);
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_MILLISECONDS, info.tryGetTypeVariant());
        assertChunkSizes(info, 10);
        for (int i = 0; i < 10; ++i)
        {
            assertTrue(Arrays.equals(timeDurationSeriesMicros, HDF5TimeUnit.MICROSECONDS
                    .convert(reader.readTimeDurationArrayBlock(timeDurationSeriesDS, 10, i))));
        }
        final HDF5DataSetInformation info2 = reader.getDataSetInformation(timeDurationSeriesDS2);
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_SECONDS, info2.tryGetTypeVariant());
        assertChunkSizes(info2, 10);
        for (int i = 0; i < 10; ++i)
        {
            final long[] block =
                    HDF5TimeUnit.MICROSECONDS.convert(reader.readTimeDurationArrayBlock(
                            timeDurationSeriesDS2, 10, i));
            for (int j = 0; j < block.length; ++j)
            {
                assertEquals(HDF5TimeUnit.MICROSECONDS.convert(timeDurationSeries[j]), block[j]);
            }
        }
        for (int i = 0; i < 10; ++i)
        {
            final HDF5TimeDurationArray block =
                    reader.readTimeDurationArrayBlock(timeDurationSeriesDS2, 10, i);
            for (int j = 0; j < block.getLength(); ++j)
            {
                assertTrue(block.get(j).isEquivalent(timeDurationSeries[j]));
            }
        }
        for (HDF5DataBlock<HDF5TimeDurationArray> block : reader
                .getTimeDurationArrayNaturalBlocks(timeDurationSeriesDS2))
        {
            final HDF5TimeDurationArray data = block.getData();
            for (int j = 0; j < data.getLength(); ++j)
            {
                assertTrue(data.get(j) + "<>" + timeDurationSeries[j],
                        data.get(j).isEquivalent(timeDurationSeries[j]));
            }
        }
        reader.close();
    }

    @Test
    public void testAttributes()
    {
        final File attributeFile = new File(workingDirectory, "attributes.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(attributeFile);
        final String datasetName = "SomeDataSet";
        writer.writeIntArray(datasetName, new int[0]);
        final String booleanAttributeName = "Boolean Attribute";
        final boolean booleanAttributeValueWritten = true;
        writer.setBooleanAttribute(datasetName, booleanAttributeName, booleanAttributeValueWritten);
        assertTrue(writer.hasAttribute(datasetName, booleanAttributeName));
        final String integerAttributeName = "Integer Attribute";
        final int integerAttributeValueWritten = 17;
        writer.setIntAttribute(datasetName, integerAttributeName, integerAttributeValueWritten);
        final String byteAttributeName = "Byte Attribute";
        final byte byteAttributeValueWritten = 17;
        writer.setByteAttribute(datasetName, byteAttributeName, byteAttributeValueWritten);
        final String stringAttributeName = "String Attribute";
        final String stringAttributeValueWritten = "Some String Value";
        writer.setStringAttribute(datasetName, stringAttributeName, stringAttributeValueWritten);
        final String stringAttributeNameVL = "String Attribute VL";
        final String stringAttributeValueVLWritten1 = "Some String Value";
        writer.setStringAttributeVariableLength(datasetName, stringAttributeNameVL,
                stringAttributeValueVLWritten1);
        final String stringAttributeValueVLWritten2 = "Some Other String Value";
        writer.setStringAttributeVariableLength(datasetName, stringAttributeNameVL,
                stringAttributeValueVLWritten2);
        final String integerArrayAttributeName = "Integer Array Attribute";
        final int[] integerArrayAttributeValueWritten = new int[]
            { 17, 23, 42 };
        writer.setIntArrayAttribute(datasetName, integerArrayAttributeName,
                integerArrayAttributeValueWritten);
        final String stringArrayAttributeName = "String Array Attribute";
        final String[] stringArrayAttributeValueWritten = new String[]
            { "Some String Value I", "Some String Value II", "Some String Value III" };
        writer.setStringArrayAttribute(datasetName, stringArrayAttributeName,
                stringArrayAttributeValueWritten);
        final String string2DArrayAttributeName = "String 2D Array Attribute";
        final MDArray<String> string2DArrayAttributeValueWritten =
                new MDArray<String>(
                        new String[]
                            { "Some String Value I", "Some String Value II",
                                    "Some String Value III", "IV" }, new int[]
                            { 2, 2 });
        writer.setStringMDArrayAttribute(datasetName, string2DArrayAttributeName,
                string2DArrayAttributeValueWritten);
        final HDF5EnumerationType enumType = writer.getEnumType("MyEnum", new String[]
            { "ONE", "TWO", "THREE" }, false);
        final String enumAttributeName = "Enum Attribute";
        final HDF5EnumerationValue enumAttributeValueWritten =
                new HDF5EnumerationValue(enumType, "TWO");
        writer.setEnumAttribute(datasetName, enumAttributeName, enumAttributeValueWritten);
        final String enumArrayAttributeName = "Enum Array Attribute";
        final HDF5EnumerationValueArray enumArrayAttributeValueWritten =
                new HDF5EnumerationValueArray(enumType, new String[]
                    { "TWO", "THREE", "ONE" });
        writer.setEnumArrayAttribute(datasetName, enumArrayAttributeName,
                enumArrayAttributeValueWritten);
        final String volatileAttributeName = "Some Volatile Attribute";
        writer.setIntAttribute(datasetName, volatileAttributeName, 21);
        writer.deleteAttribute(datasetName, volatileAttributeName);
        final String floatArrayAttributeName = "Float Array Attribute";
        final float[] floatArrayAttribute = new float[]
            { 3f, 3.1f, 3.14f, 3.142f, 3.1416f };
        writer.setFloatArrayAttribute(datasetName, floatArrayAttributeName, floatArrayAttribute);
        final String floatArrayMDAttributeName = "Float Array Multi-dimensional Attribute";
        final MDFloatArray floatMatrixAttribute = new MDFloatArray(new float[][]
            {
                { 1, 2, 3 },
                { 4, 5, 6 } });
        writer.setFloatMDArrayAttribute(datasetName, floatArrayMDAttributeName,
                floatMatrixAttribute);
        final MDFloatArray floatMatrixAttribute2 = new MDFloatArray(new float[][]
            {
                { 2, 3, 4 },
                { 7, 8, 9 } });
        writer.setFloatMatrixAttribute(datasetName, floatArrayMDAttributeName,
                floatMatrixAttribute2.toMatrix());
        final String byteArrayAttributeName = "Byte Array Attribute";
        final byte[] byteArrayAttribute = new byte[]
            { 1, 2, 3 };
        writer.setByteArrayAttribute(datasetName, byteArrayAttributeName, byteArrayAttribute);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(attributeFile);
        assertTrue(reader.hasAttribute(datasetName, booleanAttributeName));
        final boolean booleanAttributeValueRead =
                reader.getBooleanAttribute(datasetName, booleanAttributeName);
        assertEquals(booleanAttributeValueWritten, booleanAttributeValueRead);
        final int integerAttributeValueRead =
                reader.getIntAttribute(datasetName, integerAttributeName);
        assertEquals(integerAttributeValueWritten, integerAttributeValueRead);
        final byte byteAttributeValueRead = reader.getByteAttribute(datasetName, byteAttributeName);
        assertEquals(byteAttributeValueWritten, byteAttributeValueRead);
        HDF5DataTypeInformation info =
                reader.getAttributeInformation(datasetName, integerAttributeName);
        assertEquals(HDF5DataClass.INTEGER, info.getDataClass());
        assertEquals(4, info.getElementSize());
        final String stringAttributeValueRead =
                reader.getStringAttribute(datasetName, stringAttributeName);
        assertEquals(stringAttributeValueWritten, stringAttributeValueRead);
        final int[] intArrayAttributeValueRead =
                reader.getIntArrayAttribute(datasetName, integerArrayAttributeName);
        assertTrue(Arrays.equals(integerArrayAttributeValueWritten, intArrayAttributeValueRead));
        final String[] stringArrayAttributeValueRead =
                reader.getStringArrayAttribute(datasetName, stringArrayAttributeName);
        assertTrue(Arrays.equals(stringArrayAttributeValueWritten, stringArrayAttributeValueRead));
        info = reader.getAttributeInformation(datasetName, stringArrayAttributeName);
        assertTrue(info.isArrayType());
        assertEquals(HDF5DataClass.STRING, info.getDataClass());
        assertEquals(22, info.getElementSize()); // maxlength
        assertEquals(3, info.getNumberOfElements());
        assertEquals(1, info.getDimensions().length);
        final MDArray<String> string2DArrayAttributeValueRead =
                reader.getStringMDArrayAttribute(datasetName, string2DArrayAttributeName);
        assertEquals(string2DArrayAttributeValueWritten, string2DArrayAttributeValueRead);
        final String stringAttributeValueVLRead =
                reader.getStringAttribute(datasetName, stringAttributeNameVL);
        assertEquals(stringAttributeValueVLWritten2, stringAttributeValueVLRead);
        final HDF5EnumerationValue enumAttributeValueRead =
                reader.getEnumAttribute(datasetName, enumAttributeName);
        final String enumAttributeStringValueRead =
                reader.getEnumAttributeAsString(datasetName, enumAttributeName);
        assertEquals(enumAttributeValueWritten.getValue(), enumAttributeValueRead.getValue());
        assertEquals(enumAttributeValueWritten.getValue(), enumAttributeStringValueRead);
        final String[] enumArrayAttributeReadAsString =
                reader.getEnumArrayAttributeAsString(datasetName, enumArrayAttributeName);
        assertEquals(enumArrayAttributeValueWritten.getLength(),
                enumArrayAttributeReadAsString.length);
        for (int i = 0; i < enumArrayAttributeReadAsString.length; ++i)
        {
            assertEquals(enumArrayAttributeValueWritten.getValue(i),
                    enumArrayAttributeReadAsString[i]);
        }
        final HDF5EnumerationValueArray enumArrayAttributeRead =
                reader.getEnumArrayAttribute(datasetName, enumArrayAttributeName);
        assertEquals(enumArrayAttributeValueWritten.getLength(), enumArrayAttributeRead.getLength());
        for (int i = 0; i < enumArrayAttributeRead.getLength(); ++i)
        {
            assertEquals(enumArrayAttributeValueWritten.getValue(i),
                    enumArrayAttributeRead.getValue(i));
        }
        // Let's try to read the first element of the array using getEnumAttributeAsString
        assertEquals(enumArrayAttributeValueWritten.getValue(0),
                reader.getEnumAttributeAsString(datasetName, enumArrayAttributeName));
        // Let's try to read the first element of the array using getEnumAttribute
        assertEquals(enumArrayAttributeValueWritten.getValue(0),
                reader.getEnumAttribute(datasetName, enumArrayAttributeName).getValue());
        assertFalse(reader.hasAttribute(datasetName, volatileAttributeName));
        assertTrue(Arrays.equals(floatArrayAttribute,
                reader.getFloatArrayAttribute(datasetName, floatArrayAttributeName)));
        assertTrue(floatMatrixAttribute2.equals(reader.getFloatMDArrayAttribute(datasetName,
                floatArrayMDAttributeName)));
        assertTrue(floatMatrixAttribute2.equals(new MDFloatArray(reader.getFloatMatrixAttribute(
                datasetName, floatArrayMDAttributeName))));
        assertTrue(Arrays.equals(byteArrayAttribute,
                reader.getByteArrayAttribute(datasetName, byteArrayAttributeName)));
        reader.close();
    }

    @Test
    public void testTimeStampAttributes()
    {
        final File attributeFile = new File(workingDirectory, "timeStampAttributes.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(attributeFile);
        final String datasetName = "SomeDataSet";
        final String lastChangedAttr = "lastChanged";
        final String someLongAttr = "someLong";
        final Date now = new Date();
        writer.writeIntArray(datasetName, new int[0]);
        writer.setLongAttribute(datasetName, someLongAttr, 115L);
        writer.setDateAttribute(datasetName, lastChangedAttr, now);
        writer.close();
        final IHDF5Reader reader = HDF5Factory.openForReading(attributeFile);
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                reader.tryGetTypeVariant(datasetName, lastChangedAttr));
        assertFalse(reader.isTimeStamp(datasetName));
        assertTrue(reader.isTimeStamp(datasetName, lastChangedAttr));
        assertFalse(reader.isTimeDuration(datasetName, lastChangedAttr));
        assertEquals(now, reader.getDateAttribute(datasetName, lastChangedAttr));
        assertFalse(reader.isTimeStamp(datasetName, someLongAttr));
        try
        {
            reader.getTimeStampAttribute(datasetName, someLongAttr);
            fail("Did not detect non-time-stamp attribute.");
        } catch (HDF5JavaException ex)
        {
            assertEquals("Attribute 'someLong' of data set 'SomeDataSet' is not a time stamp.",
                    ex.getMessage());
        }
        reader.close();
    }

    @Test
    public void testTimeDurationAttributes()
    {
        final File attributeFile = new File(workingDirectory, "timeDurationAttributes.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(attributeFile);
        final String datasetName = "SomeDataSet";
        final String validUntilAttr = "validUtil";
        final String someLongAttr = "someLong";
        writer.writeIntArray(datasetName, new int[0]);
        writer.setTimeDurationAttribute(datasetName, validUntilAttr, 10, HDF5TimeUnit.MINUTES);
        writer.setLongAttribute(datasetName, someLongAttr, 115L);
        writer.close();
        final IHDF5Reader reader = HDF5Factory.openForReading(attributeFile);
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_MINUTES,
                reader.tryGetTypeVariant(datasetName, validUntilAttr));
        assertFalse(reader.isTimeStamp(datasetName));
        assertFalse(reader.isTimeStamp(datasetName, validUntilAttr));
        assertTrue(reader.isTimeDuration(datasetName, validUntilAttr));
        assertEquals(HDF5TimeUnit.MINUTES, reader.tryGetTimeUnit(datasetName, validUntilAttr));
        assertEquals(new HDF5TimeDuration(10, HDF5TimeUnit.MINUTES),
                reader.getTimeDurationAttribute(datasetName, validUntilAttr));
        assertEquals(10 * 60, reader.getTimeDurationAttribute(datasetName, validUntilAttr)
                .getValue(HDF5TimeUnit.SECONDS));
        assertFalse(reader.isTimeDuration(datasetName, someLongAttr));
        try
        {
            reader.getTimeDurationAttribute(datasetName, someLongAttr);
            fail("Did not detect non-time-duration attribute.");
        } catch (HDF5JavaException ex)
        {
            assertEquals("Attribute 'someLong' of data set 'SomeDataSet' is not a time duration.",
                    ex.getMessage());
        }
        reader.close();
    }

    @Test
    public void testTimeStampArrayAttributes()
    {
        final File attributeFile = new File(workingDirectory, "timeStampArrayAttributes.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(attributeFile);
        final String datasetName = "SomeDataSet";
        final String lastChangedAttr = "lastChanged";
        final String someLongAttr = "someLong";
        final Date now = new Date();
        writer.writeIntArray(datasetName, new int[0]);
        writer.setLongArrayAttribute(datasetName, someLongAttr, new long[]
            { 115L });
        writer.setDateArrayAttribute(datasetName, lastChangedAttr, new Date[]
            { now });
        writer.close();
        final IHDF5Reader reader = HDF5Factory.openForReading(attributeFile);
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                reader.tryGetTypeVariant(datasetName, lastChangedAttr));
        assertFalse(reader.isTimeStamp(datasetName));
        assertTrue(reader.isTimeStamp(datasetName, lastChangedAttr));
        assertFalse(reader.isTimeDuration(datasetName, lastChangedAttr));
        assertEquals(1, reader.getDateArrayAttribute(datasetName, lastChangedAttr).length);
        assertEquals(now, reader.getDateArrayAttribute(datasetName, lastChangedAttr)[0]);
        assertFalse(reader.isTimeStamp(datasetName, someLongAttr));
        try
        {
            reader.getTimeStampArrayAttribute(datasetName, someLongAttr);
            fail("Did not detect non-time-stamp attribute.");
        } catch (HDF5JavaException ex)
        {
            assertEquals("Attribute 'someLong' of data set 'SomeDataSet' is not a time stamp.",
                    ex.getMessage());
        }
        reader.close();
    }

    @Test
    public void testTimeDurationArrayAttributes()
    {
        final File attributeFile = new File(workingDirectory, "timeDurationArrayAttributes.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(attributeFile);
        final String datasetName = "SomeDataSet";
        final String validUntilAttr = "validUtil";
        final String someLongAttr = "someLong";
        writer.writeIntArray(datasetName, new int[0]);
        writer.setTimeDurationArrayAttribute(datasetName, validUntilAttr,
                HDF5TimeDurationArray.create(HDF5TimeUnit.MINUTES, 10));
        writer.setLongArrayAttribute(datasetName, someLongAttr, new long[]
            { 115L });
        writer.close();
        final IHDF5Reader reader = HDF5Factory.openForReading(attributeFile);
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_MINUTES,
                reader.tryGetTypeVariant(datasetName, validUntilAttr));
        assertFalse(reader.isTimeStamp(datasetName));
        assertFalse(reader.isTimeStamp(datasetName, validUntilAttr));
        assertTrue(reader.isTimeDuration(datasetName, validUntilAttr));
        assertEquals(HDF5TimeUnit.MINUTES, reader.tryGetTimeUnit(datasetName, validUntilAttr));
        assertEquals(1, reader.getTimeDurationArrayAttribute(datasetName, validUntilAttr)
                .getLength());
        assertEquals(new HDF5TimeDuration(10, HDF5TimeUnit.MINUTES), reader
                .getTimeDurationArrayAttribute(datasetName, validUntilAttr).get(0));
        assertEquals(10 * 60, reader.getTimeDurationArrayAttribute(datasetName, validUntilAttr)
                .getValue(0, HDF5TimeUnit.SECONDS));
        assertFalse(reader.isTimeDuration(datasetName, someLongAttr));
        try
        {
            reader.getTimeDurationArrayAttribute(datasetName, someLongAttr);
            fail("Did not detect non-time-duration attribute.");
        } catch (HDF5JavaException ex)
        {
            assertEquals("Attribute 'someLong' of data set 'SomeDataSet' is not a time duration.",
                    ex.getMessage());
        }
        reader.close();
    }

    @Test
    public void testAttributeDimensionArray()
    {
        final File attributeFile = new File(workingDirectory, "attributeDimensionalArray.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(attributeFile);
        final HDF5ArrayTypeFloatWriter efWriter = new HDF5ArrayTypeFloatWriter((HDF5Writer) writer);
        final String datasetName = "SomeDataSet";
        final String attributeName = "farray";
        final float[] farray = new float[]
            { 0, 10, 100 };

        writer.writeIntArray(datasetName, new int[0]);
        efWriter.setFloatArrayAttributeDimensional(datasetName, attributeName, farray);
        final HDF5DataTypeInformation info =
                writer.getAttributeInformation(datasetName, attributeName);
        assertEquals("FLOAT(4, #3)", info.toString());
        assertFalse(info.isArrayType());
        writer.close();

        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(attributeFile);
        assertTrue(Arrays.equals(farray, reader.getFloatArrayAttribute(datasetName, attributeName)));
    }

    @Test
    public void testAttributeDimensionArrayOverwrite()
    {
        final File attributeFile =
                new File(workingDirectory, "attributeDimensionalArrayOverwrite.h5");
        attributeFile.delete();
        assertFalse(attributeFile.exists());
        attributeFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(attributeFile);
        final HDF5ArrayTypeFloatWriter efWriter = new HDF5ArrayTypeFloatWriter((HDF5Writer) writer);
        final String datasetName = "SomeDataSet";
        final String attributeName = "farray";
        final float[] farray = new float[]
            { 0, 10, 100 };

        writer.writeIntArray(datasetName, new int[0]);
        efWriter.setFloatArrayAttributeDimensional(datasetName, attributeName, farray);
        writer.setFloatArrayAttribute(datasetName, attributeName, farray);
        final HDF5DataTypeInformation info =
                writer.getAttributeInformation(datasetName, attributeName);
        assertEquals("FLOAT(4, #3)", info.toString());
        assertTrue(info.isArrayType());
        writer.close();

        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(attributeFile);
        assertTrue(Arrays.equals(farray, reader.getFloatArrayAttribute(datasetName, attributeName)));
    }

    @Test
    public void testCreateDataTypes()
    {
        final File file = new File(workingDirectory, "types.h5");
        final String enumName = "TestEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        try
        {
            final List<String> initialDataTypes = writer.getGroupMembers(HDF5Utils.DATATYPE_GROUP);

            writer.getEnumType(enumName, new String[]
                { "ONE", "TWO", "THREE" }, false);
            final Set<String> dataTypes =
                    new HashSet<String>(writer.getGroupMembers(HDF5Utils.DATATYPE_GROUP));
            assertEquals(initialDataTypes.size() + 1, dataTypes.size());
            assertTrue(dataTypes.contains(HDF5Utils.ENUM_PREFIX + enumName));
        } finally
        {
            writer.close();
        }
    }

    @Test
    public void testGroups()
    {
        final File groupFile = new File(workingDirectory, "groups.h5");
        groupFile.delete();
        assertFalse(groupFile.exists());
        groupFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(groupFile);
        final String groupName1 = "/group";
        final String groupName2 = "/group2";
        final String groupName4 = "/dataSetGroup";
        final String groupName5 = "/group5";
        final String dataSetName = groupName4 + "/dataset";
        writer.createGroup(groupName1);
        writer.createGroup(groupName2);
        writer.writeByteArray(dataSetName, new byte[]
            { 1 });
        assertTrue(writer.isGroup(groupName1));
        assertTrue(writer.isGroup(groupName2));
        assertTrue(writer.isGroup(groupName4));
        assertFalse(writer.isGroup(dataSetName));
        assertFalse(writer.isGroup(groupName5));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(groupFile);
        assertTrue(reader.isGroup(groupName1));
        assertEquals(HDF5ObjectType.GROUP, reader.getObjectType(groupName1));
        assertTrue(reader.isGroup(groupName4));
        assertEquals(HDF5ObjectType.GROUP, reader.getObjectType(groupName4));
        assertFalse(reader.isGroup(dataSetName));
        reader.close();
    }

    @Test
    public void testGetObjectType()
    {
        final File file = new File(workingDirectory, "typeInfo.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeBoolean("/some/flag", false);
        writer.createSoftLink("/some", "/linkToSome");
        writer.createSoftLink("/some/flag", "/linkToFlag");
        writer.createHardLink("/some/flag", "/some/flag2");
        writer.setBooleanAttribute("/some/flag2", "test", true);
        assertEquals(HDF5ObjectType.GROUP, writer.getObjectType("/some"));
        assertEquals(HDF5ObjectType.SOFT_LINK, writer.getObjectType("/linkToSome", false));
        assertEquals(HDF5ObjectType.GROUP, writer.getObjectType("/some"));
        assertEquals(HDF5ObjectType.GROUP, writer.getObjectType("/linkToSome"));
        assertEquals(HDF5ObjectType.DATASET, writer.getObjectType("/some/flag", false));
        assertEquals(HDF5ObjectType.DATASET, writer.getObjectType("/some/flag"));
        assertEquals(HDF5ObjectType.SOFT_LINK, writer.getObjectType("/linkToFlag", false));
        assertEquals(HDF5ObjectType.DATASET, writer.getObjectType("/linkToFlag"));
        assertFalse(writer.exists("non_existent"));
        assertEquals(HDF5ObjectType.NONEXISTENT, writer.getObjectType("non_existent"));
        writer.close();
    }

    @Test(expectedExceptions = HDF5JavaException.class)
    public void testGetLinkInformationFailed()
    {
        final File file = new File(workingDirectory, "linkInfo.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        try
        {
            assertFalse(writer.exists("non_existent"));
            writer.getLinkInformation("non_existent").checkExists();
        } finally
        {
            writer.close();
        }
    }

    @Test
    public void testGetDataSetInformation()
    {
        final File file = new File(workingDirectory, "dsInfo.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeInt("dsScalar", 12);
        writer.writeShortMatrix("ds", new short[][]
            {
                { (short) 1, (short) 2, (short) 3 },
                { (short) 4, (short) 5, (short) 6 } });
        final String s = "this is a string";
        writer.writeString("stringDS", s);
        writer.writeStringVariableLength("stringDSVL", s);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5DataSetInformation scalarInfo = reader.getDataSetInformation("dsScalar");
        assertEquals(HDF5DataClass.INTEGER, scalarInfo.getTypeInformation().getDataClass());
        assertEquals(4, scalarInfo.getTypeInformation().getElementSize());
        assertEquals(0, scalarInfo.getRank());
        assertTrue(scalarInfo.isScalar());
        assertEquals(0, scalarInfo.getDimensions().length);
        assertNull(scalarInfo.tryGetChunkSizes());
        final HDF5DataSetInformation info = reader.getDataSetInformation("ds");
        assertEquals(HDF5DataClass.INTEGER, info.getTypeInformation().getDataClass());
        assertEquals(2, info.getTypeInformation().getElementSize());
        assertEquals(2, info.getRank());
        assertFalse(info.isScalar());
        assertEquals(2, info.getDimensions()[0]);
        assertEquals(3, info.getDimensions()[1]);
        assertChunkSizes(info, HDF5Utils.MIN_CHUNK_SIZE, HDF5Utils.MIN_CHUNK_SIZE);
        final HDF5DataSetInformation stringInfo = reader.getDataSetInformation("stringDS");
        assertEquals(HDF5DataClass.STRING, stringInfo.getTypeInformation().getDataClass());
        assertEquals(s.length() + 1, stringInfo.getTypeInformation().getElementSize());
        assertEquals(0, stringInfo.getDimensions().length);
        assertEquals(0, stringInfo.getMaxDimensions().length);
        assertEquals(HDF5StorageLayout.COMPACT, stringInfo.getStorageLayout());
        assertNull(stringInfo.tryGetChunkSizes());
        final HDF5DataSetInformation stringInfoVL = reader.getDataSetInformation("stringDSVL");
        assertEquals(HDF5DataClass.STRING, stringInfoVL.getTypeInformation().getDataClass());
        assertTrue(stringInfoVL.getTypeInformation().isVariableLengthType());
        assertEquals(-1, stringInfoVL.getTypeInformation().getElementSize());
        assertEquals(0, stringInfoVL.getDimensions().length);
        assertEquals(HDF5StorageLayout.COMPACT, stringInfoVL.getStorageLayout());
        assertNull(stringInfoVL.tryGetChunkSizes());
        assertEquals(0, stringInfoVL.getDimensions().length);
        assertEquals(0, stringInfoVL.getMaxDimensions().length);
        reader.close();
    }

    @Test(expectedExceptions = HDF5SymbolTableException.class)
    public void testGetDataSetInformationFailed()
    {
        final File file = new File(workingDirectory, "dsInfo.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        try
        {
            assertFalse(writer.exists("non_existent"));
            writer.getDataSetInformation("non_existent");
        } finally
        {
            writer.close();
        }
    }

    @Test
    public void testGetGroupMemberInformation()
    {
        final File groupFile = new File(workingDirectory, "groupMemberInformation.h5");
        groupFile.delete();
        assertFalse(groupFile.exists());
        groupFile.deleteOnExit();
        final String groupName1 = "/group";
        final String groupName2 = "/dataSetGroup";
        final String dataSetName = groupName2 + "/dataset";
        final String dataSetName2 = "ds2";
        final String linkName = "/link";
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(groupFile);
        try
        {
            writer.createGroup(groupName1);
            writer.writeByteArray(dataSetName, new byte[]
                { 1 });
            writer.writeString(dataSetName2, "abc");
            writer.createSoftLink(dataSetName2, linkName);
        } finally
        {
            writer.close();
        }
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(groupFile);
        final Map<String, HDF5LinkInformation> map = new HashMap<String, HDF5LinkInformation>();
        for (HDF5LinkInformation info : reader.getAllGroupMemberInformation("/", false))
        {
            map.put(info.getPath(), info);
        }
        HDF5LinkInformation info;
        assertEquals(5, map.size());
        info = map.get(groupName1);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.GROUP, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());
        info = map.get(groupName2);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.GROUP, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());
        info = map.get("/" + dataSetName2);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.DATASET, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());
        info = map.get(linkName);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.SOFT_LINK, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());

        map.clear();
        for (HDF5LinkInformation info2 : reader.getGroupMemberInformation("/", true))
        {
            map.put(info2.getPath(), info2);
        }
        assertEquals(4, map.size());
        info = map.get(groupName1);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.GROUP, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());
        info = map.get(groupName2);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.GROUP, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());
        info = map.get("/" + dataSetName2);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.DATASET, info.getType());
        assertNull(info.tryGetSymbolicLinkTarget());
        info = map.get(linkName);
        assertNotNull(info);
        assertTrue(info.exists());
        assertEquals(HDF5ObjectType.SOFT_LINK, info.getType());
        assertEquals(dataSetName2, info.tryGetSymbolicLinkTarget());

        reader.close();
    }

    @Test
    public void testHardLink()
    {
        final File linkFile = new File(workingDirectory, "hardLink.h5");
        linkFile.delete();
        assertFalse(linkFile.exists());
        linkFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(linkFile);
        final String str = "BlaBlub";
        writer.writeString("/data/set", str);
        writer.createHardLink("/data/set", "/data/link");
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(linkFile);
        assertEquals(HDF5ObjectType.DATASET, reader.getObjectType("/data/link"));
        assertEquals(str, reader.readString("/data/link"));
        reader.close();
    }

    @Test
    public void testSoftLink()
    {
        final File linkFile = new File(workingDirectory, "softLink.h5");
        linkFile.delete();
        assertFalse(linkFile.exists());
        linkFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(linkFile);
        writer.writeBoolean("/data/set", true);
        writer.createSoftLink("/data/set", "/data/link");
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(linkFile);
        assertEquals(HDF5ObjectType.SOFT_LINK, reader.getObjectType("/data/link", false));
        assertEquals("/data/set", reader.getLinkInformation("/data/link")
                .tryGetSymbolicLinkTarget());
        reader.close();
    }

    @Test
    public void testUpdateSoftLink()
    {
        final File file = new File(workingDirectory, "updateSoftLink.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final long now = System.currentTimeMillis();
        final String dataSetName1 = "creationTime1";
        final String dataSetName2 = "creationTime2";
        final String linkName = "time";
        writer.writeTimeStamp(dataSetName1, now);
        writer.writeTimeStamp(dataSetName2, now);
        writer.createSoftLink(dataSetName1, linkName);
        writer.createOrUpdateSoftLink(dataSetName2, linkName);
        try
        {
            writer.createOrUpdateSoftLink(dataSetName1, dataSetName2);
        } catch (HDF5LibraryException ex)
        {
            assertEquals(HDF5Constants.H5E_EXISTS, ex.getMinorErrorNumber());
        }
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        assertEquals(dataSetName2, reader.getLinkInformation(linkName).tryGetSymbolicLinkTarget());
        reader.close();
    }

    @Test
    public void testBrokenSoftLink()
    {
        final File linkFile = new File(workingDirectory, "brokenSoftLink.h5");
        linkFile.delete();
        assertFalse(linkFile.exists());
        linkFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(linkFile);
        writer.createSoftLink("/does/not/exist", "/linkToNowhere");
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(linkFile);
        assertFalse(reader.exists("/linkToNowhere"));
        assertTrue(reader.exists("/linkToNowhere", false));
        assertEquals(HDF5ObjectType.SOFT_LINK, reader.getObjectType("/linkToNowhere", false));
        assertEquals("/does/not/exist", reader.getLinkInformation("/linkToNowhere")
                .tryGetSymbolicLinkTarget());
        reader.close();
    }

    @Test
    public void testDeleteSoftLink()
    {
        final File linkFile = new File(workingDirectory, "deleteSoftLink.h5");
        linkFile.delete();
        assertFalse(linkFile.exists());
        linkFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(linkFile);
        writer.writeBoolean("/group/boolean", true);
        writer.createSoftLink("/group", "/link");
        writer.delete("/link");
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(linkFile);
        assertFalse(reader.exists("/link", false));
        assertTrue(reader.exists("/group"));
        assertTrue(reader.exists("/group/boolean"));
        reader.close();
    }

    @Test
    public void testNullOnGetSymbolicLinkTargetForNoLink()
    {
        final File noLinkFile = new File(workingDirectory, "noLink.h5");
        noLinkFile.delete();
        assertFalse(noLinkFile.exists());
        noLinkFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(noLinkFile);
        writer.writeBoolean("/data/set", true);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(noLinkFile);
        try
        {
            assertNull(reader.getLinkInformation("/data/set").tryGetSymbolicLinkTarget());
        } finally
        {
            reader.close();
        }
    }

    @Test
    public void testExternalLink()
    {
        final File fileToLinkTo = new File(workingDirectory, "fileToLinkTo.h5");
        fileToLinkTo.delete();
        assertFalse(fileToLinkTo.exists());
        fileToLinkTo.deleteOnExit();
        final IHDF5Writer writer1 = HDF5FactoryProvider.get().open(fileToLinkTo);
        final String dataSetName = "/data/set";
        final String dataSetValue = "Some data set value...";
        writer1.writeString(dataSetName, dataSetValue);
        writer1.close();
        final File linkFile = new File(workingDirectory, "externalLink.h5");
        linkFile.delete();
        assertFalse(linkFile.exists());
        linkFile.deleteOnExit();
        final IHDF5Writer writer2 = HDF5FactoryProvider.get().open(linkFile);
        final String linkName = "/data/link";
        writer2.createExternalLink(fileToLinkTo.getPath(), dataSetName, linkName);
        writer2.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(linkFile);
        assertEquals(HDF5ObjectType.EXTERNAL_LINK, reader.getObjectType(linkName, false));
        assertEquals(dataSetValue, reader.readString(linkName));
        final String expectedLink =
                OSUtilities.isWindows() ? "EXTERNAL::targets\\unit-test-wd\\hdf5-roundtrip-wd\\fileToLinkTo.h5::/data/set"
                        : "EXTERNAL::targets/unit-test-wd/hdf5-roundtrip-wd/fileToLinkTo.h5::/data/set";
        assertEquals(expectedLink, reader.getLinkInformation(linkName).tryGetSymbolicLinkTarget());
        reader.close();
    }

    @Test
    public void testDataTypeInfoOptions()
    {
        final File file = new File(workingDirectory, "dataTypeInfoOptions.h5");
        final String enumDsName = "/testEnum";
        final String dateDsName = "/testDate";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.configure(file).writer();
        writer.writeEnum(enumDsName, JavaEnum.TWO);
        writer.writeDate(dateDsName, new Date(10000L));
        writer.close();
        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5DataTypeInformation minimalEnumInfo =
                reader.getDataSetInformation(enumDsName, DataTypeInfoOptions.MINIMAL)
                        .getTypeInformation();
        assertFalse(minimalEnumInfo.knowsDataTypePath());
        assertFalse(minimalEnumInfo.knowsDataTypeVariant());
        assertNull(minimalEnumInfo.tryGetName());
        assertNull(minimalEnumInfo.tryGetTypeVariant());
        final HDF5DataTypeInformation defaultInfo =
                reader.getDataSetInformation(enumDsName).getTypeInformation();
        assertFalse(defaultInfo.knowsDataTypePath());
        assertTrue(defaultInfo.knowsDataTypeVariant());
        assertNull(defaultInfo.tryGetName());
        assertEquals(HDF5DataTypeVariant.NONE, defaultInfo.tryGetTypeVariant());
        final HDF5DataTypeInformation allInfo =
                reader.getDataSetInformation(enumDsName, DataTypeInfoOptions.ALL)
                        .getTypeInformation();
        assertTrue(allInfo.knowsDataTypePath());
        assertTrue(allInfo.knowsDataTypeVariant());
        assertEquals(JavaEnum.class.getSimpleName(), allInfo.tryGetName());

        final HDF5DataTypeInformation minimalDateInfo =
                reader.getDataSetInformation(dateDsName, DataTypeInfoOptions.MINIMAL)
                        .getTypeInformation();
        assertFalse(minimalDateInfo.knowsDataTypePath());
        assertFalse(minimalDateInfo.knowsDataTypeVariant());
        assertNull(minimalDateInfo.tryGetName());
        assertNull(minimalDateInfo.tryGetTypeVariant());

        final HDF5DataTypeInformation defaultDateInfo =
                reader.getDataSetInformation(dateDsName, DataTypeInfoOptions.DEFAULT)
                        .getTypeInformation();
        assertFalse(defaultDateInfo.knowsDataTypePath());
        assertTrue(defaultDateInfo.knowsDataTypeVariant());
        assertNull(defaultDateInfo.tryGetName());
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                defaultDateInfo.tryGetTypeVariant());

        final HDF5DataTypeInformation allDateInfo =
                reader.getDataSetInformation(dateDsName, DataTypeInfoOptions.ALL)
                        .getTypeInformation();
        assertTrue(allDateInfo.knowsDataTypePath());
        assertTrue(allDateInfo.knowsDataTypeVariant());
        assertNull(allDateInfo.tryGetName());
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                allDateInfo.tryGetTypeVariant());

        reader.close();
    }

    enum JavaEnum
    {
        ONE, TWO, THREE
    }

    @Test
    public void testJavaEnum()
    {
        final File file = new File(workingDirectory, "javaEnum.h5");
        final String dsName = "/testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.configure(file).keepDataSetsIfTheyExist().writer();
        writer.writeEnum(dsName, JavaEnum.THREE);
        writer.setEnumAttribute(dsName, "attr", JavaEnum.TWO);
        writer.close();
        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        assertEquals(JavaEnum.THREE, reader.readEnum(dsName, JavaEnum.class));
        assertEquals(JavaEnum.TWO, reader.getEnumAttribute(dsName, "attr", JavaEnum.class));
        final String valueStr = reader.readEnumAsString(dsName);
        assertEquals("THREE", valueStr);
        final HDF5EnumerationValue value = reader.readEnum(dsName);
        assertEquals("THREE", value.getValue());
        final String expectedDataTypePath =
                HDF5Utils.createDataTypePath(HDF5Utils.ENUM_PREFIX, JavaEnum.class.getSimpleName());
        assertEquals(expectedDataTypePath, reader.tryGetDataTypePath(value.getType()));
        assertEquals(expectedDataTypePath, reader.tryGetDataTypePath(dsName));
        final HDF5EnumerationType type = reader.getDataSetEnumType(dsName);
        assertEquals(3, type.getValues().size());
        assertEquals("ONE", type.getValues().get(0));
        assertEquals("TWO", type.getValues().get(1));
        assertEquals("THREE", type.getValues().get(2));
        reader.close();
    }

    @Test
    public void testEnum()
    {
        final File file = new File(workingDirectory, "enum.h5");
        final String enumTypeName = "testEnumType";
        final String dsName = "/testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.configure(file).keepDataSetsIfTheyExist().writer();
        HDF5EnumerationType type = writer.getEnumType(enumTypeName, new String[]
            { "ONE", "TWO", "THREE" }, false);
        writer.writeEnum(dsName, new HDF5EnumerationValue(type, "THREE"));
        // That is wrong, but we disable the check, so no exception should be thrown.
        writer.getEnumType(enumTypeName, new String[]
            { "THREE", "ONE", "TWO" }, false);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        type = reader.getEnumType(enumTypeName);
        assertEquals(enumTypeName, type.tryGetName());
        final HDF5DataTypeInformation typeInfo =
                reader.getDataSetInformation(dsName, DataTypeInfoOptions.ALL).getTypeInformation();
        assertEquals(enumTypeName, typeInfo.tryGetName());
        assertEquals(HDF5Utils.createDataTypePath(HDF5Utils.ENUM_PREFIX, enumTypeName),
                typeInfo.tryGetDataTypePath());
        final String valueStr = reader.readEnumAsString(dsName);
        assertEquals("THREE", valueStr);
        final HDF5EnumerationValue value = reader.readEnum(dsName);
        assertEquals("THREE", value.getValue());
        final String expectedDataTypePath =
                HDF5Utils.createDataTypePath(HDF5Utils.ENUM_PREFIX, enumTypeName);
        assertEquals(expectedDataTypePath, reader.tryGetDataTypePath(value.getType()));
        assertEquals(expectedDataTypePath, reader.tryGetDataTypePath(dsName));
        type = reader.getDataSetEnumType(dsName);
        assertEquals("THREE", reader.readEnum(dsName, type).getValue());
        reader.close();
        final IHDF5Writer writer2 = HDF5FactoryProvider.get().open(file);
        type = writer2.getEnumType(enumTypeName, new String[]
            { "ONE", "TWO", "THREE" }, true);
        assertEquals("THREE", writer2.readEnum(dsName, type).getValue());
        writer2.close();
    }

    @Test
    public void testEnum16()
    {
        final File file = new File(workingDirectory, "enum16bit.h5");
        final String enumTypeName = "testEnumType16";
        final String dsName = "/testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.configure(file).keepDataSetsIfTheyExist().writer();
        HDF5EnumerationType type = createEnum16Bit(writer, enumTypeName);
        writer.writeEnum(dsName, new HDF5EnumerationValue(type, "17"));
        final String[] confusedValues = new String[type.getValueArray().length];
        System.arraycopy(confusedValues, 0, confusedValues, 1, confusedValues.length - 1);
        confusedValues[0] = "XXX";
        // This is wrong, but we disabled the check.
        writer.getEnumType(enumTypeName, confusedValues, false);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        type = reader.getEnumType(enumTypeName);
        final String valueStr = reader.readEnumAsString(dsName);
        assertEquals("17", valueStr);
        final HDF5EnumerationValue value = reader.readEnum(dsName);
        assertEquals("17", value.getValue());
        type = reader.getDataSetEnumType(dsName);
        assertEquals("17", reader.readEnum(dsName, type).getValue());
        reader.close();
        final IHDF5Writer writer2 = HDF5FactoryProvider.get().open(file);
        type = writer2.getEnumType(enumTypeName, type.getValueArray(), true);
        assertEquals("17", writer2.readEnum(dsName, type).getValue());
        // That is wrong, but we disable the check, so no exception should be thrown.
        writer2.close();
    }

    @Test(expectedExceptions = HDF5JavaException.class)
    public void testConfusedEnum()
    {
        final File file = new File(workingDirectory, "confusedEnum.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        IHDF5Writer writer = HDF5Factory.open(file);
        HDF5EnumerationType type = writer.getEnumType("testEnum", new String[]
            { "ONE", "TWO", "THREE" }, false);
        writer.writeEnum("/testEnum", new HDF5EnumerationValue(type, 2));
        writer.close();
        try
        {
            writer = HDF5Factory.configure(file).keepDataSetsIfTheyExist().writer();
            writer.getEnumType("testEnum", new String[]
                { "THREE", "ONE", "TWO" }, true);
        } finally
        {
            writer.close();
        }
    }

    @Test
    public void testReplaceConfusedEnum()
    {
        final File file = new File(workingDirectory, "replaceConfusedEnum.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        IHDF5Writer writer = HDF5Factory.open(file);
        HDF5EnumerationType type = writer.getEnumType("testEnum", new String[]
            { "ONE", "TWO", "THREE" }, false);
        writer.writeEnum("/testEnum", new HDF5EnumerationValue(type, 2));
        writer.close();
        writer = HDF5Factory.open(file);
        final HDF5EnumerationType type2 = writer.getEnumType("testEnum", new String[]
            { "THREE", "ONE", "TWO" }, true);
        assertEquals("testEnum", type2.getName());
        assertEquals("testEnum__REPLACED_1", writer.getDataSetEnumType("/testEnum").getName());
        writer.close();
    }

    @Test
    public void testEnumArray()
    {
        final File file = new File(workingDirectory, "enumArray.h5");
        final String enumTypeName = "testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5EnumerationType enumType = writer.getEnumType(enumTypeName, new String[]
            { "ONE", "TWO", "THREE" }, false);
        HDF5EnumerationValueArray arrayWritten =
                new HDF5EnumerationValueArray(enumType, new String[]
                    { "TWO", "ONE", "THREE" });
        writer.writeEnumArray("/testEnum", arrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5EnumerationValueArray arrayRead = reader.readEnumArray("/testEnum");
        enumType = reader.getDataSetEnumType("/testEnum");
        final HDF5EnumerationValueArray arrayRead2 = reader.readEnumArray("/testEnum", enumType);
        final String[] stringArrayRead = reader.readEnumArrayAsString("/testEnum");
        assertEquals(arrayWritten.getLength(), stringArrayRead.length);
        assertEquals(arrayWritten.getLength(), arrayRead.getLength());
        assertEquals(arrayWritten.getLength(), arrayRead2.getLength());
        for (int i = 0; i < stringArrayRead.length; ++i)
        {
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayRead.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayRead2.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), stringArrayRead[i]);
        }
        reader.close();
    }

    @Test
    public void testJavaEnumArray()
    {
        final File file = new File(workingDirectory, "javaEnumArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final JavaEnum[] arrayWritten = new JavaEnum[]
            { JavaEnum.TWO, JavaEnum.ONE, JavaEnum.THREE, JavaEnum.ONE };
        writer.writeEnumArray("/testEnum", arrayWritten);
        final JavaEnum[] attributeArrayWritten = new JavaEnum[]
            { JavaEnum.THREE, JavaEnum.ONE, JavaEnum.TWO };
        writer.setEnumArrayAttribute("/testEnum", "attr", attributeArrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final JavaEnum[] arrayRead = reader.readEnumArray("/testEnum", JavaEnum.class);
        final JavaEnum[] attributeArrayRead =
                reader.getEnumArrayAttribute("/testEnum", "attr", JavaEnum.class);
        reader.close();
        assertEquals(arrayWritten.length, arrayRead.length);
        for (int i = 0; i < arrayWritten.length; ++i)
        {
            assertEquals(arrayWritten[i], arrayRead[i]);
        }
        assertEquals(attributeArrayWritten.length, attributeArrayRead.length);
        for (int i = 0; i < attributeArrayWritten.length; ++i)
        {
            assertEquals(attributeArrayWritten[i], attributeArrayRead[i]);
        }
    }

    @Test
    public void testEnumArrayBlock()
    {
        final File file = new File(workingDirectory, "enumArrayBlock.h5");
        final String enumTypeName = "testEnum";
        final int chunkSize = 4;
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5EnumerationType enumType = writer.getEnumType(enumTypeName, new String[]
            { "ONE", "TWO", "THREE" }, false);
        writer.createEnumArray("/testEnum", enumType, chunkSize);
        HDF5EnumerationValueArray arrayWritten =
                new HDF5EnumerationValueArray(enumType, new String[]
                    { "TWO", "ONE", "THREE", "TWO" });
        writer.writeEnumArrayBlock("/testEnum", arrayWritten, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5EnumerationValueArray arrayReadBlock0 =
                reader.readEnumArrayBlock(enumTypeName, chunkSize, 0);
        enumType = reader.getDataSetEnumType(enumTypeName);
        final HDF5EnumerationValueArray arrayReadBlock1 =
                reader.readEnumArrayBlock(enumTypeName, enumType, chunkSize, 1);
        final String[] stringArrayRead = reader.readEnumArrayAsString(enumTypeName);
        assertEquals(arrayWritten.getLength() * 2, stringArrayRead.length);
        assertEquals(arrayWritten.getLength(), arrayReadBlock0.getLength());
        assertEquals(arrayWritten.getLength(), arrayReadBlock1.getLength());
        for (int i = 0; i < arrayReadBlock0.getLength(); ++i)
        {
            assertEquals("Index " + i, "ONE", arrayReadBlock0.getValue(i));
            assertEquals("Index " + i, "ONE", stringArrayRead[i]);
        }
        for (int i = 0; i < arrayReadBlock0.getLength(); ++i)
        {
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayReadBlock1.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), stringArrayRead[chunkSize + i]);
        }
        final HDF5EnumerationValueArray[] dataBlocksExpected = new HDF5EnumerationValueArray[]
            { arrayReadBlock0, arrayReadBlock1 };
        int blockIndex = 0;
        for (HDF5DataBlock<HDF5EnumerationValueArray> block : reader.getEnumArrayNaturalBlocks(
                enumTypeName, enumType))
        {
            final HDF5EnumerationValueArray blockExpected = dataBlocksExpected[blockIndex++];
            final HDF5EnumerationValueArray blockRead = block.getData();
            assertEquals(chunkSize, blockRead.getLength());
            for (int i = 0; i < blockExpected.getLength(); ++i)
            {
                assertEquals("Index " + i, blockExpected.getValue(i), blockRead.getValue(i));
            }
        }
        reader.close();
    }

    @Test
    public void testEnumArrayBlockScalingCompression()
    {
        final File file = new File(workingDirectory, "enumArrayBlockScalingCompression.h5");
        final String enumTypeName = "testEnum";
        final int chunkSize = 4;
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5EnumerationType enumType = writer.getEnumType(enumTypeName, new String[]
            { "ONE", "TWO", "THREE" }, false);
        writer.createEnumArray("/testEnum", enumType, 0, chunkSize,
                HDF5IntStorageFeatures.INT_AUTO_SCALING);
        HDF5EnumerationValueArray arrayWritten =
                new HDF5EnumerationValueArray(enumType, new String[]
                    { "TWO", "ONE", "THREE", "ONE" });
        writer.writeEnumArrayBlock("/testEnum", arrayWritten, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5EnumerationValueArray arrayReadBlock0 =
                reader.readEnumArrayBlock(enumTypeName, chunkSize, 0);
        enumType = reader.getDataSetEnumType(enumTypeName);
        final HDF5EnumerationValueArray arrayReadBlock1 =
                reader.readEnumArrayBlock(enumTypeName, enumType, chunkSize, 1);
        final String[] stringArrayRead = reader.readEnumArrayAsString(enumTypeName);
        assertEquals(arrayWritten.getLength() * 2, stringArrayRead.length);
        assertEquals(arrayWritten.getLength(), arrayReadBlock0.getLength());
        assertEquals(arrayWritten.getLength(), arrayReadBlock1.getLength());
        for (int i = 0; i < arrayReadBlock0.getLength(); ++i)
        {
            assertEquals("Index " + i, "ONE", arrayReadBlock0.getValue(i));
            assertEquals("Index " + i, "ONE", stringArrayRead[i]);
        }
        for (int i = 0; i < arrayReadBlock0.getLength(); ++i)
        {
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayReadBlock1.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), stringArrayRead[chunkSize + i]);
        }
        final HDF5EnumerationValueArray[] dataBlocksExpected = new HDF5EnumerationValueArray[]
            { arrayReadBlock0, arrayReadBlock1 };
        int blockIndex = 0;
        for (HDF5DataBlock<HDF5EnumerationValueArray> block : reader.getEnumArrayNaturalBlocks(
                enumTypeName, enumType))
        {
            final HDF5EnumerationValueArray blockExpected = dataBlocksExpected[blockIndex++];
            final HDF5EnumerationValueArray blockRead = block.getData();
            assertEquals(chunkSize, blockRead.getLength());
            for (int i = 0; i < blockExpected.getLength(); ++i)
            {
                assertEquals("Index " + i, blockExpected.getValue(i), blockRead.getValue(i));
            }
        }
        reader.close();
    }

    @Test
    public void testEnumArray16BitFromIntArray()
    {
        final File file = new File(workingDirectory, "enumArray16BitFromIntArray.h5");
        final String enumTypeName = "testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = createEnum16Bit(writer, enumTypeName);
        final int[] arrayWritten = new int[]
            { 8, 16, 722, 913, 333 };
        writer.writeEnumArray("/testEnum", new HDF5EnumerationValueArray(enumType, arrayWritten));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final String[] stringArrayRead = reader.readEnumArrayAsString("/testEnum");
        assertEquals(arrayWritten.length, stringArrayRead.length);
        for (int i = 0; i < stringArrayRead.length; ++i)
        {
            assertEquals("Index " + i, enumType.getValues().get(arrayWritten[i]),
                    stringArrayRead[i]);
        }
        final HDF5EnumerationValueArray arrayRead = reader.readEnumArray("/testEnum");
        assertEquals(arrayWritten.length, arrayRead.getLength());
        for (int i = 0; i < arrayRead.getLength(); ++i)
        {
            assertEquals("Index " + i, enumType.getValues().get(arrayWritten[i]),
                    arrayRead.getValue(i));
        }
        reader.close();
    }

    @Test
    public void testEnumArray16BitFromIntArrayScaled()
    {
        final File file = new File(workingDirectory, "testEnumArray16BitFromIntArrayScaled.h5");
        final String enumTypeName = "testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = createEnum16Bit(writer, enumTypeName);
        final int[] arrayWritten = new int[]
            { 8, 16, 722, 913, 333 };
        writer.writeEnumArray("/testEnum", new HDF5EnumerationValueArray(enumType, arrayWritten),
                HDF5IntStorageFeatures.INT_AUTO_SCALING);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final String[] stringArrayRead = reader.readEnumArrayAsString("/testEnum");
        assertEquals(arrayWritten.length, stringArrayRead.length);
        for (int i = 0; i < stringArrayRead.length; ++i)
        {
            assertEquals("Index " + i, enumType.getValues().get(arrayWritten[i]),
                    stringArrayRead[i]);
        }
        final HDF5EnumerationValueArray arrayRead = reader.readEnumArray("/testEnum");
        assertEquals(arrayWritten.length, arrayRead.getLength());
        for (int i = 0; i < arrayRead.getLength(); ++i)
        {
            assertEquals("Index " + i, enumType.getValues().get(arrayWritten[i]),
                    arrayRead.getValue(i));
        }
        reader.close();
    }

    @Test
    public void testEnumArray16BitFromIntArrayLarge()
    {
        final File file = new File(workingDirectory, "enumArray16BitFromIntArrayLarge.h5");
        final String enumTypeName = "testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = createEnum16Bit(writer, enumTypeName);
        final int[] arrayWritten = new int[100];
        for (int i = 0; i < arrayWritten.length; ++i)
        {
            arrayWritten[i] = 10 * i;
        }
        writer.writeEnumArray("/testEnum", new HDF5EnumerationValueArray(enumType, arrayWritten));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final String[] stringArrayRead = reader.readEnumArrayAsString("/testEnum");
        assertEquals(arrayWritten.length, stringArrayRead.length);
        for (int i = 0; i < stringArrayRead.length; ++i)
        {
            assertEquals("Index " + i, enumType.getValues().get(arrayWritten[i]),
                    stringArrayRead[i]);
        }
        reader.close();
    }

    @Test
    public void testEnumArrayBlock16Bit()
    {
        final File file = new File(workingDirectory, "enumArrayBlock16Bit.h5");
        final String enumTypeName = "testEnum";
        final int chunkSize = 4;
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5EnumerationType enumType = createEnum16Bit(writer, enumTypeName);
        writer.createEnumArray("/testEnum", enumType, chunkSize);
        final HDF5EnumerationValueArray arrayWritten =
                new HDF5EnumerationValueArray(enumType, new int[]
                    { 8, 16, 722, 913 });
        writer.writeEnumArrayBlock("/testEnum", arrayWritten, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5EnumerationValueArray arrayReadBlock0 =
                reader.readEnumArrayBlock(enumTypeName, chunkSize, 0);
        enumType = reader.getDataSetEnumType(enumTypeName);
        final HDF5EnumerationValueArray arrayReadBlock1 =
                reader.readEnumArrayBlock(enumTypeName, enumType, chunkSize, 1);
        final String[] stringArrayRead = reader.readEnumArrayAsString(enumTypeName);
        assertEquals(arrayWritten.getLength() * 2, stringArrayRead.length);
        assertEquals(arrayWritten.getLength(), arrayReadBlock0.getLength());
        assertEquals(arrayWritten.getLength(), arrayReadBlock1.getLength());
        for (int i = 0; i < arrayReadBlock0.getLength(); ++i)
        {
            assertEquals("Index " + i, "0", arrayReadBlock0.getValue(i));
            assertEquals("Index " + i, "0", stringArrayRead[i]);
        }
        for (int i = 0; i < arrayReadBlock0.getLength(); ++i)
        {
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayReadBlock1.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), stringArrayRead[chunkSize + i]);
        }
        final HDF5EnumerationValueArray[] dataBlocksExpected = new HDF5EnumerationValueArray[]
            { arrayReadBlock0, arrayReadBlock1 };
        int blockIndex = 0;
        for (HDF5DataBlock<HDF5EnumerationValueArray> block : reader.getEnumArrayNaturalBlocks(
                enumTypeName, enumType))
        {
            final HDF5EnumerationValueArray blockExpected = dataBlocksExpected[blockIndex++];
            final HDF5EnumerationValueArray blockRead = block.getData();
            assertEquals(chunkSize, blockRead.getLength());
            for (int i = 0; i < blockExpected.getLength(); ++i)
            {
                assertEquals("Index " + i, blockExpected.getValue(i), blockRead.getValue(i));
            }
        }
        reader.close();
    }

    @Test
    public void testEnumArrayScaleCompression()
    {
        final File file = new File(workingDirectory, "enumArrayScaleCompression.h5");
        final String enumTypeName = "testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5EnumerationType enumType = writer.getEnumType(enumTypeName, new String[]
            { "A", "C", "G", "T" }, false);
        final Random rng = new Random();
        final String[] arrayWrittenString = new String[100000];
        for (int i = 0; i < arrayWrittenString.length; ++i)
        {
            arrayWrittenString[i] = enumType.getValues().get(rng.nextInt(4));
        }
        final HDF5EnumerationValueArray arrayWritten =
                new HDF5EnumerationValueArray(enumType, arrayWrittenString);
        writer.writeEnumArray("/testEnum", arrayWritten, INT_AUTO_SCALING);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5EnumerationValueArray arrayRead = reader.readEnumArray("/testEnum");
        enumType = reader.getDataSetEnumType("/testEnum");
        final HDF5EnumerationValueArray arrayRead2 = reader.readEnumArray("/testEnum", enumType);
        final String[] stringArrayRead = reader.readEnumArrayAsString("/testEnum");
        assertEquals(arrayWritten.getLength(), stringArrayRead.length);
        assertEquals(arrayWritten.getLength(), arrayRead.getLength());
        assertEquals(arrayWritten.getLength(), arrayRead2.getLength());
        for (int i = 0; i < stringArrayRead.length; ++i)
        {
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayRead.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), arrayRead2.getValue(i));
            assertEquals("Index " + i, arrayWritten.getValue(i), stringArrayRead[i]);
        }
        reader.close();

        // Shouldn't work in strict HDF5 1.6 mode.
        final File file2 = new File(workingDirectory, "scaleoffsetfilterenumfailed.h5");
        file2.delete();
        assertFalse(file2.exists());
        file2.deleteOnExit();
        final IHDF5Writer writer2 =
                HDF5FactoryProvider.get().configure(file2).fileFormat(FileFormat.STRICTLY_1_6)
                        .writer();
        HDF5EnumerationType enumType2 = writer2.getEnumType(enumTypeName, new String[]
            { "A", "C", "G", "T" }, false);
        final HDF5EnumerationValueArray arrayWritten2 =
                new HDF5EnumerationValueArray(enumType2, arrayWrittenString);
        try
        {
            writer2.writeEnumArray("/testEnum", arrayWritten2, INT_AUTO_SCALING);
            fail("Usage of scaling compression in strict HDF5 1.6 mode not detected");
        } catch (IllegalStateException ex)
        {
            assertTrue(ex.getMessage().indexOf("not allowed") >= 0);
        }
        writer2.close();
    }

    @Test
    public void testOpaqueType()
    {
        final File file = new File(workingDirectory, "opaqueType.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final String opaqueDataSetName = "/opaque/ds";
        final String byteArrayDataSetName = "/bytearr/ds";
        final String opaqueTag = "my opaque type";
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final byte[] byteArrayWritten = new byte[]
            { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        writer.writeByteArray(byteArrayDataSetName, byteArrayWritten);
        writer.writeOpaqueByteArray(opaqueDataSetName, opaqueTag, byteArrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        HDF5DataSetInformation info = reader.getDataSetInformation(byteArrayDataSetName);
        assertEquals(HDF5DataClass.INTEGER, info.getTypeInformation().getDataClass());
        assertChunkSizes(info, byteArrayWritten.length);
        info = reader.getDataSetInformation(opaqueDataSetName);
        assertEquals(HDF5DataClass.OPAQUE, info.getTypeInformation().getDataClass());
        assertEquals(opaqueTag, info.getTypeInformation().tryGetOpaqueTag());
        assertChunkSizes(info, byteArrayWritten.length);
        assertEquals(opaqueTag, reader.tryGetOpaqueTag(opaqueDataSetName));
        assertEquals(opaqueTag, reader.tryGetOpaqueType(opaqueDataSetName).getTag());
        assertNull(reader.tryGetOpaqueTag(byteArrayDataSetName));
        assertNull(reader.tryGetOpaqueType(byteArrayDataSetName));
        final byte[] byteArrayRead = reader.readAsByteArray(byteArrayDataSetName);
        assertTrue(Arrays.equals(byteArrayWritten, byteArrayRead));
        final byte[] byteArrayReadOpaque = reader.readAsByteArray(opaqueDataSetName);
        assertTrue(Arrays.equals(byteArrayWritten, byteArrayReadOpaque));
        reader.close();
    }

    private HDF5EnumerationType createEnum16Bit(final IHDF5Writer writer, final String enumTypeName)
    {
        final String[] enumValues = new String[1024];
        for (int i = 0; i < enumValues.length; ++i)
        {
            enumValues[i] = Integer.toString(i);
        }
        final HDF5EnumerationType enumType = writer.getEnumType(enumTypeName, enumValues, false);
        return enumType;
    }

    @Test
    public void testEnumArrayFromIntArray()
    {
        final File file = new File(workingDirectory, "enumArrayFromIntArray.h5");
        final String enumTypeName = "testEnum";
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = writer.getEnumType(enumTypeName, new String[]
            { "ONE", "TWO", "THREE" }, false);
        final int[] arrayWritten =
                new int[]
                    { enumType.tryGetIndexForValue("TWO").byteValue(),
                            enumType.tryGetIndexForValue("ONE").byteValue(),
                            enumType.tryGetIndexForValue("THREE").byteValue() };
        writer.writeEnumArray("/testEnum", new HDF5EnumerationValueArray(enumType, arrayWritten));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final String[] stringArrayRead = reader.readEnumArrayAsString("/testEnum");
        assertEquals(arrayWritten.length, stringArrayRead.length);
        for (int i = 0; i < stringArrayRead.length; ++i)
        {
            assertEquals("Index " + i, enumType.getValues().get(arrayWritten[i]),
                    stringArrayRead[i]);
        }
        reader.close();
    }

    static class Record
    {
        int a;

        float b;

        long l;

        double c;

        short d;

        boolean e;

        String f;

        HDF5EnumerationValue g;

        int[] ar;

        float[] br;

        long[] lr;

        double[] cr;

        short[] dr;

        byte[] er;

        MDIntArray fr;

        char[] gr;

        Record(int a, float b, long l, double c, short d, boolean e, String f,
                HDF5EnumerationValue g, int[] ar, float[] br, long[] lr, double[] cr, short[] dr,
                byte[] er, MDIntArray fr, char[] gr)
        {
            this.a = a;
            this.b = b;
            this.l = l;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
            this.g = g;
            this.ar = ar;
            this.br = br;
            this.lr = lr;
            this.cr = cr;
            this.dr = dr;
            this.er = er;
            this.fr = fr;
            this.gr = gr;
        }

        Record()
        {
        }

        static HDF5CompoundMemberInformation[] getMemberInfo(HDF5EnumerationType enumType)
        {
            return HDF5CompoundMemberInformation.create(Record.class, getShuffledMapping(enumType));
        }

        static HDF5CompoundType<Record> getHDF5Type(IHDF5Reader reader)
        {
            final HDF5EnumerationType enumType = reader.getEnumType("someEnumType", new String[]
                { "1", "Two", "THREE" });
            return reader.getCompoundType(null, Record.class, getMapping(enumType));
        }

        private static HDF5CompoundMemberMapping[] getMapping(HDF5EnumerationType enumType)
        {
            return new HDF5CompoundMemberMapping[]
                { mapping("a"), mapping("b"), mapping("l"), mapping("c"), mapping("d"),
                        mapping("e"), mapping("f").length(3), mapping("g").enumType(enumType),
                        mapping("ar").length(3), mapping("br").length(2), mapping("lr").length(3),
                        mapping("cr").length(1), mapping("dr").length(2), mapping("er").length(4),
                        mapping("fr").dimensions(2, 2), mapping("gr").length(5) };
        }

        private static HDF5CompoundMemberMapping[] getShuffledMapping(HDF5EnumerationType enumType)
        {
            return new HDF5CompoundMemberMapping[]
                { mapping("er").length(4), mapping("e"), mapping("b"), mapping("br").length(2),
                        mapping("g").enumType(enumType), mapping("lr").length(3),
                        mapping("gr").length(5), mapping("c"), mapping("ar").length(3),
                        mapping("a"), mapping("d"), mapping("cr").length(1),
                        mapping("f").length(3), mapping("fr").dimensions(2, 2),
                        mapping("dr").length(2), mapping("l") };
        }

        //
        // Object
        //

        @Override
        public int hashCode()
        {
            final HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(a);
            builder.append(b);
            builder.append(c);
            builder.append(d);
            builder.append(e);
            builder.append(f);
            builder.append(g);
            builder.append(ar);
            builder.append(br);
            builder.append(cr);
            builder.append(dr);
            builder.append(er);
            return builder.toHashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || obj instanceof Record == false)
            {
                return false;
            }
            final Record that = (Record) obj;
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(a, that.a);
            builder.append(b, that.b);
            builder.append(c, that.c);
            builder.append(d, that.d);
            builder.append(e, that.e);
            builder.append(f, that.f);
            builder.append(g, that.g);
            builder.append(ar, that.ar);
            builder.append(br, that.br);
            builder.append(cr, that.cr);
            builder.append(dr, that.dr);
            builder.append(er, that.er);
            builder.append(fr, that.fr);
            return builder.isEquals();
        }

        @Override
        public String toString()
        {
            final ToStringBuilder builder = new ToStringBuilder(this);
            builder.append(a);
            builder.append(b);
            builder.append(c);
            builder.append(d);
            builder.append(e);
            builder.append(f);
            builder.append(g);
            builder.append(ar);
            builder.append(br);
            builder.append(cr);
            builder.append(dr);
            builder.append(er);
            builder.append(fr);
            return builder.toString();
        }

    }

    @Test
    public void testCompoundAttribute()
    {
        final File file = new File(workingDirectory, "compoundAttribute.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final SimpleInheretingRecord recordWritten =
                new SimpleInheretingRecord(3.14159f, 42, (short) 17, "xzy", new long[][]
                    {
                        { 1, 2, 3 },
                        { 4, 5, 6 } });
        writer.setCompoundAttribute("/", "cpd", recordWritten);
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5CompoundType<SimpleInheretingRecord> type =
                reader.getAttributeCompoundType("/", "cpd", SimpleInheretingRecord.class);
        assertFalse(type.isMappingIncomplete());
        assertFalse(type.isDiskRepresentationIncomplete());
        assertFalse(type.isMemoryRepresentationIncomplete());
        type.checkMappingComplete();
        final SimpleInheretingRecord recordRead =
                reader.getCompoundAttribute("/", "cpd", SimpleInheretingRecord.class);
        assertEquals(recordWritten, recordRead);
        reader.close();
    }

    @Test
    public void testCompound()
    {
        final File file = new File(workingDirectory, "compound.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final SimpleInheretingRecord recordWritten =
                new SimpleInheretingRecord(3.14159f, 42, (short) 17, "xzy", new long[][]
                    {
                        { 1, 2, 3 },
                        { 4, 5, 6 } });
        writer.writeCompound("cpd", recordWritten);
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5CompoundType<SimpleInheretingRecord> type =
                reader.getDataSetCompoundType("cpd", SimpleInheretingRecord.class);
        assertFalse(type.isMappingIncomplete());
        assertFalse(type.isDiskRepresentationIncomplete());
        assertFalse(type.isMemoryRepresentationIncomplete());
        type.checkMappingComplete();
        final SimpleInheretingRecord recordRead = reader.readCompound("cpd", type);
        assertEquals(recordWritten, recordRead);
        reader.close();
    }

    @Test
    public void testCompoundJavaEnum()
    {
        final File file = new File(workingDirectory, "compoundJavaEnum.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final JavaEnumCompoundType recordWritten = new JavaEnumCompoundType(TestEnum.CHERRY);
        writer.writeCompound("cpd", recordWritten);
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5CompoundType<JavaEnumCompoundType> type =
                reader.getDataSetCompoundType("cpd", JavaEnumCompoundType.class);
        assertFalse(type.isMappingIncomplete());
        assertFalse(type.isDiskRepresentationIncomplete());
        assertFalse(type.isMemoryRepresentationIncomplete());
        type.checkMappingComplete();
        final JavaEnumCompoundType recordRead = reader.readCompound("cpd", type);
        assertEquals(recordWritten, recordRead);
        reader.close();
    }

    @Test
    public void testCompoundJavaEnumMap()
    {
        final File file = new File(workingDirectory, "compoundJavaEnumMap.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final HDF5CompoundDataMap recordWritten = new HDF5CompoundDataMap();
        recordWritten.put("fruit", TestEnum.ORANGE);
        writer.writeCompound("cpd", recordWritten);
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5CompoundType<HDF5CompoundDataMap> type =
                reader.getDataSetCompoundType("cpd", HDF5CompoundDataMap.class);
        assertFalse(type.isMappingIncomplete());
        assertFalse(type.isDiskRepresentationIncomplete());
        assertFalse(type.isMemoryRepresentationIncomplete());
        type.checkMappingComplete();
        final Map<String, Object> recordRead = reader.readCompound("cpd", type);
        assertEquals(1, recordRead.size());
        assertEquals("ORANGE", recordRead.get("fruit").toString());
        reader.close();
    }

    @Test
    public void testCompoundIncompleteJavaPojo()
    {
        final File file = new File(workingDirectory, "compoundIncompleteJavaPojo.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final SimpleInheretingRecord recordWritten =
                new SimpleInheretingRecord(3.14159f, 42, (short) 17, "xzy", new long[][]
                    {
                        { 1, 2, 3 },
                        { 4, 5, 6 } });
        writer.writeCompound("cpd", recordWritten);
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5CompoundType<SimpleRecord> type =
                reader.getDataSetCompoundType("cpd", SimpleRecord.class);
        assertTrue(type.isMappingIncomplete());
        assertFalse(type.isDiskRepresentationIncomplete());
        assertTrue(type.isMemoryRepresentationIncomplete());
        try
        {
            type.checkMappingComplete();
            fail("Uncomplete mapping not detected.");
        } catch (HDF5JavaException ex)
        {
            assertEquals(
                    "Incomplete mapping for compound type 'SimpleInheretingRecord': unmapped members: {ll}",
                    ex.getMessage());
        }
        final SimpleRecord recordRead = reader.readCompound("cpd", type);
        assertEquals(recordWritten.getF(), recordRead.getF());
        assertEquals(recordWritten.getI(), recordRead.getI());
        assertEquals(recordWritten.getD(), recordRead.getD());
        assertEquals(recordWritten.getS(), recordRead.getS());
        reader.close();
    }

    @Test
    public void testCompoundMap()
    {
        final File file = new File(workingDirectory, "testCompoundMap.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.configure(file).useUTF8CharacterEncoding().writer();
        final HDF5EnumerationType enumType = writer.getEnumType("someEnumType", new String[]
            { "1", "Two", "THREE" });
        final HDF5CompoundDataMap map = new HDF5CompoundDataMap();
        final float a = 3.14159f;
        map.put("a", a);
        final int[] b = new int[]
            { 17, -1 };
        map.put("b", b);
        final String c = "Teststring\u3453";
        map.put("c", c);
        final HDF5EnumerationValueArray d = new HDF5EnumerationValueArray(enumType, new String[]
            { "Two", "1" });
        map.put("d", d);
        final BitSet e = new BitSet();
        e.set(15);
        map.put("e", e);
        final float[][] f = new float[][]
            {
                { 1.0f, -1.0f },
                { 1e6f, -1e6f } };
        map.put("f", f);
        final MDLongArray g = new MDLongArray(new long[]
            { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[]
            { 2, 2, 2 });
        map.put("g", g);
        final HDF5TimeDuration h = new HDF5TimeDuration(17, HDF5TimeUnit.HOURS);
        map.put("h", h);
        final Date ii = new Date(10000);
        map.put("i", ii);
        writer.writeCompound("cpd", map);
        writer.close();

        final IHDF5Reader reader =
                HDF5Factory.configureForReading(file).useUTF8CharacterEncoding().reader();
        final HDF5CompoundType<HDF5CompoundDataMap> typeRead =
                reader.getDataSetCompoundType("cpd", HDF5CompoundDataMap.class);
        assertEquals("a:b:c:d:e:f:g:h:i", typeRead.getName());
        final HDF5CompoundDataMap mapRead = reader.readCompound("cpd", typeRead);
        assertEquals(9, mapRead.size());
        assertEquals(a, mapRead.get("a"));
        assertTrue(ArrayUtils.toString(mapRead.get("b")), ArrayUtils.isEquals(b, mapRead.get("b")));
        assertEquals(c, mapRead.get("c"));
        final HDF5EnumerationValueArray dRead = (HDF5EnumerationValueArray) mapRead.get("d");
        assertEquals("someEnumType", dRead.getType().getName());
        assertEquals(d.getLength(), dRead.getLength());
        for (int i = 0; i < d.getLength(); ++i)
        {
            assertEquals("enum array idx=" + i, d.getValue(i), dRead.getValue(i));
        }
        assertEquals(e, mapRead.get("e"));
        assertTrue(ArrayUtils.toString(mapRead.get("f")), ArrayUtils.isEquals(f, mapRead.get("f")));
        assertEquals(g, mapRead.get("g"));
        assertEquals(h, mapRead.get("h"));
        assertEquals(ii, mapRead.get("i"));

        final HDF5CompoundType<HDF5CompoundDataMap> typeRead2 =
                reader.getDataSetCompoundType("cpd", HDF5CompoundDataMap.class,
                        new HDF5CompoundMappingHints().enumReturnType(EnumReturnType.STRING));
        final HDF5CompoundDataMap mapRead2 = reader.readCompound("cpd", typeRead2);
        final String[] dRead2 = (String[]) mapRead2.get("d");
        assertEquals(dRead.getLength(), dRead2.length);
        for (int i = 0; i < dRead2.length; ++i)
        {
            assertEquals(dRead.getValue(i), dRead2[i]);
        }

        final HDF5CompoundType<HDF5CompoundDataMap> typeRead3 =
                reader.getDataSetCompoundType("cpd", HDF5CompoundDataMap.class,
                        new HDF5CompoundMappingHints().enumReturnType(EnumReturnType.ORDINAL));
        final HDF5CompoundDataMap mapRead3 = reader.readCompound("cpd", typeRead3);
        final int[] dRead3 = (int[]) mapRead3.get("d");
        assertEquals(dRead.getLength(), dRead3.length);
        for (int i = 0; i < dRead3.length; ++i)
        {
            assertEquals(dRead.getOrdinal(i), dRead3[i]);
        }
        reader.close();
    }

    @Test
    public void testCompoundMapManualMapping()
    {
        final File file = new File(workingDirectory, "testCompoundMapManualMapping.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = writer.getEnumType("someEnumType", new String[]
            { "1", "Two", "THREE" });
        final HDF5CompoundType<HDF5CompoundDataMap> type =
                writer.getCompoundType(
                        "MapCompoundA",
                        HDF5CompoundDataMap.class,
                        new HDF5CompoundMemberMapping[]
                            {
                                    HDF5CompoundMemberMapping.mapping("a").memberClass(float.class),
                                    mapping("b").memberClass(int[].class).length(2),
                                    mapping("c").memberClass(char[].class).length(12),
                                    mapping("d").enumType(enumType).length(2),
                                    mapping("e").memberClass(BitSet.class).length(2),
                                    mapping("f").memberClass(float[][].class).dimensions(2, 2),
                                    mapping("g").memberClass(MDLongArray.class).dimensions(
                                            new int[]
                                                { 2, 2, 2 }),
                                    mapping("h").memberClass(HDF5TimeDuration.class).typeVariant(
                                            HDF5DataTypeVariant.TIME_DURATION_HOURS),
                                    mapping("i").memberClass(Date.class) });
        final HDF5CompoundDataMap map = new HDF5CompoundDataMap();
        final float a = 3.14159f;
        map.put("a", a);
        final int[] b = new int[]
            { 17, -1 };
        map.put("b", b);
        final String c = "Teststring";
        map.put("c", c);
        final HDF5EnumerationValueArray d = new HDF5EnumerationValueArray(enumType, new String[]
            { "Two", "1" });
        map.put("d", d);
        final BitSet e = new BitSet();
        e.set(15);
        map.put("e", e);
        final float[][] f = new float[][]
            {
                { 1.0f, -1.0f },
                { 1e6f, -1e6f } };
        map.put("f", f);
        final MDLongArray g = new MDLongArray(new long[]
            { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[]
            { 2, 2, 2 });
        map.put("g", g);
        final HDF5TimeDuration h = new HDF5TimeDuration(17, HDF5TimeUnit.HOURS);
        map.put("h", h);
        final Date ii = new Date(10000);
        map.put("i", ii);
        writer.writeCompound("cpd", type, map);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5CompoundType<HDF5CompoundDataMap> typeRead =
                reader.getDataSetCompoundType("cpd", HDF5CompoundDataMap.class);
        assertEquals("MapCompoundA", typeRead.getName());
        final HDF5CompoundDataMap mapRead = reader.readCompound("cpd", typeRead);
        assertEquals(9, mapRead.size());
        assertEquals(a, mapRead.get("a"));
        assertTrue(ArrayUtils.toString(mapRead.get("b")), ArrayUtils.isEquals(b, mapRead.get("b")));
        assertEquals(c, mapRead.get("c"));
        final HDF5EnumerationValueArray dRead = (HDF5EnumerationValueArray) mapRead.get("d");
        assertEquals("someEnumType", dRead.getType().getName());
        assertEquals(d.getLength(), dRead.getLength());
        for (int i = 0; i < d.getLength(); ++i)
        {
            assertEquals("enum array idx=" + i, d.getValue(i), dRead.getValue(i));
        }
        assertEquals(e, mapRead.get("e"));
        assertTrue(ArrayUtils.toString(mapRead.get("f")), ArrayUtils.isEquals(f, mapRead.get("f")));
        assertEquals(g, mapRead.get("g"));
        assertEquals(h, mapRead.get("h"));
        assertEquals(ii, mapRead.get("i"));
        reader.close();
    }

    @Test
    public void testCompoundMapManualMappingWithConversion()
    {
        final File file =
                new File(workingDirectory, "testCompoundMapManualMappingWithConversion.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = writer.getEnumType("someEnumType", new String[]
            { "1", "Two", "THREE" });
        final HDF5CompoundType<HDF5CompoundDataMap> type =
                writer.getCompoundType(
                        "MapCompoundA",
                        HDF5CompoundDataMap.class,
                        new HDF5CompoundMemberMapping[]
                            {
                                    HDF5CompoundMemberMapping.mapping("a").memberClass(float.class),
                                    mapping("b").memberClass(short.class),
                                    mapping("c").memberClass(Date.class),
                                    mapping("d").enumType(enumType).length(2),
                                    mapping("e").memberClass(double.class),
                                    mapping("f").memberClass(HDF5TimeDuration.class).typeVariant(
                                            HDF5DataTypeVariant.TIME_DURATION_HOURS) });
        final HDF5CompoundDataMap map = new HDF5CompoundDataMap();
        final double a = 3.14159;
        map.put("a", a);
        final int b = 17;
        map.put("b", b);
        final long c = System.currentTimeMillis();
        map.put("c", c);
        final int[] d = new int[]
            { 1, 0 };
        map.put("d", d);
        final long e = 187493613;
        map.put("e", e);
        final short f = 12;
        map.put("f", f);
        writer.writeCompound("cpd", type, map);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5CompoundType<HDF5CompoundDataMap> typeRead =
                reader.getDataSetCompoundType("cpd", HDF5CompoundDataMap.class);
        assertEquals("MapCompoundA", typeRead.getName());
        final HDF5CompoundDataMap mapRead = reader.readCompound("cpd", typeRead);
        assertEquals(map.size(), mapRead.size());
        assertEquals((float) a, mapRead.get("a"));
        assertEquals((short) b, mapRead.get("b"));
        assertEquals(new Date(c), mapRead.get("c"));
        final HDF5EnumerationValueArray dRead = (HDF5EnumerationValueArray) mapRead.get("d");
        assertEquals("someEnumType", dRead.getType().getName());
        assertEquals(d.length, dRead.getLength());
        for (int i = 0; i < d.length; ++i)
        {
            assertEquals("enum array idx=" + i, d[i], dRead.getOrdinal(i));
        }
        assertEquals((double) e, mapRead.get("e"));
        assertEquals(new HDF5TimeDuration(f, HDF5TimeUnit.HOURS), mapRead.get("f"));
        reader.close();
    }

    @Test
    public void testCompoundManualMapping()
    {
        final File file = new File(workingDirectory, "compoundManualMapping.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<Record> compoundType = Record.getHDF5Type(writer);
        HDF5EnumerationType enumType = writer.getEnumType("someEnumType");
        final Record recordWritten =
                new Record(1, 2.0f, 100000000L, 3.0, (short) 4, true, "one",
                        new HDF5EnumerationValue(enumType, "THREE"), new int[]
                            { 1, 2, 3 }, new float[]
                            { 8.0f, -17.0f }, new long[]
                            { -10, -11, -12 }, new double[]
                            { 3.14159 }, new short[]
                            { 1000, 2000 }, new byte[]
                            { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                            {
                                { 1, 2 },
                                { 3, 4 } }), new char[]
                            { 'A', 'b', 'C' });
        writer.writeCompound("/testCompound", compoundType, recordWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5CompoundMemberInformation[] memMemberInfo =
                Record.getMemberInfo(reader.getEnumType("someEnumType"));
        final HDF5CompoundMemberInformation[] diskMemberInfo =
                reader.getCompoundDataSetInformation("/testCompound", DataTypeInfoOptions.ALL);
        assertEquals(memMemberInfo.length, diskMemberInfo.length);
        Arrays.sort(memMemberInfo);
        Arrays.sort(diskMemberInfo);
        for (int i = 0; i < memMemberInfo.length; ++i)
        {
            assertEquals(memMemberInfo[i], diskMemberInfo[i]);
        }
        compoundType = Record.getHDF5Type(reader);
        final Record recordRead = reader.readCompound("/testCompound", Record.getHDF5Type(reader));
        assertEquals(recordWritten, recordRead);
        reader.close();
    }

    @Test
    public void testCompoundMapArray()
    {
        final File file = new File(workingDirectory, "testCompoundMapArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final HDF5CompoundDataMap map1 = new HDF5CompoundDataMap();
        final float a1 = 3.14159f;
        map1.put("a", a1);
        final HDF5CompoundDataMap map2 = new HDF5CompoundDataMap();
        final float a2 = 18.32f;
        map2.put("a", a2);
        final HDF5CompoundDataMap map3 = new HDF5CompoundDataMap();
        final float a3 = 1.546e5f;
        map3.put("a", a3);
        writer.writeCompoundArray("cpd", new HDF5CompoundDataMap[]
            { map1, map2, map3 });
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final HDF5CompoundDataMap[] maps =
                reader.readCompoundArray("cpd", HDF5CompoundDataMap.class);
        assertEquals(3, maps.length);
        assertEquals(map1, maps[0]);
        assertEquals(map2, maps[1]);
        assertEquals(map3, maps[2]);
        reader.close();
    }

    @Test
    public void testCompoundMapMDArray()
    {
        final File file = new File(workingDirectory, "testCompoundMapMDArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        final HDF5CompoundDataMap map1 = new HDF5CompoundDataMap();
        final float a1 = 3.14159f;
        map1.put("a", a1);
        final HDF5CompoundDataMap map2 = new HDF5CompoundDataMap();
        final float a2 = 18.32f;
        map2.put("a", a2);
        final HDF5CompoundDataMap map3 = new HDF5CompoundDataMap();
        final float a3 = 1.546e5f;
        map3.put("a", a3);
        final HDF5CompoundDataMap map4 = new HDF5CompoundDataMap();
        final float a4 = -3.2f;
        map4.put("a", a4);
        writer.writeCompoundMDArray("cpd", new MDArray<HDF5CompoundDataMap>(
                new HDF5CompoundDataMap[]
                    { map1, map2, map3, map4 }, new int[]
                    { 2, 2 }));
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final MDArray<HDF5CompoundDataMap> maps =
                reader.readCompoundMDArray("cpd", HDF5CompoundDataMap.class);
        assertTrue(ArrayUtils.isEquals(new int[]
            { 2, 2 }, maps.dimensions()));
        assertEquals(map1, maps.get(0, 0));
        assertEquals(map2, maps.get(0, 1));
        assertEquals(map3, maps.get(1, 0));
        assertEquals(map4, maps.get(1, 1));
        reader.close();
    }

    static class DateRecord
    {
        Date d;

        DateRecord()
        {
        }

        DateRecord(Date d)
        {
            this.d = d;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((d == null) ? 0 : d.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DateRecord other = (DateRecord) obj;
            if (d == null)
            {
                if (other.d != null)
                    return false;
            } else if (!d.equals(other.d))
                return false;
            return true;
        }

    }

    @Test
    public void testDateCompound()
    {
        final File file = new File(workingDirectory, "compoundWithDate.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<DateRecord> compoundType =
                writer.getCompoundType(DateRecord.class, new HDF5CompoundMemberMapping[]
                    { mapping("d") });
        final DateRecord recordWritten = new DateRecord(new Date());
        final String objectPath = "/testDateCompound";
        writer.writeCompound(objectPath, compoundType, recordWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5CompoundMemberInformation[] memMemberInfo =
                HDF5CompoundMemberInformation.create(DateRecord.class, mapping("d"));
        final HDF5CompoundMemberInformation[] diskMemberInfo =
                HDF5CompoundMemberInformation.create(DateRecord.class,
                        new HDF5CompoundMemberMapping[]
                            { mapping("d") });
        assertEquals(memMemberInfo.length, diskMemberInfo.length);
        for (int i = 0; i < memMemberInfo.length; ++i)
        {
            assertEquals(memMemberInfo[i], diskMemberInfo[i]);
        }
        compoundType = reader.getCompoundType(DateRecord.class, new HDF5CompoundMemberMapping[]
            { mapping("d") });
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                compoundType.getObjectByteifyer().getByteifyers()[0].getTypeVariant());
        final DateRecord recordRead =
                reader.readCompound(objectPath,
                        reader.getCompoundType(DateRecord.class, mapping("d")));
        assertEquals(recordWritten, recordRead);
        HDF5CompoundType<HDF5CompoundDataMap> mapCompoundType =
                reader.getDataSetCompoundType(objectPath, HDF5CompoundDataMap.class);
        assertEquals(HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                mapCompoundType.getObjectByteifyer().getByteifyers()[0].getTypeVariant());
        final HDF5CompoundDataMap mapRead = reader.readCompound(objectPath, mapCompoundType);
        assertEquals(recordWritten.d, mapRead.get("d"));
        reader.close();
    }

    static class MatrixRecord
    {
        byte[][] b;

        short[][] s;

        int[][] i;

        long[][] l;

        float[][] f;

        double[][] d;

        MatrixRecord()
        {
        }

        MatrixRecord(byte[][] b, short[][] s, int[][] i, long[][] l, float[][] f, double[][] d)
        {
            this.b = b;
            this.s = s;
            this.i = i;
            this.l = l;
            this.f = f;
            this.d = d;
        }

        static HDF5CompoundMemberMapping[] getMapping()
        {
            return new HDF5CompoundMemberMapping[]
                { mapping("b").dimensions(1, 2), mapping("s").dimensions(2, 1),
                        mapping("i").dimensions(2, 2), mapping("l").dimensions(3, 2),
                        mapping("f").dimensions(2, 2), mapping("d").dimensions(2, 3) };
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MatrixRecord other = (MatrixRecord) obj;
            if (!HDF5RoundtripTest.equals(b, other.b))
                return false;
            if (!HDF5RoundtripTest.equals(d, other.d))
                return false;
            if (!HDF5RoundtripTest.equals(f, other.f))
                return false;
            if (!HDF5RoundtripTest.equals(i, other.i))
                return false;
            if (!HDF5RoundtripTest.equals(l, other.l))
                return false;
            if (!HDF5RoundtripTest.equals(s, other.s))
                return false;
            return true;
        }

    }

    @Test
    public void testMatrixCompound()
    {
        final File file = new File(workingDirectory, "compoundWithMatrix.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<MatrixRecord> compoundType =
                writer.getCompoundType(MatrixRecord.class, MatrixRecord.getMapping());
        final MatrixRecord recordWritten = new MatrixRecord(new byte[][]
            {
                { 1, 2 } }, new short[][]
            {
                { 1 },
                { 2 } }, new int[][]
            {
                { 1, 2 },
                { 3, 4 } }, new long[][]
            {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 } }, new float[][]
            {
                { 1, 2 },
                { 3, 4 } }, new double[][]
            {
                { 1, 2, 3 },
                { 4, 5, 6 } });
        String name = "/testMatrixCompound";
        writer.writeCompound(name, compoundType, recordWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5CompoundMemberInformation[] memMemberInfo =
                HDF5CompoundMemberInformation.create(MatrixRecord.class, MatrixRecord.getMapping());
        final HDF5CompoundMemberInformation[] diskMemberInfo =
                HDF5CompoundMemberInformation.create(MatrixRecord.class, MatrixRecord.getMapping());
        assertEquals(memMemberInfo.length, diskMemberInfo.length);
        for (int i = 0; i < memMemberInfo.length; ++i)
        {
            assertEquals(memMemberInfo[i], diskMemberInfo[i]);
        }
        compoundType = reader.getCompoundType(MatrixRecord.class, MatrixRecord.getMapping());
        final MatrixRecord recordRead =
                reader.readCompound(name,
                        reader.getCompoundType(MatrixRecord.class, MatrixRecord.getMapping()));
        assertEquals(recordWritten, recordRead);
        reader.close();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMatrixCompoundSizeMismatch()
    {
        final File file = new File(workingDirectory, "compoundWithSizeMismatchMatrix.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<MatrixRecord> compoundType =
                writer.getCompoundType(MatrixRecord.class, MatrixRecord.getMapping());
        final MatrixRecord recordWritten = new MatrixRecord(new byte[][]
            {
                { 1, 2 } }, new short[][]
            {
                { 1 },
                { 2 } }, new int[][]
            {
                { 1, 2 },
                { 3, 4 } }, new long[][]
            {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 } }, new float[][]
            {
                { 1, 2 },
                { 3, 4 } }, new double[][]
            {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 },
                { 9, 10, 11, 12, 13 } });
        String name = "/testMatrixCompound";
        writer.writeCompound(name, compoundType, recordWritten);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMatrixCompoundDifferentNumberOfColumnsPerRow()
    {
        final File file =
                new File(workingDirectory, "compoundWithMatrixDifferentNumberOfColumnsPerRow.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<MatrixRecord> compoundType =
                writer.getCompoundType(MatrixRecord.class, MatrixRecord.getMapping());
        final MatrixRecord recordWritten = new MatrixRecord(new byte[][]
            {
                { 1, 2 } }, new short[][]
            {
                { 1 },
                { 2 } }, new int[][]
            {
                { 1, 2 },
                { 3, 4 } }, new long[][]
            {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 } }, new float[][]
            {
                { 1, 2 },
                { 3, 4 } }, new double[][]
            {
                { 1, 2, 3 },
                { 4, 5 } });
        String name = "/testMatrixCompound";
        writer.writeCompound(name, compoundType, recordWritten);
    }

    private static boolean equals(double[][] a, double[][] a2)
    {
        if (a == a2)
        {
            return true;
        }
        if (a == null || a2 == null)
        {
            return false;
        }

        int rows = a.length;
        if (a2.length != rows)
        {
            return false;
        }

        for (int i = 0; i < rows; i++)
        {
            int columns = a[i].length;
            if (a2[i].length != columns)
            {
                return false;
            }
            for (int j = 0; j < columns; j++)
            {
                if (Double.doubleToLongBits(a[i][j]) != Double.doubleToLongBits(a2[i][j]))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean equals(byte[][] a, byte[][] a2)
    {
        if (a == a2)
        {
            return true;
        }
        if (a == null || a2 == null)
        {
            return false;
        }

        int rows = a.length;
        if (a2.length != rows)
        {
            return false;
        }

        for (int i = 0; i < rows; i++)
        {
            int columns = a[i].length;
            if (a2[i].length != columns)
            {
                return false;
            }
            for (int j = 0; j < columns; j++)
            {
                if (a[i][j] != a2[i][j])
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean equals(short[][] a, short[][] a2)
    {
        if (a == a2)
        {
            return true;
        }
        if (a == null || a2 == null)
        {
            return false;
        }

        int rows = a.length;
        if (a2.length != rows)
        {
            return false;
        }

        for (int i = 0; i < rows; i++)
        {
            int columns = a[i].length;
            if (a2[i].length != columns)
            {
                return false;
            }
            for (int j = 0; j < columns; j++)
            {
                if (a[i][j] != a2[i][j])
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean equals(int[][] a, int[][] a2)
    {
        if (a == a2)
        {
            return true;
        }
        if (a == null || a2 == null)
        {
            return false;
        }

        int rows = a.length;
        if (a2.length != rows)
        {
            return false;
        }

        for (int i = 0; i < rows; i++)
        {
            int columns = a[i].length;
            if (a2[i].length != columns)
            {
                return false;
            }
            for (int j = 0; j < columns; j++)
            {
                if (a[i][j] != a2[i][j])
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean equals(long[][] a, long[][] a2)
    {
        if (a == a2)
        {
            return true;
        }
        if (a == null || a2 == null)
        {
            return false;
        }

        int rows = a.length;
        if (a2.length != rows)
        {
            return false;
        }

        for (int i = 0; i < rows; i++)
        {
            int columns = a[i].length;
            if (a2[i].length != columns)
            {
                return false;
            }
            for (int j = 0; j < columns; j++)
            {
                if (a[i][j] != a2[i][j])
                {
                    return false;
                }
            }
        }

        return true;
    }

    @Test
    public void testCompoundOverflow()
    {
        final File file = new File(workingDirectory, "compoundOverflow.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<Record> compoundType = Record.getHDF5Type(writer);
        HDF5EnumerationType enumType = writer.getEnumType("someEnumType");
        final Record recordWritten =
                new Record(1, 2.0f, 100000000L, 3.0, (short) 4, true, "one",
                        new HDF5EnumerationValue(enumType, "THREE"), new int[]
                            { 1, 2, 3 }, new float[]
                            { 8.0f, -17.0f }, new long[]
                            { -10, -11, -12 }, new double[]
                            { 3.14159 }, new short[]
                            { 1000, 2000 }, new byte[]
                            { 11, 12, 13, 14, 0, 0, 0 }, new MDIntArray(new int[][]
                            {
                                { 5, 6 },
                                { 7, 8 } }), new char[]
                            { 'A', 'b', 'C' });
        try
        {
            writer.writeCompound("/testCompound", compoundType, recordWritten);
            fail("Failed to detect overflow.");
        } catch (HDF5JavaException ex)
        {
            if (ex.getMessage().contains("must not exceed 4 bytes") == false)
            {
                throw ex;
            }
            // Expected.
        } finally
        {
            writer.close();
        }
    }

    static class BitFieldRecord
    {
        BitSet bs;

        BitFieldRecord(BitSet bs)
        {
            this.bs = bs;
        }

        BitFieldRecord()
        {
        }

        static HDF5CompoundMemberInformation[] getMemberInfo()
        {
            return HDF5CompoundMemberInformation.create(BitFieldRecord.class,
                    mapping("bs").length(100));
        }

        static HDF5CompoundType<BitFieldRecord> getHDF5Type(IHDF5Reader reader)
        {
            return reader.getCompoundType(BitFieldRecord.class, mapping("bs").length(100));
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof BitFieldRecord == false)
            {
                return false;
            }
            final BitFieldRecord that = (BitFieldRecord) obj;
            return this.bs.equals(that.bs);
        }

        @Override
        public int hashCode()
        {
            return bs.hashCode();
        }
    }

    @Test
    public void testBitFieldCompound()
    {
        final File file = new File(workingDirectory, "compoundWithBitField.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<BitFieldRecord> compoundType = BitFieldRecord.getHDF5Type(writer);
        final BitSet bs = new BitSet();
        bs.set(39);
        bs.set(100);
        final BitFieldRecord recordWritten = new BitFieldRecord(bs);
        writer.writeCompound("/testCompound", compoundType, recordWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final HDF5CompoundMemberInformation[] memMemberInfo = BitFieldRecord.getMemberInfo();
        final HDF5CompoundMemberInformation[] diskMemberInfo =
                reader.getCompoundDataSetInformation("/testCompound");
        assertEquals(memMemberInfo.length, diskMemberInfo.length);
        for (int i = 0; i < memMemberInfo.length; ++i)
        {
            assertEquals(memMemberInfo[i], diskMemberInfo[i]);
        }
        compoundType = BitFieldRecord.getHDF5Type(reader);
        final BitFieldRecord recordRead = reader.readCompound("/testCompound", compoundType);
        assertEquals(recordWritten, recordRead);
        reader.close();
    }

    @Test
    public void testCompoundArray()
    {
        final File file = new File(workingDirectory, "compoundArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<Record> compoundType = Record.getHDF5Type(writer);
        HDF5EnumerationType enumType = writer.getEnumType("someEnumType", new String[]
            { "1", "Two", "THREE" }, false);
        Record[] arrayWritten =
                new Record[]
                    {
                            new Record(1, 2.0f, 100000000L, 3.0, (short) 4, true, "one",
                                    new HDF5EnumerationValue(enumType, "THREE"), new int[]
                                        { 1, 2, 3 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 1, 2 },
                                            { 3, 4 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(2, 3.0f, 100000000L, 4.0, (short) 5, false, "two",
                                    new HDF5EnumerationValue(enumType, "1"), new int[]
                                        { 4, 5, 6 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 5, 6 },
                                            { 7, 8 } }), new char[]
                                        { 'A', 'b', 'C' }), };
        writer.writeCompoundArray("/testCompound", compoundType, arrayWritten,
                HDF5GenericStorageFeatures.GENERIC_COMPACT);
        HDF5CompoundType<Record> inferredType = writer.getNamedCompoundType(Record.class);
        // Write again, this time with inferred type.
        writer.writeCompoundArray("/testCompound", inferredType, arrayWritten,
                HDF5GenericStorageFeatures.GENERIC_COMPACT);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        compoundType = Record.getHDF5Type(reader);
        inferredType = reader.getDataSetCompoundType("/testCompound", Record.class);
        Record[] arrayRead = reader.readCompoundArray("/testCompound", inferredType);
        Record firstElementRead = reader.readCompound("/testCompound", compoundType);
        assertEquals(arrayRead[0], firstElementRead);
        for (int i = 0; i < arrayRead.length; ++i)
        {
            assertEquals("" + i, arrayWritten[i], arrayRead[i]);
        }
        reader.close();
    }

    @Test
    public void testCompoundArrayBlockWise()
    {
        final File file = new File(workingDirectory, "compoundVectorBlockWise.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<Record> compoundType = Record.getHDF5Type(writer);
        HDF5EnumerationType enumType = writer.getEnumType("someEnumType");
        writer.createCompoundArray("/testCompound", compoundType, 6, 3);
        Record[] arrayWritten1 =
                new Record[]
                    {
                            new Record(1, 2.0f, 100000000L, 3.0, (short) 4, true, "one",
                                    new HDF5EnumerationValue(enumType, "THREE"), new int[]
                                        { 1, 2, 3 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 1, 2 },
                                            { 3, 4 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(2, 3.0f, 100000000L, 4.0, (short) 5, false, "two",
                                    new HDF5EnumerationValue(enumType, "1"), new int[]
                                        { 4, 5, 6 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 1, 2 },
                                            { 3, 4 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(3, 3.0f, 100000000L, 5.0, (short) 6, true, "two",
                                    new HDF5EnumerationValue(enumType, "Two"), new int[]
                                        { -1, -2, -3 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 1, 2 },
                                            { 3, 4 } }), new char[]
                                        { 'A', 'b', 'C' }), };
        Record[] arrayWritten2 =
                new Record[]
                    {
                            new Record(4, 4.0f, 100000000L, 6.0, (short) 7, false, "two",
                                    new HDF5EnumerationValue(enumType, "Two"), new int[]
                                        { 100, 200, 300 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(5, 5.0f, 100000000L, 7.0, (short) 8, true, "two",
                                    new HDF5EnumerationValue(enumType, "THREE"), new int[]
                                        { 400, 500, 600 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(6, 6.0f, 100000000L, 8.0, (short) 9, false, "x",
                                    new HDF5EnumerationValue(enumType, "1"), new int[]
                                        { -100, -200, -300 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }), };
        writer.writeCompoundArrayBlock("/testCompound", compoundType, arrayWritten1, 0);
        writer.writeCompoundArrayBlock("/testCompound", compoundType, arrayWritten2, 1);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        compoundType = Record.getHDF5Type(reader);
        Record[] arrayRead = reader.readCompoundArrayBlock("/testCompound", compoundType, 3, 0);
        for (int i = 0; i < arrayRead.length; ++i)
        {
            assertEquals("" + i, arrayWritten1[i], arrayRead[i]);
        }
        arrayRead = reader.readCompoundArrayBlock("/testCompound", compoundType, 3, 1);
        for (int i = 0; i < arrayRead.length; ++i)
        {
            assertEquals("" + i, arrayWritten2[i], arrayRead[i]);
        }
        arrayRead = reader.readCompoundArrayBlockWithOffset("/testCompound", compoundType, 3, 1);
        for (int i = 1; i < arrayRead.length; ++i)
        {
            assertEquals("" + i, arrayWritten1[i], arrayRead[i - 1]);
        }
        assertEquals("" + (arrayRead.length - 1), arrayWritten2[0], arrayRead[arrayRead.length - 1]);
        reader.close();
    }

    @Test
    public void testCompoundMDArray()
    {
        final File file = new File(workingDirectory, "compoundMDArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(file);
        writer.writeCompoundMDArray("cpd", new MDArray<SimpleRecord>(new SimpleRecord[]
            { createSR(1), createSR(2), createSR(3), createSR(4), createSR(5), createSR(6) },
                new int[]
                    { 2, 3 }));
        writer.close();

        final IHDF5Reader reader = HDF5Factory.openForReading(file);
        final MDArray<SimpleRecord> records = reader.readCompoundMDArray("cpd", SimpleRecord.class);
        assertEquals(6, records.size());
        assertTrue(ArrayUtils.isEquals(new int[]
            { 2, 3 }, records.dimensions()));
        assertEquals(createSR(1), records.get(0, 0));
        assertEquals(createSR(2), records.get(0, 1));
        assertEquals(createSR(3), records.get(0, 2));
        assertEquals(createSR(4), records.get(1, 0));
        assertEquals(createSR(5), records.get(1, 1));
        assertEquals(createSR(6), records.get(1, 2));
        reader.close();
    }

    private static SimpleRecord createSR(int i)
    {
        return new SimpleRecord(i, i, (short) i, Integer.toString(i));
    }

    @Test
    public void testCompoundMDArrayManualMapping()
    {
        final File file = new File(workingDirectory, "compoundMDArrayManualMapping.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<Record> compoundType = Record.getHDF5Type(writer);
        HDF5EnumerationType enumType = writer.getEnumType("someEnumType");
        final Record[] arrayWritten =
                new Record[]
                    {
                            new Record(1, 2.0f, 100000000L, 3.0, (short) 4, true, "one",
                                    new HDF5EnumerationValue(enumType, "THREE"), new int[]
                                        { 1, 2, 3 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(2, 3.0f, 100000000L, 4.0, (short) 5, false, "two",
                                    new HDF5EnumerationValue(enumType, "1"), new int[]
                                        { 4, 5, 6 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(3, 3.0f, 100000000L, 5.0, (short) 6, true, "two",
                                    new HDF5EnumerationValue(enumType, "Two"), new int[]
                                        { 7, 8, 9 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(4, 4.0f, 100000000L, 6.0, (short) 7, false, "two",
                                    new HDF5EnumerationValue(enumType, "Two"), new int[]
                                        { 10, 11, 12 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }), };
        final MDArray<Record> mdArrayWritten = new MDArray<Record>(arrayWritten, new int[]
            { 2, 2 });
        writer.writeCompoundMDArray("/testCompound", compoundType, mdArrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        compoundType = Record.getHDF5Type(reader);
        final MDArray<Record> mdArrayRead =
                reader.readCompoundMDArray("/testCompound", compoundType);
        assertEquals(mdArrayWritten, mdArrayRead);
        reader.close();
    }

    @Test
    public void testCompoundMDArrayBlockWise()
    {
        final File file = new File(workingDirectory, "compoundMDArrayBlockWise.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<Record> compoundType = Record.getHDF5Type(writer);
        HDF5EnumerationType enumType = writer.getEnumType("someEnumType");
        writer.createCompoundMDArray("/testCompound", compoundType, new long[]
            { 2, 2 }, new int[]
            { 2, 1 });
        final Record[] arrayWritten1 =
                new Record[]
                    {
                            new Record(1, 2.0f, 100000000L, 3.0, (short) 4, true, "one",
                                    new HDF5EnumerationValue(enumType, "THREE"), new int[]
                                        { 1, 2, 3 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(2, 3.0f, 100000000L, 4.0, (short) 5, false, "two",
                                    new HDF5EnumerationValue(enumType, "1"), new int[]
                                        { 2, 3, 4 }, new float[]
                                        { 8.1f, -17.1f }, new long[]
                                        { -10, -13, -12 }, new double[]
                                        { 3.1415 }, new short[]
                                        { 1000, 2001 }, new byte[]
                                        { 11, 12, 13, 17 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }), };
        final Record[] arrayWritten2 =
                new Record[]
                    {
                            new Record(3, 3.0f, 100000000L, 5.0, (short) 6, true, "two",
                                    new HDF5EnumerationValue(enumType, "Two"), new int[]
                                        { 3, 4, 5 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }),
                            new Record(4, 4.0f, 100000000L, 6.0, (short) 7, false, "two",
                                    new HDF5EnumerationValue(enumType, "Two"), new int[]
                                        { 4, 5, 6 }, new float[]
                                        { 8.0f, -17.0f }, new long[]
                                        { -10, -11, -12 }, new double[]
                                        { 3.14159 }, new short[]
                                        { 1000, 2000 }, new byte[]
                                        { 11, 12, 13, 14 }, new MDIntArray(new int[][]
                                        {
                                            { 6, 7 },
                                            { 8, 9 } }), new char[]
                                        { 'A', 'b', 'C' }), };
        final MDArray<Record> mdArrayWritten1 = new MDArray<Record>(arrayWritten1, new int[]
            { 2, 1 });
        final MDArray<Record> mdArrayWritten2 = new MDArray<Record>(arrayWritten2, new int[]
            { 2, 1 });
        writer.writeCompoundMDArrayBlock("/testCompound", compoundType, mdArrayWritten1, new long[]
            { 0, 0 });
        writer.writeCompoundMDArrayBlock("/testCompound", compoundType, mdArrayWritten2, new long[]
            { 0, 1 });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        compoundType = Record.getHDF5Type(reader);
        final MDArray<Record> mdArrayRead1 =
                reader.readCompoundMDArrayBlock("/testCompound", compoundType, new int[]
                    { 2, 1 }, new long[]
                    { 0, 0 });
        final MDArray<Record> mdArrayRead2 =
                reader.readCompoundMDArrayBlock("/testCompound", compoundType, new int[]
                    { 2, 1 }, new long[]
                    { 0, 1 });
        assertEquals(mdArrayWritten1, mdArrayRead1);
        assertEquals(mdArrayWritten2, mdArrayRead2);
        reader.close();
    }

    static class RecordA
    {
        int a;

        double b;

        RecordA(int a, float b)
        {
            this.a = a;
            this.b = b;
        }

        RecordA()
        {
        }

        static HDF5CompoundType<RecordA> getHDF5Type(IHDF5Reader reader)
        {
            return reader.getCompoundType(RecordA.class, mapping("a"), mapping("b"));
        }
    }

    static class RecordB
    {
        float a;

        long b;

        RecordB(float a, int b)
        {
            this.a = a;
            this.b = b;
        }

        RecordB()
        {
        }

        static HDF5CompoundType<RecordB> getHDF5Type(IHDF5Reader reader)
        {
            return reader.getCompoundType(RecordB.class, mapping("a"), mapping("b"));
        }
    }

    static class RecordC
    {
        float a;

        RecordC(float a)
        {
            this.a = a;
        }

        RecordC()
        {
        }

    }

    static class RecordD
    {
        @CompoundElement(memberName = "a")
        float b;

        RecordD(float b)
        {
            this.b = b;
        }

        RecordD()
        {
        }

    }

    static class MatrixElementRecord
    {
        int row;

        int col;

        MatrixElementRecord()
        {
        }

        MatrixElementRecord(int row, int col)
        {
            this.row = row;
            this.col = col;
        }

        boolean equals(@SuppressWarnings("hiding")
        int row, @SuppressWarnings("hiding")
        int col)
        {
            return this.row == row && this.col == col;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof MatrixElementRecord == false)
            {
                return false;
            }
            final MatrixElementRecord m = (MatrixElementRecord) o;
            return equals(m.row, m.col);
        }

        @Override
        public String toString()
        {
            return "(" + row + "," + col + ")";
        }
    }

    @Test
    public void testIterateOverMDCompoundArrayInNaturalBlocks()
    {
        final File datasetFile =
                new File(workingDirectory, "iterateOverMDCompoundArrayInNaturalBlocks.h5");
        datasetFile.delete();
        assertFalse(datasetFile.exists());
        datasetFile.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(datasetFile);
        final String dsName = "ds";
        final HDF5CompoundType<MatrixElementRecord> typeW =
                writer.getInferredCompoundType(MatrixElementRecord.class);
        writer.createCompoundMDArray(dsName, typeW, new long[]
            { 4, 4 }, new int[]
            { 2, 2 });
        writer.writeCompoundMDArrayBlock(dsName, typeW, new MDArray<MatrixElementRecord>(
                new MatrixElementRecord[]
                    { new MatrixElementRecord(1, 1), new MatrixElementRecord(1, 2),
                            new MatrixElementRecord(2, 1), new MatrixElementRecord(2, 2) },
                new int[]
                    { 2, 2 }), new long[]
            { 0, 0 });
        writer.writeCompoundMDArrayBlock(dsName, typeW, new MDArray<MatrixElementRecord>(
                new MatrixElementRecord[]
                    { new MatrixElementRecord(3, 1), new MatrixElementRecord(3, 2),
                            new MatrixElementRecord(4, 1), new MatrixElementRecord(4, 2) },
                new int[]
                    { 2, 2 }), new long[]
            { 1, 0 });
        writer.writeCompoundMDArrayBlock(dsName, typeW, new MDArray<MatrixElementRecord>(
                new MatrixElementRecord[]
                    { new MatrixElementRecord(1, 3), new MatrixElementRecord(1, 4),
                            new MatrixElementRecord(2, 3), new MatrixElementRecord(2, 4) },
                new int[]
                    { 2, 2 }), new long[]
            { 0, 1 });
        writer.writeCompoundMDArrayBlock(dsName, typeW, new MDArray<MatrixElementRecord>(
                new MatrixElementRecord[]
                    { new MatrixElementRecord(3, 3), new MatrixElementRecord(3, 4),
                            new MatrixElementRecord(4, 3), new MatrixElementRecord(4, 4) },
                new int[]
                    { 2, 2 }), new long[]
            { 1, 1 });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(datasetFile);
        int i = 0;
        int j = 0;
        final HDF5CompoundType<MatrixElementRecord> typeR =
                reader.getInferredCompoundType(MatrixElementRecord.class);
        for (HDF5MDDataBlock<MDArray<MatrixElementRecord>> block : reader
                .getCompoundMDArrayNaturalBlocks(dsName, typeR))
        {
            final String ij = new MatrixElementRecord(i, j).toString() + ": ";
            assertTrue(ij + Arrays.toString(block.getIndex()), Arrays.equals(new long[]
                { i, j }, block.getIndex()));
            assertTrue(ij + Arrays.toString(block.getData().dimensions()), Arrays.equals(new int[]
                { 2, 2 }, block.getData().dimensions()));
            assertTrue(ij + Arrays.toString(block.getData().getAsFlatArray()), Arrays.equals(
                    new MatrixElementRecord[]
                        { new MatrixElementRecord(1 + i * 2, 1 + j * 2),
                                new MatrixElementRecord(1 + i * 2, 2 + j * 2),
                                new MatrixElementRecord(2 + i * 2, 1 + j * 2),
                                new MatrixElementRecord(2 + i * 2, 2 + j * 2) }, block.getData()
                            .getAsFlatArray()));
            if (++j > 1)
            {
                j = 0;
                ++i;
            }
        }
        assertEquals(2, i);
        assertEquals(0, j);
        reader.close();
    }

    @Test
    public void testConfusedCompound()
    {
        final File file = new File(workingDirectory, "confusedCompound.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<RecordA> compoundTypeInt = RecordA.getHDF5Type(writer);
        final RecordA recordWritten = new RecordA(17, 42.0f);
        writer.writeCompound("/testCompound", compoundTypeInt, recordWritten);
        writer.close();
        final IHDF5Reader reader =
                HDF5FactoryProvider.get().configureForReading(file).performNumericConversions()
                        .reader();
        HDF5CompoundType<RecordB> compoundTypeFloat = RecordB.getHDF5Type(reader);
        try
        {
            reader.readCompound("/testCompound", compoundTypeFloat);
            fail("Unsuitable data set type not detected.");
        } catch (HDF5JavaException ex)
        {
            assertEquals(
                    "The compound type 'UNKNOWN' is not suitable for data set '/testCompound'.",
                    ex.getMessage());
        }
        reader.close();
    }

    static class SimpleRecord
    {
        private float f;

        private int i;

        @CompoundElement(typeVariant = HDF5DataTypeVariant.TIME_DURATION_SECONDS)
        private short d;

        @CompoundElement(dimensions = 4)
        private String s;

        SimpleRecord()
        {
        }

        SimpleRecord(float f, int i, short d, String s)
        {
            this.f = f;
            this.i = i;
            this.d = d;
            this.s = s;
        }

        public float getF()
        {
            return f;
        }

        public int getI()
        {
            return i;
        }

        public short getD()
        {
            return d;
        }

        public String getS()
        {
            return s;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + d;
            result = prime * result + Float.floatToIntBits(f);
            result = prime * result + i;
            result = prime * result + ((s == null) ? 0 : s.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            SimpleRecord other = (SimpleRecord) obj;
            if (d != other.d)
            {
                return false;
            }
            if (Float.floatToIntBits(f) != Float.floatToIntBits(other.f))
            {
                return false;
            }
            if (i != other.i)
            {
                return false;
            }
            if (s == null)
            {
                if (other.s != null)
                {
                    return false;
                }
            } else if (!s.equals(other.s))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "SimpleRecord [f=" + f + ", i=" + i + ", d=" + d + ", s=" + s + "]";
        }

    }

    static class SimpleInheretingRecord extends SimpleRecord
    {
        SimpleInheretingRecord()
        {
        }

        @CompoundElement(memberName = "ll", dimensions =
            { 2, 3 })
        private long[][] l;

        public SimpleInheretingRecord(float f, int i, short d, String s, long[][] l)
        {
            super(f, i, d, s);
            this.l = l;
        }

        public long[][] getL()
        {
            return l;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Arrays.hashCode(l);
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!super.equals(obj))
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            SimpleInheretingRecord other = (SimpleInheretingRecord) obj;
            if (ArrayUtils.isEquals(l, other.l) == false)
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "SimpleInheretingRecord [l=" + ArrayUtils.toString(l) + ", getF()=" + getF()
                    + ", getI()=" + getI() + ", getD()=" + getD() + ", getS()=" + getS() + "]";
        }
    }

    enum TestEnum
    {
        APPLE, ORANGE, CHERRY
    }

    static class JavaEnumCompoundType
    {
        TestEnum fruit;

        JavaEnumCompoundType()
        {
        }

        JavaEnumCompoundType(TestEnum fruit)
        {
            this.fruit = fruit;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fruit == null) ? 0 : fruit.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            JavaEnumCompoundType other = (JavaEnumCompoundType) obj;
            if (fruit != other.fruit)
            {
                return false;
            }
            return true;
        }
    }

    @Test
    public void testInferredCompoundType()
    {
        final File file = new File(workingDirectory, "inferredCompoundType.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5CompoundType<SimpleRecord> typeW =
                writer.getInferredCompoundType(SimpleRecord.class);
        writer.writeCompound("sc", typeW, new SimpleRecord(2.2f, 17, (short) 10, "test"));
        long[][] arrayWritten = new long[][]
            {
                { 1, 2, 3 },
                { 4, 5, 6 } };
        final HDF5CompoundType<SimpleInheretingRecord> itype =
                writer.getInferredCompoundType(SimpleInheretingRecord.class);
        writer.writeCompound("sci", itype, new SimpleInheretingRecord(-3.1f, 42, (short) 17,
                "some", arrayWritten));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().configureForReading(file).reader();
        final HDF5CompoundType<SimpleRecord> typeR =
                reader.getInferredCompoundType(SimpleRecord.class);
        final SimpleRecord recordRead = reader.readCompound("sc", typeR);
        final HDF5CompoundType<SimpleInheretingRecord> inheritedTypeR =
                reader.getInferredCompoundType(SimpleInheretingRecord.class);
        final SimpleInheretingRecord recordInheritedRead =
                reader.readCompound("sci", inheritedTypeR);
        final HDF5CompoundMemberInformation[] info =
                reader.getCompoundMemberInformation(SimpleRecord.class);
        assertEquals("d", info[2].getName());
        assertEquals(HDF5DataTypeVariant.TIME_DURATION_SECONDS, info[2].getType()
                .tryGetTypeVariant());
        reader.close();

        assertEquals(2.2f, recordRead.getF());
        assertEquals(17, recordRead.getI());
        assertEquals("test", recordRead.getS());

        assertEquals(-3.1f, recordInheritedRead.getF());
        assertEquals(42, recordInheritedRead.getI());
        assertEquals("some", recordInheritedRead.getS());
        assertTrue(equals(arrayWritten, recordInheritedRead.getL()));
    }

    @CompoundType(mapAllFields = false)
    static class IncompleteMappedCompound
    {
        @CompoundElement
        float a;

        @CompoundElement
        int b;

        // unmapped
        String c;

        public IncompleteMappedCompound()
        {
        }

        public IncompleteMappedCompound(float a, int b, String c)
        {
            this.a = a;
            this.b = b;
            this.c = c;
        }

    }

    @Test
    public void testInferredIncompletelyMappedCompoundType()
    {
        final File file = new File(workingDirectory, "inferredIncompletelyMappedCompoundType.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeCompound("cpd", new IncompleteMappedCompound(-1.111f, 11, "Not mapped"));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().configureForReading(file).reader();
        final IncompleteMappedCompound cpd =
                reader.readCompound("cpd", IncompleteMappedCompound.class);
        reader.close();
        assertEquals(-1.111f, cpd.a);
        assertEquals(11, cpd.b);
        assertNull(cpd.c);
    }

    @Test
    public void testNameChangeInCompoundMapping()
    {
        final File file = new File(workingDirectory, "nameChangeInCompoundMapping.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final String typeName = "a_float";
        HDF5CompoundType<RecordC> compoundTypeInt =
                writer.getInferredCompoundType(typeName, RecordC.class);
        final RecordC recordWritten = new RecordC(33.33333f);
        writer.writeCompound("/testCompound", compoundTypeInt, recordWritten);
        writer.close();
        final IHDF5Reader reader =
                HDF5FactoryProvider.get().configureForReading(file).performNumericConversions()
                        .reader();
        HDF5CompoundType<RecordD> compoundTypeFloat =
                reader.getNamedCompoundType(typeName, RecordD.class);
        final RecordD recordRead = reader.readCompound("/testCompound", compoundTypeFloat);
        assertEquals(recordWritten.a, recordRead.b);
        reader.close();
    }

    static class SimpleRecordWithEnum
    {
        HDF5EnumerationValue e;

        SimpleRecordWithEnum()
        {
        }

        public SimpleRecordWithEnum(HDF5EnumerationValue e)
        {
            this.e = e;
        }
    }

    @Test
    public void testInferredCompoundTypedWithEnum()
    {
        final File file = new File(workingDirectory, "inferredCompoundTypeWithEnum.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final String[] alternatives = new String[257];
        for (int i = 0; i < alternatives.length; ++i)
        {
            alternatives[i] = Integer.toString(i);
        }
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final HDF5EnumerationType enumType = writer.getEnumType("type", alternatives);
        final SimpleRecordWithEnum r =
                new SimpleRecordWithEnum(new HDF5EnumerationValue(enumType, "3"));
        final HDF5CompoundType<SimpleRecordWithEnum> typeW = writer.getInferredCompoundType(r);
        writer.writeCompound("sce", typeW, r);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().configureForReading(file).reader();
        final HDF5CompoundType<SimpleRecordWithEnum> typeR =
                reader.getNamedCompoundType(SimpleRecordWithEnum.class);
        final SimpleRecordWithEnum recordRead = reader.readCompound("sce", typeR);
        assertEquals("3", recordRead.e.getValue());
        reader.close();

    }

    static class SimpleRecordWithEnumArray
    {
        @CompoundElement(dimensions = 5)
        HDF5EnumerationValueArray e;

        SimpleRecordWithEnumArray()
        {
        }

        public SimpleRecordWithEnumArray(HDF5EnumerationValueArray e)
        {
            this.e = e;
        }
    }

    @Test
    public void testInferredCompoundTypeWithEnumArray()
    {
        final File file = new File(workingDirectory, "inferredCompoundTypeWithEnumArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final String[] alternatives = new String[512];
        for (int i = 0; i < alternatives.length; ++i)
        {
            alternatives[i] = Integer.toString(i);
        }
        final HDF5EnumerationType enumType = writer.getEnumType("type", alternatives);
        final SimpleRecordWithEnumArray r =
                new SimpleRecordWithEnumArray(new HDF5EnumerationValueArray(enumType, new String[]
                    { "3", "2", "1", "511", "3" }));
        final HDF5CompoundType<SimpleRecordWithEnumArray> cType = writer.getInferredCompoundType(r);
        writer.writeCompound("sce", cType, r);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().configureForReading(file).reader();
        final HDF5CompoundType<SimpleRecordWithEnumArray> typeR =
                reader.getNamedCompoundType(SimpleRecordWithEnumArray.class);
        final SimpleRecordWithEnumArray recordRead = reader.readCompound("sce", typeR);
        reader.close();

        assertEquals(5, recordRead.e.getLength());
        assertEquals("3", recordRead.e.getValue(0));
        assertEquals("2", recordRead.e.getValue(1));
        assertEquals("1", recordRead.e.getValue(2));
        assertEquals("511", recordRead.e.getValue(3));
        assertEquals("3", recordRead.e.getValue(4));
    }

    static class RecordWithMatrix
    {
        String s;

        MDFloatArray fm;

        public RecordWithMatrix()
        {
        }

        RecordWithMatrix(String s, MDFloatArray fm)
        {
            this.s = s;
            this.fm = fm;
        }

        static HDF5CompoundType<RecordWithMatrix> getHDF5Type(IHDF5Reader reader)
        {
            return reader.getCompoundType(null, RecordWithMatrix.class, getMapping());
        }

        private static HDF5CompoundMemberMapping[] getMapping()
        {
            return new HDF5CompoundMemberMapping[]
                { mapping("s").length(5), mapping("fm").dimensions(2, 2) };
        }

    }

    @Test
    public void testMDArrayCompound()
    {
        final File file = new File(workingDirectory, "mdArrayCompound.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<RecordWithMatrix> compoundTypeMatrix =
                RecordWithMatrix.getHDF5Type(writer);
        final RecordWithMatrix recordWritten =
                new RecordWithMatrix("tag", new MDFloatArray(new float[][]
                    {
                        { 1, 2 },
                        { 3, 4 } }));
        writer.writeCompound("/testCompound", compoundTypeMatrix, recordWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        HDF5CompoundType<RecordWithMatrix> compoundTypeMatrixRead =
                RecordWithMatrix.getHDF5Type(reader);
        final RecordWithMatrix recordRead =
                reader.readCompound("/testCompound", compoundTypeMatrixRead);
        assertEquals(recordWritten.s, recordRead.s);
        assertEquals(recordWritten.fm, recordRead.fm);
    }

    @Test
    public void testMDArrayCompoundArray()
    {
        final File file = new File(workingDirectory, "mdArrayCompoundArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        HDF5CompoundType<RecordWithMatrix> compoundTypeMatrix =
                RecordWithMatrix.getHDF5Type(writer);
        final RecordWithMatrix[] recordArrayWritten = new RecordWithMatrix[]
            { new RecordWithMatrix("tag1", new MDFloatArray(new float[][]
                {
                    { 1, 2 },
                    { 3, 4 } })), new RecordWithMatrix("tag2", new MDFloatArray(new float[][]
                {
                    { 10, 20 },
                    { 30, 40 } })), new RecordWithMatrix("tag3", new MDFloatArray(new float[][]
                {
                    { 100, 200 },
                    { 300, 400 } })), };
        writer.writeCompoundArray("/testCompoundArray", compoundTypeMatrix, recordArrayWritten);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        HDF5CompoundType<RecordWithMatrix> compoundTypeMatrixRead =
                RecordWithMatrix.getHDF5Type(reader);
        final RecordWithMatrix[] recordReadArray =
                reader.readCompoundArray("/testCompoundArray", compoundTypeMatrixRead);
        assertEquals(3, recordReadArray.length);
        for (int i = 0; i < recordArrayWritten.length; ++i)
        {
            assertEquals("" + i, recordArrayWritten[i].s, recordReadArray[i].s);
            assertEquals("" + i, recordArrayWritten[i].fm, recordReadArray[i].fm);
        }
    }

    @Test
    public void testSetDataSetSize()
    {
        final File file = new File(workingDirectory, "testSetDataSetSize.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.createByteArray("ds", 0, 10);
        writer.setDataSetSize("ds", 20);
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        assertEquals(20, reader.getDataSetInformation("ds").getSize());
        int idx = 0;
        for (byte b : reader.readByteArray("ds"))
        {
            assertEquals("Position " + (idx++), 0, b);
        }
        reader.close();
    }

    @Test
    public void testNumericConversion()
    {
        final File file = new File(workingDirectory, "numericConversions.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeFloat("pi", 3.14159f);
        writer.writeFloat("INFINITY", Float.POSITIVE_INFINITY);
        writer.writeDouble("DINFINITY", Double.NEGATIVE_INFINITY);
        writer.writeDouble("verySmallFloat", 1e-100);
        writer.writeDouble("veryLargeFloat", 1e+100);
        writer.setDoubleAttribute("pi", "eps", 1e-5);
        writer.writeLong("smallInteger", 17L);
        writer.writeLong("largeInteger", Long.MAX_VALUE);
        writer.close();
        final IHDF5ReaderConfigurator config =
                HDF5FactoryProvider.get().configureForReading(file).performNumericConversions();
        // If this platform doesn't support numeric conversions, the test would fail.
        if (config.platformSupportsNumericConversions() == false)
        {
            return;
        }
        final IHDF5Reader reader = config.reader();
        assertEquals(3.14159, reader.readDouble("pi"), 1e-5);
        assertEquals(3, reader.readInt("pi"));
        assertEquals(1e-5f, reader.getFloatAttribute("pi", "eps"), 1e-9);
        assertEquals(17, reader.readByte("smallInteger"));
        assertEquals(0.0f, reader.readFloat("verySmallFloat"));
        assertEquals(Double.POSITIVE_INFINITY, reader.readDouble("INFINITY"));
        try
        {
            reader.readInt("largeInteger");
            fail("Failed to detect overflow");
        } catch (HDF5DatatypeInterfaceException ex)
        {
            assertEquals(HDF5Constants.H5E_CANTCONVERT, ex.getMinorErrorNumber());
        }
        try
        {
            reader.readFloat("veryLargeFloat");
            fail("Failed to detect overflow");
        } catch (HDF5DatatypeInterfaceException ex)
        {
            assertEquals(HDF5Constants.H5E_CANTCONVERT, ex.getMinorErrorNumber());
        }
        try
        {
            reader.readLong("veryLargeFloat");
            fail("Failed to detect overflow");
        } catch (HDF5DatatypeInterfaceException ex)
        {
            assertEquals(HDF5Constants.H5E_CANTCONVERT, ex.getMinorErrorNumber());
        }
        // On HDF5 1.8.3, numeric conversions on sparc don't detect overflows
        // for INFINITY and DINFINITY values.
        if (OSUtilities.getCPUArchitecture().startsWith("sparc"))
        {
            return;
        }
        try
        {
            reader.readFloat("DINFINITY");
            fail("Failed to detect overflow");
        } catch (HDF5DatatypeInterfaceException ex)
        {
            assertEquals(HDF5Constants.H5E_CANTCONVERT, ex.getMinorErrorNumber());
        }
        try
        {
            reader.readLong("INFINITY");
            fail("Failed to detect overflow");
        } catch (HDF5DatatypeInterfaceException ex)
        {
            assertEquals(HDF5Constants.H5E_CANTCONVERT, ex.getMinorErrorNumber());
        }
        reader.close();
    }

    @Test
    public void testObjectReferenceOverwriteWithKeep()
    {
        final File file = new File(workingDirectory, "testObjectReferenceOverwriteWithKeep.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(file).keepDataSetsIfTheyExist().writer();
        writer.writeString("a", "TestA");
        writer.writeString("aa", "TestAA");
        writer.writeObjectReference("b", "aa");
        writer.delete("a");
        // If keepDataSetsIfTheyExist() was not given above, the dataset would be deleted, the
        // header of the new dataset would be written at the old position of "a" and the object
        // reference "b" would be dangling.
        writer.writeString("aa", "TestX");
        assertEquals("/aa", writer.readObjectReference("/b"));
        writer.move("/aa", "/C");
        assertEquals("/C", writer.readObjectReference("/b"));
        writer.close();
    }

    @Test
    public void testObjectReferenceOverwriteWithKeepOverridden()
    {
        final File file =
                new File(workingDirectory, "testObjectReferenceOverwriteWithKeepOverridden.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer =
                HDF5FactoryProvider.get().configure(file).keepDataSetsIfTheyExist().writer();
        writer.writeString("a", "TestA");
        writer.writeString("aa", "TestAA");
        writer.writeObjectReference("b", "aa");
        writer.delete("a");
        // As we override keepDataSetsIfTheyExist() by
        // HDF5GenericStorageFeatures.GENERIC_COMPACT_DELETE,
        // the dataset will be deleted and the header of the new dataset will be written at the old
        // position of "a", thus the object
        // reference "b" will be dangling.
        writer.writeString("aa", "TestX", HDF5GenericStorageFeatures.GENERIC_COMPACT_DELETE);
        // Check for dangling reference.
        assertEquals("", writer.readObjectReference("/b"));
        writer.close();
    }

    @Test
    public void testObjectReference()
    {
        final File file = new File(workingDirectory, "testObjectReference.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeString("a", "TestA");
        writer.writeObjectReference("b", "a");
        assertEquals("/a", writer.readObjectReference("/b"));
        writer.move("/a", "/C");
        assertEquals("/C", writer.readObjectReference("/b"));
        assertEquals("TestA", writer.readString(writer.readObjectReference("/b", false)));
        writer.close();
    }

    @Test
    public void testObjectReferenceArray()
    {
        final File file = new File(workingDirectory, "testObjectReferenceArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeString("a1", "TestA");
        writer.writeString("a2", "TestB");
        writer.writeString("a3", "TestC");
        writer.writeObjectReferenceArray("b", new String[]
            { "a1", "a2", "a3" });
        assertTrue(ArrayUtils.isEquals(new String[]
            { "/a1", "/a2", "/a3" }, writer.readObjectReferenceArray("/b")));
        writer.move("/a1", "/C");
        assertTrue(ArrayUtils.isEquals(new String[]
            { "/C", "/a2", "/a3" }, writer.readObjectReferenceArray("/b")));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        assertTrue(ArrayUtils.isEquals(new String[]
            { "/C", "/a2", "/a3" }, reader.readObjectReferenceArray("/b")));
        final String[] refs = reader.readObjectReferenceArray("/b", false);
        assertEquals("TestA", reader.readString(refs[0]));
        assertEquals("/C", reader.resolvePath(refs[0]));
        assertEquals("TestB", reader.readString(refs[1]));
        assertEquals("/a2", reader.resolvePath(refs[1]));
        assertEquals("TestC", reader.readString(refs[2]));
        assertEquals("/a3", reader.resolvePath(refs[2]));
        reader.close();
    }

    @Test
    public void testObjectReferenceArrayBlockWise()
    {
        final File file = new File(workingDirectory, "testObjectReferenceArrayBlockWise.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final String[] completeArray = new String[16];
        for (int i = 0; i < completeArray.length; ++i)
        {
            writer.writeString("a" + (i + 1), "TestA" + i);
            completeArray[i] = "/a" + (i + 1);
        }
        writer.createObjectReferenceArray("b", completeArray.length, completeArray.length / 4,
                HDF5IntStorageFeatures.INT_NO_COMPRESSION);
        final String[][] chunk = new String[4][4];
        System.arraycopy(completeArray, 0, chunk[0], 0, 4);
        System.arraycopy(completeArray, 4, chunk[1], 0, 4);
        System.arraycopy(completeArray, 8, chunk[2], 0, 4);
        System.arraycopy(completeArray, 12, chunk[3], 0, 4);
        writer.writeObjectReferenceArrayBlock("b", chunk[0], 0);
        writer.writeObjectReferenceArrayBlock("b", chunk[2], 2);
        writer.writeObjectReferenceArrayBlock("b", chunk[1], 1);
        writer.writeObjectReferenceArrayBlock("b", chunk[3], 3);
        assertTrue(ArrayUtils.isEquals(completeArray, writer.readObjectReferenceArray("/b")));
        writer.move("/a1", "/C");
        completeArray[0] = "/C";
        chunk[0][0] = "/C";
        assertTrue(ArrayUtils.isEquals(completeArray, writer.readObjectReferenceArray("/b")));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        int idx = 0;
        for (HDF5DataBlock<String[]> block : reader.getObjectReferenceArrayNaturalBlocks("b"))
        {
            assertTrue("" + idx, ArrayUtils.isEquals(chunk[idx++], block.getData()));
        }
        reader.close();
    }

    @Test
    public void testObjectReferenceMDArray()
    {
        final File file = new File(workingDirectory, "testObjectReferenceMDArray.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeString("a1", "TestA");
        writer.writeString("a2", "TestA");
        writer.writeString("a3", "TestA");
        writer.writeString("a4", "TestA");
        writer.writeObjectReferenceMDArray("b", new MDArray<String>(new String[]
            { "a1", "a2", "a3", "a4" }, new int[]
            { 2, 2 }));
        assertEquals(new MDArray<String>(new String[]
            { "/a1", "/a2", "/a3", "/a4" }, new int[]
            { 2, 2 }), writer.readObjectReferenceMDArray("/b"));
        writer.move("/a1", "/C");
        assertEquals(new MDArray<String>(new String[]
            { "/C", "/a2", "/a3", "/a4" }, new int[]
            { 2, 2 }), writer.readObjectReferenceMDArray("/b"));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        assertEquals(new MDArray<String>(new String[]
            { "/C", "/a2", "/a3", "/a4" }, new int[]
            { 2, 2 }), reader.readObjectReferenceMDArray("/b"));
        reader.close();
    }

    @Test
    public void testObjectReferenceMDArrayBlockWise()
    {
        final File file = new File(workingDirectory, "testObjectReferenceMDArrayBlockWise.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        final String[] completeArray = new String[16];
        for (int i = 0; i < completeArray.length; ++i)
        {
            writer.writeString("a" + (i + 1), "TestA" + i);
            completeArray[i] = "/a" + (i + 1);
        }
        writer.createObjectReferenceMDArray("b", new long[]
            { 4, 4 }, new int[]
            { 1, 4 }, HDF5IntStorageFeatures.INT_NO_COMPRESSION);
        final String[][] chunk = new String[4][4];
        System.arraycopy(completeArray, 0, chunk[0], 0, 4);
        System.arraycopy(completeArray, 4, chunk[1], 0, 4);
        System.arraycopy(completeArray, 8, chunk[2], 0, 4);
        System.arraycopy(completeArray, 12, chunk[3], 0, 4);
        writer.writeObjectReferenceMDArrayBlock("b", new MDArray<String>(chunk[0], new int[]
            { 1, 4 }), new long[]
            { 0, 0 });
        writer.writeObjectReferenceMDArrayBlock("b", new MDArray<String>(chunk[2], new int[]
            { 1, 4 }), new long[]
            { 2, 0 });
        writer.writeObjectReferenceMDArrayBlock("b", new MDArray<String>(chunk[1], new int[]
            { 1, 4 }), new long[]
            { 1, 0 });
        writer.writeObjectReferenceMDArrayBlock("b", new MDArray<String>(chunk[3], new int[]
            { 1, 4 }), new long[]
            { 3, 0 });
        assertEquals(new MDArray<String>(completeArray, new int[]
            { 4, 4 }), writer.readObjectReferenceMDArray("/b"));
        writer.move("/a1", "/C");
        completeArray[0] = "/C";
        chunk[0][0] = "/C";
        assertEquals(new MDArray<String>(completeArray, new int[]
            { 4, 4 }), writer.readObjectReferenceMDArray("/b"));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        int idx = 0;
        for (HDF5MDDataBlock<MDArray<String>> block : reader
                .getObjectReferenceMDArrayNaturalBlocks("b"))
        {
            assertEquals("" + idx, new MDArray<String>(chunk[idx++], new int[]
                { 1, 4 }), block.getData());
        }
        reader.close();
    }

    @Test
    public void testObjectReferenceAttribute()
    {
        final File file = new File(workingDirectory, "testObjectReferenceAttribute.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeString("a", "TestA");
        writer.writeString("b", "TestB");
        writer.setObjectReferenceAttribute("a", "partner", "b");
        assertEquals("/b", writer.getObjectReferenceAttribute("/a", "partner"));
        writer.move("/b", "/C");
        assertEquals("/C", writer.getObjectReferenceAttribute("/a", "partner"));
        writer.close();
    }

    @Test
    public void testObjectReferenceArrayAttribute()
    {
        final File file = new File(workingDirectory, "testObjectReferenceArrayAttribute.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeString("a1", "TestA1");
        writer.writeString("a2", "TestA2");
        writer.writeString("a3", "TestA3");
        writer.writeString("b", "TestB");
        writer.setObjectReferenceArrayAttribute("b", "partner", new String[]
            { "a1", "a2", "a3" });
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final String[] referencesRead = reader.getObjectReferenceArrayAttribute("b", "partner");
        assertEquals(3, referencesRead.length);
        assertEquals("/a1", referencesRead[0]);
        assertEquals("/a2", referencesRead[1]);
        assertEquals("/a3", referencesRead[2]);
        reader.close();
    }

    @Test
    public void testObjectReferenceMDArrayAttribute()
    {
        final File file = new File(workingDirectory, "testObjectReferenceMDArrayAttribute.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        final IHDF5Writer writer = HDF5FactoryProvider.get().open(file);
        writer.writeString("a1", "TestA1");
        writer.writeString("a2", "TestA2");
        writer.writeString("a3", "TestA3");
        writer.writeString("a4", "TestA4");
        writer.writeString("b", "TestB");
        writer.setObjectReferenceMDArrayAttribute("b", "partner", new MDArray<String>(new String[]
            { "a1", "a2", "a3", "a4" }, new int[]
            { 2, 2 }));
        writer.close();
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(file);
        final MDArray<String> referencesRead =
                reader.getObjectReferenceMDArrayAttribute("b", "partner");
        assertTrue(ArrayUtils.isEquals(new int[]
            { 2, 2 }, referencesRead.dimensions()));
        assertEquals("/a1", referencesRead.get(0, 0));
        assertEquals("/a2", referencesRead.get(0, 1));
        assertEquals("/a3", referencesRead.get(1, 0));
        assertEquals("/a4", referencesRead.get(1, 1));
        reader.close();
    }

    @Test
    public void testHDF5FileDetection() throws IOException
    {
        final File hdf5File = new File(workingDirectory, "testHDF5FileDetection.h5");
        hdf5File.delete();
        assertFalse(hdf5File.exists());
        hdf5File.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(hdf5File);
        writer.writeString("a", "someString");
        writer.close();
        assertTrue(HDF5Factory.isHDF5File(hdf5File));

        final File noHdf5File = new File(workingDirectory, "testHDF5FileDetection.h5");
        noHdf5File.delete();
        assertFalse(noHdf5File.exists());
        noHdf5File.deleteOnExit();
        FileUtils.writeByteArrayToFile(noHdf5File, new byte[]
            { 1, 2, 3, 4 });
        assertFalse(HDF5Factory.isHDF5File(noHdf5File));
    }

    @Test
    public void testHDFJavaLowLevel()
    {
        final File file = new File(workingDirectory, "testHDFJavaLowLevel.h5");
        file.delete();
        assertFalse(file.exists());
        file.deleteOnExit();
        int fileId =
                ncsa.hdf.hdf5lib.H5.H5Fcreate(file.getAbsolutePath(),
                        ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_TRUNC,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT);
        int groupId =
                ncsa.hdf.hdf5lib.H5.H5Gcreate(fileId, "constants",
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT);
        int spcId = ncsa.hdf.hdf5lib.H5.H5Screate(ncsa.hdf.hdf5lib.HDF5Constants.H5S_SCALAR);
        int dsId =
                ncsa.hdf.hdf5lib.H5.H5Dcreate(groupId, "pi",
                        ncsa.hdf.hdf5lib.HDF5Constants.H5T_IEEE_F32LE, spcId,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT);
        ncsa.hdf.hdf5lib.H5.H5Dwrite(dsId, ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_FLOAT,
                ncsa.hdf.hdf5lib.HDF5Constants.H5S_SCALAR,
                ncsa.hdf.hdf5lib.HDF5Constants.H5S_SCALAR,
                ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT, new float[]
                    { 3.14159f });
        ncsa.hdf.hdf5lib.H5.H5Dclose(dsId);
        ncsa.hdf.hdf5lib.H5.H5Sclose(spcId);
        ncsa.hdf.hdf5lib.H5.H5Gclose(groupId);
        ncsa.hdf.hdf5lib.H5.H5Fclose(fileId);

        fileId =
                ncsa.hdf.hdf5lib.H5.H5Fopen(file.getAbsolutePath(),
                        ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_RDONLY,
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT);
        spcId = ncsa.hdf.hdf5lib.H5.H5Screate(ncsa.hdf.hdf5lib.HDF5Constants.H5S_SCALAR);
        dsId =
                ncsa.hdf.hdf5lib.H5.H5Dopen(fileId, "/constants/pi",
                        ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT);
        final float[] data = new float[1];
        ncsa.hdf.hdf5lib.H5.H5Dread(dsId, ncsa.hdf.hdf5lib.HDF5Constants.H5T_NATIVE_FLOAT,
                ncsa.hdf.hdf5lib.HDF5Constants.H5S_ALL, ncsa.hdf.hdf5lib.HDF5Constants.H5S_ALL,
                ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT, data);
        assertEquals(3.14159f, data[0], 0f);

        ncsa.hdf.hdf5lib.H5.H5Dclose(dsId);
        ncsa.hdf.hdf5lib.H5.H5Sclose(spcId);
        ncsa.hdf.hdf5lib.H5.H5Fclose(fileId);
    }
}