import jquery from "./types/jquery"
import underscore from "./types/underscore"
import common from "./types/common"
import openbis from "./types/openbis.esm"

exports.default = new Promise((resolve) => {
    require(["jquery", "underscore", "openbis", "test/common", "test/dtos"], function (
        $: jquery.JQueryStatic,
        _: underscore.UnderscoreStatic,
        openbisRequireJS,
        common: common.CommonConstructor,
        dtos
    ) {
        var executeModule = function (moduleName: string, facade: openbis.openbis, dtos: openbis.bundle) {
            QUnit.module(moduleName)

            function assertFileEquals(c: common.CommonClass, actualFile: openbis.File, expectedPath: string, expectedDirectory: boolean) {
                c.assertEqual(actualFile.getPath(), expectedPath, "File path")
                c.assertEqual(actualFile.getDirectory(), expectedDirectory, "File directory")
            }

            QUnit.test("list()", async function (assert) {
                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await facade.getAfsServerFacade().write("test-owner", "test-file-1", 0, "test-content-1")
                    await facade.getAfsServerFacade().write("test-owner/test-folder-1", "test-file-2", 0, "test-content-2")
                    await facade.getAfsServerFacade().write("test-owner/test-folder-1", "test-file-3", 0, "test-content-3")
                    await facade.getAfsServerFacade().write("test-owner/test-folder-2", "test-file-4", 0, "test-content-4")

                    var list = await facade.getAfsServerFacade().list("test-owner", "", true)

                    list.sort((file1, file2) => {
                        return file1.getPath().localeCompare(file2.getPath())
                    })

                    c.assertEqual(list.length, 6, "Number of files")

                    assertFileEquals(c, list[0], "/test-file-1", false)
                    assertFileEquals(c, list[1], "/test-folder-1", true)
                    assertFileEquals(c, list[2], "/test-folder-1/test-file-2", false)
                    assertFileEquals(c, list[3], "/test-folder-1/test-file-3", false)
                    assertFileEquals(c, list[4], "/test-folder-2", true)
                    assertFileEquals(c, list[5], "/test-folder-2/test-file-4", false)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })
        }

        resolve(function () {
            var afsServerUrl = "http://localhost:8085/data-store-server"
            executeModule("Afs tests (RequireJS)", new openbisRequireJS(null, afsServerUrl), dtos)
            executeModule("Afs tests (module VAR)", new window.openbis.openbis(null, afsServerUrl), window.openbis)
            executeModule("Afs tests (module ESM)", new window.openbisESM.openbis(null, afsServerUrl), window.openbisESM)
        })
    })
})
