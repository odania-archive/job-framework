jobFramework.controller('ListPipelinesController', ['$scope', '$http', '$interval', '$window',
	function ($scope, $http, $interval, $window) {
		console.log("ListPipelinesController");

		function reloadPipelines() {
			var params = {};
			if ($window.viewId != null) {
				params = {viewId: $window.viewId}
			}

			$http.get("/api/pipelines", {params: params}).then(function (response) {
				$scope.pipelines = response.data;
			}, function (err) {
				console.error("Error occurred");
				console.error(err);
			});
		}

		$scope.pipelines = {};
		$scope.viewId = null;

		reloadPipelines();
		var reloadPipelinesTimer = $interval(reloadPipelines, 4000);
	}
]);
