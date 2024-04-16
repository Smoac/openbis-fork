({
  include: [
    'openbis',
    //
    __FILES__
    //
  ],
  paths: {
    jquery: 'lib/jquery/js/jquery',
    stjs: 'lib/stjs/js/stjs',
    underscore: 'lib/underscore/js/underscore',
    moment: 'lib/moment/js/moment',
    afs : 'afs/server-data-store-facade'
  },
  shim: {
    stjs: {
      exports: 'stjs',
      deps: ['underscore']
    },
    underscore: {
      exports: '_'
    }
  }
})
