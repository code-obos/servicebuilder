package no.obos.util.servicebuilder.util;

import no.obos.util.version.Version;
import no.obos.util.version.VersionUtil;

public class ApiVersionUtil {

    public static String getApiVersion(Class classOnLocalClassPath) {
        Version version = getVersion(classOnLocalClassPath);
        return version.getMajor() + "." + version.getMinor();
    }

    public static String getMajorVersion(Class classOnLocalClassPath) {
        Version version = getVersion(classOnLocalClassPath);
        return version.getMajor().toString();
    }

    public static String getMinorVersion(Class classOnLocalClassPath) {
        Version version = getVersion(classOnLocalClassPath);
        return version.getMinor().toString();
    }

    private static Version getVersion(Class classOnLocalClassPath) {
        return new VersionUtil(classOnLocalClassPath).getVersion();
    }

}
