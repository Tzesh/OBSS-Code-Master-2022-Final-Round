package com.example.springboot.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainSite {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String url;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public long analysisDuration;
    public List<String> redirectedURLs = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int responseCode;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String responseMessage;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<OtherSite> internalLinks;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<OtherSite> externalLinks;
}
