app = angular.module 'register', ['ngResource']

app.controller( 'RegisterController', ['$scope', '$resource', ($scope, $resource) ->
	
	Users = $resource '/api/v1/users/:id'

	$scope.message = {type: 'none'}
	
	$scope.submit = (role) ->
		Users.save $scope.user, (result, response) ->
			if role == {}
				$scope.message = {type: 'success', text: "User created"}
			else
				$scope.message = {type: 'success', text: "User created: " + role.role + " for '" + role.title + "'"}
		, (response) ->
			$scope.message = {type: 'error', text: response.data}
	] )