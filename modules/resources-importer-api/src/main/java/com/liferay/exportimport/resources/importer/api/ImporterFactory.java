/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.exportimport.resources.importer.api;

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
import com.liferay.exportimport.resources.importer.portlet.preferences.PortletPreferencesTranslator;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.journal.util.JournalConverter;
import com.liferay.portal.kernel.deploy.DeployManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.plugin.PluginPackage;
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
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypes;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.TextFormatter;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.SAXReader;
import com.liferay.portal.search.index.IndexStatusManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.servlet.ServletContext;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dnebinger
 */
@Component(service = ImporterFactory.class)
public class ImporterFactory {

	public static final String RESOURCES_DIR =
		"/WEB-INF/classes/resources-importer/";

	public static final String TEMPLATES_DIR =
		"/WEB-INF/classes/templates-importer/";

	public Importer createImporter(
			long companyId, ServletContext servletContext,
			PluginPackageProperties pluginPackageProperties)
		throws Exception {

		String resourcesDir = pluginPackageProperties.getResourcesDir();

		Set<String> resourcePaths = servletContext.getResourcePaths(
			RESOURCES_DIR);
		Set<String> templatePaths = servletContext.getResourcePaths(
			TEMPLATES_DIR);

		URL privateLARURL = null;
		URL publicLARURL = servletContext.getResource(
			RESOURCES_DIR.concat("archive.lar"));

		if (publicLARURL == null) {
			privateLARURL = servletContext.getResource(
				RESOURCES_DIR.concat("private.lar"));

			publicLARURL = servletContext.getResource(
				RESOURCES_DIR.concat("public.lar"));
		}

		Importer importer = null;

		if ((privateLARURL != null) || (publicLARURL != null)) {
			LARImporter larImporter = getLARImporter();

			URLConnection privateLARURLConnection = null;

			if (privateLARURL != null) {
				privateLARURLConnection = privateLARURL.openConnection();

				larImporter.setPrivateLARInputStream(
					privateLARURLConnection.getInputStream());
			}

			URLConnection publicLARURLConnection = null;

			if (publicLARURL != null) {
				publicLARURLConnection = publicLARURL.openConnection();

				larImporter.setPublicLARInputStream(
					publicLARURLConnection.getInputStream());
			}

			importer = larImporter;
		}
		else if ((resourcePaths != null) && !resourcePaths.isEmpty()) {
			importer = getResourceImporter();

			importer.setJournalConverter(_journalConverter);
			importer.setResourcesDir(RESOURCES_DIR);
		}
		else if ((templatePaths != null) && !templatePaths.isEmpty()) {
			importer = getResourceImporter();

			Group group = _groupLocalService.getCompanyGroup(companyId);

			importer.setGroupId(group.getGroupId());

			importer.setJournalConverter(_journalConverter);
			importer.setResourcesDir(TEMPLATES_DIR);
		}
		else if (Validator.isNotNull(resourcesDir)) {
			importer = getFileSystemImporter();

			importer.setJournalConverter(_journalConverter);
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

		PluginPackage pluginPackage =
			DeployManagerUtil.getInstalledPluginPackage(
				servletContext.getServletContextName());

		importer.setVersion(pluginPackage.getVersion());

		importer.afterPropertiesSet();
	}

	protected FileSystemImporter getFileSystemImporter() {
		return new FileSystemImporter(
			_assetTagLocalService, _ddmFormJSONDeserializer,
			_ddmFormXSDDeserializer, _ddmStructureLocalService,
			_ddmTemplateLocalService, _ddmxml, _dlAppLocalService,
			_dlFileEntryLocalService, _dlFolderLocalService, _groupLocalService,
			_indexStatusManager, _indexerRegistry, _journalArticleLocalService,
			_journalFolderLocalService, _layoutLocalService,
			_layoutPrototypeLocalService, _layoutSetLocalService,
			_layoutSetPrototypeLocalService, _mimeTypes, _portal,
			_portletPreferencesFactory, _portletPreferencesLocalService,
			_portletPreferencesTranslator, _portletPreferencesTranslators,
			_repositoryLocalService, _saxReader, _themeLocalService, _userLocalService);
	}

	protected LARImporter getLARImporter() {
		return new LARImporter(_exportImportConfigurationLocalService, _exportImportLocalService,
				_groupLocalService, _layoutLocalService, _layoutPrototypeLocalService,
				_layoutSetPrototypeLocalService, _userLocalService);
	}

	protected ResourceImporter getResourceImporter() {
		return new ResourceImporter(
			_assetTagLocalService, _ddmFormJSONDeserializer,
			_ddmFormXSDDeserializer, _ddmStructureLocalService,
			_ddmTemplateLocalService, _ddmxml, _dlAppLocalService,
			_dlFileEntryLocalService, _dlFolderLocalService, _groupLocalService,
			_indexStatusManager, _indexerRegistry, _journalArticleLocalService,
			_journalFolderLocalService, _layoutLocalService,
			_layoutPrototypeLocalService, _layoutSetLocalService,
			_layoutSetPrototypeLocalService, _mimeTypes, _portal,
			_portletPreferencesFactory, _portletPreferencesLocalService,
			_portletPreferencesTranslator, _portletPreferencesTranslators,
			_repositoryLocalService, _saxReader, _themeLocalService, _userLocalService);
	}

	@Reference(
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(!(portlet.preferences.translator.portlet.id=" + ResourcesImporterConstants.PORTLET_ID_DEFAULT + "))",
		unbind = "unsetPortletPreferencesTranslators"
	)
	protected void setPortletPreferencesTranslators(
		PortletPreferencesTranslator portletPreferencesTranslator,
		Map<String, Object> properties) {

		String rootPortletId = GetterUtil.getString(
			properties.get("portlet.preferences.translator.portlet.id"));

		if (Validator.isNotNull(rootPortletId)) {
			_portletPreferencesTranslators.put(
				rootPortletId, portletPreferencesTranslator);
		}

		if (_log.isWarnEnabled()) {
			_log.warn(
				"The property \"portlet.preferences.translator.portlet.id\" " +
					"is null");
		}
	}

	protected void unsetPortletPreferencesTranslators(
		PortletPreferencesTranslator portletPreferencesTranslator,
		Map<String, Object> properties) {

		String rootPortletId = GetterUtil.getString(
			properties.get("rootPortletId"));

		if (Validator.isNull(rootPortletId)) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"No rootPortletId defined for service: " +
						portletPreferencesTranslator);
			}

