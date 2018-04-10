package com.liferay.exportimport.resources.importer.custom;

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

public class VersionDefinedRIExternalDirLPPProperties extends BaseLiferayPluginPackageProperties {

	private String _version;

	public VersionDefinedRIExternalDirLPPProperties(final String version) {
		super();

		setVersion(version);
	}

	/**
	 * setVersion: Set the version string for the properties given the version.
	 * @param version
	 */
	public void setVersion(final String version) {
		// get the current dir
		String currentDir = getResourcesImporterExternalDir();

		if (Validator.isNotNull(_version)) {
			// there was a previous version, we need to drop it
			int pos = currentDir.indexOf(_version);

			if (pos >= 0) {
				currentDir = currentDir.substring(0, pos);
			}
		}

		// save our designated version string.
		_version = version;

		// call the local method to affect the actual change.
		setResourcesImporterExternalDir(currentDir);
	}

	/**
	 * setResourcesImporterExternalDir: An override method that ensures when you set the extern dir,
	 * it will tack on the designated version.
	 * @param resourcesImporterExternalDir
	 */
	@Override
	public void setResourcesImporterExternalDir(final String resourcesImporterExternalDir) {

		String externalDir = resourcesImporterExternalDir;

		// add a slash if missing
		if (! externalDir.endsWith(StringPool.SLASH)) {
			externalDir = externalDir + StringPool.SLASH;
		}

		// tack on the version dir and we're set...
		externalDir = externalDir + _version + StringPool.SLASH;

		// update the property
		// use superclass directly to not invoke this logic on the override method.
		super.setResourcesImporterExternalDir(externalDir);
	}
}
