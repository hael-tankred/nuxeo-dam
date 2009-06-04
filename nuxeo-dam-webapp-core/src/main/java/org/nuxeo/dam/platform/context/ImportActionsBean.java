package org.nuxeo.dam.platform.context;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

@Name("importActions")
@Scope(ScopeType.CONVERSATION)
public class ImportActionsBean {

    protected static final Log log = LogFactory.getLog(ImportActionsBean.class);

    public static final String BATCH_TYPE_NAME = "ImportSet";

    public static final String IMPORTSET_ROOT_PATH = "/default-domain/import-sets";

    protected DocumentModel newImportSet;

    @In(create = true)
    protected CoreSession documentManager;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    // won't inject this because of seam problem after activation
    // ::protected Map<String, String> messages;
    protected ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected Context eventContext;

    protected FileManager fileManagerService;

    protected FileManager getFileManagerService() throws Exception {
        if (fileManagerService == null) {
            fileManagerService = Framework.getService(FileManager.class);
        }
        return fileManagerService;
    }

    public DocumentModel getNewImportSet() throws ClientException {
        if (newImportSet == null) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put(CoreEventConstants.PARENT_PATH, IMPORTSET_ROOT_PATH);
            newImportSet = documentManager.createDocumentModel(BATCH_TYPE_NAME,
                    context);
        }

        return newImportSet;
    }

    public String createImportSet() throws Exception {
        String title = (String) newImportSet.getProperty("dublincore", "title");
        if (title == null) {
            title = "";
        }
        String name = IdUtils.generateId(title);
        // set parent path and name for document model
        newImportSet.setPathInfo(IMPORTSET_ROOT_PATH, name);

        Map<String, Object> properties = newImportSet.getProperties("file");
        Blob blob = (Blob) properties.get("content");

        newImportSet = documentManager.createDocument(newImportSet);

        if (blob != null) {
            String filename = (String) properties.get("filename");
            getFileManagerService().createDocumentFromBlob(
                documentManager, blob, newImportSet.getPathAsString(), true,
                filename);
        }

        documentManager.save();

        logDocumentWithTitle("document_saved", "Created the document: ", newImportSet);

        invalidateImportContext();
        return "nxstartup";
    }

    public void createAssetsFromFile(DocumentModel importSet) throws Exception {

    }

    public String cancel() {
        invalidateImportContext();
        return "nxstartup";
    }

    public void invalidateImportContext() {
        newImportSet = null;
    }

    /**
     * Logs a {@link DocumentModel} title and the passed string (info).
     */
    public void logDocumentWithTitle(String facesMessage, String someLogString,
            DocumentModel document) {

        facesMessages.add(FacesMessage.SEVERITY_INFO, resourcesAccessor
                .getMessages().get(facesMessage), resourcesAccessor
                .getMessages().get(newImportSet.getType()));

        if (null != document) {
            log.trace('[' + getClass().getSimpleName() + "] " + someLogString
                    + ' ' + document.getId());
            log.debug("CURRENT DOC PATH: " + document.getPathAsString());
        } else {
            log.trace('[' + getClass().getSimpleName() + "] " + someLogString
                    + " NULL DOC");
        }
    }

}