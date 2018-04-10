package com.liferay.exportimport.resources.importer.custom.upgrade;

import com.liferay.exportimport.resources.importer.api.Importer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;

/**
 * class ResourceImporterUpgradeProcess: This is an upgrade process that should handle
 * loading resources using the Resource Importer.
 *
 * @author dnebinger
 */
public class ResourceImporterUpgradeProcess extends UpgradeProcess implements UpgradeStep {

	/**
	 * ResourceImporterUpgradeProcess: Constructor
	 * @param importer
	 * @param version
	 */
	public ResourceImporterUpgradeProcess(final Importer importer, final String version, final long adminUserId) {
		super();

		_importer = importer;
		_version = version;
		_adminUserId = adminUserId;
	}

	@Override
	protected void doUpgrade() throws Exception {

		// okay, so we should have the instance of the resource importer.

		if (_log.isDebugEnabled()) {
			_log.debug("Starting resource import for version " + _version + "...");
		}

		// before we start the upgrade, we should pretend to be an administrator...
		long currentId = PrincipalThreadLocal.getUserId();
		boolean changed = false;

		if ((currentId == 0) && (_adminUserId >= 0)) {
			PrincipalThreadLocal.setName(_adminUserId);
			changed = true;
		}

		try {
			_importer.importResources();
		} catch (Exception e) {
			_log.error("Error importing v" + _version + " resources: " + e.getMessage(), e);

			throw e;
		}

		if (changed) {
			// PrincipalThreadLocal.setName(currentId);
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Done importing v" + _version + " resources.");
		}
	}

	private final Importer _importer;
	private final String _version;
	private final long _adminUserId;

	private static final Log _log = LogFactoryUtil.getLog(ResourceImporterUpgradeProcess.class);
}
