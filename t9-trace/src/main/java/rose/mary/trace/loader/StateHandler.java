package rose.mary.trace.loader;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pep.per.mint.common.util.Util;
import rose.mary.trace.core.cache.CacheProxy;
import rose.mary.trace.core.data.common.State;
import rose.mary.trace.core.data.common.Trace;
import rose.mary.trace.core.helper.checker.StateCheckerMap;

public class StateHandler {

    Logger logger = LoggerFactory.getLogger(StateHandler.class);

    CacheProxy<String, State> finCache;

    public StateHandler(CacheProxy<String, State> finCache) {
        this.finCache = finCache;
    }

    Object monitor = new Object();

    StateCommitter committer;

    public void handleState(Trace trace, List<State> stateList) throws Exception {

        String botId = Util.join(trace.getIntegrationId(), "@", trace.getDate(), "@", trace.getOriginHostId());
        synchronized (monitor) {
            State state = finCache.get(botId);
            boolean first = false;
            if (state == null) {
                long currentDate = System.currentTimeMillis();
                state = new State();
                state.setCreateDate(currentDate);
                state.setBotId(botId);
                first = true;
            }
            StateCheckerMap.map.get(trace.getStateCheckHandlerId()).checkAndSet(first, trace, state);
            //logger.info(Util.join(botId, ",status:", state.getStatus(), ", type:", trace.getType()));
            if (!state.skip()) {
                state.setLoaded(false);
                finCache.put(botId, state);
                stateList.add(state);
            }
        }
        
    }

}
