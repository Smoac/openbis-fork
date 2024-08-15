# How to prepare, test and publish openbis.esm package to NPM

## How to prepare the package

* generate ESM bundle by running `:api-openbis-javascript:bundleOpenbisStaticResources` gradle task
* generate TypeScript ESM definition file by running `:api-openbis-typescript:generateTypeScript` gradle task
* copy all `openbis.esm.*` files (.d.ts, .js, .js.LICENSE.txt, .js.map) from `api-openbis-javascript/src/v3` to `api-openbis-javascript/npm/packages/openbis.esm/` folder
* in openbis.esm folder execute `npm link`

## How to test the package

- in test-app folder execute:
  - `npm install`
  - `npm link @openbis/openbis.esm`
  - `npm start`
- open http://localhost:3000 and check everything works as expected
- stop test-app
- in test-app-ts folder execute:
  - `npm install`
  - `npm link @openbis/openbis.esm`
  - `npm start`
- open http://localhost:3000 and check everything works as expected
- stop test-app-ts

## How to publish the package

- update version in openbis.esm/package.json file (already published versions cannot be published again)
- in openbis.esm folder execute:
  - `npm login` (this will open a web browser window and ask you to login to NPM, the credentials can be found in "openbis-team" mailbox in "accounts" folder, see: https://unlimited.ethz.ch/display/IDSIS/openBIS+Team+Email)
  - `npm publish --access public`
