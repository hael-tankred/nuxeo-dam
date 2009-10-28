package org.nuxeo.dam.webapp.annotations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.dam.webapp.contentbrowser.DocumentActions;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.runtime.api.Framework;

@Name("annotationsActions")
@Scope(ScopeType.CONVERSATION)
@SerializedConcurrentAccess
public class AnnotationsActions {

    private static final Log log = LogFactory.getLog(AnnotationsActions.class);

    @In(create = true)
    protected transient DocumentActions documentActions;

    private URLPolicyService urlService;

    private URLPolicyService getUrlService() {
        if (urlService == null) {
            try {
                urlService = Framework.getService(URLPolicyService.class);
            } catch (Exception e) {
                log.error("Could not retrieve the URLPolicyService", e);
            }
        }
        return urlService;
    }

    /**
     * Method used to generate Annotation Url provided to facelet
     */
    public String getAnnotationsURL() {
        return getAnnotationsURL(documentActions.getCurrentSelection());
    }

    public String getAnnotationsURL(DocumentModel document) {
        DocumentLocation docLocation = new DocumentLocationImpl(
                document.getRepositoryName(), document.getRef());
        DocumentView docView = new DocumentViewImpl(docLocation, "annotations_popup");
        String url = getUrlService().getUrlFromDocumentView(docView,
                BaseURL.getBaseURL());
        url = RestHelper.addCurrentConversationParameters(url);
        return url;
    }

}