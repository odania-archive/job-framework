jobFramework.controller('BuildController', ['$scope', '$http', '$interval', '$anchorScroll', '$location',
	function ($scope, $http, $interval, $anchorScroll, $location) {
	console.log("BuildController Pipeline " + pipelineId + " BuildNr " + buildNr);

	function scrollToBottom() {
		$location.hash('anchor-bottom');
		$anchorScroll();
	}

	function reloadBuild() {
		$http.get("/api/pipelines/" + pipelineId + "/builds/" + buildNr).then(function (response) {
			console.log("Success reloading build", response.data);
			$scope.build = response.data;

			if ($scope.autoScrollToBottom) {
				scrollToBottom();
			}
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	}

	$scope.autoScrollToBottom = false;
	$scope.build = {};
	$scope.autoButtonClass = 'btn-info';

	reloadBuild();
	var reloadBuildTimer = $interval(reloadBuild, 4000);

	$scope.toggleScrollToBottom = function() {
		$scope.autoScrollToBottom = !$scope.autoScrollToBottom;

		$scope.autoButtonClass = $scope.autoScrollToBottom ? 'btn-success' : 'btn-info';

		if ($scope.autoScrollToBottom) {
			scrollToBottom();
		}
	};
	$scope.goToBottom = function() {
		scrollToBottom();
	};
	$scope.goToAnchor = function(stepName) {
		var anchorName = 'anchor-' + stepName;
		if ($location.hash() != anchorName) {
			$location.hash(anchorName);
		} else {
			$anchorScroll();
		}
	};
}]);
