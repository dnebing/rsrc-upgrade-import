# ResourcesImporter During UpgradeProcess

This workspace includes the module(s) used to demonstrate how to invoke
the ResourceImporter during an UpgradeProcess.

Why might you want to do this?  Well, if you have assets that you want to load,
you may not want them to load every time your module is redeployed during development.
Or you may have resources that need to be updated but not the full asset load.

Personally, I just wanted to ensure that if my bundle was redeployed during
development I didn't have issues with multiple versions, load errors, etc. I wanted
to control when the load was done, not Liferay, and I knew I could do that using 
an UpgradeProcess implementation.

It can also be a handy way to ensure that assets get deployed into an environment
as part of an upgrade.

Anywho, check out the corresponding blog post here for more details...

Documentation for building this out comes from the following:

Upgrade Processes: https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/creating-an-upgrade-process-for-your-app




Images used are courtesy of https://unsplash.com/ and the following photographers:
* Dominik QN
* Gabrielle Costa
* Alexandru Rotariu
* Alvaro Nino
* Clark Young
* Ferenc Horvath
* Chinda Sam
* Andrea Natali
* Alessandro Desantis
