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

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.util.List;


/**
 * A wrapper for handling Clarifai's functions, including its image prediction service.
 */
class ClarifaiHandler
{

    //The API Key for accessing Clarifai's services, set through the nuxeo.conf configuration file
    private String API_KEY;

    /**
     * Constructor -> initializes the API_KEY variable from the Nuxeo conf file, or throws
     * an exception if an API key cannot be found.
     */
    ClarifaiHandler ()
    {
        API_KEY = Framework.getProperty("org.nuxeo.clarifaivision.key");
        if ( API_KEY == null || API_KEY.equals("") )
        {
            throw new NuxeoException("No API Key has been provided. Please add the key to nuxeo.conf using the" +
                                             "org.nuxeo.clarifaivision.key parameter");
        }
    }

    /**
     * Tags a DocumentModel picture object with tags retrieved from Clarifai's image prediction
     * service, which takes in pictures and returns tags based on machine learning algorithms.
     *
     * @param doc -> a DocumentModel Picture object to tag with Clarifai
     */
    void tag ( DocumentModel doc )
    {
        if ( doc == null )
            return;

        //Gets the small image preview to be sent to Clarifai to save time
        FileBlob blob;

        try
        {
            blob = (FileBlob) doc.getPropertyObject("picture", "views").get(1).get("content").getValue();
        }
        catch ( Throwable e )
        {
            return;
        }

        File file = blob.getFile();

        if ( file == null )
            return;


        //Calls the Clarifai client to receive tags
        ClarifaiClient client = new ClarifaiBuilder(API_KEY).buildSync();
        List <ClarifaiOutput <Concept>> rep = client.getDefaultModels().generalModel().predict().withInputs(
                ClarifaiInput.forImage(file)).executeSync().get();

        //Call the CoreSession from the DocumentModel given to add tags to the image
        TagService  service = Framework.getLocalService(TagService.class);
        String      id      = doc.getId();
        CoreSession session = doc.getCoreSession();

        //Add tags to image with nested forEach loop
        rep.forEach(out -> out.data().forEach(con -> service.tag(session, id, con.name(), "Administrator")));
    }


    String getAPIKey ()
    {
        return API_KEY;
    }

    void setAPIKey ( String apiKey )
    {
        this.API_KEY = API_KEY;
    }
}
