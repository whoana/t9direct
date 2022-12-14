package rose.mary.trace.loader;


import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pep.per.mint.common.util.Util;
import rose.mary.trace.core.cache.CacheProxy;
import rose.mary.trace.core.data.common.State;
import rose.mary.trace.core.data.common.StateEvent;
import rose.mary.trace.core.data.common.Trace;
import rose.mary.trace.core.helper.checker.StateCheckerMap;

import rose.mary.trace.core.util.IntCounter;

public class RouteHandler {

    Logger logger = LoggerFactory.getLogger(RouteHandler.class);

    CacheProxy<String, State> finCache;
    
    List<CacheProxy<String, StateEvent>> botCaches;
 
	CacheProxy<String, Integer> routingCache;

    IntCounter counter;
    
    public RouteHandler(
        CacheProxy<String, State> finCache, 
        List<CacheProxy<String, StateEvent>> botCaches, 
        CacheProxy<String, Integer> routingCache
    ) {
        this.finCache = finCache;
        this.botCaches = botCaches;
        this.routingCache = routingCache;
        this.counter = new IntCounter(0, botCaches.size() - 1, 1);
    }
  
    public void handleState(Trace trace) throws Exception {

        String botId = Util.join(trace.getIntegrationId(), "@", trace.getDate(), "@", trace.getOriginHostId());
        State state = finCache.get(botId);
        
        boolean first = false;
        if (state == null) {
            long currentDate = System.currentTimeMillis();
            state = new State();
            state.setCreateDate(currentDate);
            state.setBotId(botId);
            first = true;
            finCache.put(botId, state);
        }
        
        synchronized (state) {
            StateCheckerMap.map.get(trace.getStateCheckHandlerId()).checkAndSet(first, trace, state);
            if (!state.skip()) {
                state.setLoaded(false);

                Integer index = getBotCacheIndex(botId);

                CacheProxy<String, StateEvent> botCache = botCaches.get(index);

                // 2022.08.23 dup 에러가 발생됨. merge 문을 사용하였음에도 발생.
                // 동일 배치처리 SQL 블럭에 동일 건이 포함되면 merge 문에서도 에러가 발생되지 않나 싶다.
                // 키값으로 uniqId 대신에 state.getBotId() 를 사용하는 것은 어떨까?
                // String uniqId = state.getBotId();
                String uniqId = UUID.randomUUID().toString();
                StateEvent se = new StateEvent();
                se.setId(uniqId);
                se.setBotId(state.getBotId());
                botCache.put(uniqId, se);
            }
            //logger.info(Util.join(botId, "thread:", Thread.currentThread().getName(), ",state:", Util.toJSONString(state)));
        }        
    }

    private Integer getBotCacheIndex(String botId) throws Exception {
		Integer index = routingCache.get(botId);
		if (index == null) {
			index = counter.getAndIncrease();
			routingCache.put(botId, index);
		}
		return index;
	}
}
