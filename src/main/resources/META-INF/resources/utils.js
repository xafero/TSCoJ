(function() {
	var mem = {};
	var root = '/' + 'tmp';
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
			print(txt.trim());
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
			// print('Exit status => ' + status);
			mem["__result__", status];
		},
		dump : function() {
			return mem;
		}
	}
})()