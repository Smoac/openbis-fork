//
// IMAGE VIEWER
//
function ImageViewerWidget(openbis, dataSetCode) {
    this.init(openbis, dataSetCode);
}

$.extend(ImageViewerWidget.prototype, {
    init: function(openbis, dataSetCode){
    	this.facade = new OpenbisFacade(openbis);
    	this.dataSetCode = dataSetCode;
    	this.panel = $("<div>")
    },
    
    load: function(callback){
    	if(this.loaded){
    		callback();
    	}else{
            var thisImageViewer = this;
            
            var manager = new CallbackManager(function(){
                thisImageViewer.loaded = true;  
                callback();
            });
            
            this.facade.tryGetDataStoreBaseURL(thisImageViewer.dataSetCode, manager.registerCallback(function(response){
                thisImageViewer.dataStoreUrl = response.result;
            }));
            
            this.facade.getImageInfo(thisImageViewer.dataSetCode, manager.registerCallback(function(response){
                thisImageViewer.imageInfo = response.result;
            }));
            
            this.facade.getImageResolutions(thisImageViewer.dataSetCode, manager.registerCallback(function(response){
                thisImageViewer.imageResolutions = response.result;
            }));
    	}
    },
    
    render: function(){
    	var thisImageViewer = this;
        
        this.load(function(){
        	
            var channelWidget = new ChannelChooserWidget(thisImageViewer.getChannels());
            channelWidget.addChangeListener(function(){
            	imageWidget.setChannels(channelWidget.getSelectedOrMergedChannels());
            });
            thisImageViewer.panel.append(channelWidget.render());

            var resolutionWidget = new ResolutionChooserWidget(thisImageViewer.getResolutions());
            resolutionWidget.addChangeListener(function(){
            	imageWidget.setResolution(resolutionWidget.getSelectedResolution());
            });
            thisImageViewer.panel.append(resolutionWidget.render());
            
            var channelStackWidget = new ChannelStackChooserWidget(thisImageViewer.getChannelStacks());
            channelStackWidget.addChangeListener(function(){
                imageWidget.setChannelStackId(channelStackWidget.getSelectedChannelStack().id);
            });
            thisImageViewer.panel.append(channelStackWidget.render());
            
            var imageWidget = new ImageWidget();
            imageWidget.setSessionToken(thisImageViewer.facade.getSession());
            imageWidget.setDataStoreUrl(thisImageViewer.dataStoreUrl);
            imageWidget.setDataSetCode(thisImageViewer.dataSetCode);
            imageWidget.setChannelStackId(channelStackWidget.getSelectedChannelStack().id);
            imageWidget.setResolution(resolutionWidget.getSelectedResolution());
            imageWidget.setChannels(channelWidget.getSelectedOrMergedChannels());
            
            thisImageViewer.panel.append(imageWidget.render());

        });
    	
    	return this.panel;
    },
    
    getChannels: function(){
    	return this.imageInfo.imageDataset.imageDataset.imageParameters.channels;
    },
    
    getChannelStacks: function(){
    	return this.imageInfo.channelStacks;
    },
    
    getResolutions: function(){
        return this.imageResolutions;
    }
    
});

//
// FACADE
//
function OpenbisFacade(openbis){
    this.init(openbis);
}

$.extend(OpenbisFacade.prototype, {
    init: function(openbis){
        this.openbis = openbis;
    },
    
    getSession: function(){
        return this.openbis.getSession();
    },
    
    tryGetDataStoreBaseURL: function(dataSetCode, action){
    	this.openbis.tryGetDataStoreBaseURL(dataSetCode, action);
    },

    getImageInfo: function(dataSetCode, callback){
        this.openbis.getImageInfo(dataSetCode, null, callback);
    },
    
    getImageResolutions: function(dataSetCode, callback){
        this.openbis.getImageResolutions(dataSetCode, callback);
    }
});

//
// CHANNEL CHOOSER
//
function ChannelChooserWidget(channels) {
    this.init(channels);
}

