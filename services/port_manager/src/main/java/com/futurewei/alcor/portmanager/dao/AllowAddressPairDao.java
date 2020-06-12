package com.futurewei.alcor.portmanager.dao;

import com.futurewei.alcor.web.entity.port.AllowAddressPair;
import com.futurewei.alcor.web.entity.port.FixedIp;
import org.apache.ibatis.annotations.*;

import javax.print.DocFlavor;
import java.util.List;

@Mapper
public interface AllowAddressPairDao {

    @Select("select * from t_allowed_address_pair where port_id=#{portId}")
    List<FixedIp> findAllowAddressPairsByPortId(String portId);

    @Insert("insert into t_allowed_address_pair (ip_address, mac_address, port_id) values " +
            "(#{ipAddress}, #{macAddress}, #{portId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    List<FixedIp> insertAllowAddressPair(String portId, AllowAddressPair allowAddressPair);

    @Insert({
            "<script>",
            "insert into t_allowed_address_pair (ip_address, mac_address, port_id) values" +
                    "<foreach collection='allowAddressPairs' item='item' index='index' separator=','>",
            "(#{item.ipAddress}, #{item.macAddress}, #{portId})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAllowAddressPairs(String portId, @Param(value="allowAddressPairs") List<AllowAddressPair> allowAddressPairs);
}
