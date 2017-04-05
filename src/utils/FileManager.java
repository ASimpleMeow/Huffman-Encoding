package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Manages the input and outputs for files
 * 
 * @author Oleksandr Kononov
 * @version 27-04-2017
 *
 */
public class FileManager{
	
	/**
	 * Returns the selected files content as a String of data
	 * 
	 * @param forEncoding whether the file is going to be use for encoding or not
	 * @return The String data in the file
	 */
	public String readFileToString(boolean forEncoding){
		
		return (forEncoding)? readUnencodedFile() : readEncodedFile();
	}
	
	/**
	 * Reads in the file selected per byte and returns a String
	 * of binary which will be used for Huffman decoding.
	 * 
	 * @return String data of a Huffman encoded file
	 */
	private String readEncodedFile(){
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		StringBuilder sb; //Will be used to compose the binary String
		String result; //Final adjustments for the binary data and the output of the method
		
		//Handle exception if the file is not found or file selection cancelled
		try {
			fis = new FileInputStream(getFile(false));
		} catch (FileNotFoundException e) {
			System.out.println("File not found or cancelled\n");
		}
		
		bis = new BufferedInputStream(fis);
		sb = new StringBuilder();
		
		/*
		 * Read data by bytes from selected file, change it to it's binary form
		 * and format that binary form before adding to the StringBuilder
		 */
		byte b = 0;
		try {
			while((b =(byte) bis.read()) != -1){
				String binaryFormat = Integer.toString((int)b, 2);
				while(binaryFormat.length() < 7) binaryFormat = "0"+binaryFormat;
				sb.append(binaryFormat);
			}
		} catch (IOException e) {
			System.out.println("ERROR READING FILE!\n");
			e.printStackTrace();
		}	
		
		/*
		 * Remove the tailing zero's by using the last byte to inform us how many
		 * there were.
		 */
		result = sb.toString();
		String spacesString = result.substring(result.length()-7, result.length());
		int spaces = Integer.parseInt(spacesString,2);
		result = result.substring(0, result.length()-7-spaces);
		
		try {
			if(bis != null) bis.close();
			if(fis != null) fis.close();
		} catch (IOException e) {
			System.out.println("ERROR CLOSING BUFFERED INPUT READER!\n");
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the text of a selected file.
	 * 
	 * @return String data of a text file which is to be encoded by Huffman encoding
	 */
	private String readUnencodedFile(){
		InputStream is = null;;
		BufferedReader buf = null;
		String line; //A line of text from the file
		StringBuilder sb; //StringBuilder to build the complete data String
		
		try{
			is = new FileInputStream(getFile(true));
			buf = new BufferedReader(new InputStreamReader(is));
			        
			line = buf.readLine();
			sb = new StringBuilder();

			while(line != null){
			   sb.append(line);
			   sb.append("\n");
			   line = buf.readLine();
			}
			        
			String result = sb.toString();
			buf.close();
			is.close();
			return result;
		}catch(IOException e){
			System.out.println("ERROR I/O READING OF FILE\n");
			e.printStackTrace();
		}catch(NullPointerException e){
			System.out.println("File Selection Canceled\n");
		}catch(Exception e){
			System.out.println("Unexpected Error Has Occurred\n");
			e.printStackTrace();
		}finally{
			try{
		        if (buf != null) buf.close();
		        if (is != null) is.close();
		    }catch ( IOException e){}
		}
		return null;
	}
	
	/**
	 * Writes the bytes from the Huffman encoding to an output file
	 * 
	 * @param huffmanCode Byte array from the Huffman Encoding to be written
	 */
	public void writeFileToBytes(byte[] huffmanCode){
		BufferedOutputStream bos = null;
		try{
			bos = new BufferedOutputStream(new FileOutputStream("data/output.dat"));
			try{
				bos.write(huffmanCode);
				bos.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve the path of a file that will be encoded with Huffman.
	 * 
	 * @param forEncoding Specify whether the file will be used for encoding or decoding
	 * @return
	 */
	private String getFile(boolean forEncoding){
		
		String filePath = "";
		JFrame jf = new JFrame( "Please choose a file to compress" ); //Title for JFrame container
        jf.setAlwaysOnTop( true );
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        FileFilter filter = (forEncoding)? new FileNameExtensionFilter("Text Files","txt"):
        	new FileNameExtensionFilter("Dat File","dat");
        fileChooser.setFileFilter(filter);
        fileChooser.showOpenDialog( jf );
        try{
        	filePath = fileChooser.getSelectedFile().getPath();
        }catch(NullPointerException e){
        	jf.dispose();
        	return null;
        }
        jf.dispose();
        
        return filePath;
	}
}