$.extend(ChannelChooserWidget.prototype, {

    init: function(channels){
    	this.channels = channels;
    	this.selectedChannel = null;
    	this.mergedChannels = channels.map(function(channel){
            return channel.code;
        });
    	
    	this.listeners = new ListenerManager();
    	this.panel = $("<div>").addClass("widget");
    },
    
    render: function(){
    	var thisChooser = this; 
        
    	$("<div>").text("Channel:").appendTo(this.panel);
    	
        var select = $("<select>").appendTo(this.panel);
        
        $("<option>").attr("value", "").text("Merged Channels").appendTo(select);
        
        this.channels.forEach(function(channel){
        	$("<option>").attr("value", channel.code).text(channel.label).appendTo(select);
        });
        
        select.change(function(){
            if(select.val() == ""){
                thisChooser.setSelectedChannel(null);                    
            }else{
                thisChooser.setSelectedChannel(select.val());
            }
        });

        var mergedChannels = $("<div>").addClass("mergedChannels").appendTo(this.panel);
        
        this.channels.forEach(function(channel){
            var mergedChannel = $("<span>").addClass("mergedChannel").appendTo(mergedChannels);
            $("<input>").attr("type", "checkbox").attr("value", channel.code).appendTo(mergedChannel);
            mergedChannel.append(channel.label);
        });

        mergedChannels.find("input").change(function(){
        	var channels = []
        	mergedChannels.find("input:checked").each(function(){
        		var checkbox = $(this);
        		channels.push(checkbox.val());
        	});
        	thisChooser.setMergedChannels(channels);
        });

        this.rendered = true;
        this.refresh();
        
        return this.panel;
    },
    
    refresh: function(){
    	if(!this.rendered){
    		return;
    	}
    	
    	var thisChooser = this;
    	
    	var select = this.panel.find("select");
    	var mergedChannels = this.panel.find(".mergedChannels");
    	
    	if(this.getSelectedChannel()){
            select.val(this.getSelectedChannel());
            mergedChannels.hide();
    	}else{
            select.val("");
            mergedChannels.find("input").each(function(){
                var checkbox = $(this);
                var checked = $.inArray(checkbox.val(), thisChooser.getMergedChannels()) != -1;
                checkbox.prop("checked", checked);
                if(checked){
                    checkbox.prop("disabled", thisChooser.getMergedChannels().length == 1);
                }
            });
            mergedChannels.show();
    	}
    },

    getSelectedChannel: function(){
        return this.selectedChannel;
    },
    
    setSelectedChannel: function(channel){
        this.selectedChannel = channel;
        this.refresh();
        this.notifyChangeListeners();
    },
    
    getMergedChannels: function(){
    	return this.mergedChannels;
    },
    
    setMergedChannels: function(channels){
        this.mergedChannels = channels;
        this.refresh();
        this.notifyChangeListeners();
    },
    
    getSelectedOrMergedChannels: function(){
        if(this.getSelectedChannel()){
        	return [this.getSelectedChannel()];
        }else{
        	return this.getMergedChannels();
        }
    },
    
    addChangeListener: function(listener){
    	this.listeners.addListener('change', listener);
    },
    
    notifyChangeListeners: function(){
    	this.listeners.notifyListeners('change');
    }
    
});

//
// RESOLUTION CHOOSER
//
function ResolutionChooserWidget(resolutions) {
    this.init(resolutions);
}

$.extend(ResolutionChooserWidget.prototype, {

	 init: function(resolutions){
         this.resolutions = resolutions;
         this.selectedResolution = null;
         this.listeners = new ListenerManager();
         this.panel = $("<div>").addClass("widget");
     },
     
     render: function(){
         var thisChooser = this; 
         
         $("<div>").text("Resolution:").appendTo(this.panel);
         
         var select = $("<select>").appendTo(this.panel);
         
         $("<option>").attr("value", "").text("Default").appendTo(select);
         
         this.resolutions.forEach(function(resolution){
        	 var value = resolution.width + "x" + resolution.height;
             $("<option>").attr("value", value).text(value).appendTo(select);
         });

         select.change(function(){
        	 if(select.val() == ""){
        		 thisChooser.setSelectedResolution(null);
        	 }else{
        		 thisChooser.setSelectedResolution(select.val());
        	 }
         });
         
         this.rendered = true;
         this.refresh();

         return this.panel;
     },

     refresh: function(){
    	 if(!this.rendered){
    		 return;
    	 }
    	 
    	 var select = this.panel.find("select");
    	 if(this.selectedResolution){
    		 select.val(this.selectedResolution);
    	 }
     },
     
     getSelectedResolution: function(){
         return this.selectedResolution;
     },
     
     setSelectedResolution: function(resolution){
         this.selectedResolution = resolution;
         this.refresh();
         this.notifyChangeListeners();
     },
     
     addChangeListener: function(listener){
         this.listeners.addListener('change', listener);
     },
     
     notifyChangeListeners: function(){
         this.listeners.notifyListeners('change');
     }

});

