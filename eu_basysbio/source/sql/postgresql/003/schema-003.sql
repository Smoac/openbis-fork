/* ---------------------------------------------------------------------- */
/* Script generated with: DeZign for Databases v5.2.3                     */
/* Target DBMS:           PostgreSQL 8                                    */
/* Project file:          schema.dez                                      */
/* Project name:                                                          */
/* Author:                                                                */
/* Script type:           Database creation script                        */
/* Created on:            2010-04-13 14:59                                */
/* Model version:         Version 2010-04-13                              */
/* ---------------------------------------------------------------------- */


/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN REAL AS DOUBLE PRECISION;

CREATE DOMAIN IDENTIFIER AS CHARACTER VARYING(1000);

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN CODE AS CHARACTER VARYING(40);

CREATE DOMAIN HEADER AS CHARACTER VARYING(1000);

CREATE DOMAIN PROPERTY AS CHARACTER VARYING(100);

CREATE DOMAIN INTEGER AS INTEGER;

/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

/* ---------------------------------------------------------------------- */
/* Add table "DATA_COLUMN_VALUES"                                         */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATA_COLUMN_VALUES (
    DACO_ID TECH_ID  NOT NULL,
    ROW_ID TECH_ID  NOT NULL,
    VALUE REAL,
    CONSTRAINT PK_DATA_COLUMN_VALUES PRIMARY KEY (DACO_ID, ROW_ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "SAMPLES"                                                    */
/* ---------------------------------------------------------------------- */

CREATE TABLE SAMPLES (
    ID BIGSERIAL  NOT NULL,
    PERM_ID CODE  NOT NULL,
    CONSTRAINT PK_SAMPLES PRIMARY KEY (ID),
    CONSTRAINT TUC_SAMPLES_1 UNIQUE (PERM_ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "DATA_SETS"                                                  */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATA_SETS (
    ID BIGSERIAL  NOT NULL,
    EXPE_ID TECH_ID  NOT NULL,
    PERM_ID CODE  NOT NULL,
    CONSTRAINT PK_DATA_SETS PRIMARY KEY (ID),
    CONSTRAINT TUC_DATA_SETS_1 UNIQUE (PERM_ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "EXPERIMENTS"                                                */
/* ---------------------------------------------------------------------- */

CREATE TABLE EXPERIMENTS (
    ID BIGSERIAL  NOT NULL,
    PERM_ID CODE  NOT NULL,
    CONSTRAINT PK_EXPERIMENTS PRIMARY KEY (ID),
    CONSTRAINT TUC_EXPERIMENTS_1 UNIQUE (PERM_ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "EVENTS"                                                     */
/* ---------------------------------------------------------------------- */

CREATE TABLE EVENTS (
    LAST_SEEN_DELETION_EVENT_ID BIGINT  NOT NULL
);

/* ---------------------------------------------------------------------- */
/* Add table "COLUMNS"                                                    */
/* ---------------------------------------------------------------------- */

CREATE TABLE COLUMNS (
    ID BIGSERIAL  NOT NULL,
    DASE_ID TECH_ID  NOT NULL,
    HEADER HEADER  NOT NULL,
    CONSTRAINT PK_COLUMNS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "DATA_COLUMNS"                                               */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATA_COLUMNS (
    ID BIGSERIAL  NOT NULL,
    SAMP_ID TECH_ID,
    DASE_ID TECH_ID  NOT NULL,
    EXPERIMENT_CODE PROPERTY  NOT NULL,
    CULTIVATION_METHOD PROPERTY  NOT NULL,
    BIOLOGICAL_REPLICATE_CODE PROPERTY  NOT NULL,
    TIME_POINT INTEGER  NOT NULL,
    TIME_POINT_TYPE PROPERTY  NOT NULL,
    TECHNICAL_REPLICATE_CODE PROPERTY  NOT NULL,
    CELLOC PROPERTY  NOT NULL,
    TIME_SERIES_DATA_SET_TYPE PROPERTY  NOT NULL,
    VALUE_TYPE PROPERTY  NOT NULL,
    SCALE PROPERTY  NOT NULL,
    BI_ID PROPERTY  NOT NULL,
    CONTROLLED_GENE PROPERTY  NOT NULL,
    CONSTRAINT PK_DATA_COLUMNS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "COLUMN_VALUES"                                              */
/* ---------------------------------------------------------------------- */

CREATE TABLE COLUMN_VALUES (
    COLU_ID TECH_ID  NOT NULL,
    ROW_ID TECH_ID  NOT NULL,
    VALUE IDENTIFIER,
    CONSTRAINT PK_COLUMN_VALUES PRIMARY KEY (COLU_ID, ROW_ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "ROWS"                                                       */
/* ---------------------------------------------------------------------- */

CREATE TABLE ROWS (
    ID BIGSERIAL  NOT NULL,
    CONSTRAINT PK_ROWS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Foreign key constraints                                                */
/* ---------------------------------------------------------------------- */

ALTER TABLE DATA_COLUMN_VALUES ADD CONSTRAINT ROWS_DATA_COLUMN_VALUES 
    FOREIGN KEY (ROW_ID) REFERENCES ROWS (ID);

ALTER TABLE DATA_COLUMN_VALUES ADD CONSTRAINT DATA_COLUMNS_DATA_COLUMN_VALUES 
    FOREIGN KEY (DACO_ID) REFERENCES DATA_COLUMNS (ID);

ALTER TABLE DATA_SETS ADD CONSTRAINT EXPERIMENTS_DATA_SETS 
    FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS (ID);

ALTER TABLE COLUMNS ADD CONSTRAINT DATA_SETS_COLUMNS 
    FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID);

ALTER TABLE DATA_COLUMNS ADD CONSTRAINT SAMPLES_DATA_COLUMNS 
    FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES (ID);

ALTER TABLE DATA_COLUMNS ADD CONSTRAINT DATA_SETS_DATA_COLUMNS 
    FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID);

ALTER TABLE COLUMN_VALUES ADD CONSTRAINT COLUMNS_COLUMN_VALUES 
    FOREIGN KEY (COLU_ID) REFERENCES COLUMNS (ID);

ALTER TABLE COLUMN_VALUES ADD CONSTRAINT ROWS_COLUMN_VALUES 
    FOREIGN KEY (ROW_ID) REFERENCES ROWS (ID);
