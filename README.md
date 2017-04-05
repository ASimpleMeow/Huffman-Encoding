# Huffman-Encoding
A program to demonstrate Huffman's encoding and decoding algorithm using a binary tree.
_Note: The characters I focused on are ASCII, UTF-8 is not supported_

##The approach I used for the algorithm : ##
	*Take in data from the user selected text file as a String
	*Using the data create a Huffman Binary Tree with the frequency at which the characters appear in the data
	*Traverse the Huffman Tree for all characters getting the encoding for them in the process.
	*Output the encoded data, which is composed out of the following parts:
		-A HEX number signifying Huffman Encoding
		-A 13 bit number which allows for Huffman Tree instructions to be extracted upon decoding
		-Huffman Tree instructions which, upon decoding will be used to rebuild the current Huffman Tree
		-The encoded data
		-A byte signifying the end of the Huffman encoding data
	*When decoding the file is selected by the user and is read in
	*The data is seperated into the above mentioned sections
	*The HEX number is compared to verify the file as being Huffman Encoded
	*The Huffman Tree is rebuilt from the instructions in the file
	*The data is decoded using the Huffman Tree and printed to the console
	
##Additional user statistics are provided in the console such as : ##
	*Time taken to compress
	*Uncompressed byte size
	*Compressed byte size
	*Time taken to decompress