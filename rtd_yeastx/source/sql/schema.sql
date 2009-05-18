CREATE DATABASE metabol OWNER BRINN ENCODING 'UTF8';

\c metabol

SET ROLE 'brinn';

CREATE TABLE msruns (
  msRunId BIGSERIAL NOT NULL,
  permId VARCHAR(40),
  rawDataFileName VARCHAR(255) NOT NULL,
  rawDataFilePath VARCHAR(1000) DEFAULT NULL,
  acquisitionDate VARCHAR(30) DEFAULT NULL,
  instrumentType VARCHAR(20) DEFAULT NULL,
  instrumentManufacturer VARCHAR(50) DEFAULT NULL,
  instrumentModel VARCHAR(100) DEFAULT NULL,
  methodIonisation VARCHAR(10) DEFAULT NULL,
  methodSeparation VARCHAR(100) DEFAULT NULL,
  startTime REAL NOT NULL,
  endTime REAL NOT NULL,
  PRIMARY KEY (msRunId),
  UNIQUE(permId)
);

CREATE TABLE chromatograms (
  chromId BIGSERIAL NOT NULL,
  msRunId BIGINT NOT NULL,
  Q1Mz REAL NOT NULL,
  Q3LowMz REAL NOT NULL,
  Q3HighMz REAL NOT NULL,
  label VARCHAR(100) DEFAULT NULL,
  polarity VARCHAR(1) DEFAULT NULL,
  runTimes TEXT NOT NULL,
  intensities TEXT NOT NULL,
  PRIMARY KEY (chromId),
  CONSTRAINT FK_chromatogram_1 FOREIGN KEY (msRunId) REFERENCES msruns (msRunId) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX chromatogram_fk on chromatograms(msRunId);