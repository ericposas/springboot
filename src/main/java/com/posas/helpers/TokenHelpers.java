package com.posas.helpers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;

public class TokenHelpers {

    /**
     * 
     * {@summary} gets the nested jwt attributes in "resource_access"; made
     * available by then calling resource.get(clientId).get("attribute_name")
     * 
     * @param principal
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> getTokenResource(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> resource = (LinkedTreeMap<String, LinkedTreeMap<String, List<String>>>) token
                .getTokenAttributes()
                .get("resource_access");
        return resource;
    }

    /**
     * 
     * @param principal
     * @return a Map of token attributes; we can access them like .get("name")
     * like a normal map property
     */
    public static Map<String, Object> getTokenAttributes(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        return token.getTokenAttributes();
    }

}
