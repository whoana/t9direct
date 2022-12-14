package rose.mary.trace.core.helper.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rose.mary.trace.core.config.OldStateCheckHandlerConfig;
import rose.mary.trace.core.data.common.State;
import rose.mary.trace.core.data.common.Trace;

public class OldStateCheckHandler implements StateChecker {

	Logger logger = LoggerFactory.getLogger(getClass());

	OldStateCheckHandlerConfig config;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int KEY_SNDR = 10;
	public static final int KEY_BRKR = 20;
	public static final int KEY_REPL = 30;
	public static final int KEY_RBRK = 40;
	public static final int KEY_RCVR = 50;

	final static public String ST_SUCCESS = "00";
	final static public String ST_ING = "01";
	final static public String ST_FAIL = "99";

	boolean usePreviousProcessInfo = false;

	public OldStateCheckHandler(OldStateCheckHandlerConfig config) {
		this.config = config;
	}

	@Override
	public void checkAndSet(boolean first, Trace trace, State state) {

		String trackingDate = trace.getDate();
		String orgHostId = trace.getOriginHostId();
		String status = trace.getStatus();
		String type = trace.getType();
		int typeSeq = config.getNodeMap().putIfAbsent(type, 0);
		int recordCount = trace.getRecordCount();
		int dataAmount = trace.getDataSize();
		String compress = trace.getCompress();
		int cost = 0;
		try {
			cost = Integer.parseInt(trace.getElapsedTime());
		} catch (Exception e) {
			cost = 0;
		}
		int todoNodeCount = trace.getTodoNodeCount();
		String errorCode = trace.getErrorCode();
		String errorMessage = trace.getErrorMessage();
		String integrationId = trace.getIntegrationId();

		state.setSkip(false);
		if (first) {
			// ------------------------------------------
			// 최초로 도착한 헤더정보를 기준으로 세팅되는 값
			// ------------------------------------------
			state.setIntegrationId(integrationId);
			state.setTrackingDate(trackingDate);
			state.setOrgHostId(orgHostId);
			state.setCompress(compress);
			state.setCost(cost);// 처리시간값(단위는 자유)
			state.setDataAmount(dataAmount);
			state.setRecordCount(recordCount);
			state.setTodoNodeCount(todoNodeCount);
			state.setFinishSenderCount(0);
		}

		switch (typeSeq) {
			case KEY_SNDR:
				state.setFinishSenderCount(state.getFinishSenderCount() + 1);
				if (ST_FAIL.equals(status)) {
					state.setErrorCode(errorCode);
					state.setErrorMessage(errorMessage);
					state.setErrorNodeCount(state.getErrorNodeCount() + 1);
				} else {

				}
				break;
			case KEY_BRKR:
				KEY_RBRK: if (ST_FAIL.equals(status)) {
					state.setErrorCode(errorCode);
					state.setErrorMessage(errorMessage);
					state.setErrorNodeCount(state.getErrorNodeCount() + 1);
				} else {
					state.setSkip(true);
				}
				break;
			case KEY_REPL:
				if (ST_FAIL.equals(status)) {
					state.setErrorCode(errorCode);
					state.setErrorMessage(errorMessage);
					state.setErrorNodeCount(state.getErrorNodeCount() + 1);
				} else {
					state.setSkip(true);
				}
				break;

			case KEY_RCVR:
				state.setFinishNodeCount(state.getFinishNodeCount() + 1);
				if (ST_FAIL.equals(status)) {
					state.setErrorCode(errorCode);
					state.setErrorMessage(errorMessage);
					state.setErrorNodeCount(state.getErrorNodeCount() + 1);
				} else {
				}
				break;
			default:
				if (ST_FAIL.equals(status)) {
					state.setErrorCode(errorCode);
					state.setErrorMessage(errorMessage);
					state.setErrorNodeCount(state.getErrorNodeCount() + 1);
				} else {
				}
				break;
		}

		// 트레킹 키값 조합 디버깅시 사용할 용도로 남겨둠 .20220826
		// String tk = state.getBotId() + "@" + typeSeq;

		String beforeStatus = state.getStatus();
		boolean notFinished = !state.isFinish();
		boolean senderReceived = state.isFinishSender();
		int finishNodeCount = state.getFinishNodeCount();
		// --------------------------------------------------
		// 완료여부 세팅
		// --------------------------------------------------
		if (notFinished && finishNodeCount >= todoNodeCount && senderReceived) {
			// 직전상태 미완료 이며, 처리해야할 노드 숫자가 발생된 트래킹 노드 수가 일치하고 첫번째 노드를 받았으면
			state.setFinish(true); // 완료처리
		}

		// --------------------------------------------------
		// 최종 상태(State.status) 세팅
		// --------------------------------------------------
		if (ST_ING.equals(beforeStatus)) {// 이전 상태가 "진행중" 일때만 상태 변경 처리
			if (ST_FAIL.equals(status)) {
				state.setStatus(ST_FAIL);// 실패
			} else {
				if (state.isFinish()) {
					state.setStatus(ST_SUCCESS);// 성공
				} else {
					state.setStatus(ST_ING);// 진행중
				}
			}
		}

		// state.setContext(tk);
		// logger.debug("boter, tk=[" + tk + "]");
	}

}
