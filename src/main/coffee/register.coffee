app = angular.module( 'register', [] )

app.controller( 'registerFormController', ['$scope', ($scope) ->
	
	Users = $resource '/api/v1/users/:id'
	
	$scope.email = ""
	
	$scope.submit = ->
		console.log userid
		console.log $scope.text
	] )