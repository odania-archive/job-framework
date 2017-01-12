jobFramework.controller('QueueController', ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
	console.log("QueueController ");

	function reloadQueue() {
		$http.get("/api/queue").then(function (response) {
			$scope.queued = response.data.queued;
			$scope.currentBuilds = response.data.current;
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	}

	$scope.queued = [];
	$scope.currentBuilds = {};

	reloadQueue();
	var reloadQueueTimer = $interval(reloadQueue, 4000);

	$scope.removeFromQueue = function (pipelineId, idx) {
		$http.delete("/api/queue/remove", {params: {pipelineId: pipelineId, idx: idx}}).then(function (response) {
			$scope.queued = response.data.queued;
			$scope.currentBuilds = response.data.current;
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	};

	$scope.abortBuild = function (pipelineId, buildNr) {
		$http.delete("/api/queue/abortBuild", {params: {pipelineId: pipelineId, buildNr: buildNr}}).then(function (response) {
			$scope.queued = response.data.queued;
			$scope.currentBuilds = response.data.current;
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	};
}]);
