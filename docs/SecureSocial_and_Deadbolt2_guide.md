#SecureSocial and Deadbolt2

Akvaario uses SecureSocial and Deadbolt2 to handle authentication and authorisation respectively. Both are open source solutions and the source can be found on github: [Deadbolt2][1], [SecureSocial][2]. SecureSocial also has a website [here][3] that includes a simple "Getting started" section. This document is meant for developers as an introduction into the two systems and how they are used and managed in Akvaario.

[1]: https://github.com/schaloner/deadbolt-2
[2]: https://github.com/jaliss/securesocial
[3]: http://securesocial.ws/

##SecureSocial

SecureSocial is an open source solution for handling authentication and some authorisation. The former part is the one that we make extensive use of, but the much more flexible capabilities of Deadbolt2 means that the latter part is not utilised. What makes SecureSocial so attractive is the ease of integrating it and how many features this integration automatically offers. Besides some basic installation and configuration (for which instructions can be found on SecureSocial's site) the only thing that needs to be implemented is a class that extends the UserServicePlugin class (which will be discussed in more detail below). This then automatically provides registration, login and logout capabilites, and cookie and token handling. The central concepts that will be explained below are the Identity trait, SecuredActions and the UserService.

###The Identity trait

The way that SecureSocial identifies and tracks users is through the use of the Identity trait. SecureSocial offers a basic SocialUser class that already implements the Identity trait, but one could also write a custom user class as long as it implements Identity. In Akvaario we simply make use of the first alternative.

The information that the Identity contains is an identityId, the users name and email, a url for the user's avatar, the authentication method used, and password info. The identityId is made up of a combination of the the user's userid (in Akvaario this is the email for customers and the Futurice username for employees) and the authentication provider (userpass, short for userpassword, for customers and futurice for employees). If one needs to access the Identity in some part of the system this can be done with the SecureSocial.currentUser(request) method.

###SecuredAction

Instead of the standard Action in the Play framework, SecureSocial provides SecuredActions. SecuredActions simply checks if there is an authenticated user logged in when trying to perform an action. The reason why SecuredActions are convenient is that if the person making the request that the SecuredAction handles is not logged in, then he is automatically redirected to the login page.

###UserService

The UserService is the central hub for managing users and tokens. All functionality tied to retrieving and saving users and tokens is implemented in the UserService class. In Akvaario this functionality is implemented in the MongoUserService class, which extends the UserServicePlugin. The token management is quite straightforward, but when handling users one must make sure to properly differentiate between different types of users, based on the Identity provided. In case there are any changes that need to made to how users are managed in the context of SecureSocial, the UserService is where these changes should be made.

###Customizing mails
SecureSocial can send emails for registration, successfully singing up, password retrieval etc. SecureSocial provides default templates for all the different kinds of emails, but it is also possible (and often preferable) to customise the content of the emails. This is done by creating a custom template in app/view and adding it to the correct function in app.views.loginTemplate. The mails' default properties can be edited in the messages file in app/conf (for example the subject for different kinds of mails). SecureSocial also supports multilanguage options. This can be achieved by creating eg. messages.en files for each language (some exist by default).

##Deadbolt2

Deadbolt2 is an open source solution that provides both controller and template level authorisation. The github repo contains a short [guide][4] that explains how to install Deabolt2 and also gives an overview of the central concepts, but the more indepth Scala specific parts of the guide are mostly unwritten. This guide should provide enough information so that shouldn't be a problem.   
Authorisation is done through DeadboltActions, which are basically Action wrappers. For the DeadboltActions to work there are three classes that need to be implemented: a user class that extends the Subject interface, a DeadboltHandler and a DynamicResourceHandler. These will all be discussed below. For examples of the different kinds of controller and template autorisations read [this][5] page.

[4]: https://github.com/schaloner/deadbolt-2-guide
[5]: http://deadbolt-2-scala.herokuapp.com/

###Subject

The Subject interface is what is primarily used to define users within Deadbolt2. Subject works in combination with two other interfaces: Role and Permission. These interfaces are not strictly required, but must be implemented for certain DeadboltActions to work. In Akvaario this functionality has been implemented within the AkvaarioUser class. Subject requires an identifier for each user, and for AkvaarioUsers this responds to the userid associated with user's Identity (more on this in the DeadboltHandler section). Additionally the user class must have two funtions that return lists of the Roles and Permissions the user has. These functions are required when extending the Subject interface, but the user class should contain all information tied to the user that is needed by the handlers, for example which projects the user is assigned to.

###DeadboltHandler

DeadboltHandler is a trait that defines four functions: a check done before authorisation is performed, the retrieval of the current Subject, what to do when authorisation fails, and retrieving the DynamicResourceHandler (explained in greater detail in the next section). The most important function of the four is getSubject. The other three are rather straightforward, but it's important to understand getSubject's role. Inside the function should be all needed checks and lookups needed to create a Subject that contains all the information needed to perform authorisation. This is so that the DynamicResourceHandler is kept focused on determining if a Subject has the proper authorisation and does not involve itself in defining the Subject's attributes. In Akvaario, DeadboltHandler is implemented in the AkvaarioDeadboltHandler class and a handler is passed as a parameter to all DeadboltActions.

###DynamicResourceHandler

DynamicResourceHandler is a trait that defines two functions: isAllowed and checkPermission. The latter is used if the Pattern restriction is used with a custom pattern. The former allows for defining completely arbitrary rules. Akvaario implements DynamicResourceHandler in AkvaarioDynamicResourceHandler. Inside is a map that maps strings (the name of rules) to DynamicResourceHandlers, which then in turn determine the authorisation. This makes it easy to simply add as many rules as one wants. Just to remind: the DynamicResourceHandlers should only have to call on getSubject to recieve all the information they need to determine authorisation. If one notices that there is need for more information, then it should be added in AkvaarioUser and AkvaarioDeadboltHandler and not in the DynamicResourceHandler.
