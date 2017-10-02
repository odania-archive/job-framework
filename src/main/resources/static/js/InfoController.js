jobFramework.controller('InfoController', ['$scope', '$http', '$window',
	function ($scope, $http, $window) {
		console.log("InfoController");

		$scope.reloadConfig = function() {
			$http.post("/api/reload").then(function () {
				$window.location.reload();
			}, function (err) {
				console.error("Error occurred");
				console.error(err);
			});
		}

	}
]);
