import java.io.*;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) throws Exception {
		
		if(args.length == 1) {
			// display file names on console
			for(int i=0; i<args.length; i++) {
				System.out.println("args["+ i +"] : " + args[i]);
			} 
						
			// open input and debug file
			String nameInFile = args[0];
			BufferedReader inFile = new BufferedReader(new FileReader(nameInFile));
			String nameDebugFile = nameInFile.replace(".txt", "_DeBug.txt");
			PrintWriter debugFile = new PrintWriter(new File(nameDebugFile));
			
			// HuffmanCoding object created
			HuffmanCoding x = new HuffmanCoding();
			
			// compute character counts and print to debugFile
			x.computeCharCounts(inFile);
			x.printCountAry(debugFile);
			inFile.close();
			
			// construct Huffman linked list and print to debugFile
			debugFile.println("LList during LList construction:");
			x.constructHuffmanLList(debugFile);
			
			// construct Huffman binary tree and print to debugFile
			debugFile.println("LList during BinTree construction:");
			x.constructHuffmanBinTree(debugFile);
			
			// construct character codes
			x.constructCharCode(x.getRoot(), "");
			// Step 6 in IV.Main of spec sheet
			debugFile.println("\nLList after constructCharCode:");
			x.printList(debugFile);
			
			// traversals
			debugFile.println("\nPreOrder Traversal:");
			x.preOrderTraversal(x.getRoot(), debugFile);
			debugFile.println("\nInOrder Traversal:");
			x.inOrderTraversal(x.getRoot(), debugFile);
			debugFile.println("\nPostOrder Traversal:");
			x.postOrderTraversal(x.getRoot(), debugFile);
			
			// close debug file
			debugFile.close();
			
			// user interface
			x.userInterface();
						
		}
		else {
			System.out.println("Invalid number of arguments.");
		}
		
	} // end main =======================================================================
	
	
	public static class HuffmanCoding extends BinaryTree {
		private static final int N = 256;
		private int[] charCountAry;
		private String[] charCode;
		
		// constructor
		// instantiate and initialize arrays
		public HuffmanCoding() {
			charCountAry = new int[N];
			charCode = new String[N];
			for(int i=0; i<N; i++) {
				charCountAry[i] = 0;
				charCode[i] = "";
			}
		} // end constructor
		
		public treeNode getRoot() {return root;};
		
		
		// compute character counts of given file
		public void computeCharCounts(BufferedReader inFile) throws Exception {
			int index;	
			while((index = inFile.read()) != -1) {
				charCountAry[index]++;
			}
		} // end computeCharCounts()
		
		
		// prints character counts to given file
		// for debug file readability ascii characters 'new line', 'carriage return', and 'space'
		// 	are represented as [n], [c], [s] respectively
		public void printCountAry(PrintWriter outFile) {
			for(int i=0; i<N; i++) {
				if(charCountAry[i] != 0) {
					if(i == 10) outFile.print("[n]");
					else if(i == 13) outFile.print("[c]");
					else if(i == 32) outFile.print("[s]");
					else outFile.print((char) i);
					outFile.write(" " + String.valueOf(charCountAry[i]) + "\n");
				}
			}
			outFile.println();
		} // end printCountAry()
		
		
		// constructs the huffman linked list from character counts
		// for debug file readability ascii characters 'new line', 'carriage return', and 'space'
		// 	are stored as [n], [c], [s] respectively
		public void constructHuffmanLList(PrintWriter outFile) {		
			for(int i=0; i<N; i++) {
				if(charCountAry[i] > 0) {
					String chr;
					if(i == 10) chr = "[n]";
					else if(i == 13) chr = "[c]";
					else if(i == 32) chr = "[s]";
					else chr = Character.toString((char)i);	// cast i to ascii char as String
					
					// create and insert node for character and its prob 
					int prob = charCountAry[i];
					treeNode newNode = new treeNode(chr, prob, "", null, null, null);
					insertNewNode(newNode);
					
					// print LList state at current iteration
					printList(outFile);
				}
			}
			outFile.println();
		} // end constructHuffmanLList()
		
		
		// constructs the huffman binary tree from LList
		public void constructHuffmanBinTree(PrintWriter outFile) {
			while(listHead.next.next != null) {
				// merge first two nodes of LList
				treeNode newNode = new treeNode(listHead.next.chStr + listHead.next.next.chStr,
												listHead.next.prob + listHead.next.next.prob,
												"",
												listHead.next,
												listHead.next.next,
												null);
				// insert newNode and move listHead
				insertNewNode(newNode);
				listHead.next = listHead.next.next.next;
				
				// print LList state at current iteration
				printList(outFile);
			}
			root = listHead.next;
		} // end constructHuffmanBinTree()
		
		
		// construct character codes from binary tree
		public void constructCharCode(treeNode T, String code) {
			if(isLeaf(T)) {
				T.code = code;
				
				// find index of T.chStr
				// if T.chStr is an ascii format character dummy ([n][c][s])
				//	then get index of actual character
				int index;
				if(T.chStr.equals("[n]")) index = (int) '\n';
				else if(T.chStr.equals("[c]")) index = (int) '\r';
				else if(T.chStr.equals("[s]")) index = (int) ' ';
				else index = (int) T.chStr.charAt(0);
				
				charCode[index] = code;
			} else {
				constructCharCode(T.left, code + "0");
				constructCharCode(T.right, code + "1");
			}
		} // end constructCharCode()
		
		
		// huffman encode given inFile to given outFile
		public void Encode(BufferedReader inFile, PrintWriter outFile) throws Exception{
			int index;
			while((index = inFile.read()) != -1) {
				String code = charCode[index];
				outFile.print(code);
			}
		} // end Encode()
		
		
		// decode given inFile to given outFile
		// read in bits one at a time, iterate through huffman binary tree
		// when leafnode is found, print corresponding character
		public void Decode(BufferedReader inFile, PrintWriter outFile) throws Exception {
			treeNode spot = root;
			int inChar;
			while((inChar = inFile.read()) != -1) {
				String oneBit = Character.toString((char)inChar);
				if(isLeaf(spot)) {
					// ascii format character dummy([n][c][s]) correction
					//  if spot.chStr is a dummy, print actual format character
					String temp = spot.chStr;
					if(temp.equals("[n]")) outFile.print("\n");
					else if(temp.equals("[c]")) outFile.print("\r");
					else if(temp.equals("[s]")) outFile.print(" ");
					else outFile.print(spot.chStr);
					spot = root;
				}
				if(oneBit.compareTo("0") == 0) {
					spot = spot.left;
				} else if(oneBit.compareTo("1") == 0 ) {
					spot = spot.right;
				} else {
					System.out.println("Error! The compress file contains invalid character!");
					System.exit(0);
				}
			}
			if(!isLeaf(spot)) System.out.println("Error:  The compress file is corrupted!");	
		} // end Decode()
		
		
		// user interface for file encoding
		// ask user they want to decode a file
		//   if yes, as for filename and encode/decode
		//	 else exit program
		public void userInterface() throws Exception{
			String nameOrg = "";
			String nameCompress;
			String nameDeCompress;
			char yesNo;
			Scanner scan = new Scanner(System.in);
			
			while(true) {
				System.out.print("Would you like to encode a file? (Y/N): ");
				yesNo = scan.nextLine().charAt(0);			
				if(yesNo == 'N' || yesNo == 'n') {
					System.out.println("Program terminated.");
					scan.close();
					System.exit(0);
				} else if(yesNo == 'Y' || yesNo == 'y'){
					System.out.print("Please enter name of file to be compressed: ");		
				} else {
					System.out.println("Invalid input.");
					continue;
				}
				
				try {
					nameOrg = scan.nextLine();
					
					BufferedReader orgFile = new BufferedReader(new FileReader(nameOrg));
					nameCompress = nameOrg.replace(".txt", "_Compressed.txt");
					nameDeCompress = nameOrg.replace(".txt", "_DeCompressed.txt");
					
					PrintWriter compFile = new PrintWriter(new File(nameCompress));
					PrintWriter deCompFile = new PrintWriter(new File(nameDeCompress));
					
					Encode(orgFile, compFile);
					
					compFile.close();
					BufferedReader compInFile = new BufferedReader(new FileReader(nameCompress));
					
					Decode(compInFile, deCompFile);
					
					orgFile.close();
					compInFile.close();
					deCompFile.close();
				} catch(FileNotFoundException e) {
					System.out.println("File not found.");
				}				
			}
			
		} // end userInterface()
		
	} // end class: HuffmanCoding =======================================================
	
	
	public static class BinaryTree extends linkedList{
		protected treeNode root;
		
		// constructor
		public BinaryTree() {/* empty */};
	
		
		// pre-order traversal of huffman binary tree
		public void preOrderTraversal(treeNode T, PrintWriter outFile) {
			if(isLeaf(T)) {
				T.printNode(outFile);
			} else {
				T.printNode(outFile);
				if(T.left != null) preOrderTraversal(T.left, outFile);
				if(T.right != null) preOrderTraversal(T.right, outFile);
			}
		} // end preOrderTraversal()
		
		
		// in-order traversal of huffman binary tree
		public void inOrderTraversal(treeNode T, PrintWriter outFile) {
			if(isLeaf(T)) {
				T.printNode(outFile);
			} else {
				if(T.left != null) inOrderTraversal(T.left, outFile);
				T.printNode(outFile);
				if(T.right != null) inOrderTraversal(T.right, outFile);
			}
		} // end inOrderTraversal()
		
		
		// post-order traversal of huffman binary tree
		public void postOrderTraversal(treeNode T, PrintWriter outFile) {
			if(isLeaf(T)) {
				T.printNode(outFile);
			} else {
				if(T.left != null) postOrderTraversal(T.left, outFile);
				if(T.right != null) postOrderTraversal(T.right, outFile);
				T.printNode(outFile);
			}
		} // end postOrderTraversal()
		
		
		// returns true ig given node is a leaf node, else false
		public boolean isLeaf(treeNode T) {
			if(T.left == null && T.right == null) return true;
			else return false;
		} // end isLeaf()
		
	} // end class: BinaryTree ==========================================================
	
	
	public static class linkedList {
		protected treeNode listHead;
		
		// constructor
		public linkedList() {
			listHead = new treeNode("dummy", 0, "", null, null, null);
		} // end constructor
		
		
		// insert given node into LList
		public void insertNewNode(treeNode newNode) {
			treeNode spot = findSpot(newNode);
			insert(spot, newNode);
		} // end insertNewNode()
		
		
		// returns spot where given now should be added
		public treeNode findSpot(treeNode newNode) {
			treeNode spot = listHead;
			while(spot.next != null && spot.next.prob < newNode.prob) {
				spot = spot.next;
			}
			return spot;
		} // end findSpot()
		
		
		// inserts newNode after spot
		public void insert(treeNode spot, treeNode newNode) {
			newNode.next = spot.next;
			spot.next = newNode;
		} // end insert()
		
		
		// print LList to given outFile
		public void printList(PrintWriter outFile) {			
			treeNode nav = listHead;	
			
			outFile.print("listHead--> ");
			while(nav.next != null) {
				outFile.print("(\"" + nav.chStr + "\", " + nav.prob + ", \"" +
					nav.next.chStr + "\")--> ");
				nav = nav.next;
			}
			outFile.print("(\"" + nav.chStr + "\", " + nav.prob +
					", NULL)--> NULL\n");
		} // end printList()
		
		
	} // end class: linkedList ==========================================================
		
		
	public static class treeNode {
		private String chStr;
		private int prob;
		private String code;
		protected treeNode left;
		protected treeNode right;
		protected treeNode next;		
		
		// constructor
		public treeNode(String s, int p, String c, treeNode l, treeNode r, treeNode n) {
			chStr = s;
			prob = p;
			code = c;
			left = l;
			right = r;
			next = n;
		} // end constructor
		
		
		// print this.treeNode data to given outFile
		public void printNode(PrintWriter outFile) {
			outFile.print("(\"" + chStr + "\", ");
			outFile.print(prob + ", ");
			if(next != null) outFile.print("\"" + next.chStr + "\", ");
			else outFile.print("NULL, ");
			if(left != null) outFile.print("\"" + left.chStr + "\", ");
			else outFile.print("NULL, ");
			if(right != null) outFile.print("\"" + right.chStr + "\")\n");
			else outFile.print("NULL)\n");
		} // end printNode()
		
			
	} // end class: treeNode ============================================================
			
	
} // end class: Main
