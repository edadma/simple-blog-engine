app = angular.module( 'post', ['ngSanitize'] )

app.controller( 'PostController', ['$scope', ($scope) ->
	$scope.categories = {}
	
	$scope.submit = ->
		categories = []
		
		for k, v of $scope.categories
			if v
				categories.push( parseInt(v) )
				
		console.log [!$scope.title, !$scope.text, $scope.categories, categories]
	] )