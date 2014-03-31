#Anatomy of Akvaario

This document contains a description of the structure and anatomy of the Akvaario Service.

Akvaario is based on Play Framework 2. The basic anatomy of Play 2 can be found [here](http://www.playframework.com/documentation/2.0/Anatomy). For information about the libraries see the [Akvaario README](../README.md). For more information about the authentication and authorization specifically see the [SecureSocial and Deadbolt2 guide](./SecureSocial_and_Deadbolt2_guide.md).

##Akvaario folder structure

This is the structure of the application containing all _relevant_ files and folders. Files of lesser relevance may have been omitted.

    app                           → Application sources
    └ controllers                 → Application controllers
      └ AdminController.scala     → Controller for administrative functions
      └ APIController.scala       → Controller for the API functionality
      └ Application.scala         → Front page controller
    └ models                      → Application business layer
      └ AkvaarioDAO.scala         → Database interface
      └ AkvaarioUser.scala        → Akvaario user model
      └ FumDAO.scala              → Futurice User Management interface
      └ MailClient.scala          → GMail mailbox interface
    └ plugins                     → Application plugins
      └ fixtures                  → Logic for applying test fixtures
    └ security                    → User management related logic
      └ AkvaarioDeadboltHand...   → Handler for Deadbolt authorization
      └ AkvaarioDynamicResou...   → Logic for customized authorization
      └ FuturiceOpenID.scala      → Futurice OpenID authentication interface
    └ services                    → Application services
      └ MongoUserService.scala    → SecureSocial user service with MongoDB support
    └ views                       → Templates
      └ addcustomer.scala.html    → Admin panel customer invite view
      └ admin.scala.html          → Admin panel main template
      └ contactinfo.scala.html    → Admin panel customer contact info view
      └ editlinks.scala.html      → Admin panel project links view
      └ index.scala.html          → Front page redirect control
      └ login.scala.html          → User login page
      └ loginTemplate.scala       → Authentication redirect control
      └ main.scala.html           → Front page main template
      └ signUp.scala.html         → Customer signup page
      └ status.scala.html         → Admin panel project status update view
    conf                          → Configurations files
    └ fixtures                    → Data fixtures for testing purposes
    docs                          → Akvaario documentation
    lib                           → Unmanaged libraries dependencies
    └ javax.mail.jar              → JavaMail API
    logs                          → Application logs
    mongo                         → Scripts for database interaction
    project                       → sbt configuration files
    └ build.properties            → Marker for sbt project
    └ Build.scala                 → Application build script
    └ plugins.sbt                 → sbt plugins
    public                        → Public assets
    └ images                      → Image files
    └ lib                         → JS libraries dependencies (Bootstrap, Angular.js, D3, dimple.js)
    └ src                         → Public asset sources
      └ css                       → CSS files
      └ js                        → Javascript files
        └ AddCustomer.js          → Script for handling customer invitation and addition to project
        └ app.js                  → Angular.js configuration
        └ controllers.js          → Angular.js controllers
        └ EditLinks.js            → Script for editing project related links
        └ graphics.js             → Functions for drawing graphs using dimple.js
      └ partials                  → Akvaario partial views
        └ contacts.html           → Futurice contacts partial (main view)
        └ customer-contacts.html  → Customer contacts partial (main view) 
        └ links.html              → Project links partial (main view)
        └ mail.html               → Project weekly mail partial (main view)
        └ main.html               → Main view
        └ status.html             → Project status update partial (main view)
    robot                         → Robot Framework test cases
    logs                          → Standard logs folder
    └ application.log             → Default log file
    target                        → Generated stuff
    test                          → Play Framework unit tests