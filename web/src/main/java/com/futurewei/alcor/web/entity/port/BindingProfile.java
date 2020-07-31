package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class BindingProfile implements Serializable {
    @JsonProperty("local_link_information")
    private List<LocalLinkInformation> listOfLocalLinkInfos;
}