//
// CHANNEL STACK CHOOSER
//
function ChannelStackChooserWidget(channelStacks) {
    this.init(channelStacks);
}

$.extend(ChannelStackChooserWidget.prototype, {

     init: function(channelStacks){
         var manager = new ChannelStackManager(channelStacks);
         
         if(manager.isMatrix()){
        	 this.widget = new ChannelStackMatrixChooserWidget(channelStacks);
         }else{
        	 this.widget = new ChannelStackDefaultChooserWidget(channelStacks);
         }
     },
     
     render: function(){
    	 return this.widget.render();
     },
     
     getSelectedChannelStack: function(){
         return this.widget.getSelectedChannelStack();
     },
     
     addChangeListener: function(listener){
    	 this.widget.addChangeListener(listener);
     },
     
     notifyChangeListeners: function(){
    	 this.widget.notifyChangeListeners();
     }

});

//
// CHANNEL STACK MATRIX CHOOSER
//
function ChannelStackMatrixChooserWidget(channelStacks) {
    this.init(channelStacks);
}

$.extend(ChannelStackMatrixChooserWidget.prototype, {

    init: function(channelStacks){
        this.channelStackManager = new ChannelStackManager(channelStacks);
        this.selectedTimePoint = this.channelStackManager.getTimePoints()[0]; 
        this.selectedDepth = this.channelStackManager.getDepths()[0];
        this.listeners = new ListenerManager();
        this.panel = $("<div>").addClass("widget");
    },
    
    render: function(){
        var thisChooser = this; 
        
        $("<div>").text("Channel Stack:").appendTo(this.panel);
        
        $("<span>").text("T:").appendTo(this.panel);

        var timeSelect = $("<select>").addClass("timeChooser").appendTo(this.panel);
        
        this.channelStackManager.getTimePoints().forEach(function(timePoint){
            $("<option>").attr("value", timePoint).text(timePoint).appendTo(timeSelect);
        });

        timeSelect.change(function(){
            thisChooser.setSelectedTimePoint(timeSelect.val());
        });
        
        $("<span>").text("D:").appendTo(this.panel);
        
        var depthSelect = $("<select>").addClass("depthChooser").appendTo(this.panel);
        
        this.channelStackManager.getDepths().forEach(function(depth){
            $("<option>").attr("value", depth).text(depth).appendTo(depthSelect);
        });

        depthSelect.change(function(){
            thisChooser.setSelectedDepth(depthSelect.val());
        });

        this.buttons = new MovieButtonsWidget(this.channelStackManager.getTimePoints().length);
        
        this.buttons.addChangeListener(function(){
        	var frame = thisChooser.buttons.getSelectedFrame();
        	thisChooser.setSelectedTimePoint(thisChooser.channelStackManager.getTimePoint(frame));
        });
        
        this.panel.append(this.buttons.render());
        
        this.rendered = true;
        this.refresh();

        return this.panel;
    },
    
    refresh: function(){
    	 if(!this.rendered){
             return;
         }
         
         var timeSelect = this.panel.find("select.timeChooser");
         
         if(this.selectedTimePoint){
             timeSelect.val(this.selectedTimePoint);
         }
         
         var depthSelect = this.panel.find("select.depthChooser");
         
         if(this.selectedDepth){
             depthSelect.val(this.selectedDepth);
         }          
    },
    
    getSelectedChannelStack: function(){
    	return this.channelStackManager.getChannelStack(this.selectedTimePoint, this.selectedDepth);
    },
    
    setSelectedTimePoint: function(timePoint){
    	if(this.selectedTimePoint != timePoint){
	    	this.selectedTimePoint = timePoint;
	    	this.buttons.setSelectedFrame(this.channelStackManager.getTimePointIndex(timePoint));
	    	this.refresh();
	    	this.notifyChangeListeners();
    	}
    },
    
    setSelectedDepth: function(depth){
    	if(this.selectedDepth != depth){
	        this.selectedDepth = depth;
	        this.refresh();
	        this.notifyChangeListeners();
    	}
    },
    
    addChangeListener: function(listener){
        this.listeners.addListener('change', listener);
    },
    
    notifyChangeListeners: function(){
        this.listeners.notifyListeners('change');
    }

});

