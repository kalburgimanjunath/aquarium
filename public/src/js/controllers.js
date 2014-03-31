var akvaarioControllers = angular.module('akvaarioControllers', []);

akvaarioControllers.controller('akvaarioCtrl', ['$scope', '$routeParams', '$http', '$location',
  function ($scope, $routeParams, $http, $location) {
    
    /*
    Get project and contacts from API
    */
    $http.get('/api/'+$routeParams.projectId).success(function(data) {
      $scope.contacts = data["users"];
      $scope.projectName = data["project"]["name"];

      // TODO: Change this to be more angulary?
      $("#project-name").html("/ " + $scope.projectName.substring(1));  
      $("#admin-section-link").attr("href", "/admin/"+$scope.projectName);

    }).error(function(data, status, headers, config){
      console.log("API returned: " + status);
      
      if (status > 400) {
          $location.path("/404"); // path not hash
        }
      });

    /*
    Get logged in users name
    */
    $http.get('/api/user/current').success(function(data) {
      $("#loggedIn-user").html(data.user);
    });

    /*
    Get project e-mails from API
    */
    $http.get('/api/mail/'+$routeParams.projectId).success(function(data) {
      $scope.mails = data["mails"];
    });

    /*
    Get project links from API
    */
    $http.get('/api/form/links/'+$routeParams.projectId).success(function(data) {
      $scope.links = data["links"];
    });

    /*
    Get status updates from API
    */
    $http.get('/api/form/status/'+$routeParams.projectId).success(function(data) {
      $scope.reports = data["forms"];
      drawBudgetGraph(data["forms"]);
    });

    /*
    Get customer contact info from API
    */
    $http.get('/api/form/contactinfo/'+$routeParams.projectId).success(function(data) {
      $scope.customerContacts = data["forms"];
    });

    /*
    Define templates
    */
    $scope.contactsTemplate="/assets/src/partials/contacts.html";
    $scope.customerContactsTemplate="/assets/src/partials/customer-contacts.html";
    $scope.mailTemplate="/assets/src/partials/mail.html";
    $scope.linkTemplate="/assets/src/partials/links.html";
    $scope.statusTemplate="/assets/src/partials/status.html";    
  }
  ]);

akvaarioControllers.controller('notFoundCtrl', ['$scope', '$routeParams', '$http', '$location',
  function ($scope, $routeParams, $http, $location) {
    console.log("Not Found");
  }
  ]);

// Controller for redirecting users to their projects when accessing root
akvaarioControllers.controller('userProjectsCtrl', ['$scope', '$location', '$http', 
  function ($scope, $location, $http){
    $http.get('/api/user/current')
    .success(function(data){
      if (data.projects.length === 1) {
        $location.path("/"+data.projects[0]);
      } else if (data.projects.length < 1) {
        $location.path("/404");
      }
      $scope.projects = data.projects;
    });
  }]);

akvaarioControllers.controller('addCustomerCtrl', ['$scope', '$http',
  function ($scope, $http) {
    $scope.formData = {};
    $scope.processForm = function() {
      $scope.disabled = true;
      $http({
        method  : 'POST',
        url     : '/customerproject',
        data    : $.param($scope.formData),
        headers : { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      .success(function(data) {
        $scope.projectStatus = "alert alert-success";
        $scope.projectStatusMsg = "The customer has been added to the project!"
      })
      .error(function(data) {
        $scope.projectStatus = "alert alert-danger";
        $scope.projectStatusMsg = "Error: Could not add user to project!";
      });

      $http({
        method  : 'POST',
        url     : '/signup',
        data    : $.param($scope.formData),
        headers : { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      .success(function(data) {
        $scope.emailStatus = "alert alert-success";
        $scope.emailStatusMsg = "An e-mail with a registration link has been sent to the customer!";
        $scope.disabled = false;
      })
      .error(function(data) {
        $scope.emailStatus = "alert alert-danger";
        $scope.emailStatusMsg = "Error: Invitation failed!";
        $scope.disabled = false;
      });


      $scope.formData = '';
    }
  }
  ]);
