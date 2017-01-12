jobFramework.controller('ListPipelinesController', ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
		console.log("ListPipelinesController");

		function reloadPipelines() {
			$http.get("/api/pipelines").then(function (response) {
				$scope.pipelines = response.data;
			}, function (err) {
				console.error("Error occurred");
				console.error(err);
			});
		}

		$scope.pipelines = {};

		reloadPipelines();
		var reloadPipelinesTimer = $interval(reloadPipelines, 4000);
	}
]);
