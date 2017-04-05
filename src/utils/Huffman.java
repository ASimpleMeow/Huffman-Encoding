package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Huffman encoding and decoding is handled by this class.
 * 
 * My approach for this is to prepare the encoding to be
 * broken into a few parts.
 * 
 * - Magic Number 
 * - Number to signify the length of the instructions
 * for rebuilding the tree.
 * -Instructions for rebuilding the Huffman Tree
 * -The Huffman encoding itself
 * -A byte at the end to signify the end of the encoding.
 * 
 * @author Oleksandr Kononov
 * @version 31-03-2017
 *
 */
public class Huffman {
	/*
	* A a character to node map
	* Will be very useful for building a quick encoding library
	* for fast encoding
	*/
	Map<Character,Node> charNodes = new HashMap<Character,Node>();
	Node root;
	
	/**
	 * Constructor for the Huffman
	 * @param data String data to be encoded/decoded.
	 * @param encoding boolean to specify where the message is to be
	 * 		  encoded or decoded.
	 */
	public Huffman(String data, boolean encoding){
		if(encoding){
			prepareHuffmanQueue(data);
		}else{
			root = new Node("");
			buildHuffmanTree(data);
		}
	}
	
	
	/*
	 * 
	 * ENCODING PART OF THE HUFFMAN CLASS
	 * 
	 */
	
	
	/**
	 * Method to begin the Huffman Tree building process by
	 * separating the uncompressed String into characters
	 * and adding them to the priority queue.
	 * 
	 * @param uncompressed String data which is to be compressed, this will be
	 * 	used to create the Huffman Tree.
	 */
	private void prepareHuffmanQueue(String uncompressed){
		
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		
		/*
		 * Go through the whole String and fill out the
		 * charNode map incrementing the frequencies as
		 * necessary.
		 */
		for(char c : uncompressed.toCharArray()){
			if(charNodes.containsKey(c)){
				charNodes.get(c).freq++;
			}else{
				charNodes.put(c, new Node(String.valueOf(c)));
			}
		}
		
		
		//Add all the nodes to the priority queue
		for(Node node : charNodes.values()){
			queue.add(node);
		}
		
		buildHuffmanTree(queue);
	}
	
	/**
	 * Builds the Huffman Tree from the provided Priority Queue
	 * 
	 * @param queue Priority Queue with all nodes for the Huffman Tree
	 */
	private void buildHuffmanTree(PriorityQueue<Node> queue){
		
		//Continue until there is only one root left, the root
		while(queue.size()>1){
			Node left = queue.remove();	//Character with smallest frequency
			Node right = queue.remove();//Character with next smallest frequency
			Node parent = new Node("");//Parent for the two nodes
			parent.freq = left.freq + right.freq;
			parent.left = left;
			parent.right = right;
			
			left.parent = parent;
			right.parent = parent;
			
			queue.add(parent);
		}
		
		//Root is the final node in the queue
		root = queue.remove();
	}
	
	/**
	 * Encodes and formats the message using the 
	 * pre built Huffman Tree
	 * 
	 * @param data Uncompressed String data to be encoded
	 * @param magicNumber The HEX magic number for the Huffman Encoding
	 * @return byte array of the complete Huffman encoding
	 */
	public byte[] getEncoding(String data, int magicNumber){
		
		//Initialising local variables : 
		Map<Character,String> encodings = new HashMap<Character,String>();//Quick encoding library
		StringBuilder encoding = new StringBuilder();//Final encoding (without magicNumber)
		String hex = ""; // Will story the binary of magic number
		String instuctionNum = ""; //Will store the number for the length of tree instructions
		
		//Turn the hex to binary
	    hex = Integer.toBinaryString(magicNumber);
		
	    //Append the instructions for rebuilding the tree to the encoding
		encoding.append(traverseTree());
		
		/*
		 * instructionNum will hold the length of the instructions for rebuilding the
		 * Huffman Tree for decoding. Through testing using all ASCII character I can
		 * conclude that no more than 13 bits (8192 length) will be required.
		 */
		instuctionNum = Integer.toBinaryString(encoding.toString().length());
		while(instuctionNum.length() < 13) instuctionNum = "0"+instuctionNum;
		hex += instuctionNum;
		
		/*
		 * Build a quick encoding library for all characters.
		 * Therefore I don't need to find encodings for characters that I already
		 * encountered before
		 */
		for(char c : charNodes.keySet()){
			encodings.put(c, findCharRec(String.valueOf(c),"",root));
		}
		
		for(char c : data.toCharArray()){
			encoding.append(encodings.get(c));
		}
		
		return stringToBytes(hex+encoding.toString());
	}
	
