# OpenBIS Change Log

## Version 20.10.9 (31 Jun 2024)

### Core
- Improvement: V3 API - Allow to use existing sessionToken with Java and JS facade (BIS-1336)
- Improvement: PDF Export - Formulas within spreadsheets are not converted to calculated values (BIS-1076)
- Improvement: PDF Export - Added section names in exported pdfs (BIS-1335)
- Improvement: Excel Import - Import the exported image data (BIS-1050)
- Improvement: Excel Import - Masterdata import, existing plugins support (BIS-1096)
- Improvement: Excel Import - Large imports support (BIS-1059)
- Improvement: Excel Export - "pdf" folder from exports renamed to "hierarchy" (BIS-1338)
- Improvement: Excel Export - Empty Experiment folders from export removed (BIS-1337)
- Improvement: Excel Export - Use identifiers instead of PermID in object properties for exported pdf (BIS-1055)
- Bugfix: PDF Export - Failed PDF export when there are several colored spans (BIS-1371)
- Bugfix: Excel Import - Exception during variable substitution (BIS-1058, BIS-1373)
- Bugfix: Excel Import - Issue with import of exported object with data in spreadsheet (BIS-1049)
- Bugfix: Excel Export - Object Properties References missing on xlsx (BIS-1051)

### ELN
- Improvement: ELN UI - Spreadsheet Improvements (BIS-1025)
- Improvement: ELN UI - Object/Experiment Links in word processor (BIS-1029)
- Improvement: ELN UI - ELN UI: GFB - Notes taking Widget for Experiments (BIS-1003)
- Improvement: ELN UI - GFB - Object/Experiment Links in spreadsheet (BIS-1004)
- Improvement: ELN UI - Customise ELN Welcome Page (BIS-1087)
- Improvement: ELN UI - Change behavior of eln-lims-dropbox script with hidden files (BIS-1093)
- Bugfix: ELN UI - Blanc Sample Form (BIS-1369)
- Bugfix: ELN UI - ELN-LIMS Dropbox dataset with metadata.json containing UTF-8 Characters fails (BIS-1052)
- Bugfix: ELN UI - Large Configurations Support ~ large vocabularies fail (BIS-1389)
- Bugfix: ELN UI - ELN-LIMS URL Encoding Issues (BIS-1057)
- Bugfix: ELN UI - ELN-LIMS loading without DSS (BIS-1028)

### Admin
- Improvement: Admin UI - Show identifier (name) for object property in Tables (BIS-1261)
- Improvement: Admin UI - Show user account in admin UI (BIS-776)

## Version 20.10.8 (29 May 2024)

### Core
- New Feature: Additional configuration parameters for MultiDataSetArchiveSanityCheckMaintenanceTask (SSDM-14098)
- New Feature: Registration of shared/space objects can be prevented at the system level (SSDM-14067)
- New Feature: Extend User and Email field length in PersonPE and Database (BIS-799)
- New Feature: Timezone of timestamp changed to database timezone (SSDM-14228) 
- New Feature: EXCEL Master Data Importer API (preview, non final) (BIS-772, BIS-773, BIS-994, BIS-999, BIS-1010, BIS-1011, BIS-1025, BIS-1040)
- New Feature: EXCEL Master Data Exporter API (preview, to be changed) (BIS-772, BIS-773, BIS-994, BIS-999, BIS-1010, BIS-1011, BIS-1025, BIS-1040)

- Improvement: Excel Export - Add support for cells bigger than 32k (BIS-789, BIS-790)
- Improvement: Excel Export - internal name space types are skipped from updates done by non system users (BIS-793)

- Bugfix: Excel Import - Keep Dynamic Properties Dynamic (SSDM-14224)
- Bugfix: UserManagementMaintenanceTask: role assignment error (SSDM-14261)
- Bugfix: Deadlock on display settings (SSDM-14263)
- Bugfix: DataSetAndPathInfoDBConsistencyCheckTask causing endless repetitions of tryGetDataSetLocation, causing AS logs to dramatically blow up (SSDM-14237)
- Bugfix: DSS Becomes Zombie when Dropbox folder is unreachable (SSDM-14074)
- Bugfix: MultiDataSetArchiveSanityCheckMaintenanceTask fails for h5ar files (SSDM-14124)
- Bugfix: MultiDataSetArchiver sanity check skips checksum verification if pathinfo db entries are missing (SSDM-14125)
- Bugfix: Enabling extra logging switches causes problems on AS start up (SSDM-14207)
- Bugfix: FastDownloadServlet and V3.searchFiles do not release data set locks (SSDM-14203)
- Bugfix: DSS threads do not release data set locks before dying (SSDM-14204)

### ELN
- Bugfix: Problem with selecting checkboxes at Object Browser > Register Objects popup in Firefox (SSDM-14195)
- Bugfix: ELN: not clickable checkboxes in multi-select dropdowns in popups (SSDM-14226)
- Bugfix: Typo in create inventory space form (SSDM-14133)
- Bugfix: Fix Toolbar Plugin Not Loading (BIS-991)
- Bugfix: Login screen disabled on small width screens / Android (BIS-984)
- Bugfix: Remove Life Sciences and Basic ELN Types from Installer, Link on Community Github (BIS-800)

### Admin
- Improvement: Improve the error message shown when a user with an already existing userid is attempted to be created (SSDM-14194)

## Version 20.10.7.3 (23 November 2023)
### ELN

- Bugfix: Circular Annotations deletion (SSDM-14135)
### Admin

- Improvement: XLS Master Data Importer: Make Version Optional, vocabularies bug fix (SSDM-14129)
## Version 20.10.7.2 (13 October 2023)
### ELN

