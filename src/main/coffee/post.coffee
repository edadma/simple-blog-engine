app = angular.module( 'post', ['ngSanitize', 'ngResource'] )

app.controller( 'PostController', ['$scope', ($scope) ->
	Posts = $resource( '/api/v1/posts' )
	
	$scope.categories = {}
	
	$scope.submit = ->
		categories = []
		
		for k, v of $scope.categories
			if v
				categories.push( parseInt(v) )
				
		console.log [!$scope.title, !$scope.text, $scope.categories, categories]
	] )