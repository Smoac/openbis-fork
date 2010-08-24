/**************************************************************************/
/* Schema Version: 006                                                    */
/* Author: Bernd Rinn, 2010, CISD                                         */
/**************************************************************************/

/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN CODE AS VARCHAR(40);

CREATE DOMAIN DESCRIPTION AS VARCHAR(100);

CREATE DOMAIN REAL AS DOUBLE PRECISION;

/* ---------------------------------------------------------------------- */
/* Add table "DATA_SETS"                                                */
/* ---------------------------------------------------------------------- */

CREATE SEQUENCE DATA_SET_ID_SEQ;

CREATE TABLE DATA_SETS (
    DASE_ID TECH_ID NOT NULL DEFAULT NEXTVAL('DATA_SET_ID_SEQ'),
    CODE CODE NOT NULL,
    UPLOADER_EMAIL DESCRIPTION NOT NULL,
    EXP_CODE CODE NOT NULL,
    EXP_PERM_ID CODE NOT NULL,
    CONSTRAINT DATA_PK PRIMARY KEY (ID),
    CONSTRAINT DATA_BK_UK UNIQUE (CODE)
);


/* ---------------------------------------------------------------------- */
/* Add table "TIME_SERIES"                                                */
/* ---------------------------------------------------------------------- */

CREATE SEQUENCE TIME_SERIES_ID_SEQ;
CREATE SEQUENCE TIME_SERIES_ROW_ID_SEQ;
CREATE SEQUENCE TIME_SERIES_VALUE_GROUP_ID_SEQ;

CREATE TABLE TIME_SERIES (
    TISE_ID TECH_ID NOT NULL DEFAULT NEXTVAL('TIME_SERIES_ID_SEQ'),
    DASE_ID TECH_ID NOT NULL,
    ROW_ID TECH_ID NOT NULL,
    VALUE_GROUP_ID TECH_ID NOT NULL,
    IDENTIFIER CODE NOT NULL,
    IDENTIFIER_TYPE CODE NOT NULL,
    IDENTIFIER_HUMAN_READABLE DESCRIPTION,
    DATA_SET_TYPE CODE NOT NULL,
    EXPERIMENT_TYPE CODE NOT NULL,
    CULTIVATION_METHOD CODE NOT NULL,
    BIOLOGICAL_REPLICATES CODE NOT NULL,
    TECHNICAL_REPLICATES CODE NOT NULL,
    TIME_POINT INTEGER NOT NULL,
    TIME_POINT_TYPE CODE NOT NULL,
    CELL_LOCATION CODE NOT NULL,
    VALUE REAL,
    VALUE_TYPE CODE NOT NULL,
    UNIT CODE NOT NULL,
    SCALE CODE NOT NULL,
    
    CONSTRAINT TISE_PK PRIMARY KEY (TISE_ID),
    CONSTRAINT TISE_DASE_FK FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (DASE_ID)
);

/* ---------------------------------------------------------------------- */
/* Add indices                                                            */
/* ---------------------------------------------------------------------- */

CREATE INDEX TISE_DASE_FK_I ON TIME_SERIES (DASE_ID);
CREATE INDEX TISE_ROW_ID_I ON TIME_SERIES (ROW_ID);
CREATE INDEX TISE_VALUE_GROUP_ID_I ON TIME_SERIES (VALUE_GROUP_ID);
CREATE INDEX TISE_IDENTIFIER_I ON TIME_SERIES (IDENTIFIER);

/* ---------------------------------------------------------------------- */
/* Add table "CHIP_CHIP_DATA"                                             */
/* ---------------------------------------------------------------------- */

CREATE SEQUENCE CHIP_CHIP_ID_SEQ;
CREATE SEQUENCE CHIP_CHIP_ROW_ID_SEQ;
CREATE SEQUENCE CHIP_CHIP_VALUE_GROUP_ID_SEQ;

CREATE TABLE CHIP_CHIP_DATA (
    CHCH_ID TECH_ID NOT NULL DEFAULT NEXTVAL('CHIP_CHIP_DATA_ID_SEQ'),
    DASE_ID TECH_ID NOT NULL,
    ROW_ID TECH_ID NOT NULL,
    VALUE_GROUP_ID TECH_ID NOT NULL,
    BSU_IDENTIFIER CODE NOT NULL,
    GENE_NAME DESCRIPTION,
    GENE_FUNCTION DESCRIPTION,
    ARRAY_DESIGN CODE,
    MICROARRAY_ID INTEGER,
    EXPERIMENT_TYPE CODE NOT NULL,
    CULTIVATION_METHOD CODE NOT NULL,
    BIOLOGICAL_REPLICATES CODE NOT NULL,
    TECHNICAL_REPLICATES CODE NOT NULL,
    CELL_LOCATION CODE NOT NULL,
    CHIP_PEAK_POSITION_VALUE INTEGER NOT NULL,
    GENOME_SOURCE_VERSION CODE NOT NULL,
    CHIP_LOCAL_HEIGHT_VALUE REAL,
    CHIP_SCORE_VALUE REAL,
    SCALE CODE NOT NULL,
    IS_PEAK_INTERGENIC BOOLEAN NOT NULL,
    NEARBY_GENE_NAMES DESCRIPTION,
    NEARBY_BSU_IDS DESCRIPTION,
    DISTANCES_FROM_START,
    
    CONSTRAINT CHCH_PK PRIMARY KEY (CHCH_ID),
    CONSTRAINT CHCH_DASE_FK FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (DASE_ID)
);

