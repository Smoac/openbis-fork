import openbis from '@openbis/openbis.esm';

function App() {
  async function searchSpaces() {
    var facade = new openbis.openbis("http://localhost:8888/openbis/openbis/rmi-application-server-v3.json");
    await facade.login("admin", "password")
    var spaces = await facade.searchSpaces(new openbis.SpaceSearchCriteria(), new openbis.SpaceFetchOptions())
    alert(JSON.stringify(spaces.getObjects().map(space => space.getCode())))
  }
  return (
    <div>
      openBIS bundle test
      <br />
      <button onClick={searchSpaces}>Search Spaces</button>
    </div>
  );
}

export default App;
