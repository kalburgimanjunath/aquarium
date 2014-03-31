var akvaario = angular.module('akvaario', [
  'ngRoute',
  'akvaarioControllers',
  'ui.bootstrap'
  ]);

akvaario.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/404', {
      templateUrl: '/assets/src/partials/404.html',
      controller: 'notFoundCtrl'
    }).
    when('/addcustomer', {
      templateUrl: '/assets/src/partials/add_user.html',
      controller: 'addCustomerCtrl'
    }).
    when('/:projectId', {
      templateUrl: '/assets/src/partials/main.html',
      controller: 'akvaarioCtrl'
    }).
    when('/', {
      templateUrl: '/assets/src/partials/list.html',
      controller: 'userProjectsCtrl'
    }).
    otherwise({
      redirectTo: '/'
    });
  }]);

akvaario.directive('errSrc', function() {
  return {
    link: function(scope, element, attrs) {
      element.bind('error', function() {
        element.attr('src', attrs.errSrc);
      });
    }
  }
});