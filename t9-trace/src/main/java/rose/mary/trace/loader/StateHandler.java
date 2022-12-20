package rose.mary.trace.loader;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pep.per.mint.common.util.Util;
import rose.mary.trace.core.cache.CacheProxy;
import rose.mary.trace.core.data.common.State;
import rose.mary.trace.core.data.common.Trace;
import rose.mary.trace.core.envs.Variables;
import rose.mary.trace.core.helper.checker.StateCheckerMap;
// import rose.mary.trace.database.service.BotService;

public class StateHandler {

    Logger logger = LoggerFactory.getLogger(StateHandler.class);

    CacheProxy<String, State> finCache;

    public StateHandler(CacheProxy<String, State> finCache) {
        this.finCache = finCache;
    }
    // public StateHandler(CacheProxy<String, State> finCache, BotService
    // botService) {
    // this.finCache = finCache;
    // this.botService = botService;
    // }

    // Object monitor = new Object();

    StateCommitter committer;

    // public void handleState(Trace trace, List<State> stateList) throws Exception
    // {

    // String botId = Util.join(trace.getIntegrationId(), "@", trace.getDate(), "@",
    // trace.getOriginHostId());
    // State state = finCache.get(botId);
    // synchronized (monitor) {

    // boolean first = false;
    // if (state == null) {
    // long currentDate = System.currentTimeMillis();
    // state = new State();
    // state.setCreateDate(currentDate);
    // state.setBotId(botId);
    // first = true;
    // finCache.put(botId, state);
    // }

    // StateCheckerMap.map.get(trace.getStateCheckHandlerId()).checkAndSet(first,
    // trace, state);
    // if (!state.skip()) {
    // state.setLoaded(false);
    // stateList.add(state);
    // }
    // //logger.info(Util.join(botId, "thread:", Thread.currentThread().getName(),
    // ",state:", Util.toJSONString(state)));
    // }

    // }
    Object monitor = new Object();

    public void handleState(Trace trace, List<State> stateList) throws Exception {
        synchronized (monitor) {
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

            // synchronized (state) {
            StateCheckerMap.map.get(trace.getStateCheckHandlerId()).checkAndSet(first, trace, state);
            if (!state.skip()) {
                state.setLoaded(false);
                stateList.add(state);
                if (Variables.stateTrace) {
                    logger.info(Util.join(
                            "rh:", state.getBotId(),
                            ":status:", state.getStatus(),
                            ", tdc:" + state.getTodoNodeCount(),
                            ", fnc:" + state.getFinishNodeCount(),
                            ", fsc:", state.getFinishSenderCount(),
                            ", type:", trace.getType(),
                            ", host:", trace.getHostId()));
                }
            }
        }

    }

    // BotService botService;

    // public void commitStates(Collection<Trace> traces, boolean loadError, boolean
    // loadContents, Collection<State> states, CacheProxy<String, State> finCache)
    // throws Exception {
    // synchronized (monitor) {
    // botService.mergeBots(traces , loadError, loadContents, states, finCache);
    // }
    // }

}
