package com.liferay.exportimport.resources.importer.custom;

import com.liferay.exportimport.resources.importer.api.ImporterFactory;
import com.liferay.exportimport.resources.importer.custom.util.StringInputStream;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * class BaseLiferayPluginPackageProperties: This is a base class that can be used
 * as a subsitute for a liferay-plugin-package.properties file in the bundle.  The
 * goal here is to provide an OSGi-friendly way of overriding the property keys/values
 * that will be used to configure the Resources Importer.
 *
 * @author dnebinger
 */
public class BaseLiferayPluginPackageProperties {

	public static final String AUTHOR = "author";
	public static final String CHANGE_LOG = "change-log";
	public static final String LICENSES = "licenses";
	public static final String LIFERAY_VERSIONS = "liferay-versions";
	public static final String LONG_DESCRIPTION = "long-description";
	public static final String MODULE_GROUP_ID = "module-group-id";
	public static final String MODULE_INCREMENTAL_VERSION = "module-incremental-version";
	public static final String NAME = "name";
	public static final String PAGE_URL = "page-url";
	public static final String SHORT_DESCRIPTION = "short-description";
	public static final String TAGS = "tags";
	public static final String RESOURCES_IMPORTER_EXTERNAL_DIR = "resources-importer-external-dir";
	public static final String RESOURCES_IMPORTER_TARGET_CLASS_NAME = "resources-importer-target-class-name";
	public static final String RESOURCES_IMPORTER_TARGET_VALUE = "resources-importer-target-value";
	public static final String RESOURCES_IMPORTER_INDEX_AFTER_IMPORT = "resources-importer-index-after-import";
	public static final String RESOURCES_IMPORTER_APPEND_VERSION = "resources-importer-append-version";
	public static final String RESOURCES_IMPORTER_DEVELOPER_MODE_ENABLED = "resources-importer-developer-mode-enabled";
	public static final String RESOURCES_IMPORTER_UPDATE_MODE_ENABLED = "resources-importer-update-mode-enabled";

	private Properties _properties = new Properties();

	public BaseLiferayPluginPackageProperties() {

		// set up the initial set of properties that you might find in a normal l-p-p.props file.
		_properties.setProperty(AUTHOR, "Liferay, Inc.");
		_properties.setProperty(CHANGE_LOG,"");
		_properties.setProperty(LICENSES, "LGPL");
		_properties.setProperty(LIFERAY_VERSIONS, "7.0.1+,7.1.0+");
		_properties.setProperty(LONG_DESCRIPTION, "");
		_properties.setProperty(MODULE_GROUP_ID, "liferay");
		_properties.setProperty(MODULE_INCREMENTAL_VERSION, "1");
		_properties.setProperty(NAME, "Sample");
		_properties.setProperty(PAGE_URL,"http://www.liferay.com");
		_properties.setProperty(SHORT_DESCRIPTION, "");
		_properties.setProperty(TAGS, "");
	}

	/**
	 * getPropertiesInputStream: When the properties are ready, the input stream of the properties
	 * is used by the RI for configuration.  Since RI expects to get this stream via an embedded
	 * liferay-plugin-package.properties file, this method will expose the data via an IS to the
	 * instance property data.
	 * @return InputStream The stream to use.
	 */
	public InputStream getPropertiesInputStream() {
		// return to a string
		String propertiesString = PropertiesUtil.toString(_properties);

		// and we need an return an InputStream object
		return new StringInputStream(propertiesString);
	}

	/**
	 * BaseLiferayPluginPackageProperties: Constructor that uses the given properties
	 * as a set of overrides.
	 * @param properties
	 */
	public BaseLiferayPluginPackageProperties(Properties properties) {
		this();

		updateProperties(properties);
	}

	/**
	 * clone: Clones the instance and the properties.
	 * @return BaseLiferayPluginPackageProperties The clone.
	 */
	public BaseLiferayPluginPackageProperties clone() {
		BaseLiferayPluginPackageProperties props = new BaseLiferayPluginPackageProperties(_properties);

		return props;
	}

	/**
	 * updateProperties: Updates the internal properties from the given props object.
	 * Can be used, for example, in an OSGi @Activate implementation to pass thru
	 * the component properties to this method for twaking property values.
	 * @param properties
	 */
	protected void updateProperties(final Properties properties) {
		// update from the given properties...
		if ((Validator.isNotNull(properties)) && (! properties.isEmpty())) {
			String key, value;

			Enumeration keyEnum = properties.propertyNames();

			while (keyEnum.hasMoreElements()) {
				key = (String) keyEnum.nextElement();
				value = properties.getProperty(key);

				_properties.setProperty(key, value);
			}
		}
	}

	/**
	 * setResourcesImporterTargetGuestGroup: Utility method to target the guest group
	 * for resources importer load.
	 */
	public void setResourcesImporterTargetGuestGroup() {
		setResourcesImporterTargetGroup(GroupConstants.GUEST);
	}

	/**
	 * setResourcesImporterTargetGroup: Sets a target group to load resources to.
	 * @param groupName
	 */
	public void setResourcesImporterTargetGroup(String groupName) {
		setResourcesImporterTargetClassName(Group.class.getName());
		setResourcesImporterTargetValue(groupName);
	}

	public String getAuther() {
		return _properties.getProperty(AUTHOR);
	}

	public void setAuther(String auther) {
		_properties.setProperty(AUTHOR, auther);
	}

