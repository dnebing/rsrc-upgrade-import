# Instructions

For this sample, we defined the resources-importer-external-dir property
in the liferay-plugin-package.properties file to point at the **/WEB-INF/resources-importer/**
directory.

This is the base directory where all of the version folders should be.

So create a folder in this directory for each version of your module that has an
upgrade process.

For this sample, we are declaring versions **1.0.0**, **1.1.0** and **1.1.1** so
we need the three directories. The contents are actually pretty darn simple; no
pages, just some documents (images) and web contents that use those images.

In each directory we'll have the corresponding files
needed for the Resource Importer as defined here: 
https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/importing-resources-with-a-theme

The information about the files begins at the paragraph starting with 
"All of the resources a theme uses with the resources importer".

Note that this documentation assumes that you are bundling your resource importer 
stuff with a theme, something this project does not do. So some of the instructions
in the documentation will need to be taken as *guidance*, not *literally*.


