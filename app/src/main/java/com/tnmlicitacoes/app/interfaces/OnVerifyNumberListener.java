package com.tnmlicitacoes.app.interfaces;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tnmlicitacoes.app.apollo.RequestCodeMutation;

public interface OnVerifyNumberListener {

    /**
     * Handles the response for the requestCode mutation
     * @param response Apollo response
     * @param e Apollo exception in failure
     */
    void onRequestCodeResponse(Response<RequestCodeMutation.Data> response, ApolloException e);

    void onRegisterFinished(String refreshToken, String accessToken);
}
