package com.paola.paolarestapi.restapi.service;

/*
  Switch options for custom REST API data source routing.
  LOCAL = custom DB-backed API, PUBLIC = ReqRes cloud source for read operations.
*/
public enum RestApiSourceType {
    LOCAL,
    PUBLIC
}
