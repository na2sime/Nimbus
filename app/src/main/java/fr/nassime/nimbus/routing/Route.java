package fr.nassime.nimbus.routing;

import com.sun.net.httpserver.HttpHandler;
import lombok.Getter;

import java.util.regex.Pattern;

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

    public boolean matches(String path) {
        return pattern.matcher(path).matches();
    }

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

    public String[] getParamNames() {
        java.util.regex.Matcher matcher = Pattern.compile("\\{([^/]+)\\}").matcher(rawPath);
        java.util.ArrayList<String> paramNames = new java.util.ArrayList<>();
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        return paramNames.toArray(new String[0]);
    }
}
