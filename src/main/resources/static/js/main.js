var jobFramework = angular.module('jobFramework', ['ngRoute']);

jobFramework.controller('PipelineController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
	console.log("Pipeline " + pipelineId);

	$http.get('/api/pipelines/' + pipelineId).then(function (data) {
		console.info("Loaded api info", data);
		$scope.pipeline = data.data;
		$scope.paramsSteps = $scope.pipeline.paramsSteps;

		setNextStep();
	}, function (err) {
		console.error("Error occurred");
		console.error(err);
	});

	function addChoices(options, choices) {
		angular.forEach(choices, function(value, key) {
			options[key] = value;
		});

		return options;
	}

	function removeChoices(options, choices) {
		angular.forEach(choices, function(value, key) {
			delete options[key];
		});

		return options;
	}

	function setNextStep() {
		$scope.currentStepIdx += 1;
		console.debug("Next Step ", $scope.currentStep);
		$scope.hasNextStep = $scope.paramsSteps.length > ($scope.currentStepIdx + 1);
		console.debug("Has Next ", $scope.hasNextStep);
		console.info("Length: " + $scope.paramsSteps.length);
		$scope.currentStep = $scope.paramsSteps[$scope.currentStepIdx];
		console.info("Current Step: ", $scope.currentStep);
		$scope.currentParams = $scope.currentStep.params;

		console.debug("Step Info", $scope.currentStep, $scope.hasNextStep, $scope.currentStep);
	}

	$scope.isVisible = function(param) {
		var isVisible = true;
		//console.debug("isHidden for " + param.name);

		angular.forEach(param.dependencies, function(value) {
			var currentValue = $scope.data[value.param];
			var display = value.display;
			//console.debug("[" + param.name + "] Looking for hidden value of " + value.param + " CurrentValue: " + currentValue);

			angular.forEach(value.values, function(value) {
				if (currentValue === value) {
					//console.debug("Found isHidden " + value + " - " + display);
					isVisible = display;
				}
			});
		});

		return isVisible;
	};

	$scope.getOptions = function(param) {
		//console.debug("getOptions for " + param.name);
		var options = {};

		angular.forEach(param.dependencies, function(value) {

			if (value.param === null) {
				addChoices(options, value.choices);
			} else {
				var currentValue = $scope.data[value.param];
				var display = value.display;
				var choices = value.choices;
				//console.debug("[" + param.name + "] Looking for value of " + value.param + " CurrentValue: " + currentValue);

				angular.forEach(value.values, function(value) {
					if (currentValue === value) {
						//console.debug("Found " + value + " - " + display);

						if (display) {
							//console.debug("addChoices " + display);
							addChoices(options, choices);
						} else {
							//console.debug("removeChoices " + display);
							removeChoices(options, choices);
						}
					}
				});

			}
		});

		return options;
	};

	$scope.exec = function() {
		var postData = {};
		angular.forEach($scope.data, function(value, key) {
			if (Array.isArray(value)) {
				postData[key] = value.join(",");
			} else {
				postData[key] = value;
			}
		});

		console.debug("Exec", $scope.data);
		$http.post('/api/pipelines/' + pipelineId, postData).then(function (data) {
			console.log("Success", data);
			$window.location.href = '/pipelines/' + pipelineId;
		}, function (err) {
			console.error("Error occurred");
			console.error(err);
		});
	};

	$scope.nextStep = function() {
		setNextStep();
	};

	$scope.previousStep = function() {
		$scope.currentStepIdx -= 2;
		setNextStep();
	};

	$scope.currentStepIdx = -1;
	$scope.hasNextStep = true;
	$scope.data = {};
}]);

$(function () {
	console.log("Set active");
	$('a[href="' + this.location.pathname + '"]').parents('li,ul').addClass('active');
});
