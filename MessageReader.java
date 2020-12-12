
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class MessageReader{

	public synchronized byte[] fetchTCPBitfieldPayload(InputStream inputDataStream) {
		byte[] clientTCPBitField = new byte[0];
		try {
			int byteIndex = 4;
			byte[] messageDataLen = new byte[4];
			inputDataStream.read(messageDataLen);
			clientTCPBitField = readAndFetchBitfieldPayload(inputDataStream, ByteArrayUtil.convertByteArrayToInteger(messageDataLen) - 1);
		} catch (IOException ioException) {
			System.err.println("Exception Occured while Fetching Bitfield Payload: "+ioException.getMessage());
			ioException.printStackTrace();
		}
		return clientTCPBitField;
	}
	

	public int fetchIndexOfPiece(byte[] processBitfield, byte[] clientBitField, AtomicBoolean[] bitFieldNeeded) {
		byte[] dataNeeded = new byte[processBitfield.length];
		byte[] tempForMerge = new byte[processBitfield.length];
		int fillVal = 0;

		Arrays.fill(tempForMerge, (byte)0);

		byte[] requestBitFieldAsBytes = ByteArrayUtil.mergeBooleanArraytoByteArray(bitFieldNeeded, tempForMerge);
		byte[] availableBytes = new byte[processBitfield.length];
		List<Integer> listOfAvailableBytes = new ArrayList<Integer>();
		int i = 0;

		while (i < processBitfield.length) {
			availableBytes[i] = (byte) (processBitfield[i] & requestBitFieldAsBytes[i]);
			dataNeeded[i] = (byte) ((availableBytes[i] ^ clientBitField[i]) & ~availableBytes[i]);

			if (dataNeeded[i] != 0){
				listOfAvailableBytes.add(i);
			}

			i++;
		}
		return fetchPieceIndex(listOfAvailableBytes, dataNeeded);
	}
		
	public byte[] readAndfetchMessagePayload(InputStream ins, int payloadLength) {
		byte[] payloadMessage = new byte[0];
		int lengthOfthePayload = payloadLength;
		try {
			while (lengthOfthePayload != 0) {
				int availableBytesFromStream = ins.available();
				int readData = 0;
				if (payloadLength > availableBytesFromStream) {
					readData = availableBytesFromStream;
				}else{
					readData = payloadLength;
				}

				byte[] dataToInsert = new byte[readData];
				if (readData != 0) {
					ins.read(dataToInsert);
					payloadMessage = ByteArrayUtil.mergeTwoByteArrays(payloadMessage, dataToInsert);
					lengthOfthePayload = lengthOfthePayload - readData;
				}
			}
		} catch (IOException io) {
			io.printStackTrace();
		}
		return payloadMessage;
	}
	
	public String fetchTCPHasMessage(InputStream inputStream) {
		try {
			isHandShakeHeaderValid(inputStream);
			inputStream.read(new byte[10]);
			byte[] peerId = new byte[4];
			inputStream.read(peerId);
			return new String(peerId);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		String empty = "";
		return "";
	}
	
	public void isHandShakeHeaderValid(InputStream inputStrem) throws IOException {
		byte[] inputHeader = new byte[18];
		inputStrem.read(inputHeader);

		if (!("P2PFILESHARINGPROJ".equals(new String(inputHeader)))){
			throw new RuntimeException("Header Mismatch");
		}
	}

	public boolean isPeerInterested(byte[] peerProcessBitField, byte[] peerClientBitField) {
		byte peerInterestInBytes;
		int startingIndex = 0;

		while (startingIndex < peerProcessBitField.length) {
			peerInterestInBytes = (byte) (~peerProcessBitField[startingIndex] & peerClientBitField[startingIndex]);

			if (peerInterestInBytes != 0) {
				return true;
			}
			startingIndex++;
		}

		return false;
	}
	
	public int generateRandomInteger(int high) {
		return new Random().nextInt(high);
	}
	
	public int generateRandomSet(byte msg) {
		int bitInd = generateRandomInteger(8);
		int i = 0;
		while (i < 8) {
			if ((msg & (1 << i)) != 0) {
				bitInd = i;
				break;
			}
			i++;
		}
		return bitInd;
	}
	
	public int fetchPieceIndex(List<Integer> listToFetchFrom, byte[] byteArrayNeeded) {
		if(listToFetchFrom.isEmpty()){
			return -1;
		}

		int indexOfByte = listToFetchFrom.get(generateRandomInteger(listToFetchFrom.size()));
		byte byteFromArray = byteArrayNeeded[indexOfByte];
		int bitIndexAsInt = generateRandomSet(byteFromArray);

		return (indexOfByte*8) + (7-bitIndexAsInt);
	}
	
	public byte[] readAndFetchBitfieldPayload(InputStream inputStream, int length) throws IOException {
		byte[] clientBitFieldPayload = new byte[length];
		byte[] streamType = new byte[1];

		inputStream.read(streamType);

		byte convertedArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.BITFIELD.ordinal())[3];

		if(streamType[0] == convertedArray){
			inputStream.read(clientBitFieldPayload);
		}

		return clientBitFieldPayload;
	}

}