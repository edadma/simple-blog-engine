app = angular.module( 'register', [] )

app.controller( 'registrationFormCtrl', ['$scope', ($scope) ->
	$scope.text = ""
	
	$scope.submit = ->
		console.log userid
		console.log $scope.text
	] )