//
// CHANNEL STACK DEFAULT CHOOSER
//
function ChannelStackDefaultChooserWidget(channelStacks) {
    this.init(channelStacks);
}

$.extend(ChannelStackDefaultChooserWidget.prototype, {

    init: function(channelStacks){
        this.channelStackManager = new ChannelStackManager(channelStacks);
        this.listeners = new ListenerManager();
        this.panel = $("<div>");
    },
    
    render: function(){
        this.panel.append("Default channel stack chooser");
        return this.panel;
    },
    
    getSelectedChannelStack: function(){
        return null;
    },
    
    setSelectedChannelStack: function(channelStack){
    },
    
    addChangeListener: function(listener){
        this.listeners.addListener('change', listener);
    },
    
    notifyChangeListeners: function(){
        this.listeners.notifyListeners('change');
    }

});

//
// CHANNEL STACK MANAGER
//

function ChannelStackManager(channelStacks) {
    this.init(channelStacks);
}

$.extend(ChannelStackManager.prototype, {

     init: function(channelStacks){
    	 this.channelStacks = channelStacks;
     },
     
     isMatrix: function(){
    	return !this.isSeriesNumberPresent() && !this.isTimePointMissing() && !this.isDepthMissing() && this.isDepthConsistent();  
     },
     
     isSeriesNumberPresent: function(){
    	 return this.channelStacks.some(function(channelStack){
            return channelStack.seriesNumberOrNull;        		 
    	 });
     },
     
     isTimePointMissing: function(){
         return this.channelStacks.some(function(channelStack){
             return channelStack.timePointOrNull == null;              
          });
     },
     
     isDepthMissing: function(){
         return this.channelStacks.some(function(channelStack){
             return channelStack.depthOrNull == null;              
          });
     },
     
     isDepthConsistent: function(){
         var map = this.getChannelStackMap();
         var depthCounts = {};
         
         for(timePoint in map){
             var entry = map[timePoint];
             var depthCount = Object.keys(entry).length;
             depthCounts[depthCount] = true;
         }
         
         return Object.keys(depthCounts).length == 1;
     },
     
     getTimePoints: function(){
    	 if(!this.timePoints){
             var timePoints = {};
             
             this.channelStacks.forEach(function(channelStack){
            	 if(channelStack.timePointOrNull != null){
            		 timePoints[channelStack.timePointOrNull] = true;	 
            	 }
             });
             
             this.timePoints = Object.keys(timePoints);
    	 }
    	 return this.timePoints;
     },
     
     getTimePoint: function(index){
    	 return this.getTimePoints()[index];
     },
     
     getTimePointIndex: function(timePoint){
    	 if(!this.timePointsMap){
    		 var map = {};
    		 
    		 this.getTimePoints().forEach(function(timePoint, index){
    			map[timePoint] = index;
    		 });
    		 
    		 this.timePointsMap = map;
    	 }
    	 
    	 return this.timePointsMap[timePoint];
     },
     
     getDepths: function(){
         if(!this.depths){
             var depths = {};
             
             this.channelStacks.forEach(function(channelStack){
                 if(channelStack.depthOrNull != null){
                	 depths[channelStack.depthOrNull] = true;    
                 }
             });
             
             this.depths = Object.keys(depths);
         }
         return this.depths;
     },
     
     getDepth: function(index){
    	 return this.getDepths()[index];
     },     
     
     getDepthIndex: function(depth){
    	 if(!this.depthsMap){
    		 var map = {};
    		 
    		 this.getDepths().forEach(function(depth, index){
    			map[depth] = index;
    		 });
    		 
    		 this.depthsMap = map;
    	 }
    	 
    	 return this.depthsMap[depth];
     },
     
     getChannelStack: function(timePoint, depthLevel){
    	 var map = this.getChannelStackMap();
    	 var entry = map[timePoint];
    	 
    	 if(entry){
    		 return entry[depthLevel];
    	 }else{
    		 return null;
    	 }
     },
     
     getChannelStacks: function(){
    	 return this.channelStacks;
     },
     
     getChannelStackMap: function(){
         if(!this.channelStackMap){
             var map = {};
             this.channelStacks.forEach(function(channelStack){
                 if(channelStack.timePointOrNull != null && channelStack.depthOrNull != null){
                     var entry = map[channelStack.timePointOrNull];
                     if(!entry){
                         entry = {};
                         map[channelStack.timePointOrNull] = entry;
                     }
                     entry[channelStack.depthOrNull] = channelStack;
                 }
             });
             this.channelStackMap = map;
         }
         return this.channelStackMap; 
     }
});

