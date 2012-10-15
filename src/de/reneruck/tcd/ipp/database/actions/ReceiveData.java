package de.reneruck.tcd.ipp.database.actions;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import de.reneruck.tcd.ipp.database.DatabaseQueryHandler;
import de.reneruck.tcd.ipp.database.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.Datagram;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.Transition;
import de.reneruck.tcd.ipp.fsm.Action;
import de.reneruck.tcd.ipp.fsm.TransitionEvent;

public class ReceiveData implements Action {

	private ObjectOutputStream out;
	private Queue<Transition> queue = new LinkedBlockingQueue<Transition>();
	private TemporalTransitionsStore transitionQueue;
	private DatabaseQueryHandler databaseQueryHandler;

	public ReceiveData(ObjectOutputStream out, TemporalTransitionsStore transitionsQueue) {
		this.out = out;
		this.transitionQueue = transitionsQueue;
		this.databaseQueryHandler = new DatabaseQueryHandler(this.queue, this.transitionQueue);
	}

	@Override
	public void execute(TransitionEvent event) throws Exception {
		if(!this.databaseQueryHandler.isRunning()) {
			startServer();
		}
		Object content = event.getParameter(Statics.CONTENT_TRANSITION);
		if(content != null && content instanceof Transition) {
			this.queue.add((Transition)content);
			Map<String, Object> datagramPayload = new HashMap<String, Object>();
			datagramPayload.put(Statics.TRAMSITION_ID, ((Transition)content).getTransitionId());
			this.out.writeObject(new Datagram(Statics.ACK, datagramPayload));
		} else {
			System.err.println("Invalid event content");
		}
	}

	private void startServer() {
		this.databaseQueryHandler.setRunning(true);
		this.databaseQueryHandler.start();
	}

}