# Steps for program execution:

1. You can either enter a desired port number as a command line argument or leave it blank and it will use the default port number
2. Once program is run the server is operational and awaiting HTTP requests
3. Open command prompt and telnet localhost
4. Now you can send GET, HEAD or POST requests in the format shown below (.html or .txt): GET filename.html or filename.txt HEAD filename.html or filename.txt POST filename.html or filename.txt -All files are of type .html or .txt for simplicity sake(when creating files using POST you can use .html .htm .text .txt but upon creation it will convert .htm to .html AND .text to .txt for simplicity so all HTML files have same extension .html and all TEXT files have same extension .txt)
5. The program will return the HTTP response data as well as associated status code/info
6. The connection will be terminated and a new connection must be opened to send another request (non-persistent)

# How it works:

1. The program initializes the HTTP version and root path via the config file myhttp.conf
2. Once the program is run it checks for command line arguements (port #) if none are present it uses the default port #
3. Program then waits for an HTTP request
4. Once an HTTP request is received it determines the HTTP method (GET, HEAD or POST)
5. Ensures a file extension is included in fileRequested (if not returns error code 400
6. Once the HTTP method is identified it proceeds to handle the request appropriately
7. If the HTTP method is none of the 3 supported methods it returns a NOT_SUPPORTED.html, error code (501) and terminates connection
8. GET method will return the contents of the desired file as well as the appropriate header data (version, status code(200), date, content-type, content-length). If the file requested does not exist, responds with 404.index and error code.
9. HEAD method will just return the header data (version, status code(200), date, content-type, content-length) of the desired file. If the file requested does not exist, responds with 404.index and error code.
10. POST method will first check if a file with the input name already exists, if so it terminates connection. Otherwise it will create the desired file of type .html .htm .txt .text although for simplicity sake .htm files are converted to .html and .text files are converted to .txt files (so all files of same type have exact same extension). If the input file extension is not one of the supported file types it will return error code 400, terminated the connection and not proceed with creating the file. The POST method creates an empty file with the desired name and type (txt or html) and returns status code 201 upon completion.
© 2020 GitHub, Inc.
