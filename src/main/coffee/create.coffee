app = angular.module 'create', ['ngResource']

app.controller( 'CreateController', ['$scope', '$resource', ($scope, $resource) ->
	
	Blogs = $resource '/api/v1/blogs/:id'
	Domains = $resource '/api/v1/domains/:id'
	
	$scope.message = {type: 'none'}
	
	$scope.check = (domain) ->
		if ($scope.blog.domain)
			Domains.get {id: $scope.blog.domain + "." + domain}, (result, response) ->
				$scope.subdomain = if result.available then 'available' else 'exists'
				console.log $scope.subdomain
		else
			$scope.subdomain = ''
			
	$scope.submit = (domain) ->
		blog = angular.copy( $scope.blog )
		blog.domain = blog.domain + "." + domain
		Blogs.save blog, (result, response) ->
			$scope.blogid = result.id
			$scope.message = {type: 'success', text: "Blog created - now click 'Setup Administrator'"}
		, (response) ->
			$scope.message = {type: 'error', text: response.data}
	] )