- Improvement: Make object property a link (SSDM-13901)
- Improvement: Support spaces for identifiers separation in the PASTE ANY search (SSDM-13829)
- Improvement: Support PermIDs in the PASTE ANY Parent/Children Search (SSDM-13830)
- Improvement: Rename "Scan barcode" to "Scan QR codes/ barcodes" everywhere in ELN UI (SSDM-13842)
- Bugfix: Collection /ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION not created in new multi-group instances (SSDM-14068)
- Bugfix: XLS Imports and ELN UI don't take URLs with certain special characters (SSDM-13905)
- Bugfix: Issues with calculations in ELN spreadsheets (SSDM-13736)
- Bugfix: Multi-position box deletion bug (SSDM-13834)
- Bugfix: General Settings of the ELN overwrite the general settings of the Group in the multiple group instance (SSDM-13934)
- Bugfix: Remove system pop up from the back button (support issues on mobile/tablets) (SSDM-13894)
- Bugfix: Sample form can't update Objects when project-samples is disabled (SSDM-13962)
- Bugfix: Explicit "false" is not saved for "boolean" properties (SSDM-14093)
### Admin

- Improvement: XLS Master Data Importer: Make Version Optional (SSDM-13961)
### Core

- New Feature: V3 API : Import (BIS-771)
- New Feature: V3 API : Provide bundles with all V3 API JS files (BIS-761)
- Bugfix: Usage Reporting Task : Task fails with NPE if samples without space were created (SSDM-14065)
- Bugfix: User Management Task : Task removes roles that could have been configured on the json config (SSDM-13940)
- Bugfix: User Management Task : Task should not create exiting user space if a user is de-activated and re-activated (SSDM-13421)
- Bugfix: Properties : Fix properties that contain both a value and a link to a controlled vocabulary term (SSDM-13843)
- Bugfix: PAT : Remove hash from validity PAT warning emails (SSDM-14099)
## Version 20.10.7.1 (25 July 2023)
### ELN

- Improvement: ELN Dropbox provides now a report with all error messages when registration fails. (SSDM-13794)
- Improvement: Navigation further avoids to show empty folders in some situations. (SSDM-13827)
- Bugfix: Back button behaviour was in some situations incorrect. Example: pressing back after navigating to an experiment from an identifier in a table. (SSDM-13811)
- Bugfix: Zenodo Export form did break in two situations, now fixed. (SSDM-13813, SSDM-13824)
- Bugfix: Dataset Viewer was not showing the list of files on the DataSet form, only on the Collection/Sample forms, is now fixed. (SSDM-13827)
### Admin

- Improvement: Vocabulary term template now provides an explanation on how to use it. (SSDM-13817)
## Version 20.10.7 (5 July 2023)
### Core

- Improvement: Improved SFTP Folder listing performance (SSDM-13489)
- Improvement: Improved SFTP download performance (SSDM-13490)
- Improvement: V3 autogenerated code behaviour can be overridden by providing a code as it works on V1 (SSDM-12646)
- Bugfix: V3 rights for creation/update are now consistent with what is intended. Before in some cases POWER USER was necessary for things USER should be enough (SSDM-13718)
- Bugfix: V3 Removal of property type that already has some values now works correctly (SSDM-13784)
- Bugfix: User Management maintenance task now reuses the same space for a user if it gets deactivated/activated again (SSDM-13421)
- Bugfix: User Management maintenance task now assign rights correctly when moving a user from one group to another instead of rights getting lost (SSDM-13716)
- Bugfix: DataSetArchiverOrphanFinderTask fix erroneous reporting of missing tar files when using archivers on multi-group instances with sub-folders (SSDM-13725)
### ELN

- Removal: Plasmapper 2.0 integration since the external service was been decommissioned (SSDM-13664)
- New Feature: New Barcode / QR Code widget supporting scanner and camera in all places where barcodes could be used on the UI (SSDM-12100)
- New Feature: Mobile Support, navigation component can be collapsed . (SSDM-12100)
- New Feature: Dataset table in Object and Collection Forms (SSDM-13683)
- New Feature: NOT operator on Advance Search (SSDM-13427)
- Improvement: XLS Templates no longer contain names of types, to avoid long verbose names (SSDM-12531)
- Improvement: Number is now formatted with separators following the US locale (SSDM-13640)
- Improvement: Project View now separates the overview  from the list of collections/experiments (SSDM-13643)
- Improvement: Navigation menu refreshes when moving objects (SSDM-13720)
- Improvement: Navigation menu refreshes when copying objects (SSDM-13786)
- Improvement: Navigation menu nodes are not cached, this helps use cases when nodes are updated out of user control (SSDM-13785)
- Bugfix: Problem deleting spaces(SSDM-13676)
- Bugfix: Some label rendering glitches fixed (SSDM-13692)
- Bugfix: Label incorrectly named renamed from Name to Code on Objected move menu (SSDM-13728)
- Bugfix: Some glitches with repeated columns tables fixed (SSDM-13712)
- Bugfix: XLS Dataset exports (SSDM-13693)
- Bugfix: Windows Postgres version detection (SSDM-13709)
### Admin UI

- Improvement: XLS Imports now skips DB sanity check used by large migration greatly lowering import times (SSDM-13788)
- Bugfix: Now unofficial terms stay unofficial if the official checkbox is not checked. (SSDM-13730)

### ELN/Admin UI

- Improvement: High resolution logos and icons (SSDM-13504)
- Improvement: Navigation, removal of empty nodes (SSDM-13671)
## Version 20.10.6 (26 April 2023)
### Core