	/**
	 * Converts the Huffman Encoding from it's binary String form
	 * to byte array form.
	 * 
	 * @param stringEncoding binary String form of the encoding
	 * @return byte array form of the encoding
	 */
	private byte[] stringToBytes(String stringEncoding){
		
		//Since I don't know the exact length, I used ArrayList
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		 
		//For the String encoding, in 7 length increments
		for(int i=0; i<stringEncoding.length();i=i+7){
			if(i+7 <= stringEncoding.length()){
				String temp = stringEncoding.substring(i,i+7);
				byte newByte = (byte)Integer.parseInt(temp,2);
				bytes.add(newByte);
				
				//If this is the last iteration, add final 0 byte
				if(i+8 > stringEncoding.length())
					 bytes.add((byte)0);
			}else{
				String temp = stringEncoding.substring(i,stringEncoding.length());
				byte counter = 0; //Will count how many zero are added to the end
				while(temp.length() < 7){
					temp+="0";
					counter++;
				}
				byte newByte = (byte)Integer.parseInt(temp,2);
				bytes.add(newByte);
				bytes.add(counter);
				break;
			}
		}
		
		//Converting from byte ArrayList to byte primitive array
		byte[] byteArray = new byte[bytes.size()];
		for(int i=0; i<bytes.size(); i++)
			byteArray[i] = bytes.get(i);
		 
		return byteArray;
	}
	
	
	/**
	 * Iteratively traverse the Huffman Tree from the root Node
	 * provided and encoded it into the encoding
	 * 
	 * @return The String encoding of how to rebuild this Huffman Tree.
	 */
	private String traverseTree(){
		
		Node current = this.root;
		String encoding = "";
		HashSet<Node> visited = new HashSet<Node>();
		
		/*
		 * The encoding works this way : 
		 * -Traverse the tree depth-wise and if the current node is not
		 * a leaf node, put a zero.
		 * -If the current node is a leaf node put a one and straight after the
		 * character binary
		 * -Do not visit visited nodes to avoid a loop
		 * -Once the root node had been visited twice, return the encoding
		 */
		while(true){
			if(current.isLeaf()){
				String binary = "";
				for(byte b : current.value.getBytes()) binary += b;
				binary = Integer.toBinaryString(Integer.valueOf(binary));
				if(binary.length() < 7) while(binary.length() < 7) binary = "0"+binary;
				encoding += "1"+binary;
				visited.add(current);
				if(current.parent != null) current = current.parent;
			}else{
				encoding += "0";
				
				if(current.left != null && !visited.contains(current.left)){
					current = current.left;
				}else if(current.right != null && !visited.contains(current.right)){
					current = current.right;
				}else{
					/*
					 * In the case I'm returning to the root, count the zero's added,
					 * and if I indeed returned to the root for the second time,
					 * remove these zero to save space (since they're useless)
					 */
					int counter = 0;
					while(current.parent != null){
						visited.add(current);
						current = current.parent;
						if(!visited.contains(current.right)) break;
						counter++;
						encoding += "0";
					}
					//If you returned to the root without finding a non visited right node
					if(current.parent == null){
						if(visited.contains(current)){
							//remove the useless zero's at the end
							encoding = encoding.substring(0, encoding.length()-counter);
							return encoding;
						}
						visited.add(current);
					}
				}
			}
		}
	}
	
