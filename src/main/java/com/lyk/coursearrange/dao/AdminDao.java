package com.lyk.coursearrange.dao;

import com.lyk.coursearrange.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lequal
 * @since 2020-03-06
 */
public interface AdminDao extends BaseMapper<Admin> {

    @Select("SELECT * FROM tb_admin WHERE admin_no=#{account} AND password=#{password}" +
            "        UNION" +
            "        SELECT * FROM tb_admin WHERE username=#{account} AND password=#{password}" +
            "        UNION" +
            "        SELECT * FROM tb_admin WHERE realname=#{account} AND password=#{password}")
    Admin adminLogin(@Param("account") String account, @Param("password") String password);
}
