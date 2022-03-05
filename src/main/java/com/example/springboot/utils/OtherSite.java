package com.example.springboot.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedList;
import java.util.List;

public class OtherSite {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String parsedUrl;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String finalUrl;
    public boolean secured;
    public boolean reachable;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<String> redirectedURLs = new LinkedList<>();
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public long totalAccessDuration;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int contentLength;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int responseCode;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String responseMessage;
}
