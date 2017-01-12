var jobFramework = angular.module('jobFramework', ['ngRoute']);

$(function () {
	console.log("Set active");
	$('a[href="' + this.location.pathname + '"]').parents('li,ul').addClass('active');
});
