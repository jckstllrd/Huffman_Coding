import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

class Node {
    char character;
    int freq;
    String code = "";
    Node left;
    Node right;

    public Node(char character, int freq) {
        this.character = character;
        setFreq(freq);
    }
    
    /**
     * A constructor for creating a Node only needing the frequency of the Node's char 
     * @param freq The frequency as an integer
     */
    public Node(int freq) {
        setFreq(freq);
    }
    
    /** 
     * A method to get the code of the Node
     * @return String Returns a code in string format
     */
    public String getCode() {
        return code;
    }
    /** 
     * A method to be able to set the code 
     * @param c Takes in the code as c
     */
    public void setCode(String c) {
        String newCode = c + code;
        this.code = newCode;
    }
    
    /** 
     * Allows the tree to add encryptions to all branches in a tree
     * @param c Takes in the new string value c
     */
    public void addEncryption(String c) {
         
        if (left == null && right == null) {
            this.code = c + getCode();
        }
        if(left != null) {
            left.addEncryption(c);
        }
        if(right != null){
            right.addEncryption(c);
        }
    }

    /**
     * A method just used to be able to see that the tree is working correctly
     */
    public void printTree() {
        if (left == null && right==null) {
            System.out.println("\n\nCharacter: " + getChar() + "\nFrequency: " + getFreq() + "\nCode: " + getCode());
        } else if (left!=null && right!=null) {
            left.printTree();
            right.printTree();
        }
    }
    
    /** 
     * A method for setting the frequency of the Node's char
     * @param freq The frequency of the char
     */
    public void setFreq(int freq) {
        this.freq = freq;
    }
    
    /** 
     * A getter for frequency
     * @return int Returns as an integer
     */
    public int getFreq() {
        return freq;
    }
    
    /** A setter for the left child Node
     * @param left The input of a node
     */
    public void setLeft(Node left) {
        this.left = left;
    }
    
    /** 
     * A setter for the right child Node
     * @param right Takes a Node as input
     */
    public void setRight(Node right) {
        this.right = right;
    }
    
    /** 
     * A getter for the char of the Node
     * @return character as a char
     */
    public char getChar() {
        return character;
    }
    
    /** 
     * Allows Nodes to be printed out when referenced
     * Unused for actual encoding
     * @return String Returns the string
     */
    public String toString() {
        return "Character: " + getChar() + "\nFrequency: " + getFreq() + "\nCode: " + getCode();
    }
}

class MyComparator implements Comparator<Node> {
    
    /**
     * A method just used to compare the sizes of each node for the queue
     * @param n1 The first node to compare
     * @param n2 Second node to compare
     * @return int The value returned to show sizes
     */
    public int compare(Node n1, Node n2) {
        return n1.getFreq() - n2.getFreq();
        }
}

public class Huffman {
    
    /** 
     * The constructor for Huffman class
     * Doesn't need parameters
     */
    private Huffman() {}
    
    /** 
     * A method for encoding a file
     * @param fileName Takes in the name of the file
     */
    void encodeFile(String fileName) {
        String fileString = "";
        try {
            StringBuffer fileData = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader("kingJamesBible.txt"));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            fileString = fileData.toString();
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<Character, Node> frequencies = characterCounter(fileString);
        PriorityQueue<Node> nodeQueue = makeQueue(frequencies);
        
        HashMap<Character, String> charCodes = new HashMap<Character, String>();
        Node tree = createTree(nodeQueue);
        charCodes = createCodeMap(tree, charCodes);
        
        convertToBinary(fileName, charCodes);
    }
    
