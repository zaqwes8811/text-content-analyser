package read_chain.web_wrapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import info_core_accessors.FabricImmutableNodeControllersImpl;
import info_core_accessors.ImmutableNodeAccessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.checkthread.annotations.NotThreadSafe;
import through_functional.configurator.GlobalConfigurator;

import java.io.IOException;

@NotThreadSafe
public class ServletGetterOnWordData extends HttpServlet {
  private ContainerNodeControllers CONTAINER_;
  private final String PATH_TO_CONFIGURATION_FILE_ = "my.yaml";
  @Override
  public void doGet(
  		HttpServletRequest request,
  		HttpServletResponse response) throws ServletException, IOException
  	{
  	
  	try {
      if(null == CONTAINER_) {
        BuilderControllers builder = new BuilderControllers();
        ImmutableSet<String> namesNodes =
            new GlobalConfigurator(PATH_TO_CONFIGURATION_FILE_).getRegisteredNodes().get();
        ImmutableList<ImmutableNodeAccessor> controllers = builder.createControllersForAllNodes(
            namesNodes, new FabricImmutableNodeControllersImpl());

        CONTAINER_ = new ContainerNodeControllers(controllers);
      }
  	} catch (Exception e) {
      // Вот проблемы создают эти проверяемые исключения
  	}

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    // TODO: Сделать декоратор для класса
    // TODO: Это хардкод!
    Integer idxNode = 0;
    String jsonResponse = new Gson().toJson(CONTAINER_.getPerWordData(idxNode));

    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(jsonResponse);
  }
}