//
// MOVIE BUTTONS WIDGET
//
function MovieButtonsWidget(frameCount) {
    this.init(frameCount);
}

$.extend(MovieButtonsWidget.prototype, {

     init: function(frameCount){
    	 this.frameCount = frameCount;
    	 this.frameAction = null;
    	 this.selectedDelay = 500;
    	 this.selectedFrame = 0;
    	 this.listeners = new ListenerManager();
         this.panel = $("<div>").addClass("widget");
     },
     
     render: function(){
         var thisButtons = this; 
         
         var play = $("<button>").addClass("play").text("Play").appendTo(this.panel);
         
         play.click(function(){
        	 thisButtons.play();
         });
         
         var stop = $("<button>").addClass("stop").text("Stop").appendTo(this.panel);
         
         stop.click(function(){
        	 thisButtons.stop();
         });

         var prev = $("<button>").addClass("prev").text("<<").appendTo(this.panel);
         
         prev.click(function(){
        	 thisButtons.setSelectedFrame(thisButtons.getSelectedFrame() - 1);
         });
         
         var next = $("<button>").addClass("next").text(">>").appendTo(this.panel);
         
         next.click(function(){
        	 thisButtons.setSelectedFrame(thisButtons.getSelectedFrame() + 1);
         });
         
         var delay = $("<input>").attr("type", "text").addClass("delay").appendTo(this.panel);
         
         delay.change(function(){
        	 thisButtons.setSelectedDelay(delay.val());
         });

         this.rendered = true;
         this.refresh();

         return this.panel;
     },

     refresh: function(){
         if(!this.rendered){
             return;
         }
         
         var play = this.panel.find("button.play");
         play.prop("disabled", this.frameAction != null);
         
         var stop = this.panel.find("button.stop");
         stop.prop("disabled", this.frameAction == null);
         
         var prev = this.panel.find("button.prev");
         prev.prop("disabled", this.getSelectedFrame() == 0);
         
         var next = this.panel.find("button.next");
         next.prop("disabled", this.getSelectedFrame() == (this.frameCount - 1));
         
         var delay = this.panel.find("input.delay");
         delay.val(this.selectedDelay);
     },
     
     play: function(){
    	 if(this.frameAction){
    		 return;
    	 }
    	 
    	 if(this.getSelectedFrame() == this.frameCount - 1){
    		 this.setSelectedFrame(0);
    	 }
    	 
    	 var thisButtons = this;
    	 
    	 this.frameAction = function(){
    		 if(!thisButtons.frameAction){
    			 return;
    		 }
    		 
    		 if(thisButtons.getSelectedFrame() < thisButtons.frameCount - 1){
    			 thisButtons.setSelectedFrame(thisButtons.getSelectedFrame() + 1);
      			 setTimeout(thisButtons.frameAction, thisButtons.selectedDelay);
    		 }else{
    			 thisButtons.stop();
    			 thisButtons.setSelectedFrame(0);
    		 }
    	 };
    	 
    	 this.frameAction();
     },
     
     stop: function(){
    	 this.frameAction = null;
         this.refresh();
     },

     getSelectedDelay: function(){
    	 return this.selectedDelay;
     },
     
     setSelectedDelay: function(delay){
    	 if(this.selectedDelay != delay){
	    	 this.selectedDelay = delay;
	    	 this.refresh();
    	 }
     },
     
     getSelectedFrame: function(){
         return this.selectedFrame;
     },
     
     setSelectedFrame: function(frame){
    	 frame = Math.min(Math.max(0, frame), this.frameCount - 1);

    	 if(this.selectedFrame != frame){
	         this.selectedFrame = frame; 
	         this.refresh();
	         this.notifyChangeListeners();
    	 }
     },
     
     addChangeListener: function(listener){
         this.listeners.addListener('change', listener);
     },
     
     notifyChangeListeners: function(){
         this.listeners.notifyListeners('change');
     }

});

