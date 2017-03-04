var jobFramework = angular.module('jobFramework', ['ngRoute']);
jobFramework.run(['$rootScope', function($rootScope) {
	$rootScope.exitCodeStates = angular.fromJson(exitCodeStates);
	console.log("exitCodeStates", $rootScope.exitCodeStates);

	$rootScope.getExitCodeStateFor = function (exitCode) {
		var exitCodeState = $rootScope.exitCodeStates[exitCode];

		if (exitCode == null) {
			return {description: 'Not started', resultStatus: 'NOT_STARTED', color: 'gray'};
		}

		if (exitCodeState == null) {
			exitCodeState = $rootScope.exitCodeStates['default'];
		}

		return exitCodeState;
	};
	$rootScope.getCssColorForExitCode = function (exitCode) {
		return {'background-color': $rootScope.getExitCodeStateFor(exitCode).color};
	};
	$rootScope.getExitCodeString = function (exitCode) {
		if (exitCode == null) {
			return '';
		}

		return 'Exit Code ' +  exitCode;
	};
}]);

jobFramework.filter('orderObjectBy', function(){
	return function(input, attribute) {
		if (!angular.isObject(input)) return input;

		var array = [];
		for(var objectKey in input) {
			array.push(input[objectKey]);
		}

		array.sort(function(a, b){
			a = parseInt(a[attribute]);
			b = parseInt(b[attribute]);
			return a - b;
		});
		return array;
	}
});

$(function () {
	console.log("Set active");
	$('a[href="' + this.location.pathname + '"]').parents('li,ul').addClass('active');
});
