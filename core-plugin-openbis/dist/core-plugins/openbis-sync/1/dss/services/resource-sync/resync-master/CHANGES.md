resync change log
=================

The first two components of the version tags are tied to the ResourceSync 
core specification version. Versions 1.0.x implement the v1.0 
ResourceSync specification which was standardized as ANSI/NISO Z39.99-2014
<http://www.openarchives.org/rs/1.0/toc>.

v1.0.0 2014-06-XX
  * Update for v1.0, Z39.99-2014 specification (http://www.openarchives.org/rs/1.0/)
  * Partial implementation of dump output (no read yet)

v0.9.5 2013-11-06
  * Still working toward v0.9.1 specification (http://www.openarchives.org/rs/0.9.1/)
  * Set up for Travis-CI working from github
  * Use /usr/bin/env to find python in executables
  * Fix timezone handling in w3c_datetime.py
  * Fix a number of documentation inconsistencies
 
v0.9.4 2013-09-06
  * Work toward v0.9.1 specification (http://www.openarchives.org/rs/0.9.1/)
  * ResourceSync Description becomes Source Description

v0.9.3 2013-07-31
  * Add dependencies and tests to setup, no code changes 

v0.9.2 2013-07-04
  * Cosmetic only

v0.9.1 2013-07-04
  * First release working toward v0.9 specification
  * Use test examples from v0.9 specification, add code to build them too
  * Added ResourceSync Description document
  * Improved resync-explore binary replaces --explore mode
  * Relax default notion of URL authority, add --strictauth

v0.6.3 2013-05...
  * Improved --explore mode

v0.6.2 2013-05-14
  * Fixed example code in README!
  * Added --version flag
  * Added --paths option to specify local paths to search rather than mappings

v0.6.1 2013-05-09
  * First release working toward v0.6 specification
  * Changed test examples to be those from v0.6 specification
  * Added first stab at ResourceDump and ResourceDumpManifest objects

v0.5.3 2013-05-08
  * Final release working with v0.5 specification
  * Improved handling of assumed mappings for testing client on a local filesystem
  * Fixed sitemapindex support for large resource lists, add rel="up" for component sitemaps

v0.5.2 2013-03-26
  * Fixed setup.py

v0.5.1 2013-03-22
  * Code reworked for 0.5 specification (http://www.openarchives.org/rs/0.5/)
  * Client code handles --baseline, --audit and --incremental sync against the simulator
