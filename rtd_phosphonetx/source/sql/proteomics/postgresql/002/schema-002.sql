/* ---------------------------------------------------------------------- */
/* Script generated with: DeZign for Databases v5.2.3                     */
/* Target DBMS:           PostgreSQL 8                                    */
/* Project file:          schema.dez                                      */
/* Project name:                                                          */
/* Author:                                                                */
/* Script type:           Database creation script                        */
/* Created on:            2009-12-07 11:03                                */
/* Model version:         Version 2009-12-07                              */
/* ---------------------------------------------------------------------- */


/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN CHECKSUM AS CHARACTER VARYING(8);

CREATE DOMAIN CODE AS CHARACTER VARYING(40);

CREATE DOMAIN DESCRIPTION AS CHARACTER VARYING(2000);

CREATE DOMAIN INTEGER_NUMBER AS INTEGER;

CREATE DOMAIN REAL_NUMBER AS DOUBLE PRECISION;

CREATE DOMAIN LONG_SEQUENCE AS TEXT;

CREATE DOMAIN SHORT_DESCRIPTION AS CHARACTER VARYING(200);

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN SHORT_SEQUENCE AS CHARACTER VARYING(1000);

CREATE DOMAIN ACCESSION_NUMBER AS CHARACTER VARYING(256);

CREATE DOMAIN SPECTRUM_REFERENCE AS CHARACTER VARYING(100);

/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

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
/* Add table "DATA_SETS"                                                  */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATA_SETS (
    ID BIGSERIAL  NOT NULL,
    EXPE_ID TECH_ID  NOT NULL,
    SAMP_ID TECH_ID,
    DB_ID TECH_ID  NOT NULL,
    PERM_ID CODE  NOT NULL,
    CONSTRAINT PK_DATA_SETS PRIMARY KEY (ID),
    CONSTRAINT TUC_DATA_SETS_1 UNIQUE (PERM_ID)
);

CREATE INDEX IX_FK_DATA_SETS_EXPERIMENTS ON DATA_SETS (EXPE_ID);

CREATE INDEX IX_FK_DATA_SETS_SAMPLES ON DATA_SETS (SAMP_ID);

/* ---------------------------------------------------------------------- */
/* Add table "MODIFICATIONS"                                              */
/* ---------------------------------------------------------------------- */

