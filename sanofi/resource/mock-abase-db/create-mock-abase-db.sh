#!/bin/bash
USER=`whoami`
ABASE_DB="abase_mock_db"

psql -U postgres -c "create database $ABASE_DB with owner $USER template = template0 encoding = 'UNICODE'"

psql -U $USER -d $ABASE_DB -f db.sql

# Alternative version for tests on sprint server:
#psql -U postgres -c "create database abase_mock_db with owner openbis template = template0 encoding = 'UNICODE'"
#psql -U openbis -d abase_mock_db -c "CREATE TABLE plates (WELL_CODE VARCHAR NOT NULL, MATERIAL_CODE VARCHAR NOT NULL, ABASE_COMPOUND_ID VARCHAR NOT NULL, ABASE_COMPOUND_BATCH_ID VARCHAR NOT NULL, ABASE_PLATE_CODE VARCHAR NOT NULL)"

#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A001', 'COMPOUND-1', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A002', 'COMPOUND-2', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A003', 'COMPOUND-3', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A004', 'COMPOUND-4', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A005', 'COMPOUND-5', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A006', 'COMPOUND-6', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A007', 'COMPOUND-7', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A008', 'COMPOUND-8', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A009', 'COMPOUND-9', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A010', 'COMPOUND-10', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A011', 'COMPOUND-11', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A012', 'COMPOUND-12', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H001', 'COMPOUND-1', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H002', 'COMPOUND-2', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H003', 'COMPOUND-3', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H004', 'COMPOUND-4', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H005', 'COMPOUND-5', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H006', 'COMPOUND-6', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H007', 'COMPOUND-7', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H008', 'COMPOUND-8', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H009', 'COMPOUND-9', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H010', 'COMPOUND-10', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H011', 'COMPOUND-11', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"
#psql -U openbis -d abase_mock_db -c "#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H012', 'COMPOUND-12', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1')"

#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A001', 'COMPOUND-1', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A002', 'COMPOUND-2', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A003', 'COMPOUND-3', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A004', 'COMPOUND-4', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A005', 'COMPOUND-5', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A006', 'COMPOUND-6', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A007', 'COMPOUND-7', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A008', 'COMPOUND-8', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A009', 'COMPOUND-9', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A010', 'COMPOUND-10', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A011', 'COMPOUND-11', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('A012', 'COMPOUND-12', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H001', 'COMPOUND-1', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H002', 'COMPOUND-2', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H003', 'COMPOUND-3', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H004', 'COMPOUND-4', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H005', 'COMPOUND-5', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H006', 'COMPOUND-6', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H007', 'COMPOUND-7', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H008', 'COMPOUND-8', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H009', 'COMPOUND-9', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H010', 'COMPOUND-10', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H011', 'COMPOUND-11', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
#INSERT INTO plates (well_code, material_code, abase_compound_id, abase_compound_batch_id, abase_plate_code) values ('H012', 'COMPOUND-12', 'ABASE Compound (1)', 'ABASE Batch Compound (1)', 'PLATE1');
