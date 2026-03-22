package com.paola.paolarestapi.integration;

import com.paola.paolarestapi.integration.dto.ReqResImportResponse;
import com.paola.paolarestapi.integration.model.SnapshotUser;
import com.paola.paolarestapi.integration.service.ReqResImportService;
import com.paola.paolarestapi.integration.service.SnapshotValidationService;
import com.paola.paolarestapi.integration.service.XPathUserSearchService;
import com.paola.paolarestapi.integration.service.XmlSnapshotService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/*
  IntegrationResource provides Day 2 helper endpoints under /api/integration.

  Purpose:
  - Trigger import from public ReqRes API into local DB + XML snapshot.
  - Validate generated XML snapshot against users-snapshot.xsd.
  - Search snapshot with XPath to verify Day 2 SOAP logic quickly via REST.

  Why this exists:
  - It gives a simple development/testing surface before wiring SOAP clients.
  - It exposes the core Day 2 steps as independent endpoints.
*/
@Path("/integration")
@Produces(MediaType.APPLICATION_JSON)
public class IntegrationResource {
    private final ReqResImportService reqResImportService = new ReqResImportService();
    private final SnapshotValidationService snapshotValidationService = new SnapshotValidationService();
    private final XmlSnapshotService xmlSnapshotService = new XmlSnapshotService();
    private final XPathUserSearchService xPathUserSearchService = new XPathUserSearchService();

    @POST
    @Path("/import-reqres")
    public Response importReqResUsers() {
        ReqResImportResponse response = reqResImportService.importUsersAndCreateSnapshot();
        return Response.ok(response).build();
    }

    @GET
    @Path("/validate-snapshot")
    public Response validateSnapshot() {
        List<String> messages = snapshotValidationService.validate(xmlSnapshotService.getSnapshotPath());
        return Response.ok(messages).build();
    }

    @GET
    @Path("/search-snapshot")
    public Response searchSnapshot(@QueryParam("term") String term) {
        List<SnapshotUser> users = xPathUserSearchService.search(xmlSnapshotService.getSnapshotPath(), term);
        return Response.ok(users).build();
    }
}
