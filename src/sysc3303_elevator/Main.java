package sysc3303_elevator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Quinn Parrott
 *
 */
public class Main {
	
	/**
	 * Used by `GroupBy` to turn an item into a key.
	 * 
	 * @see GroupBy
	 * @author Quinn Parrott
	 */
	public interface GrouperFunction<K, V> { K byKey(V t1); }
	
	/**
	 * Group a collection if items into buckets
	 * 
	 * @param collection The list of items.
	 * @param grouperFun The function that will choose which bucket an item goes into.
	 * @author Quinn Parrott
	 */
	public static <K, V> HashMap<K, ArrayList<V>> GroupBy(Iterable<V> collection, GrouperFunction<K, V> grouperFun) {
		var groups = new HashMap<K, ArrayList<V>>();

		for (var item : collection) {
			var key = grouperFun.byKey(item);
			var groupItems = groups.getOrDefault(key, new ArrayList<>());
			groupItems.add(item);
			groups.putIfAbsent(key, groupItems);
		}

		return groups;
	}

	public static void Run(ArrayList<FloorEvent> events) {
		var floors = GroupBy(events, event -> event.floor());

		var floorToSchedulerQueue = new LinkedBlockingQueue<FloorEvent>();
		var schedulerToFloorQueue = new LinkedBlockingQueue<Message>();
		var schedulerToElevatorQueue = new LinkedBlockingQueue<FloorEvent>();
		var elevatorToSchedulerQueue = new LinkedBlockingQueue<Message>();

		var f1 = new Floor(1, floorToSchedulerQueue, schedulerToFloorQueue, floors.getOrDefault(1, new ArrayList<>()));
		var f2 = new Floor(2, floorToSchedulerQueue, schedulerToFloorQueue, floors.getOrDefault(2, new ArrayList<>()));
		var f3 = new Floor(3, floorToSchedulerQueue, schedulerToFloorQueue, floors.getOrDefault(3, new ArrayList<>()));
		var s1 = new Scheduler(elevatorToSchedulerQueue, schedulerToFloorQueue, floorToSchedulerQueue, schedulerToElevatorQueue);
		var e1 = new Elevator(schedulerToElevatorQueue, elevatorToSchedulerQueue);

		var threads = new Thread[] {
			new Thread(f1, "floor_1"),
			new Thread(f2, "floor_2"),
			new Thread(f3, "floor_3"),
			new Thread(s1, "scheduler_1"),
			new Thread(e1, "elevator_1")
		};

		for (var thread : threads) {
			Logger.println(String.format("Starting '%s'", thread.getName()));
			thread.start();
		}


		// Wait for all threads to exit
		for (var thread : threads) {
			try {
				thread.join();
				Logger.println(String.format("Thread '%s' joined", thread.getName()));
			} catch (InterruptedException e) { }
		}
		Logger.println("All done.");
	}

	public static void main(String[] args) {

		Optional<InputStream> fileStream = Optional.empty();
		fileStream = Optional.of(new ByteArrayInputStream("14:05:15.0 2 up 4\n14:05:16.0 1 up 3\n14:05:17.0 3 down 2\n14:05:18.0 2 up 3".getBytes()));
		try {
			if (fileStream.isEmpty()) {
				fileStream = Optional.of(new FileInputStream(args[0]));
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return;
		}

		var floorReader = new FloorFormatReader(fileStream.get());
		Run(floorReader.toList());
	}

}
