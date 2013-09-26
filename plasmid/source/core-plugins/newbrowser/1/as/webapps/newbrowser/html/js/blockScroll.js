// left: 37, up: 38, right: 39, down: 40,
// spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36

var BlockScrollUtil = new function() {
		this.keys = [37, 38, 39, 40];
		
		this.preventDefault = function(e) {
			e = e || window.event;
			if (e.preventDefault)
				e.preventDefault();
			e.returnValue = false;
		}
		
		this.keydown = function(e) {
			for (var i = BlockScrollUtil.keys.length; i--;) {
				if (e.keyCode === BlockScrollUtil.keys[i]) {
					BlockScrollUtil.preventDefault(e);
					return;
				}
			}
		}
		
		this.wheel = function(e) {
			BlockScrollUtil.preventDefault(e);
		}
		
		this.disable_scroll = function() {
			if (window.addEventListener) {
				window.addEventListener('DOMMouseScroll', BlockScrollUtil.wheel, false);
			}
			window.onmousewheel = document.onmousewheel = BlockScrollUtil.wheel;
			
			document.onkeydown = function(e) {
				this.keydown(e);
			};
		}
		
		this.enable_scroll = function() {
			if (window.removeEventListener) {
				window.removeEventListener('DOMMouseScroll', BlockScrollUtil.wheel, false);
			}
			window.onmousewheel = document.onmousewheel = document.onkeydown = null;
		}
	}
