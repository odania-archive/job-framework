jobFramework.controller('BuildController', ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
	console.log("BuildController Pipeline " + pipelineId + " BuildNr " + buildNr);

	function reloadBuild() {
		$http.get("/api/pipelines/" + pipelineId + "/builds/" + buildNr).then(function (response) {
			console.log("Success reloading build", response.data);
			$scope.build = response.data;
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	}


	$scope.build = {};

	reloadBuild();
	var reloadBuildTimer = $interval(reloadBuild, 4000);
}]);
