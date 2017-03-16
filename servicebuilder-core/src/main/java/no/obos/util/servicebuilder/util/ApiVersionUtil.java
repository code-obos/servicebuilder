package no.obos.util.servicebuilder.util;

import no.obos.util.version.Version;
import no.obos.util.version.VersionUtil;

public class ApiVersionUtil {
    public static String getApiVersion(Class classOnLocalClassPath) {
        Version version = new VersionUtil(classOnLocalClassPath).getVersion();
        return version.getMajor() + "." + version.getMinor();
    }


}
