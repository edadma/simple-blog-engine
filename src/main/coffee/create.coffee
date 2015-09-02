app = angular.module 'create', ['ngResource']

app.controller( 'CreateController', ['$scope', '$resource', ($scope, $resource) ->
	
	Blogs = $resource '/api/v1/blogs/:id'
	
	$scope.message = {type: 'none'}
	
	$scope.check = ->
		console.log 'check'
		if ($scope.blog.subdomain)
			Blogs.get {id: $scope.blog.subdomain}, (result, response) ->
				$scope.subdomain = 'exists'
			, (response) ->
				$scope.subdomain = 'available'
		else
			$scope.subdomain = ''
			
	$scope.submit = (role) ->
		Users.save $scope.user, (result, response) ->
			if role == {}
				$scope.message = {type: 'success', text: "User created"}
			else
				$scope.message = {type: 'success', text: "User created: " + role.role + " for '" + role.title + "'"}
		, (response) ->
			$scope.message = {type: 'error', text: response.data}
	] )