- New Feature: OpenBIS class to interact with AS and DSS with methods to handle uploads and semantic annotations. (SSDM-13017)
- New Feature: Dataset Creation from V3 API: Java, javascript and python3 facades support. (SSDM-13253) right arrow openBISV3API-RegisterDataSets
- New Feature: V3 API search criteria methods withSampleProperty, withVocabularyProperty added (SSDM-12986)
- New Feature: Maintenance Task that un-archives all data from a certain point in time and verifies checksums (SSDM-12971) right arrow Maintenance Tasks - MultiDataSetArchiveSanityCheckMaintenanceTask
- New Feature: Maintenance Task : Delete temporary stage directories from failed registrations from Dropboxes and sharding directories (SSDM-11605) right arrow Enabled by default, no action required
- Improvement: Performance improvements when adding children and creating new entities on the persistence layer. As used by XLS Imports. (SSDM-13033, SSDM-13195, SSDM-13207)
- Improvement: Performance improvements when using general search from the V1 API as used by the Core UI (SSDM-13449)
- Improvement: Make dropbox recovery marker directory configurable (SSDM-12056)
- Improvement: User management task make instance admins configurable (SSDM-13080)
- Bugfix: ArchivingByRequestTask : NPE when a data set does not have an experiment (SSDM-13172)
- Bugfix: MultiDataSetArchiver : make unarchiving more robust with StrongLink (SSDM-13084)
- Bugfix: DSS registration on AS -  Wait for safe registration (SSDM-13327)
- Bugfix: PAT support for single sign on setups (SSDM-13362)
### Admin UI / ELN

- New Feature: New navigation component. (SSDM-12451, SSDM-11608, SSDM-12480, SSDM-13274, SSDM-13118, SSDM-12479, SSDM-12098)right arrow User Documentation - Navigation Menu
- New Feature: Table XLS Exports of metadata and master data. (SSDM-13206, SSDM-13163, SSDM-13256, SSDM-13450, SSDM-13463, SSDM-13414, SSDM-13420) right arrow User Documentation - Tables
- New Feature: XLS Imports, zip support (SSDM-13422)
- Bugfix: XLS Imports, Allow ""-"" in codes.  Allow updating properties if the file contains a $property. All deletions to require delete tag and other bug fixes  (SSDM-13320, SSDM-13343, SSDM-13447, SSDM-13446)
### Admin UI

- New Feature: Info view. (SSDM-13133)
### ELN

- New Feature: Dropbox monitor (SSDM-13135)right arrow User Documentation - Dropbox Monitor
- New Feature: Barcode Scanner using camera from the main menu right arrow User Documentation - Barcodes
- New Feature: New Plugin Interface methods and improve the interface documentation (SSDM-13203, SSDM-13265) right arrow ELN-LIMS WEB UI Extensions
- Improvement: Removal of REQUEST.ORDER_NUMBER (SSDM-13199)
- Improvement: XLS Menu items renames (SSDM-13202)
- Improvement: Better error messages and email notifications on the ELN-LIMS dropbox. (SSDM-11306)
- Improvement: Publication API Support for Multi Group setups (Zenodo and research collection exports) (SSDM-11744)
- Improvement: Performance improvement when building a dropdown with thousands of items on Chrome browsers (SSDM-13549)
- Improvement: ELN Dropbox helper tool updated (SSDM-13360)
- Improvement: Use the Vocabulary Term URL on the VIEW mode on forms (SSDM-13436)
- Improvement: +new button in ELN is only be added if object type is specified (SSDM-13268)
- Improvement: Show permId and Identifiers more prominently (SSDM-13197)
- Improvement: remove +Add button when adding parents in ELN (SSDM-13169)
- Improvement: Inconsistency in Collection forms between Inventory and Lab notebook (SSDM-12991)
- Improvement: Add column with default barcode in Collection tables (SSDM-12889)
- Improvement: Search by size for datasets (SSDM-12472)
- Improvement: Changes on General Settings in multi-group instances (SSDM-13262)
- Improvement: Match view of folders on the SFTP to the ELN Navigation menu (SSDM-13261, SSDM-12929)
- Improvement: Use default ELN-LIMS settings on new instances instead of forcing manual configuration (SSDM-12892)
- Improvement: Preserve Plugin configurations (SSDM-13413)
- Bugfix: Date filter on tables (SSDM-13315, SSDM-13156)
- Bugfix: Boolean value to be now treated as Try-Valued, True, False or Null (SSDM-13481, SSDM-13085)
- Bugfix: after saving the ELN Settings, the page stays in edit mode, not in view mode (SSDM-13196)
- Bugfix: UnicodeEncodeError in OPERATION.generalExports.py (SSDM-13367)
- Bugfix: file-authentication: having special characters in name prevents editing in User profile (SSDM-13171)
## Version 20.10.5 (29 November 2022)
### Core

- (SSDM-11550) New Feature : Personal Access Tokens (PAT)
- (SSDM-12514) New Feature : API Event listener for integrations

- (SSDM-12625) New Feature : Active Users Email triggered from Admin UI
- (SSDM-13173) New Feature: Workspace API Extension
- (SSDM-12900) Bugfix : Sessions not properly closed
- (SSDM-12496) Bugfix  : XLS Import Issue with Project property field in XLS import conflict with openBIS Project
- (SSDM-12933) Bugfix  : XLS Import problem with upgrades on some instances - error about object types that already exist
- (SSDM-12988) Improvement : XLS Import Improved Error Message - for object references
- (SSDM-13031) Improvement  : XLS Import Improved Error Message - Missing header
- (SSDM-12996) Improvement : XLS Import Admin UI Integration
- (SSDM-13015) New Feature  : XLS Import Semantic Annotations
- (SSDM-11744) Improvement : Publications API to support multi-group instance (RC and Zenodo)
- (SSDM-12286, SSDM-13163) New Feature : Exports for Master data and Metadata - Core
- (SSDM-12905) Bugfix : MultiDataSetArchiver problems with LTS
- (SSDM-13117) Bugfix :  MultiDataSetArchiver wrongly detected inconsistency between pathinfo DB and filesystem for H5 files
- (SSDM-13084) MultiDataSetArchiver : retry mechanism for unarchiving

