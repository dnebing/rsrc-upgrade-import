package com.liferay.exportimport.resources.importer.custom;

import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.dynamic.data.mapping.io.DDMFormJSONDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormXSDDeserializer;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.dynamic.data.mapping.util.DDMXML;
import com.liferay.exportimport.kernel.service.ExportImportConfigurationLocalService;
import com.liferay.exportimport.kernel.service.ExportImportLocalService;
import com.liferay.exportimport.resources.importer.api.Importer;
import com.liferay.exportimport.resources.importer.api.ImporterException;
import com.liferay.exportimport.resources.importer.api.ImporterFactory;
import com.liferay.exportimport.resources.importer.api.PluginPackageProperties;
import com.liferay.exportimport.resources.importer.api.ResourcesImporterConstants;
import com.liferay.exportimport.resources.importer.portlet.preferences.PortletPreferencesTranslator;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.journal.util.JournalConverter;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactory;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutPrototypeLocalService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalService;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.service.ThemeLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.MimeTypes;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.TextFormatter;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.SAXReader;
import com.liferay.portal.search.index.IndexStatusManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * class BundleFriendlyImporterFactory: So the regular ImporterFactory kind of expects
 * that it is contained in a bundle with a registered servlet context.  However, in my
 * own experimentation, I didn't want to rely on a servlet context, let alone a registered
 * servlet context.
 * So this implementation is much more bundle-friendly, IMHO.
 *
 * @author dnebinger
 */
@Component(service = BundleFriendlyImporterFactory.class)
public class BundleFriendlyImporterFactory extends ImporterFactory {

	@Activate
	@Modified
	public void activate(final BundleContext bundleContext) {
		_bundleVersion = bundleContext.getBundle().getVersion().toString();
	}

	private String _bundleVersion;

	public Importer createImporter(
			long companyId, ServletContext servletContext,
			PluginPackageProperties pluginPackageProperties)
			throws Exception {

		String resourcesDir = pluginPackageProperties.getResourcesDir();

		Importer importer = null;

		if (Validator.isNotNull(resourcesDir)) {

			/****************************
			 * NOTE: This is different from the original.  Instead of using filesystem importer, it
			 * still returns the resource importer so resources will be processed from the bundle.
			 ****************************/
			importer = getResourceImporter();
			// end change

			importer.setJournalConverter(getJournalConverter());
			importer.setResourcesDir(resourcesDir);
		}

		if (importer == null) {
			throw new ImporterException("No valid importer found");
		}

		configureImporter(
				companyId, importer, servletContext, pluginPackageProperties);

		return importer;
	}

	protected void configureImporter(
			long companyId, Importer importer, ServletContext servletContext,
			PluginPackageProperties pluginPackageProperties)
			throws Exception {

		importer.setAppendVersion(pluginPackageProperties.isAppendVersion());
		importer.setCompanyId(companyId);
		importer.setDeveloperModeEnabled(
				pluginPackageProperties.isDeveloperModeEnabled());
		importer.setIndexAfterImport(
				pluginPackageProperties.indexAfterImport());
		importer.setServletContext(servletContext);
		importer.setServletContextName(servletContext.getServletContextName());
		importer.setTargetClassName(
				pluginPackageProperties.getTargetClassName());

		String targetValue = pluginPackageProperties.getTargetValue();

		if (Validator.isNull(targetValue)) {
			targetValue = TextFormatter.format(
					servletContext.getServletContextName(), TextFormatter.J);
		}

		importer.setTargetValue(targetValue);

		importer.setUpdateModeEnabled(
				pluginPackageProperties.isUpdateModeEnabled());

		// apply the current bundle version
		importer.setVersion(_bundleVersion);

		importer.afterPropertiesSet();
	}

	@Reference(
			cardinality = ReferenceCardinality.AT_LEAST_ONE,
			policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY,
			target = "(!(portlet.preferences.translator.portlet.id=" + ResourcesImporterConstants.PORTLET_ID_DEFAULT + "))",
			unbind = "unsetPortletPreferencesTranslators"
	)
	@Override
	protected void setPortletPreferencesTranslators(PortletPreferencesTranslator portletPreferencesTranslator, Map<String, Object> properties) {
		super.setPortletPreferencesTranslators(portletPreferencesTranslator, properties);
	}

	@Override
	protected void unsetPortletPreferencesTranslators(PortletPreferencesTranslator portletPreferencesTranslator, Map<String, Object> properties) {
		super.unsetPortletPreferencesTranslators(portletPreferencesTranslator, properties);
	}

	@Override
	@Reference
	protected void setExportImportConfigurationLocalService(ExportImportConfigurationLocalService _exportImportConfigurationLocalService) {
		super.setExportImportConfigurationLocalService(_exportImportConfigurationLocalService);
	}

	@Override
	@Reference
	protected void setExportImportLocalService(ExportImportLocalService _exportImportLocalService) {
		super.setExportImportLocalService(_exportImportLocalService);
	}

	@Reference
	@Override
	protected void setAssetTagLocalService(AssetTagLocalService _assetTagLocalService) {
		super.setAssetTagLocalService(_assetTagLocalService);
	}

