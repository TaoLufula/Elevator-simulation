package sysc3303_elevator;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.jupiter.api.Test;

class ElevatorTest {
	

	@Test
	void test() throws Throwable {

		var event1 = new FloorEvent(null, 0, null, 0);
		var event2 = new FloorEvent(null, 1, null, 0);
		
		var exception = new RuntimeException();

		var inbound = new BlockingQueueMocker<FloorEvent>() {
			public int takeCount = 0;
			public FloorEvent take() throws InterruptedException {
				takeCount++;
				switch (takeCount - 1) {
				case 0: {
					return event1;
				}
				case 1: {
					return event2;
				}
				default:
					throw exception;
				}
				
			};
		};

		var outbound = new BlockingQueueMocker<Message>() {
			public int count = 0;
			@Override
			public void put(Message e) throws InterruptedException {
				count++;
			}
		};

		var e1 = new Elevator(
				inbound,
				outbound
		);
		
		var t1 = new Thread(e1);

		var catcher = new UncaughtExceptionHandler() {
			public Throwable caughtException = null;
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				caughtException = e;
			}
		};
		t1.setDefaultUncaughtExceptionHandler(catcher);

		t1.start();
		t1.join();

		if (exception != catcher.caughtException) {
			throw catcher.caughtException;
		}
		assertEquals(3, inbound.takeCount);
		assertEquals(2, outbound.count);
	}

}
