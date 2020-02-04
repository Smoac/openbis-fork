var UserTests = new function() {

    this.startUserTests = function() {
        testChain = Promise.resolve();
                 //5. User Manager (end of test)
        testChain.then(() => TestUtil.deleteCookies("suitename"))
                 .then(() => TestUtil.login("testId", "pass"))
                 .then(() => this.inventorySpaceForTestUser())
                 //6. Sample Form - Creation
                 .then(() => this.creationSampleForm())
                 //13. Inventory Table - Imports for Create - Automatic Codes
                 .then(() => this.importsAutomaticCodes())
                 //14. Inventory Table - Imports for Create - Given Codes
                 .then(() => this.importsGivenCodes())
                 //15. Sample Form - Storage
                 .then(() => this.storageTest())
                 //16. Storage Manager - Moving Box
                 .then(() => this.movingBoxTest())
                 .catch(error => { console.log(error) });
    }


    this.inventorySpaceForTestUser = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var ids = ["LAB_NOTEBOOK",
                       "TESTID",
                       "MATERIALS",
                       "_MATERIALS_BACTERIA_BACTERIA_COLLECTION",
                       "_MATERIALS_CELL_LINES_CELL_LINE_COLLECTION",
                       "_MATERIALS_FLIES_FLY_COLLECTION",
                       "_MATERIALS_PLANTS_PLANT_COLLECTION",
                       "_MATERIALS_PLASMIDS_PLASMID_COLLECTION",
                       "_MATERIALS_POLYNUCLEOTIDES_OLIGO_COLLECTION",
                       "_MATERIALS_POLYNUCLEOTIDES_RNA_COLLECTION",
                       "_MATERIALS_REAGENTS_ANTIBODY_COLLECTION",
                       "_MATERIALS_REAGENTS_CHEMICAL_COLLECTION",
                       "_MATERIALS_REAGENTS_ENZYME_COLLECTION",
                       "_MATERIALS_REAGENTS_MEDIA_COLLECTION",
                       "_MATERIALS_REAGENTS_SOLUTION_BUFFER_COLLECTION",
                       "_MATERIALS_YEASTS_YEAST_COLLECTION",
                       "METHODS",
                       "_METHODS_PROTOCOLS_GENERAL_PROTOCOLS",
                       "_METHODS_PROTOCOLS_PCR_PROTOCOLS",
                       "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS"];

            Promise.resolve().then(() => TestUtil.verifyInventory(ids))
                             .then(() => e.verifyExistence("USER_MANAGER", false))
                             .then(() => resolve());
        });
    }

    this.creationSampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => UserTests.createBacteria("BAC1", "Aurantimonas"))
                             .then(() => UserTests.createBacteria("BAC2", "Burantimonas"))
                             .then(() => UserTests.createBacteria("BAC3", "Curantimonas"))
                             .then(() => UserTests.createBacteria("BAC4", "Durantimonas"))
                             .then(() => resolve());
        });
    }

    this.createBacteria = function(code, name) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var richText = '<p><span style="color:#000080;"><strong>F-&nbsp;tonA21 thi-1 thr-1 leuB6 lacY1</strong></span><strong>&nbsp;</strong><span style="color:#008000;"><i><strong>glnV44 rfbC1 fhuA1 ?? mcrB e14-(mcrA-)</strong></i></span><i><strong>&nbsp;</strong></i><span style="color:#cc99ff;"><strong><u>hsdR(rK&nbsp;-mK&nbsp;+) λ-</u></strong></span></p>';

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("sampleFormTitle"))
                             .then(() => e.equalTo("sampleFormTitle", "Create Object Bacteria", true, false))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.waitForFill("codeId"))
                             .then(() => e.equalTo("codeId", code, true, false))
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", name, false))
                             //Paste from Word
                             .then(() =>  TestUtil.ckeditorSetData("BACTERIA.GENOTYPE", richText))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             //Check saving results
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.equalTo("NAME", name, true, false))
                             .then(() => TestUtil.ckeditorTestData("BACTERIA.GENOTYPE", richText))
                             .then(() => resolve());
        });
    }

    this.importsAutomaticCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_without_identifier.tsv";
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => UserTests.importBacteriasFromFile(baseURL + pathToResource))
                             // check that bacterias was created
                             .then(() => e.waitForId("bac5-column-id"))
                             .then(() => e.waitForId("bac6-column-id"))
                             .then(() => e.waitForId("bac7-column-id"))
                             .then(() => e.waitForId("bac8-column-id"))
                             .then(() => resolve());
        });
    }

    this.importsGivenCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_with_identifier.tsv";
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => UserTests.importBacteriasFromFile(baseURL + pathToResource))
                             // check that bacterias was created
                             .then(() => e.waitForId("bac10-column-id"))
                             .then(() => e.waitForId("bac11-column-id"))
                             .then(() => e.waitForId("next-page-id"))
                             .then(() => e.click("next-page-id"))
                             .then(() => e.waitForId("bac12-column-id"))
                             .then(() => e.waitForId("bac13-column-id"))
                             .then(() => resolve());
        });
    }

    this.importBacteriasFromFile = function(file) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("options-menu-btn"))
                             .then(() => e.click("options-menu-btn"))
                             .then(() => e.waitForId("register-object-btn"))
                             .then(() => e.click("register-object-btn"))
                             .then(() => e.waitForId("choose-type-btn"))
                             .then(() => e.change("choose-type-btn", "BACTERIA", false))
                             .then(() => TestUtil.setFile("name", file, "text"))
                             .then(() => e.waitForId("accept-type-file"))
                             .then(() => e.click("accept-type-file"))
                             .then(() => resolve());
        });
    }

    this.storageTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             // we wait for the save-button, cause page contains add-storage-btn
                             // even when page can't be edit. So we wait when page be reloaded.
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.waitForId("add-storage-btn"))
                             .then(() => e.click("add-storage-btn"))
                             .then(() => e.waitForId("storage-drop-down-id"))
                             .then(() => e.change("storage-drop-down-id", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("storage-drop-down-id-1-2"))
                             .then(() => e.click("storage-drop-down-id-1-2"))
                             .then(() => e.waitForId("box-name-id"))
                             .then(() => e.write("box-name-id", "Test Box", false))
                             .then(() => e.waitForId("box-size-drop-down-id"))
                             .then(() => e.change("box-size-drop-down-id", "4X4", false))
                             .then(() => e.waitForId("storage-drop-down-id-C-2"))
                             .then(() => e.click("storage-drop-down-id-C-2"))
                             .then(() => e.click("storage-accept"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // check that new storage was created
                             .then(() => e.waitForId("testbox-c2-id"))
                             .then(() => resolve());
        });
    }

    this.movingBoxTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("toggle-storage-b-id"))
                             .then(() => e.click("toggle-storage-b-id"))
                             .then(() => e.waitForId("storage-drop-down-id-b"))
                             .then(() => e.change("storage-drop-down-id-b", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-2-storage-box"))
                             .then(() => e.waitForId("storage-drop-down-id-b-1-1"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-1-2-storage-box", "storage-drop-down-id-b-1-1", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             .then(() => resolve());
        });
    }
}