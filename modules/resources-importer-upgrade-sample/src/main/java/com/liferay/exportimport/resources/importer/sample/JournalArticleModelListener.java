package com.liferay.exportimport.resources.importer.sample;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import org.osgi.service.component.annotations.Component;

/**
 * class JournalArticleModelListener: This is just a sample model listener so we can see the result of our resources
 * being imported by the RI.  This is absolutely not necessary for using the RI.
 *
 * @author dnebinger
 */
@Component(
		immediate = true,
		service = ModelListener.class
)
public class JournalArticleModelListener extends BaseModelListener<JournalArticle> implements ModelListener<JournalArticle> {

	@Override
	public void onBeforeCreate(JournalArticle model) throws ModelListenerException {
		if (_log.isDebugEnabled()) {
			_log.debug("Creating journal article [" + model.getArticleId() + "]:");
			_log.debug("  Title: [" + model.getTitle() + "].");

			try {
				JournalFolder folder = model.getFolder();

				_log.debug("  Going into folder [" + folder.getName() + "] of company " + folder.getCompanyId() + ".");
			} catch (PortalException e) {
				_log.error("Error accessing article folder: " + e.getMessage(), e);
			}
		}

		super.onBeforeCreate(model);
	}

	@Override
	public void onBeforeUpdate(JournalArticle model) throws ModelListenerException {
		if (_log.isDebugEnabled()) {
			_log.debug("Updating journal article [" + model.getArticleId() + "]:");
			_log.debug("  Title: [" + model.getTitle() + "].");

			try {
				JournalFolder folder = model.getFolder();

				_log.debug("  Going into folder [" + folder.getName() + "] of company " + folder.getCompanyId() + ".");
			} catch (PortalException e) {
				_log.error("Error accessing article folder: " + e.getMessage(), e);
			}
		}

		super.onBeforeUpdate(model);
	}

	private static final Log _log = LogFactoryUtil.getLog(JournalArticleModelListener.class);
}
