/*Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.macmanager.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacManagerConstant {
    public static final String MAC_STATE_ACTIVE = "Active";
    public static final String MAC_STATE_INACTIVE = "Inactive";

    public static final String DEFAULT_RANGE = "range0";
    public static final String MAC_RANGE_STATE_ACTIVE = "Active";
    public static final String MAC_RANGE_STATE_INACTIVE = "Inactive";
    public static final String MAC_RANGE_STATE_EXCLUDE = "Exclude";
    public static final int MAC_PREGENERATE_SIZE = 20;

    public static final String MAC_EXCEPTION_UNIQUENESSSS_VILOATION = "This mac address is not unique!!";
    public static final String MAC_EXCEPTION_RETRY_LIMIT_EXCEED = "It exceeded the limit of retry for to create a MAC. Please check active MAC ranges.";
    public static final String MAC_EXCEPTION_MAC_NOT_EXISTING = "Non existing MAC address.";
    public static final String MAC_EXCEPTION_PARAMETER_NULL_EMPTY = "Parameter is null or empty";
    public static final String MAC_EXCEPTION_PARAMETER_INVALID = "Parameter data format or value is not valid";
    public static final String MAC_EXCEPTION_MACADDRESS_INVALID_FORMAT = "MAC address format is not valid";
    public static final String MAC_EXCEPTION_MACADDRESS_FULL = "All MAC addresses are generated. There is no available MAC address.";
    public static final String MAC_EXCEPTION_MACSTATE_NULL = "MacState contains null value";
    public static final String MAC_EXCEPTION_MACSTATE_INVALID_EMPTY = "Mac state contains data which is empty";
    public static final String MAC_EXCEPTION_MACSTATE_INVALID_FORMAT = "Mac state contains invalid data value";
    public static final String MAC_EXCEPTION_MACSTATE_INVALID_DATA = "Mac state contains invalid value";
    public static final String MAC_EXCEPTION_RANGE_NOT_EXISTING = "MAC range does not exist";
    public static final String MAC_EXCEPTION_RANGE_NOT_ACTIVE = "MAC range is not active. Please retry after changing ranage state as active";
    public static final String MAC_EXCEPTION_RANGE_INVALID_EMPTY = "MAC range contains empty value";
    public static final String MAC_EXCEPTION_RANGE_INVALID_FORMAT = "MAC range contains invalid data format";
    public static final String MAC_EXCEPTION_RANGE_INVALID_DATA = "MAC range contains invalid value";
    public static final String MAC_EXCEPTION_RANGE_VALUE_NULL = "MAC range contains null value";
    public static final String MAC_EXCEPTION_RANGE_VALUE_INVALID = "MAC range is not vaild. Ranges' start value should be less than end value.";
    public static final String MAC_EXCEPTION_REPOSITORY_EXCEPTION = "There is an error for a service to call a repository";
    public static final String MAC_EXCEPTION_DELETE_DEFAULT_RANGE = "It is not allowed to delete default range.";

    public static final String MAC_EXCEPTION_REQUIRE_PARAMS_NULL = "params project_id or vpc_id or port_id can not be null";
    public static final String MAC_EXCEPTION_REQUIRE_PARAMS_EMPTY = "params project_id or vpc_id or port_id can not be empty";
    public static final String MAC_EXCEPTION_STATE_INVALID = "state must be Active or Inactive";
}
