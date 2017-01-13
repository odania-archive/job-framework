jobFramework.controller('PipelineController', ['$rootScope', '$scope', '$http', '$interval',
	function ($rootScope, $scope, $http, $interval) {
		console.log("PipelineController");

		function reloadPipeline() {
			$http.get("/api/pipelines/" + pipelineId).then(function (response) {
				$scope.pipeline = response.data.pipeline;
				$scope.pipeline.state = response.data.state;
				$scope.builds = response.data.builds;
			}, function (err) {
				console.error("Error occurred");
				console.error(err);
			});
		}

		$scope.pipeline = {};
		$scope.builds = {};
		$scope.restultStates = {};

		reloadPipeline();
		var reloadPipelineTimer = $interval(reloadPipeline, 4000);

		function getBuildState(step, build) {
			var currentState = build.results[step.name];

			if (currentState == null) {
				return {exitCode: -1, resultStatus: 'NOT_STARTED'};
			}

			return currentState;
		}

		$scope.getResultStatusFor = function(step, build) {
			var currentState = getBuildState(step, build);
			return $rootScope.getExitCodeStateFor(currentState.exitCode).description;
		};

		$scope.getStepCssClass = function(step, build) {
			var currentState = getBuildState(step, build);
			return $rootScope.getCssColorForExitCode(currentState.exitCode);
		};
	}
]);
