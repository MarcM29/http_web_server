package http_web_server;

	//Marc McCombe
	//CPS730 section 02
	//Assignment 1
	//Text resources used: 
		//Web protocols and practice (Krishnamurthy & Rexford) textbook
	//Online resources used:
		//https://developer.mozilla.org/en-US/docs/Learn/Common_questions/What_is_a_web_server
		//https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
		//https://www.rgagnon.com/javadetails/java-have-a-simple-http-server.html
		//https://www.ntu.edu.sg/home/ehchua/programming/webprogramming/HTTP_Basics.html
		//https://www.youtube.com/watch?v=rapwWK-oino
		//https://netbeez.net/blog/telnet-to-test-connectivity-to-tcp/
		//https://www.youtube.com/user/sylsau/featured
		//https://whatis.techtarget.com/definition/Web-server
		//https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
	//In class lecture material used
	//Date: 2020/02/05

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

	public class HTTP_SERVER implements Runnable{ 
		
		//DEFAULT HTTP SERVER DECLARATIONS
		//ipAddress = localhost;
		static File CONFIG_FILE = new File("myhttp.conf");
		//DEFAULT_PATH and version will be extracted from config file
		static File DEFAULT_PATH;
		static String version;
		//
		static final String DEFAULT_FILE = "index.html";
		static final String FILE_NOT_FOUND = "404.html";

		//Default port if no command line arguments are entered
		static int PORT = 61634;
		
		//Default socket setup
		private Socket connect;
		public HTTP_SERVER(Socket c) {
			connect = c;
		}
		
		public static void main(String[] args) {
			try {
				//Parses myhttp.conf to extract root path AND HTTP version
				  BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE)); 
				  String st; 
				  String[] path;
				  st = br.readLine();
				  path = st.split(" ");
				  DEFAULT_PATH = new File(path[1]);
				  version = path[0];
				//
				//If command line arguments are present then it assigns PORT to the entered port #
				try {
					PORT = Integer.parseInt(args[0]);
				}
				//If no command line arguments are present is leaves PORT variable alone
				catch(Exception e) { 
					System.out.println("No command line arguement received, default port 61,634 will be used");
				}
				//Opens server socket to listen on PORT
				ServerSocket serverConnect = new ServerSocket(PORT);
				System.out.println("Listening on port "+PORT);
				
				//Endlessly listens for requests until terminated
				while (true) {
					HTTP_SERVER myServer = new HTTP_SERVER(serverConnect.accept());
					Thread thread = new Thread(myServer);
					thread.start();
				}
				
				
			} catch (Exception e) {
				System.out.println("Error with input port");
				System.out.println("400 (bad request)");
				}
		}

		@Override
		public void run() {
			//Creates placeholder variables to be used in following try/catch block
			BufferedReader in = null; 
			PrintWriter out = null; 
			BufferedOutputStream dataOut = null;
			String fileRequested = null;
			
			try {
				//Creates reader, writer and an output stream for data
				in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
				out = new PrintWriter(connect.getOutputStream());
				dataOut = new BufferedOutputStream(connect.getOutputStream());
				
				//Reads first line from client request
				String input = in.readLine();
				//Parse this line to obtain the METHOD from client request (GET, HEAD, POST)
				StringTokenizer parse = new StringTokenizer(input);
				String method = parse.nextToken().toUpperCase();
				//Continues parsing to obtain the requested file name
				fileRequested = parse.nextToken().toLowerCase();	
				
				//If the HTTP request method is GET
				if (method.equals("GET")) {
					if (fileRequested.endsWith("/")) {
						fileRequested += DEFAULT_FILE;
					}
					//Ensures the requested file includes an extension
					if ((fileRequested.contains("."))) {
						//Checks if the requested file is one of the restricted file(s)
						if (!(fileRequested.equals("myhttp.conf"))) {
						//Assigns file variable to be equal to the requested file
						File file = new File(DEFAULT_PATH, fileRequested);
						//Obtains header info
						int fileLength = (int) file.length();
						String content = getContentType(fileRequested);
						byte[] fileData = readFileData(file, fileLength);
						//
						System.out.println("GET request detected, returning "+fileRequested);
						//Returns HTTP headers
						out.println(version +" 200 OK");
						out.println("Date: " + new Date());
						out.println("Content-type: " + content);
						out.println("Content-length: " + fileLength);
						out.println(); 
						//Must out.flush() otherwise connection will close before response is finished
						out.flush(); 
						//Returns requested file
						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
						System.out.println();
						}
						//If requested file is restricted (no read access)
						//Returns error code 403 and does not return file info
						else {
							System.out.println("Cannot read requested file");
				            System.out.println("403 (no read permissions)");
				            out.println("Cannot read requested file");
				            out.println("403 (no read permissions)");
				            out.flush();
				            System.out.println();
						}
					}
					//If there is no file extension included in fileRequested
					else {
						System.out.println("Error with file extension");
			            System.out.println("400 (bad request)");
			            out.println("Error with file extension");
			            out.println("400 (bad request)");
			            out.flush();
			            System.out.println();
					}
				}
				
				//If HTTP request method is HEAD
				else if(method.contentEquals("HEAD")) {
					if (fileRequested.endsWith("/")) {
						fileRequested += DEFAULT_FILE;
					}
					//Ensures the requested file includes an extension
					if ((fileRequested.contains("."))) {
						//Checks if the requested file is one of the restricted file(s)
						if (!(fileRequested.equals("myhttp.conf"))) {
						//Assigns file variable to be equal to the requested file
						File file = new File(DEFAULT_PATH, fileRequested);
						//Since we are only inspecting header field if the file does
						//not exist an exception will not be thrown(hence calling 404.html),
						//and blank headers will be returned. We fix this by a simple if 
						//statement checking to ensure file exists and if not it returns 404.html
						//If file does not exist
						if (!(file.exists())) {
							//System.out.println("Error with file name given: "+ fileRequested);
							//System.out.println("400 (bad request)");
							//out.println("Error with file name given: "+ fileRequested);
							//out.println("400 (bad request)");
							fileNotFound(out, dataOut, fileRequested);
							System.out.println();
						}
						//If file exists
						else {
						//Obtains header info
						int fileLength = (int) file.length();
						String content = getContentType(fileRequested);
						//Since method is HEAD we only care about headers
						System.out.println("HEAD request detected, returning header contents");
						out.println(version +" 200 OK");
						out.println("Date: " + new Date());
						out.println("Content-type: " + content);
						out.println("Content-length: " + fileLength);
						out.println();
						//Must out.flush() otherwise connection will close before response is finished
						out.flush();
						System.out.println();
						}
					}
						//If requested file is restricted (no read access)
						//Returns error code 403 and does not return file info
						else {
							System.out.println("Cannot read requested file");
				            System.out.println("403 (no read permissions)");
				            out.println("Cannot read requested file");
				            out.println("403 (no read permissions)");
				            out.flush();
				            System.out.println();
						}
					}
					//If there is no file extension included in fileRequested
					else {
						System.out.println("Error with file extension");
			            System.out.println("400 (bad request)");
			            out.println("Error with file extension");
			            out.println("400 (bad request)");
			            out.flush();
			            System.out.println();
					}
				}
				
				//If HTTP request method is POST
				else if(method.equals("POST")){
					int flag = 0;
					//Parses myhttp.conf to extract root path AND HTTP version
					BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE)); 
					  String st; 
					  String[] path;
					  st = br.readLine();
					  path = st.split(" ");
					 //
					String fileCreationPath = path[1];
					String[] tempFileRequested = fileRequested.split("\\.");
					File file = new File(fileCreationPath+fileRequested);
					//If file type is .html .htm or .txt then it creates document
					if(tempFileRequested[1].equals("html") || tempFileRequested[1].equals("htm")) {
						file = new File(fileCreationPath+tempFileRequested[0]+".html");
					}
					else if (tempFileRequested[1].equals("txt") || tempFileRequested[1].equals("text")) {
						file = new File(fileCreationPath+tempFileRequested[0]+".txt");
					}
					//Any other file types are not supported for creation (for the purpose of simplicity)
					else {
						System.out.println("Cannot create file of that type (try .html or .txt)");
			            System.out.println("400 (bad request)");
			            out.println("Cannot create file of that type (try .html or .txt)");
			            out.println("400 (bad request)");
			            out.flush();
			            System.out.println();
			            flag = 1;
					}
					//If a file with specified file name does not exist AND it is a supported file type
					if(!(file.exists()) && flag==0) {
						System.out.println("POST request detected, creating "+ fileRequested);
						try {
				            //File file = new File(fileCreationPath+fileRequested+".html");
				            file.createNewFile();
							out.println(version + " 201 File Created successfully");
							out.flush();
				        } catch (IOException e) {
				        	System.out.println("Error with creating requested file");
				            System.out.println("400 (bad request)");
				            out.println("Error with creating requested file");
				            out.println("400 (bad request)");
				            out.flush();
				        }
						System.out.println();
					}
					//If requestedFile already exists or file type not supported
					else {
						out.println("Cannot create file with specified name");
						out.flush();
						System.out.println("Cannot create file with specified name");
						System.out.println();
						flag=0;
					}
				}
				
				//If the method is not one of the 3 supported HTTP methods
				else {
					fileRequested = "not_supported.html";
					if (fileRequested.endsWith("/")) {
						fileRequested += DEFAULT_FILE;
					}
					//Assigns file variable to be equal to the requested file
					File file = new File(DEFAULT_PATH, fileRequested);
					//Obtains header info
					int fileLength = (int) file.length();
					String content = getContentType(fileRequested);
					byte[] fileData = readFileData(file, fileLength);
					//
					//Returns HTTP headers
					out.println(version +" 200 OK");
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); 
					//Must out.flush() otherwise connection will close before response is finished
					out.flush(); 
					//Returns requested file
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
					//Returns appropriate error code
					out.println();
					out.println("501 (Not implemented requests)");
					System.out.println("HTTP request not supported: "+ method);
					System.out.println("501 (Not implemented requests");
					//static final String METHOD_NOT_SUPPORTED = "not_supported.html";
					out.flush();
					System.out.println();
				}
				
			}
			//If file requested does not exist (GET or HEAD) method then catches
			//the exception and redirects the fileNotFound method
			catch (FileNotFoundException e) {
				try {
					fileNotFound(out, dataOut, fileRequested);
				} catch (Exception ex) {
					System.out.println("Error within fileNotFound method:" + e);
				}

			}
			//Catches any other exception thrown during run()
			catch (Exception e) {
				System.out.println("");
			}
			finally {
				try {
					//Close all potential resource leaks and connections
					in.close();
					out.close();
					dataOut.close();
					connect.close(); 
				} catch (Exception e) {
					System.out.println(e);
				} 
				System.out.println("Connection terminated");
			}
		}
		
		//Ensures the file requested is one of the two accepted types (text or html)
		private String getContentType(String fileRequested) {
			if (fileRequested.endsWith(".htm")||fileRequested.endsWith(".html"))
				return "text/html";
			else
				return "text/plain";
		}
		
		//Reads content of the requested file
		private byte[] readFileData(File file, int fileLength) throws IOException {
			FileInputStream fileIn = null;
			byte[] fileData = new byte[fileLength];
			try {
				fileIn = new FileInputStream(file);
				fileIn.read(fileData);
			} finally {
				if (fileIn != null) 
					fileIn.close();
			}
			return fileData;
		}

		
		//Handles the case where a file not found exception is thrown in run()
		private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
			File file = new File(DEFAULT_PATH, FILE_NOT_FOUND);
			int fileLength = (int) file.length();
			String content = "text/html";
			byte[] fileData = readFileData(file, fileLength);
			
			out.println(version + " 404 File Not Found");
			out.println("Content-type: " + content);
			out.println("Content-length: " + fileLength);
			out.println(); 
			out.flush(); 
			dataOut.write(fileData, 0, fileLength);
			dataOut.flush();
			
			System.out.println("Requested file not found: "+ fileRequested);
			System.out.println("404 (file does not exist)");
		}
		
	}

