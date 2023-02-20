package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GenerateRouteServlet", urlPatterns = "/GenerateRouteServlet")
public class GenerateRouteServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final HttpServletRequest request, final HttpServletResponse response
    ) {
        // given wall ID
        // TODO: we are prototyping only for the MoonBoard wall so verify that the requested wall is the MoonBoard

        // run route generation to return list of holds and coordinates to be used in route:
        // - direct call to python script
        // - or call to tensorflow REST API / java tensorflow
        //   - however these will require additional proccessing which would have to be written on java side
        //   - => will just stick with python call
        
        // python route generation call: pass grade as stdin

        // returns comma separated list of coordinates (hold positions to be used)
        // TODO: we really need a whole restructuring of service classes so that Holds exist as children of wall not associated with routes

        // construct route from grade and holds
        // store route in database
    }

}
