define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var ServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(ServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.ServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return ServiceSortOptions;
})