jobFramework.controller('PipelineController', ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
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

		$scope.getResultStatusFor = function(step, build) {
			var currentState = build.results[step.name];

			if (currentState == null) {
				return 'NOT_STARTED';
			}

			return currentState.resultStatus;
		};

		$scope.getStepCssClass = function(step, build) {
			return ($scope.getResultStatusFor(step, build) == 'SUCCESS') ? 'build-success' : 'build-error';
		};
	}
]);
