
import java.util.concurrent.atomic.AtomicBoolean;

public class ByteArrayUtil {

	static byte[] mergeByteArraywithByte(byte[] destinationArray, byte byteToMerge) {
		byte[] mergedArrayWithBit = new byte[destinationArray.length + 1];

		System.arraycopy(destinationArray, 0, mergedArrayWithBit, 0, destinationArray.length);
		mergedArrayWithBit[destinationArray.length] = byteToMerge;

		return mergedArrayWithBit;

	}

	static byte[] mergeTwoByteArrays(byte[] arrayOne, byte[] arrayTwo) {

		byte[] mergedArray = new byte[arrayOne.length + arrayTwo.length];

		System.arraycopy(arrayOne, 0, mergedArray, 0, arrayOne.length);

		System.arraycopy(arrayTwo, 0, mergedArray, arrayOne.length, arrayTwo.length);

		return mergedArray;

	}

	static byte[] mergeBooleanArraytoByteArray(AtomicBoolean[] booleanArray, byte[] byteArray) {
		for (int ind = 0; ind < booleanArray.length; ind++) {
			if (booleanArray[ind].get()) {
				byteArray[ind / 8] |= 1 << (7 - (ind % 8));
			}
		}
		return byteArray;
	}

	static byte[] convertIntegerToByteArray(int id) {

		byte[] conv = new byte[4];

		int BYTE_ONE = 8;
		int BYTE_TWO = 16;
		int BYTE_THREE  = 24;

		conv[0] = (byte) ((id >> BYTE_THREE) & 0xFF);
		conv[1] = (byte) ((id >> BYTE_TWO) & 0xFF);
		conv[2] = (byte) ((id >> BYTE_ONE) & 0xFF);
		conv[3] = (byte) (id & 0xFF);

		return conv;
	}

	static int convertByteArrayToInteger(byte[] value) {
		int BYTE_ONE = 8;
		int BYTE_TWO = 16;
		int BYTE_THREE  = 24;

		int conv0 = ((value[0] & 0xFF) << BYTE_THREE);
		int conv1 = ((value[1] & 0xFF) << BYTE_TWO);
		int conv2 = ((value[2] & 0xFF) << BYTE_ONE);
		int conv3 = (value[3] & 0xFF);

		return conv0 | conv1 | conv2 | conv3;
	}

}