package org.nuxeo.dam.webapp;

import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import static org.jboss.seam.ScopeType.CONVERSATION;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Context;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.codec.DocumentFileCodec;
import org.nuxeo.ecm.platform.util.RepositoryLocation;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.webapp.delegate.DocumentManagerBusinessDelegate;

import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
@Scope(CONVERSATION)
@Name("pictureActions")
public class PictureActions implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In
    private transient Context conversationContext;

    public void downloadPicture(DocumentView docView) throws ClientException {
        if (docView != null) {
            DocumentLocation docLoc = docView.getDocumentLocation();
            if (documentManager == null) {
                RepositoryLocation loc = new RepositoryLocation(
                        docLoc.getServerName());
                documentManager = getOrCreateDocumentManager(loc);
            }
            DocumentModel doc = documentManager.getDocument(docLoc.getDocRef());
            if (doc != null) {
                String[] propertyPath = docView.getParameter(
                        DocumentFileCodec.FILE_PROPERTY_PATH_KEY).split(":");
                String title = null;
                String field = null;
                Property datamodel = null;
                if (propertyPath.length == 2) {
                    title = propertyPath[0];
                    field = propertyPath[1];
                    datamodel = doc.getProperty("picture:views");
                } else if (propertyPath.length == 3) {
                    String schema = propertyPath[0];
                    title = propertyPath[1];
                    field = propertyPath[2];
                    datamodel = doc.getProperty(schema + ":" + "views");
                }
                Property view = null;
                for (Property property : datamodel) {
                    if (property.get("title").getValue().equals(title)) {
                        view = property;
                    }
                }

                if (view == null) {
                    for (Property property : datamodel) {
                        if (property.get("title").getValue().equals("Thumbnail")) {
                            view = property;
                        }
                    }
                }
                if (view==null) {
                    return;
                }
                Blob blob = (Blob) view.getValue(field);
                String filename = (String) view.getValue("filename");
                // download
                FacesContext context = FacesContext.getCurrentInstance();

                ComponentUtils.download(context, blob, filename);
            }
        }
    }

    protected CoreSession getOrCreateDocumentManager(RepositoryLocation repositoryLocation) throws ClientException {
        if (documentManager != null) {
            return documentManager;
        }
        DocumentManagerBusinessDelegate documentManagerBD = (DocumentManagerBusinessDelegate) Contexts.lookupInStatefulContexts("documentManager");
        if (documentManagerBD == null) {
            // this is the first time we select the location, create a
            // DocumentManagerBusinessDelegate instance
            documentManagerBD = new DocumentManagerBusinessDelegate();
            conversationContext.set("documentManager", documentManagerBD);
        }
        documentManager = documentManagerBD.getDocumentManager(repositoryLocation);
        return documentManager;
    }

}