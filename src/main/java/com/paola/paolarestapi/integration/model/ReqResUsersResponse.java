package com.paola.paolarestapi.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/*
  Represents paged response envelope from ReqRes /api/users.
  Includes page metadata and "data" list of ReqResUserItem.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqResUsersResponse {
    private Integer page;
    @JsonProperty("per_page")
    private Integer perPage;
    private Integer total;
    @JsonProperty("total_pages")
    private Integer totalPages;
    private List<ReqResUserItem> data;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<ReqResUserItem> getData() {
        return data;
    }

    public void setData(List<ReqResUserItem> data) {
        this.data = data;
    }
}