- (SSDM-13052) MultiDataSetArchiving : resource does not exist error

- (SSDM-13018) MultiDataSetArchiver : retry sanity check on any exception
- (SSDM-12980) Bugfix: UserManagementTask - Role Assignments - Corner Case Null Pointer
- (SSDM-12976) Improvements: Additional archiver options for unreliable File API backends
- (SSDM-12855) Improvement: Avoid TS Vector too big when migrating to 20.10 + Format improvement
- (SSDM-12936)  Improvement: Disable converting types on XLS Master Data
### Jupyter Integration:

- (SSDM-12909) Bugfix : Session token not automatically saved in Jupyter notebook
### Admin UI

- (SSDM-11675) Improvement : Get rid of Redux and Redux-Saga
- (SSDM-12451) New Feature : New database navigation component in Admin UI (tech preview)
### ELN-LIMS:

- (SSDM-11539) New Feature: Space management in ELN or new admin UI
- (SSDM-11968) Bugfix : Update CKEditor to fix underscore problem
- (SSDM-12220) Bugfix : Show description of boolean fields
- (SSDM-12327) Bugfix : keep parents and children in object templates
- (SSDM-12740) Bugfix : Cannot unselect an object type in Default Object type field in Collection
- (SSDM-12938) Bugfix : Search in property of type "Object" is not restricted to the specified object type
- (SSDM-12946) Bugfix : Dataset metadata not cleared on type change
- (SSDM-12981) Bugfix : Storage Widget Multi position delete corner case
- (SSDM-12997) Bugfix : DataSet container widget is not cleared when changing the sample
- (SSDM-12987) Bugfix : When I flag "Delete also all descendant objects" openbis goes in loop
- (SSDM-12582) Improvement : ELN Inconsistencies
- (SSDM-12584) Improvement : Add missing attributes to dataset search, archiving, unarchiving
- (SSDM-12634) Improvement : Support more Barcodes scanners
- (SSDM-12654) Improvement : Change dropdown for deletion of dependencies in trashcan
- (SSDM-12729) Improvement : Make property "order status" mandatory in requests and orders
- (SSDM-12730) Improvement : Show Description in Project form by default
- (SSDM-12766) Improvement : Export should have names instead of codes
- (SSDM-12809) Improvement : Show warning when leaving an object without saving after adding parents/children
- (SSDM-12930) Improvement : Update Settings texts
- (SSDM-12937) Improvement : Disallow config changes to system spaces,Resolved
- (SSDM-12990) Improvement : ELN Storage : if a storage is deleted, the positions assigned to it are not and it is not possible to delete them afterwards
- (SSDM-13105) Improvement : Superscript and subscript text in CKE editor
- (SSDM-13159) Improvement : Load improvements for Parents Table and Storage Views
- (SSDM-13152) New Feature : Exports for Master data and Metadata - UI
## Version 20.10.4 (3 August 2022)
### Core

- (SSDM-9831)  New Feature: Sample FK Properties : Materials Migration
- (SSDM-10984) New Feature: Excel masterdata spreadsheet rewrite in Java with parser giving error messages by line
- (SSDM-12561) Improvement: Search Engine: Improve search using existing joins
- (SSDM-12527) Improvement: Search Engine: Remove joins and enforce early filtering with subqueries for PropertySearchCriteria
- (SSDM-12661) Improvement: Refactor AbstractMaintenanceTask to AbstractGroupMaintenanceTask
- (SSDM-12554) Improvement: users removed from user management config file are not always disabled
- (SSDM-12574) Improvement: Source of root certificates and checks for certificate chains used in openBIS AS+DSS cert stores
- (SSDM-12656) Improvement: Archiver: Test consistency between data store and pathinfo database BEFORE writing tarball
- (SSDM-12500) Improvement: Archiving: Calculate data set size if not found in database
- (SSDM-12464) Improvement: Multi Data Set Archiving: Check after successful finalization multi_dataset_archive database
- (SSDM-12565) Improvement: Maintenance task to delete unused datasets on scratch share.
- (SSDM-12707) Bugfix: NPE in DataSetArchiverOrphanFinderTask
- (SSDM-12393) Bugfix: DSS startup check for AS MaintenanceTasks
- (SSDM-12703) Bugfix: SFTP shows non-existing files as empty files/folders
- (SSDM-12655) Bugfix: Search complete openBIS repo for places where we open a v3 api session internally and close them
- (SSDM-12782) Bugfix: Fix Vocabulary from Property Type Conversion
### ELN

