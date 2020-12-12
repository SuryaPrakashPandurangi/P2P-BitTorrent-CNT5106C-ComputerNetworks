
import java.util.concurrent.atomic.AtomicBoolean;

public class ByteIOUtil {

	static byte[] mergeByteArraywithByte(byte[] destinationArray, byte byteToMerge) {

		byte[] mergedArrayWithBit = new byte[destinationArray.length + 1];
		System.arraycopy(destinationArray, 0, mergedArrayWithBit, 0, destinationArray.length);
		mergedArrayWithBit[destinationArray.length] = byteToMerge;

		return mergedArrayWithBit;
	}




	static byte[] convertIntegerToByteArray(int id) {

		byte[] byteArrayFinal = new byte[4];

		int BYTE_THREE  = 24;
		byteArrayFinal[0] = (byte) ((id >> 24) & 0xFF);
		int BYTE_TWO = 16;
		byteArrayFinal[1] = (byte) ((id >> 16) & 0xFF);
		int BYTE_ONE = 8;
		byteArrayFinal[2] = (byte) ((id >> 8) & 0xFF);
		byteArrayFinal[3] = (byte) (id & 0xFF);

		return byteArrayFinal;
	}

	static byte[] mergeTwoByteArrays(byte[] byteArrayOne, byte[] byteArrayTwo) {

		byte[] mergedArray = new byte[byteArrayOne.length + byteArrayTwo.length];

		System.arraycopy(byteArrayOne, 0, mergedArray, 0, byteArrayOne.length);
		System.arraycopy(byteArrayTwo, 0, mergedArray, byteArrayOne.length, byteArrayTwo.length);
		return mergedArray;

	}

	static int byteArrayToInteger(byte[] value) {

		int integerFour = ((value[0] & 0xFF) << 24);
		int integerThree = ((value[1] & 0xFF) << 16);
		int integerTwo = ((value[2] & 0xFF) << 8);
		int integerOne = (value[3] & 0xFF);

		int finalResult =  integerFour | integerThree | integerTwo | integerOne;
		return finalResult;

	}

	static byte[] convertBooleanDataToBytes(AtomicBoolean[] inputBooleans, byte[] responseBytes) {
		for (int ind = 0; ind < inputBooleans.length; ind++) {
			if (inputBooleans[ind].get()) {
				responseBytes[ind / 8] |= 1 << (7 - (ind % 8));
			}
		}
		return responseBytes;
	}

}