	public String getChangeLog() {
		return _properties.getProperty(CHANGE_LOG);
	}

	public void setChangeLog(String changeLog) {
		_properties.setProperty(CHANGE_LOG, changeLog);
	}

	public String getLicenses() {
		return _properties.getProperty(LICENSES);
	}

	public void setLicenses(String licenses) {
		_properties.setProperty(LICENSES, licenses);
	}

	public String getLiferayVersions() {
		return _properties.getProperty(LIFERAY_VERSIONS);
	}

	public void setLiferayVersions(String liferayVersions) {
		_properties.setProperty(LIFERAY_VERSIONS, liferayVersions);
	}

	public String getLongDescription() {
		return _properties.getProperty(LONG_DESCRIPTION);
	}

	public void setLongDescription(String longDescription) {
		_properties.setProperty(LONG_DESCRIPTION, longDescription);
	}

	public String getModuleGroupId() {
		return _properties.getProperty(MODULE_GROUP_ID);
	}

	public void setModuleGroupId(String moduleGroupId) {
		_properties.setProperty(MODULE_GROUP_ID, moduleGroupId);
	}

	public String getModuleIncrementalVersion() {
		return _properties.getProperty(MODULE_INCREMENTAL_VERSION);
	}

	public void setModuleIncrementalVersion(String moduleIncrementalVersion) {
		_properties.setProperty(MODULE_INCREMENTAL_VERSION, moduleIncrementalVersion);
	}

	public String getName() {
		return _properties.getProperty(NAME);
	}

	public void setName(String name) {
		_properties.setProperty(NAME, name);
	}

	public String getPageUrl() {
		return _properties.getProperty(PAGE_URL);
	}

	public void setPageUrl(String pageUrl) {
		_properties.setProperty(PAGE_URL, pageUrl);
	}

	public String getShortDescription() {
		return _properties.getProperty(SHORT_DESCRIPTION);
	}

	public void setShortDescription(String shortDescription) {
		_properties.setProperty(SHORT_DESCRIPTION, shortDescription);
	}

	public String getTags() {
		return _properties.getProperty(TAGS);
	}

	public void setTags(String tags) {
		_properties.setProperty(TAGS, tags);
	}

	public String getResourcesImporterExternalDir() {
		// if the property value is null, we will return the default value as determined by the
		// ImporterFactory.

		return GetterUtil.getString(_properties.getProperty(RESOURCES_IMPORTER_EXTERNAL_DIR), ImporterFactory.RESOURCES_DIR);
	}

	public void setResourcesImporterExternalDir(String resourcesImporterExternalDir) {
		_properties.setProperty(RESOURCES_IMPORTER_EXTERNAL_DIR, resourcesImporterExternalDir);
	}

	public String getResourcesImporterTargetClassName() {
		return _properties.getProperty(RESOURCES_IMPORTER_TARGET_CLASS_NAME);
	}

	public void setResourcesImporterTargetClassName(String resourcesImporterTargetClassName) {
		_properties.setProperty(RESOURCES_IMPORTER_TARGET_CLASS_NAME, resourcesImporterTargetClassName);
	}

	public String getResourcesImporterTargetValue() {
		return _properties.getProperty(RESOURCES_IMPORTER_TARGET_VALUE);
	}

	public void setResourcesImporterTargetValue(String resourcesImporterTargetValue) {
		_properties.setProperty(RESOURCES_IMPORTER_TARGET_VALUE, resourcesImporterTargetValue);
	}

	public boolean isResourcesImporterIndexAfterImport() {
		return GetterUtil.getBoolean(_properties.getProperty(RESOURCES_IMPORTER_INDEX_AFTER_IMPORT), true);
	}

	public void setResourcesImporterIndexAfterImport(boolean resourcesImporterIndexAfterImport) {
		_properties.setProperty(RESOURCES_IMPORTER_INDEX_AFTER_IMPORT, String.valueOf(resourcesImporterIndexAfterImport));
	}

	public boolean isResourcesImporterAppendVersion() {
		return GetterUtil.getBoolean(_properties.getProperty(RESOURCES_IMPORTER_APPEND_VERSION), true);
	}

	public void setResourcesImporterAppendVersion(boolean resourcesImporterAppendVersion) {
		_properties.setProperty(RESOURCES_IMPORTER_APPEND_VERSION, String.valueOf(resourcesImporterAppendVersion));
	}

	public boolean isResourcesImporterDeveloperModeEnabled() {
		return GetterUtil.getBoolean(_properties.getProperty(RESOURCES_IMPORTER_DEVELOPER_MODE_ENABLED));
	}

	public void setResourcesImporterDeveloperModeEnabled(boolean resourcesImporterDeveloperModeEnabled) {
		_properties.setProperty(RESOURCES_IMPORTER_DEVELOPER_MODE_ENABLED, String.valueOf(resourcesImporterDeveloperModeEnabled));
	}

	public boolean isResourcesImporterUpdateModeEnabled() {
		return GetterUtil.getBoolean(_properties.getProperty(RESOURCES_IMPORTER_UPDATE_MODE_ENABLED));
	}

	public void setResourcesImporterUpdateModeEnabled(boolean resourcesImporterUpdateModeEnabled) {
		_properties.setProperty(RESOURCES_IMPORTER_UPDATE_MODE_ENABLED, String.valueOf(resourcesImporterUpdateModeEnabled));
	}
}
