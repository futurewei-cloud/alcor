/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
