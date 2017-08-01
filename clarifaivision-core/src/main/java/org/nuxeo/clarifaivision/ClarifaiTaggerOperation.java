/*
 * (C) Copyright 2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *      Oisin Coveney
 */
package org.nuxeo.clarifaivision;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;


@Operation ( id = ClarifaiTaggerOperation.ID, category = Constants.CAT_DOCUMENT, label = "Clarifai Tagger Operation",
        description = "Tags pictures using Clarifai's image prediction service (https://clarifai.com/)" )
public class ClarifaiTaggerOperation
{

    static final         String ID           = "Document.ClarifaiTaggerOperation";
    private static final String PICTURE_TYPE = "Picture";

    /**
     * @param doc -> a Nuxeo DocumentModel object called from an Operation chain
     *
     * @return a DocumentModel object which contains a Picture with new tags from Clarifai's image prediction service,
     * or the DocumentModel object that was inputted if the input was invalid.
     *
     * @throws NuxeoException if the DocumentModel is not an image
     */
    @OperationMethod
    public DocumentModel run ( DocumentModel doc ) throws NuxeoException
    {
        if ( !doc.getType().equals(PICTURE_TYPE) )
            throw new NuxeoException("Operation only works with " + PICTURE_TYPE + " document types");
        else
        {
            ClarifaiHandler handler = new ClarifaiHandler();
            handler.tag(doc);
            return doc;
        }
    }
}
