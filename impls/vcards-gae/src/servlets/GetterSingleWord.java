package servlets;

//import com.google.appengine.repackaged.org.apache.http.annotation.NotThreadSafe;
import gae_store_space.AppInstance;
import gae_store_space.PageKind;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.NotThreadSafe;
import servlets.protocols.PathValue;
import servlets.protocols.WordDataValue;

import com.google.gson.Gson;

@NotThreadSafe
public class GetterSingleWord extends HttpServlet {
  private static final long serialVersionUID = -1906449812056035297L;
  
  private AppInstance app = AppInstance.getInstance(); 

	@Override
  public void doGet(
    HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException
  {

		String value = request.getParameter("arg0");
		
		if (value == null) 
			throw new IllegalArgumentException();
		
		PathValue path = new Gson().fromJson(value, PathValue.class);
		
    // path
		response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);
    
    PageKind p = app.getPage(path.pageName).get();
    WordDataValue v = p.getWordData(path.genName).get();
    
    String json = new Gson().toJson(v);
 
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(json);
  }
}