    /** 
     * Allows me to create a copy of the original file after parsing to ensure the encoding is done on the desired file
     * @param filename Takes in the name of the file
     * @param fileOutput As well as the output of the file after being read to a string
     */
    public void createOriginal(String filename, String fileOutput) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename + "original.txt"));
            writer.write(fileOutput);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** 
     * The method to decode a file
     * @param filename Takes in the name of the file
     */
    public void decodeFile(String filename) {
        
        String file = "";
        try {
            byte[] allBytes = Files.readAllBytes(Paths.get(filename + ".bin"));
            file = GetString(allBytes);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        HashMap<Character, String> codes = null;
        try {
            FileInputStream fis = new FileInputStream(filename + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            codes = (HashMap<Character,String>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        String decodedString = "";
        String buffer = "";
        StringBuilder ds = new StringBuilder();
        char[] fileString = file.toCharArray();
        for (char c : fileString) {
            buffer += c;
            Boolean charDecoded = false;
            for (char d : codes.keySet()) {
                if (codes.get(d).equals(buffer)) {
                    ds.append(d);
                    buffer = "";
                    charDecoded = true;
                }
            }

        }
        decodedString = ds.toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename + ".txt"));
            writer.write(decodedString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    
    /** 
     * Converts a string of 1's and 0's into binary
     * @param s Takes in the string
     * @return byte[] Returns an array of bytes
     */
    static byte[] GetBinary(String s) {
        StringBuilder sBuilder = new StringBuilder(s);
        while (sBuilder.length() % 8 != 0) {
            sBuilder.append('0');
        }
        s = sBuilder.toString();

        byte[] data = new byte[s.length() / 8];

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '1') {
                data[i >> 3] |= 0x80 >> (i & 0x7);
            }
        }
        return data;
    }
    
    /** 
     * Allows you to convert an array of bytes back into a string
     * @param bytes The array of bytes
     * @return String The string which can now be decoded
     */
    static String GetString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
    
    /** 
     * Creates a priority queue for the Nodes
     * @param frequencies The hashmap of characters and their frequencies
     * @return PriorityQueue<Node> Returns a queue that can be used to create a tree
     */
    PriorityQueue<Node> makeQueue(HashMap<Character, Node> frequencies) {
        PriorityQueue<Node> nodeQueue = new PriorityQueue<Node>(frequencies.size(), new MyComparator());
        for (char c : frequencies.keySet()) {
            nodeQueue.add(new Node(c, frequencies.get(c).getFreq()));
        }
        return nodeQueue;
    }

    
    /** 
     * A method the creates a hashmap of all the occurrences of a character in a string
     * @param data The string that is used
     * @return HashMap<Character, Node> The hashmap containing all char keys and their freq values
     */
    HashMap<Character, Node> characterCounter(String data) {
        HashMap<Character, Node> characters = new HashMap<Character, Node>();

        char[] strArray = data.toCharArray();

        for (char c: strArray) {
            if (characters.containsKey(c)) {
                Node n = new Node(c, characters.get(c).getFreq() + 1);
                characters.put(c, n);
            }
            else {
                Node n = new Node(c, 1);
                characters.put(c, n);
            }
        }
        return characters;
    }
    
    /** 
     * A method that creates a Huffman tree
     * @param q The priority queue that is used
     * @return Node Returns the root node of the tree
     */
    Node createTree(PriorityQueue<Node> q) {
        while(q.size()>1) {
            Node n1 = q.poll();
            Node n2 = q.poll();
            int freq = n1.getFreq() + n2.getFreq();
            Node root = new Node(freq);
            root.setLeft(n1);
            n1.addEncryption("0");
            root.setRight(n2);
            n2.addEncryption("1");
            q.add(root);   
        }
        Node tree = q.poll();
        return tree;
    }
    
    /** 
     * The code map that is used to store all finalised char codes
     * @param root The Huffman tree
     * @param charCodes An empty hashmap
     * @return HashMap<Character, String> A populated hashmap
     */
    HashMap<Character,String> createCodeMap(Node root,HashMap<Character,String> charCodes) {
        if(root.left == null && root.right == null) {
            charCodes.put(root.getChar(), root.getCode());
        }
        if (root.left != null) {
            createCodeMap(root.left, charCodes);
        }
        if (root.right != null) {
            createCodeMap(root.right, charCodes);
        }
        return charCodes;
    }
    /** 
     * Converts the final file to a .bin file
     * @param filename The name of the original file
     * @param convertedFile The string of the file after reading
     * @param codes The hashmap of all the characters and their codes
     */
    public void convertToBinary(String filename, HashMap<Character, String> codes) {   
        String fileString = "";
        try {
            StringBuffer fileData = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(filename + ".txt"));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            fileString = fileData.toString();
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        char[] fileText =fileString.toCharArray();

        StringBuilder sb = new StringBuilder();
        for (char c : fileText) {
            sb.append(codes.get(c));
        }
        byte[] binary_write = GetBinary(sb.toString());
        
        try {
            OutputStream outputStream = new FileOutputStream(filename + "_compressed.bin");
            outputStream.write(binary_write);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(filename + "_compressed.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(codes);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    /** 
     * The main method which is used as an interface
     * @param args Takes in no arguments
     */
    public static void main(String[] args) {
        Huffman huff = new Huffman();
        System.out.println("Please enter the file you would like to use, followed by a + (compress) or a - (decompress)");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String[] inputs = input.split(" ");
        String filename = inputs[0];
        if (inputs[1].equals("+")) {
            double start = System.nanoTime();
            huff.encodeFile(filename);
            double finish = System.nanoTime();
            double timeElapsed = finish - start;
            System.out.println(timeElapsed/1000000000);
        } else if (inputs[1].equals("-")) {
            double start = System.nanoTime();
            huff.decodeFile(filename);
            double finish = System.nanoTime();
            double timeElapsed = finish - start;
            System.out.println(timeElapsed/1000000000);
        }
        sc.close();
    }
}


    