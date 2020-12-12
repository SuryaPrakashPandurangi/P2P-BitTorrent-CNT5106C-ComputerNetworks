
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class PeerMessageReader {

	public synchronized byte[] fetchTCPBitFieldPayload(InputStream dataStream) {
		byte[] peerClientBitField = new byte[0];
		try {
			byte[] payloadMessagelength = new byte[4];
			dataStream.read(payloadMessagelength);
			peerClientBitField = readBitfieldMsgPayload(dataStream, ByteIOUtil.byteArrayToInteger(payloadMessagelength) - 1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return peerClientBitField;
	}

	public byte[] fetchAndReadMessagePayload(InputStream inputStream, int payloadMessageLength) {
		byte[] messagePayloadAsBuyes = new byte[0];
		int payloadLengthToRead = payloadMessageLength;
		try {
			while (payloadLengthToRead != 0) {
				int streamDataAvailable = inputStream.available();
				int dataFinishedReading = 0;
				if (payloadMessageLength > streamDataAvailable) {
					dataFinishedReading = streamDataAvailable;
				} else {
					dataFinishedReading = payloadMessageLength;
				}

				byte[] byteDataFinishedReading = new byte[dataFinishedReading];
				if (dataFinishedReading != 0) {
					inputStream.read(byteDataFinishedReading);
					messagePayloadAsBuyes = ByteIOUtil.mergeTwoByteArrays(messagePayloadAsBuyes, byteDataFinishedReading);
					payloadLengthToRead = payloadLengthToRead - dataFinishedReading;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return messagePayloadAsBuyes;
	}

	public int requestPieceIndex(byte[] processBitfield, byte[] clientBitField, AtomicBoolean[] neededOrNotBitfield) {
		byte[] dataNeeded = new byte[processBitfield.length];
		byte[] mergeTempData = new byte[processBitfield.length];
		Arrays.fill(mergeTempData, (byte)0);
		byte[] requestBitField = ByteIOUtil.convertBooleanDataToBytes(neededOrNotBitfield, mergeTempData);
		byte[] dataAvailableAsBytes = new byte[processBitfield.length];
		List<Integer> neededListData = new ArrayList<Integer>();
		int i = 0;
		while (i < processBitfield.length) {
			dataAvailableAsBytes[i] = (byte) (processBitfield[i] & requestBitField[i]);
			dataNeeded[i] = (byte) ((dataAvailableAsBytes[i] ^ clientBitField[i]) & ~dataAvailableAsBytes[i]);
			if (dataNeeded[i] != 0){
				neededListData.add(i);
			}

			i++;
		}
		return fetchAndReturnPieceIndex(neededListData, dataNeeded);
	}
		

	
	public String fetchAndReadTCPHandShakeMessage(InputStream inputStream) {
		try {
			ishandShakeHeaderValid(inputStream);
			inputStream.read(new byte[10]);
			byte[] peerIdRead = new byte[4];
			inputStream.read(peerIdRead);
			return new String(peerIdRead);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return "";
	}

	public byte[] readBitfieldMsgPayload(InputStream inputStream, int bitFieldPayloadLength) throws IOException {

		byte[] clientDataBitField = new byte[bitFieldPayloadLength];
		byte[] type = new byte[1];
		inputStream.read(type);
		byte byteArrayInt = ByteIOUtil.convertIntegerToByteArray(PeerCommunicationMessageType.BITFIELD.ordinal())[3];
		if(type[0] == byteArrayInt) {
			inputStream.read(clientDataBitField);
		}
		return clientDataBitField;
	}
	
	public void ishandShakeHeaderValid(InputStream inputStream) throws IOException {
		byte[] ipHeaderArray = new byte[18];
		String HEADER = "P2PFILESHARINGPROJ";
		inputStream.read(ipHeaderArray);
		if (!(new String(ipHeaderArray).equals(HEADER)))
			throw new RuntimeException("Header Mismatch");
	}


	
	public int generateRandomInteger(int integerToConvert) {
		return new Random().nextInt(integerToConvert);
	}
	
	public int generateRandomSet(byte messageForRandomSet) {
		int indexOfMessage = generateRandomInteger(8);
		int i = 0;
		while (i < 8) {
			if ((messageForRandomSet & (1 << i)) != 0) {
				indexOfMessage = i;
				break;
			}
			i++;
		}
		return indexOfMessage;
	}

	public boolean doSendInterestedMessage(byte[] peerClientProcessBitField, byte[] peerProcessClientBitField) {
		byte isTheByteDataSet;
		int beginningIndex = 0;
		while (beginningIndex < peerClientProcessBitField.length) {
			isTheByteDataSet = (byte) (~peerClientProcessBitField[beginningIndex] & peerProcessClientBitField[beginningIndex]);
			if (isTheByteDataSet != 0) {
				return true;
			}
			beginningIndex++;
		}
		return false;
	}

	public int fetchAndReturnPieceIndex(List<Integer> pieceList, byte[] indexNeeded) {
		if(pieceList.isEmpty()){
			return -1;
		}
		int indexOfByteData = pieceList.get(generateRandomInteger(pieceList.size()));
		byte randomindexNeededAsByte = indexNeeded[indexOfByteData];
		int bitDataIndex = generateRandomSet(randomindexNeededAsByte);
		return (indexOfByteData*8) + (7-bitDataIndex);
	}
	


}