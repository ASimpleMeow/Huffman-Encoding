package controllers;


import java.util.Scanner;

import utils.FileManager;
import utils.Huffman;

/**
 * The main class from which the program is run.
 * 
 * Here the user is about to interact with the program via the menu,
 * which allows for Compression of a file, Decompression of a file
 * and exit.
 * 
 * @author Oleksandr Kononov
 * @version 20-04-2017
 *
 */
public class Main {
	
	private Scanner in;
	private FileManager fm;
	private final int MAGIC_NUMBER = 0x0CADD099; //Magic number for the Huffman Encoding
	
	public static void main(String[] args){
		new Main().run();
	}
	
	public void run(){
		in = new Scanner(System.in);
		fm = new FileManager();
		runMenu();
	}
	
	/**
	 * The functional part of the menu, here is where the user controls what
	 * the user wants.
	 */
	public void runMenu(){
		int userChoice = showMenu();
		
		while(userChoice != 0){
			switch(userChoice){
			case 1:
				compressFile();
				break;
			case 2:
				decompressFile();
				break;
			default:
				System.out.println("INVALID COMMAND!");
				break;
			}
			in.nextLine(); //SCANNER BUG FIX
			userChoice = showMenu();
		}
	}
	
	/**
	 * Display the menu options to the user and take input from the user.
	 * 
	 * @return The choice the user makes
	 */
	private int showMenu(){
		System.out.println("1) Select a file to compress");
		System.out.println("2) Select a file to decompress");
		System.out.println("0) EXIT");
		System.out.print(">> ");
		try{
			return in.nextInt();
		}catch(Exception e){
			return -1;
		}
	}
	
	/**
	 * Carry out the steps to compress a file of the users choice and write it to file.
	 */
	private void compressFile(){
		long start = -1; //Start time for the compression process
		long finish = -1; //Finish time for the compression process
		float uncompressedSize = -1f; //The size in bytes of the original data
		float compressedSize = -1f; //The size in bytes of the compressed data
		float compressionPercentage = -1f; //The compression percentage (Na if not applicable)
		
		
		start = System.currentTimeMillis();
		
		/*
		 * -Take in user selected file
		 * -Compress/Encode the String
		 * -Write the byte array to a file
		 */
		String uncompressedString = fm.readFileToString(true);
		if(uncompressedString == null) return;
		Huffman huffman = new Huffman(uncompressedString,true);
		byte[] compressedEncoding = huffman.getEncoding(uncompressedString,MAGIC_NUMBER);
		fm.writeFileToBytes(compressedEncoding);
		
		//Prepare statistics for the user
		finish = System.currentTimeMillis();
		uncompressedSize = uncompressedString.length();
		compressedSize = compressedEncoding.length;
		compressionPercentage = (1 - (compressedSize / uncompressedSize))*100;
		
		//Output statistics for the user
		System.out.println("Time taken (miliseconds): "+(finish-start));
		System.out.println("Uncompressed Bytes Size : "+uncompressedSize);
		System.out.println("Compressed Bytes Size : "+compressedSize);
		System.out.println("Compression Percentage : "+((uncompressedSize<compressedSize)?"Na":compressionPercentage+"%"));
	}
	
	/**
	 * Carry out steps to decompress a file of the users choice and output it to console.
	 */
	private void decompressFile(){
		long start= -1; //Start time for the decompression process
		long finish = -1; //Finish time for the decompression process
		
		start = System.currentTimeMillis();
		
		//Take in compressed/encoded data from user selected file
		String compressedString = fm.readFileToString(false);
		if(compressedString == null) return;
		
		//Verify the compressed file with magic number
		String hexCheck = Integer.toBinaryString(MAGIC_NUMBER);
		if(!compressedString.substring(0,hexCheck.length()).equals(hexCheck)){
			System.out.println("ERROR! Not a huffman compressed file!");
			return;
		}
		int indexValue = Integer.parseInt(compressedString.substring(28,41),2);
		
		// Decompress/Decode data
		Huffman huffman = new Huffman(compressedString.substring(41,41+indexValue),false);
		String uncompressedString = huffman.getDecoding(compressedString.substring(41+indexValue));
		
		//Prepare statistics for the user
		finish = System.currentTimeMillis();
		
		//Output decoded data and statistics for the user
		System.out.println("\n"+uncompressedString);
		System.out.println("Time taken (miliseconds): "+(finish-start));
	}
}
