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

package ch.systemsx.cisd.openbis.generic.shared.basic;

/**
 * @author Izabela Adamczyk
 */
public class DataSetUploadInfo
{

    String sample;

    String dataSetType;

    String fileType;

    public DataSetUploadInfo()
    {
    }

    public DataSetUploadInfo(String sample, String dataSetType, String fileType)
    {
        setSample(sample);
        setDataSetType(dataSetType);
        setFileType(fileType);
    }

    public String getSample()
    {
        return sample;
    }

    public void setSample(String sample)
    {
        this.sample = sample;
    }

    public String getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(String dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public static class DataSetUploadInfoHelper
    {
        private static final String SEPARATOR = ",";

        enum CommentElements
        {
            SAMPLE, DATA_SET_TYPE, FILE_TYPE;
        }

        public static String encodeAsCifexComment(DataSetUploadInfo info)
        {
            String[] commentElements = new String[CommentElements.values().length];
            commentElements[CommentElements.SAMPLE.ordinal()] = info.getSample();
            commentElements[CommentElements.DATA_SET_TYPE.ordinal()] = info.getDataSetType();
            commentElements[CommentElements.FILE_TYPE.ordinal()] = info.getFileType();
            String comment = "";
            for (String el : commentElements)
            {
                if (comment.length() != 0)
                {
                    comment += SEPARATOR;
                }
                comment += el;
            }
            return comment;
        }

        public static DataSetUploadInfo extractFromCifexComment(String comment)
        {
            String[] commentElements = comment.split(SEPARATOR);
            DataSetUploadInfo result = new DataSetUploadInfo();
            result.setSample(commentElements[CommentElements.SAMPLE.ordinal()]);
            result.setDataSetType(commentElements[CommentElements.DATA_SET_TYPE.ordinal()]);
            result.setFileType(commentElements[CommentElements.FILE_TYPE.ordinal()]);
            return result;
        }
    }

}
