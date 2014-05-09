define([ "jquery" ], function($) {

	//
	// CHANNEL STACK MANAGER
	//

	function ChannelStackManager(channelStacks) {
		this.init(channelStacks);
	}

	$.extend(ChannelStackManager.prototype, {

		init : function(channelStacks) {
			this.channelStacks = channelStacks;
			this.channelStacks.sort(function(o1, o2) {
				var s1 = o1.seriesNumberOrNull;
				var s2 = o2.seriesNumberOrNull;
				var t1 = o1.timePointOrNull;
				var t2 = o2.timePointOrNull;
				var d1 = o1.depthOrNull;
				var d2 = o2.depthOrNull;

				var compare = function(v1, v2) {
					if (v1 == null) {
						if (v2 == null) {
							return 0;
						} else {
							return -1;
						}
					} else if (v2 == null) {
						return 1;
					} else {
						if (v1 > v2) {
							return 1;
						} else if (v1 < v2) {
							return -1;
						} else {
							return 0;
						}
					}
				}

				return compare(s1, s2) * 100 + compare(t1, t2) * 10 + compare(d1, d2);
			});
		},

		isMatrix : function() {
			/*
			 * TODO return (!this.isSeriesNumberPresent() ||
			 * this.getSeriesNumbers().length == 1) &&
			 * !this.isTimePointMissing() && !this.isDepthMissing() &&
			 * this.isDepthConsistent();
			 */
			return !this.isSeriesNumberPresent() && !this.isTimePointMissing() && !this.isDepthMissing() && this.isDepthConsistent();
		},

		isSeriesNumberPresent : function() {
			return this.channelStacks.some(function(channelStack) {
				return channelStack.seriesNumberOrNull;
			});
		},

		isTimePointMissing : function() {
			return this.channelStacks.some(function(channelStack) {
				return channelStack.timePointOrNull == null;
			});
		},

		isDepthMissing : function() {
			return this.channelStacks.some(function(channelStack) {
				return channelStack.depthOrNull == null;
			});
		},

		isDepthConsistent : function() {
			var map = this.getChannelStackByTimePointAndDepthMap();
			var depthCounts = {};

			for (timePoint in map) {
				var entry = map[timePoint];
				var depthCount = Object.keys(entry).length;
				depthCounts[depthCount] = true;
			}

			return Object.keys(depthCounts).length == 1;
		},

		getSeriesNumbers : function() {
			if (!this.seriesNumbers) {
				var seriesNumbers = {};

				this.channelStacks.forEach(function(channelStack) {
					if (channelStack.seriesNumberOrNull != null) {
						seriesNumbers[channelStack.seriesNumberOrNull] = true;
					}
				});

				this.seriesNumbers = Object.keys(seriesNumbers).map(function(seriesNumber) {
					return parseInt(seriesNumber);
				}).sort();
			}
			return this.seriesNumbers;
		},

		getSeriesNumber : function(index) {
			return this.getSeriesNumbers()[index];
		},

		getTimePoints : function() {
			if (!this.timePoints) {
				var timePoints = {};

				this.channelStacks.forEach(function(channelStack) {
					if (channelStack.timePointOrNull != null) {
						timePoints[channelStack.timePointOrNull] = true;
					}
				});

				this.timePoints = Object.keys(timePoints).map(function(timePoint) {
					return parseInt(timePoint);
				}).sort();
			}
			return this.timePoints;
		},

		getTimePoint : function(index) {
			return this.getTimePoints()[index];
		},

		getTimePointIndex : function(timePoint) {
			if (!this.timePointsMap) {
				var map = {};

				this.getTimePoints().forEach(function(timePoint, index) {
					map[timePoint] = index;
				});

				this.timePointsMap = map;
			}

			return this.timePointsMap[timePoint];
		},

		getDepths : function() {
			if (!this.depths) {
				var depths = {};

				this.channelStacks.forEach(function(channelStack) {
					if (channelStack.depthOrNull != null) {
						depths[channelStack.depthOrNull] = true;
					}
				});

				this.depths = Object.keys(depths).map(function(depth) {
					return parseInt(depth);
				}).sort();
			}
			return this.depths;
		},

		getDepth : function(index) {
			return this.getDepths()[index];
		},

		getDepthIndex : function(depth) {
			if (!this.depthsMap) {
				var map = {};

				this.getDepths().forEach(function(depth, index) {
					map[depth] = index;
				});

				this.depthsMap = map;
			}

			return this.depthsMap[depth];
		},

		getChannelStackIndex : function(channelStackId) {
			if (!this.channelStackMap) {
				var map = {};

				this.getChannelStacks().forEach(function(channelStack, index) {
					map[channelStack.id] = index;
				});

				this.channelStackMap = map;
			}

			return this.channelStackMap[channelStackId];
		},

		getChannelStackById : function(channelStackId) {
			if (!this.channelStackByIdMap) {
				var map = {};
				this.channelStacks.forEach(function(channelStack) {
					map[channelStack.id] = channelStack;
				});
				this.channelStackByIdMap = map;
			}
			return this.channelStackByIdMap[channelStackId];
		},

		getChannelStackByTimePointAndDepth : function(timePoint, depth) {
			var map = this.getChannelStackByTimePointAndDepthMap();
			var entry = map[timePoint];

			if (entry) {
				return entry[depth];
			} else {
				return null;
			}
		},

		getChannelStackByTimePointAndDepthMap : function() {
			if (!this.channelStackByTimePointAndDepthMap) {
				var map = {};
				this.channelStacks.forEach(function(channelStack) {
					if (channelStack.timePointOrNull != null && channelStack.depthOrNull != null) {
						var entry = map[channelStack.timePointOrNull];
						if (!entry) {
							entry = {};
							map[channelStack.timePointOrNull] = entry;
						}
						entry[channelStack.depthOrNull] = channelStack;
					}
				});
				this.channelStackByTimePointAndDepthMap = map;
			}
			return this.channelStackByTimePointAndDepthMap;
		},

		getChannelStacks : function() {
			return this.channelStacks;
		}

	});

	return ChannelStackManager;

});
