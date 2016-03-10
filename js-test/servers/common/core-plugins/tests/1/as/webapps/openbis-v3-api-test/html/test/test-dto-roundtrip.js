define(['jquery', 'underscore', 'openbis', 'test/common'], function($, _, openbis, common) {
	return function() {
		QUnit.module("Dto roundtrip test");

		var testAction = function(c, fAction, fCheck) {
			c.start();

			c.createFacadeAndLogin()
				.then(function(facade) {
					c.ok("Login");
					return fAction(facade);
				})
				.then(function(res) {
					c.ok("Sent data. Checking results...");
					return fCheck(res);
				})
				.then(function() {
					c.finish();
				})
				.fail(function(error) {
					c.fail(error.message);
					c.finish();
				});
		}

		QUnit.test("dtosRoundtripTest()", function(assert){
			var c = new common(assert);
			
			var id = new c.CustomASServiceCode("custom-service-a");
			var actionFacade;

			var fAction = function(facade) {
				actionFacade = facade;

				return _.chain(c.getDtos())
					.map(function(proto) {
						return new c.CustomASServiceExecutionOptions().withParameter("object", new proto(""));
					})
					.map(function(options) {
						return facade.executeCustomASService(id, options);
					})
					.value();
			}

			var fCheck = function(promises) {
				return $.when.apply($, promises).then(function(here_we_get_unknown_number_of_resolved_dtos_so_foo){
					c.ok("Got results");
					
					var dtos = Array.prototype.slice.call(arguments);
					var roundtrips = _.map(dtos, function(dto){

						c.ok("Testing " + dto['@type']);
						c.ok('Rountrip ok.');

						var proto = require(dto['@type'].replace(/\./g, '/'));
						if (proto) {
							var subj = new proto("");

							_.chain(_.keys(dto))
							.filter(function(key) {
								return !key.startsWith("@");
							})
							.each(function(key){
								var val = dto[key];
								if (val && _.isFunction(val.getValue)) {
									val = val.getValue();
								}

								if (val) {
									var setter = _.find(_.functions(subj), function(fn) {
										return fn.toLowerCase() === key.toLowerCase() || fn.toLowerCase() === "set" + key.toLowerCase();
									});
									c.ok("Setter: [set]" + key);

									if (setter) {
										subj[setter](val);
									} else {
										c.ok("Skipping field " + key + " that has no setter.");
									}
								} else {
									c.ok("Skipping field " + key + " as it's empty (i.e. complex).");
								}
							});


							// let's send it back and see if it's acceptable
							var options = new c.CustomASServiceExecutionOptions().withParameter("object", subj).withParameter("echo", "true");
							return actionFacade.executeCustomASService(id, options)
								.then(function(res) {
									assert.deepEqual(JSON.parse(JSON.stringify(res)), JSON.parse(JSON.stringify(dto)), "Reconstructed " + dto['@type'] + " from Java template has same fields as the one generated and initialized by java.");
								});

						} else {
							debugger;
							c.fail('Type ' + dto['@type'] + ' is unknown to the common.');

						}
					});
					var applied = $.when.apply($, roundtrips);

					return applied;

				});
			}
			
			testAction(c, fAction, fCheck);

		});
	}
});