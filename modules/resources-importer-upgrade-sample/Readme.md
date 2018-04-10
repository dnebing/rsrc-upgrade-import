# Resources Importer Upgrade Sample

This is a sample bundle that uses the resources importer API stuff to implement
an upgrade process to load resources.

The bnd.bnd file has the version number that will be tied to the bundle.

The src/resources/WEB-INF/resources-importer has a subdirectory for the various versions to process.

The com.liferay.exportimport.resources.importer.sample.ResourceImporterUpgradeStepRegistrator class
registers the individual upgrade steps.

