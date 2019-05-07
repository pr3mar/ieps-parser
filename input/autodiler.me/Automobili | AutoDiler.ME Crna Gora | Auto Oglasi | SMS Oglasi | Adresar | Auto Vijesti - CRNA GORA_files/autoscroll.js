function autoscroll(o,s,m,a) {
	this.obj    = o;
	this.start  = s;
	this.max    = m;
	this.auto   = a;
	this.nadole = true;
	this.divs   = null;
	this.up     = null;
	this.down   = null;
	this.init   = function(){
		var self = this;
		this.obj.onmouseover = function(e){self.auto = false};

		this.divs = this.obj.getElementsByTagName("li");
		this.up = document.createElement("a");
		this.up.setAttribute("class","autoscroll_up");
		this.up.setAttribute("href","#/");
		this.up.onclick = function(e){self.scrollup()};
		this.down = document.createElement("a");
		this.down.setAttribute("class","autoscroll_down");
		this.down.setAttribute("href","#/");
		this.down.onclick = function(e){self.scrolldown()};
		this.obj.insertBefore(this.up, this.obj.firstChild);
		this.obj.appendChild(this.down);
		this.refresh();
		if (this.auto) this.autorefresh();
	}
	this.autorefresh = function(){
		var self = this;
		setTimeout(function(){
			if (self.auto) {
				if(self.nadole)
					self.scrolldown();
				else
					self.scrollup();
			}
			self.autorefresh();
		}, 5000);
	}
	this.refresh = function(){
		this.up.style.display = (this.start > 0)?'block':'none';
		this.down.style.display = (this.start + this.max < this.divs.length)?'block':'none';
		for (i = 0; i != this.divs.length; i++) {
			this.divs[i].style.display = (i >= this.start && i < this.start + this.max)?'block':'none';
		}
	}
	this.scrollup = function(){
		if (this.start > 0) {
			this.start--;
			this.nadole = false;
		} else this.nadole = true;
		this.refresh();
		return;
	}
	this.scrolldown = function(){
		if (this.start + this.max < this.divs.length) {
			this.start++;
			this.nadole = true;
		} else this.nadole = false;
		this.refresh();
		return;
	}
	this.init();
}