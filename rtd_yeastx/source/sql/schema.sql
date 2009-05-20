CREATE DATABASE metabol OWNER BRINN ENCODING 'UTF8';

CREATE ROLE metabol_readonly;

CREATE ROLE metabol_readwrite;

GRANT metabol_readwrite to brinn;

\c metabol

CREATE TABLE msruns (
  msRunId BIGSERIAL NOT NULL,
  permId VARCHAR(40),
  rawDataFileName VARCHAR(255) NOT NULL,
  rawDataFilePath VARCHAR(1000) DEFAULT NULL,
  acquisitionDate TIMESTAMP DEFAULT NULL,
  instrumentType VARCHAR(20) DEFAULT NULL,
  instrumentManufacturer VARCHAR(50) DEFAULT NULL,
  instrumentModel VARCHAR(100) DEFAULT NULL,
  methodIonisation VARCHAR(10) DEFAULT NULL,
  methodSeparation VARCHAR(100) DEFAULT NULL,
  operator VARCHAR(20) DEFAULT NULL,
  setId BIGINT DEFAULT NULL,
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

GRANT SELECT ON msruns TO metabol_readonly;
GRANT SELECT ON chromatograms TO metabol_readonly;

GRANT ALL PRIVILEGES ON TABLE msruns TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE chromatograms TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE msruns_msrunid_seq TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE chromatograms_chromid_seq TO GROUP metabol_readwrite;
