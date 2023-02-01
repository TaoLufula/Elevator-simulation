package sysc3303_elevator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class FloorFormatReaderTest {

	@Test
	void testSimple() throws IOException {
		ByteArrayInputStream e = new ByteArrayInputStream("14:05:15.0 2 up 4\n".getBytes());
		assertEquals(new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4), (new FloorFormatReader(e)).next());
	}

	@Test

	}

	@Test
	void toList() throws IOException {
		ByteArrayInputStream e = new ByteArrayInputStream("14:05:15.0 2 up 4\n14:05:15.0 1 down 3".getBytes());
		var reader = new FloorFormatReader(e);
		assertArrayEquals(new FloorEvent[]{
			new FloorEvent(LocalTime.of(14, 5, 15, 0), 2, Direction.Up, 4),
			new FloorEvent(LocalTime.of(14, 5, 15, 0), 1, Direction.Down, 3),
		},
			reader.toList().toArray()
		);
	}

}
