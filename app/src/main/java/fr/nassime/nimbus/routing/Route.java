package fr.nassime.nimbus.routing;

import com.sun.net.httpserver.HttpHandler;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * The Route class represents a mapping between an HTTP request path and a corresponding handler.
 * It supports path parameter extraction from dynamic segments and checks if a given path matches
 * the route's pattern.
 *
 * A route is defined by a raw path with optional dynamic segments specified within curly braces,
 * which are converted into regular expressions for matching purposes.
 */
@Getter
public class Route {

    private final Pattern pattern;
    private final HttpHandler handler;
    private final String rawPath;

    public Route(String path, HttpHandler handler) {
        this.rawPath = path;
        String regex = path.replaceAll("\\{([^/]+)\\}", "([^/]+)");
        this.pattern = Pattern.compile("^" + regex + "$");
        this.handler = handler;
    }

    /**
     * Checks if the given path matches the predefined pattern for the route.
     *
     * @param path the HTTP request path to be matched against the route's pattern
     * @return true if the provided path matches the route's pattern, false otherwise
     */
    public boolean matches(String path) {
        return pattern.matcher(path).matches();
    }

    /**
     * Extracts path parameters from the given path based on the route's pattern.
     *
     * @param path the request path to extract parameters from, which should match the route's pattern
     * @return an array of path parameters if the path matches the pattern; an empty array otherwise
     */
    public String[] extractPathParams(String path) {
        java.util.regex.Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            String[] params = new String[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                params[i] = matcher.group(i + 1);
            }
            return params;
        }
        return new String[0];
    }

    /**
     * Extracts the names of the path parameters defined in the raw path of the route.
     * Path parameters are enclosed in curly braces within the route's raw path.
     *
     * @return an array of parameter names found in the raw path, or an empty array
     * if no parameters are defined.
     */
    public String[] getParamNames() {
        java.util.regex.Matcher matcher = Pattern.compile("\\{([^/]+)\\}").matcher(rawPath);
        java.util.ArrayList<String> paramNames = new java.util.ArrayList<>();
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        return paramNames.toArray(new String[0]);
    }
}
