/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.sdk.controllers.tasks;

import android.util.Log;

import org.hisp.dhis2.android.sdk.network.http.ApiRequest;
import org.hisp.dhis2.android.sdk.network.http.ApiRequestCallback;
import org.hisp.dhis2.android.sdk.network.http.Header;
import org.hisp.dhis2.android.sdk.network.http.Request;
import org.hisp.dhis2.android.sdk.network.http.RestMethod;
import org.hisp.dhis2.android.sdk.network.managers.NetworkManager;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.User;

import java.util.ArrayList;
import java.util.List;

import static org.hisp.dhis2.android.sdk.utils.Preconditions.isNull;


public class LoadAssignedProgramsTask implements INetworkTask {
    private final ApiRequest.Builder<List<OrganisationUnit>> requestBuilder;

    public LoadAssignedProgramsTask(NetworkManager networkManager,
                                    ApiRequestCallback<List<OrganisationUnit>> callback) {

        isNull(callback, "ApiRequestCallback must not be null");
        isNull(networkManager.getServerUrl(), "Server URL must not be null");
        isNull(networkManager.getHttpManager(), "HttpManager must not be null");
        isNull(networkManager.getBase64Manager(), "Base64Manager must not be null");

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Authorization", networkManager.getCredentials()));
        headers.add(new Header("Accept", "application/json"));

        String url = networkManager.getServerUrl() + "/api/me/programs/";
        Request request = new Request(RestMethod.GET, url, headers, null);

        requestBuilder = new ApiRequest.Builder<>();
        requestBuilder.setRequest(request);
        requestBuilder.setNetworkManager(networkManager.getHttpManager());
        requestBuilder.setRequestCallback(callback);
    }

    @Override
    public void execute() {
        new Thread() {
            public void run() {
                requestBuilder.build().request();
            }
        }.start();
    }
}
