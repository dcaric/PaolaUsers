package com.paola.paolarestapi.restapi.dto;

import java.util.Map;

/*
  What GraphQL is:
  - GraphQL is a query language/API style where client asks for exactly the fields it needs.
  - Instead of many fixed REST endpoints, one endpoint can handle different data shapes.

  Why it is used here:
  - Assignment requires GraphQL support on top of the same user data model.
  - It demonstrates flexible querying and mutation in addition to REST CRUD endpoints.

  How it is used here:
  - Client sends JSON body with:
      1) query (GraphQL operation text)
      2) variables (optional input map for mutation values)
  - GraphQlService reads this DTO and routes operation logic.
*/
public class GraphQlRequest {
    private String query;
    private Map<String, Object> variables;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
