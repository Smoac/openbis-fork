import openbis from "./lib/openbis/openbis.esm"

exports.default = new Promise((resolve) => {
    require(["jquery", "underscore", "openbis", "test/common", "test/dtos"], function (
        $,
        _,
        openbisRequireJS: openbis.openbisConstructor,
        common,
        dtos
    ) {
        var executeModule = function (moduleName: string, facade: openbis.openbis, dtos: openbis.bundle) {
            QUnit.module(moduleName)

            QUnit.test("loginAs()", async function (assert) {
                var c = new common(assert, dtos)
                c.start()

                var criteria = new dtos.SpaceSearchCriteria()
                var fetchOptions = new dtos.SpaceFetchOptions()

                try {
                    await facade.login("openbis_test_js", "password")
                    var spacesForInstanceAdmin = await facade.searchSpaces(criteria, fetchOptions)

                    await facade.loginAs("openbis_test_js", "password", "test_space_admin")
                    var spacesForSpaceAdmin = await facade.searchSpaces(criteria, fetchOptions)

                    c.assertTrue(spacesForInstanceAdmin.getTotalCount() > spacesForSpaceAdmin.getTotalCount())
                    c.assertObjectsWithValues(spacesForSpaceAdmin.getObjects(), "code", ["TEST"])
                    c.finish()
                } catch (error: any) {
                    c.fail(error.message)
                    c.finish()
                }
            })

            QUnit.test("getSessionInformation()", async function (assert) {
                var c = new common(assert, dtos)
                c.start()

                try {
                    await facade.login("openbis_test_js", "password")
                    var sessionInformation = await facade.getSessionInformation()
                    c.assertTrue(sessionInformation != null)
                    c.assertTrue(sessionInformation.getPerson() != null)
                    c.finish()
                } catch (error: any) {
                    c.fail(error.message)
                    c.finish()
                }
            })

            QUnit.test("loginAsAnonymousUser()", async function (assert) {
                var c = new common(assert, dtos)
                c.start()

                var criteria = new dtos.SpaceSearchCriteria()
                var fetchOptions = new dtos.SpaceFetchOptions()

                try {
                    await facade.loginAsAnonymousUser()
                    var spaces = await facade.searchSpaces(criteria, fetchOptions)
                    c.assertTrue(spaces.getTotalCount() == 1)
                    c.finish()
                } catch (error: any) {
                    c.fail(error.message)
                    c.finish()
                }
            })
        }

        resolve(function () {
            executeModule("Login tests (RequireJS)", new openbisRequireJS(), dtos)
            executeModule("Login tests (module VAR)", new window.openbis.openbis(), window.openbis)
            executeModule("Login tests (module ESM)", new window.openbisESM.openbis(), window.openbisESM)
        })
    })
})
