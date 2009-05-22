CREATE DATABASE metabol ENCODING 'UTF8';

CREATE ROLE metabol_readonly;

CREATE ROLE metabol_readwrite;

GRANT metabol_readwrite to brinn;

\c metabol

CREATE TABLE eicmsruns (
  eicMsRunId BIGSERIAL NOT NULL,
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
  PRIMARY KEY (eicMsRunId),
  UNIQUE(permId)
);

CREATE TABLE chromatograms (
  chromId BIGSERIAL NOT NULL,
  eicMsRunId BIGINT NOT NULL,
  Q1Mz REAL NOT NULL,
  Q3LowMz REAL NOT NULL,
  Q3HighMz REAL NOT NULL,
  label VARCHAR(100) DEFAULT NULL,
  polarity VARCHAR(1) DEFAULT NULL,
  runTimes TEXT NOT NULL,
  intensities TEXT NOT NULL,
  PRIMARY KEY (chromId),
  CONSTRAINT FK_chromatogram_1 FOREIGN KEY (eicMsRunId) REFERENCES eicmsruns (eicMsRunId) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX chromatogram_fk_idx on chromatograms(eicMsRunId);

CREATE TABLE fiamsruns (
  fiaMsRunId BIGSERIAL NOT NULL,
  permId VARCHAR(40),
  rawDataFileName VARCHAR(255) NOT NULL,
  rawDataFilePath VARCHAR(1000) DEFAULT NULL,
  acquisitionDate TIMESTAMP DEFAULT NULL,
  instrumentType VARCHAR(20) DEFAULT NULL,
  instrumentManufacturer VARCHAR(50) DEFAULT NULL,
  instrumentModel VARCHAR(100) DEFAULT NULL,
  methodIonisation VARCHAR(10) DEFAULT NULL,
  methodSeparation VARCHAR(100) DEFAULT NULL,
  polarity VARCHAR(1) DEFAULT NULL,
  lowMz REAL NOT NULL,
  highMz REAL NOT NULL,
  internalStandard REAL NOT NULL,
  -- Is this a good name?
  od REAL NOT NULL,
  operator VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (fiaMsRunId),
  UNIQUE(permId)
);

CREATE TABLE profiles (
  fiaMsRunId BIGINT NOT NULL,
  lowMz REAL NOT NULL,
  highMz REAL NOT NULL,
  mz TEXT NOT NULL,
  intensities TEXT NOT NULL,
  PRIMARY KEY (fiaMsRunId, lowMz),
  CONSTRAINT FK_profiles_1 FOREIGN KEY (fiaMsRunId) REFERENCES fiamsruns (fiaMsRunId) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX profile_idx on profiles(fiaMsRunId, lowMz, highMz);

CREATE TABLE centroids (
  fiaMsRunId BIGINT NOT NULL,
  mz REAL NOT NULL,
  intensity REAL NOT NULL,
  correlation REAL NOT NULL,
  PRIMARY KEY (fiaMsRunId, mz),
  CONSTRAINT FK_centroids_1 FOREIGN KEY (fiaMsRunId) REFERENCES fiamsruns (fiaMsRunId) ON DELETE CASCADE ON UPDATE CASCADE
);

GRANT SELECT ON TABLE eicmsruns TO GROUP metabol_readonly;
GRANT SELECT ON TABLE chromatograms TO GROUP metabol_readonly;
GRANT SELECT ON TABLE fiamsruns TO GROUP metabol_readonly;
GRANT SELECT ON TABLE profiles TO GROUP metabol_readonly;
GRANT SELECT ON TABLE centroids TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE eicmsruns_eicmsrunid_seq TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE chromatograms_chromid_seq TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE fiamsruns_fiamsrunid_seq TO GROUP metabol_readonly;

GRANT ALL PRIVILEGES ON TABLE eicmsruns TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE chromatograms TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE fiamsruns TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE profiles TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE centroids TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE eicmsruns_eicmsrunid_seq TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE chromatograms_chromid_seq TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE fiamsruns_fiamsrunid_seq TO GROUP metabol_readwrite;
