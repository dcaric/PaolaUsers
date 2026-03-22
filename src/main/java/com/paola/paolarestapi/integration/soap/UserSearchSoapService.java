package com.paola.paolarestapi.integration.soap;

import com.paola.paolarestapi.integration.model.SnapshotUser;
import com.paola.paolarestapi.integration.service.SnapshotValidationService;
import com.paola.paolarestapi.integration.service.XPathUserSearchService;
import com.paola.paolarestapi.integration.service.XmlSnapshotService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
  SOAP service for Day 2 user search.

  Behavior:
  - Locate local snapshot XML.
  - Validate snapshot against XSD.
  - If invalid: return validation messages and stop.
  - If valid: execute XPath search and return mapped SOAP results.
*/
@WebService(serviceName = "UserSearchSoapService")
public class UserSearchSoapService {
    private final SnapshotValidationService snapshotValidationService = new SnapshotValidationService();
    private final XPathUserSearchService xPathUserSearchService = new XPathUserSearchService();
    private final XmlSnapshotService xmlSnapshotService = new XmlSnapshotService();

    @WebMethod
    public UserSearchSoapResponse searchUsers(@WebParam(name = "term") String term) {
        UserSearchSoapResponse response = new UserSearchSoapResponse();
        Path snapshotPath = xmlSnapshotService.getSnapshotPath();

        List<String> validationMessages = snapshotValidationService.validate(snapshotPath);
        if (!validationMessages.isEmpty()) {
            response.setMessage("Snapshot XML is invalid. Search aborted.");
            response.setValidationMessages(validationMessages);
            return response;
        }

        List<SnapshotUser> found = xPathUserSearchService.search(snapshotPath, term);
        List<SoapUserResult> soapResults = new ArrayList<SoapUserResult>();
        for (SnapshotUser user : found) {
            SoapUserResult item = new SoapUserResult();
            item.setId(user.getId());
            item.setEmail(user.getEmail());
            item.setFirstName(user.getFirstName());
            item.setLastName(user.getLastName());
            item.setAvatar(user.getAvatar());
            soapResults.add(item);
        }

        response.setMessage("Search completed.");
        response.setUsers(soapResults);
        return response;
    }
}
