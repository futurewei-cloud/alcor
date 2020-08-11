package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BindingProfile {
    @JsonProperty("local_link_information")
    private List<LocalLinkInformation> listOfLocalLinkInfos;
}
