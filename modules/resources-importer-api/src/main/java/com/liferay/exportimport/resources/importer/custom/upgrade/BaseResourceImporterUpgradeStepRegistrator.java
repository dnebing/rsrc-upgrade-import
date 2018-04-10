package com.liferay.exportimport.resources.importer.custom.upgrade;

import com.liferay.exportimport.resources.importer.api.Importer;
import com.liferay.exportimport.resources.importer.api.PluginPackageProperties;
import com.liferay.exportimport.resources.importer.custom.BundleFriendlyImporterFactory;
import com.liferay.exportimport.resources.importer.custom.BaseLiferayPluginPackageProperties;
import com.liferay.exportimport.resources.importer.custom.context.LiferayPluginPackageCustomizingServletContext;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;

/**
 * class BaseResourceImporterUpgradeStepRegistrator: Base registrator for registering
 * resource importer upgrade steps.
 *
 * @author dnebinger
 */
public abstract class BaseResourceImporterUpgradeStepRegistrator implements UpgradeStepRegistrator {

	/**
	 * registerUpgradeStep: Utility method to register an upgrade step w/ the given details.
	 * @param registry
	 * @param bundleSymbolicName
	 * @param versionFrom
	 * @param versionTo
	 * @param lppProperties
	 * @throws PortalException
	 */
	protected void registerUpgradeStep(final Registry registry, final String bundleSymbolicName, final String versionFrom, final String versionTo,
	                                   final BaseLiferayPluginPackageProperties lppProperties) throws PortalException {

		// use the default company for loading.
		long companyId = getDefaultCompanyId();

		// we should derive a user id from the company.
		long userId = getCompanyAdminUserId(companyId);

		// complete registration
		registerUpgradeStep(registry, bundleSymbolicName, versionFrom, versionTo, companyId, userId, lppProperties);
	}

	/**
	 * registerUpgradeStep: Utility method to register an upgrade step w/ the given details.
	 * @param registry
	 * @param bundleSymbolicName
	 * @param versionFrom
	 * @param versionTo
	 * @param companyId
	 * @param lppProperties
	 * @throws PortalException
	 */
	protected void registerUpgradeStep(final Registry registry, final String bundleSymbolicName, final String versionFrom, final String versionTo,
	                                   final long companyId, final BaseLiferayPluginPackageProperties lppProperties) throws PortalException {

		// we should derive a user id from the company.
		long userId = getCompanyAdminUserId(companyId);

		// complete registration
		registerUpgradeStep(registry, bundleSymbolicName, versionFrom, versionTo, companyId, userId, lppProperties);
	}

	/**
	 * registerUpgradeStep: Utility method to register an upgrade step w/ the given details.
	 * @param registry
	 * @param bundleSymbolicName
	 * @param versionFrom
	 * @param versionTo
	 * @param companyId
	 * @param userId
	 * @param lppProperties
	 * @throws PortalException
	 */
	protected void registerUpgradeStep(final Registry registry, final String bundleSymbolicName, final String versionFrom, final String versionTo,
	                                   final long companyId, final long userId, final BaseLiferayPluginPackageProperties lppProperties) throws PortalException {

		// let's create the upgrade step
		UpgradeStep step = createResourceImporterUpgradeStep(versionTo, companyId, userId, lppProperties);

		// now that we have the step, we can register
		registry.register(bundleSymbolicName, versionFrom, versionTo, step);
	}

	/**
	 * createResourceImporterUpgradeStep: Creates a resource importer upgrade step for the given company and version.
	 * @param version This is the version to load resources from.  You would have a corresponding folder w/ this version number to load from.
	 * @param companyId The company id to load the resources into.
	 * @param userId The user id to become to load the resources as.
	 * @return UpgradeStep The instance to register.
	 * @throws PortalException
	 */
	protected UpgradeStep createResourceImporterUpgradeStep(final String version, final long companyId, final long userId, final BaseLiferayPluginPackageProperties lppProperties) throws PortalException {
		// okay, so we need to wrap the servlet context for our version...
		LiferayPluginPackageCustomizingServletContext servletContext = new LiferayPluginPackageCustomizingServletContext(_servletContext, lppProperties);

		// since we have the context, we can get the liferay-plugin-package.properties object instance.
		PluginPackageProperties pluginPackageProperties = null;
		try {
			pluginPackageProperties = new PluginPackageProperties(servletContext);
		} catch (IOException e) {
			_log.error("Error importing the properties: " + e.getMessage(), e);

			throw new PortalException("Failed importing resources importer properties.", e);
		}

		// since we have the context, we can now create an importer
		Importer importer = null;
		try {
			importer = _importerFactory.createImporter(companyId, servletContext, pluginPackageProperties);
		} catch (Exception e) {
			_log.error("Failed in creation of Resources Importer instance:  " + e.getMessage(), e);

			throw new PortalException("Failed to create Resources Importer instance.", e);
		}

		// now that we have the importer, we can create the upgrade step.
		return new ResourceImporterUpgradeProcess(importer, version, userId);
	}

	/**
	 * getDefaultCompanyId: Return the default company id.
	 * @return long The default company id.
	 */
	protected long getDefaultCompanyId() {
		long companyId = _portal.getDefaultCompanyId();

		return companyId;
	}

	/**
	 * getDefaultCompanyAdminUserId: Return an admin user id for the default company id.
	 * @return long The admin user id.
	 */
	protected long getDefaultCompanyAdminUserId() {
		return getCompanyAdminUserId(getDefaultCompanyId());
	}

