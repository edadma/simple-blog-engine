app = angular.module( 'post', ['ngSanitize'] )

app.controller( 'registrationFormCtrl', ['$scope', ($scope) ->
	$scope.text = ""
	
	$scope.submit = ->
		console.log userid
		console.log $scope.text
	] )