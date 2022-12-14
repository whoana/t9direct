/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.database.mapper.m01;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import rose.mary.trace.core.data.Foo;

/**
 * <pre>
 * rose.mary.trace.database.mapper
 * FooMapper.java
 * </pre>
 * 
 * @author whoana
 * @date Aug 27, 2019
 */
@Mapper
public interface FooMapper {

	@Select("select * from FOO where ID = #{id}")
	Foo findById(@Param("id") String id) throws Exception;

	@Select("select * from FOO")
	List<Foo> findList() throws Exception;

	@Insert("insert into FOO values(#{id}, #{val})")
	int insert(Foo foo) throws Exception;
}