	/**
	 * getCompanyAdminUserId: Return an admin user id for the given company id.
	 * @param companyId
	 * @return long The admin user id.
	 */
	protected long getCompanyAdminUserId(final long companyId) {
		Company company = _companyLocalService.fetchCompany(companyId);

		return getCompanyAdminUserId(company);
	}

	/**
	 * getCompanyAdminUserId: Finds a user id who is an admin for the given company.
	 * @param company
	 * @return long The admin user id.
	 * @throws PortalException
	 */
	protected long getCompanyAdminUserId(Company company) {
		if (Validator.isNull(company)) {
			// no company, nothing to find.
			return -1;
		}

		Role role = null;
		try {
			role = _roleLocalService.getRole(company.getCompanyId(), RoleConstants.ADMINISTRATOR);
		} catch (PortalException e) {
			_log.error("Error fetching admin role for company: " + e.getMessage(), e);

			return -1;
		}

		long[] userIds = _userLocalService.getRoleUserIds(role.getRoleId());

		long activeUserId = getActiveUserIdFromArray(userIds);

		if (activeUserId != -1) {
			return activeUserId;
		}

		List<Group> groups = _groupLocalService.getRoleGroups(role.getRoleId());

		for (Group group : groups) {
			if (group.isOrganization()) {
				userIds = _organizationLocalService.getUserPrimaryKeys(group.getClassPK());

				activeUserId = getActiveUserIdFromArray(userIds);

				if (activeUserId != -1) {
					return activeUserId;
				}
			} else if (group.isRegularSite()) {
				userIds = _groupLocalService.getUserPrimaryKeys(group.getGroupId());

				activeUserId = getActiveUserIdFromArray(userIds);

				if (activeUserId != -1) {
					return activeUserId;
				}
			} else if (group.isUserGroup()) {
				userIds = _userGroupLocalService.getUserPrimaryKeys(group.getClassPK());

				activeUserId = getActiveUserIdFromArray(userIds);

				if (activeUserId != -1) {
					return activeUserId;
				}
			}
		}

		_log.error("Unable to find an administrator user in company " + company.getCompanyId());

		return -1;
	}

	/**
	 * getActiveUserIdFromArray: Find an active admin user id from the given array.
	 * @param userIds
	 * @return long An active user id or <code>-1</code> if there isn't one.
	 */
	private long getActiveUserIdFromArray(long[] userIds) {
		if (!ArrayUtil.isEmpty(userIds)) {
			for (long id : userIds) {
				if (isActive(id)) {
					return id;
				}
			}
		}

		return -1;
	}

	/**
	 * isActive: Utility method to determine if the found user would be active
	 * and not the default user.
	 * @param userId
	 * @return boolean <code>true</code> if the user is active and not default.
	 */
	protected boolean isActive(final long userId) {
		User user = _userLocalService.fetchUser(userId);

		if (Validator.isNull(user)) {
			return false;
		}

		if (!user.isActive()) return false;

		if (Validator.isNull(user.getFirstName()) || Validator.isNull(user.getLastName())) {
			return false;
		}

		// we also want to skip the default user
		return ! user.isDefaultUser();
	}

	/**
	 * setServletContext: In order to use the resource importer, we need a servlet context.
	 * @param servletContext
	 */
	protected void setServletContext(ServletContext servletContext) {
		_servletContext = servletContext;
	}

	protected void setBundleFriendlyImporterFactory(final BundleFriendlyImporterFactory importerFactory) {
		_importerFactory = importerFactory;
	}

	protected void setPortal(final Portal portal) {
		_portal = portal;
	}

	protected void setCompanyLocalService(final CompanyLocalService companyLocalService) {
		_companyLocalService = companyLocalService;
	}

	protected void setUserLocalService(final UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	protected void setGroupLocalService(final GroupLocalService groupLocalService) {
		_groupLocalService = groupLocalService;
	}

	protected void setRoleLocalService(final RoleLocalService roleLocalService) {
		_roleLocalService = roleLocalService;
	}

	protected void setUserGroupLocalService(final UserGroupLocalService userGroupLocalService) {
		_userGroupLocalService = userGroupLocalService;
	}

	protected void setOrganizationLocalService(final OrganizationLocalService organizationLocalService) {
		_organizationLocalService = organizationLocalService;
	}

	protected ServletContext getServletContext() {
		return _servletContext;
	}

	protected BundleFriendlyImporterFactory getImporterFactory() {
		return _importerFactory;
	}

	protected Portal getPortal() {
		return _portal;
	}

	protected CompanyLocalService getCompanyLocalService() {
		return _companyLocalService;
	}

	protected UserLocalService getUserLocalService() {
		return _userLocalService;
	}

	protected GroupLocalService getGroupLocalService() {
		return _groupLocalService;
	}

	protected RoleLocalService getRoleLocalService() {
		return _roleLocalService;
	}

	protected OrganizationLocalService getOrganizationLocalService() {
		return _organizationLocalService;
	}

	protected UserGroupLocalService getUserGroupLocalService() {
		return _userGroupLocalService;
	}

	private ServletContext _servletContext;
	private BundleFriendlyImporterFactory _importerFactory;
	private Portal _portal;
	private CompanyLocalService _companyLocalService;
	private UserLocalService _userLocalService;
	private GroupLocalService _groupLocalService;
	private RoleLocalService _roleLocalService;
	private OrganizationLocalService _organizationLocalService;
	private UserGroupLocalService _userGroupLocalService;

	private static final Log _log = LogFactoryUtil.getLog(BaseResourceImporterUpgradeStepRegistrator.class);
}
