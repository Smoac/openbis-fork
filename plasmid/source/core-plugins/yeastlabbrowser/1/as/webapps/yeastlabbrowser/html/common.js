function dataset(sample, data) {
   if(data.result){
      for (var i = 0; i < data.result.length; i++) {
        console.log(data.result)
        openbisServer.listFilesForDataSet(data.result[i].code, "/", true, filelist.curry(sample, data.result[i]));
      }
   }
}