//
// IMAGE
//
function ImageWidget() {
    this.init();
}

$.extend(ImageWidget.prototype, {

    init: function(){
    	this.panel = $("<div>")
    },
    
    render: function(){
    	$("<img>").appendTo(this.panel);
    	this.rendered = true;
    	this.refresh();
        return this.panel;
    },
    
    refresh: function(){
    	if(!this.rendered){
    		return;
    	}
    	
    	var url = this.dataStoreUrl + "/datastore_server_screening";
    	url += "?sessionID=" + this.sessionToken;
    	url += "&dataset=" + this.dataSetCode;
    	url += "&channelStackId=" + this.channelStackId;

    	this.channels.forEach(function(channel){
    		url += "&channel=" + channel;
    	});
    	
    	if(this.resolution){
    		url += "&mode=thumbnail" + this.resolution;	
    	}else{
    		url += "&mode=thumbnail480x480";
    	}
    	
    	this.panel.find("img").attr("src", url);
    },
    
    setDataStoreUrl: function(dataStoreUrl){
    	this.dataStoreUrl = dataStoreUrl;
    	this.refresh();
    },
    
    setSessionToken: function(sessionToken){
    	this.sessionToken = sessionToken;
    	this.refresh();
    },
    
    setDataSetCode: function(dataSetCode){
    	this.dataSetCode = dataSetCode;
    	this.refresh();
    },
    
    setChannelStackId: function(channelStackId){
    	this.channelStackId = channelStackId;
    	this.refresh();
    },
    
    setChannels: function(channels){
    	this.channels = channels;
    	this.refresh();
    },
    
    setResolution: function(resolution){
    	this.resolution = resolution;
    	this.refresh();
    }
    
});

//
// CALLBACK MANAGER
//
function CallbackManager(callback) {
	this.init(callback);
}

$.extend(CallbackManager.prototype, {

	init: function(callback){
		this.callback = callback;
		this.callbacks = {};
	},
	
    registerCallback: function(callback){
    	var manager = this;
    	
    	var wrapper = function(){
    		callback.apply(this, arguments);
    		
    		delete manager.callbacks[callback]
    		
    		for(c in manager.callbacks){
    		    return;        			
    		}
    		
    		manager.callback();
    	}
    	
    	this.callbacks[callback] = callback;
    	return wrapper;
    }
});


//
// LISTENER MANAGER
//
function ListenerManager() {
	this.init();
}

$.extend(ListenerManager.prototype, {

	init: function(){
		this.listeners = {};
	},
	
    addListener: function(eventType, listener){
    	if(!this.listeners[eventType]){
    		this.listeners[eventType] = []
    	}
    	this.listeners[eventType].push(listener);
    },
    
    notifyListeners: function(eventType){
        if(this.listeners[eventType]){
            this.listeners[eventType].forEach(function(listener){
                listener();
            });
        }
    }
});
