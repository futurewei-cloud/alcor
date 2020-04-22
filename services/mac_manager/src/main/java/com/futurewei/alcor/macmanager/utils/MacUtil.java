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

public class MacUtil {
    public static final String MAC_STATE_ACTIVE = "Active";
    public static final String MAC_STATE_INACTIVE = "Inactive";

    public static final String DEFAULT_RANGE = "range0";
    public static final String MAC_RANGE_STATE_ACTIVE = "Active";
    public static final String MAC_RANGE_STATE_INACTIVE = "Inactive";
    public static final String MAC_RANGE_STATE_EXCLUDE = "Exclude";

    public static final String MAC_EXCEPTION_UNIQUENESSSS_VILOATION = "This mac address is not unique!!";
    public static final String MAC_EXCEPTION_RETRY_LIMIT_EXCEED = "It exceeded the limit of retry for to create a MAC. Please check active MAC ranges";
    public static final String MAC_EXCEPTION_RANGE_VALUE_INVALID = "MAC range is not vaild. Ranges' start value should be less than end value";
}