	/**
	 * Recursively go through the Huffman Tree to find the
	 * character provided, recording the encoding to get to it
	 * in the process.
	 * 
	 * @param c The character I'm looking for.
	 * @param encoding The encoding for this character
	 * @param root The current node in my search
	 * @return The Huffman Encoding for this character
	 */
	private String findCharRec(String c,String encoding,Node root){
		
		if(root.isLeaf()) {
			return (root.value.equals(c))? encoding:null;
		}	
		
		String leftChild = findCharRec(c,encoding+"0",root.left); //go to left child
		String rightChild = findCharRec(c,encoding+"1",root.right); //go to right child
		
		return (leftChild == null)? rightChild : leftChild;
	}
	
	
	/*
	 * 
	 * DECODING PART OF THE HUFFMAN CLASS
	 * 
	 */
	
	/**
	 * Using the instructions data, rebuilds the Huffman Tree
	 * 
	 * @param instructions The encoded String instructions of how to rebuild the Huffman Tree
	 */
	private void buildHuffmanTree(String instructions){
		Node current = this.root;
		
		/*
		 * My approach for rebuilding the Huffman Tree is this:
		 * -If the current character from the instructions is zero then
		 * make an empty node and continue building the tree
		 * -If the current character is one then this a leaf node,
		 * build it, convert the next 7 bits to ASCII and place it in the node,
		 * return to the parent.
		 * 
		 * The tree is built depth first from left to right,
		 * so if the right node of the current node is null, go there,
		 * otherwise continue going back to the parent.
		 */
		while(instructions.length() > 1){
			
			if(instructions.charAt(0) == '0'){
				if(current.left == null){
					current.left = new Node("");
					current.left.parent = current;
					current = current.left;
					instructions = instructions.substring(1);
				}else if(current.right == null){
					current.right = new Node("");
					current.right.parent = current;
					current = current.right;
					instructions = instructions.substring(1);
				}else{
					while(current.parent != null){
						current = current.parent;
						instructions = instructions.substring(1);
						if(current.right == null) break;
					}
					//Stop since I've returned to the root and the right side is complete
					if(current.parent == null && current.right != null) return;
				}
			}else{
				current.value = String.valueOf((char) Integer.parseInt(instructions.substring(1,8),2));
				instructions = instructions.substring(8);
				current = current.parent;
			}
		}
	}
	
	/**
	 * Uses the built Huffman Tree to find the character with the encoding data,
	 * building up the decoded String from that
	 * 
	 * @param data Encoded String data
	 * @return A decoded String from the data
	 */
	public String getDecoding(String data){
		StringBuilder decoding = new StringBuilder();
		Node currentNode = root;
		
		/*
		 * For the duration of the data, iteratively go through the tree,
		 * if you found the leaf node, append it to the decoding and set
		 * the currentNode back to root.
		 */
		for(char c : data.toCharArray()){
			if(c == '1'){
				currentNode = currentNode.right;
			}else if (c == '0'){
				currentNode = currentNode.left;
			}
			if(!currentNode.value.isEmpty()){
				decoding.append(currentNode.value);
				currentNode = root;
			}
		}
		return decoding.toString();
	}
	
}

/**
 * A Node class for the use in Huffman Tree
 * 
 * @author Oleksandr Kononov
 */
class Node implements Comparable<Node>{
	String value;
	int freq;
	Node left;
	Node right;
	Node parent;
	
	public Node(String value){
		this.value = value;
		freq = 1;
		left = null;
		right = null;
		parent = null;
	}
	
	public boolean isLeaf(){
		return (left == null) && (right == null);
	}

	/**
	 * Compares this Node to another Node with their frequencies and the 
	 * length of their value
	 */
	@Override
	public int compareTo(Node other) {
		if(this.freq - other.freq < 0){
			if(this.value.length() - other.value.length() < 0) return -2;
			else return -1;
		}else if (this.freq - other.freq == 0){
			if(this.value.length() - other.value.length() < 0) return -1;
			else return 0;
		}else{
			return this.freq - other.freq;
		}
	}
}