- (SSDM-12621) New Feature : Creation of spaces
- (SSDM-12622) New Feature : New Processing Plugin Tool View
- (SSDM-12623) New Feature : Custom Imports -> New Upload to Dropbox Tool View
- (SSDM-12551) New Feature : Add filter for parents and children in ELN tables
- (SSDM-12815) New Feature : Barcode Characters Configurable
- (SSDM-12650) New Feature : Virtual FTP to follow current ELN-LIMS settings to categorise spaces and types visibility
- (SSDM-12520) Improvement : Experiment type to Experiment table in project overview and remove selection of Experiment type in project overview
- (SSDM-12765) Bugfix: Freezing failing with SwitchAAI IDs
- (SSDM-12518) Bugfix: Issue with boolean values in tables
- (SSDM-12694) Bugfix: Storage deletion in ELN
- (SSDM-12735) Bugfix: Navigation Tree doesn't show data sets for the children of a sample
- (SSDM-12771) Bugfix: cannot create request with new product added to request on multi group instances
- (SSDM-12767) Bugfix: On first creation the JS settings from plugins where not being respected
- (SSDM-12651) Bugfix: Batch upload of storage positions avoids repeating box names
- (SSDM-12732) Bugfix: SFTP shows non-existing files as empty files/folders
## Version 20.10.3.1 (13 June 2022)
### Core

- (SSDM-12045) Improvement : UserManagementMaintenanceTask - Improved template
- (SSDM-12485) Improvement : UserManagementMaintenanceTask - Create empty mapping file
- (SSDM-12081) Bugfix : freezing affects trashcan
- (SSDM-12530) Bugfix : poor performance of events search maintenance task - memory leak
- (SSDM-12556) Bugfix : poor performance of events search maintenance task - fetching too many events
### ELN

- (SSDM-11623) Improvement : Multi Group Support - Group configuration is only applied to its spaces
- (SSDM-12370) Improvement : Truncate long lists of parents/children displayed in tables
- (SSDM-12468) Improvement : Improved performance for tables
- (SSDM-12519) Improvement : add property "$show_in_project_overview" to ENTRY object type
- (SSDM-12309) Bugfix : object type Chemical shows storage widget even if this is disabled in the Settings
- (SSDM-12400) Bugfix : missing scroll down in new XLS batch upload template
- (SSDM-12412) Bugfix : when a new type is created in a multi-group instance, show in main menu is automatically enabled in all settings
- (SSDM-12417) Bugfix : error without text given if I do not add a Label for parents in ELN Settings
- (SSDM-12459) Bugfix : CKEditor missing some features and displaying wrong layout
- (SSDM-12477) Bugfix : description is not shown in MULTILINEVARCHAR fields
- (SSDM-12522) Bugfix : selection of experiments to show in project overview does not work properly
- (SSDM-12526) Bugfix : delete message for object type ENTRY shows html tags
## Version 20.10.3 (7 March 2022)
### Core

- (SSDM-11728) Bugfix : Dynamic Properties evaluation fails if sample components are accessed
- (SSDM-12059) Bugfix : SFTP : datasets connected only to samples are not shown
- (SSDM-12033) Bugfix : SFTP: delay for a user to access the recently added group
- (SSDM-11556) Improvement : export-master-data.py should export fields descriptions
- (SSDM-12293) Improvement : UserManagementMaintenanceTask: Allow to assign roles to all user spaces
- (SSDM-12051) Improvement : Extend DeletedObject by identifier and entity type
- (SSDM-11784) Improvement : Upgrade apache sshd library (for our sftp service needed)
- (SSDM-11953) Improvement : Update R version on JupyterHub image to 4.1x
- (SSDM-11978) Improvement : Upgrade Jackson to 2.9.10.8
- (SSDM-12031) Improvement : Upgrade to latest jetty 9.4 version
- (SSDM-11579) New Feature : AS Maintenance Task - Lib folder not being loaded
- (SSDM-11354) New Feature : Query Engine : Caching Implementation
- (SSDM-11954) New Feature : Maintenance task which removed deleted data sets from the archive
- (SSDM-12110) Remove CIFEX from openBIS
### ELN

