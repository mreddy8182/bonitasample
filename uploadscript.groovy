import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.search.SearchOptions;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.*;

ProcessAPI processApi = apiAccessor.getProcessAPI();

List<Document> docList = processApi.getDocumentList(processInstanceId, "myMultipleDocuments",0,100);

for (doc in docList) {

	print doc.getContentFileName()

	String url = "http://localhost:38080/UploadServlet30/UploadServlet";
	String charset = "UTF-8";
	String param = "value";
	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
	String CRLF = "\r\n"; // Line separator required by multipart/form-data.

	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	connection.setDoOutput(true);
	connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	connection.setRequestMethod("POST");

	try{
		
		OutputStream output = connection.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

		byte[] conBytes = processApi.getDocumentContent(doc.getContentStorageId());
		
		writer.append("--" + boundary).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + doc.getContentFileName() + "\"").append(CRLF);
		writer.append("Content-Type: " + doc.getContentMimeType()).append(CRLF);
		writer.append("Content-Transfer-Encoding: binary").append(CRLF);
		writer.append(CRLF).flush();
		output.write(conBytes);
		output.flush(); // Important before continuing with writer!
		writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

		// End of multipart/form-data.
		writer.append("--" + boundary + "--").append(CRLF).flush();

	}catch(Exception e){
		print e
	}

	// Request is lazily fired whenever you need to obtain information about response.
	int responseCode = ((HttpURLConnection) connection).getResponseCode();
	println responseCode; // Should be 200

}

return null;
