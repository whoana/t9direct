/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.database.mapper.m01;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import rose.mary.trace.core.data.common.Bot;
import rose.mary.trace.core.data.common.State;
import rose.mary.trace.core.data.common.Unmatch;

/**
 * <pre>
 * rose.mary.trace.database.mapper.m01
 * BOTMapper.java
 * </pre>
 * 
 * @author whoana
 * @date Sep 18, 2019
 */
@Mapper
public interface BotMapper {

	public int restore(Bot bot) throws Exception;

	public int updateUnmatch(Unmatch unmatch) throws Exception;

	public State getState(
		@Param("integrationId") String integrationId, 
		@Param("trackingDate") String trackingDate,
		@Param("orgHostId") String orgHostId) throws Exception;

	public List<State> getNotFinishedStates(@Param("fromDate") String fromDate, @Param("toDate") String toDate);

    public int loadSms(Bot bot) throws Exception;

}