- (SSDM-9305) Bugfix : Hints for children set in Settings are not shown when editing objects
- (SSDM-12184) Bugfix : Plain Text Widget and Monospace font saves HTML tags
- (SSDM-10064) Bugfix : Typo correction in Request form
- (SSDM-11256) Bugfix : Error when two storages with same code are created in 2 different ELN_SETTINGS
- (SSDM-11339) Bugfix : Move folders of disabled users to "Others disabled" in multi-group instances
- (SSDM-11819) Bugfix : REAL data type : Missing comparator for advanced search and wrong sorting for properties
- (SSDM-11977) Bugfix : Using bold text in ENTRY title breaks alphabetical sorting in main menu
- (SSDM-11980) Bugfix : Data Set Upload form does not work after a failed login attempt (ID#18459457)
- (SSDM-12055) Bugfix : Parents and children sections are not always displayed in the same place in object forms
- (SSDM-12072) Bugfix : Rescaling images embedded in text fields in Collection table overview
- (SSDM-12227) Bugfix : Move objects does not always work correctly
- (SSDM-12229) Bugfix : ELN Filters all text received from server instead only needed causing Glitch with plain txt
- (SSDM-12310) Bugfix : Rich Text: Images with non-ASCII characters in the file name are not shown
- (SSDM-12323) Bugfix : Sample deletion confirmation popup unexpected HTML tags - (SSDM-10066) Improvement : Delete Experiment/Project should delete Object/Datasets/Experiment on the Experiment/Project
- (SSDM-12049) Bugfix : Export metadata and data is not working
- (SSDM-12073) Bugfix : Could not add new user
- (SSDM-12361) Bugfix : Object templates cannot be created in group_settings of multi-group instance
- (SSDM-12057) Bugfix : User name stored in a session token has different letter case than the actual user name (ID#18459293)
- (SSDM-10474) Improvement : Collection Forms Views Configured by Individual Collection
- (SSDM-12103) Improvement : Change "deleted" message to "moved to trashcan" for entries that are moved to the trashcan
- (SSDM-11098) Improvement : Keep order of properties for types as specified in admin UI
- (SSDM-11533) Improvement : Show parents/children name in addition to identifiers in ELN tables
- (SSDM-12194) Improvement : Barcodes: Making check on minimum length more robust and validate custom barcodes
- (SSDM-12146) Improvement : Modify ELN masterdata plugin to avoid creation of default spaces in multigroup instances
- (SSDM-12218) Improvement : Tables Improvements for Rich Text Content
- (SSDM-8701) New Feature : XLS Import
- (SSDM-12312) New Feature : Allow extra tool actions
- (SSDM-10071) New Feature : Entity history
- (SSDM-9646) New Feature : Add select all/deselect all to all tables in ELN
- (SSDM-10541) New Feature : Add move all to ELN tables
- (SSDM-11664) Table Widget : Common table widget for ELN and NG UI
- (SSDM-11951) Table Widget : Sorting by multiple columns
- (SSDM-12023) Table Widget : Filtering by date ranges
- (SSDM-12025) Table Widget : Dropdown filter for Boolean properties
- (SSDM-12149) Table Widget : Show by default more columns in ELN tables
- (SSDM-12250) Table Widget : Sticky first column
### pyBIS

- (SSDM-11738) : get_samples() with children
### New Admin UI

- (SSDM-12150) : New Feature : XLS Import
- (SSDM-11169) : New Feature : Property types overview
- (SSDM-11727) : Remove the concept of local property types
## Version 20.10.2.3 (15 November 2021)
### ELN

- Fix security vulnerability.
## Version 20.10.2.2 (30 November 2021)
### Core

- (SSDM-11586) Bugfix: Pybis - uses session from last login when used in JupyterHub
- (SSDM-11792) Bugfix: Pybis - remove the usage of environment variables in Jupyter Authenticator and Pybis
- (SSDM-11879) Bugfix: Can't edit samples in Core UI when project samples disabled
- (SSDM-11462) Bugfix: V3 API - Nested fetched sort options don't work as expected
- (SSDM-11917) Bugfix: V3 API - Don't break when assigning project to sample on project samples disabled.
- (SSDM-11859) Bugfix: V1 API - reverting a deletion via coreUI or ELN is very inefficient
- (SSDM-11863) Bugfix: Python Master data export doesn't escape special characters on description
- (SSDM-11948) Bugfix: Multi Data Set Unarchiving making more robust in case of deleted data sets
- (SSDM-11964) Bugfix: user management task constantly recreates user space
- (SSDM-11602) V3 API - getRights: Adding DELETE and updating EDIT
- (SSDM-11884) Permanent deletion should show dependent deletion sets
- (SSDM-11885) Improve postregistration in case of error
### ELN

- (SSDM-10078) Bugfix: Non deletable datasets can't be moved to trashcan
- (SSDM-10301) Bugfix: 2nd level of Parents/Children now shows on Parents/Children table
- (SSDM-11425) Bugfix: Enter dates manually not always work
- (SSDM-11622) Bugfix: Multi Group Storage Support: Storage and Templates are now created for the selected group settings instead randomly
- (SSDM-11876) Bugfix: Multi Group Ordering Support: create request in a multigroup instance fails because the wrong space is used
- (SSDM-11734) Bugfix: Advanced Search - Selecting Type OR BUG
- (SSDM-11786) Bugfix: installer fail when folder exists but is empty
- (SSDM-11949) Bugfix: Archiving helper
- (SSDM-11972) Bugfix: ELN Data Set View doesn't show metadata and files of Data Sets of type UNKNOWN
- (SSDM-11844) Multi Group Storage Support: Editing Users/Groups from the ELN/LIMS
- (SSDM-11670) Side Menu Links Plugin Template
- (SSDM-9867) Advance search shows dropdown for well known values.
- (SSDM-10681) Rename "use as template" to "copy to Experiment" for protocols addition
- (SSDM-11455) Storage Tool that shows storage left for all users
- (SSDM-11557) Remove UNKOWN type from More dropdown in ELN Project
- (SSDM-11958) Show UNKNOWN dataset type on navigation menus by default
- (SSDM-11733) eln-lims dropbox metadata registration support
- (SSDM-11732) eln-lims dropbox metadata.json template export in ELN UI
- (SSDM-11855) Multi Group Storage Support: When creating storage positions only available groups show, when updating only the ones belonging to the same group as position



## Version 20.10.2.1 (6 October 2021)
### Core

- (SSDM-11740) Fix SFTP to use session token
### ELN

- (SSDM-11799) Can not create copy of an object with children
## Version 20.10.2 GA (General Availability) (22 September 2021)
### Core

- (SSDM-10942) V3 API search : Improve partial match search
- (SSDM-10941) V3 API search : Searching for several words does not scale efficiently
- (SSDM-10971) MaintenanceTaskPlugin allows to run MaintenanceTask at specified days/times
- (SSDM-10831) Entity Deletion History - maintenance task
- (SSDM-11000) V3 API search : Implement prefix matching efficient search
- (SSDM-11080) Bugfix : Relationships history : Sample-project relations are not stored/returned
- (SSDM-11124) Bugfix : Issue with deletion of MICROSCOPY_EXPERIMENT and objects
- (SSDM-11227) Bugfix : openbis_statistics_server build not working out of the box
- (SSDM-11165) Cloning a dropbox
- (SSDM-10832) V3 API : Entity Deletion History
- (SSDM-11166) Bad performance of MicroscopyThumbnailCreationTask
- (SSDM-11252) openBIS capabilities config for adding a parent for which the user has only observer rights
- (SSDM-10988) Bugfix : V3 API search : Add missing fields in partial match search
- (SSDM-11323) Bugfix : Null pointer is thrown in 20.10 using getSpace() from SampleImmutable in V2
- (SSDM-11158) Bugfix : V3 API search : Number of results after translation has changed error
- (SSDM-10497) V3 API search : NOT implementation
- (SSDM-10231) V3 API search : Full Text Search on Standard Engine
- (SSDM-11271) Plugin that generates thumbnails should allow to set the number of concurrent thumbs to do
- (SSDM-11268) SFTP Hierarchy resolver that shows the tree the same way as on the ELN-LIMS with Microscopy/Flow enabled
- (SSDM-11267) SFTP Hierarchy resolver that shows the tree the same way as on the ELN-LIMS UI
- (SSDM-11237) Make locale settings in all databases consistent
- (SSDM-11388) Bugfix : V3 API search : Search not working properly for some empty search criteria
- (SSDM-11223) API : Enable Compression in Jetty for the API
- (SSDM-10994) V3 API search: Sorting by multiple properties
- (SSDM-10962) Disable unnecessary plugings in the default Docker installation
- (SSDM-11420) Bugfix : Widget for addition of datasets when creating new jupyter notebook does not have scrollbar
- (SSDM-11577) Bugfix : Moving objects to space called SHARED_MATERIALS does not work
- (SSDM-11559) Bugfix : Cannot remove widget assignment in ELN settings
### ELN

- (SSDM-10940) Warning when searching for more than 3 words in global search
- (SSDM-10986) Anonymous login in ELN
- (SSDM-11086) Matching mode : Remember the last selected option in user settings
- (SSDM-11091) Bugfix : Editing a storage position does not work
- (SSDM-11049) Bugfix : Data set registration with file with leading space
- (SSDM-11118) Bugfix : Error Messages and wrong order in ELN
- (SSDM-11133) Bugfix : Incorrect automatic experiment code generation
- (SSDM-11174) Bugfix : Opening microscopy image viewer breaks "New..." and "More..." dropdowns
- (SSDM-11151) Bugfix : ELN hangs when copying a sample with STORAGE_POSITION child
- (SSDM-11213) Bugfix : 'visible' flag at the property level is not respected
- (SSDM-11132) Bugfix : Deletion of rows in spreadsheet component does not work
- (SSDM-11238) Bugfix : Error asigning storage
- (SSDM-11013) The limit of 50 samples on ELN Tree counts the ones you want to hide for the total
- (SSDM-11269) The limit of 50 samples on ELN Tree doesn't allow to expand after certain level
- (SSDM-11305) Move Object should allow to choose all child objects and datasets on the same space and project to be included
- (SSDM-11270) Extend plugin system to allow hiding datasets by type
- (SSDM-10944) Prototype NG_UI table integration in ELN
- (SSDM-11215) Bugfix : Flag 'create-continuous-sample-codes' is not respected in some places
- (SSDM-10968) Add space management to ELN
- (SSDM-11347) Bugfix : Overlapping messages during user registration in ELN
- (SSDM-11005) Bugfix : Vulneraibility check of Util.showError()
- (SSDM-11341) Bugfix: Hide Nagios dataset type from ELN UI
- (SSDM-11555) Bugfix : Export ignores first 3 digits of MULTILINE_VARCHAR fields
- (SSDM-11540) Bugfix : Export does not always work if objects contain a spreadheet
### New Admin UI

- (SSDM-10936) Bugfix : Bug with Order of Requests with missing quantity
- (SSDM-11346) Bugfix : Remove user in new admin UI does not work
- (SSDM-10833) Entity deletion history
- (SSDM-10939) Add SWITCH aai login to NG UI
- (SSDM-11178) Make new admin UI and ELN consistent
## Version 20.10.1 EA (Early Access) (12 March 2021)
### Core

- (SSDM-10320) Bugfix : Installer fails to Upgrade
- (SSDM-10316) Bugfix : SWITCH AAI user management tasks adds user folders each time it runs
- (SSDM-10385) Bugfix : SSLError when trying to connect to OpenBIS from JupyterHub
- (SSDM-10317) Bugfix : Missing material properties in full text search
- (SSDM-10306) Dataset Uploader : Accepting modern certificate authorities
- (SSDM-10366) Unique property values support (database changes only)
- (SSDM-10382) Bugfix : sample_identifier column doesn't update on some row updates on the database
- (SSDM-10332) New Search Engine : Full text search aggregation running on the database
- (SSDM-10304) openBIS sync : do not synchronize internally managed master data
- (SSDM-10140) Don't start if incorrect Postgres version is found
- (SSDM-10416) Disable Lucene character escape function in openBIS 20.10
- (SSDM-10473) Bugfix : tsvector_document of experiments_all not updated when project is moved
- (SSDM-10493) Bugfix : tsvector_document, sample_identifier and space_id not corretly updated when project moved to another space
- (SSDM-10469) V3 API - add "openbis-version" on the getServerInformation
- (SSDM-10413) V3 API - a method that would return available query databases is missing
- (SSDM-10395) V3 API - add "deletePersons" method
- (SSDM-10429) Bugfix : fix login issue in JupyterHub
- (SSDM-10405) New Search Engine : Nested AND/OR (Implementation)
- (SSDM-10304) openBIS sync : do not synchronize internally managed master data
- (SSDM-10574) Bugfix : Sorting by a non existing property on sample/experiment/dataset search lead to elements not containing it to not to appear on the results
- (SSDM-10566) Bugfix : V3 API search : Search by code issues
- (SSDM-10538) Bugfix : Project samples - inconsistent sample space and project after sample space change
- (SSDM-10471) Bugfix : Fix issue with DSS check script
- (SSDM-9413) Statistics collection for openBIS
- (SSDM-10702) Bugfix : V3 API search : Search by code issues

- (SSDM-10797) Bugfix : Search Engine Bug : String Equals ending in space not matching

- (SSDM-10894) Bugfix : UserManagementMaintenanceTask fails with stacked file authentication services

- (SSDM-10679) Bugfix : Possible Authorization Bug on DSS Search

- (SSDM-10611) Bugfix : obis doesn't work with git-annex version 8

- (SSDM-10707) Bugfix : Wildcard behavior in coreUI advance search

- (SSDM-10696) Change entity-history.enabled to true by default

- (SSDM-10539) Work on integration between LDAP and SWITCH AAI

- (SSDM-10782) Modify user management task for multi-group to support shared inventory spaces

- (SSDM-10677) V3 API : Make wildcard search configurable
- (SSDM-10911) Bugfix : V3 API: Global search compatibility fixes
- (SSDM-10830) Entity Deletion History - new database table
- (SSDM-10411) V3 API - a method for plugins evaluation is missing
- (SSDM-10390) Search Engine : minor performance issues found during the new UI performance testing
- (SSDM-10196) New Search engine : Missing criteria methods
### ELN

- (SSDM-10000) ELN - Barcodes Follow Up
- (SSDM-10149) Plugin Toolbar Extension
- (SSDM-10309) User Manager Improvements
- (SSDM-10387) Bugfix : ELN success message of batch uploads says 'samples' instead of 'objects'
- (SSDM-10931) Bugfix : ELN Navigation tree doesn't show data sets
- (SSDM-10913) Bugfix : ### ELN Global search compatibility fixes (UI)
- (SSDM-10904) Bugfix : Fix bug in DSS eln-lims-api reporting-plugin: openBIS java.lang.IllegalStateException: zip file closed
- (SSDM-10940) Warning when searching for more than 3 words in global search
- (SSDM-10936) Bugfix : Bug with Order of Requests with missing quantity
- (SSDM-10519) ELN UI : Fix Full Text Search sorting to score and show rank
### New Admin UI

- (SSDM-10186) NEW openBIS UI - Group Management page
- (SSDM-10401) NEW openBIS UI - Plugins management page
- (SSDM-10432) NEW openBIS UI - queries execution
- (SSDM-10402) NEW openBIS UI - queries management
- (SSDM-10431) NEW openBIS UI - plugins evaluation
- (SSDM-10663) NEW openBIS UI - "Initial value" field is not shown when needed
- (SSDM-10646) NEW openBIS UI - revise internal property type check
- (SSDM-10433) NEW openBIS UI - table overviews
- (SSDM-10420) NEW openBIS UI - use the new naming (e.g 'Object' instead of 'Sample')
- (SSDM-10912) NEW openBIS UI : enable the UI core plugin in the installer by default
- (SSDM-10428) Bugfix : NEW openBIS UI - maintain the application path in URL
## Version 20.10.0 RC (Release Candidate) (27 October 2020)
- New features and Major Changes compared to release 19.06
- Extensions to the data model:
- Date Data Type: Intended to be use when timestamps are not needed.
- Sample Property Type: Allows to link Samples without using the Parent/Children relationships.
- Sample Relationship Properties: Allows to add information to relationship connections.
- Changes to the data model:
- The Internally Managed and Internal Namespace concepts for properties have been merged. Now there is only Internally managed. Only the SYSTEM user can modify these.
- Search engine: Completely rewritten to be faster, scale better and lower memory consumption. Queries will now behave like classic database queries instead of fuzzy full text search queries.
### Admin UI Currently a preview, will replace the Core UI on the future.
### ELN-LIMS UI
- Improved plugin system for the UI.
- Microscopy and Flow Cytometry UI are now ELN plugins.
- Mayor Technology Upgrades, now using:
  - Java 11
  - Postgres 11
  - Bioformats 6.5.1
  - Jetty 9.4.30
  - and other many upgrades.
  - and lots, lots, lots of bug fixes.
- For more details see sprint change log between S301 and S334.

## Deprecated
As a rule of a thumb, deprecated features should stop being used since they can be removed in future releases.

- Material entity type is deprecated: Sample Property Types can be used instead for the same use cases.
- File Type is deprecated.
- Managed Properties are deprecated.
- Core UI is deprecated. For admin tasks use the new Admin UI.
- V1 API: A reminder that all new developments should be done using the V3 API, V1 even if still kept for backwards compatibility with old plugins is not developed anymore.
- GeneralInformationService
- GeneralInformationChangingService

### V3 API
- isInternalNamesSpace & setInternalNameSpace : Now manage the same flag "Internally Managed"
- FetchOptions.cacheMode : The new search engine ignores this, always getting the results from the database.
- EntityWithPropertiesSortOptions.fetchedFieldsScore : The new search engine ignores this, only full text search has a weights system to sort results, non usable on standard queries.
- AbstractEntitySearchCriteria.withProperty : deprecated in favour of using withXXXProperty, XXX being String, Date, Boolean or Number, check Javadoc for more details.
- AbstractEntitySearchCriteria.withAnyProperty : deprecated in favour of using withAnyXXXProperty, XXX being String, Date, Boolean or Number, check Javadoc for more details.

- Hot-deployed (aka "predeployed") Java plugins (i.e. entity validation plugin, dynamic property plugin, managed property plugin) are deprecated. WARNING: using them may lead to a deadlock during the Application Server startup.
## Removed
- The technologies for proteomics and screening where deprecated on 19.06 not been provided by the installer. Now they are finally removed and openBIS instances with these technologies CAN'T be upgraded. The upgrade procedure will detect their presence and prevent the upgrade.