	@Override
	@Reference
	protected void setDdmFormJSONDeserializer(DDMFormJSONDeserializer _ddmFormJSONDeserializer) {
		super.setDdmFormJSONDeserializer(_ddmFormJSONDeserializer);
	}

	@Override
	@Reference
	protected void setDdmFormXSDDeserializer(DDMFormXSDDeserializer _ddmFormXSDDeserializer) {
		super.setDdmFormXSDDeserializer(_ddmFormXSDDeserializer);
	}

	@Override
	@Reference
	protected void setDdmStructureLocalService(DDMStructureLocalService _ddmStructureLocalService) {
		super.setDdmStructureLocalService(_ddmStructureLocalService);
	}

	@Override
	@Reference
	protected void setDdmTemplateLocalService(DDMTemplateLocalService _ddmTemplateLocalService) {
		super.setDdmTemplateLocalService(_ddmTemplateLocalService);
	}

	@Override
	@Reference
	protected void setDdmxml(DDMXML _ddmxml) {
		super.setDdmxml(_ddmxml);
	}

	@Override
	@Reference
	protected void setDlAppLocalService(DLAppLocalService _dlAppLocalService) {
		super.setDlAppLocalService(_dlAppLocalService);
	}

	@Override
	@Reference
	protected void setDlFileEntryLocalService(DLFileEntryLocalService _dlFileEntryLocalService) {
		super.setDlFileEntryLocalService(_dlFileEntryLocalService);
	}

	@Override
	@Reference
	protected void setDlFolderLocalService(DLFolderLocalService _dlFolderLocalService) {
		super.setDlFolderLocalService(_dlFolderLocalService);
	}

	@Override
	@Reference
	protected void setGroupLocalService(GroupLocalService _groupLocalService) {
		super.setGroupLocalService(_groupLocalService);
	}

	@Override
	@Reference
	protected void setIndexerRegistry(IndexerRegistry _indexerRegistry) {
		super.setIndexerRegistry(_indexerRegistry);
	}

	@Override
	@Reference
	protected void setIndexStatusManager(IndexStatusManager _indexStatusManager) {
		super.setIndexStatusManager(_indexStatusManager);
	}

	@Override
	@Reference
	protected void setJournalArticleLocalService(JournalArticleLocalService _journalArticleLocalService) {
		super.setJournalArticleLocalService(_journalArticleLocalService);
	}

	@Override
	@Reference
	protected void setJournalConverter(JournalConverter _journalConverter) {
		super.setJournalConverter(_journalConverter);
	}

	@Override
	@Reference
	protected void setJournalFolderLocalService(JournalFolderLocalService _journalFolderLocalService) {
		super.setJournalFolderLocalService(_journalFolderLocalService);
	}

	@Override
	@Reference
	protected void setLayoutLocalService(LayoutLocalService _layoutLocalService) {
		super.setLayoutLocalService(_layoutLocalService);
	}

	@Reference
	@Override
	protected void setLayoutPrototypeLocalService(LayoutPrototypeLocalService _layoutPrototypeLocalService) {
		super.setLayoutPrototypeLocalService(_layoutPrototypeLocalService);
	}

	@Reference
	@Override
	protected void setLayoutSetLocalService(LayoutSetLocalService _layoutSetLocalService) {
		super.setLayoutSetLocalService(_layoutSetLocalService);
	}

	@Reference
	@Override
	protected void setLayoutSetPrototypeLocalService(LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService) {
		super.setLayoutSetPrototypeLocalService(_layoutSetPrototypeLocalService);
	}

	@Reference
	@Override
	protected void setMimeTypes(MimeTypes _mimeTypes) {
		super.setMimeTypes(_mimeTypes);
	}

	@Reference
	@Override
	protected void setPortal(Portal _portal) {
		super.setPortal(_portal);
	}

	@Reference
	@Override
	protected void setPortletPreferencesFactory(PortletPreferencesFactory _portletPreferencesFactory) {
		super.setPortletPreferencesFactory(_portletPreferencesFactory);
	}

	@Reference
	@Override
	protected void setPortletPreferencesLocalService(PortletPreferencesLocalService _portletPreferencesLocalService) {
		super.setPortletPreferencesLocalService(_portletPreferencesLocalService);
	}

	@Reference(
			target = "(portlet.preferences.translator.portlet.id=" + ResourcesImporterConstants.PORTLET_ID_DEFAULT + ")"
	)
	@Override
	protected void setPortletPreferencesTranslator(PortletPreferencesTranslator _portletPreferencesTranslator) {
		super.setPortletPreferencesTranslator(_portletPreferencesTranslator);
	}

	@Reference
	@Override
	protected void setRepositoryLocalService(RepositoryLocalService _repositoryLocalService) {
		super.setRepositoryLocalService(_repositoryLocalService);
	}

	@Reference
	@Override
	protected void setSaxReader(SAXReader _saxReader) {
		super.setSaxReader(_saxReader);
	}

	@Reference
	@Override
	protected void setThemeLocalService(ThemeLocalService _themeLocalService) {
		super.setThemeLocalService(_themeLocalService);
	}

	@Reference
	@Override
	protected void setUserLocalService(UserLocalService _userLocalService) {
		super.setUserLocalService(_userLocalService);
	}
}
