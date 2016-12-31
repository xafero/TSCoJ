(function() {
	var mem = {};
	var root = '/' + 'tmp';
	var interop = {
		out : 42,
		diag : 43
	};
	return {
		close : function() {
			mem = {};
		},
		getCurrentDirectory : function() {
			return root;
		},
		fileExists : function(p) {
			return mem[p] != null;
		},
		directoryExists : function(p) {
			return mem[p] != null;
		},
		getExecutingFilePath : function() {
			return root + '/' + 'utils' + '.ts';
		},
		write : function(txt) {
			var spli = "): ";
			if (txt.contains(spli))
				interop.diag.process(txt.split(spli, 2));
			else
				interop.out.print(txt);
		},
		push : function(key, value) {
			mem[key] = value;
			mem[root + "/" + key] = value;
		},
		readFile : function(path, encoding) {
			return mem[path];
		},
		writeFile : function(path, data, byteOrderMark) {
			mem[path] = data;
		},
		getDirectories : function(path) {
			return [];
		},
		exit : function(status) {
			interop.out.println('Exit status => ' + status);
			mem["__result__", status];
		},
		dump : function() {
			return mem;
		},
		setOutput : function(writer) {
			interop.out = writer;
		},
		setDiagnose : function(helper) {
			interop.diag = helper;
		}
	}
})()