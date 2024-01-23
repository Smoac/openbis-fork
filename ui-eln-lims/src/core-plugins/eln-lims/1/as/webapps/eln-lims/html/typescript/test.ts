import openbis from "./openbis.esm"

export default class Test {
    public async test(): Promise<openbis.SearchResult<openbis.Space>> {
        var facade = new openbis.openbis("http://localhost:8888/openbis/openbis/rmi-application-server-v3.json");
        await facade.login("admin","password")
        return await facade.searchSpaces(new openbis.SpaceSearchCriteria(), new openbis.SpaceFetchOptions())
    }
}
