# Google Analytics quick userguide

## Loging in to Analytics

Go to [Google Analytics][1] and login with futurice google analytics
account. The login credentials can be found in FUM or you can ask 
futurice IT 

[1]: http://www.google.com/analytics

## Where is the analytics plug-in code?

The plugin can be found as a script in the beginning of the
"main.scala.html" file. Note that all google analytics scripts
in this project are implemented with "Universal Analytics"
and not with the classical google analytics language.

## Implementing tracking functionality

### Tracking "klikking"

To add follow-up functionality to a certain element in the page just add 
the “onclick” js. into any html tag that describes the given element in 
the main.html file 

	Example: ... id="project-name" onclick="ga('send', 'event', 'TTDSBNH-
	click', 'Project name clicked');"...

### Tracking user

There is a dimensionValue variable that recognizes a categorizes user as developer or customer. This can be viewed in GA under custom dimension where a userscope dimension has been implemented.

### Tracking codes used in Akvaario

<code> ga('send', 'pageview');
    	ga('send', 'action');
    	ga('send', 'event', 'page-loaded', 'Page (re)loaded');
    	ga('set', 'dimension1', dimensionValue);
    	ga('send', 'pageview');
    	onclick="ga('send', 'event', 'useless-click', 'Project name clicked');"
    	onclick="ga('send', 'event', 'useless-click', 'Akvaario clicked');"
</code> 
## Debuging

For debugging there is a useful tool provided by Google in the google
app store. It can be found [here][2]

[2]: https://chrome.google.com/webstore/detail/google-analytics-debugger/jnkmfdileelhofjcijamephohjechhna?hl=en

Note that you don't need to modify the main analytics plug-in script if
you are developing & testing locally vs. publically. Further 
elaboration is provided in the code comments. 

## Usefull links 

Here are some usefull links if analytics isin't familiar
- [Universal Analytics guide][3]
- [Custom Variables][4]
- [Event tracking][5]


[3]: https://developers.google.com/analytics/devguides/collection/upgrade/
[4]: http://stackoverflow.com/questions/16440499/how-to-set-custom-variables-through-the-new-analytics-js-of-ga
[5]: http://stackoverflow.com/questions/19959569/event-tracking-using-google-universal-analytics 