CREATE TABLE MODIFICATIONS (
    ID BIGSERIAL  NOT NULL,
    MOPE_ID TECH_ID  NOT NULL,
    POS INTEGER_NUMBER  NOT NULL,
    MASS REAL_NUMBER  NOT NULL,
    CONSTRAINT PK_MODIFICATIONS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "PEPTIDES"                                                   */
/* ---------------------------------------------------------------------- */

CREATE TABLE PEPTIDES (
    ID BIGSERIAL  NOT NULL,
    PROT_ID TECH_ID  NOT NULL,
    SEQUENCE SHORT_SEQUENCE  NOT NULL,
    CHARGE INTEGER_NUMBER  NOT NULL,
    CONSTRAINT PK_PEPTIDES PRIMARY KEY (ID)
);

CREATE INDEX IX_FK_PEPTIDES_PROTEINS ON PEPTIDES (PROT_ID);

/* ---------------------------------------------------------------------- */
/* Add table "PROTEINS"                                                   */
/* ---------------------------------------------------------------------- */

CREATE TABLE PROTEINS (
    ID BIGSERIAL  NOT NULL,
    DASE_ID TECH_ID  NOT NULL,
    PROBABILITY REAL_NUMBER  NOT NULL,
    CONSTRAINT PK_PROTEINS PRIMARY KEY (ID)
);

CREATE INDEX IDX_PROTEINS_1 ON PROTEINS (DASE_ID);

/* ---------------------------------------------------------------------- */
/* Add table "SAMPLES"                                                    */
/* ---------------------------------------------------------------------- */

CREATE TABLE SAMPLES (
    ID BIGSERIAL  NOT NULL,
    PERM_ID CODE  NOT NULL,
    EXPE_ID TECH_ID  NOT NULL,
    CONSTRAINT PK_SAMPLES PRIMARY KEY (ID),
    CONSTRAINT TUC_SAMPLES_1 UNIQUE (PERM_ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "SEQUENCES"                                                  */
/* ---------------------------------------------------------------------- */

CREATE TABLE SEQUENCES (
    ID BIGSERIAL  NOT NULL,
    DB_ID TECH_ID  NOT NULL,
    PRRE_ID TECH_ID  NOT NULL,
    AMINO_ACID_SEQUENCE LONG_SEQUENCE  NOT NULL,
    CHECKSUM CHECKSUM  NOT NULL,
    CONSTRAINT PK_SEQUENCES PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "IDENTIFIED_PROTEINS"                                        */
/* ---------------------------------------------------------------------- */

CREATE TABLE IDENTIFIED_PROTEINS (
    ID BIGSERIAL  NOT NULL,
    PROT_ID TECH_ID  NOT NULL,
    SEQU_ID TECH_ID  NOT NULL,
    CONSTRAINT PK_IDENTIFIED_PROTEINS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "ABUNDANCES"                                                 */
/* ---------------------------------------------------------------------- */

CREATE TABLE ABUNDANCES (
    ID BIGSERIAL  NOT NULL,
    PROT_ID TECH_ID  NOT NULL,
    SAMP_ID TECH_ID  NOT NULL,
    VALUE REAL_NUMBER  NOT NULL,
    CONSTRAINT PK_ABUNDANCES PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "PROBABILITY_FDR_MAPPINGS"                                   */
/* ---------------------------------------------------------------------- */

CREATE TABLE PROBABILITY_FDR_MAPPINGS (
    ID BIGSERIAL  NOT NULL,
    DASE_ID TECH_ID  NOT NULL,
    PROBABILITY REAL_NUMBER  NOT NULL,
    FALSE_DISCOVERY_RATE REAL_NUMBER  NOT NULL,
    CONSTRAINT PK_PROBABILITY_FDR_MAPPINGS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "PROTEIN_REFERENCES"                                         */
/* ---------------------------------------------------------------------- */

CREATE TABLE PROTEIN_REFERENCES (
    ID BIGSERIAL  NOT NULL,
    ACCESSION_NUMBER ACCESSION_NUMBER  NOT NULL,
    DESCRIPTION DESCRIPTION,
    CONSTRAINT PK_PROTEIN_REFERENCES PRIMARY KEY (ID),
    CONSTRAINT TUC_PROTEIN_REFERENCES_1 UNIQUE (ACCESSION_NUMBER)
);

CREATE INDEX IDX_PROTEIN_REFERENCES_1 ON PROTEIN_REFERENCES (ACCESSION_NUMBER);

/* ---------------------------------------------------------------------- */
/* Add table "DATABASES"                                                  */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATABASES (
    ID BIGSERIAL  NOT NULL,
    NAME_AND_VERSION SHORT_DESCRIPTION  NOT NULL,
    CONSTRAINT PK_DATABASES PRIMARY KEY (ID),
    CONSTRAINT TUC_DATABASES_1 UNIQUE (NAME_AND_VERSION)
);

/* ---------------------------------------------------------------------- */
/* Add table "MODIFIED_PEPTIDES"                                          */
/* ---------------------------------------------------------------------- */

CREATE TABLE MODIFIED_PEPTIDES (
    ID BIGSERIAL  NOT NULL,
    PEPT_ID TECH_ID  NOT NULL,
    NTERM_MASS REAL_NUMBER  NOT NULL,
    CTERM_MASS REAL_NUMBER  NOT NULL,
    CONSTRAINT PK_MODIFIED_PEPTIDES PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "SPECTRUM_REFERENCES"                                        */
/* ---------------------------------------------------------------------- */

CREATE TABLE SPECTRUM_REFERENCES (
    ID BIGSERIAL  NOT NULL,
    PEPT_ID TECH_ID  NOT NULL,
    REFERENCE SPECTRUM_REFERENCE  NOT NULL,
    CONSTRAINT PK_SPECTRUM_REFERENCES PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "MODIFICATION_FRACTIONS"                                     */
/* ---------------------------------------------------------------------- */

CREATE TABLE MODIFICATION_FRACTIONS (
    ID BIGSERIAL  NOT NULL,
    MODI_ID TECH_ID  NOT NULL,
    SAMP_ID TECH_ID,
    FRACTION REAL_NUMBER  NOT NULL,
    CONSTRAINT PK_MODIFICATION_FRACTIONS PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------- */
/* Add table "EVENTS"                                                     */
/* ---------------------------------------------------------------------- */

CREATE TABLE EVENTS (
    LAST_SEEN_DELETION_EVENT_ID TECH_ID  NOT NULL
);

/* ---------------------------------------------------------------------- */
/* Foreign key constraints                                                */
/* ---------------------------------------------------------------------- */

ALTER TABLE DATA_SETS ADD CONSTRAINT DA_EX_FK 
    FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS (ID);

ALTER TABLE DATA_SETS ADD CONSTRAINT DA_SA_FK 
    FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES (ID);

ALTER TABLE DATA_SETS ADD CONSTRAINT DATABASES_DATA_SETS 
    FOREIGN KEY (DB_ID) REFERENCES DATABASES (ID);

ALTER TABLE MODIFICATIONS ADD CONSTRAINT MODIFIED_PEPTIDES_MODIFICATIONS 
    FOREIGN KEY (MOPE_ID) REFERENCES MODIFIED_PEPTIDES (ID) ON DELETE CASCADE;

ALTER TABLE PEPTIDES ADD CONSTRAINT PE_PR_FK 
    FOREIGN KEY (PROT_ID) REFERENCES PROTEINS (ID) ON DELETE CASCADE;

ALTER TABLE PROTEINS ADD CONSTRAINT DATA_SETS_PROTEINS 
    FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE;

ALTER TABLE SEQUENCES ADD CONSTRAINT DATABASES_SEQUENCES 
    FOREIGN KEY (DB_ID) REFERENCES DATABASES (ID);

ALTER TABLE SEQUENCES ADD CONSTRAINT PROTEIN_REFERENCES_SEQUENCES 
    FOREIGN KEY (PRRE_ID) REFERENCES PROTEIN_REFERENCES (ID);

ALTER TABLE IDENTIFIED_PROTEINS ADD CONSTRAINT PROTEINS_IDENTIFIED_PROTEINS 
    FOREIGN KEY (PROT_ID) REFERENCES PROTEINS (ID) ON DELETE CASCADE;

ALTER TABLE IDENTIFIED_PROTEINS ADD CONSTRAINT SEQUENCES_IDENTIFIED_PROTEINS 
    FOREIGN KEY (SEQU_ID) REFERENCES SEQUENCES (ID);

ALTER TABLE ABUNDANCES ADD CONSTRAINT SAMPLES_ABUNDANCES 
    FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES (ID);

ALTER TABLE ABUNDANCES ADD CONSTRAINT PROTEINS_ABUNDANCES 
    FOREIGN KEY (PROT_ID) REFERENCES PROTEINS (ID) ON DELETE CASCADE;

ALTER TABLE PROBABILITY_FDR_MAPPINGS ADD CONSTRAINT DATA_SETS_PROBABILITY_FDR_MAPPINGS 
    FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE;

ALTER TABLE MODIFIED_PEPTIDES ADD CONSTRAINT PEPTIDES_MODIFIED_PEPTIDES 
    FOREIGN KEY (PEPT_ID) REFERENCES PEPTIDES (ID) ON DELETE CASCADE;

ALTER TABLE SPECTRUM_REFERENCES ADD CONSTRAINT PEPTIDES_SPECTRUM_REFERENCES 
    FOREIGN KEY (PEPT_ID) REFERENCES PEPTIDES (ID) ON DELETE CASCADE;

ALTER TABLE MODIFICATION_FRACTIONS ADD CONSTRAINT MODIFICATIONS_MODIFICATION_FRACTIONS 
    FOREIGN KEY (MODI_ID) REFERENCES MODIFICATIONS (ID) ON DELETE CASCADE;

ALTER TABLE MODIFICATION_FRACTIONS ADD CONSTRAINT SAMPLES_MODIFICATION_FRACTIONS 
    FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES (ID);
