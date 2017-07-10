package com.simple.server.factory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.simple.server.config.EndpointType;
import com.simple.server.domain.IRec;
import com.simple.server.domain.UniRequest;
import com.simple.server.domain.UniResult;
import com.simple.server.domain.contract.SubRouting;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.IncomingBufferMsg;
import com.simple.server.domain.contract.TagRequestMsg;
import com.simple.server.domain.contract.UniResultMsg;
import com.simple.server.domain.uni.IncomingBuffer;
import com.simple.server.statistics.time.Timing;

/**
 * Фабрика для генераций объектов IContract<->IRec
 * 
 * @author Ivanov Igor
 * @version 1.0
 * @since 1.0
 */

@Component("contractRecFactory")
public class ContractRecFactory {

	
	/** Создание объекта IRec из IContract.
	 * @param msg IContract 
	 * @return res IRec
	 * @throws Exception
	*/
	public IRec newRec(IContract msg) throws Exception {
		IRec res = null;

		if (msg instanceof TagRequestMsg) {
			res = new UniRequest();
			res.copyFrom(msg);
			return res;
		}

		if (msg instanceof IncomingBufferMsg) {
			res = new IncomingBuffer();
			res.copyFrom(msg);
			return res;
		}

		EndpointType endpoint = msg.getEndPointId();

		if (EndpointType.LOG.equals(endpoint)) {
			if (msg instanceof SubRouting) {
				res = new UniRequest();
				res.copyFrom(msg);
				return res;
			}
		}

		throw new Exception(String.format("ContactRecFactory.newRec: no mapping for class <%s>, endpoint <%s>",
				msg.getClass(), endpoint));
	}

	/** Создание объекта IContract из IRec.
	 * @param rec IRec 
	 * @return res IContract
	 * @throws Exception
	*/
	public IContract newContract(IRec rec) throws Exception {
		IContract res = null;

		if (rec instanceof UniResult) {
			res = new UniResultMsg();
			res.copyFrom(rec);
			return res;
		}

		if (rec instanceof IncomingBuffer) {
			res = new IncomingBufferMsg();
			res.copyFrom(rec);
			return res;
		}

		EndpointType endpoint = rec.getEndpoint();
		/*
		 * example: if(EndpointType.LOG.equals(endpoint)) {
		 * 
		 * if (rec instanceof NavSorder) { res = new SorderMsg();
		 * res.copyFrom(rec); return res; }
		 */
		throw new Exception(String.format("ContactRecFactory.newContract: no mapping for class <%s>, endpoint <%s>",
				rec.getClass(), endpoint));
	}

	
	/** Создание списка объектов IRec из списка IContract.
	 * @param list List<IContract> 
	 * @return res List<IContract>
	 * @throws Exception
	*/
	public List<IRec> newRecList(List<IContract> list) throws Exception {
		List<IRec> res = new ArrayList<IRec>();
		for (IContract msg : list) {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());
			IRec rec = newRec(msg);
			res.add(rec);
		}
		return res;
	}

}
