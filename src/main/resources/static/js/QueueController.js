jobFramework.controller('QueueController', ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
	console.log("QueueController ");

	function reloadQueue() {
		$http.get("/api/queue").then(function (response) {
			console.log("Success reload queue", response.data);
			$scope.queued = response.data.queued;
			$scope.currentBuilds = response.data.current;
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	}

	reloadQueue();
	var reloadQueueTimer = $interval(reloadQueue, 4000);

	$scope.queued = [];
	$scope.currentBuilds = {};
}]);
