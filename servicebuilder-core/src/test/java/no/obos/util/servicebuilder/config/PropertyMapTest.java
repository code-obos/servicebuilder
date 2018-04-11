package no.obos.util.servicebuilder.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PropertyMapTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void getResolvesRecursively_notFound_exception() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("b", "${a}456");

        assertThatThrownBy(() -> propertyMap.get("b")).hasMessageContaining("Missing value");
    }

    @Test
    public void getResolvesRecursively() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("a", "123")
                .put("b", "${a}456");

        assertThat(propertyMap.get("b")).isEqualTo("123456");
    }

    @Test
    public void getResolvesRecursively_multipleTimes() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("a", "123")
                .put("b", "${a}456")
                .put("c", "${b}789");

        assertThat(propertyMap.get("c")).isEqualTo("123456789");
    }

    @Test
    public void getResolvesProperties() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("b", "${a}def");

        System.setProperty("a", "abc");

        assertThat(propertyMap.get("b")).isEqualTo("abcdef");
    }

    @Test
    public void getResolvesProperties_multipleTimes() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("z", "${y}ghi");

        System.setProperty("x", "abc");
        System.setProperty("y", "${x}def");

        assertThat(propertyMap.get("z")).isEqualTo("abcdefghi");
    }

    @Test
    public void getResolvesProperties_blank() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("x", "${y}789");

        System.setProperty("y", "");

        assertThat(propertyMap.get("x")).isEqualTo("789");
    }

    @Test
    public void getResolvesEnvironment() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("b", "${a}456");

        environmentVariables.set("a", "123");

        assertThat(propertyMap.get("b")).isEqualTo("123456");
    }

    @Test
    public void getResolvesEnvironment_multipleTimes() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("z", "${y}789");

        environmentVariables.set("x", "123");
        environmentVariables.set("y", "${x}456");

        assertThat(propertyMap.get("z")).isEqualTo("123456789");
    }

    @Test
    public void getResolvesEnvironment_blank() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("x", "${y}789");

        environmentVariables.set("y", "");

        assertThat(propertyMap.get("x")).isEqualTo("789");
    }

    @Test
    public void getPrioritisesPropertyOverEnvironmentOverPropertyMap() {
        PropertyMap propertyMap = PropertyMap.empty
                .put("q", "m")
                .put("a", "abc")
                .put("b", "123");

        System.setProperty("q", "n");
        System.setProperty("a", "def");
        System.setProperty("c", "xyz");

        environmentVariables.set("q", "o");
        environmentVariables.set("b", "456");
        environmentVariables.set("c", "æøå");

        assertThat(propertyMap.get("q")).isEqualTo("n");
        assertThat(propertyMap.get("a")).isEqualTo("def");
        assertThat(propertyMap.get("b")).isEqualTo("456");
        assertThat(propertyMap.get("c")).isEqualTo("xyz");
    }

}