			return;
		}

		_portletPreferencesTranslators.remove(rootPortletId);
	}

	protected ExportImportConfigurationLocalService getExportImportConfigurationLocalService() {
		return _exportImportConfigurationLocalService;
	}

	@Reference
	protected void setExportImportConfigurationLocalService(ExportImportConfigurationLocalService _exportImportConfigurationLocalService) {
		this._exportImportConfigurationLocalService = _exportImportConfigurationLocalService;
	}

	protected ExportImportLocalService getExportImportLocalService() {
		return _exportImportLocalService;
	}

	@Reference
	protected void setExportImportLocalService(ExportImportLocalService _exportImportLocalService) {
		this._exportImportLocalService = _exportImportLocalService;
	}

	protected AssetTagLocalService getAssetTagLocalService() {
		return _assetTagLocalService;
	}

	@Reference
	protected void setAssetTagLocalService(AssetTagLocalService _assetTagLocalService) {
		this._assetTagLocalService = _assetTagLocalService;
	}

	protected DDMFormJSONDeserializer getDdmFormJSONDeserializer() {
		return _ddmFormJSONDeserializer;
	}

	@Reference
	protected void setDdmFormJSONDeserializer(DDMFormJSONDeserializer _ddmFormJSONDeserializer) {
		this._ddmFormJSONDeserializer = _ddmFormJSONDeserializer;
	}

	protected DDMFormXSDDeserializer getDdmFormXSDDeserializer() {
		return _ddmFormXSDDeserializer;
	}

	@Reference
	protected void setDdmFormXSDDeserializer(DDMFormXSDDeserializer _ddmFormXSDDeserializer) {
		this._ddmFormXSDDeserializer = _ddmFormXSDDeserializer;
	}

	protected DDMStructureLocalService getDdmStructureLocalService() {
		return _ddmStructureLocalService;
	}

	@Reference
	protected void setDdmStructureLocalService(DDMStructureLocalService _ddmStructureLocalService) {
		this._ddmStructureLocalService = _ddmStructureLocalService;
	}

	protected DDMTemplateLocalService getDdmTemplateLocalService() {
		return _ddmTemplateLocalService;
	}

	@Reference
	protected void setDdmTemplateLocalService(DDMTemplateLocalService _ddmTemplateLocalService) {
		this._ddmTemplateLocalService = _ddmTemplateLocalService;
	}

	protected DDMXML getDdmxml() {
		return _ddmxml;
	}

	@Reference
	protected void setDdmxml(DDMXML _ddmxml) {
		this._ddmxml = _ddmxml;
	}

	protected DLAppLocalService getDlAppLocalService() {
		return _dlAppLocalService;
	}

	@Reference
	protected void setDlAppLocalService(DLAppLocalService _dlAppLocalService) {
		this._dlAppLocalService = _dlAppLocalService;
	}

	protected DLFileEntryLocalService getDlFileEntryLocalService() {
		return _dlFileEntryLocalService;
	}

	@Reference
	protected void setDlFileEntryLocalService(DLFileEntryLocalService _dlFileEntryLocalService) {
		this._dlFileEntryLocalService = _dlFileEntryLocalService;
	}

	protected DLFolderLocalService getDlFolderLocalService() {
		return _dlFolderLocalService;
	}

	@Reference
	protected void setDlFolderLocalService(DLFolderLocalService _dlFolderLocalService) {
		this._dlFolderLocalService = _dlFolderLocalService;
	}

	protected GroupLocalService getGroupLocalService() {
		return _groupLocalService;
	}

	@Reference
	protected void setGroupLocalService(GroupLocalService _groupLocalService) {
		this._groupLocalService = _groupLocalService;
	}

	protected IndexerRegistry getIndexerRegistry() {
		return _indexerRegistry;
	}

	@Reference
	protected void setIndexerRegistry(IndexerRegistry _indexerRegistry) {
		this._indexerRegistry = _indexerRegistry;
	}

	protected IndexStatusManager getIndexStatusManager() {
		return _indexStatusManager;
	}

	@Reference
	protected void setIndexStatusManager(IndexStatusManager _indexStatusManager) {
		this._indexStatusManager = _indexStatusManager;
	}

	protected JournalArticleLocalService getJournalArticleLocalService() {
		return _journalArticleLocalService;
	}

	@Reference
	protected void setJournalArticleLocalService(JournalArticleLocalService _journalArticleLocalService) {
		this._journalArticleLocalService = _journalArticleLocalService;
	}

	protected JournalConverter getJournalConverter() {
		return _journalConverter;
	}

	@Reference
	protected void setJournalConverter(JournalConverter _journalConverter) {
		this._journalConverter = _journalConverter;
	}

	protected JournalFolderLocalService getJournalFolderLocalService() {
		return _journalFolderLocalService;
	}

	@Reference
	protected void setJournalFolderLocalService(JournalFolderLocalService _journalFolderLocalService) {
		this._journalFolderLocalService = _journalFolderLocalService;
	}

	protected LayoutLocalService getLayoutLocalService() {
		return _layoutLocalService;
	}

	@Reference
	protected void setLayoutLocalService(LayoutLocalService _layoutLocalService) {
		this._layoutLocalService = _layoutLocalService;
	}

	protected LayoutPrototypeLocalService getLayoutPrototypeLocalService() {
		return _layoutPrototypeLocalService;
	}

	@Reference
	protected void setLayoutPrototypeLocalService(LayoutPrototypeLocalService _layoutPrototypeLocalService) {
		this._layoutPrototypeLocalService = _layoutPrototypeLocalService;
	}

	protected LayoutSetLocalService getLayoutSetLocalService() {
		return _layoutSetLocalService;
	}

	@Reference
	protected void setLayoutSetLocalService(LayoutSetLocalService _layoutSetLocalService) {
		this._layoutSetLocalService = _layoutSetLocalService;
	}

	protected LayoutSetPrototypeLocalService getLayoutSetPrototypeLocalService() {
		return _layoutSetPrototypeLocalService;
	}

	@Reference
	protected void setLayoutSetPrototypeLocalService(LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService) {
		this._layoutSetPrototypeLocalService = _layoutSetPrototypeLocalService;
	}

	protected MimeTypes getMimeTypes() {
		return _mimeTypes;
	}

	@Reference
	protected void setMimeTypes(MimeTypes _mimeTypes) {
		this._mimeTypes = _mimeTypes;
	}

	protected Portal getPortal() {
		return _portal;
	}

	@Reference
	protected void setPortal(Portal _portal) {
		this._portal = _portal;
	}

	protected PortletPreferencesFactory getPortletPreferencesFactory() {
		return _portletPreferencesFactory;
	}

	@Reference
	protected void setPortletPreferencesFactory(PortletPreferencesFactory _portletPreferencesFactory) {
		this._portletPreferencesFactory = _portletPreferencesFactory;
	}

	protected PortletPreferencesLocalService getPortletPreferencesLocalService() {
		return _portletPreferencesLocalService;
	}

	@Reference
	protected void setPortletPreferencesLocalService(PortletPreferencesLocalService _portletPreferencesLocalService) {
		this._portletPreferencesLocalService = _portletPreferencesLocalService;
	}

	protected PortletPreferencesTranslator getPortletPreferencesTranslator() {
		return _portletPreferencesTranslator;
	}

	@Reference(
			target = "(portlet.preferences.translator.portlet.id=" + ResourcesImporterConstants.PORTLET_ID_DEFAULT + ")"
	)
	protected void setPortletPreferencesTranslator(PortletPreferencesTranslator _portletPreferencesTranslator) {
		this._portletPreferencesTranslator = _portletPreferencesTranslator;
	}

	protected Map<String, PortletPreferencesTranslator> getPortletPreferencesTranslators() {
		return _portletPreferencesTranslators;
	}

	protected RepositoryLocalService getRepositoryLocalService() {
		return _repositoryLocalService;
	}

	@Reference
	protected void setRepositoryLocalService(RepositoryLocalService _repositoryLocalService) {
		this._repositoryLocalService = _repositoryLocalService;
	}

	protected SAXReader getSaxReader() {
		return _saxReader;
	}

	@Reference
	protected void setSaxReader(SAXReader _saxReader) {
		this._saxReader = _saxReader;
	}

	protected ThemeLocalService getThemeLocalService() {
		return _themeLocalService;
	}

	@Reference
	protected void setThemeLocalService(ThemeLocalService _themeLocalService) {
		this._themeLocalService = _themeLocalService;
	}

	protected UserLocalService getUserLocalService() {
		return _userLocalService;
	}

	@Reference
	protected void setUserLocalService(UserLocalService _userLocalService) {
		this._userLocalService = _userLocalService;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ImporterFactory.class);

	private ExportImportConfigurationLocalService _exportImportConfigurationLocalService;
	private ExportImportLocalService _exportImportLocalService;
	private AssetTagLocalService _assetTagLocalService;
	private DDMFormJSONDeserializer _ddmFormJSONDeserializer;
	private DDMFormXSDDeserializer _ddmFormXSDDeserializer;
	private DDMStructureLocalService _ddmStructureLocalService;
	private DDMTemplateLocalService _ddmTemplateLocalService;
	private DDMXML _ddmxml;
	private DLAppLocalService _dlAppLocalService;
	private DLFileEntryLocalService _dlFileEntryLocalService;
	private DLFolderLocalService _dlFolderLocalService;
	private GroupLocalService _groupLocalService;
	private IndexerRegistry _indexerRegistry;
	private IndexStatusManager _indexStatusManager;
	private JournalArticleLocalService _journalArticleLocalService;
	private JournalConverter _journalConverter;
	private JournalFolderLocalService _journalFolderLocalService;
	private LayoutLocalService _layoutLocalService;
	private LayoutPrototypeLocalService _layoutPrototypeLocalService;
	private LayoutSetLocalService _layoutSetLocalService;
	private LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService;
	private MimeTypes _mimeTypes;
	private Portal _portal;
	private PortletPreferencesFactory _portletPreferencesFactory;
	private PortletPreferencesLocalService _portletPreferencesLocalService;
	private PortletPreferencesTranslator _portletPreferencesTranslator;

	private final Map<String, PortletPreferencesTranslator>
		_portletPreferencesTranslators = new ConcurrentHashMap<>();

	private RepositoryLocalService _repositoryLocalService;
	private SAXReader _saxReader;
	private ThemeLocalService _themeLocalService;
	private UserLocalService _userLocalService;
}