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

            async function deleteFile(facade: openbis.openbis, owner: string, source: string) {
                try {
                    await facade.getAfsServerFacade().delete(owner, source)
                } catch (error) {
                    if (!error.includes("NoSuchFileException")) {
                        throw error
                    }
                }
            }

            function assertFileEquals(c: common.CommonClass, actualFile: openbis.File, expectedPath: string, expectedDirectory: boolean) {
                c.assertEqual(actualFile.getPath(), expectedPath, "File path")
                c.assertEqual(actualFile.getDirectory(), expectedDirectory, "File directory")
            }

            async function assertFileExists(c: common.CommonClass, owner: string, source: string) {
                try {
                    await facade.getAfsServerFacade().read(owner, source, 0, 0)
                    c.assertTrue(true)
                } catch (error) {
                    c.fail()
                }
            }

            async function assertFileDoesNotExist(c: common.CommonClass, owner: string, source: string) {
                try {
                    await facade.getAfsServerFacade().read(owner, source, 0, 0)
                    c.fail()
                } catch (error) {
                    c.assertTrue(error.includes("NoSuchFileException"))
                }
            }

            QUnit.test("list()", async function (assert) {
                const testFolder = "test-list"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, "test-file-1", 0, "test-content-1")
                    await facade.getAfsServerFacade().write(testFolder + "/test-folder-1", "test-file-2", 0, "test-content-2")
                    await facade.getAfsServerFacade().write(testFolder + "/test-folder-1", "test-file-3", 0, "test-content-3")
                    await facade.getAfsServerFacade().write(testFolder + "/test-folder-2", "test-file-4", 0, "test-content-4")

                    var list = await facade.getAfsServerFacade().list(testFolder, "", true)

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

            QUnit.test("read() / write()", async function (assert) {
                const testFolder = "test-read-write"
                const testFile = "test-file"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFile, 0, testContent)

                    var content = await facade.getAfsServerFacade().read(testFolder, testFile, 0, testContent.length)
                    c.assertEqual(await content.text(), testContent)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })

            QUnit.test("delete()", async function (assert) {
                const testFolder = "test-delete"
                const testFile = "test-file"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFile, 0, testContent)

                    var content = await facade.getAfsServerFacade().read(testFolder, testFile, 0, testContent.length)
                    c.assertEqual(await content.text(), testContent)

                    await facade.getAfsServerFacade().delete(testFolder, testFile)

                    await assertFileDoesNotExist(c, testFolder, testFile)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })

            QUnit.test("copy()", async function (assert) {
                const testFolder = "test-copy"
                const testFileToCopy = "test-file-to-copy"
                const testFileCopied = "test-file-copied"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFileToCopy, 0, testContent)
                    await facade.getAfsServerFacade().copy(testFolder, testFileToCopy, testFolder, testFileCopied)

                    var contentToCopy = await facade.getAfsServerFacade().read(testFolder, testFileToCopy, 0, testContent.length)
                    c.assertEqual(await contentToCopy.text(), testContent)

                    var contentCopied = await facade.getAfsServerFacade().read(testFolder, testFileCopied, 0, testContent.length)
                    c.assertEqual(await contentCopied.text(), testContent)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })

            QUnit.test("move()", async function (assert) {
                const testFolder = "test-move"
                const testFileToMove = "test-file-to-move"
                const testFileMoved = "test-file-moved"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFileToMove, 0, testContent)
                    await facade.getAfsServerFacade().move(testFolder, testFileToMove, testFolder, testFileMoved)

                    await assertFileDoesNotExist(c, testFolder, testFileToMove)

                    var content = await facade.getAfsServerFacade().read(testFolder, testFileMoved, 0, testContent.length)
                    c.assertEqual(await content.text(), testContent)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })

            QUnit.test("create()", async function (assert) {
                const testFolder = "test-create"
                const testFile = "test-file"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await deleteFile(facade, testFolder, "")
                    await assertFileDoesNotExist(c, testFolder, testFile)

                    await facade.getAfsServerFacade().create(testFolder, testFile, false)
                    await assertFileExists(c, testFolder, testFile)

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
