app = angular.module( 'post', ['ngSanitize', 'ngResource'] )

app.controller( 'PostController', ['$scope', '$resource', ($scope, $resource) ->
	Post = $resource( '/api/v1/post' )
	
	$scope.categories = {}
	
	$scope.submit = ->
		categories = []
		
		for k, v of $scope.categories
			if v
				categories.push( parseInt(v) )
				
		console.log [!$scope.title, !$scope.content, categories]
		Post.save
			title: $scope.title
			content: $scope.content
			categories: categories
	] )