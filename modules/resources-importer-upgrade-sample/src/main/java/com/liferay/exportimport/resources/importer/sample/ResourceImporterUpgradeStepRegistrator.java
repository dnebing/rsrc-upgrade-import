package com.liferay.exportimport.resources.importer.sample;

import com.liferay.exportimport.resources.importer.custom.BaseLiferayPluginPackageProperties;
import com.liferay.exportimport.resources.importer.custom.BundleFriendlyImporterFactory;
import com.liferay.exportimport.resources.importer.custom.upgrade.BaseResourceImporterUpgradeStepRegistrator;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.*;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.ServletContext;

/**
 * class ResourceImporterUpgradeStepRegistrator: Sample registrator for registering
 * resource importer upgrade steps.
 *
 * @author dnebinger
 */
@Component(
		immediate = true,
		service = UpgradeStepRegistrator.class
)
public class ResourceImporterUpgradeStepRegistrator extends BaseResourceImporterUpgradeStepRegistrator {

	private static final String BUNDLE_SYMBOLIC_NAME = "com.liferay.exportimport.resources.importer.sample";
	private static final String RESOURCE_IMPORTER_EXTERNAL_DIR_PREFIX = "/WEB-INF/resources-importer/";

	private static final String V_000 = "0.0.0";
	private static final String V_100 = "1.0.0";
	private static final String V_110 = "1.1.0";
	private static final String V_111 = "1.1.1";

	/**
	 * register: Registers the upgrade steps in the registry
	 * @param registry
	 */
	@Override
	public void register(Registry registry) {

		// we need a liferay plugin package properties file
		BaseLiferayPluginPackageProperties v100Props = new BaseLiferayPluginPackageProperties();

		// update it w/ the info we want to set.
		v100Props.setName(BUNDLE_SYMBOLIC_NAME);

		// we want to load the resources to the guest group so they are publically visible.
		v100Props.setResourcesImporterTargetGuestGroup();

		// set the external dir
		v100Props.setResourcesImporterExternalDir(RESOURCE_IMPORTER_EXTERNAL_DIR_PREFIX + V_100 + '/');

		// make copies for the additional registrations
		BaseLiferayPluginPackageProperties v110Props = v100Props.clone();
		v110Props.setResourcesImporterExternalDir(RESOURCE_IMPORTER_EXTERNAL_DIR_PREFIX + V_110 + '/');

		BaseLiferayPluginPackageProperties v111Props = v100Props.clone();
		v111Props.setResourcesImporterExternalDir(RESOURCE_IMPORTER_EXTERNAL_DIR_PREFIX + V_111 + '/');

		// now we can do the registrations

		/******************************
		 * NOTE: Whatever upgrade steps you register, they will all run even if you are not at the version.
		 ******************************/

		try {
			// register 0.0.0 -> 1.0.0
			registerUpgradeStep(registry, BUNDLE_SYMBOLIC_NAME, V_000, V_100, v100Props);

			// register 1.0.0 -> 1.1.0
			registerUpgradeStep(registry, BUNDLE_SYMBOLIC_NAME, V_100, V_110, v110Props);

			// register 1.1.0 -> 1.1.1
			// registerUpgradeStep(registry, BUNDLE_SYMBOLIC_NAME, V_110, V_111, v111Props);

		} catch (Exception e) {
			_log.error("Error registering resource importer upgrade step: " + e.getMessage(), e);
		}
	}

	/**
	 * setServletContext: In order to use the resource importer, we need a servlet context.
	 * This @Reference injection, using our module's symbolic name, will inject the servlet
	 * context for this bundle.
	 * @param servletContext
	 */
	@Reference(
			target = "(osgi.web.symbolicname=com.liferay.exportimport.resources.importer.sample)",
			unbind = "-"
	)
	protected void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}

	@Reference(unbind = "-")
	protected void setBundleFriendlyImporterFactory(final BundleFriendlyImporterFactory importerFactory) {
		super.setBundleFriendlyImporterFactory(importerFactory);
	}

	@Reference(unbind = "-")
	protected void setPortal(final Portal portal) {
		super.setPortal(portal);
	}

	@Reference(unbind = "-")
	protected void setCompanyLocalService(final CompanyLocalService companyLocalService) {
		super.setCompanyLocalService(companyLocalService);
	}

	@Reference(unbind = "-")
	protected void setUserLocalService(final UserLocalService userLocalService) {
		super.setUserLocalService(userLocalService);
	}

	@Reference(unbind = "-")
	protected void setGroupLocalService(final GroupLocalService groupLocalService) {
		super.setGroupLocalService(groupLocalService);
	}

	@Reference(unbind = "-")
	protected void setRoleLocalService(final RoleLocalService roleLocalService) {
		super.setRoleLocalService(roleLocalService);
	}

	@Reference(unbind = "-")
	protected void setUserGroupLocalService(final UserGroupLocalService userGroupLocalService) {
		super.setUserGroupLocalService(userGroupLocalService);
	}
	@Reference(unbind = "-")
	protected void setOrganizationLocalService(final OrganizationLocalService organizationLocalService) {
		super.setOrganizationLocalService(organizationLocalService);
	}

	private static final Log _log = LogFactoryUtil.getLog(ResourceImporterUpgradeStepRegistrator.class);
}
