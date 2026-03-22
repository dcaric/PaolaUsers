package com.paola.paolarestapi.integration.dto;

import java.util.ArrayList;
import java.util.List;

/*
  Response DTO returned by import endpoint.

  It summarizes Day 2 import execution:
  - how many users were fetched from ReqRes,
  - how many were saved to local DB,
  - where snapshot XML was written,
  - validation messages from snapshot XSD check.
*/
public class ReqResImportResponse {
    private int fetchedFromReqRes;
    private int savedToDatabase;
    private String snapshotPath;
    private List<String> snapshotValidationMessages = new ArrayList<String>();

    public int getFetchedFromReqRes() {
        return fetchedFromReqRes;
    }

    public void setFetchedFromReqRes(int fetchedFromReqRes) {
        this.fetchedFromReqRes = fetchedFromReqRes;
    }

    public int getSavedToDatabase() {
        return savedToDatabase;
    }

    public void setSavedToDatabase(int savedToDatabase) {
        this.savedToDatabase = savedToDatabase;
    }

    public String getSnapshotPath() {
        return snapshotPath;
    }

    public void setSnapshotPath(String snapshotPath) {
        this.snapshotPath = snapshotPath;
    }

    public List<String> getSnapshotValidationMessages() {
        return snapshotValidationMessages;
    }

    public void setSnapshotValidationMessages(List<String> snapshotValidationMessages) {
        this.snapshotValidationMessages = snapshotValidationMessages;
    }
}
