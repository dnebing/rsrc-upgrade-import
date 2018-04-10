package com.liferay.exportimport.resources.importer.custom.context;

import com.liferay.exportimport.resources.importer.custom.BaseLiferayPluginPackageProperties;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * class LiferayPluginPackageCustomizingServletContext: A servlet context wrapper that will return the custom input streams
 * for the liferay plugin package properties object data.
 *
 * @author dnebinger
 */
public class LiferayPluginPackageCustomizingServletContext extends ServletContextWrapper {

	private static final String LIFERAY_PLUGIN_PACKAGE_PROPERTIES = "/WEB-INF/liferay-plugin-package.properties";
	private BaseLiferayPluginPackageProperties _lppProperties;

	/**
	 * LiferayPluginPackageCustomizingServletContext: Constructor
	 * @param servletContext
	 * @param lppProperties
	 */
	public LiferayPluginPackageCustomizingServletContext(final ServletContext servletContext, final BaseLiferayPluginPackageProperties lppProperties) {
		super(servletContext);

		_lppProperties = lppProperties;
	}

	/**
	 * getResourceAsStream: Returns an InputStream to the
	 * @param path
	 * @return
	 */
	@Override
	public InputStream getResourceAsStream(String path) {
		// if we are not retrieving the properties file, just give it back.
		if (! LIFERAY_PLUGIN_PACKAGE_PROPERTIES.equals(path)) {
			return super.getResourceAsStream(path);
		}

		if (_log.isDebugEnabled()) {
			_log.info("Returning InputStream to custom liferay plugin package object data.");
		}

		// we need an return an InputStream object
		return _lppProperties.getPropertiesInputStream();
	}

	private static final Log _log = LogFactoryUtil.getLog(LiferayPluginPackageCustomizingServletContext.class);
}
